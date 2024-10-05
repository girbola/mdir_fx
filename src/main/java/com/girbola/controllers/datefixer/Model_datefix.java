/*
 @(#)Copyright:  Copyright (c) 2012-2024 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.datefixer;


import com.girbola.Main;
import com.girbola.concurrency.ConcurrencyUtils;
import com.girbola.controllers.datefixer.DateFix_Utils.Field;
import com.girbola.controllers.datefixer.utils.DateFixGuiUtils;
import com.girbola.controllers.main.Model_main;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.controllers.main.tables.FolderInfo_Utils;
import com.girbola.dialogs.Dialogs;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.sql.SqliteConnection;
import com.girbola.thumbinfo.ThumbInfo;
import com.girbola.utils.FileInfoUtils;
import com.girbola.workdir.WorkDirHandler;
import common.utils.Conversion;
import common.utils.FileNameParseUtils;
import common.utils.FileUtils;
import common.utils.date.DateUtils;
import java.util.*;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.girbola.Main.bundle;
import static com.girbola.Main.simpleDates;
import static com.girbola.messages.Messages.*;

/**
 * @author Marko Lokka
 */
public class Model_datefix extends DateFixerModel {
    private final String ERROR = Model_datefix.class.getSimpleName();
    private Model_main model_Main;

    private ScheduledExecutorService selector_exec = Executors.newScheduledThreadPool(1);
    private ObservableList<String> workDir_obs = FXCollections.observableArrayList();

    private VBox infoTables_container;
    private ObservableList<MetaData> metaDataTableView_obs = FXCollections.observableArrayList();

    private SimpleBooleanProperty changes_made = new SimpleBooleanProperty(false);
    private SimpleBooleanProperty ignored = new SimpleBooleanProperty(false);
    private SimpleBooleanProperty copied = new SimpleBooleanProperty(false);
    private SimpleBooleanProperty events = new SimpleBooleanProperty(true);
    private SimpleBooleanProperty locations = new SimpleBooleanProperty(true);

    private AtomicBoolean content_changed = new AtomicBoolean(false);
    private ObservableHandler observableHandler = new ObservableHandler();

    private AnchorPane anchorPane;
    private FolderInfo folderInfo_full;
    private FolderInfo folderInfo_filtered;

    private TilePane tilePane;

    //private GridPane gridPane;
    private TableView<EXIF_Data_Selector> cameras_TableView;
    private TableView<EXIF_Data_Selector> dates_TableView;
    private TableView<EXIF_Data_Selector> events_TableView;
    private TableView<EXIF_Data_Selector> locations_TableView;

    private VBox rightInfoPanel;
    private TableView<MetaData> metaDataTableView;
    private SimpleBooleanProperty rightInfo_visible = new SimpleBooleanProperty(false);

    private DateFix_Utils dateFix_Utils = new DateFix_Utils();

    private Path currentFolderPath;
    private QuickPick_Navigator quickPick_Navigator;
    private ScrollPane scrollPane;
    private SelectionModel selectionModel = new SelectionModel();
    private TilePane quickPick_tilePane;
    private int imagesPerLine;

    private WorkDirHandler workDirHandler;

    private RenderVisibleNode renderVisibleNode = null;
    private Connection connection;
    private ObservableList<Node> allNodes = FXCollections.observableArrayList();
    private ObservableList<Node> notVisibleNodes = FXCollections.observableArrayList();

    public Model_datefix(Model_main model_Main, Path aCurrentFolderPath) {
        this.currentFolderPath = aCurrentFolderPath;
        this.model_Main = model_Main;
        this.connection = SqliteConnection.connector(currentFolderPath, Main.conf.getMdir_db_fileName());
    }

    public void instantiateRenderVisibleNodes() {
        if (renderVisibleNode == null) {
            renderVisibleNode = new RenderVisibleNode(scrollPane, currentFolderPath, this.connection);
        }
    }

    public void updateCameraInfos(List<FileInfo> fileInfo_List) {
        getCameras_TableView().getItems().clear();
        getDateFix_Utils().createTableEXIF_Data_Selector_list(fileInfo_List, getCameras_TableView().getItems(), Field.CAMERA.getType());

    }

