package servent.message;

public class AskJobFractalIDResultMessage extends BasicMessage {

    private static final long serialVersionUID = 6380521438182073523L;

    private String jobName;

    public AskJobFractalIDResultMessage(int senderPort, int receiverPort, String senderIpAddress,
                                        String receiverIpAddress, String jobName) {
        super(MessageType.ASK_JOB_FRACTALID_RESULT, senderPort, receiverPort, senderIpAddress, receiverIpAddress);
        this.jobName = jobName;
    }

    public String getJobName() {
        return jobName;
    }
}
