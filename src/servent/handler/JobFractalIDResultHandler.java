package servent.handler;

import app.AppConfig;
import app.models.Point;
import app.models.ServentInfo;
import servent.message.JobFractalIDResultMessage;
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

public class JobFractalIDResultHandler implements MessageHandler {

    private Message clientMessage;

    public JobFractalIDResultHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() != MessageType.JOB_FRACTALID_RESULT) {
            AppConfig.timestampedErrorPrint("Job fractalID result handler got a message that is not JOB_FRACTALID_RESULT");
            return;
        }

        JobFractalIDResultMessage resultMessage = (JobFractalIDResultMessage) clientMessage;
        int receiverId = resultMessage.getFinalReceiverId();
        List<Point> resultPoints = resultMessage.getComputedPoints();
        String jobName = resultMessage.getJobName();
        String fractalId = resultMessage.getFractalId();
        int width = resultMessage.getWidth();
        int height = resultMessage.getHeight();
        double proportion = resultMessage.getProportion();

        // if I am not intended final receiver then just pass message further
        if (receiverId != AppConfig.myServentInfo.getId()) {
            ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(receiverId);

            JobFractalIDResultMessage message = new JobFractalIDResultMessage(
                    resultMessage.getSenderPort(), nextServent.getListenerPort(),
                    resultMessage.getSenderIpAddress(), nextServent.getIpAddress(), receiverId,
                    jobName, fractalId, resultPoints, width, height, proportion);
            MessageUtil.sendMessage(message);
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
            ImageIO.write(newImage, "PNG", new File("fractals/" + jobName + fractalId + "_" + proportion + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
