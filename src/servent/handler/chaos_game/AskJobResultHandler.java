package servent.handler.chaos_game;

import app.AppConfig;
import app.models.Point;
import app.models.ServentInfo;
import servent.handler.MessageHandler;
import servent.message.*;
import servent.message.chaos_game.AskJobResultMessage;
import servent.message.chaos_game.JobResultMessage;
import servent.message.util.MessageUtil;

import java.util.ArrayList;
import java.util.List;

public class AskJobResultHandler implements MessageHandler {

    private Message clientMessage;

    public AskJobResultHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() != MessageType.ASK_JOB_RESULT) {
            AppConfig.timestampedErrorPrint("Ask job result handler got a message that is not ASK_JOB_RESULT");
            return;
        }

        AskJobResultMessage askJobResultMessage = (AskJobResultMessage) clientMessage;
        int lastServentId = askJobResultMessage.getLastServentId();
        int receiverId = askJobResultMessage.getFinalReceiverId();
        List<Point> receivedComputedPoints = askJobResultMessage.getComuptedPoints();
        String jobName = askJobResultMessage.getJobName();

        // if I am not intended final receiver then just pass message further
        if (receiverId != AppConfig.myServentInfo.getId()) {
            ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(receiverId);

            AskJobResultMessage arm = new AskJobResultMessage(clientMessage.getSenderPort(),
                    nextServent.getListenerPort(), clientMessage.getSenderIpAddress(),
                    nextServent.getIpAddress(), jobName, lastServentId, receiverId, receivedComputedPoints);
            MessageUtil.sendMessage(arm);
            return;
        }

        // add my points
        List<Point> myComputedPoints = new ArrayList<>(AppConfig.chordState.getExecutionJob().getComputedPoints());
        receivedComputedPoints.addAll(myComputedPoints);
        if (AppConfig.myServentInfo.getId() == lastServentId) {
            // send result to the node which requested it
            int width = AppConfig.chordState.getExecutionJob().getWidth();
            int height = AppConfig.chordState.getExecutionJob().getHeight();
            double proportion = AppConfig.chordState.getExecutionJob().getProportion();
            int finalReceiverId = AppConfig.chordState.getNodeIdForServentPortAndAddress(clientMessage.getSenderPort(), clientMessage.getSenderIpAddress());
            ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(finalReceiverId);

            JobResultMessage jobResultMessage = new JobResultMessage(AppConfig.myServentInfo.getListenerPort(),
                    nextServent.getListenerPort(), AppConfig.myServentInfo.getIpAddress(),
                    nextServent.getIpAddress(), finalReceiverId, jobName, receivedComputedPoints, width, height, proportion);
            MessageUtil.sendMessage(jobResultMessage);
        } else {
            // send to first successor
            int firstSuccessorId = AppConfig.chordState.getFirstSuccessorId();

            AskJobResultMessage arm = new AskJobResultMessage(clientMessage.getSenderPort(),
                    AppConfig.chordState.getNextNodePort(), clientMessage.getSenderIpAddress(),
                    AppConfig.chordState.getNextNodeIpAddress(), jobName, lastServentId, firstSuccessorId,
                    receivedComputedPoints);
            MessageUtil.sendMessage(arm);
        }
    }
}
