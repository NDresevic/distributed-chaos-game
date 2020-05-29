package servent.message;

public class QuitMessage extends BasicMessage {

    private static final long serialVersionUID = 2276991847916324467L;

    private int quitterId;
    private int myId;

    public QuitMessage(int senderPort, int receiverPort, String senderIpAddress, String receiverIpAddress, int quitterId,
                       int myId) {
        super(MessageType.QUIT, senderPort, receiverPort, senderIpAddress, receiverIpAddress);
        this.quitterId = quitterId;
        this.myId = myId;
    }

    public int getQuitterId() { return quitterId; }

    public int getMyId() { return myId; }
}
