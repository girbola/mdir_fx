package com.girbola.controllers.datefixer;

import com.girbola.controllers.datefixer.utils.DateFixGuiUtils;
import com.girbola.messages.Messages;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class DateFixLoadingProcessLoader {


    public static void reNumberTheFrames(ModelDatefix model_datefix) {
        int counter = 1;

        for (Node node : model_datefix.getTilePane().getChildren()) {
            Messages.sprintf("ImageFrameeeeee: " + node.getId());
            changeImageNumberOfNode(node, counter);

            counter++;
        }
    }

    private static void changeImageNumberOfNode(Node node, int counter) {
        if (node instanceof VBox vbox && node.getId().equals("imageFrame")) {
            Label imageFrameImageNumber = DateFixGuiUtils.getImageFrameNumberLabel(vbox);
            if (imageFrameImageNumber != null) {
                Messages.sprintf("imageFrameImageNumber: " + imageFrameImageNumber.getText() + " Counter: " + counter);
                Platform.runLater(() -> {
                    imageFrameImageNumber.setText("");
                    imageFrameImageNumber.setText("" + counter);
                });
            }
        }
    }
}
