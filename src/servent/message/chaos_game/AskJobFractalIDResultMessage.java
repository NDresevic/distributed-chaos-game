package servent.message.chaos_game;

import servent.message.BasicMessage;
import servent.message.MessageType;

public class AskJobFractalIDResultMessage extends BasicMessage {

    private static final long serialVersionUID = 6380521438182073523L;

    private String jobName;
    private int finalReceiverId;

    public AskJobFractalIDResultMessage(int senderPort, int receiverPort, String senderIpAddress,
                                        String receiverIpAddress, String jobName, int finalReceiverId) {
        super(MessageType.ASK_JOB_FRACTALID_RESULT, senderPort, receiverPort, senderIpAddress, receiverIpAddress);
        this.jobName = jobName;
        this.finalReceiverId = finalReceiverId;
    }

    public String getJobName() {
        return jobName;
    }

    public int getFinalReceiverId() {
        return finalReceiverId;
    }
}
