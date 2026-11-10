package com.girbola.controllers.datefixer;

import com.girbola.controllers.datefixer.utils.DateFixGuiUtils;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import common.utils.date.DateUtils;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
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
        if (node instanceof VBox vbox && node.getId().equals(DateFixConstants.IMAGEFRAME.getType())) {
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

    public static Map<LocalDate, List<Node>> groupByDate(ModelDatefix modelDatefix) {
        Messages.sprintf("groupByDate");

        // Use TreeMap instead of HashMap if you want the date keys automatically sorted chronologically
        Map<LocalDate, List<Node>> nodesGroupedByDate = new HashMap<>();

        for (Node node : modelDatefix.getTilePane().getChildren()) {
            if (node instanceof VBox && DateFixConstants.IMAGEFRAME.getType().equals(node.getId())) {
                FileInfo fileInfo = (FileInfo) node.getUserData();

                if (fileInfo != null) {
                    LocalDate localDate = DateUtils.longToLocalDateTime(fileInfo.getDate()).toLocalDate();

                    // Groups nodes by date: fetches existing list or initializes a new one
                    nodesGroupedByDate.computeIfAbsent(localDate, k -> new ArrayList<>()).add(node);
                }
            }
        }

        return nodesGroupedByDate;
    }

    public static void hideShowImageFrameTop(ModelDatefix modelDatefix) {
   Messages.sprintf("hideShowImageFrameTop");
        for (Node node : modelDatefix.getTilePane().getChildren()) {
            if (node instanceof VBox vbox && node.getId().equals(DateFixConstants.IMAGEFRAME.getType())) {
                GridPane imageFrameTop = DateFixGuiUtils.getImageFrameTopGridPane(vbox);
                if (imageFrameTop != null) {
                    Platform.runLater(() -> {
                        imageFrameTop.setVisible(!imageFrameTop.isVisible());
                    });
                } else {
                    Messages.sprintfError("imageFrameTop is null");
                }
            }
        }
    }

    public static void hideShowImageFrameBottom(ModelDatefix modelDatefix) {
        Messages.sprintf("hideShowImageFrameBottom");
        for (Node node : modelDatefix.getTilePane().getChildren()) {
            if (node instanceof VBox vbox && node.getId().equals(DateFixConstants.IMAGEFRAME.getType())) {
                HBox imageFrameBottom = DateFixGuiUtils.getImageFrameBottomHBox(vbox);
                if (imageFrameBottom != null) {
                    Platform.runLater(() -> {
                        imageFrameBottom.setVisible(!imageFrameBottom.isVisible());
                    });
                } else {
                    Messages.sprintfError("imageFrameBottom is null");
                }
            }
        }
    }
}
