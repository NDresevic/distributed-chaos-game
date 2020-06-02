package servent.message.chaos_game;

import app.models.Point;
import servent.message.BasicMessage;
import servent.message.MessageType;

import java.util.ArrayList;
import java.util.List;

public class QuitMessage extends BasicMessage {

    private static final long serialVersionUID = 2276991847916324467L;

    private int quitterId;
    private String jobName;
    private String fractalId;
    private List<Point> quitterComputedPoints;

    public QuitMessage(int senderPort, int receiverPort, String senderIpAddress, String receiverIpAddress,
                       int quitterId, String jobName, String fractalId, List<Point> quitterComputedPoints) {
        super(MessageType.QUIT, senderPort, receiverPort, senderIpAddress, receiverIpAddress);
        this.quitterId = quitterId;
        this.jobName = jobName;
        this.fractalId = fractalId;
        this.quitterComputedPoints = quitterComputedPoints;
    }

    public QuitMessage(int senderPort, int receiverPort, String senderIpAddress, String receiverIpAddress,
                       int quitterId) {
        this(senderPort, receiverPort, senderIpAddress, receiverIpAddress, quitterId,
                "", "", new ArrayList<>());
    }

    public int getQuitterId() { return quitterId; }

    public String getJobName() {
        return jobName;
    }

    public String getFractalId() {
        return fractalId;
    }

    public List<Point> getQuitterComputedPoints() {
        return quitterComputedPoints;
    }
}
