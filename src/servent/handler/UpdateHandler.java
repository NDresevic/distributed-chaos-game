package servent.handler;

import java.util.HashMap;
import java.util.Map;

import app.AppConfig;
import app.models.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.UpdateMessage;
import servent.message.util.MessageUtil;

public class UpdateHandler implements MessageHandler {

	private Message clientMessage;
	
	public UpdateHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {
		if (clientMessage.getMessageType() != MessageType.UPDATE) {
			AppConfig.timestampedErrorPrint("Update message handler got message that is not UPDATE");
			return;
		}

		UpdateMessage updateMessage = (UpdateMessage) clientMessage;
		Map<Integer, ServentInfo> allNodes = new HashMap<>(updateMessage.getNodesMap());
		allNodes.put(AppConfig.myServentInfo.getId(), AppConfig.myServentInfo);
		AppConfig.chordState.addNodes(allNodes);

		// if I didn't send the message (it didn't make a circle) send to my first successor
		if (updateMessage.getSenderPort() != AppConfig.myServentInfo.getListenerPort()) {
			UpdateMessage nextUpdate = new UpdateMessage(clientMessage.getSenderPort(), AppConfig.chordState.getNextNodePort(),
					clientMessage.getSenderIpAddress(), AppConfig.chordState.getNextNodeIpAddress(),
					AppConfig.myServentInfo.getId(), AppConfig.chordState.getAllNodeIdInfoMap());
			MessageUtil.sendMessage(nextUpdate);
		}
	}

}
