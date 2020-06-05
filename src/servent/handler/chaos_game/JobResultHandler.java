package servent.handler.chaos_game;

import app.AppConfig;
import app.models.Point;
import app.models.ServentInfo;
import app.util.ImageUtil;
import servent.handler.MessageHandler;
import servent.message.chaos_game.JobResultMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class JobResultHandler implements MessageHandler {

    private Message clientMessage;

    public JobResultHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() != MessageType.JOB_RESULT) {
            AppConfig.timestampedErrorPrint("Job result handler got a message that is not JOB_RESULT");
            return;
        }

        JobResultMessage jobResultMessage = (JobResultMessage) clientMessage;
        int receiverId = jobResultMessage.getFinalReceiverId();
        List<Point> resultPoints = jobResultMessage.getComputedPoints();
        String jobName = jobResultMessage.getJobName();
        int width = jobResultMessage.getWidth();
        int height = jobResultMessage.getHeight();
        double proportion = jobResultMessage.getProportion();

        // if I am not intended final receiver then just pass message further
        if (receiverId != AppConfig.myServentInfo.getId()) {
            ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(receiverId);

            JobResultMessage jrm = new JobResultMessage(jobResultMessage.getSenderPort(),
                    nextServent.getListenerPort(), jobResultMessage.getSenderIpAddress(),
                    nextServent.getIpAddress(), receiverId, jobName, resultPoints, width, height, proportion);
            MessageUtil.sendMessage(jrm);
            return;
        }

        AppConfig.lamportMutex.releaseMyCriticalSection();
        ImageUtil.renderImage(jobName, "", width, height, proportion, resultPoints);
    }
}
