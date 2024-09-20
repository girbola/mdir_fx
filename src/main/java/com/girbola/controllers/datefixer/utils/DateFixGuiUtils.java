package com.girbola.controllers.datefixer.utils;

import com.girbola.messages.Messages;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class DateFixGuiUtils {

    public static Label createImageNumberLbl(int index) {
        Label label = new Label("" + index);
        label.getStyleClass().add("imageNumber");
        label.setId("imageNumber");
        label.setMouseTransparent(true);
        return label;
    }

    public static VBox createImageFrame(String name, int imageFrameX, int imageFrameY) {
        VBox frame_vbox = new VBox();
        frame_vbox.setAlignment(Pos.TOP_CENTER);
        frame_vbox.setId(name);
        frame_vbox.setPrefSize(imageFrameX, imageFrameY);
        frame_vbox.setMinSize(imageFrameX, imageFrameY);
        frame_vbox.setMaxSize(imageFrameX, imageFrameY);
        frame_vbox.getStyleClass().add(name);
        return frame_vbox;
    }

    public static StackPane createImageFrameStackPane(int index) {
        Messages.sprintf("createImageFrameStackPane: " + index);
        StackPane stackPane = new StackPane();
        stackPane.setAlignment(Pos.CENTER);
        stackPane.setId("imageFrameStackPane");
        stackPane.getStyleClass().add("imageFrameStackPane");
        stackPane.setMouseTransparent(true);
        return stackPane;
    }
}
