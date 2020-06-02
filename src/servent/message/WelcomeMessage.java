package servent.message;

import java.util.Map;

public class WelcomeMessage extends BasicMessage {

	private static final long serialVersionUID = -8981406250652693908L;

	private int id;
	private String firstServentIpAddressPort;
	private Map<Integer, Integer> values;
	
	public WelcomeMessage(int senderPort, int receiverPort, String senderIpAddress, String receiverIpAddress, int id,
						  String firstServentIpAddressPort, Map<Integer, Integer> values) {
		super(MessageType.WELCOME, senderPort, receiverPort, senderIpAddress, receiverIpAddress);

		this.id = id;
		this.firstServentIpAddressPort = firstServentIpAddressPort;
		this.values = values;
	}

	public int getId() { return id; }

	public String getFirstServentIpAddressPort() { return firstServentIpAddressPort; }

	public Map<Integer, Integer> getValues() {
		return values;
	}
}
