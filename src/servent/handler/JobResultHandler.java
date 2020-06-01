package servent.handler;

import app.AppConfig;
import app.models.Point;
import app.models.ServentInfo;
import servent.message.JobResultMessage;
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

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        WritableRaster writableRaster = image.getRaster();
        int[] rgb = new int[3];
        rgb[0] = 255;
        for (Point p : resultPoints) {
            writableRaster.setPixel(p.getX(), p.getY(), rgb);
        }
        BufferedImage newImage = new BufferedImage(writableRaster.getWidth(), writableRaster.getHeight(),
                BufferedImage.TYPE_3BYTE_BGR);
        newImage.setData(writableRaster);
        try {
            ImageIO.write(newImage, "PNG", new File("fractals/" + jobName + "_" + proportion + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
