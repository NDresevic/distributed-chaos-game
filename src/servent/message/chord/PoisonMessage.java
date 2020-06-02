package servent.message.chord;

import servent.message.BasicMessage;
import servent.message.MessageType;

public class PoisonMessage extends BasicMessage {

	private static final long serialVersionUID = -5625132784318034900L;

	public PoisonMessage() {
		super(MessageType.POISON, 0, 0, "", "");
	}
}
