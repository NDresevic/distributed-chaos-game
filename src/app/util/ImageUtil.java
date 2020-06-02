package app.util;

import app.AppConfig;
import app.models.Point;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class ImageUtil {

    public static void renderImage(String jobName, String fractalID, int width, int height, double proportion,
                                   List<Point> points) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        WritableRaster writableRaster = image.getRaster();
        int[] rgb = new int[3];
        rgb[0] = 255;
        for (Point p : points) {
            writableRaster.setPixel(p.getX(), p.getY(), rgb);
        }

        BufferedImage newImage = new BufferedImage(writableRaster.getWidth(), writableRaster.getHeight(),
                BufferedImage.TYPE_3BYTE_BGR);
        newImage.setData(writableRaster);
        try {
            if (!fractalID.equals("")) {
                ImageIO.write(newImage, "PNG", new File("fractals/" + jobName + fractalID + "_" + proportion + ".png"));
            } else {
                ImageIO.write(newImage, "PNG", new File("fractals/" + jobName + "_" + proportion + ".png"));
            }
            AppConfig.timestampedStandardPrint("Fractal image rendered.");
        } catch (IOException e) {
            AppConfig.timestampedErrorPrint(e.getMessage());
            e.printStackTrace();
        }
    }
}
