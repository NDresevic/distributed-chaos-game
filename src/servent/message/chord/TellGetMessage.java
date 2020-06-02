package servent.message.chord;

import servent.message.BasicMessage;
import servent.message.MessageType;

public class TellGetMessage extends BasicMessage {

	private static final long serialVersionUID = -6213394344524749872L;

	public TellGetMessage(int senderPort, int receiverPort, String senderIpAddress, String receiverIpAddress,
						  int key, int value) {
		super(MessageType.TELL_GET, senderPort, receiverPort, senderIpAddress, receiverIpAddress,
				key + ":" + value);
	}
}
