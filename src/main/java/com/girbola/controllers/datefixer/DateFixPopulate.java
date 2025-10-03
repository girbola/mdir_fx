
package com.girbola.controllers.datefixer;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.girbola.Main;
import com.girbola.configuration.UIContants;
import com.girbola.controllers.datefixer.table.EXIF_Data_Selector;
import com.girbola.controllers.datefixer.utils.DateFixGuiUtils;
import com.girbola.controllers.loading.LoadingProcessTask;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import common.utils.FileUtils;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kordamp.ikonli.javafx.FontIcon;

import static com.girbola.Main.bundle;
import static com.girbola.Main.conf;
import static com.girbola.controllers.datefixer.ImageUtils.playVideo;
import static com.girbola.messages.Messages.errorSmth;
import static com.girbola.messages.Messages.sprintf;
import static com.girbola.misc.Misc.getLineNumber;


public class DateFixPopulate extends Task<ObservableList<Node>> {

    private final static String ERROR = DateFixPopulate.class.getSimpleName();

    private FolderInfo folderInfo;
    private TilePane quickPick_tilePane;
    private ModelDatefix model_dateFix;
    private ScrollPane scrollPane;
    private Scene scene;
    private DoubleProperty node_height = new SimpleDoubleProperty(0);

    private VBox frame;
    private AtomicInteger counter = new AtomicInteger(0);
    private LoadingProcessTask loadingProcess_task;

    private TilePane tilePane;

    public DateFixPopulate(Scene scene, ModelDatefix aModel_datefix, TilePane aTilePane, LoadingProcessTask loadingProcess_task) {
        this.scene = scene;
        this.model_dateFix = aModel_datefix;
        this.quickPick_tilePane = aModel_datefix.getQuickPick_tilePane();
        this.tilePane = aTilePane;
        this.model_dateFix.getSelectionModel().clearAll(tilePane);
        this.folderInfo = this.model_dateFix.getFolderInfo_full();
        this.scrollPane = aModel_datefix.getScrollPane();
        this.loadingProcess_task = loadingProcess_task;
        sprintf("FolderInfo.getMinDate(): " + this.folderInfo.getMinDate());
        aModel_datefix.setDateTime(this.folderInfo.getMinDate(), true);
        aModel_datefix.setDateTime(this.folderInfo.getMaxDate(), false);
        tilePane.setHgap(4);
        tilePane.setVgap(4);

    }

    @Override
    protected ObservableList<Node> call() throws Exception {
        ObservableList<Node> nodes = FXCollections.observableArrayList();
        List<FileInfo> fileInfoList = model_dateFix.getFolderInfo_full().getFileInfoList();

        Messages.sprintf("DateFixPopulate.call() and size: " + fileInfoList.size());

        for (FileInfo fileInfo : fileInfoList) {
            if (!validateAndProcessFile(fileInfo)) {
                continue;
            }

            VBox mediaFrame = processMediaFile(fileInfo);
            if (mediaFrame != null) {
                nodes.add(mediaFrame);
                counter.incrementAndGet();
            }
        }
        return nodes;
    }

    private boolean validateAndProcessFile(FileInfo fileInfo) {
        if (Main.getProcessCancelled()) {
            cancel();
            sprintf("Process has been cancelled!");
            return false;
        }

        return Files.exists(Paths.get(fileInfo.getOrgPath())) &&
                (fileInfo.isImage() || fileInfo.isVideo() || fileInfo.isRaw());
    }

    private VBox processMediaFile(FileInfo fileInfo) {
        Messages.sprintf("DATEFIX: " + fileInfo.getOrgPath());

        VBox mediaFrame = createMediaFrame(fileInfo);
        if (mediaFrame == null) {
            return null;
        }

        mediaFrame.setUserData(fileInfo);
        addStatusButton(mediaFrame);

        return mediaFrame;
    }

    private VBox createMediaFrame(FileInfo fileInfo) {
        if (fileInfo.isImage() || fileInfo.isRaw()) {
            VBox imageFrameImage = createImageFrame(fileInfo, counter.get());
            setSelectedImageRoutine(fileInfo, imageFrameImage);
            return imageFrameImage;
        } else if (fileInfo.isVideo()) {
            VBox imageFrameVideo = createImageFrame(fileInfo, counter.get());
            setSelectedVideoRoutine(fileInfo, imageFrameVideo);
            return imageFrameVideo;
        }
        return null;
    }

