package servent.message.lamport_mutex;

import servent.message.BasicMessage;
import servent.message.MessageType;

public class ReleaseCriticalSectionMessage extends BasicMessage {

    private static final long serialVersionUID = 5870184300875318255L;

    private int finalReceiverId;

    public ReleaseCriticalSectionMessage(int senderPort, int receiverPort, String senderIpAddress,
                                         String receiverIpAddress, int finalReceiverId) {
        super(MessageType.RELEASE_CRITICAL_SECTION, senderPort, receiverPort, senderIpAddress, receiverIpAddress);
        this.finalReceiverId = finalReceiverId;
    }

    public int getFinalReceiverId() { return finalReceiverId; }
}
