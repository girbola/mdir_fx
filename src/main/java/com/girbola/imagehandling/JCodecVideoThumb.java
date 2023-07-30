package com.girbola.imagehandling;

import com.girbola.Main;
import com.girbola.imagehandling.jcodec.AWTUtil;
import com.girbola.messages.Messages;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.opencv.opencv_core.IplImage;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.*;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Picture;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class JCodecVideoThumb {
    public static List<BufferedImage> findThumbnails(Path file) {
        boolean found = false;
        try {
            List<BufferedImage> bufferedImageList = frameGrabberThumber(file.toFile());
            if (bufferedImageList != null) {
                return bufferedImageList;
            }
        } catch (Exception ignored) {


        }
        if (!found) {
            try {
                List<BufferedImage> bufferedImageList = ffMpegFrameGrabberThumber(file);
                if (bufferedImageList == null) {
                    System.out.println("bufferedImageList were null");
                    return null;
                }
                System.out.println("Returning bufferedImageList!");
                return bufferedImageList;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static List<BufferedImage> frameGrabberThumber(File file) throws FFmpegFrameGrabber.Exception {
        List<BufferedImage> bufferedImageList;
        double duration = getVideoLenght(file);
        if (duration < 0) {
            return null;
        }
        List<Double> thumbnailSnapShotList = defineSnapShotsOfThumbnails(duration);
        bufferedImageList = tryGetVideoThumbsUsingFrameGrab(file, thumbnailSnapShotList);
        if (bufferedImageList == null || thumbnailSnapShotList.isEmpty()) {
            Messages.sprintf("Video thumbnail arrary were empty");
            return null;
        }
        if (bufferedImageList != null && !bufferedImageList.isEmpty()) {
            System.out.println(" bufferedImageList: " + bufferedImageList.size());
        } else {
            System.out.println("bufferedImageList null? " + (bufferedImageList == null ? true : false));
        }

        System.out.println("Total duration: " + duration + " GetList done: " + thumbnailSnapShotList.size());
        return bufferedImageList;
    }

    private static List<BufferedImage> tryGetVideoThumbsUsingFrameGrab(File file, List<Double> list) throws FFmpegFrameGrabber.Exception {
        List<BufferedImage> buff_list = new ArrayList<>();

        try {
            FrameGrab grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(file));
            if (grab == null) {
                return null;
            }
            grab.seekToSecondSloppy(list.get(0));
            if (list.size() > 1) {
                for (int i = 1; i < list.size(); i++) {
                    if (Main.getProcessCancelled()) {
                        return null;
                    }
                    double sec = list.get(i);
                    Messages.sprintf("createFrameGrab sec: " + i);
                    Picture picture = grab.getNativeFrame();
                    BufferedImage buff = AWTUtil.toBufferedImage(picture);
                    buff = Thumbnails.of(buff).height(150).keepAspectRatio(true).asBufferedImage();
                    if (buff != null) {
                        buff_list.add(buff);
                        Messages.sprintf("sec before seek: " + sec);
                        grab.seekToSecondSloppy(sec);
                    }
                }
            }
        } catch (IOException | JCodecException e) {
            System.out.println("Exception is thrown when creating frame grab");
            e.printStackTrace();
            return null;
        }
        return buff_list;
    }

    private static List<BufferedImage> ffMpegFrameGrabberThumber(Path fileName) throws FFmpegFrameGrabber.Exception {
        List<BufferedImage> bufferedImageList = new ArrayList<>();

        try {

            FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(fileName.toFile());
            frameGrabber.start();

            long videoLength = frameGrabber.getLengthInTime();
            double interval = videoLength / 5;


            Java2DFrameConverter frameConverter = new Java2DFrameConverter();

            long length = frameGrabber.getLengthInTime();

            for (int i = 0; i < 5; i++) {
                double timeInSeconds = interval * i;
                frameGrabber.setTimestamp((long) timeInSeconds);
                bufferedImageList.add(frameConverter.convert(frameGrabber.grabImage()));
            }

            frameGrabber.stop();
            Messages.sprintf("=======bufferedImageList" + bufferedImageList.size());
            return bufferedImageList;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }


    }

    private static List<Double> defineSnapShotsOfThumbnails(double duration) {
        List<Double> list = new ArrayList<>();
        if (duration <= 5) {
            list.add(0.0);
        } else if (duration <= 10 && duration >= 6) {
            double startPos = 0;
            double ratio = (duration / 2); //
            list.add(startPos);
            for (int i = 0; i < 1; i++) {
                startPos += ratio;
                list.add(startPos);
            }
        } else {
            double startPos = (duration / 10); // = 3. 10% from duration. This could avoid black screen as a start image
            double ratio = ((duration - startPos) / 5); //
            list.add(startPos);
            for (int i = 0; i < 4; i++) {
                startPos += ratio;
                list.add(startPos);
            }
        }
        return list;
    }

    public static double getVideoLenght(File file) {
        Messages.sprintf("getVideoLenght: " + file);
        Format f = null;
        try {
            f = JCodecUtil.detectFormat(file);
            Demuxer d = JCodecUtil.createDemuxer(f, file);
            DemuxerTrack vt = d.getVideoTracks().get(0);
            DemuxerTrackMeta dtm = vt.getMeta();
            return dtm.getTotalDuration();
        } catch (IOException e) {
            Messages.sprintfError("JCodec not supporting current file: " + file);
            return -1;
        }
    }
}