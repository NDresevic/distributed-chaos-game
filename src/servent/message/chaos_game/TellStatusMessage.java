package servent.message.chaos_game;

import servent.message.BasicMessage;
import servent.message.MessageType;

import java.util.Map;

public class TellStatusMessage extends BasicMessage {

    private static final long serialVersionUID = 2057311911123502064L;

    // [jobName -> [fractalId -> number of drawn points]]
    Map<String, Map<String, Integer>> resultMap;
    private int version;
    private int finalReceiverId;

    public TellStatusMessage(int senderPort, int receiverPort, String senderIpAddress, String receiverIpAddress,
                             int finalReceiverId, Map<String, Map<String, Integer>> resultMap, int version) {
        super(MessageType.TELL_STATUS, senderPort, receiverPort, senderIpAddress, receiverIpAddress);
        this.finalReceiverId = finalReceiverId;
        this.resultMap = resultMap;
        this.version = version;
    }

    public int getFinalReceiverId() {
        return finalReceiverId;
    }

    public Map<String, Map<String, Integer>> getResultMap() {
        return resultMap;
    }

    public int getVersion() {
        return version;
    }
}
