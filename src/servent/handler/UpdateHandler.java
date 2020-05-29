package servent.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

		if (updateMessage.getSenderPort() != AppConfig.myServentInfo.getListenerPort()) {
			Message nextUpdate = new UpdateMessage(clientMessage.getSenderPort(), AppConfig.chordState.getNextNodePort(),
					clientMessage.getSenderIpAddress(), AppConfig.chordState.getNextNodeIpAddress(),
					AppConfig.myServentInfo.getId(), AppConfig.chordState.getAllNodeIdInfoMap(), "newMessageText");
			MessageUtil.sendMessage(nextUpdate);
		}

//		if (updateMessage.getSenderPort() != AppConfig.myServentInfo.getListenerPort()) {
//			ServentInfo newNodInfo = new ServentInfo(updateMessage.getSenderIpAddress(), updateMessage.getSenderPort());
//			int newNodeId = updateMessage.getSenderId();
//			newNodInfo.setId(newNodeId);
//			Map<Integer, ServentInfo> newNodes = new HashMap<>();
//			newNodes.put(newNodeId, newNodInfo);
//			AppConfig.chordState.addNodes(newNodes);
//
//			String newMessageText = "";
//			if (clientMessage.getMessageText().equals("")) {
//				newMessageText = AppConfig.myServentInfo.getId() + "," + AppConfig.myServentInfo.getListenerPort();
//				// todo: is this ok?
//				AppConfig.chordState.setPredecessor(newNodInfo);
//			} else {
//				newMessageText = clientMessage.getMessageText() + ";" + AppConfig.myServentInfo.getId() + "," + AppConfig.myServentInfo.getListenerPort();
//			}
//			Message nextUpdate = new UpdateMessage(clientMessage.getSenderPort(), AppConfig.chordState.getNextNodePort(),
//					clientMessage.getSenderIpAddress(), AppConfig.chordState.getNextNodeIpAddress(),
//					AppConfig.myServentInfo.getId(), AppConfig.chordState.getAllNodeIdInfoMap(), newMessageText);
//			MessageUtil.sendMessage(nextUpdate);
//		}
//		} else {
////			String messageText = clientMessage.getMessageText();
////			String[] idsPorts = messageText.split(";");
////
////			Map<Integer, ServentInfo> allNodes = new HashMap<>();
////			for (String idPort : idsPorts) {
////				int id = Integer.parseInt(idPort.split(",")[0]);
////				int port = Integer.parseInt(idPort.split(",")[1]);
////
////				// TODO: promeni ovde localhost
////				ServentInfo serventInfo = new ServentInfo("localhost", port);
////				serventInfo.setId(id);
////				allNodes.put(id, serventInfo);
////			}
//			Map<Integer, ServentInfo> allNodes = new HashMap<>();
//
//			AppConfig.chordState.addNodes(allNodes);
//		}
	}

}
