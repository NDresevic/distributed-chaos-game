package servent.message;

public class UpdateMessage extends BasicMessage {

	private static final long serialVersionUID = 3586102505319194978L;

	public UpdateMessage(int senderPort, int receiverPort, String text) {
		super(MessageType.UPDATE, senderPort, receiverPort, text);
	}
}
