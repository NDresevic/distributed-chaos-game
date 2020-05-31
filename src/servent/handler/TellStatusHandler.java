package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.TellStatusMessage;

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
        Map<String, Map<String, Integer>> resultMap = tellStatusMessage.getResultMap();
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
            if (tellStatusMessage.getVersion() != 0) {
                result.append("totalPointsCount=" + totalPointsCount + ", totalNodesCount=" + totalServentsCount + "\n");
            }
        }
        // broj tacaka na fraktalu i koliko cvorova radi na njemu
        AppConfig.timestampedStandardPrint(result.toString());
    }
}
