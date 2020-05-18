package servent.message;

public class SorryMessage extends BasicMessage {

	private static final long serialVersionUID = 8866336621366084210L;

	public SorryMessage(int senderPort, int receiverPort) {
		super(MessageType.SORRY, senderPort, receiverPort);
	}
}
