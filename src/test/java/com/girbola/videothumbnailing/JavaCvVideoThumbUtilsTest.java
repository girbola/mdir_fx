package com.girbola.videothumbnailing;

import com.girbola.configuration.UIContants;
import com.girbola.controllers.datefixer.utils.GUI_Methods;
import com.girbola.messages.Messages;
import java.awt.image.BufferedImage;
import java.util.List;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class JavaCvVideoThumbUtilsTest {

    @Test
    public void testGetFrames() throws Exception {

        // Arrange
        Path file = Paths.get("src", "test", "resources", "test-material", "153976-817104245_tiny.mp4");

        List<BufferedImage> list = JavaCvVideoThumbUtils.getList(file);
        Messages.sprintf("list size: " + list.size());
        assertTrue(list.size() == 5);
        for (BufferedImage bi : list) {
            assertTrue(bi.getHeight() == UIContants.THUMBNAIL_MAX_HEIGHT);
        }
    }
}