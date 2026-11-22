package com.girbola.imagehandling;

import com.girbola.fileinfo.FileInfo;
import javafx.application.Platform;
import javafx.concurrent.WorkerStateEvent;
import javafx.scene.image.ImageView;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;


public class MFFmpegFrameGrabberTest {

    @BeforeAll
    static void startJavaFx() throws InterruptedException {
        CountDownLatch started = new CountDownLatch(1);
        try {
            Platform.startup(started::countDown);
            if (!started.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("JavaFX did not start");
            }
        } catch (IllegalStateException alreadyStarted) {
            if (!"Toolkit already initialized".equals(alreadyStarted.getMessage())) {
                throw alreadyStarted;
            }
        }
    }

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

    @Test
    public void heicGridIsDecodedAsCompletePrimaryImage() {
        Path fileName = Paths.get("src", "test", "resources", "test-material", "IMG_5436.HEIC");
        FileInfo fileInfo = new FileInfo();
        fileInfo.setOrgPath(fileName.toString());
        MFFmpegFrameGrabber grabber = new MFFmpegFrameGrabber(fileInfo, new ImageView(), 240);

        List<BufferedImage> result = grabber.frameGrabber(fileName);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        BufferedImage image = result.getFirst();
        Assertions.assertEquals(290, image.getHeight());
        Assertions.assertEquals(0.75, (double) image.getWidth() / image.getHeight(), 0.005);
        double[] expectedRed = {74, 85, 89, 105};
        for (int quadrant = 0; quadrant < expectedRed.length; quadrant++) {
            long totalRed = 0;
            for (int x = 0; x < 20; x++) {
                for (int y = 0; y < 20; y++) {
                    int px = (int) ((quadrant % 2 * 0.5 + (x + 0.5) * 0.025) * image.getWidth());
                    int py = (int) ((quadrant / 2 * 0.5 + (y + 0.5) * 0.025) * image.getHeight());
                    totalRed += new Color(image.getRGB(px, py)).getRed();
                }

            }
            Assertions.assertEquals(expectedRed[quadrant], totalRed / 400.0, 15.0,
                    "HEIC image quadrant " + quadrant);
        }
    }

    @Test
    void heicThumbnailDisplaysWithoutVideoContainerHierarchy() throws Exception {
        Path fileName = Paths.get("src", "test", "resources", "test-material", "IMG_5436.HEIC");
        CountDownLatch completed = new CountDownLatch(1);
        FutureTask<ImageView> setup = new FutureTask<>(() -> {
            FileInfo fileInfo = new FileInfo();
            fileInfo.setOrgPath(fileName.toString());
            ImageView imageView = new ImageView();
            MFFmpegFrameGrabber task = new MFFmpegFrameGrabber(fileInfo, imageView, 240);
            task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, event -> completed.countDown());
            task.addEventHandler(WorkerStateEvent.WORKER_STATE_FAILED, event -> completed.countDown());
            Thread worker = new Thread(task, "heic-test");
            worker.setDaemon(true);
            worker.start();
            return imageView;
        });
        Platform.runLater(setup);
        ImageView view = setup.get(10, TimeUnit.SECONDS);
        Assertions.assertTrue(completed.await(30, TimeUnit.SECONDS));
        FutureTask<Void> verify = new FutureTask<>(() -> {
            Assertions.assertNotNull(view.getImage());
            Assertions.assertEquals(0.75, view.getImage().getWidth() / view.getImage().getHeight(), 0.005);
            return null;
        });
        Platform.runLater(verify);
        verify.get(10, TimeUnit.SECONDS);
    }

    @Test
    void heicCacheReturnsIndependentImagesAndInvalidatesChangedFiles(@TempDir Path directory) throws Exception {
        Path source = Paths.get("src", "test", "resources", "test-material", "IMG_5436.HEIC");
        Path file = Files.copy(source, directory.resolve("image.HEIC"));
        FileInfo fileInfo = new FileInfo();
        fileInfo.setOrgPath(file.toString());
        MFFmpegFrameGrabber grabber = new MFFmpegFrameGrabber(fileInfo, new ImageView(), 240);

        BufferedImage first = grabber.frameGrabber(file).getFirst();
        int originalPixel = first.getRGB(0, 0);
        first.setRGB(0, 0, ~originalPixel);
        BufferedImage cached = grabber.frameGrabber(file).getFirst();
        Assertions.assertEquals(originalPixel, cached.getRGB(0, 0));
        Assertions.assertNotSame(first, cached);

        Files.writeString(file, "not a HEIC image");
        Files.setLastModifiedTime(file, FileTime.fromMillis(System.currentTimeMillis() + 2000));
        Assertions.assertNull(grabber.frameGrabber(file));
    }
}