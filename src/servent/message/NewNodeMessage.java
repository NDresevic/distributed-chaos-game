package servent.message;

public class NewNodeMessage extends BasicMessage {

	private static final long serialVersionUID = 3899837286642127636L;

	private String firstServentIpAddressPort;

	public NewNodeMessage(int senderPort, int receiverPort, String senderIpAddress, String receiverIpAddress,
						  String firstServentIpAddressPort) {
		super(MessageType.NEW_NODE, senderPort, receiverPort, senderIpAddress, receiverIpAddress);
		this.firstServentIpAddressPort = firstServentIpAddressPort;
	}

	public String getFirstServentIpAddressPort() { return firstServentIpAddressPort; }
}
