package servent.handler.chaos_game;

import app.AppConfig;
import app.models.JobScheduleType;
import app.models.Point;
import app.models.ServentInfo;
import servent.handler.MessageHandler;
import servent.message.chaos_game.ComputedPointsMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

import java.util.List;

public class ComputedPointsHandler implements MessageHandler {

    private Message clientMessage;

    public ComputedPointsHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() != MessageType.COMPUTED_POINTS) {
            AppConfig.timestampedErrorPrint("Computed points handler got a message that is not COMPUTED_POINTS");
            return;
        }

        ComputedPointsMessage computedPointsMessage = (ComputedPointsMessage) clientMessage;
        String jobName = computedPointsMessage.getJobName();
        String fractalId = computedPointsMessage.getFractalId();
        List<Point> computedPoints = computedPointsMessage.getComputedPoints();
        int receiverId = computedPointsMessage.getFinalReceiverId();

        // if I am not intended final receiver then just pass message further
        if (receiverId != AppConfig.myServentInfo.getId()) {
            ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(receiverId);

            ComputedPointsMessage cpm = new ComputedPointsMessage(computedPointsMessage.getSenderPort(),
                    nextServent.getListenerPort(), computedPointsMessage.getSenderIpAddress(),
                    nextServent.getIpAddress(), jobName, fractalId, computedPoints, receiverId);
            MessageUtil.sendMessage(cpm);
            return;
        }

        AppConfig.timestampedStandardPrint("Received computed points from {fractalID=" + fractalId + ", jobName=" + jobName + "}");
        AppConfig.chordState.addComputedPoints(computedPoints);
    }
}
