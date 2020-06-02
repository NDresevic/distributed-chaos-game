package servent.message.chaos_game;

import servent.message.BasicMessage;
import servent.message.MessageType;

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