    public void updateDateInfos(List<FileInfo> fileInfo_List) {
        getDates_TableView().getItems().clear();
        getDateFix_Utils().createDates_list(fileInfo_List);
    }

    public void updateLocationInfos(List<FileInfo> fileInfo_List) {
        getLocations_TableView().getItems().clear();
        getDateFix_Utils().createTableEXIF_Data_Selector_list(fileInfo_List, getLocations_TableView().getItems(), Field.LOCATION.getType());

    }

    public void updateEventsInfos(List<FileInfo> fileInfo_List) {
        getEvents_TableView().getItems().clear();
        getDateFix_Utils().createTableEXIF_Data_Selector_list(fileInfo_List, getEvents_TableView().getItems(), Field.EVENT.getType());
    }

    public void updateAllInfos(List<FileInfo> fileInfo_List) {
        getCameras_TableView().getItems().clear();
        getDates_TableView().getItems().clear();
        getEvents_TableView().getItems().clear();
        getLocations_TableView().getItems().clear();

        updateCameraInfos(fileInfo_List);
        updateDateInfos(fileInfo_List);
        updateEventsInfos(fileInfo_List);
        updateLocationInfos(fileInfo_List);
    }

    public void updateAllInfos(TilePane gridPane) {
        getCameras_TableView().getItems().clear();
        getDates_TableView().getItems().clear();
        getEvents_TableView().getItems().clear();
        getLocations_TableView().getItems().clear();

        updateCameraInfo(tilePane);
        updateDatesInfos(tilePane);
        updateEventsInfos(tilePane);
        updateLocationsInfos(tilePane);
    }

    public void updateCameraInfo(TilePane tilePane) {

        List<FileInfo> fileInfo_list = new ArrayList<>();
        for (Node node : tilePane.getChildren()) {
            if (node instanceof VBox) {
                FileInfo fileInfo = (FileInfo) node.getUserData();
                if (fileInfo != null) {
                    fileInfo_list.add(fileInfo);
                }
            }
        }
        if (getCameras_TableView() == null) {
            Messages.sprintfError("Cameras TableView were null!!!!");
        } else {
            dateFix_Utils.createTableEXIF_Data_Selector_list(fileInfo_list, getCameras_TableView().getItems(), Field.CAMERA.getType());
        }
    }

    public void updateEventsInfos(TilePane tilePane) {
        List<FileInfo> fileInfo_list = new ArrayList<>();
        for (Node node : tilePane.getChildren()) {
            if (node instanceof VBox) {
                FileInfo fileInfo = (FileInfo) node.getUserData();
                if (fileInfo != null) {
                    fileInfo_list.add(fileInfo);
                }
            }
        }
        dateFix_Utils.createTableEXIF_Data_Selector_list(fileInfo_list, getEvents_TableView().getItems(), Field.EVENT.getType());
    }

    public void updateLocationsInfos(TilePane tilePane) {
        List<FileInfo> fileInfo_list = new ArrayList<>();
        for (Node node : tilePane.getChildren()) {
            if (node instanceof VBox) {
                FileInfo fileInfo = (FileInfo) node.getUserData();
                if (fileInfo != null) {
                    fileInfo_list.add(fileInfo);
                }
            }
        }
        dateFix_Utils.createTableEXIF_Data_Selector_list(fileInfo_list, getLocations_TableView().getItems(), Field.LOCATION.getType());
    }

    public void updateDatesInfos(TilePane tilePane) {
        List<FileInfo> fileInfo_list = new ArrayList<>();
        for (Node node : tilePane.getChildren()) {
            if (node instanceof VBox && node.getId().equals("imageFrame")) {
                FileInfo fileInfo = (FileInfo) node.getUserData();
                if (fileInfo != null) {
                    fileInfo_list.add(fileInfo);
                } else {
                    Messages.errorSmth(ERROR, "fileinfo were null!", null, Misc.getLineNumber(), true);
                }
            }
        }
        // wef;
        dateFix_Utils.createDates_list(fileInfo_list);
    }

