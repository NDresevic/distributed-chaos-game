package servent.message.lamport_mutex;

import servent.message.BasicMessage;
import servent.message.MessageType;

public class AckIdleMessage extends BasicMessage {

    private static final long serialVersionUID = -7960576368579868536L;

    private int finalReceiverId;

    public AckIdleMessage(int senderPort, int receiverPort, String senderIpAddress, String receiverIpAddress,
                          int finalReceiverId) {
        super(MessageType.ACK_IDLE, senderPort, receiverPort, senderIpAddress, receiverIpAddress);
        this.finalReceiverId = finalReceiverId;
    }

    public int getFinalReceiverId() { return finalReceiverId; }
}
