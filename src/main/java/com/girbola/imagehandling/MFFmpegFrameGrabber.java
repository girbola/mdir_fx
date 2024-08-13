package com.girbola.imagehandling;

import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import common.utils.ImageUtils;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.girbola.videothumbnailing.JCodecVideoThumbUtils.grabListOfTimeLine;

public class MFFmpegFrameGrabber extends Task<List<BufferedImage>> {
    private FileInfo fileInfo;
    private ImageView imageView;
    private int image_width;
    private Timeline timeLine;

    public MFFmpegFrameGrabber(FileInfo fileInfo, ImageView imageView, double image_width) {
        this.fileInfo = fileInfo;
        this.imageView = imageView;
        this.image_width = ((int) image_width);
    }

    public List<BufferedImage> frameGrabber(Path path) {
        //final int thumbnailCount = 5; // Number of thumbnails to generate

        try (FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(path.toFile())) {
            try {
                frameGrabber.start();
            } catch (FFmpegFrameGrabber.Exception e) {
                Messages.sprintfError("Cannot start frameGrabber: " + e.getMessage());
                return null;
            }

            long totalLength = frameGrabber.getLengthInTime();
            List<Double> doubles = grabListOfTimeLine(totalLength);

            List<BufferedImage> list = new ArrayList<>();
            for (Double timeStamp : doubles) {
                long frameNumber = (long) Math.floor(timeStamp);
                frameGrabber.setTimestamp(frameNumber);
                frameGrabber.setFormat("jpg");
                Frame frame = frameGrabber.grabImage();
                Java2DFrameConverter converter = new Java2DFrameConverter();

                BufferedImage bufferedImage = converter.getBufferedImage(frame);
                if (bufferedImage != null) {
                    BufferedImage bufferedImage1 = ImageUtils.scaleImageWithAspectRatio(bufferedImage, 640);
                    list.add(bufferedImage1);
                    bufferedImage = null;
                    bufferedImage1 = null;
                } else {
                    Messages.sprintfError("Cannot grab bufferedImage: " + path + " frame number: " + frameNumber);
                    break;
                }
            }

            frameGrabber.stop();
            frameGrabber.release();

            return list;
        } catch (
                IOException e) {
            Messages.sprintf("Problem to grab thumbnails from video. MESSAGE: " + e.getMessage());
            return null;
        }
    }


    @Override
    protected List<BufferedImage> call() throws Exception {
        Messages.sprintf("Processing VideoThumbmaker: " + fileInfo.getOrgPath());
        List<BufferedImage> list = null;
        try {
            list = frameGrabber(Paths.get(fileInfo.getOrgPath()));
        } catch (Exception ex) {
            Messages.sprintfError("Exception ex: " + ex.getMessage());
            cancelled();
            return null;
        }
        return list;
    }

    @Override
    protected void succeeded() {
        List<BufferedImage> list = new ArrayList<>();
        try {
            list = get();
            if (list == null) {
                cancelled();
                return;
            }
        } catch (Exception e) {
            super.cancel();
            Messages.sprintfError("buffered failed: " + e);
            return;
        }

        StackPane pane = (StackPane) imageView.getParent();
        VBox rootPane = (VBox) pane.getParent();

        if (list == null || list.isEmpty()) {
            Messages.sprintfError("VideoThumbMaker video thumblist were null. returning: " + fileInfo.getOrgPath());
            pane.getChildren().add(new Label("Video. NP"));
            return;
        }


        Label label = new Label("Video");
        label.setStyle("-fx-text-fill: orange;");
        label.setMouseTransparent(true);
        StackPane.setAlignment(label, Pos.TOP_CENTER);
        pane.getChildren().add(label);

        VideoPreview videoPreview = new VideoPreview(list, imageView);
        Platform.runLater(() -> {
            imageView.setImage(videoPreview.getImage(0));
        });
        imageView.setUserData(list);

        rootPane.setOnMouseEntered(event -> {
            timeLine = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
                Messages.sprintf(" Showing video: " + timeLine.getCycleCount() + " File: " + fileInfo.getOrgPath() + videoPreview.getIndex());
                Image image = SwingFXUtils.toFXImage(videoPreview.showNextBufferedImage(), null);
                Messages.sprintf("image.getWidth()::: " + image.getWidth());
                Platform.runLater(() -> {
                    imageView.setImage(image);
                });
            }));
            timeLine.setCycleCount(5);
            timeLine.play();
        });
    }

    @Override
    protected void cancelled() {
        super.cancelled();
    }

    @Override
    protected void failed() {
        super.failed();
    }

}
