package servent.handler.chord;

import java.util.Map;

import app.AppConfig;
import app.models.ServentInfo;
import servent.handler.MessageHandler;
import servent.message.chord.AskGetMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.chord.TellGetMessage;
import servent.message.util.MessageUtil;

public class AskGetHandler implements MessageHandler {

	private Message clientMessage;
	
	public AskGetHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.ASK_GET) {
			try {
				int key = Integer.parseInt(clientMessage.getMessageText());
				if (AppConfig.chordState.isKeyMine(key)) {
					Map<Integer, Integer> valueMap = AppConfig.chordState.getValueMap(); 
					int value = -1;
					
					if (valueMap.containsKey(key)) {
						value = valueMap.get(key);
					}
					
					TellGetMessage tgm = new TellGetMessage(AppConfig.myServentInfo.getListenerPort(), clientMessage.getSenderPort(),
															AppConfig.myServentInfo.getIpAddress(), clientMessage.getSenderIpAddress(),
															key, value);
					MessageUtil.sendMessage(tgm);
				} else {
					ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(key);
					AskGetMessage agm = new AskGetMessage(clientMessage.getSenderPort(), nextNode.getListenerPort(),
							clientMessage.getSenderIpAddress(), nextNode.getIpAddress(), clientMessage.getMessageText());
					MessageUtil.sendMessage(agm);
				}
			} catch (NumberFormatException e) {
				AppConfig.timestampedErrorPrint("Got ask get with bad text: " + clientMessage.getMessageText());
			}
			
		} else {
			AppConfig.timestampedErrorPrint("Ask get handler got a message that is not ASK_GET");
		}

	}

}
