package servent.handler.chaos_game;

import app.AppConfig;
import app.models.FractalIdJob;
import app.models.JobExecution;
import app.models.Point;
import app.models.ServentInfo;
import servent.handler.MessageHandler;
import servent.message.chaos_game.ComputedPointsMessage;
import servent.message.chaos_game.IdleMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

import java.util.List;
import java.util.Map;

public class IdleHandler implements MessageHandler {

    private Message clientMessage;

    public IdleHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() != MessageType.IDLE) {
            AppConfig.timestampedErrorPrint("Idle handler got a message that is not IDLE");
            return;
        }

        IdleMessage idleMessage = (IdleMessage) clientMessage;
        int receiverId = idleMessage.getFinalReceiverId();
        Map<Integer, FractalIdJob> serventJobsMap = idleMessage.getServentJobsMap();
        Map<FractalIdJob, FractalIdJob> mappedFractalJobs = idleMessage.getMappedFractalsJobs();

        // if I am not intended final receiver then just pass message further
        if (receiverId != AppConfig.myServentInfo.getId()) {
            ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(receiverId);

            IdleMessage im = new IdleMessage(idleMessage.getSenderPort(), nextServent.getListenerPort(),
                    idleMessage.getSenderIpAddress(), nextServent.getIpAddress(), serventJobsMap, receiverId,
                    mappedFractalJobs);
            MessageUtil.sendMessage(im);
            return;
        }

        // send my previously computed points if needed
        if (AppConfig.chordState.getExecutionJob() != null) {   // I am currently executing a job
            JobExecution je = AppConfig.chordState.getExecutionJob();
            FractalIdJob myFractalJob = new FractalIdJob(je.getFractalId(), je.getJobName());

            if (mappedFractalJobs.containsKey(myFractalJob)) {
                FractalIdJob hisFractalJob = mappedFractalJobs.get(myFractalJob);
                List<Point> myComputedPoints = je.getComputedPoints();
                int dataReceiverId = AppConfig.chordState.getIdForFractalIDAndJob(hisFractalJob.getFractalId(), hisFractalJob.getJobName());
                ServentInfo nextDataServent = AppConfig.chordState.getNextNodeForServentId(dataReceiverId);

                ComputedPointsMessage cpm = new ComputedPointsMessage(AppConfig.myServentInfo.getListenerPort(),
                        nextDataServent.getListenerPort(), AppConfig.myServentInfo.getIpAddress(),
                        nextDataServent.getIpAddress(), myFractalJob.getJobName(), myFractalJob.getFractalId(),
                        myComputedPoints, dataReceiverId);
                MessageUtil.sendMessage(cpm);
            }
        }

        AppConfig.chordState.setServentJobs(serventJobsMap);
        AppConfig.chordState.resetAfterReceivedComputedPoints();
        AppConfig.timestampedStandardPrint("I am idle...");
    }
}