    public void acceptEverything(TilePane tilePane) {
        boolean changed = false;
        // CssStylesController css = new CssStylesController();
        Dialog<ButtonType> changesDialog = Dialogs.createDialog_YesNo(Main.scene_Switcher.getScene_dateFixer().getWindow(), bundle.getString("iHaveCheckedEverythingAndAcceptAllChanges"));

        Optional<ButtonType> result = changesDialog.showAndWait();
        if (result.get().getButtonData().equals(ButtonBar.ButtonData.YES)) {
            for (Node node : tilePane.getChildren()) {
                if (node instanceof VBox && node.getId().equals("imageFrame")) {
                    HBox bottom = DateFixGuiUtils.getBottomHBox(node);
                    Label tf = DateFixGuiUtils.getFileDateLabel(node);
                    if (tf != null) {
                        if (!tf.getStyle().equals(CssStylesEnum.BAD_STYLE.getStyle())) {
                            FileInfo fileInfo = (FileInfo) node.getUserData();
                            if (fileInfo != null) {
                                if (!fileInfo.isIgnored()) {
                                    fileInfo.setDate(Conversion.stringDateToLong(tf.getText(), simpleDates.getSdf_ymd_hms_minusDots_default()));
                                    fileInfo.setGood(true);
                                    fileInfo.setSuggested(false);
                                    fileInfo.setBad(false);
                                    fileInfo.setConfirmed(true);
                                    node.setStyle(CssStylesEnum.GOOD_STYLE.getStyle());
                                    changed = true;
                                }
                            }
                        }
                    }
                }
            }
            if (changed) {
                // TODO Korjaa apply_btn;
                FolderInfo_Utils.calculateFileInfoStatuses(getFolderInfo_full());
                if (model_Main.tables() == null) {
                    Main.setProcessCancelled(true);
                    errorSmth(ERROR, "", null, Misc.getLineNumber(), true);
                }
                model_Main.tables().refreshAllTables();
                updateAllInfos(getFolderInfo_full().getFileInfoList());

                getFolderInfo_full().setChanged(true);
                setDateTime(getFolderInfo_full().getMinDate(), true);
                setDateTime(getFolderInfo_full().getMaxDate(), false);
            }
            getSelectionModel().clearAll(tilePane);
        }
        /*

         */
    }

    public void dateFromFileName() {
        if (getSelectionModel().getSelectionList().isEmpty()) {
            warningText(Main.bundle.getString("youHaventSelectedMedia"));
            return;
        }
        for (Node node : getSelectionModel().getSelectionList()) {
            if (node instanceof VBox && node.getId().equals("imageFrame")) {
                FileInfo fileInfo = (FileInfo) node.getUserData();
                if (fileInfo != null) {
                    long date = FileNameParseUtils.hasFileNameDate(Paths.get(fileInfo.getOrgPath()));
                    HBox bottom = DateFixGuiUtils.getBottomHBox(node);
                    Label tf = DateFixGuiUtils.getFileDateLabel(node);
                    tf.setText("" + DateUtils.longToLocalDateTime(date).format(Main.simpleDates.getDtf_ymd_hms_minusDots_default()));
                    if (date != 0) {
                        bottom.setStyle(CssStylesEnum.GOOD_STYLE.getStyle());
                    } else {
                        bottom.setStyle(CssStylesEnum.BAD_STYLE.getStyle());
                    }
                }
            }
        }
    }

    private HBox getFileDateHBox(Node node) {
        if (node instanceof VBox) {
            if (node.getId().equals("imageFrame")) {
                for (Node node2 : ((VBox) node).getChildren()) {
                    if (node2 instanceof HBox) {
                        return (HBox) node2;
                    }
                }
            }
        }
        return null;
    }

    public void restoreSelectedExifDateInfos() {

        if (getSelectionModel().getSelectionList().isEmpty()) {
            warningText(Main.bundle.getString("youHaventSelectedMedia"));
            return;
        }
        for (Node node : getSelectionModel().getSelectionList()) {
            if (node instanceof VBox && node.getId().equals("imageFrame")) {
                sprintf("Node is: " + node.getId() + " NODE ALL INFO: " + node.toString());
                HBox bottomHBox = DateFixGuiUtils.getBottomHBox(node);
                Label tf = DateFixGuiUtils.getFileDateLabel(node);

                FileInfo fileInfo = (FileInfo) node.getUserData();

                tf.setText("" + DateUtils.longToLocalDateTime(fileInfo.getDate()).format(Main.simpleDates.getDtf_ymd_hms_minusDots_default()));
                if (fileInfo.getDate() != 0) {
                    bottomHBox.setStyle(CssStylesEnum.GOOD_STYLE.getStyle());
                } else {
                    bottomHBox.setStyle(CssStylesEnum.BAD_STYLE.getStyle());
                }

            } else {
                sprintf("restoring Selected Exif dates imageFrame were not VBox nor imageFrame. Node was: " + node);
            }
        }
    }

