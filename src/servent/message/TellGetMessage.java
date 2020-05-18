package servent.message;

public class TellGetMessage extends BasicMessage {

	private static final long serialVersionUID = -6213394344524749872L;

	public TellGetMessage(int senderPort, int receiverPort, int key, int value) {
		super(MessageType.TELL_GET, senderPort, receiverPort, key + ":" + value);
	}
}
