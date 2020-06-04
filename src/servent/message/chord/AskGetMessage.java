package servent.message.chord;

import servent.message.BasicMessage;
import servent.message.MessageType;

public class AskGetMessage extends BasicMessage {

	private static final long serialVersionUID = -8558031124520315033L;

	public AskGetMessage(int senderPort, int receiverPort, String senderIpAddress,
						 String receiverIpAddress, String text) {
		super(MessageType.ASK_GET, senderPort, receiverPort, senderIpAddress, receiverIpAddress, text);
	}
}
