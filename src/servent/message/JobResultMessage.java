package servent.message;

import app.models.Point;

import java.util.List;

public class JobResultMessage extends BasicMessage {

    private static final long serialVersionUID = 5839429684400309826L;

    private String jobName;
    private List<Point> computedPoints;
    private int width;
    private int height;

    public JobResultMessage(int senderPort, int receiverPort, String senderIpAddress, String receiverIpAddress,
                            String jobName, List<Point> computedPoints, int width, int height) {
        super(MessageType.JOB_RESULT, senderPort, receiverPort, senderIpAddress, receiverIpAddress);
        this.jobName = jobName;
        this.computedPoints = computedPoints;
        this.width = width;
        this.height = height;
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
}
