package servent.message;

public class AskResultMessage extends BasicMessage {

    private static final long serialVersionUID = -2961633943823731472L;

    public AskResultMessage(int senderPort, int receiverPort, String senderIpAddress, String receiverIpAddress) {
        super(MessageType.ASK_RESULT, senderPort, receiverPort, senderIpAddress, receiverIpAddress);
    }
}
