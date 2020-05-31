package servent.handler;

import app.AppConfig;
import app.models.JobExecution;
import servent.message.AskJobFractalIDResultMessage;
import servent.message.JobFractalIDResultMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

public class AskJobFractalIDResultHandler implements MessageHandler {

    private Message clientMessage;

    public AskJobFractalIDResultHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() != MessageType.ASK_JOB_FRACTALID_RESULT) {
            AppConfig.timestampedErrorPrint("Ask job fractalID result handler got a message that is not ASK_JOB_FRACTALID_RESULT");
            return;
        }

        AskJobFractalIDResultMessage resultMessage = (AskJobFractalIDResultMessage) clientMessage;
        JobExecution jobExecution = AppConfig.chordState.getExecutionJob();

        // send back
        // todo: slanje
        JobFractalIDResultMessage message = new JobFractalIDResultMessage(
                AppConfig.myServentInfo.getListenerPort(),
                resultMessage.getSenderPort(),
                AppConfig.myServentInfo.getIpAddress(),
                resultMessage.getSenderIpAddress(),
                jobExecution.getJobName(), jobExecution.getFractalId(), jobExecution.getComputedPoints(),
                jobExecution.getWidth(), jobExecution.getHeight(), jobExecution.getProportion());
        MessageUtil.sendMessage(message);
    }
}
