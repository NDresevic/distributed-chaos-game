package servent.message.chaos_game;

import servent.message.BasicMessage;
import servent.message.MessageType;

public class StopJobMessage extends BasicMessage {

    private static final long serialVersionUID = 7829496430293731081L;

    private String jobName;

    public StopJobMessage(int senderPort, int receiverPort, String senderIpAddress, String receiverIpAddress,
                          String jobName) {
        super(MessageType.STOP_JOB, senderPort, receiverPort, senderIpAddress, receiverIpAddress);
        this.jobName = jobName;
    }

    public String getJobName() {
        return jobName;
    }
}
