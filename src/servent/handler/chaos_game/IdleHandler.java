package servent.handler.chaos_game;

import app.AppConfig;
import app.models.*;
import servent.handler.MessageHandler;
import servent.message.chaos_game.IdleMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

import java.util.List;
import java.util.Map;

public class IdleHandler implements MessageHandler {

    private Message clientMessage;
    private IdleMessage idleMessage;

    private int receiverId;
    private Map<Integer, FractalIdJob> serventJobsMap;
    private Map<FractalIdJob, FractalIdJob> mappedFractalJobs;
    private List<Job> activeJobs;
    private JobScheduleType scheduleType;

    public IdleHandler(Message clientMessage) {
        this.clientMessage = clientMessage;

        idleMessage = (IdleMessage) clientMessage;
        receiverId = idleMessage.getFinalReceiverId();
        serventJobsMap = idleMessage.getServentJobsMap();
        mappedFractalJobs = idleMessage.getMappedFractalsJobs();
        activeJobs = idleMessage.getActiveJobs();
        scheduleType = idleMessage.getScheduleType();
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() != MessageType.IDLE) {
            AppConfig.timestampedErrorPrint("Idle handler got a message that is not IDLE");
            return;
        }

        if (receiverId != AppConfig.myServentInfo.getId()) {  // I am not intended receiver, forward message
            this.forwardMessage();
            return;
        }

        if (AppConfig.chordState.getExecutionJob() != null) {   // send my data if I was executing
            JobExecutionHandler.sendMyCurrentData(mappedFractalJobs, scheduleType);
        }

        AppConfig.chordState.setServentJobs(serventJobsMap);
        AppConfig.chordState.addNewJobs(activeJobs);
        AppConfig.chordState.resetAfterReceivedComputedPoints();
        AppConfig.timestampedStandardPrint("I am idle...");
    }

    private void forwardMessage() {
        ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(receiverId);

        IdleMessage im = new IdleMessage(idleMessage.getSenderPort(), nextServent.getListenerPort(),
                idleMessage.getSenderIpAddress(), nextServent.getIpAddress(), serventJobsMap, receiverId,
                mappedFractalJobs, activeJobs, scheduleType);
        MessageUtil.sendMessage(im);
    }
}
