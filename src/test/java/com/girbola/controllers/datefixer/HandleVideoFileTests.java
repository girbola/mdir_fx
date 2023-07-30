package com.girbola.controllers.datefixer;

import com.girbola.fileinfo.FileInfo;
import com.girbola.fileinfo.FileInfoUtils;
import com.girbola.messages.Messages;
import javafx.concurrent.Task;
import javafx.scene.image.ImageView;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class HandleVideoFileTests {


    @Test
    public void testHandleVideoFileTests() {
        Path file = Paths.get("C:\\Users\\marko\\OneDrive\\Kuvat\\Ruotsin reissu\\VID_20220412_235646.mp4");
        FileInfo fileInfo = null;
        try {
            fileInfo = FileInfoUtils.createFileInfo(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Task<List<BufferedImage>> handleVideoFile = new HandleVideoFile(file, fileInfo, new ImageView(), 200 );

        Thread thread = new Thread(handleVideoFile, "handleVideoFile");

        handleVideoFile.setOnSucceeded((e) -> {
            Messages.sprintf("Successfully done");
        });


        handleVideoFile.setOnFailed((e) -> {
            Messages.sprintf("Successfully failed");
        });


        handleVideoFile.setOnCancelled((e) -> {
            Messages.sprintf("Successfully cancelled");
        });

        thread.start();



    }

}
