package com.girbola.controllers.datefixer.utils;

import com.girbola.configuration.UIContants;
import com.girbola.controllers.datefixer.CssStylesEnum;
import com.girbola.controllers.datefixer.DateFixConstants;
import com.girbola.controllers.datefixer.ModelDatefix;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.utils.FileInfoUtils;
import common.utils.FileUtils;
import java.nio.file.Paths;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;

import java.nio.file.Path;

import static com.girbola.Main.simpleDates;
import static com.girbola.messages.Messages.sprintf;

public class DateFixGuiUtils {

    public static Label createImageNumberLbl(int index) {
        Label label = new Label("" + index);
        label.setWrapText(false);
        label.setId("imageNumber");
        label.getStyleClass().add("imageNumber");
        label.setMouseTransparent(true);
        return label;
    }

    public static VBox createImageFrame() {
        VBox imageFrameContainer = new VBox();
        imageFrameContainer.setAlignment(Pos.TOP_CENTER);
        imageFrameContainer.setId(DateFixConstants.IMAGEFRAME.getType());
        imageFrameContainer.getStyleClass().add(DateFixConstants.IMAGEFRAME.getType());
        imageFrameContainer.setFillWidth(true);
        imageFrameContainer.setPrefSize(UIContants.IMAGE_FRAME_WIDTH, UIContants.IMAGE_FRAME_HEIGHT);
//        imageFrameContainer.setMinSize(UIContants.IMAGE_FRAME_WIDTH, UIContants.IMAGE_FRAME_HEIGHT);
//        imageFrameContainer.setMaxSize(UIContants.IMAGE_FRAME_WIDTH, UIContants.IMAGE_FRAME_HEIGHT);
        return imageFrameContainer;
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
//        fileNameLabel.setMaxHeight(23);
//        fileNameLabel.setMinHeight(23);
//        fileNameLabel.setPrefHeight(23);

        fileNameLabel.setText(path.getFileName().toString());
        return fileNameLabel;
    }

    public static Label createFileDate_tf(FileInfo fileInfo, HBox hbox) {
        Label label = new Label(simpleDates.getSdf_ymd_hms_minusDots_default().format(fileInfo.getDate()));
        label.getStyleClass().add("fileDate_tf");

        label.setFocusTraversable(false);
        label.setId("fileDate");
        label.setMaxHeight(Region.USE_COMPUTED_SIZE);
        label.setMinHeight(Region.USE_COMPUTED_SIZE);
        label.setPrefHeight(Region.USE_COMPUTED_SIZE);
        label.setMinWidth(Region.USE_COMPUTED_SIZE);
        label.setPrefWidth(Region.USE_COMPUTED_SIZE);
        label.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(label, Priority.ALWAYS);

        label.setTextOverrun(OverrunStyle.ELLIPSIS);
        label.setWrapText(false);
        // Truncate text if it's wider than the parent

        if (FileInfoUtils.isGood(fileInfo)) {
            label.setStyle(CssStylesEnum.GOOD_STYLE.getStyle());
        } else if (FileInfoUtils.isBad(fileInfo)) {
            label.setStyle(CssStylesEnum.BAD_STYLE.getStyle());
        } else if (FileInfoUtils.isModified(fileInfo)) {
            label.setStyle(CssStylesEnum.MODIFIED_STYLE.getStyle());
        } else if (FileInfoUtils.isConfirmed(fileInfo)) {
            label.setStyle(CssStylesEnum.CONFIRMED_STYLE.getStyle());
        } else if (FileInfoUtils.isSuggested(fileInfo)) {
            label.setStyle(CssStylesEnum.SUGGESTED_STYLE.getStyle());
        }

        return label;
    }

