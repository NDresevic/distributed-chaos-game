package servent.handler.lamport_mutex;

import app.AppConfig;
import app.models.ServentInfo;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.lamport_mutex.AckJobExecutionMessage;
import servent.message.util.MessageUtil;

public class AckJobExecutionHandler implements MessageHandler {

    private Message clientMessage;
    private AckJobExecutionMessage ackJobExecutionMessage;

    private int receiverId;

    public AckJobExecutionHandler(Message clientMessage) {
        this.clientMessage = clientMessage;

        ackJobExecutionMessage = (AckJobExecutionMessage) clientMessage;
        receiverId = ackJobExecutionMessage.getFinalReceiverId();
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() != MessageType.ACK_JOB_EXECUTION) {
            AppConfig.timestampedErrorPrint("Ack job execution got a message that is not ACK_JOB_EXECUTION");
            return;
        }

        if (receiverId != AppConfig.myServentInfo.getId()) {    // I am not intended receiver, forward message
            this.forwardMessage();
            return;
        }

        int senderId = AppConfig.chordState.getNodeIdForServentPortAndAddress(ackJobExecutionMessage.getSenderPort(),
                ackJobExecutionMessage.getSenderIpAddress());
        AppConfig.timestampedStandardPrint("Acknowledge message received from: " + senderId);
        AppConfig.chordState.getReceivedAckMessagesCount().getAndIncrement();
    }

    private void forwardMessage() {
        ServentInfo intercessorServent = AppConfig.chordState.getNextNodeForServentId(receiverId);

        AckJobExecutionMessage ajem = new AckJobExecutionMessage(ackJobExecutionMessage.getSenderPort(),
                intercessorServent.getListenerPort(), ackJobExecutionMessage.getSenderIpAddress(),
                intercessorServent.getIpAddress(), receiverId);
        MessageUtil.sendMessage(ajem);
    }
}