    public void restoreLastModified() {
        sprintf("lastModified_date_btn pressed");
        if (getSelectionModel().getSelectionList().isEmpty()) {
            warningText(Main.bundle.getString("youHaventSelectedMedia"));
            return;
        }
        for (Node node : getSelectionModel().getSelectionList()) {
            sprintf("Node is: " + node.getId() + " NODE ALL INFO: " + node.toString());
            if (node instanceof VBox && node.getId().equals("imageFrame")) {
                FileInfo fileInfo = (FileInfo) node.getUserData();
                if (fileInfo != null) {
                    File file = new File(fileInfo.getOrgPath());
                    sprintf("Node name is: " + node + " LastMod was: " + DateUtils.longToLocalDateTime(file.lastModified()).format(Main.simpleDates.getDtf_ymd_hms_minusDots_default()));
                    HBox bottom = DateFixGuiUtils.getBottomHBox(node);
                    Label tf = DateFixGuiUtils.getFileDateLabel(node);
                    if (tf != null) {
                        tf.setText("" + DateUtils.longToLocalDateTime(file.lastModified()).format(Main.simpleDates.getDtf_ymd_hms_minusDots_default()));
                        bottom.setStyle(CssStylesEnum.MODIFIED_STYLE.getStyle());
                    } else {
                        Messages.sprintfError("fileDate Label were null:  " + tf);
                    }
                }
            }
        }
    }

