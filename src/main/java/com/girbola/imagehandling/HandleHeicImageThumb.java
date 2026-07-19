package com.girbola.imagehandling;

import com.girbola.Main;
import com.girbola.fileinfo.FileInfo;
import common.utils.FfmpegRunner;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public class HandleHeicImageThumb extends Task<Image> {

    private final String ERROR = HandleHeicImageThumb.class.getSimpleName();

    private final Path thumbImage;
    private final double image_width;
    private final ImageView imageView;

    public HandleHeicImageThumb(Path path, double width, ImageView imageView) {
        this.thumbImage = path;
        this.image_width = width;
        this.imageView = imageView;
    }


//    public static Task<Image> handleHeicThumbTask(FileInfo fileinfo, double thumb_x_MAX, ImageView imageView) {
//
////✅¸ JavaFX Task using the wrapper
//        Task<BufferedImage> task = new Task<>() {
//
//            @Override
//            protected BufferedImage call() throws Exception {
//
//                int width = 4032;
//                int height = 3024;
//
//                FfmpegRunner.CancelFlag cancel = new FfmpegRunner.CancelFlag();
//
//                try (FfmpegRunner runner =
//                             new FfmpegRunner("/usr/bin/ffmpeg", "image.heic")) {
//
//// ✅ Progress thread (still single FFmpeg process)
//                    Thread progressThread = new Thread(() -> {
//                        try {
//                            runner.readProgress(
//                                    p -> updateProgress(p, 1),
//                                    1,
//                                    cancel
//                            );
//                        } catch (IOException ignored) {}
//                    });
//                    progressThread.start();
//
//// ✅ Binary frame read (no parsing)
//                    byte[] rgb = runner.readRawFrame(width, height);
//                    runner.waitFor(15_000);
//
//                    return rgbToBufferedImage(rgb, width, height);
//                }
//            }
//
//            @Override
//            protected void cancelled() {
//// triggers cooperative cancellation
//            }
//        };
//    }

    public static Task<Image> handleHeicThumb(FileInfo fileInfo, double thumb_x_MAX, ImageView imageView) {

        return new Task<>() {
            @Override
            protected Image call() throws Exception {
                if (isCancelled()) {
                    Main.setProcessCancelled(true);
                    return null;
                }
                if (Main.getProcessCancelled()) {
                    Main.setProcessCancelled(true);
                    cancel();
                    return null;
                }

//                FfmpegRunner ef = new FfmpegRu¸nner(path, inputPath);


                return null;
            }
        };
    }

    @Override
    protected Image call() throws Exception {
        return null;
    }

    @Override
    protected void failed() {
        super.failed();
    }
    @Override
    protected void cancelled() {
        super.cancelled();
    }
    @Override
    protected void succeeded() {
        super.succeeded();
        //imageView.setImage();
    }
}
