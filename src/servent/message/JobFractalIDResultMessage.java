package servent.message;

import app.models.Point;

import java.util.List;

public class JobFractalIDResultMessage extends BasicMessage {

    private static final long serialVersionUID = -3963464240111926090L;

    private String jobName;
    private String fractalId;
    private List<Point> computedPoints;
    private int width;
    private int height;
    private double proportion;

    public JobFractalIDResultMessage(int senderPort, int receiverPort, String senderIpAddress, String receiverIpAddress,
                                     String jobName, String fractalId, List<Point> computedPoints, int width, int height,
                                     double proportion) {
        super(MessageType.JOB_FRACTALID_RESULT, senderPort, receiverPort, senderIpAddress, receiverIpAddress);
        this.jobName = jobName;
        this.fractalId = fractalId;
        this.computedPoints = computedPoints;
        this.width = width;
        this.height = height;
        this.proportion = proportion;
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