    private void addStatusButton(VBox mediaFrame) {
        Button statusButton = model_dateFix.getQuickPick_Navigator()
                .createStatusButton(node_height.get(), mediaFrame);

        if (statusButton == null) {
            return;
        }

        if (quickPick_tilePane == null) {
            Messages.errorSmth(ERROR, "quickPick_tilePane were null!!!!",
                    null, Misc.getLineNumber(), true);
            return;
        }

        Messages.sprintf("StatusButton were not null: " + statusButton);
        Platform.runLater(() -> quickPick_tilePane.getChildren().add(statusButton));
    }

    private VBox createImageFrame(FileInfo fileInfo, int index) {
        // Create main container
        VBox imageFrameVBox = DateFixGuiUtils.createImageFrame();
        VBox.setVgrow(imageFrameVBox, Priority.NEVER);

        // Create top section with file info
        VBox topSection = createTopSection(fileInfo, index);

        // Create middle section with image
        HBox imageSection = createImageSection(fileInfo);

        // Create bottom section with controls
        VBox bottomSection = createBottomSection(fileInfo);

        // Assemble the frame
        imageFrameVBox.getChildren().addAll(topSection, imageSection, bottomSection);

        return imageFrameVBox;
    }

    private VBox createTopSection(FileInfo fileInfo, int index) {
        VBox topVBox = DateFixGuiUtils.createInfoContainer(2);
        VBox.setVgrow(topVBox, Priority.NEVER);

        // First row: file extension and frame number
        HBox topInfo1 = createTopInfoRow1(fileInfo, index);

        // Second row: dimensions
        HBox topInfo2 = createTopInfoRow2(fileInfo);

        topVBox.getChildren().addAll(topInfo1, topInfo2);
        return topVBox;
    }

    private HBox createTopInfoRow1(FileInfo fileInfo, int index) {
        HBox topInfo1 = DateFixGuiUtils.createTopContainer(2);
        HBox.setHgrow(topInfo1, Priority.ALWAYS);

        Label fileExtension = DateFixGuiUtils.createFileExtension(fileInfo);

        // Create labels
        if(fileInfo.isVideo()) {
            fileExtension.setText("");
            FontIcon videoIcon = new FontIcon("bi-camera-reels");
            videoIcon.setIconSize(26);
            fileExtension.setGraphic(videoIcon);
        }
        Label leftSpacer = DateFixGuiUtils.createSpacer(30);
        Label rightSpacer = DateFixGuiUtils.createSpacer(30);
        Label imageFrameNumber = DateFixGuiUtils.createImageNumberLbl(index + 1);

        // Create center splitter
        HBox topContainerSplitter = DateFixGuiUtils.createTopContainer(10);
        topContainerSplitter.setAlignment(Pos.CENTER);
        HBox.setHgrow(topContainerSplitter, Priority.ALWAYS);

        // Configure spacers to grow
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);
        VBox.setVgrow(leftSpacer, Priority.ALWAYS);
        VBox.setVgrow(rightSpacer, Priority.ALWAYS);

        // Add all elements
        topInfo1.getChildren().setAll(fileExtension, leftSpacer, topContainerSplitter, rightSpacer, imageFrameNumber);

