package servent.message.lamport_mutex;

import servent.message.BasicMessage;
import servent.message.MessageType;

public class ReplyMessage extends BasicMessage {

    private static final long serialVersionUID = 2426559763180786872L;

    private int finalReceiverId;

    public ReplyMessage(int senderPort, int receiverPort, String senderIpAddress, String receiverIpAddress,
                        int clock, int finalReceiverId) {
        super(MessageType.REPLY, senderPort, receiverPort, senderIpAddress, receiverIpAddress);
        this.setClock(clock);
        this.finalReceiverId = finalReceiverId;
    }

    public int getFinalReceiverId() { return finalReceiverId; }

    @Override
    public boolean isFifo() {
        return true;
    }
}
