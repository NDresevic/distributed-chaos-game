package servent.handler;

import app.AppConfig;
import app.models.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.UpdateMessage;
import servent.message.WelcomeMessage;
import servent.message.util.MessageUtil;

import java.util.HashMap;
import java.util.Map;

public class WelcomeHandler implements MessageHandler {

	private Message clientMessage;
	
	public WelcomeHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {
		if (clientMessage.getMessageType() != MessageType.WELCOME) {
			AppConfig.timestampedErrorPrint("Welcome handler got a message that is not WELCOME");
			return;
		}
		WelcomeMessage welcomeMsg = (WelcomeMessage) clientMessage;
		int myId = welcomeMsg.getId();
		AppConfig.myServentInfo.setId(myId);
		AppConfig.chordState.init(welcomeMsg);

		Map<Integer, ServentInfo> nodesMap = new HashMap<>(AppConfig.chordState.getAllNodeIdInfoMap());
		UpdateMessage um = new UpdateMessage(AppConfig.myServentInfo.getListenerPort(),
				AppConfig.chordState.getNextNodePort(), AppConfig.myServentInfo.getIpAddress(),
				AppConfig.chordState.getNextNodeIpAddress(), nodesMap);
		MessageUtil.sendMessage(um);
	}
}
