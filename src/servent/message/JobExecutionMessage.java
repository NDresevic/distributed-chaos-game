package servent.message;

import app.models.FractalIdJob;
import app.models.Job;
import app.models.Point;

import java.util.List;
import java.util.Map;

public class JobExecutionMessage extends BasicMessage {

    private static final long serialVersionUID = -4792250302040560741L;

    private List<String> fractalIds;
    private List<Point> startPoints;
    private Job job;
    private Map<Integer, FractalIdJob> serventJobsMap;
    private int level;
    private int finalReceiverId;

    public JobExecutionMessage(int senderPort, int receiverPort, String senderIpAddress, String receiverIpAddress,
                               List<String> fractalIds, List<Point> startPoints, Job job,
                               Map<Integer, FractalIdJob> serventJobsMap, int level, int finalReceiverId) {
        super(MessageType.JOB_EXECUTION, senderPort, receiverPort, senderIpAddress, receiverIpAddress);
        this.fractalIds = fractalIds;
        this.startPoints = startPoints;
        this.job = job;
        this.serventJobsMap = serventJobsMap;
        this.level = level;
        this.finalReceiverId = finalReceiverId;
    }

    public List<String> getFractalIds() {
        return fractalIds;
    }

    public List<Point> getStartPoints() {
        return startPoints;
    }

    public Job getJob() {
        return job;
    }

    public Map<Integer, FractalIdJob> getServentJobsMap() {
        return serventJobsMap;
    }

    public int getLevel() {
        return level;
    }

    public int getFinalReceiverId() {
        return finalReceiverId;
    }
}
