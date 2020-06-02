package servent.message.chord;

import servent.message.BasicMessage;
import servent.message.MessageType;

public class PutMessage extends BasicMessage {

	private static final long serialVersionUID = 5163039209888734276L;

	public PutMessage(int senderPort, int receiverPort, String senderIpAddress, String receiverIpAddress,
					  int key, int value) {
		super(MessageType.PUT, senderPort, receiverPort, senderIpAddress, receiverIpAddress,
				key + ":" + value);
	}
}