    public void renameFileNameWithDate() {
        sprintf("touchFileNameWithDate pressed");
        if (getSelectionModel().getSelectionList().isEmpty()) {
            warningText(Main.bundle.getString("youHaventSelectedMedia"));
            return;
        }
        for (Node node : getSelectionModel().getSelectionList()) {
            sprintf("Node is: " + node.getId() + " NODE ALL INFO: " + node.toString());
            FileInfo fileInfo = (FileInfo) node.getUserData();
            if (fileInfo != null) {
                Label tf = DateFixGuiUtils.getFileDateLabel(node);
                Path sourceFile = Paths.get(fileInfo.getOrgPath());
                Path textFieldMergedFileName = Paths.get(sourceFile.getRoot().toString() + tf);

                sprintf("Node name is: " + node + " LastMod was: " + DateUtils.longToLocalDateTime(sourceFile.toFile().lastModified()).format(Main.simpleDates.getDtf_ymd_hms_minusDots_default()));

                try {
                    Path newPath = FileUtils.renameFile(sourceFile, textFieldMergedFileName);
                    if (newPath != null) {
                        FileUtils.moveFile(fileInfo, sourceFile, newPath);

                        fileInfo.setOrgPath(textFieldMergedFileName.toString());
                        tf.setText("" + textFieldMergedFileName.getFileName());
                    } else {
                        sprintfError("Cannot rename file: " + textFieldMergedFileName + " target: " + sourceFile);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

//				if (tf != null) {
//					tf.setText("" + DateUtils.longToLocalDateTime(file.lastModified())
//							.format(Main.simpleDates.getDtf_ymd_hms_minusDots_default()));
//					tf.setStyle(CssStylesController.MODIFIED_STYLE.getStyle());
//				}
            } else {
                sprintf("FileInfo were getUserData were null");
            }
        }
    }

    public void exitDateFixerWindow(TilePane tilePane, Window owner, WindowEvent event) {
        Messages.sprintf("exitDateFixerWindow");
        model_Main.getMonitorExternalDriveConnectivity().cancel();

        int badDates = checkIfRedDates(tilePane);
        if (badDates != 0) {
            Dialog<ButtonType> dialog = Dialogs.createDialog_YesNoCancel(owner, bundle.getString("badFilesFoundWantToClose"));

            Messages.sprintf("2changesDialog width: " + dialog.getWidth());
            //Iterator<ButtonType> iterator = dialog.getDialogPane().getButtonTypes().iterator();

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.get().getButtonData().equals(ButtonBar.ButtonData.YES)) {
                Stage stage = (Stage) Main.scene_Switcher.getScene_dateFixer().getWindow();
                stage.setScene(Main.scene_Switcher.getScene_dateFixer());

                if (Main.conf.isSavingThumb()) {

                    Main.setProcessCancelled(true);
                    getRenderVisibleNode().stopTimeLine();
                    ConcurrencyUtils.stopExecThreadNow();

                    Task<List<ThumbInfo>> task = new SaveThumbInfos(connection, currentFolderPath, tilePane);

                    task.setOnSucceeded(workerStateEvent -> {
                        Messages.sprintf("saveThumb were succeeded: " + workerStateEvent.getSource().getMessage());

                        event.consume();
                    });
                    task.setOnFailed(workerStateEvent -> {
                        event.consume();
                        Messages.sprintf("saveThumb were failed");
                    });
                    task.setOnCancelled(workerStateEvent -> {
                        event.consume();
                        Messages.sprintf("saveThumb were cancelled");
                    });

                    Thread thread = new Thread(task);
                    thread.start();
                }

            } else if (result.get().getButtonData().equals(ButtonBar.ButtonData.CANCEL_CLOSE)) {
                event.consume();
                return;
            }
        }

        if (changes_made.get()) {
            Messages.sprintf("changes made");
            Dialog<ButtonType> changesDialog = Dialogs.createDialog_YesNoCancel(owner, bundle.getString("changesMade"));

            Messages.sprintf("changesDialog width: " + changesDialog.getWidth());

            Iterator<ButtonType> iterator = changesDialog.getDialogPane().getButtonTypes().iterator();
            while (iterator.hasNext()) {
                ButtonType next = iterator.next();
                Messages.sprintf("NEEEXT; " + next.getText());
            }
            Optional<ButtonType> result = changesDialog.showAndWait();
            if (result.get().getButtonData().equals(ButtonBar.ButtonData.YES)) {
                acceptEverything(tilePane);
                Stage stage = (Stage) Main.scene_Switcher.getScene_dateFixer().getWindow();
                stage.setScene(Main.scene_Switcher.getScene_dateFixer());
                Main.scene_Switcher.getWindow().setOnCloseRequest(model_Main.exitProgram);
                model_Main.getMonitorExternalDriveConnectivity().restart();
                Platform.runLater(() -> {
                    Main.scene_Switcher.getWindow().setOnCloseRequest(model_Main.exitProgram);
                    Main.scene_Switcher.getWindow().setScene(Main.scene_Switcher.getScene_main());
                });

                event.consume();
//				return;
            } else if (result.get().getButtonData().equals(ButtonBar.ButtonData.NO)) {
                Messages.sprintf("No PRESSED: NO changes saved");

                Main.scene_Switcher.getWindow().setOnCloseRequest(model_Main.exitProgram);
                model_Main.getMonitorExternalDriveConnectivity().restart();
                Platform.runLater(() -> {
                    Main.scene_Switcher.getWindow().setOnCloseRequest(model_Main.exitProgram);
                    Main.scene_Switcher.getWindow().setScene(Main.scene_Switcher.getScene_main());
                });
                event.consume();
            } else if (result.get().getButtonData().equals(ButtonBar.ButtonData.CANCEL_CLOSE)) {
                Messages.sprintf("Cancel pressed!");
                event.consume();
                return;
            }
        } else {
            Messages.sprintf("No changes made");
            Main.scene_Switcher.getWindow().setScene(Main.scene_Switcher.getScene_dateFixer());
            Main.scene_Switcher.getWindow().setOnCloseRequest(model_Main.exitProgram);

            event.consume();
        }
        getSelectionModel().clearAll(tilePane);
        selector_exec.shutdownNow();
        Platform.runLater(() -> {
            Main.scene_Switcher.getWindow().setScene(Main.scene_Switcher.getScene_main());
            Messages.sprintf("Changing back to main");
        });

    }

    public List<FileInfo> observableNode_toList(ObservableList<Node> filterlist) {
        ArrayList<FileInfo> arrayList = new ArrayList<>();
        for (Node node : filterlist) {
            if (node instanceof VBox && node.getId().equals("imageFrame")) {
                FileInfo fileInfo = (FileInfo) node.getUserData();
                arrayList.add(fileInfo);
            }
        }
        return null;
    }

    public BooleanProperty ignored_property() {
        return this.ignored;
    }

    public BooleanProperty events_property() {
        return this.events;
    }

    public BooleanProperty copied_property() {
        return this.copied;
    }

    public BooleanProperty locations_property() {
        return this.locations;
    }

    public boolean getIgnored() {
        return this.ignored.get();
    }

    public boolean getEvents() {
        return this.events.get();
    }

    public boolean getCopied() {
        return this.copied.get();
    }

    public boolean getLocations() {
        return this.locations.get();
    }

    public boolean fileInfoisShowing_(FileInfo fileInfo) {
        boolean hide = false;
        if (!copied.get()) {
            if (fileInfo.isCopied()) {
                hide = false;
            }
        }
        if (!ignored.get()) {
            if (fileInfo.isIgnored()) {
                hide = false;
            }
        }
//		if (!events.get()) {
//			if (fileInfo.getEvent().length() >= 1) {
//				hide = true;
//			}
//		}
//		if (!locations.get()) {
//			if (fileInfo.getLocation().length() >= 1) {
//				hide = true;
//			}
//		}

        return hide;

    }

    /**
     * Collects everything into ObservableList<Node> observable
     *
     * @param obs
     * @return ObservableList<Node>
     */

    public ObservableList<Node> filterAllNodesList(ObservableList<Node> obs) {
        ObservableList<Node> observable = FXCollections.observableArrayList();
        for (Node node : obs) {
            FileInfo fileInfo = (FileInfo) node.getUserData();

            if (!copied.get()) {
                if (!fileInfo.isIgnored()) {
                    observable.add(node);
                }
            } else {
                if (fileInfo.isCopied()) {
                    observable.remove(node);
                }
            }

        }
        return observable;
    }

    public void deselectAllExifDataSelectors() {
        for (EXIF_Data_Selector eds : getCameras_TableView().getItems()) {
            eds.setIsShowing(false);
        }
        for (EXIF_Data_Selector eds : getDates_TableView().getItems()) {
            eds.setIsShowing(false);
        }
        for (EXIF_Data_Selector eds : getEvents_TableView().getItems()) {
            eds.setIsShowing(false);
        }
        for (EXIF_Data_Selector eds : getLocations_TableView().getItems()) {
            eds.setIsShowing(false);
        }
    }

    /**
     * @return the selector_exec
     */
    public final ScheduledExecutorService getSelector_exec() {
        if (selector_exec.isShutdown() || selector_exec.isTerminated()) {
            selector_exec = Executors.newScheduledThreadPool(1);
        }
        return selector_exec;
    }

    private int checkIfRedDates(TilePane tilePane) {
        int counter = 0;
        for (Node n : tilePane.getChildren()) {
            if (n instanceof VBox) {
                if (n.getId().contains("imageFrame")) {
                    for (Node vbox : ((VBox) n).getChildren()) {
                        if (vbox instanceof HBox) {
                            for (Node hbox : ((HBox) vbox).getChildren()) {
                                if (hbox instanceof TextField) {
                                    if (hbox.getStyle().equals(CssStylesEnum.BAD_STYLE.getStyle())) {
                                        counter++;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return counter;
    }

    public AnchorPane getAnchorPane() {
        return anchorPane;
    }

    public AtomicBoolean getContent_changed() {
        return content_changed;
    }

    public boolean getRightInfo_visible() {
        return rightInfo_visible.get();
    }

    public BooleanProperty getChanges_made() {
        return changes_made;
    }

    public DateFix_Utils getDateFix_Utils() {
        return dateFix_Utils;
    }

    public final ObservableHandler getObservableHandler() {
        return observableHandler;
    }

    public final ObservableList<String> getWorkDir_obs() {
        return workDir_obs;
    }

    public FolderInfo getFolderInfo_filtered() {
        return folderInfo_filtered;
    }

    public FolderInfo getFolderInfo_full() {
        return folderInfo_full;
    }

    public int getImagesPerLine() {
        return imagesPerLine;
    }

    public ObservableList<MetaData> getMetaDataTableView_obs() {
        return this.metaDataTableView_obs;
    }

    public ObservableList<Node> getAllNodes() {
        return allNodes;
    }

    public Path getCurrentFolderPath() {
        return currentFolderPath;
    }

    public QuickPick_Navigator getQuickPick_Navigator() {
        return this.quickPick_Navigator;
    }

    public RenderVisibleNode getRenderVisibleNode() {
        return renderVisibleNode;
    }

    public ScrollPane getScrollPane() {
        return scrollPane;
    }

    public SelectionModel getSelectionModel() {
        return this.selectionModel;
    }

    public TableView<EXIF_Data_Selector> getCameras_TableView() {
        return cameras_TableView;
    }

    public TableView<EXIF_Data_Selector> getDates_TableView() {
        return dates_TableView;
    }

    public TableView<EXIF_Data_Selector> getEvents_TableView() {
        return events_TableView;
    }

    public TableView<EXIF_Data_Selector> getLocations_TableView() {
        return locations_TableView;
    }

    public TableView<MetaData> getMetaDataTableView() {
        return this.metaDataTableView;
    }

    public TilePane getQuickPick_tilePane() {
        return quickPick_tilePane;
    }

    public TilePane getTilePane() {
        return tilePane;
    }

    public VBox getInfoTables_container() {
        return infoTables_container;
    }

    public VBox getRightInfoPanel() {
        return rightInfoPanel;
    }

    public void setAllNodes(ObservableList<Node> allNodes) {
        this.allNodes = allNodes;
    }

    public void setAnchorPane(AnchorPane anchorPane) {
        this.anchorPane = anchorPane;
    }

    public void setCameras_TableView(TableView<EXIF_Data_Selector> cameras_TableView) {
        this.cameras_TableView = cameras_TableView;
    }

    public void setChanges_made(boolean changes_made) {
        this.changes_made.set(changes_made);
    }

    public void setContent_changed(AtomicBoolean content_changed) {
        this.content_changed = content_changed;
    }

    public void setCurrentFolderPath(Path currentFilePath) {
        this.currentFolderPath = currentFilePath;
    }

    public void setDates_TableView(TableView<EXIF_Data_Selector> dates_TableView) {
        this.dates_TableView = dates_TableView;
    }

    public void setEvents_TableView(TableView<EXIF_Data_Selector> table) {
        this.events_TableView = table;
    }

    public void setFolderInfo_filtered(FolderInfo folderInfo_filtered) {
        this.folderInfo_filtered = folderInfo_filtered;
    }

    public void setFolderInfo_full(FolderInfo aCurrentFolderInfo) {
        this.folderInfo_full = aCurrentFolderInfo;
    }

    public void setImagesPerLine(int imagesPerLine) {
        this.imagesPerLine = imagesPerLine;
    }

    public void setInfoTables_container(VBox infoTables_container) {
        this.infoTables_container = infoTables_container;
    }

    public void setLocations_TableView(TableView<EXIF_Data_Selector> table) {
        this.locations_TableView = table;
    }

    public void setMetaDataTableView(TableView<MetaData> metadataTableView) {
        this.metaDataTableView = metadataTableView;
    }

    public void setQuickPick_Navigator(QuickPick_Navigator aQuickPick_Navigator) {
        this.quickPick_Navigator = aQuickPick_Navigator;
    }

    public void setQuickPick_tilePane(TilePane quickPick_tilePane) {
        this.quickPick_tilePane = quickPick_tilePane;
    }

    public void setRightInfo_visible(boolean value) {
        this.rightInfo_visible.set(value);
    }

    public void setRightInfoPanel(VBox rightInfoPanel) {
        this.rightInfoPanel = rightInfoPanel;
    }

    public void setScrollPane(ScrollPane scrollPane) {
        this.scrollPane = scrollPane;
    }

    public void setTilePane(TilePane tilePane) {
        this.tilePane = tilePane;
    }

    public void setWorkDir_Handler(WorkDirHandler workDirHandler) {
        this.workDirHandler = workDirHandler;
    }

    public WorkDirHandler getWorkDir_Handler() {
        return workDirHandler;
    }

    public ObservableList<Node> getNotVisibleNodes() {
        return this.notVisibleNodes;
    }
}
