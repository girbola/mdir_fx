package com.girbola.videothumbnailing;

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
        Path file = Paths.get("/Users/lokkamarko/Movies/2025-02-19 14-04-30.mkv");

    JavaCvVideoThumbUtils.getFrames(file);

    }
}