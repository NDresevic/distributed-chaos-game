package servent.message;

import app.models.Point;

import java.util.List;

public class JobResultMessage extends BasicMessage {

    private static final long serialVersionUID = 5839429684400309826L;

    private String jobName;
    private List<Point> computedPoints;
    private int width;
    private int height;
    private double proportion;
    private int finalReceiverId;

    public JobResultMessage(int senderPort, int receiverPort, String senderIpAddress, String receiverIpAddress,
                            int finalReceiverId, String jobName, List<Point> computedPoints, int width, int height,
                            double proportion) {
        super(MessageType.JOB_RESULT, senderPort, receiverPort, senderIpAddress, receiverIpAddress);
        this.finalReceiverId = finalReceiverId;
        this.jobName = jobName;
        this.computedPoints = computedPoints;
        this.width = width;
        this.height = height;
        this.proportion = proportion;
    }

    public int getFinalReceiverId() {
        return finalReceiverId;
    }

    public String getJobName() {
        return jobName;
    }

    public List<Point> getComputedPoints() {
        return computedPoints;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public double getProportion() {
        return proportion;
    }
}
