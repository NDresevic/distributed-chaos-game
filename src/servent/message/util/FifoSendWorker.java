package servent.message.util;

import app.AppConfig;
import app.Cancellable;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.chord.PoisonMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

/**
 * We will have as many instances of these workers as we have servents. Each of them reads messages from a queue and
 * sends them via a simple socket. The thread waits for an ACK on the same socket before sending another message to the
 * same servent.
 *
 * These threads are stopped via {@link PoisonMessage}.
 */
public class FifoSendWorker implements Runnable, Cancellable {

    private int serventId;

    public FifoSendWorker(int serventId) {
        this.serventId = serventId;
    }

    @Override
    public void run() {
        MessageUtil.initializePendingMessages();

        while (true) {
            try {
                try {
                    Thread.sleep((long)(Math.random() * 1000) + 500);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }

                Message messageToSend = MessageUtil.pendingMessages.get(serventId).poll(200, TimeUnit.MILLISECONDS);

                if (messageToSend == null) { continue; }

                if (MessageUtil.MESSAGE_UTIL_PRINTING) {
                    AppConfig.timestampedStandardPrint("Sending message " + messageToSend);
                }

                if (messageToSend.getMessageType() == MessageType.POISON) { break; }

                String ipAddress = messageToSend.getReceiverIpAddress();
                int port = messageToSend.getReceiverPort();

                Socket sendSocket = new Socket(ipAddress, port);
                ObjectOutputStream oos = new ObjectOutputStream(sendSocket.getOutputStream());
                oos.writeObject(messageToSend);
                oos.flush();

                ObjectInputStream ois = new ObjectInputStream(sendSocket.getInputStream());
                String ackString = (String)ois.readObject();
                if (!ackString.equals("ACK")) {
                    AppConfig.timestampedErrorPrint("Got response which is not an ACK");
                }

                sendSocket.close();
            } catch (SocketTimeoutException ignored) {

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void stop() {
        try {
            MessageUtil.pendingMessages.get(serventId).put(new PoisonMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setServentId(int serventId) { this.serventId = serventId; }

    @Override
    public String toString() {
        return "FifoSendWorker{" +
                "serventId=" + serventId +
                '}';
    }
}