    public static Button createAcceptButton(FileInfo fi, HBox hbox, Label tf) {
        Button button = new Button();
//        button.getStylesheets().add("button");

        FontIcon fontIcon = new FontIcon();
        fontIcon.setIconColor(Color.RED);
        fontIcon.setIconLiteral("bi-check");
        fontIcon.setIconSize(10);
//        fontIcon.setIconColor(javafx.scene.paint.Color.GREEN);
        //ImageView imageView = new ImageView(GUI_Methods.loadImage("confirm.png", GuiImageFrame.BUTTON_WIDTH));
        button.setGraphic(fontIcon);
//        button.setId("accept");
//        button.getStyleClass().add("acceptButton");
        if (!fi.isGood()) {
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    Platform.runLater(() -> {
                        fontIcon.setStyle(CssStylesEnum.BAD_STYLE.getStyle());
                        String date = tf.getText();
                        tf.setText(date);
                        hbox.setStyle(CssStylesEnum.MODIFIED_STYLE.getStyle());
                    });

                }
            });
        } else {
            button.setDisable(true);
            button.setVisible(false);
        }
        return button;
    }

    public static void setGridPaneColumnWidth(ColumnConstraints column, double width) {
        column.setMinWidth(width);
        column.setMaxWidth(width);
        column.setPrefWidth(width);
    }

    public static VBox createInfoContainer(double spacing) {
        VBox vbox = new VBox();
        vbox.setSpacing(spacing);
        vbox.setAlignment(Pos.TOP_LEFT);
        vbox.setId("topInfoContainer");
        vbox.getStyleClass().add("imageFrameTop");
        vbox.setMouseTransparent(true);
        vbox.setMaxWidth(Double.MAX_VALUE);

        vbox.setFillWidth(true);
        vbox.getStyleClass().add("imageFrameTopContainer");

        return vbox;
    }

//    public static HBox createTopContainer(double spacing) {
//        HBox topContainer = new HBox();
//        topContainer.setSpacing(spacing);
//        topContainer.setAlignment(Pos.TOP_LEFT);
//        topContainer.setId("topContainer");
//        topContainer.getStyleClass().add("imageFrameTop");
//        topContainer.setMouseTransparent(true);
//
//        return topContainer;
//    }

    public static GridPane createTopGridPane() {
        GridPane topContainer = new GridPane();
        topContainer.setAlignment(Pos.TOP_LEFT);
        topContainer.setId("topContainer");
        topContainer.getStyleClass().add("imageFrameTop");
        topContainer.setMouseTransparent(true);


//        topContainer.parentProperty().addListener((obs, oldParent, newParent) -> {
//            if (newParent instanceof Region region) {
//                topContainer.prefWidthProperty().bind(region.widthProperty());
//            }
//        });

//        topContainer.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
//        topContainer.setMinSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
//        topContainer.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

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
        RowConstraints r3 = new RowConstraints(10);
        RowConstraints r4 = new RowConstraints(10);

        topContainer.getRowConstraints().addAll(r1, r2, r3, r4);

        return topContainer;
    }

    public static HBox createImageViewContainer() {
        HBox imageViewContainer = new HBox();
        imageViewContainer.getStyleClass().add("imageViewContainer");
        imageViewContainer.setId("imageViewContainer");
        imageViewContainer.setMouseTransparent(true);
        imageViewContainer.setMinSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        imageViewContainer.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        imageViewContainer.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

        imageViewContainer.setMinHeight(Math.round((double) UIContants.IMAGE_FRAME_HEIGHT * 0.7));
        imageViewContainer.setMaxHeight(Math.round((double) UIContants.IMAGE_FRAME_HEIGHT * 0.7));
        imageViewContainer.setPrefHeight(Math.round((double) UIContants.IMAGE_FRAME_HEIGHT * 0.7));

        imageViewContainer.setAlignment(Pos.CENTER);
        imageViewContainer.setFillHeight(true);

        VBox.setVgrow(imageViewContainer, Priority.ALWAYS);

        return imageViewContainer;
    }

    public static VBox createBottomContainer() {
        VBox bottomContainer = new VBox();
        bottomContainer.setId("bottomContainer");
        bottomContainer.getStyleClass().add("bottomContainer");
        bottomContainer.setFillWidth(true);
        bottomContainer.setAlignment(Pos.CENTER);

        bottomContainer.setMinSize(UIContants.IMAGE_FRAME_WIDTH - 6, Region.USE_COMPUTED_SIZE);
        bottomContainer.setMaxSize(UIContants.IMAGE_FRAME_WIDTH - 6, Region.USE_COMPUTED_SIZE);
        bottomContainer.setPrefSize(UIContants.IMAGE_FRAME_WIDTH - 6, Region.USE_COMPUTED_SIZE);
        return bottomContainer;
    }

    public static HBox createButtonDateTimeContainer(double hGap) {
        HBox buttonDateTimeContainer = new HBox();
        buttonDateTimeContainer.setSpacing(hGap);
        buttonDateTimeContainer.setAlignment(Pos.CENTER_LEFT);
        buttonDateTimeContainer.setId("bottom");
        buttonDateTimeContainer.setMaxSize(UIContants.IMAGE_FRAME_WIDTH - hGap, 30);
        buttonDateTimeContainer.setPrefSize(UIContants.IMAGE_FRAME_WIDTH - hGap, 30);
        buttonDateTimeContainer.setMinSize(UIContants.IMAGE_FRAME_WIDTH - hGap, 30);
        buttonDateTimeContainer.getStyleClass().add("buttonDateTimeContainer");

        return buttonDateTimeContainer;
    }

    public static boolean isImageFrame(Node node) {
        return node instanceof VBox && DateFixConstants.IMAGEFRAME.getType().equals(node.getId());
    }

