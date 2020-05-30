package servent.handler;

import app.AppConfig;
import app.models.Point;
import servent.message.AskJobResultMessage;
import servent.message.JobResultMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

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
        List<Point> receivedComputedPoints = askJobResultMessage.getComuptedPoints();
        String jobName = askJobResultMessage.getJobName();

        // add my points
        List<Point> myComputedPoints = AppConfig.chordState.getJobExecutionList().get(0).getComputedPoints();
        AppConfig.timestampedStandardPrint("Tacke " + myComputedPoints);
        receivedComputedPoints.addAll(myComputedPoints);
        if (AppConfig.myServentInfo.getId() == lastServentId) {
            // send result to the node which requested it
            int width = AppConfig.chordState.getJobExecutionList().get(0).getWidth();
            int height = AppConfig.chordState.getJobExecutionList().get(0).getHeight();

            // todo: popravi slanje vise
            JobResultMessage jobResultMessage = new JobResultMessage(AppConfig.myServentInfo.getListenerPort(),
                    clientMessage.getSenderPort(), AppConfig.myServentInfo.getIpAddress(),
                    clientMessage.getSenderIpAddress(), jobName, receivedComputedPoints, width, height);
            MessageUtil.sendMessage(jobResultMessage);
        } else {
            // send to first successor
            AskJobResultMessage arm = new AskJobResultMessage(clientMessage.getSenderPort(),
                    AppConfig.chordState.getNextNodePort(), clientMessage.getSenderIpAddress(),
                    AppConfig.chordState.getNextNodeIpAddress(), jobName, lastServentId, receivedComputedPoints);
            MessageUtil.sendMessage(arm);
        }
    }
}
