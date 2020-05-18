package servent.message;

public class AskGetMessage extends BasicMessage {

	private static final long serialVersionUID = -8558031124520315033L;

	public AskGetMessage(int senderPort, int receiverPort, String text) {
		super(MessageType.ASK_GET, senderPort, receiverPort, text);
	}
}
