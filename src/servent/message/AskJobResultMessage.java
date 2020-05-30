package servent.message;

import app.models.Point;

import java.util.ArrayList;
import java.util.List;

public class AskJobResultMessage extends BasicMessage {

    private static final long serialVersionUID = -2961633943823731472L;

    private String jobName;
    private int lastServentId;
    private List<Point> comuptedPoints;

    public AskJobResultMessage(int senderPort, int receiverPort, String senderIpAddress, String receiverIpAddress,
                               String jobName, int lastServentId) {
        super(MessageType.ASK_JOB_RESULT, senderPort, receiverPort, senderIpAddress, receiverIpAddress);
        this.jobName = jobName;
        this.lastServentId = lastServentId;
        this.comuptedPoints = new ArrayList<>();
    }

    public AskJobResultMessage(int senderPort, int receiverPort, String senderIpAddress, String receiverIpAddress,
                               String jobName, int lastServentId, List<Point> comuptedPoints) {
        super(MessageType.ASK_JOB_RESULT, senderPort, receiverPort, senderIpAddress, receiverIpAddress);
        this.lastServentId = lastServentId;
        this.jobName = jobName;
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
}
