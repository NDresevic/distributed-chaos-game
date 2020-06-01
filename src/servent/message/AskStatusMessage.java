package servent.message;

import java.util.HashMap;
import java.util.Map;

public class AskStatusMessage extends BasicMessage {

    private static final long serialVersionUID = -109881785085443973L;

    private String jobName;
    private String fractalId;
    // [jobName -> [fractalId -> number of drawn points]]
    Map<String, Map<String, Integer>> resultMap;
    private int finalReceiverId;

    public AskStatusMessage(int senderPort, int receiverPort, String senderIpAddress, String receiverIpAddress,
                            int finalReceiverId, String jobName, String fractalId) {
        super(MessageType.ASK_STATUS, senderPort, receiverPort, senderIpAddress, receiverIpAddress);
        this.finalReceiverId = finalReceiverId;
        this.jobName = jobName;
        this.fractalId = fractalId;
        this.resultMap = new HashMap<>();
        this.resultMap.put(jobName, new HashMap<>());
    }

    public AskStatusMessage(int senderPort, int receiverPort, String senderIpAddress, String receiverIpAddress,
                            int finalReceiverId, String jobName, String fractalId, Map<String, Map<String, Integer>> resultMap) {
        super(MessageType.ASK_STATUS, senderPort, receiverPort, senderIpAddress, receiverIpAddress);
        this.finalReceiverId = finalReceiverId;
        this.jobName = jobName;
        this.fractalId = fractalId;
        this.resultMap = resultMap;
    }

    public AskStatusMessage(int senderPort, int receiverPort, String senderIpAddress, String receiverIpAddress,
                            int finalReceiverId, Map<String, Map<String, Integer>> resultMap) {
        super(MessageType.ASK_STATUS, senderPort, receiverPort, senderIpAddress, receiverIpAddress);
        this.finalReceiverId = finalReceiverId;
        this.resultMap = resultMap;
    }

    public AskStatusMessage(int senderPort, int receiverPort, String senderIpAddress, String receiverIpAddress,
                            int finalReceiverId, String jobName) {
        super(MessageType.ASK_STATUS, senderPort, receiverPort, senderIpAddress, receiverIpAddress);
        this.finalReceiverId = finalReceiverId;
        this.jobName= jobName;
        this.resultMap = new HashMap<>();
        this.resultMap.put(jobName, new HashMap<>());
    }

    public AskStatusMessage(int senderPort, int receiverPort, String senderIpAddress, String receiverIpAddress,
                            int finalReceiverId, String jobName, Map<String, Map<String, Integer>> resultMap) {
        super(MessageType.ASK_STATUS, senderPort, receiverPort, senderIpAddress, receiverIpAddress);
        this.finalReceiverId = finalReceiverId;
        this.jobName= jobName;
        this.resultMap = resultMap;
    }

    public AskStatusMessage(int senderPort, int receiverPort, String senderIpAddress, String receiverIpAddress,
                            int finalReceiverId) {
        super(MessageType.ASK_STATUS, senderPort, receiverPort, senderIpAddress, receiverIpAddress);
        this.finalReceiverId = finalReceiverId;
        this.resultMap = new HashMap<>();
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
}