        return topInfo1;
    }

    private HBox createTopInfoRow2(FileInfo fileInfo) {
        HBox topInfo2 = DateFixGuiUtils.createTopContainer(10);

        Label dimensions = DateFixGuiUtils.createDimensionsLabel(fileInfo);
        HBox.setHgrow(dimensions, Priority.ALWAYS);

        topInfo2.getChildren().add(dimensions);

        return topInfo2;
    }

    private HBox createImageSection(FileInfo fileInfo) {
        HBox imageViewContainer = DateFixGuiUtils.createImageViewContainer();

        ImageView iv = DateFixGuiUtils.createImageView(
                fileInfo,
                UIContants.THUMBNAIL_MAX_WIDTH,
                UIContants.THUMBNAIL_MAX_HEIGHT
        );
        VBox.setVgrow(iv, Priority.NEVER);

        imageViewContainer.getChildren().add(iv);

        return imageViewContainer;
    }

    private VBox createBottomSection(FileInfo fileInfo) {
        VBox bottomContainer = DateFixGuiUtils.createBottomContainer();
        VBox.setVgrow(bottomContainer, Priority.NEVER);

        // File name label
        Label fileNameTextField = DateFixGuiUtils.createFileName_tf(Paths.get(fileInfo.getOrgPath()));

        // Date/time container with button and date field
        HBox buttonDateTimeContainer = createButtonDateTimeContainer(fileInfo);

        bottomContainer.getChildren().addAll(fileNameTextField, buttonDateTimeContainer);

        return bottomContainer;
    }

    private HBox createButtonDateTimeContainer(FileInfo fileInfo) {
        HBox buttonDateTimeContainer = DateFixGuiUtils.createButtonDateTimeContainer(6);
        VBox.setVgrow(buttonDateTimeContainer, Priority.ALWAYS);

        // Create date field
        Label fileDateTextField = DateFixGuiUtils.createFileDate_tf(fileInfo, buttonDateTimeContainer);
        HBox.setHgrow(fileDateTextField, Priority.ALWAYS);
        VBox.setVgrow(fileDateTextField, Priority.ALWAYS);

        // Create accept button
        Button accept = DateFixGuiUtils.createAcceptButton(fileInfo, buttonDateTimeContainer, fileDateTextField);

        // Create right filling region to balance layout
        Region rightFilling = createFillingRegion(accept);
Messages.sprintf("rightFilling WIDTH::::  " + rightFilling.getWidth());
        buttonDateTimeContainer.getChildren().addAll(accept, fileDateTextField, rightFilling);

        return buttonDateTimeContainer;
    }

    private Region createFillingRegion(Button referenceButton) {
        Region rightFilling = new Region();
        rightFilling.minWidthProperty().bind(referenceButton.widthProperty());
        rightFilling.prefWidthProperty().bind(referenceButton.widthProperty());
        rightFilling.maxWidthProperty().bind(referenceButton.widthProperty());
        return rightFilling;
    }

    private VBox createImageFrame_(FileInfo fileInfo, int index) {
        VBox imageFrameVBox = DateFixGuiUtils.createImageFrame();
//        VBox.setVgrow(imageFrameVBox, Priority.ALWAYS);
//        imageFrameVBox.setFillWidth(true);

        VBox topVBox = DateFixGuiUtils.createInfoContainer(2);

//        topVBox.setPrefHeight(50);
//        topVBox.setMinHeight(50);
//        topVBox.setMaxHeight(50);
        VBox.setVgrow(topVBox, Priority.NEVER);

        HBox topInfo1 = DateFixGuiUtils.createTopContainer(2);
        HBox.setHgrow(topInfo1, Priority.ALWAYS); // let top bar grow in parent

        Label imageFrameNumber = DateFixGuiUtils.createImageNumberLbl(index + 1);
        Label fileExtension = DateFixGuiUtils.createFileExtension(fileInfo);

        Label leftSpacer = DateFixGuiUtils.createSpacer(30);
        Label rightSpacer = DateFixGuiUtils.createSpacer(30);

        HBox topContainerSplitter = DateFixGuiUtils.createTopContainer(10);
        topContainerSplitter.setAlignment(Pos.CENTER);

        topInfo1.getChildren().setAll(fileExtension, leftSpacer, topContainerSplitter, rightSpacer, imageFrameNumber);

        HBox topInfo2 = DateFixGuiUtils.createTopContainer(10);
        Label dimensions = DateFixGuiUtils.createDimensionsLabel(fileInfo);

        topInfo2.getChildren().add(dimensions);
        HBox.setHgrow(dimensions, Priority.ALWAYS);
        HBox.setHgrow(topContainerSplitter, Priority.ALWAYS);
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);
        VBox.setVgrow(leftSpacer, Priority.ALWAYS);
        VBox.setVgrow(rightSpacer, Priority.ALWAYS);

        HBox imageViewContainer = DateFixGuiUtils.createImageViewContainer();
        ImageView iv = DateFixGuiUtils.createImageView(fileInfo, (UIContants.THUMBNAIL_MAX_WIDTH), UIContants.THUMBNAIL_MAX_HEIGHT);
        imageViewContainer.getChildren().add(iv);

        // Ensure the top bar has a definite width to distribute to spacers
        topInfo1.prefWidthProperty().bind(imageViewContainer.widthProperty());

        VBox bottomContainer = DateFixGuiUtils.createBottomContainer();

        HBox buttonDateTimeContainer = DateFixGuiUtils.createButtonDateTimeContainer(6);
        VBox.setVgrow(buttonDateTimeContainer, Priority.ALWAYS);

        Label fileNameTextField = DateFixGuiUtils.createFileName_tf(Paths.get(fileInfo.getOrgPath()));
        Label fileDateTextField = DateFixGuiUtils.createFileDate_tf(fileInfo, buttonDateTimeContainer);

        Button accept = DateFixGuiUtils.createAcceptButton(fileInfo, buttonDateTimeContainer, fileDateTextField);

        Region rightFilling = new Region();
        rightFilling.minWidthProperty().bind(accept.widthProperty());
        rightFilling.prefWidthProperty().bind(accept.widthProperty());
        rightFilling.maxWidthProperty().bind(accept.widthProperty());

        buttonDateTimeContainer.getChildren().addAll(accept, fileDateTextField, rightFilling);
        bottomContainer.getChildren().addAll(fileNameTextField, buttonDateTimeContainer);

        HBox.setHgrow(fileDateTextField, Priority.ALWAYS);
        VBox.setVgrow(fileDateTextField, Priority.ALWAYS);
        VBox.setVgrow(bottomContainer, Priority.NEVER);
        VBox.setVgrow(buttonDateTimeContainer, Priority.NEVER);
        VBox.setVgrow(iv, Priority.NEVER);

        topVBox.getChildren().addAll(topInfo1, topInfo2);
        imageFrameVBox.getChildren().addAll(topVBox, imageViewContainer, bottomContainer);

        return imageFrameVBox;
    }

    private Node createSpacer() {
        final Region spacer = new Region();
        // Make it always grow or shrink according to the available space
        VBox.setVgrow(spacer, Priority.ALWAYS);
        spacer.setStyle("-fx-background-color: orange;");
        return spacer;
    }

    private List<FileInfo> getFileList(ObservableList<Node> children) {
        List<FileInfo> list = new ArrayList<>();
        for (Node node : children) {
            FileInfo fileInfo = (FileInfo) node.getUserData();
            Messages.sprintf("getFileList: " + node + " fileInfo is: " + fileInfo.getOrgPath());
            list.add(fileInfo);
        }
        return list;
    }

    private void adjustTableHeight(TableView<MetaData> table, ObservableList<MetaData> obs) {
        table.setPrefHeight(obs.size() * 30);
        if (obs.size() <= 1) {
            table.setMinHeight(80);
        } else {
            table.setMinHeight(obs.size() * 30);
        }
        table.setMaxHeight(obs.size() * 30);
    }

    private void setSelectedImageRoutine(FileInfo fileInfo, VBox frame) {
        frame.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Messages.sprintf("Mouse button pressed");
                if (event.getButton().equals(MouseButton.PRIMARY)) {
                    if (event.getClickCount() == 1) {
                        Messages.sprintf("Clickcount were 1");
                        handleImageFrameSelected(event, fileInfo);

                        ObservableList<EXIF_Data_Selector> items = model_dateFix.getCameras_TableView().getItems();
                        if (items != null && !items.isEmpty()) {
                            items.forEach(item -> {
                                item.setIsShowing(false);
                            });
                        }
                        Platform.runLater(() -> {
                            model_dateFix.getCameras_TableView().getSelectionModel().clearSelection();
                        });
                        Messages.sprintf("SElected should be deselected");
                    } else if (event.getClickCount() == 2) {
                        Messages.sprintf("Clickcount were 2");
                        List<FileInfo> list = getFileList(tilePane.getChildren());
                        if (Files.exists(Paths.get(fileInfo.getOrgPath()))) {
                            ImageUtils.view(list, fileInfo, Main.sceneManager.getScene_dateFixer().getWindow());
                        } else {
                            Messages.errorSmth(ERROR, bundle.getString("imageNotExists") + " " + fileInfo.getOrgPath(), null, getLineNumber(), true);
                        }
                    } else {
                        Messages.sprintf("getselectionmodel. adding frame to selectionmodel");
                        model_dateFix.getSelectionModel().addWithToggle(frame);
                    }
                }
            }
        });
    }

    private TableView<MetaData> createTableView() {
        TableView<MetaData> table = new TableView<>();
        table.getStyleClass().add("metadataTable");

        TableColumn<MetaData, String> info_column = new TableColumn<>();
        info_column.setCellValueFactory((TableColumn.CellDataFeatures<MetaData, String> cellData) -> new SimpleObjectProperty<>(cellData.getValue().getTag()));

        TableColumn<MetaData, String> value_column = new TableColumn<>();
        value_column.setCellValueFactory((TableColumn.CellDataFeatures<MetaData, String> cellData) -> new SimpleObjectProperty<>(cellData.getValue().getValue()));
        table.getColumns().addAll(info_column, value_column);

        return table;
    }

    private void setSelectedVideoRoutine(FileInfo fileInfo, VBox frame) {
        frame.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY)) {
                if (event.getClickCount() == 1) {
                    handleImageFrameSelected(event, fileInfo);
                    ObservableList<EXIF_Data_Selector> items = model_dateFix.getCameras_TableView().getItems();
                    if (items != null && !items.isEmpty()) {
                        items.forEach(item -> {
                            item.setIsShowing(false);
                        });
                    }
                    Platform.runLater(() -> {
                        model_dateFix.getCameras_TableView().getSelectionModel().clearSelection();
                    });
                }
                if (event.getClickCount() == 2) {
                    Path path = Paths.get(fileInfo.getOrgPath());

                    if (conf.isVlcSupport()) {
                        sprintf(" if (conf.isVlcSupport()) {..");
                        if (Files.exists(path)) {
                            playVideo(path, frame);
                        } else {
                            Messages.errorSmth(ERROR, bundle.getString("imageNotExists") + " " + path, null, getLineNumber(), true);
                        }
                    } else {
                        if (Files.exists(path)) {
                            try {
                                Desktop.getDesktop().open(path.toFile());
                            } catch (IOException ex) {
                                Logger.getLogger(DateFixPopulate.class.getName()).log(Level.SEVERE, null, ex);
                                errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
                            }
                        } else {
                            Messages.errorSmth(ERROR, bundle.getString("imageNotExists") + " " + path, null, getLineNumber(), true);
                        }
                    }
                } else {
                    model_dateFix.getSelectionModel().addWithToggle(frame);
                }
            }
        });
    }

    private void handleImageFrameSelected(MouseEvent event, FileInfo fileInfo) {
        Messages.sprintf("Clicked setSelectedImageRoutine PRIMARY mouseclickcount = 1 " + event.getTarget().getClass().getName());
        Platform.runLater(() -> {

            // If this would be VBox it would give JavaFX Thread errors, so this is the way to avoid that issue. 2024-09-25
            Node sourceNode = (Node) event.getTarget();

            if (sourceNode instanceof VBox imageFrame && DateFixConstants.IMAGEFRAME.getType().equals(imageFrame.getId())) {
                model_dateFix.getRightInfoPanel().getChildren().clear();
                model_dateFix.getMetaDataTableView_obs().clear();
                model_dateFix.getSelectionModel().addWithToggle(imageFrame);
                for (Node node : imageFrame.getChildren()) {
                    if (node instanceof HBox imageFrameContainer) {
                        for (Node nodeImv : imageFrameContainer.getChildren()) {
                            if (nodeImv instanceof ImageView imv) {
                                loadMetadataToTable(imageFrame, imv, fileInfo);
                            }
                        }
                    }
                }
            }
        });
    }

    private void loadMetadataToTable(VBox selectedFrame, ImageView imv, FileInfo fileInfo) {
        Messages.sprintf("loadMetadataToTable: " + fileInfo.getOrgPath());
        if (imv.getImage() == null) {
            return;
        }

        if (!model_dateFix.getRightInfo_visible()) {
            return;
        }

        File file = new File(fileInfo.getOrgPath());
        model_dateFix.getMetaDataTableView_obs().add(new MetaData(Main.bundle.getString("filename"), file.toString()));

        try {
            Metadata metaData = ImageMetadataReader.readMetadata(file);
            updateUIWithMetadata(metaData);
        } catch (Exception e) {
            Messages.sprintfError("Cannot read metadata for media: " + file);
        }
    }

    private void updateUIWithMetadata(Metadata metaData) {
        for (Directory dir : metaData.getDirectories()) {
            if (dir == null || dir.getTags().isEmpty()) {
                continue;
            }

            TitledPane titledPane = createTitledPane();
            model_dateFix.getRightInfoPanel().getChildren().add(titledPane);
            titledPane.setText(dir.getName());

            ObservableList<MetaData> obs = FXCollections.observableArrayList();
            TableView<MetaData> table = createTableView();
            table.setItems(obs);
            titledPane.setContent(table);

            for (Tag tag : dir.getTags()) {
                obs.add(new MetaData(tag.getTagName(), tag.getDescription()));
            }

            adjustTableHeight(table, obs);
        }
    }

    private TitledPane createTitledPane() {
        return new TitledPane();
    }

}
