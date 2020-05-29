package servent.message;

public class NewNodeMessage extends BasicMessage {

	private static final long serialVersionUID = 3899837286642127636L;

	private int firstServentPort;

	public NewNodeMessage(int senderPort, int receiverPort, String senderIpAddress, String receiverIpAddress,
						  int firstServentPort) {
		super(MessageType.NEW_NODE, senderPort, receiverPort, senderIpAddress, receiverIpAddress);
		this.firstServentPort = firstServentPort;
	}

	public int getFirstServentPort() {
		return firstServentPort;
	}
}
