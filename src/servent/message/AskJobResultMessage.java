package servent.message;

import app.models.Point;

import java.util.ArrayList;
import java.util.List;

public class AskJobResultMessage extends BasicMessage {

    private static final long serialVersionUID = -2961633943823731472L;

    private String jobName;
    private int lastServentId;
    private List<Point> comuptedPoints;
    private int finalReceiverId;

    public AskJobResultMessage(int senderPort, int receiverPort, String senderIpAddress, String receiverIpAddress,
                               String jobName, int lastServentId, int finalReceiverId) {
        super(MessageType.ASK_JOB_RESULT, senderPort, receiverPort, senderIpAddress, receiverIpAddress);
        this.jobName = jobName;
        this.lastServentId = lastServentId;
        this.finalReceiverId = finalReceiverId;
        this.comuptedPoints = new ArrayList<>();
    }

    public AskJobResultMessage(int senderPort, int receiverPort, String senderIpAddress, String receiverIpAddress,
                               String jobName, int lastServentId, int finalReceiverId, List<Point> comuptedPoints) {
        super(MessageType.ASK_JOB_RESULT, senderPort, receiverPort, senderIpAddress, receiverIpAddress);
        this.lastServentId = lastServentId;
        this.jobName = jobName;
        this.finalReceiverId = finalReceiverId;
        this.comuptedPoints = comuptedPoints;
    }

    public String getJobName() {
        return jobName;
    }

    public int getLastServentId() {
        return lastServentId;
    }

    public List<Point> getComuptedPoints() {
        return comuptedPoints;
    }

    public int getFinalReceiverId() {
        return finalReceiverId;
    }
}
