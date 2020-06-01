package servent.message;

import app.models.FractalIdJob;

import java.util.Map;

public class IdleMessage extends BasicMessage {

    private static final long serialVersionUID = 7591006261115609523L;

    private Map<Integer, FractalIdJob> serventJobsMap;
    private int finalReceiverId;

    public IdleMessage(int senderPort, int receiverPort, String senderIpAddress, String receiverIpAddress,
                       Map<Integer, FractalIdJob> serventJobsMap, int finalReceiverId) {
        super(MessageType.IDLE, senderPort, receiverPort, senderIpAddress, receiverIpAddress);
        this.serventJobsMap = serventJobsMap;
        this.finalReceiverId = finalReceiverId;
    }

    public Map<Integer, FractalIdJob> getServentJobsMap() { return serventJobsMap; }

    public int getFinalReceiverId() { return finalReceiverId; }
}
