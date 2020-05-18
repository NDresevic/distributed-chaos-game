package servent.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import app.AppConfig;
import app.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.NewNodeMessage;
import servent.message.SorryMessage;
import servent.message.WelcomeMessage;
import servent.message.util.MessageUtil;

public class NewNodeHandler implements MessageHandler {

	private Message clientMessage;
	
	public NewNodeHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.NEW_NODE) {
			int newNodePort = clientMessage.getSenderPort();
			ServentInfo newNodeInfo = new ServentInfo("localhost", newNodePort);
			
			//check if the new node collides with another existing node.
			if (AppConfig.chordState.isCollision(newNodeInfo.getChordId())) {
				Message sry = new SorryMessage(AppConfig.myServentInfo.getListenerPort(), clientMessage.getSenderPort());
				MessageUtil.sendMessage(sry);
				return;
			}
			
			//check if he is my predecessor
			boolean isMyPred = AppConfig.chordState.isKeyMine(newNodeInfo.getChordId());
			if (isMyPred) { //if yes, prepare and send welcome message
				ServentInfo hisPred = AppConfig.chordState.getPredecessor();
				if (hisPred == null) {
					hisPred = AppConfig.myServentInfo;
				}
				
				AppConfig.chordState.setPredecessor(newNodeInfo);
				
				Map<Integer, Integer> myValues = AppConfig.chordState.getValueMap();
				Map<Integer, Integer> hisValues = new HashMap<>();
				
				int myId = AppConfig.myServentInfo.getChordId();
				int hisPredId = hisPred.getChordId();
				int newNodeId = newNodeInfo.getChordId();
				
				for (Entry<Integer, Integer> valueEntry : myValues.entrySet()) {
					if (hisPredId == myId) { //i am first and he is second
						if (myId < newNodeId) {
							if (valueEntry.getKey() <= newNodeId && valueEntry.getKey() > myId) {
								hisValues.put(valueEntry.getKey(), valueEntry.getValue());
							}
						} else {
							if (valueEntry.getKey() <= newNodeId || valueEntry.getKey() > myId) {
								hisValues.put(valueEntry.getKey(), valueEntry.getValue());
							}
						}
					}
					if (hisPredId < myId) { //my old predecesor was before me
						if (valueEntry.getKey() <= newNodeId) {
							hisValues.put(valueEntry.getKey(), valueEntry.getValue());
						}
					} else { //my old predecesor was after me
						if (hisPredId > newNodeId) { //new node overflow
							if (valueEntry.getKey() <= newNodeId || valueEntry.getKey() > hisPredId) {
								hisValues.put(valueEntry.getKey(), valueEntry.getValue());
							}
						} else { //no new node overflow
							if (valueEntry.getKey() <= newNodeId && valueEntry.getKey() > hisPredId) {
								hisValues.put(valueEntry.getKey(), valueEntry.getValue());
							}
						}
						
					}
					
				}
				for (Integer key : hisValues.keySet()) { //remove his values from my map
					myValues.remove(key);
				}
				AppConfig.chordState.setValueMap(myValues);
				
				WelcomeMessage wm = new WelcomeMessage(AppConfig.myServentInfo.getListenerPort(), newNodePort, hisValues);
				MessageUtil.sendMessage(wm);
			} else { //if he is not my predecessor, let someone else take care of it
				ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(newNodeInfo.getChordId());
				NewNodeMessage nnm = new NewNodeMessage(newNodePort, nextNode.getListenerPort());
				MessageUtil.sendMessage(nnm);
			}
			
		} else {
			AppConfig.timestampedErrorPrint("NEW_NODE handler got something that is not new node message.");
		}

	}

}
