package common.utils;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

import org.bytedeco.javacv.*;
import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.indexer.*;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_imgproc.*;
import org.bytedeco.opencv.opencv_calib3d.*;
import org.bytedeco.opencv.opencv_objdetect.*;

import javax.imageio.ImageIO;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_calib3d.*;
import static org.bytedeco.opencv.global.opencv_objdetect.*;

import java.io.File;

public class JCodecVideoThumbMakerTest {

    @Test
    public void testMP4() throws FrameGrabber.Exception {
        File file = new File("/home/gerbiloi/Pictures/Riston kuvat/Kuvia/Videot 200911 (Gran Kanarialla)/16112009 (Suite Monte Golf parveke esitys).mp4");

        FrameGrabber grabber = FrameGrabber.createDefault(file);
        //grabber.start();


        System.out.println("getFormat: " + grabber.getImageWidth());
        System.out.println("2AERAERAGERA: " + grabber.toString());
        //grabber.stop();

    }

    @Test
    public void testMP4FFMPEG() throws FrameGrabber.Exception {
        File file = new File("D:\\Ubuntu\\Kuvat\\Riston kuvat\\Kuvia\\Videot 200911 (Gran Kanarialla)\\16112009 (Suite Monte Golf parveke esitys).mp4");
        FFmpegFrameGrabber frameGrabber = null;

        try {
            frameGrabber = new FFmpegFrameGrabber(file);
            frameGrabber.start();
            long videoLength = frameGrabber.getLengthInTime();
            double interval = videoLength / 5;

            System.out.println("lengthInTime: " + videoLength);
            Java2DFrameConverter frameConverter = new Java2DFrameConverter();
            for (int i = 0; i < videoLength; i++) {
                double timeInSeconds = interval * i;
                frameGrabber.setTimestamp((long) timeInSeconds);

                BufferedImage bufferedImage = frameConverter
                        .convert(frameGrabber.grabImage());
                ImageIO.write(bufferedImage, "png", new File("C:\\Temp\\testi" + i + " .png"));

                System.out.println("frameGrabber bufferedImage " + bufferedImage.getWidth());

                System.out.println("2AERAERAGERA: " + frameGrabber.toString());
            }
            frameGrabber.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}
