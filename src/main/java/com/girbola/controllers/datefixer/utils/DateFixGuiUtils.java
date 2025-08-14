package com.girbola.controllers.datefixer.utils;

import com.girbola.configuration.UIContants;
import com.girbola.controllers.datefixer.CssStylesEnum;
import com.girbola.controllers.datefixer.ModelDatefix;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.kordamp.ikonli.javafx.FontIcon;

import java.nio.file.Path;

import static com.girbola.Main.simpleDates;
import static com.girbola.messages.Messages.sprintf;

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

//        if (iv.getFitWidth() >= GuiImageFrame.THUMBNAIL_MAX_WIDTH) {
//            iv.setFitWidth(GuiImageFrame.THUMBNAIL_MAX_WIDTH - 50);
//        }
//        if (iv.getFitHeight() >= GuiImageFrame.THUMBNAIL_MAX_HEIGHT) {
//            iv.setFitHeight(GuiImageFrame.THUMBNAIL_MAX_HEIGHT - 50);
//        }

        Messages.sprintf("FileInfo: " + fi.getOrgPath() + " IMAGEVIEW: " + iv.getFitWidth() + " " + iv.getFitHeight());
        iv.setId("imageView");
        return iv;
    }


    public static Label createFileName_tf(Path path) {
        Label fileNameLabel = new Label();
        fileNameLabel.getStyleClass().add("fileName_ta");

        fileNameLabel.setFocusTraversable(false);
        fileNameLabel.setId("fileName");
        fileNameLabel.setMinSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        fileNameLabel.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        fileNameLabel.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        fileNameLabel.setMaxHeight(23);
        fileNameLabel.setMinHeight(23);
        fileNameLabel.setPrefHeight(23);

        fileNameLabel.setText(path.getFileName().toString());
        return fileNameLabel;
    }

    public static Label createFileDate_tf(FileInfo fileInfo, HBox hbox) {
        Label label = new Label(simpleDates.getSdf_ymd_hms_minusDots_default().format(fileInfo.getDate()));
        label.getStyleClass().add("fileDate_tf");
        label.setFocusTraversable(false);
        label.setId("fileDate");
        label.setMaxHeight(23);
        label.setMinHeight(23);
        label.setPrefHeight(23);

        if (fileInfo.isBad()) {
            hbox.setStyle(CssStylesEnum.BAD_STYLE.getStyle());
        } else if (fileInfo.isGood()) {
            hbox.setStyle(CssStylesEnum.GOOD_STYLE.getStyle());
        } else if (fileInfo.isConfirmed()) {
            hbox.setStyle(CssStylesEnum.CONFIRMED_STYLE.getStyle());
        } else if (fileInfo.isVideo()) {
            hbox.setStyle(CssStylesEnum.VIDEO_STYLE.getStyle());
        } else if (fileInfo.isSuggested()) {
            hbox.setStyle(CssStylesEnum.SUGGESTED_STYLE.getStyle());
        }
        return label;
    }

    public static Button createAcceptButton(FileInfo fi, HBox hbox, Label tf) {
        Button button = new Button();
        FontIcon fontIcon = new FontIcon();
        fontIcon.setIconLiteral("bi-check");
        fontIcon.setIconSize(15);
        fontIcon.setIconColor(javafx.scene.paint.Color.GREEN);
        //ImageView imageView = new ImageView(GUI_Methods.loadImage("confirm.png", GuiImageFrame.BUTTON_WIDTH));
        button.setGraphic(fontIcon);
        button.setId("accept");
        button.getStyleClass().add("acceptButton");
        if (!fi.isGood()) {
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    String date = tf.getText();
                    tf.setText(date);
                    hbox.setStyle(CssStylesEnum.MODIFIED_STYLE.getStyle());
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

        topContainer.getColumnConstraints().addAll(cc1, cc2, cc3, cc4, cc5);

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

    public static VBox createBottomContainer() {
        VBox bottomContainer = new VBox();
        bottomContainer.setId("bottomContainer");
        bottomContainer.getStyleClass().add("bottomContainer");
        bottomContainer.setFillWidth(true);
        bottomContainer.setAlignment(Pos.CENTER);

        bottomContainer.setMinSize(UIContants.IMAGE_FRAME_WIDTH - 6, 40);
        bottomContainer.setMaxSize(UIContants.IMAGE_FRAME_WIDTH - 6, 40);
        bottomContainer.setPrefSize(UIContants.IMAGE_FRAME_WIDTH - 6, 40);
        return bottomContainer;
    }

    public static HBox createButtonDateTimeContainer(double hGap) {
        HBox buttonDateTimeContainer = new HBox();
        buttonDateTimeContainer.setAlignment(Pos.TOP_LEFT);
        buttonDateTimeContainer.setId("bottom");
        buttonDateTimeContainer.setMaxSize(UIContants.IMAGE_FRAME_WIDTH - hGap, 20);
        buttonDateTimeContainer.setPrefSize(UIContants.IMAGE_FRAME_WIDTH - hGap, 20);
        buttonDateTimeContainer.setMinSize(UIContants.IMAGE_FRAME_WIDTH - hGap, 20);
        buttonDateTimeContainer.getStyleClass().add("buttonDateTimeContainer");
        return buttonDateTimeContainer;
    }

    public static boolean isImageFrame(Node node) {
        return node instanceof VBox && "imageFrame".equals(node.getId());
    }

    public static void processImageFrame(VBox imageFrame, ModelDatefix modelDatefix, String style) {
        for (Node imageFrameNode : imageFrame.getChildren()) {
            if (imageFrameNode instanceof VBox) {
                Node fileDateField = imageFrameNode.lookup("#fileDate");
                if (fileDateField instanceof Label && style.equals(fileDateField.getStyle())) {
                    modelDatefix.getSelectionModel().addWithToggle(imageFrame);
                }
            }
        }
    }

    public static void selectImageFrame(ModelDatefix modelDatefix, TilePane parent, String style) {
        for (Node childNode : parent.getChildren()) {
            if (isImageFrame(childNode)) {
                VBox imageFrame = (VBox) childNode;
                FileInfo fileInfo = (FileInfo) imageFrame.getUserData();
                if (fileInfo.isImage()) {
                    processImageFrame(imageFrame, modelDatefix, style);
                }
            }
        }
    }

    public static void selectVideoImageFrame(ModelDatefix modelDatefix, TilePane parent, String style) {
        for (Node childNode : parent.getChildren()) {
            if (isImageFrame(childNode)) {
                VBox imageFrame = (VBox) childNode;
                FileInfo fileInfo = (FileInfo) imageFrame.getUserData();
                if (fileInfo.isVideo()) {
                    processImageFrame(imageFrame, modelDatefix, style);
                }
            }
        }
    }

    /**
     * Retrieves the bottom HBox from a given Node if the Node structure corresponds to specific criteria.
     * The method navigates through a VBox with id "imageFrame", finds a nested VBox with id "bottomContainer",
     * and then searches for an HBox with id "bottom" inside it.
     *
     * @param node the root Node from which the search process starts, expected to be a VBox containing specific sub-nodes
     * @return the HBox with id "bottom" if found, or null if the structure doesn't match the expected criteria
     */
    public static HBox getBottomHBox(Node node) {
        if (node instanceof VBox && node.getId().equals("imageFrame")) {
            for (Node node2 : ((VBox) node).getChildren()) {
                if (node2 instanceof VBox bottomContainer && bottomContainer.getId().equals("bottomContainer")) {
                    for (Node node3 : bottomContainer.getChildren()) {
                        if (node3 instanceof HBox hbox && hbox.getId().equals("bottom")) {
                            return (HBox) node3;
                        }
                    }
                }
            }
        }

        return null;
    }

    public static Label getFileDateLabel(Node node) {
        if (node instanceof VBox vbox && vbox.getId().equals("imageFrame")) {
            for (Node imageFrame : vbox.getChildren()) {
                if (imageFrame instanceof VBox bottomContainer && bottomContainer.getId().equals("bottomContainer")) {
                    for (Node bottomVBox : bottomContainer.getChildren()) {
                        if (bottomVBox instanceof HBox hboxBottom && hboxBottom.getId().equals("bottom")) {
                            for (Node bottomNode2 : hboxBottom.getChildren()) {
                                if (bottomNode2 instanceof Label label && label.getId().equals("fileDate")) {
                                    return label;
                                }
                            }
                        }
                    }
                }
            }

        }
        return null;
    }

    public static GridPane getImageFrameGridPane(VBox vbox) {
        for (Node node : vbox.getChildren()) {
            if (node instanceof GridPane) {
                return (GridPane) node;
            }
        }
        return null;
    }

    public static int getImageFrameImageNumber(VBox vbox) {
        GridPane imageFrameGridPane = getImageFrameGridPane(vbox);
        if (imageFrameGridPane == null) {
            Messages.sprintf("ImageNumber could not be found. First one");
            return -1;
        }

        for (Node gridPaneChild : imageFrameGridPane.getChildren()) {
            if (gridPaneChild instanceof Label label && gridPaneChild.getId().equals("imageNumber")) {
                return Integer.parseInt(label.getText());
            }
        }
        Messages.sprintf("ImageNumber could not be found. Last one");
        return -1;
    }

    public static Label getImageFrameNumberLabel(VBox vbox) {
        GridPane imageFrameGridPane = getImageFrameGridPane(vbox);
        if (imageFrameGridPane == null) {
            return null;
        }

        for (Node gridPaneChild : imageFrameGridPane.getChildren()) {
            if (gridPaneChild instanceof Label && gridPaneChild.getId().equals("imageNumber")) {
                return (Label) gridPaneChild;
            }
        }
        return null;
    }
}
