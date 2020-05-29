package servent.message;

import app.models.ServentInfo;

import java.util.Map;

public class UpdateMessage extends BasicMessage {

	private static final long serialVersionUID = 3586102505319194978L;

	private int senderId;
	private Map<Integer, ServentInfo> nodesMap;

	public UpdateMessage(int senderPort, int receiverPort, String senderIpAddress, String receiverIpAddress,
						 int senderId, Map<Integer, ServentInfo> nodesMap, String text) {
		super(MessageType.UPDATE, senderPort, receiverPort, senderIpAddress, receiverIpAddress, text);
		this.senderId = senderId;
		this.nodesMap = nodesMap;
	}

	public int getSenderId() { return senderId; }

	public Map<Integer, ServentInfo> getNodesMap() { return nodesMap; }
}
