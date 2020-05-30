package servent.message;

public class JobResultMessage extends BasicMessage {

    public JobResultMessage(int senderPort, int receiverPort, String senderIpAddress, String receiverIpAddress) {
        super(MessageType.JOB_RESULT, senderPort, receiverPort, senderIpAddress, receiverIpAddress);
    }
}
