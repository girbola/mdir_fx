package com.girbola.videothumbnailing;

import com.girbola.messages.Messages;
import java.awt.image.BufferedImage;
import java.util.List;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class JavaCvVideoThumbUtilsTest {

    @Test
    public void testGetFrames() throws Exception {

        // Arrange
        Path file = Paths.get("C:\\Users\\marko\\OneDrive\\Kuvat\\Ruotsin reissu\\VID_20220412_232520.mp4");

        List<BufferedImage> list = JavaCvVideoThumbUtils.getList(file);
        Messages.sprintf("list size: " + list.size());
    }
}