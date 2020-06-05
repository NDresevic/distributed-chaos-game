package servent.handler.lamport_mutex;

import app.AppConfig;
import app.models.ServentInfo;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.lamport_mutex.AckIdleMessage;
import servent.message.util.MessageUtil;

public class AckIdleHandler implements MessageHandler {

    private Message clientMessage;
    private AckIdleMessage ackIdleMessage;

    private int receiverId;

    public AckIdleHandler(Message clientMessage) {
        this.clientMessage = clientMessage;

        ackIdleMessage = (AckIdleMessage) clientMessage;
        receiverId = ackIdleMessage.getFinalReceiverId();
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() != MessageType.ACK_IDLE) {
            AppConfig.timestampedErrorPrint("Ack idle handler got a message that is not ACK_IDLE");
            return;
        }

        if (receiverId != AppConfig.myServentInfo.getId()) {    // I am not intended receiver, forward message
            this.forwardMessage();
            return;
        }

        AppConfig.chordState.getReceivedAckMessagesCount().getAndIncrement();
    }

    private void forwardMessage() {
        ServentInfo intercessorServent = AppConfig.chordState.getNextNodeForServentId(receiverId);

        AckIdleMessage aim = new AckIdleMessage(ackIdleMessage.getSenderPort(), intercessorServent.getListenerPort(),
                ackIdleMessage.getSenderIpAddress(), intercessorServent.getIpAddress(), receiverId);
        MessageUtil.sendMessage(aim);
    }
}
