package com.girbola.videothumbnailing;

import com.girbola.configuration.UIContants;
import com.girbola.messages.Messages;
import common.utils.ImageUtils;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import net.coobird.thumbnailator.Thumbnails;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaCvVideoThumbUtils {
    private static final int THUMBNAIL_COUNT = 5;
    private static final Logger logger = LoggerFactory.getLogger(JavaCvVideoThumbUtils.class);

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


    public static List<BufferedImage> getList(String path) throws IOException {
        return getList(Paths.get(path));
    }


    public static List<BufferedImage> getList(Path path) throws IOException {
        return getList(path.toFile());
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }


    public static List<BufferedImage> getList(File file) throws IOException {

        List<BufferedImage> listOfVideoThumbnails = new ArrayList<>();
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(file)) {
            grabber.start();

            int totalFrames = grabber.getLengthInFrames();
            if (totalFrames <= 0) {
                Messages.sprintfError("No frames found in the video file: " + file);
                return null;
            }

            int step = Math.max(1, totalFrames / THUMBNAIL_COUNT);

            for (int i = 0; i < THUMBNAIL_COUNT; i++) {
                int frameIndex = clamp(i * step, 0, totalFrames - 1);

                grabber.setFrameNumber(frameIndex);

                Frame frame = grabber.grabImage();
                if (frame == null) {
                    logger.error("Grabbing did not work on this file because frame were null at index: " + frameIndex);
                    continue;
                }

                BufferedImage scaled = ImageUtils.convertFrameToBufferedImageWithScalingThumbnail(frame, (int) UIContants.THUMBNAIL_MAX_HEIGHT);
                if (scaled != null) {
                    listOfVideoThumbnails.add(scaled);
                }
            }

            grabber.stop();

            return listOfVideoThumbnails;

        } catch (Exception e) {
            logger.error("Error while grabbing video for file {}", file, e);
            return null;
        }
    }
}
