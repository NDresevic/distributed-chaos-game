package servent.message.chaos_game;

import servent.message.BasicMessage;
import servent.message.MessageType;

import java.util.HashMap;
import java.util.Map;

public class AskStatusMessage extends BasicMessage {

    private static final long serialVersionUID = -109881785085443973L;

    private String jobName;
    private String fractalId;
    private int version;
    // [jobName -> [fractalId -> number of drawn points]]
    Map<String, Map<String, Integer>> resultMap;
    private int finalReceiverId;

    public AskStatusMessage(int senderPort, int receiverPort, String senderIpAddress, String receiverIpAddress,
                            int finalReceiverId, String jobName, String fractalId, Map<String, Map<String, Integer>> resultMap,
                            int version) {
        super(MessageType.ASK_STATUS, senderPort, receiverPort, senderIpAddress, receiverIpAddress);
        this.finalReceiverId = finalReceiverId;
        this.jobName = jobName;
        this.fractalId = fractalId;
        this.resultMap = resultMap;
        this.version = version;
    }

    public AskStatusMessage(int senderPort, int receiverPort, String senderIpAddress, String receiverIpAddress,
                            int finalReceiverId, String jobName, String fractalId, int version) {
        this(senderPort, receiverPort, senderIpAddress, receiverIpAddress, finalReceiverId, jobName, fractalId,
                new HashMap<>(), version);
    }

    public AskStatusMessage(int senderPort, int receiverPort, String senderIpAddress, String receiverIpAddress,
                            int finalReceiverId, Map<String, Map<String, Integer>> resultMap, int version) {
        this(senderPort, receiverPort, senderIpAddress, receiverIpAddress, finalReceiverId, "", "",
                resultMap, version);
    }

    public AskStatusMessage(int senderPort, int receiverPort, String senderIpAddress, String receiverIpAddress,
                            int finalReceiverId, String jobName, int version) {
        this(senderPort, receiverPort, senderIpAddress, receiverIpAddress, finalReceiverId, jobName, "",
                new HashMap<>(), version);
    }

    public AskStatusMessage(int senderPort, int receiverPort, String senderIpAddress, String receiverIpAddress,
                            int finalReceiverId, String jobName, Map<String, Map<String, Integer>> resultMap,
                            int version) {
        this(senderPort, receiverPort, senderIpAddress, receiverIpAddress, finalReceiverId, jobName, "",
                resultMap, version);
    }

    public AskStatusMessage(int senderPort, int receiverPort, String senderIpAddress, String receiverIpAddress,
                            int finalReceiverId, int version) {
        this(senderPort, receiverPort, senderIpAddress, receiverIpAddress, finalReceiverId, "", "",
                new HashMap<>(), version);
    }

    public int getFinalReceiverId() {
        return finalReceiverId;
    }

    public String getJobName() {
        return jobName;
    }

    public String getFractalId() {
        return fractalId;
    }

    public Map<String, Map<String, Integer>> getResultMap() {
        return resultMap;
    }

    public int getVersion() {
        return version;
    }
}
