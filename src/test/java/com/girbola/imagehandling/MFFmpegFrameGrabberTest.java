package com.girbola.imagehandling;

import com.girbola.fileinfo.FileInfo;
import javafx.scene.image.ImageView;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


public class MFFmpegFrameGrabberTest {

    @Test
    public void testFrameGrabber_Success() {
        Path fileName = Paths.get("src", "test", "resources", "test-material", "153976-817104245_tiny.mp4");

        FileInfo fileInfo = new FileInfo();
        fileInfo.setOrgPath(fileName.toString());
        ImageView imageView = new ImageView();
        MFFmpegFrameGrabber ffmpegFrameGrabber = new MFFmpegFrameGrabber(fileInfo, imageView, 1920);
        List<BufferedImage> result = ffmpegFrameGrabber.frameGrabber(Paths.get(fileInfo.getOrgPath()));

        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isEmpty());
    }

    @Test
    public void testFrameGrabber_WithIOException() {
        Path fileName = Paths.get("wrong-path", "153976-817104245_tiny.mp4");

        FileInfo fileInfo = new FileInfo();
        fileInfo.setOrgPath(fileName.toString());
        ImageView imageView = new ImageView();

        MFFmpegFrameGrabber ffmpegFrameGrabber = new MFFmpegFrameGrabber(fileInfo, imageView, 1920);
        List<BufferedImage> result = ffmpegFrameGrabber.frameGrabber(Paths.get(fileInfo.getOrgPath()));
        Assertions.assertNull(result);
    }

    @Test
    public void testFrameGrabber_WithFFmpegException() {
        Path fileName = Paths.get("wrong-path", "153976-817104245_tiny.xxx");

        FileInfo fileInfo = new FileInfo();
        fileInfo.setOrgPath(fileName.toString());
        ImageView imageView = new ImageView();

        MFFmpegFrameGrabber ffmpegFrameGrabber = new MFFmpegFrameGrabber(fileInfo, imageView, 1920);
        List<BufferedImage> result = ffmpegFrameGrabber.frameGrabber(Paths.get(fileInfo.getOrgPath()));
        Assertions.assertNull(result);
    }
}