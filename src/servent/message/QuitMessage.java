package servent.message;

public class QuitMessage extends BasicMessage {

    private static final long serialVersionUID = 2276991847916324467L;

    private int quitterId;

    public QuitMessage(int senderPort, int receiverPort, String senderIpAddress, String receiverIpAddress,
                       int quitterId) {
        super(MessageType.QUIT, senderPort, receiverPort, senderIpAddress, receiverIpAddress);
        this.quitterId = quitterId;
    }

    public int getQuitterId() { return quitterId; }
}
