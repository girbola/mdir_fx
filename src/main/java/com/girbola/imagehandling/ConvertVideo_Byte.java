
package com.girbola.imagehandling;

import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.thumbinfo.ThumbInfo;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class ConvertVideo_Byte extends Task<List<BufferedImage>> {

    private final String ERROR = ConvertVideo_Byte.class.getSimpleName();
    private final Path fileName;
    private final double image_width;
    private final ThumbInfo thumbInfo;
    private final ImageView imageView;
    private List<BufferedImage> bufferedImageList = new ArrayList<>();
    private Timeline timeLine;

    public ConvertVideo_Byte(Path aFileName, ThumbInfo aThumbInfo, double aImage_width, ImageView aImageView) {
        this.fileName = aFileName;
        this.thumbInfo = aThumbInfo;
        this.image_width = aImage_width;
        this.imageView = aImageView;
    }

    @Override
    protected List<BufferedImage> call() throws Exception {
        try {
            for (byte[] imageInByte : thumbInfo.getThumbs()) {
                if (imageInByte == null) {
                    Messages.sprintf("There were no more images in byte[] array");
                    cancel();
                    break;
                }
                System.out.println("----ConvertVideo_Byte video imageInByte size: " + imageInByte.length);
                InputStream in = new ByteArrayInputStream(imageInByte);

                BufferedImage bufferedImage = ImageIO.read(in);
                bufferedImageList.add(bufferedImage);
            }
        } catch (Exception ex) {
            Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), false);
            return null;
        }

        return bufferedImageList;
    }

    @Override
    protected void succeeded() {
        List<BufferedImage> list = null;
        try {
            list = get();
            System.out.println("ConvertVideo_Byte video thumblist size: " + list.size());
        } catch (Exception e) {
            super.cancel();
            System.err.println("buffered failed: " + e);
            return;
        }

        System.out.println("ConvertVideo_Byte video imageView: " + imageView);
//		System.out.println("imageView.getParent() " + imageView.getParent());
//		Node node = imageView.getParent();
//		System.out.println("node.getClass() " + node.getClass() + " node.getId: " + node.getId());
//		if(!(node instanceof StackPane)) {
//			System.err.println("ConvertVideo_Byte video imageView.getParent() were not StackPane. returning: " + fileName + " class: " + node.getId());
//			return;
//		}

        Node rootPane = imageView.getParent();
        StackPane pane = new StackPane();
        pane.setMouseTransparent(true);

        System.out.println("rootPane ID: " + rootPane.getId());
        if (list == null || list.isEmpty()) {
            System.err.println("ConvertVideo_Byte video thumblist were null. returning: " + fileName);
            System.out.println("StackPANE ID: " + pane.getId());
            pane.getChildren().add(new Label("Video. NP"));
            return;
        } else {
            Label label = new Label("Video");
            label.setStyle("-fx-text-fill: orange;");
            label.setMouseTransparent(true);
            StackPane.setAlignment(label, Pos.TOP_CENTER);
            pane.getChildren().add(label);
        }
        VideoPreview videoPreview = new VideoPreview(list, imageView);
        imageView.setImage(videoPreview.getImage(0));
        imageView.setUserData(list);
        rootPane.setOnMouseEntered(event -> {
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

    @Override
    protected void cancelled() {
        Messages.sprintf("ConvertVideo_Byte Cancelled");
    }

    @Override
    protected void failed() {
        Messages.sprintfError("ConvertVideo_Byte failed");
    }

}
