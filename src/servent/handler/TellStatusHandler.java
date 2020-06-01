package servent.handler;

import app.AppConfig;
import app.models.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.TellStatusMessage;
import servent.message.util.MessageUtil;

import java.util.Map;

public class TellStatusHandler implements MessageHandler {

    private Message clientMessage;

    public TellStatusHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() != MessageType.TELL_STATUS) {
            AppConfig.timestampedErrorPrint("Tell status handler got a message that is not TELL_STATUS");
            return;
        }

        TellStatusMessage tellStatusMessage = (TellStatusMessage) clientMessage;
        int receiverId = tellStatusMessage.getFinalReceiverId();
        Map<String, Map<String, Integer>> resultMap = tellStatusMessage.getResultMap();
        int version = tellStatusMessage.getVersion();

        // if I am not intended final receiver then just pass message further
        if (receiverId != AppConfig.myServentInfo.getId()) {
            ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(receiverId);

            TellStatusMessage tsm = new TellStatusMessage(tellStatusMessage.getSenderPort(), nextServent.getListenerPort(),
                    tellStatusMessage.getSenderIpAddress(), nextServent.getIpAddress(), receiverId, resultMap, version);
            MessageUtil.sendMessage(tsm);
            return;
        }

        // print status
        StringBuilder result = new StringBuilder("STATUS: \n");
        for (Map.Entry<String, Map<String, Integer>> entry: resultMap.entrySet()) {
            result.append("jobName=" + entry.getKey() + "\n");
            int totalPointsCount = 0;
            int totalServentsCount = 0;
            for (Map.Entry<String, Integer> e: resultMap.get(entry.getKey()).entrySet()) {
                result.append("fractalID=" + e.getKey() + ", pointsCount=" + e.getValue() + "\n");
                totalPointsCount += e.getValue();
                totalServentsCount++;
            }
            if (version != 0) {
                result.append("totalPointsCount=" + totalPointsCount + ", totalNodesCount=" + totalServentsCount + "\n");
            }
        }
        AppConfig.timestampedStandardPrint(result.toString());
    }
}
