package servent.handler;

import app.AppConfig;
import app.models.Point;
import servent.message.JobResultMessage;
import servent.message.Message;
import servent.message.MessageType;

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
        List<Point> resultPoints = jobResultMessage.getComputedPoints();
        String jobName = jobResultMessage.getJobName();
        int width = jobResultMessage.getWidth();
        int height = jobResultMessage.getHeight();

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
            ImageIO.write(newImage, "PNG", new File(jobName + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
