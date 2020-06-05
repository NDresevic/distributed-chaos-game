package servent.message;

public class WelcomeMessage extends BasicMessage {

	private static final long serialVersionUID = -8981406250652693908L;

	private int id;
	private String firstServentIpAddressPort;

	public WelcomeMessage(int senderPort, int receiverPort, String senderIpAddress, String receiverIpAddress, int id,
						  String firstServentIpAddressPort) {
		super(MessageType.WELCOME, senderPort, receiverPort, senderIpAddress, receiverIpAddress);

		this.id = id;
		this.firstServentIpAddressPort = firstServentIpAddressPort;
	}

	public int getId() { return id; }

	public String getFirstServentIpAddressPort() { return firstServentIpAddressPort; }

}
