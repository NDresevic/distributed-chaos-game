package servent.message;

public class NewNodeMessage extends BasicMessage {

	private static final long serialVersionUID = 3899837286642127636L;

	public NewNodeMessage(int senderPort, int receiverPort) {
		super(MessageType.NEW_NODE, senderPort, receiverPort);
	}
}
