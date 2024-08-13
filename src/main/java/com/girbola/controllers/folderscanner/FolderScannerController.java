/*
 @(#)Copyright:  Copyright (c) 2012-2024 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.folderscanner;

import com.girbola.Main;
import com.girbola.controllers.main.Model_main;
import com.girbola.controllers.main.Tables;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.messages.Messages;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.girbola.concurrency.ConcurrencyUtils.initNewSingleExecutionService;
import static com.girbola.messages.Messages.sprintf;

public class FolderScannerController {

    private final String ERROR = FolderScannerController.class.getSimpleName();

    //@formatter:off
    /*
     * @FXML needed! SelectedFoldersController
     */
    @FXML SelectedFoldersController selectedFoldersController;
    @FXML private Button addToSelectedFolders_btn;
    @FXML private Button analyzeList_add;
    @FXML private Button analyzeList_remove;
    @FXML private Button list;
    @FXML private ScrollPane analyzeList_scrollPane;
    @FXML private SplitPane splitPane_drives;
    @FXML private SplitPane splitPane_root;
    @FXML private TreeView<File> drives_treeView;
    @FXML private VBox analyzeList_vbox;

    private Model_main model_main;
    private ModelFolderScanner model_folderScanner = new ModelFolderScanner();

    private Scene folderScannerController_scene;
    private Stage folderScannerController_stage;

    private CheckBoxTreeItem<File> drives_rootItem;

    @FXML
    private void addToSelectedFolders_btn_action(ActionEvent event) {
        sprintf("addToSelectedFolders_btn_action...");
        for (Path path : model_folderScanner.getSelectedDrivesFoldersList_obs()) {
            sprintf("Path is: " + path);
            if (Files.exists(path)) {
                if (!selectedFolderHasValue(this.model_main.getSelectedFolders().getSelectedFolderScanner_obs(),
                        path)) {
                    if (!hasTableSelectedFolderPath(model_main.tables(), path)) {
                        //TODO Check selectedfolder selected. It might not work correctly?
                        this.model_main.getSelectedFolders().getSelectedFolderScanner_obs()
                                .add(new SelectedFolder(true, true, path.toString(),true));
                    }
                }
            }
        }

        for (TreeItem<File> fil : drives_rootItem.getChildren()) {
            if (!fil.getChildren().isEmpty()) {
                for (TreeItem<File> c_fil : fil.getChildren()) {
                    Messages.sprintf("c_fil.getValue(); " + c_fil);
                }
            }
        }
    }

    private boolean hasTableSelectedFolderPath(Tables tables, Path toSearchPath) {
        return checkFolderInfoHasFolder(tables, toSearchPath.toString(), toSearchPath);
    }

    static boolean checkFolderInfoHasFolder(Tables tables, String string, Path toSearchPath) {
        for (FolderInfo folderInfo : tables.getSortIt_table().getItems()) {
            if (folderInfo.getFolderPath().equals(string)) {
                return true;
            }
        }

        for (FolderInfo folderInfo : tables.getSorted_table().getItems()) {
            if (folderInfo.getFolderPath().equals(string)) {
                return true;
            }
        }
        return false;
    }

    private boolean selectedFolderHasValue(ObservableList<SelectedFolder> selectedFolderScanner_list, Path path) {
        for (SelectedFolder sf : selectedFolderScanner_list) {
            if (Paths.get(sf.getFolder()).equals(path)) {
                return true;
            }
        }

        return false;
    }

    @FXML
    private void analyzeList_add_action(ActionEvent event) {

    }

    @FXML
    private void analyzeList_remove_action(ActionEvent event) {
    }

    @FXML
    private void list_action(ActionEvent event) {
    }

    public void exit() {
        model_folderScanner.getScanDrives().stop();
        model_folderScanner.drive().saveList();
        folderScannerController_stage.close();
    }

    final EventHandler<KeyEvent> eventFilter = new EventHandler<KeyEvent>() {

        @Override
        public void handle(KeyEvent event) {
            if (event.getCode().equals(KeyCode.ESCAPE)) {
                exit();
            }
        }
    };

    public void setScene(Scene folderScannerController_scene) {
        this.folderScannerController_scene = folderScannerController_scene;
    }

    public void setStage(Stage folderScannerController_stage) {
        this.folderScannerController_stage = folderScannerController_stage;
        this.folderScannerController_stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                exit();
            }
        });
    }

    public void init(Model_main aModel_main) {
        Main.setProcessCancelled(false);

        this.model_main = aModel_main;
        initNewSingleExecutionService();

        drives_rootItem = new CheckBoxTreeItem<>();
        drives_rootItem.setExpanded(true);
        drives_treeView.setCellFactory(CheckBoxTreeCell.forTreeView());
//        drives_treeView.setCellFactory(treeView -> new TreeCell<>() {
//            @Override
//            public void updateItem(File path, boolean empty) {
//                super.updateItem(path, empty);
//                if (empty) {
//                    setText(null);
//                } else {
//                    setText(path.toPath().getFileName().toString());
//                }
//            }
//        });
        drives_treeView.setRoot(drives_rootItem);
        drives_treeView.setShowRoot(false);

        model_folderScanner.init(model_main, drives_rootItem);
        selectedFoldersController.init(model_main, model_folderScanner);
        folderScannerController_stage.addEventFilter(KeyEvent.KEY_PRESSED, eventFilter);
        selectedFoldersController.start();
    }
}
