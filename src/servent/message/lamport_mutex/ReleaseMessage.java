package servent.message.lamport_mutex;

import servent.message.BasicMessage;
import servent.message.MessageType;

public class ReleaseMessage extends BasicMessage {

    private static final long serialVersionUID = 140067794958060524L;

    private int finalReceiverId;

    public ReleaseMessage(int senderPort, int receiverPort, String senderIpAddress, String receiverIpAddress,
                          int clock, int finalReceiverId) {
        super(MessageType.RELEASE, senderPort, receiverPort, senderIpAddress, receiverIpAddress);
        setClock(clock);
        this.finalReceiverId = finalReceiverId;
    }

    public int getFinalReceiverId() { return finalReceiverId; }

    @Override
    public boolean isFifo() {
        return true;
    }
}
