/*
 @(#)Copyright:  Copyright (c) 2012-2024 All right reserved.
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.datefixer;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.girbola.Main;
import com.girbola.configuration.GuiImageFrame;
import com.girbola.controllers.datefixer.utils.DateFixGuiUtils;
import com.girbola.controllers.loading.LoadingProcessTask;
import com.girbola.controllers.main.tables.FolderInfo;
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
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
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

import static com.girbola.Main.*;
import static com.girbola.controllers.datefixer.ImageUtils.playVideo;
import static com.girbola.messages.Messages.errorSmth;
import static com.girbola.messages.Messages.sprintf;
import static com.girbola.misc.Misc.getLineNumber;

/**
 * @author Marko Lokka
 */
public class DateFixPopulateQuickPick extends Task<ObservableList<Node>> {

    private final static String ERROR = DateFixPopulateQuickPick.class.getSimpleName();

    private FolderInfo folderInfo;
    //private GridPane gridPane;
    private TilePane quickPick_tilePane;
    private Model_datefix model_dateFix;
    private ScrollPane scrollPane;
    private Scene scene;
    private DoubleProperty node_height = new SimpleDoubleProperty(0);

    private VBox frame;
    private AtomicInteger counter = new AtomicInteger(0);
    private LoadingProcessTask loadingProcess_task;

    private TilePane tilePane;

    public DateFixPopulateQuickPick(Scene scene, Model_datefix aModel_datefix, TilePane aTilePane, LoadingProcessTask loadingProcess_task) {
        this.scene = scene;
        this.model_dateFix = aModel_datefix;
        this.quickPick_tilePane = aModel_datefix.getQuickPick_tilePane();
        this.tilePane = aTilePane;
        this.model_dateFix.getSelectionModel().clearAll(tilePane);
        this.folderInfo = aModel_datefix.getFolderInfo_full();
        this.scrollPane = aModel_datefix.getScrollPane();
        this.loadingProcess_task = loadingProcess_task;
        sprintf("FolderInfo.getMinDate(): " + this.folderInfo.getMinDate());
        aModel_datefix.setDateTime(this.folderInfo.getMinDate(), true);
        aModel_datefix.setDateTime(this.folderInfo.getMaxDate(), false);
        tilePane.setHgap(8);
        tilePane.setVgap(8);

    }

    @Override
    protected ObservableList<Node> call() throws Exception {
        ObservableList<Node> nodes = FXCollections.observableArrayList();
        for (FileInfo fi : folderInfo.getFileInfoList()) {
            if (Main.getProcessCancelled()) {
                cancel();
                sprintf("Process has been cancelled!");
                return null;
            }
            if (Files.exists(Paths.get(fi.getOrgPath()))) {
                if (fi.isImage() || fi.isVideo() || fi.isRaw()) {
                    Messages.sprintf("DATEFIX: " + fi.getOrgPath());

                    frame = null;
                    if (fi.isImage() || fi.isRaw()) {
                        frame = createImageFrame(fi, counter.get());
                        setSelectedImageRoutine(fi, frame);
                    } else if (fi.isVideo()) {
                        frame = createImageFrame(fi, counter.get());
                        //setSelectedVideoRoutine(fi, frame);
                    }
                    if (frame != null) {
                        frame.setUserData(fi);

                        Button statusButton = model_dateFix.getQuickPick_Navigator().createStatusButton(node_height.get(), frame);
                        if (statusButton != null) {
                            if (quickPick_tilePane == null) {
                                Messages.errorSmth(ERROR, "quickPick_tilePane were null!!!!", null, Misc.getLineNumber(), true);
                            }
                            // TODO Jotain vikaa quickpick navigatorissa. Hmmmmmmmmmmmmmmm
                            Messages.sprintf("StatusButton were not null: " + statusButton);
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    quickPick_tilePane.getChildren().add(statusButton);
                                }
                            });
                        }

                        nodes.add(frame);
//                            model_dateFix.getTilePane().getChildren().add(frame);
                        counter.incrementAndGet();
                    }
                }
            }
        }
        return nodes;
    };

    private VBox createImageFrame(FileInfo fileInfo, int index) {
        VBox frame_vbox = DateFixGuiUtils.createImageFrame(GuiImageFrame.imageFrame_x, GuiImageFrame.imageFrame_y);

        GridPane topContainer = DateFixGuiUtils.createTopGridPane();

        Label imageFrameNumber = DateFixGuiUtils.createImageNumberLbl(index + 1);
        imageFrameNumber.setAlignment(Pos.TOP_RIGHT);

        Label fileExtension = new Label(FileUtils.getExtension(Paths.get(fileInfo.getOrgPath())).toUpperCase());
        fileExtension.getStyleClass().add("fileExtension");
        fileExtension.setId("fileExtension");

        topContainer.add(imageFrameNumber, 4, 0);
        topContainer.add(fileExtension, 0, 0);

        GridPane.setHalignment(imageFrameNumber, HPos.CENTER);
        GridPane.setHalignment(fileExtension, HPos.CENTER);

        HBox imageViewContainer = DateFixGuiUtils.createImageViewContainer(fileInfo, "imageViewContainer", GuiImageFrame.imageFrame_y);
        ImageView iv = DateFixGuiUtils.createImageView(fileInfo, (GuiImageFrame.thumb_x_MAX), GuiImageFrame.thumb_y_MAX);
        imageViewContainer.getChildren().add(iv);

        VBox bottomContainer = DateFixGuiUtils.createBottomContainer();

        HBox buttonDateTimeContainer = DateFixGuiUtils.createButtonDateTimeContainer();

        Label fileName_tf = DateFixGuiUtils.createFileName_tf(Paths.get(fileInfo.getOrgPath()));
        Label fileDate_tf = DateFixGuiUtils.createFileDate_tf(fileInfo, buttonDateTimeContainer);

        Button accept = DateFixGuiUtils.createAcceptButton(fileInfo, buttonDateTimeContainer, fileDate_tf);
        buttonDateTimeContainer.getChildren().addAll(accept, fileDate_tf);
        bottomContainer.getChildren().addAll(fileName_tf, buttonDateTimeContainer);

        HBox.setHgrow(fileDate_tf, Priority.ALWAYS);
        VBox.setVgrow(bottomContainer, Priority.NEVER);
        VBox.setVgrow(buttonDateTimeContainer, Priority.NEVER);
        VBox.setVgrow(iv, Priority.NEVER);
        VBox.setVgrow(topContainer, Priority.NEVER);

        frame_vbox.getChildren().addAll(topContainer, imageViewContainer, bottomContainer);

        return frame_vbox;
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
                    } else if (event.getClickCount() == 2) {
                        Messages.sprintf("Clickcount were 2");
                        List<FileInfo> list = getFileList(tilePane.getChildren());
                        if (Files.exists(Paths.get(fileInfo.getOrgPath()))) {
                            ImageUtils.view(list, fileInfo, Main.scene_Switcher.getScene_dateFixer().getWindow());
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

    private TitledPane createTitledPane() {
        return new TitledPane();
    }

    private void setSelectedVideoRoutine(FileInfo fileInfo, VBox frame) {
        frame.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY)) {
                if (event.getClickCount() == 1) {
                    handleImageFrameSelected(event, fileInfo);
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
                                Logger.getLogger(DateFixPopulateQuickPick.class.getName()).log(Level.SEVERE, null, ex);
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

            if (sourceNode instanceof VBox imageFrame && "imageFrame".equals(imageFrame.getId())) {
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

}
