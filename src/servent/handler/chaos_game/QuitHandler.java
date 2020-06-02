package servent.handler.chaos_game;

import app.AppConfig;
import app.models.ServentInfo;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.chaos_game.QuitMessage;
import servent.message.util.MessageUtil;

import java.util.HashMap;
import java.util.Map;

public class QuitHandler implements MessageHandler {

    private Message clientMessage;

    public QuitHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() != MessageType.QUIT) {
            AppConfig.timestampedErrorPrint("Quit handler got a message that is not QUIT");
            return;
        }


        QuitMessage quitMessage = (QuitMessage) clientMessage;
        int quitterId = quitMessage.getQuitterId();
        int myId = AppConfig.myServentInfo.getId();

        if (quitterId == myId || !AppConfig.chordState.getAllNodeIdInfoMap().containsKey(quitterId)) {
            AppConfig.timestampedStandardPrint("Quit message made a circle.");
            return;
        }

        Map<Integer, ServentInfo> newNodesMap = new HashMap<>();
        AppConfig.chordState.getAllNodeIdInfoMap().remove(quitterId);
        for (Map.Entry<Integer, ServentInfo> entry: AppConfig.chordState.getAllNodeIdInfoMap().entrySet()) {
            if (entry.getKey() > quitterId) {
                ServentInfo serventInfo = entry.getValue();
                serventInfo.setId(entry.getKey() - 1);
                newNodesMap.put(entry.getKey() - 1, serventInfo);
            } else {
                newNodesMap.put(entry.getKey(), entry.getValue());
            }
        }
        if (myId > quitterId) {
            AppConfig.myServentInfo.setId(myId - 1);
        }
        AppConfig.chordState.getAllNodeIdInfoMap().clear();
        AppConfig.chordState.addNodes(newNodesMap);

        // send to first successor
        QuitMessage newQuitMessage = new QuitMessage(quitMessage.getSenderPort(), AppConfig.chordState.getNextNodePort(),
                quitMessage.getSenderIpAddress(), AppConfig.chordState.getNextNodeIpAddress(), quitterId);
        MessageUtil.sendMessage(newQuitMessage);
    }
}
