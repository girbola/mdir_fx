package com.girbola.controllers.datefixer.utils;

import com.girbola.configuration.GuiImageFrame;
import com.girbola.controllers.datefixer.CssStylesEnum;
import com.girbola.controllers.datefixer.GUI_Methods;
import com.girbola.controllers.datefixer.Model_datefix;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import java.nio.file.Path;

import static com.girbola.Main.simpleDates;

public class DateFixGuiUtils {

    public static Label createImageNumberLbl(int index) {
        Label label = new Label("" + index);
        label.getStyleClass().add("imageNumber");
        label.setId("imageNumber");
        label.setMouseTransparent(true);
        return label;
    }

    public static VBox createImageFrame(int imageFrameX, int imageFrameY) {
        VBox frame_vbox = new VBox();
        frame_vbox.setAlignment(Pos.TOP_CENTER);
        frame_vbox.setId("imageFrame");
        frame_vbox.setAlignment(Pos.CENTER);
        frame_vbox.setFillWidth(true);
        frame_vbox.setPrefSize(imageFrameX, imageFrameY);
        frame_vbox.setMinSize(imageFrameX, imageFrameY);
        frame_vbox.setMaxSize(imageFrameX, imageFrameY);
        frame_vbox.setFillWidth(true);
        frame_vbox.getStyleClass().add("imageFrame");
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

    public static ImageView createImageView(FileInfo fi, double thumb_x_MAX, double thumb_y_MAX) {
        ImageView iv = new ImageView();
        iv.setFitWidth(thumb_x_MAX);
        iv.setFitHeight(thumb_y_MAX);
        iv.setPreserveRatio(true);
        iv.setMouseTransparent(true);

//        if (iv.getFitWidth() >= GuiImageFrame.thumb_x_MAX) {
//            iv.setFitWidth(GuiImageFrame.thumb_x_MAX - 50);
//        }
//        if (iv.getFitHeight() >= GuiImageFrame.thumb_y_MAX) {
//            iv.setFitHeight(GuiImageFrame.thumb_y_MAX - 50);
//        }

        Messages.sprintf("FileInfo: " + fi.getOrgPath() + " IMAGEVIEW: " + iv.getFitWidth() + " " + iv.getFitHeight());
        iv.setId("imageView");
        return iv;
    }


    public static Label createFileName_tf(Path path, String name) {
        Label fileNameLabel = new Label();
        fileNameLabel.getStyleClass().add(name);

        fileNameLabel.setFocusTraversable(false);
        fileNameLabel.setId("fileName");
        fileNameLabel.setMinSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        fileNameLabel.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        fileNameLabel.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        fileNameLabel.setMaxHeight(25);
        fileNameLabel.setMinHeight(25);
        fileNameLabel.setPrefHeight(25);

        fileNameLabel.setText(path.getFileName().toString());
        return fileNameLabel;
    }

    public static TextField createFileDate_tf(FileInfo fileInfo, String name) {
        TextField textField = new TextField(simpleDates.getSdf_ymd_hms_minusDots_default().format(fileInfo.getDate()));
        textField.getStyleClass().add(name);
        textField.setEditable(false);
        textField.setFocusTraversable(false);
        textField.setId("fileDate");
        textField.setMaxHeight(25);
        textField.setMinHeight(25);
        textField.setPrefHeight(25);

        if (fileInfo.isBad()) {
            textField.setStyle(CssStylesEnum.BAD_STYLE.getStyle());
        } else if (fileInfo.isGood()) {
            textField.setStyle(CssStylesEnum.GOOD_STYLE.getStyle());
        } else if (fileInfo.isConfirmed()) {
            textField.setStyle(CssStylesEnum.CONFIRMED_STYLE.getStyle());
        } else if (fileInfo.isVideo()) {
            textField.setStyle(CssStylesEnum.VIDEO_STYLE.getStyle());
        } else if (fileInfo.isSuggested()) {
            textField.setStyle(CssStylesEnum.SUGGESTED_STYLE.getStyle());
        }
        return textField;
    }

    public static Button createAcceptButton(FileInfo fi, TextField tf) {
        Button button = new Button();
        ImageView imageView = new ImageView(GUI_Methods.loadImage("confirm.png", GuiImageFrame.BUTTON_WIDTH));
        button.setGraphic(imageView);
        button.setId("accept");
        if (!fi.isGood()) {
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    String date = tf.getText();
                    tf.setText(date);
                    tf.setStyle(CssStylesEnum.MODIFIED_STYLE.getStyle());
                }
            });
        } else {
            button.setDisable(true);
        }
        return button;
    }


    public static void setGridPaneColumnWidth(ColumnConstraints column, double width) {
        column.setMinWidth(width);
        column.setMaxWidth(width);
        column.setPrefWidth(width);
    }

    public static GridPane createTopGridPane() {
        GridPane topContainer = new GridPane();
        topContainer.setAlignment(Pos.TOP_LEFT);
        topContainer.setId("topContainer");
        topContainer.getStyleClass().add("imageFrameTop");
        topContainer.setMouseTransparent(true);

        topContainer.setMaxSize(Region.USE_COMPUTED_SIZE, 25);
        topContainer.setMinSize(Region.USE_COMPUTED_SIZE, 25);
        topContainer.setPrefSize(Region.USE_COMPUTED_SIZE, 25);

//
//        topContainer.setMaxWidth(GuiImageFrame.imageFrame_x);
//        topContainer.setMinHeight(Region.USE_COMPUTED_SIZE);
//        topContainer.setMinWidth(GuiImageFrame.imageFrame_x);
//
//        topContainer.setPrefHeight(Region.USE_COMPUTED_SIZE);
//        topContainer.setPrefWidth(GuiImageFrame.imageFrame_x);

        ColumnConstraints cc1 = new ColumnConstraints();
        cc1.setPercentWidth(20);
        cc1.setHalignment(HPos.CENTER);

        ColumnConstraints cc2 = new ColumnConstraints();
        cc2.setPercentWidth(20);

        ColumnConstraints cc3 = new ColumnConstraints();
        cc3.setPercentWidth(20);
        cc3.setHalignment(HPos.CENTER);

        ColumnConstraints cc4 = new ColumnConstraints();
        cc4.setPercentWidth(20);

        ColumnConstraints cc5 = new ColumnConstraints();
        cc5.setPercentWidth(20);
        cc5.setHalignment(HPos.CENTER);

        topContainer.getColumnConstraints().addAll(cc1, cc2, cc3, cc4,cc5);

        RowConstraints r1 = new RowConstraints(10);
        RowConstraints r2 = new RowConstraints(10);

        topContainer.getRowConstraints().addAll(r1, r2);

        return topContainer;
    }

    public static HBox createImageViewContainer(FileInfo fileInfo, String name, int imageFrame_y) {
        HBox imageViewContainer = new HBox();
        imageViewContainer.getStyleClass().add(name);
        imageViewContainer.setId("imageViewContainer");
        imageViewContainer.setMouseTransparent(true);
        imageViewContainer.setMinSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        imageViewContainer.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        imageViewContainer.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

        imageViewContainer.setMinHeight(175);
        imageViewContainer.setMaxHeight(175);
        imageViewContainer.setPrefHeight(175);

        imageViewContainer.setAlignment(Pos.CENTER);
        imageViewContainer.setFillHeight(true);
        imageViewContainer.setId(name);

        VBox.setVgrow(imageViewContainer, Priority.ALWAYS);

        return imageViewContainer;
    }

    public static VBox createBottomContainer(String name) {
        VBox bottomContainer = new VBox();
        bottomContainer.setId(name);
        bottomContainer.getStyleClass().add(name);
        bottomContainer.setFillWidth(true);
        bottomContainer.setAlignment(Pos.CENTER);

        bottomContainer.setMinSize(GuiImageFrame.imageFrame_x-2, 40);
        bottomContainer.setMaxSize(GuiImageFrame.imageFrame_x-2, 40);
        bottomContainer.setPrefSize(GuiImageFrame.imageFrame_x-2, 40);
        return bottomContainer;
    }

    public static HBox createButtonDateTimeContainer(String name) {
        HBox buttonDateTimeContainer = new HBox();
        buttonDateTimeContainer.setId(name);
        buttonDateTimeContainer.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        buttonDateTimeContainer.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        buttonDateTimeContainer.setMinSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        buttonDateTimeContainer.setMinWidth(GuiImageFrame.imageFrame_x - 2);
        buttonDateTimeContainer.setMaxWidth(GuiImageFrame.imageFrame_x - 2);
        buttonDateTimeContainer.setPrefWidth(GuiImageFrame.imageFrame_x - 2);
        buttonDateTimeContainer.setAlignment(Pos.TOP_LEFT);

        return buttonDateTimeContainer;
    }

    public static boolean isImageFrame(Node node) {
        return node instanceof VBox && "imageFrame".equals(node.getId());
    }

    public static void processImageFrame(VBox imageFrame, Model_datefix modelDatefix, String style) {
        for (Node imageFrameNode : imageFrame.getChildren()) {
            if (imageFrameNode instanceof VBox) {
                Node fileDateField = imageFrameNode.lookup("#fileDate");
                if (fileDateField instanceof TextField && style.equals(fileDateField.getStyle())) {
                    modelDatefix.getSelectionModel().addWithToggle(imageFrame);
                }
            }
        }
    }

    public static void selectImageFrame(Model_datefix modelDatefix, TilePane parent, String style) {
        for (Node childNode : parent.getChildren()) {
            if (isImageFrame(childNode)) {
                VBox imageFrame = (VBox) childNode;
                FileInfo fileInfo = (FileInfo) imageFrame.getUserData();
                if(fileInfo.isImage()) {
                    processImageFrame(imageFrame, modelDatefix, style);
                }
            }
        }
    }

    public static void selectVideoImageFrame(Model_datefix modelDatefix, TilePane parent, String style) {
        for (Node childNode : parent.getChildren()) {
            if (isImageFrame(childNode)) {
                VBox imageFrame = (VBox) childNode;
                FileInfo fileInfo = (FileInfo) imageFrame.getUserData();
                if(fileInfo.isVideo()) {
                    processImageFrame(imageFrame, modelDatefix,style);
                }
            }
        }
    }
}
