package com.girbola.imagehandling;

import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.videothumbnailing.JavaCvVideoThumbUtils;
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
import lombok.extern.slf4j.Slf4j;
//import org.bytedeco.javacv.FFmpegFrameGrabber;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

@Slf4j
public class VideoThumbMaker extends Task<List<BufferedImage>> {

    private FileInfo fileInfo;
    private ImageView imageView;
    private double image_width;
    private Timeline timeLine;

    public VideoThumbMaker(FileInfo fileInfo, ImageView imageView, double image_width) {
        this.fileInfo = fileInfo;
        this.imageView = imageView;
        this.image_width = image_width;
    }

    @Override
    protected List<BufferedImage> call() throws Exception {
        Messages.sprintf("Processing VideoThumbmaker: " + fileInfo.getOrgPath());
        List<BufferedImage> list = null;
        try {
            list = JavaCvVideoThumbUtils.getList(new File(fileInfo.getOrgPath()));
            if (list == null) {
                return null;
            }
        } catch (Exception ex) {
            log.error("Trouble to get video thumbnails. Exception: " + ex.getMessage());
            cancelled();
            return null;
        }
        Messages.sprintf("list size is: " + list.size());
        return list;
    }

    @Override
    protected void succeeded() {
        List<BufferedImage> list = null;
        try {
            list = get();
            if (list == null) {
                cancelled();
                return;
            }
        } catch (Exception e) {
            super.cancel();
            System.err.println("buffered failed: " + e);
            return;
        }

        StackPane pane = (StackPane) imageView.getParent();
        VBox rootPane = (VBox) pane.getParent();

        if (list == null || list.isEmpty()) {
            System.err.println("VideoThumbMaker video thumblist were null. returning: " + fileInfo.getOrgPath());
            pane.getChildren().add(new Label("Video. NP"));
            return;
        }

        Label videoTextLabel = new Label("Video");
        videoTextLabel.setMouseTransparent(true);
        videoTextLabel.getStyleClass().add("videoTextLabel");
        StackPane.setAlignment(videoTextLabel, Pos.TOP_CENTER);
        pane.getChildren().add(videoTextLabel);

        VideoPreview videoPreview = new VideoPreview(list, imageView);
        imageView.setImage(videoPreview.getImage(0));
        imageView.setUserData(list);

        rootPane.setOnMouseEntered(event -> {
            Messages.sprintf("m e");
            timeLine = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
                Image image = SwingFXUtils.toFXImage(videoPreview.showNextBufferedImage(), null);
                Platform.runLater(() -> imageView.setImage(image));
            }));
            timeLine.setCycleCount(6);
            timeLine.play();
        });
        rootPane.setOnMouseExited(event -> {
            if (timeLine != null) {
                timeLine.stop();
            }
            imageView.setImage(videoPreview.getImage(0));
        });
    }

//    private FFmpegFrameGrabber createVideoThumb(String fileName) throws FFmpegFrameGrabber.Exception {
//        FFmpegFrameGrabber frameGrabber = FFmpegFrameGrabber.createDefault(fileName);
//        Messages.sprintf("=======lengthInFrames" + frameGrabber.toString());
//        return frameGrabber;
//    }

    @Override
    protected void cancelled() {
    }

    @Override
    protected void failed() {
    }

}
