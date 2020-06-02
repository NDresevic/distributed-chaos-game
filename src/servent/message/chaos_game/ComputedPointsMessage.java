package servent.message.chaos_game;

import app.models.Point;
import servent.message.BasicMessage;
import servent.message.MessageType;

import java.util.List;

public class ComputedPointsMessage extends BasicMessage {

    private static final long serialVersionUID = 812224623264821829L;

    private String jobName;
    private String fractalId;
    private List<Point> computedPoints;
    private int finalReceiverId;

    public ComputedPointsMessage(int senderPort, int receiverPort, String senderIpAddress, String receiverIpAddress,
                                 String jobName, String fractalId, List<Point> computedPoints, int finalReceiverId) {
        super(MessageType.COMPUTED_POINTS, senderPort, receiverPort, senderIpAddress, receiverIpAddress);
        this.jobName = jobName;
        this.fractalId = fractalId;
        this.computedPoints = computedPoints;
        this.finalReceiverId = finalReceiverId;
    }

    public String getJobName() {
        return jobName;
    }

    public String getFractalId() {
        return fractalId;
    }

    public List<Point> getComputedPoints() {
        return computedPoints;
    }

    public int getFinalReceiverId() {
        return finalReceiverId;
    }
}
