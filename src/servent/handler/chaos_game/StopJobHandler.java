package servent.handler.chaos_game;

import app.AppConfig;
import app.models.FractalIdJob;
import app.models.JobExecution;
import app.models.JobScheduleType;
import app.models.ServentInfo;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.chaos_game.JobScheduleMessage;
import servent.message.chaos_game.StopJobMessage;
import servent.message.util.MessageUtil;

import java.util.Map;

public class StopJobHandler implements MessageHandler {

    private Message clientMessage;

    public StopJobHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() != MessageType.STOP_JOB) {
            AppConfig.timestampedErrorPrint("Stop job handler got a message that is not STOP_JOB");
            return;
        }

        StopJobMessage stopJobMessage = (StopJobMessage) clientMessage;
        String jobName = stopJobMessage.getJobName();

        // if I am doing that job then stop
        JobExecution jobExecution = AppConfig.chordState.getExecutionJob();
        if (jobExecution != null && jobExecution.getJobName().equals(jobName)) {
            jobExecution.stop();
            AppConfig.chordState.setExecutionJob(null);
        }
        // remove that job from my servent jobs division map and list of all jobs
        AppConfig.chordState.removeJob(jobName);

        // I sent the message
        if (stopJobMessage.getSenderPort() == AppConfig.myServentInfo.getListenerPort() &&
                stopJobMessage.getSenderIpAddress().equals(AppConfig.myServentInfo.getIpAddress())) {
            AppConfig.timestampedStandardPrint("Stop job message for job \'" + jobName + "\' made a circle.");

            if (AppConfig.chordState.getActiveJobsCount() > 0) {        // need to reschedule jobs
                sendReschedulingMessage(JobScheduleType.JOB_REMOVED);
            }
            return;
        }

        // send to first successor to stop the job
        StopJobMessage message = new StopJobMessage(stopJobMessage.getSenderPort(),
                AppConfig.chordState.getNextNodePort(),
                stopJobMessage.getSenderIpAddress(),
                AppConfig.chordState.getNextNodeIpAddress(),
                jobName);
        MessageUtil.sendMessage(message);
    }

    public static boolean sendReschedulingMessage(JobScheduleType scheduleType) {
        for (Map.Entry<Integer, FractalIdJob> entry: AppConfig.chordState.getServentJobs().entrySet()) {
            int executorId = entry.getKey();
            ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(executorId);

            JobScheduleMessage jsm = new JobScheduleMessage(AppConfig.myServentInfo.getListenerPort(),
                    nextServent.getListenerPort(), AppConfig.myServentInfo.getIpAddress(),
                    nextServent.getIpAddress(), scheduleType, executorId);
            MessageUtil.sendMessage(jsm);

            return true;
        }
        return false;
    }
}
