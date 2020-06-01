package servent.handler;

import app.AppConfig;
import app.models.JobExecution;
import app.models.ServentInfo;
import servent.message.*;
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
        int receiverId = resultMessage.getFinalReceiverId();
        JobExecution jobExecution = AppConfig.chordState.getExecutionJob();

        // if I am not intended final receiver then just pass message further
        if (receiverId != AppConfig.myServentInfo.getId()) {
            ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(receiverId);
            AskJobFractalIDResultMessage message = new AskJobFractalIDResultMessage(
                    clientMessage.getSenderPort(),
                    nextServent.getListenerPort(),
                    clientMessage.getSenderIpAddress(),
                    nextServent.getIpAddress(), resultMessage.getJobName(), receiverId);
            MessageUtil.sendMessage(message);
            return;
        }

        // send back the result
        int finalReceiverId = AppConfig.chordState.getNodeIdForServentPortAndAddress(resultMessage.getSenderPort(), resultMessage.getSenderIpAddress());
        ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(finalReceiverId);

        JobFractalIDResultMessage message = new JobFractalIDResultMessage(
                AppConfig.myServentInfo.getListenerPort(), nextServent.getListenerPort(),
                AppConfig.myServentInfo.getIpAddress(), nextServent.getIpAddress(), finalReceiverId,
                jobExecution.getJobName(), jobExecution.getFractalId(), jobExecution.getComputedPoints(),
                jobExecution.getWidth(), jobExecution.getHeight(), jobExecution.getProportion());
        MessageUtil.sendMessage(message);
    }
}
