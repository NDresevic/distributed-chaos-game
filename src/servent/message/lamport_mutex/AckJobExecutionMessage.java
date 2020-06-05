package servent.message.lamport_mutex;

import servent.message.BasicMessage;
import servent.message.MessageType;

public class AckJobExecutionMessage extends BasicMessage {

    private static final long serialVersionUID = 3797192912255618369L;

    private int finalReceiverId;

    public AckJobExecutionMessage(int senderPort, int receiverPort, String senderIpAddress, String receiverIpAddress,
                                  int finalReceiverId) {
        super(MessageType.ACK_JOB_EXECUTION, senderPort, receiverPort, senderIpAddress, receiverIpAddress);
        this.finalReceiverId = finalReceiverId;
    }

    public int getFinalReceiverId() { return finalReceiverId; }
}
