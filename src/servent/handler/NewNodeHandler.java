package servent.handler;

import java.util.HashMap;
import java.util.Map;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.NewNodeMessage;
import servent.message.WelcomeMessage;
import servent.message.util.MessageUtil;

public class NewNodeHandler implements MessageHandler {

	private Message clientMessage;
	
	public NewNodeHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {
		if (clientMessage.getMessageType() != MessageType.NEW_NODE) {
			AppConfig.timestampedErrorPrint("NEW_NODE handler got something that is not new node message.");
			return;
		}

		NewNodeMessage newNodeMessage = (NewNodeMessage) clientMessage;
		int newNodePort = newNodeMessage.getSenderPort();
		String newNodeIpAddress = newNodeMessage.getSenderIpAddress();
		int firstServentPort = newNodeMessage.getFirstServentPort();

		int hisId = AppConfig.myServentInfo.getId() + 1;
		Map<Integer, Integer> hisValues = new HashMap<>();
		WelcomeMessage wm = new WelcomeMessage(AppConfig.myServentInfo.getListenerPort(), newNodePort,
				AppConfig.myServentInfo.getIpAddress(), newNodeIpAddress, hisId, firstServentPort, hisValues);
		MessageUtil.sendMessage(wm);
	}

}
