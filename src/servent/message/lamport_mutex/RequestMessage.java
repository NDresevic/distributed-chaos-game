package servent.message.lamport_mutex;

import servent.message.BasicMessage;
import servent.message.MessageType;

public class RequestMessage extends BasicMessage {

    private static final long serialVersionUID = 3942224912541171214L;

    private int finalReceiverId;

    public RequestMessage(int senderPort, int receiverPort, String senderIpAddress, String receiverIpAddress,
                          int clock, int finalReceiverId) {
        super(MessageType.REQUEST, senderPort, receiverPort, senderIpAddress, receiverIpAddress);
        this.setClock(clock);
        this.finalReceiverId = finalReceiverId;
    }

    public int getFinalReceiverId() { return finalReceiverId; }

    @Override
    public boolean isFifo() {
        return true;
    }
}
