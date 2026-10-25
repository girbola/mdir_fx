package com.girbola.imagehandling;

import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import common.utils.ImageUtils;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

public class MFFmpegFrameGrabber extends Task<List<BufferedImage>> {
    private final FileInfo fileInfo;
    private final ImageView imageView;
    private final int imageWidth;
    private Timeline timeLine;

    public MFFmpegFrameGrabber(FileInfo fileInfo, ImageView imageView, double imageWidth) {
        this.fileInfo = fileInfo;
        this.imageView = imageView;
        this.imageWidth = (int) imageWidth;
    }

    public static List<Long> grabListOfTimeLineMicros(long totalDurationMicros) {
        List<Long> list = new ArrayList<>();
        double durationSec = totalDurationMicros / 1_000_000.0;

        if (durationSec <= 5) {
            list.add(0L);
        } else if (durationSec <= 10) {
            long step = totalDurationMicros / 2;
            list.add(0L);
            list.add(step);
        } else {
            long startPos = totalDurationMicros / 10; // 10% offset to avoid black screens
            long step = (totalDurationMicros - startPos) / 5;
            long current = startPos;
            for (int i = 0; i < 5; i++) {
                list.add(current);
                current += step;
            }
        }
        return list;
    }

    public List<BufferedImage> frameGrabber(Path path) {
        try (FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(path.toFile())) {
            try {
                frameGrabber.start();
            } catch (FFmpegFrameGrabber.Exception e) {
                Messages.sprintfError("Cannot start frameGrabber: " + e.getMessage());
                return null;
            }

            long totalLengthMicros = frameGrabber.getLengthInTime();
            List<Long> timestampsMicros = grabListOfTimeLineMicros(totalLengthMicros);

            List<BufferedImage> list = new ArrayList<>();

            // Re-use a single converter instance to prevent off-heap native memory leaks
            try (Java2DFrameConverter converter = new Java2DFrameConverter()) {
                for (Long timeStampMicros : timestampsMicros) {
                    if (isCancelled()) {
                        break;
                    }

                    frameGrabber.setTimestamp(timeStampMicros);
                    Frame frame = frameGrabber.grabImage();

                    if (frame != null) {
                        BufferedImage bufferedImage = converter.getBufferedImage(frame);
                        if (bufferedImage != null) {
                            BufferedImage scaledImage = ImageUtils.scaleImageWithAspectRatio(bufferedImage);
                            list.add(scaledImage);
                        }
                    } else {
                        Messages.sprintfError("Cannot grab frame for " + path + " at " + timeStampMicros + " µs");
                    }
                }
            }

            frameGrabber.stop();
            frameGrabber.release();

            return list;
        } catch (IOException e) {
            Messages.sprintf("Problem grabbing video thumbnails: " + e.getMessage());
            return null;
        }
    }

    @Override
    protected List<BufferedImage> call() throws Exception {
        if (fileInfo == null || fileInfo.getOrgPath() == null) {
            return null;
        }
        return frameGrabber(Paths.get(fileInfo.getOrgPath()));
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        List<BufferedImage> list = getValue();

        if (list == null || list.isEmpty()) {
            Messages.sprintfError("VideoThumbMaker returned null/empty for: " + fileInfo.getOrgPath());
            return;
        }

        // Safely navigate view hierarchy
        Node parentNode = imageView.getParent();
        HBox pane = null;
        VBox rootPane = null;

        if (parentNode instanceof StackPane) {
            Node grandParent = parentNode.getParent();
            if (grandParent instanceof HBox) {
                pane = (HBox) grandParent;
                if (pane.getParent() instanceof VBox) {
                    rootPane = (VBox) pane.getParent();
                }
            }
        } else if (parentNode instanceof HBox) {
            pane = (HBox) parentNode;
            if (pane.getParent() instanceof VBox) {
                rootPane = (VBox) pane.getParent();
            }
        }

        if (rootPane == null) {
            Messages.sprintfError("Could not resolve VBox root container for ImageView");
            return;
        }

        VideoPreview videoPreview = new VideoPreview(list, imageView);

        // Directly set initial image (succeeded runs on JavaFX thread automatically)
        imageView.setImage(videoPreview.getImage(0));
        imageView.setUserData(list);

        // Setup clean hover animation handlers
        VBox finalRootPane = rootPane;
        finalRootPane.setOnMouseEntered(event -> {
            if (timeLine != null) {
                timeLine.stop();
            }
            timeLine = new Timeline(new KeyFrame(Duration.seconds(0.8), e -> {
                Image image = SwingFXUtils.toFXImage(videoPreview.showNextBufferedImage(), null);
                imageView.setImage(image);
            }));
            timeLine.setCycleCount(Timeline.INDEFINITE);
            timeLine.play();
        });

        finalRootPane.setOnMouseExited(event -> {
            if (timeLine != null) {
                timeLine.stop();
            }
            // Reset to default static thumbnail on exit
            imageView.setImage(videoPreview.getImage(0));
        });
    }

    @Override
    protected void cancelled() {
        if (timeLine != null) {
            timeLine.stop();
        }
        super.cancelled();
    }

    @Override
    protected void failed() {
        if (timeLine != null) {
            timeLine.stop();
        }
        super.failed();
    }
}
