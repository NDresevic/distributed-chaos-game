package servent.message;

import java.util.Map;

public class TellStatusMessage extends BasicMessage {

    private static final long serialVersionUID = 2057311911123502064L;

    // [jobName -> [fractalId -> number of drawn points]]
    Map<String, Map<String, Integer>> resultMap;
    private int version;

    public TellStatusMessage(int senderPort, int receiverPort, String senderIpAddress, String receiverIpAddress,
                             Map<String, Map<String, Integer>> resultMap, int version) {
        super(MessageType.TELL_STATUS, senderPort, receiverPort, senderIpAddress, receiverIpAddress);
        this.resultMap = resultMap;
        this.version = version;
    }

    public Map<String, Map<String, Integer>> getResultMap() {
        return resultMap;
    }

    public int getVersion() {
        return version;
    }
}
