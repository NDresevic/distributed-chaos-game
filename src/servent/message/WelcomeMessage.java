package servent.message;

import java.util.Map;

public class WelcomeMessage extends BasicMessage {

	private static final long serialVersionUID = -8981406250652693908L;

	private int id;
	private int firstServentPort;
	private Map<Integer, Integer> values;
	
	public WelcomeMessage(int senderPort, int receiverPort, String senderIpAddress, String receiverIpAddress, int id,
						  int firstServentPort, Map<Integer, Integer> values) {
		super(MessageType.WELCOME, senderPort, receiverPort, senderIpAddress, receiverIpAddress);

		this.id = id;
		this.firstServentPort = firstServentPort;
		this.values = values;
	}

	public int getId() { return id; }

	public int getFirstServentPort() { return firstServentPort; }

	public Map<Integer, Integer> getValues() {
		return values;
	}
}
