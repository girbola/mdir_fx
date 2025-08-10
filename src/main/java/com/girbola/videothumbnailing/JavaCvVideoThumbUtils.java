package com.girbola.videothumbnailing;

import com.girbola.messages.Messages;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class JavaCvVideoThumbUtils {

//    public static List<BufferedImage> getList(String fileName) {
//        try {
//            return getList(new File(fileName));
//        } catch (Exception e) {
//            Messages.sprintfError("FrameGrabber exception: " + e.getMessage());
//            return null;
//        }
//    }
//
//    public static double getVideoLength(File file) {
//        // Placeholder for actual implementation
//        // This method should return the length of the video in seconds
//        return -1; // Return -1 if unable to determine length
//    }


    public static List<BufferedImage> getList(File file) throws Exception {

        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(file);
        if(grabber.getLengthInFrames() <= 0) {
            Messages.sprintfError("No frames found in the video file: " + file);
            return null;
        }

        for (Map.Entry<String, String> stringStringEntry : grabber.getMetadata().entrySet()) {
            Messages.sprintf("Metadata: " + stringStringEntry.getKey() + " = " + stringStringEntry.getValue());
        }

        grabber.start();

        int totalFrames = grabber.getLengthInFrames();
        int step = totalFrames / 5;

        for (int i = 0; i < 5; i++) {
            long startTime = System.currentTimeMillis();

            int targetFrame = i * step;
            grabber.setFrameNumber(targetFrame);
            Messages.sprintf("startTime: + " + startTime);
            Frame frame = grabber.grabImage();

            Messages.sprintf("----GRABBING took: " + (System.currentTimeMillis() - startTime) + " ms for frame: " + targetFrame);
            startTime = System.currentTimeMillis();

            frame.imageWidth = 640; // Set desired width
            frame.imageHeight = 480; // Set desired height

            Messages.sprintf("---RESIZING took: " + (System.currentTimeMillis() - startTime) + " ms for frame: " + targetFrame);
            startTime = System.currentTimeMillis();

            BufferedImage bufferedImage = convert(frame);
            Messages.sprintf("--bufferedImage convert took: " + (System.currentTimeMillis() - startTime) + " ms for frame: " + targetFrame);

            if (bufferedImage == null) {
                System.out.println("Failed to grab frame at position: " + targetFrame);
                continue;
            } else {
                System.out.println("Successfully grabbed frame at position: " + bufferedImage.getWidth() + " height::: " + bufferedImage.getHeight());
            }
            if (frame != null) {
                System.out.println("Grabbed frame at position: " + targetFrame);
                // You can save or process the frame here
            }
        }

        grabber.stop();
    }


    public static BufferedImage convert(Frame frame) {
        Java2DFrameConverter converter = new Java2DFrameConverter();
        return converter.convert(frame);
    }


}
