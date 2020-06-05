package servent.message.chaos_game;

import app.models.FractalIdJob;
import app.models.Job;
import app.models.JobScheduleType;
import servent.message.BasicMessage;
import servent.message.MessageType;

import java.util.List;
import java.util.Map;

public class IdleMessage extends BasicMessage {

    private static final long serialVersionUID = 7591006261115609523L;

    private Map<Integer, FractalIdJob> serventJobsMap;
    private int finalReceiverId;
    private Map<FractalIdJob, FractalIdJob> mappedFractalsJobs;
    private List<Job> activeJobs;
    private JobScheduleType scheduleType;
    private int jobSchedulerId;

    public IdleMessage(int senderPort, int receiverPort, String senderIpAddress, String receiverIpAddress,
                       Map<Integer, FractalIdJob> serventJobsMap, int finalReceiverId,
                       Map<FractalIdJob, FractalIdJob> mappedFractalsJobs, List<Job> activeJobs,
                       JobScheduleType scheduleType, int jobSchedulerId) {
        super(MessageType.IDLE, senderPort, receiverPort, senderIpAddress, receiverIpAddress);
        this.serventJobsMap = serventJobsMap;
        this.finalReceiverId = finalReceiverId;
        this.mappedFractalsJobs = mappedFractalsJobs;
        this.activeJobs = activeJobs;
        this.scheduleType = scheduleType;
        this.jobSchedulerId = jobSchedulerId;
    }

    public Map<Integer, FractalIdJob> getServentJobsMap() { return serventJobsMap; }

    public int getFinalReceiverId() { return finalReceiverId; }

    public Map<FractalIdJob, FractalIdJob> getMappedFractalsJobs() { return mappedFractalsJobs; }

    public List<Job> getActiveJobs() { return activeJobs; }

    public JobScheduleType getScheduleType() { return scheduleType; }

    public int getJobSchedulerId() { return jobSchedulerId; }
}