//    public static void processVideoFrame(VBox imageFrame, ModelDatefix modelDatefix, String style) {
//
//        for (Node imageFrameNode : imageFrame.getChildren()) {
//            if (imageFrameNode instanceof VBox) {
//                FileInfo fileInfo = (FileInfo) imageFrame.getUserData();
//                if (style.equals(CssStylesEnum.GOOD_STYLE.getStyle()) && fileInfo.isGood()) {
//                    Messages.sprintf("111processImageFrame found: " + fileInfo.getOrgPath());
//                    modelDatefix.getSelectionModel().addWithToggle(imageFrame);
//                } else if (style.equals(CssStylesEnum.BAD_STYLE.getStyle()) && fileInfo.isBad()) {
//                    Messages.sprintf("2222processImageFrame found: " + fileInfo.getOrgPath());
//                }
//                Node fileDateField = imageFrameNode.lookup("#fileDate");
//                Messages.sprintf("111processImageFrame found: " + fileDateField + " imageFrame.getStyle()::: " + imageFrame.getStyle());

    /// /                    modelDatefix.getSelectionModel().addWithToggle(imageFrame);
//                if (fileDateField instanceof Label && style.equals(fileDateField.getStyle())) {
//                    Messages.sprintf("2222processImageFrame found: " + fileInfo.getOrgPath());
//                    modelDatefix.getSelectionModel().addWithToggle(imageFrame);
//                }
//            }
//        }
//    }
    public static void processImageFrame(VBox imageFrame, ModelDatefix modelDatefix, String style) {

//        if (style.equals(CssStylesEnum.GOOD_STYLE.getStyle()) && imageFrame.getStyle().equals(CssStylesEnum.GOOD_STYLE.getStyle())) {
//            modelDatefix.getSelectionModel().addWithToggle(imageFrame);
//        } else if (style.equals(CssStylesEnum.BAD_STYLE.getStyle()) && imageFrame.getStyle().equals(CssStylesEnum.BAD_STYLE.getStyle())) {
//            modelDatefix.getSelectionModel().addWithToggle(imageFrame);
//        }

        for (Node imageFrameNode : imageFrame.getChildren()) {
            if (imageFrameNode instanceof VBox) {
                FileInfo fileInfo = (FileInfo) imageFrame.getUserData();

                if (fileInfo.isImage()) {
                    if (style.equals(CssStylesEnum.GOOD_STYLE.getStyle())) {
                        if (fileInfo.isGood()) {
                            Messages.sprintf("3333processImageFrame found GOOOOD: " + fileInfo.getOrgPath());
                            modelDatefix.getSelectionModel().addWithToggle(imageFrame);
                        }
                    } else if (style.equals(CssStylesEnum.BAD_STYLE.getStyle())) {
                        if (fileInfo.isBad()) {
                            Messages.sprintf("4444processImageFrame found BAD: " + fileInfo.getOrgPath());
                            modelDatefix.getSelectionModel().addWithToggle(imageFrame);
                        }
                        if (style.equals(CssStylesEnum.MODIFIED_STYLE.getStyle())) {
                            if (fileInfo.isModified()) {
                                Messages.sprintf("5555processImageFrame found MODIFIED: " + fileInfo.getOrgPath());
                                modelDatefix.getSelectionModel().addWithToggle(imageFrame);
                            }
                        } else if (style.equals(CssStylesEnum.CONFIRMED_STYLE.getStyle())) {
                            if (fileInfo.isConfirmed()) {
                                Messages.sprintf("6666processImageFrame found CONFIRMED: " + fileInfo.getOrgPath());
                                modelDatefix.getSelectionModel().addWithToggle(imageFrame);
                            }
                        }
                    }
                }

//
//                if (fileInfo.isImage() && fileInfo.isBad()) {
//                    modelDatefix.getSelectionModel().addWithToggle(imageFrame);
//                }
//                if (fileInfo.isImage() && fileInfo.isGood()) {
//                    modelDatefix.getSelectionModel().addWithToggle(imageFrame);
//                }

            }
        }
    }

    public static void selectAnyMediaFrame(ModelDatefix modelDatefix, TilePane parent, String style) {
        for (Node root : parent.getChildren()) {
            if (root instanceof VBox && root.getId().equals(DateFixConstants.IMAGEFRAME.getType())) {
                VBox imageFrame = (VBox) root;
                FileInfo fileInfo = (FileInfo) root.getUserData();
                Messages.sprintf("fileInfoooooo:::::::::: " + fileInfo.toString());
                if (style.equals(CssStylesEnum.GOOD_STYLE.getStyle())) {
                    if (fileInfo.isGood() && !fileInfo.isConfirmed() && !fileInfo.isSuggested()) {
                        modelDatefix.getSelectionModel().addWithToggle(root);
                    }
                } else if (style.equals(CssStylesEnum.MODIFIED_STYLE.getStyle())) {
                    if (fileInfo.isGood() && fileInfo.isConfirmed() && !fileInfo.isSuggested()) {
                        modelDatefix.getSelectionModel().addWithToggle(root);
                    }
                } else if (style.equals(CssStylesEnum.SUGGESTED_STYLE.getStyle())) {
                    if (fileInfo.isGood() && fileInfo.isSuggested() && !fileInfo.isConfirmed()) {
                        modelDatefix.getSelectionModel().addWithToggle(root);
                    }
                } else if (style.equals(CssStylesEnum.BAD_STYLE.getStyle())) {
                    if (fileInfo.isBad()) {
                        modelDatefix.getSelectionModel().addWithToggle(root);
                    }
                } else if (style.equals(CssStylesEnum.CONFIRMED_STYLE.getStyle())) {
                    if (fileInfo.isConfirmed()) {
                        modelDatefix.getSelectionModel().addWithToggle(root);
                    }
                }

//                if (style.equals(CssStylesEnum.GOOD_STYLE.getStyle())) {
//                    if (fileInfo.isGood()) {
//                        modelDatefix.getSelectionModel().addWithToggle(root);
//                    }
//                } else if (style.equals(CssStylesEnum.BAD_STYLE.getStyle())) {
//                    if (fileInfo.isBad()) {
//                        modelDatefix.getSelectionModel().addWithToggle(root);
//                    }
//                } else {
//                    processImageFrame(imageFrame, modelDatefix, style);
//                }
            }
        }
//        for (Node childNode : parent.getChildren()) {
//            if (isImageFrame(childNode)) {
//                VBox imageFrame = (VBox) childNode;
//                processImageFrame(imageFrame, modelDatefix, style);
//            }
//        }
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

    /**
     * Selects video image frames within the given TilePane and processes them.
     * The method iterates through the children of the TilePane, identifies nodes
     * representing video frames, and applies specific styling or modifications
     * as provided.
     *
     * @param modelDatefix the data model that provides contextual information
     *                     or operations required for processing the video frames
     * @param parent       the TilePane containing child nodes that may represent video image frames
     * @param style        the style to be applied to the selected video image frames
     */
    public static void selectVideoFrame(ModelDatefix modelDatefix, TilePane parent, String style) {
        for (Node childNode : parent.getChildren()) {
            if (isImageFrame(childNode)) {
                Messages.sprintf("selectVideoFrame found VIDEO: " + childNode.getId());
                VBox imageFrame = (VBox) childNode;
                FileInfo fileInfo = (FileInfo) imageFrame.getUserData();
                if (fileInfo.isVideo()) {
                    if (style.equals(CssStylesEnum.GOOD_STYLE.getStyle())) {
                        Messages.sprintf("selectVideoFrame found VIDEO: " + fileInfo.getOrgPath());
                        modelDatefix.getSelectionModel().addWithToggle(imageFrame);
                    } else if (style.equals(CssStylesEnum.BAD_STYLE.getStyle())) {
                        Messages.sprintf("selectVideoFrame found VIDEO: " + fileInfo.getOrgPath());
                        modelDatefix.getSelectionModel().addWithToggle(imageFrame);
                        if (style.equals(CssStylesEnum.MODIFIED_STYLE.getStyle())) {
                            Messages.sprintf("selectVideoFrame found VIDEO: " + fileInfo.getOrgPath());
                            modelDatefix.getSelectionModel().addWithToggle(imageFrame);
                        } else if (style.equals(CssStylesEnum.CONFIRMED_STYLE.getStyle())) {
                            Messages.sprintf("selectVideoFrame found VIDEO: " + fileInfo.getOrgPath());
                            modelDatefix.getSelectionModel().addWithToggle(imageFrame);
                        } else if (style.equals(CssStylesEnum.SUGGESTED_STYLE.getStyle())) {
                            Messages.sprintf("selectVideoFrame found VIDEO: " + fileInfo.getOrgPath());
                            modelDatefix.getSelectionModel().addWithToggle(imageFrame);
                        }
                    }
                }
            }
        }
    }

    /**
     * Retrieves the bottom HBox from a given Node if the Node structure corresponds to specific criteria.
     * The method navigates through a VBox with id DateFixConstants.IMAGEFRAME.getType(), finds a nested VBox with id "bottomContainer",
     * and then searches for an HBox with id "bottom" inside it.
     *
     * @param node the root Node from which the search process starts, expected to be a VBox containing specific sub-nodes
     * @return the HBox with id "bottom" if found, or null if the structure doesn't match the expected criteria
     */
    public static HBox getBottomHBox(Node node) {
        if (node instanceof VBox && node.getId().equals(DateFixConstants.IMAGEFRAME.getType())) {
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
        if (node instanceof VBox vbox && vbox.getId().equals(DateFixConstants.IMAGEFRAME.getType())) {
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

    public static Label createDimensionsLabel(FileInfo fileInfo) {
        int width = (int) fileInfo.getWidth();
        int height = (int) fileInfo.getHeight();
        Label dimensionsLabel = new Label(width + " x " + height);
        dimensionsLabel.getStyleClass().add("dimensionsLabel");
        dimensionsLabel.setId("dimensionsLabel");
        dimensionsLabel.setMouseTransparent(true);
        return dimensionsLabel;
    }

    public static Label createFileExtension(FileInfo fileInfo) {
        Label label = new Label(FileUtils.getExtension(Paths.get(fileInfo.getOrgPath())).toUpperCase());
        label.setAlignment(Pos.CENTER_LEFT);
        label.getStyleClass().add("fileExtension");
        label.setId("fileExtension");
//        label.setPadding(new Insets(5, 0, 0, 5));
        return label;
    }

    public static Label createSpacer(double height) {
        Label label = new Label("");
        label.setMinHeight(height);
        label.setMaxHeight(height);
        label.setPrefHeight(height);
        return label;
    }
}
