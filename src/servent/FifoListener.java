package servent;

import app.AppConfig;
import app.Cancellable;
import app.models.ServentInfo;
import servent.message.Message;
import servent.message.lamport_mutex.ReleaseMessage;
import servent.message.lamport_mutex.ReplyMessage;
import servent.message.lamport_mutex.RequestMessage;
import servent.message.util.MessageUtil;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class FifoListener implements Runnable, Cancellable {

    private volatile boolean working = true;

    public FifoListener() { }

    @Override
    public void run() {
        ServerSocket listenerSocket = null;
        try {
            listenerSocket = new ServerSocket(AppConfig.myServentInfo.getFifoListenerPort(), 100);
            /*
             * If there is no connection after 1s, wake up and see if we should terminate.
             */
            listenerSocket.setSoTimeout(1000);
            AppConfig.timestampedStandardPrint("Fifo listener started on port: " + AppConfig.myServentInfo.getFifoListenerPort());
        } catch (IOException e) {
            AppConfig.timestampedErrorPrint("Couldn't open fifo listener socket on: " + AppConfig.myServentInfo.getFifoListenerPort());
            System.exit(0);
        }

        while (working) {
            try {
                Message clientMessage;

                Socket clientSocket = listenerSocket.accept();
                clientMessage = MessageUtil.readMessage(clientSocket);

                switch (clientMessage.getMessageType()) {
                    case REQUEST:
//                        AppConfig.timestampedStandardPrint("request handler uso");
                        RequestMessage requestMessage = (RequestMessage) clientMessage;
                        int receiverId = requestMessage.getFinalReceiverId();
                        int clock = requestMessage.getClock();

                        if (receiverId != AppConfig.myServentInfo.getId()) {    // forward if not mine
//                            AppConfig.timestampedStandardPrint("prosledjivanje request");
                            ServentInfo intercessorServent = AppConfig.chordState.getNextNodeForServentId(receiverId);

                            RequestMessage rm = new RequestMessage(requestMessage.getSenderPort(),
                                    intercessorServent.getFifoListenerPort(), requestMessage.getSenderIpAddress(),
                                    intercessorServent.getIpAddress(), clock, receiverId);
                            MessageUtil.sendMessage(rm);
                        } else {
//                            AppConfig.timestampedStandardPrint("pre hendl request");
                            this.handleRequestMessage(requestMessage);
                        }

                        break;
                    case REPLY:
//                        AppConfig.timestampedStandardPrint("reply handler uso");
                        ReplyMessage replyMessage = (ReplyMessage) clientMessage;
                        receiverId = replyMessage.getFinalReceiverId();
                        clock = replyMessage.getClock();

                        if (receiverId != AppConfig.myServentInfo.getId()) {    // forward if not mine
//                            AppConfig.timestampedStandardPrint("prosledjivanje reply");
                            ServentInfo intercessorServent = AppConfig.chordState.getNextNodeForServentId(receiverId);

                            ReplyMessage rm = new ReplyMessage(replyMessage.getSenderPort(),
                                    intercessorServent.getFifoListenerPort(), replyMessage.getSenderIpAddress(),
                                    intercessorServent.getIpAddress(), clock, receiverId);
                            MessageUtil.sendMessage(rm);
                        } else {
//                            AppConfig.timestampedStandardPrint("pre hendl reply");
                            this.handleReplyMessage(replyMessage);
                        }

                        break;
                    case RELEASE:
//                        AppConfig.timestampedStandardPrint("release handler uso");
                        ReleaseMessage releaseMessage = (ReleaseMessage) clientMessage;
                        receiverId = releaseMessage.getFinalReceiverId();
                        clock = releaseMessage.getClock();

                        if (receiverId != AppConfig.myServentInfo.getId()) {    // forward if not mine
//                            AppConfig.timestampedStandardPrint("prosledjivanje release");
                            ServentInfo intercessorServent = AppConfig.chordState.getNextNodeForServentId(receiverId);

                            ReleaseMessage rm = new ReleaseMessage(releaseMessage.getSenderPort(),
                                    intercessorServent.getFifoListenerPort(), releaseMessage.getSenderIpAddress(),
                                    intercessorServent.getIpAddress(), clock, receiverId);
                            MessageUtil.sendMessage(rm);
                        } else {
//                            AppConfig.timestampedStandardPrint("pre hendl release");
                            this.handleReleaseMessage(releaseMessage);
                        }

                        break;
                }

            } catch (SocketTimeoutException timeoutEx) {
                //Uncomment the next line to see that we are waking up every second.
//				AppConfig.timedStandardPrint("Waiting...");
            } catch (Exception e) {
                AppConfig.timestampedErrorPrint(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void stop() {
        this.working = false;
    }

    private void handleRequestMessage(RequestMessage requestMessage) {
        AppConfig.lamportClock.messageEvent(requestMessage.getClock());

        // add to request list
        AppConfig.lamportMutex.addMessageRequest(requestMessage);

        AppConfig.lamportClock.localEvent();
        // send reply message
        int receiverId = AppConfig.chordState.getNodeIdForFifoListenerPortAndAddress(requestMessage.getSenderPort(),
                requestMessage.getSenderIpAddress());
        ServentInfo intercessorServent = AppConfig.chordState.getNextNodeForServentId(receiverId);

        ReplyMessage replyMessage = new ReplyMessage(AppConfig.myServentInfo.getFifoListenerPort(),
                intercessorServent.getFifoListenerPort(), AppConfig.myServentInfo.getIpAddress(),
                intercessorServent.getIpAddress(), AppConfig.lamportClock.getClock(), receiverId);
        MessageUtil.sendMessage(replyMessage);
    }

    private void handleReplyMessage(ReplyMessage replyMessage) {
        AppConfig.lamportClock.messageEvent(replyMessage.getClock());

        // update that I received a reply from servent
        AppConfig.lamportMutex.replyMessageEvent(replyMessage);
    }

    private void handleReleaseMessage(ReleaseMessage releaseMessage) {
        AppConfig.lamportClock.messageEvent(releaseMessage.getClock());

        // remove from request list
        AppConfig.lamportMutex.releaseMessageEvent(releaseMessage);
    }
}
