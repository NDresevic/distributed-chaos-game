package servent.handler.chaos_game;

import app.AppConfig;
import app.models.JobScheduleType;
import app.models.ServentInfo;
import app.util.JobUtil;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.chaos_game.JobScheduleMessage;
import servent.message.util.MessageUtil;

public class JobScheduleHandler implements MessageHandler {

    private Message clientMessage;

    public JobScheduleHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() != MessageType.JOB_SCHEDULE) {
            AppConfig.timestampedErrorPrint("Job schedule handler got a message that is not JOB_SCHEDULE");
            return;
        }

        JobScheduleMessage jobScheduleMessage = (JobScheduleMessage) clientMessage;
        JobScheduleType scheduleType = jobScheduleMessage.getScheduleType();
        int receiverId = jobScheduleMessage.getFinalReceiverId();

        // if I am not intended final receiver then just pass message further
        if (receiverId != AppConfig.myServentInfo.getId()) {
            ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(receiverId);

            JobScheduleMessage jsm = new JobScheduleMessage(jobScheduleMessage.getSenderPort(),
                    nextServent.getListenerPort(), jobScheduleMessage.getSenderIpAddress(),
                    nextServent.getIpAddress(), scheduleType, receiverId);
            MessageUtil.sendMessage(jsm);
            return;
        }

        int serventCount = AppConfig.chordState.getAllNodeIdInfoMap().size();
        JobUtil.executeJobScheduling(serventCount, scheduleType);
    }
}
