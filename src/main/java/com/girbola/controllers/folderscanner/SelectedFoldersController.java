/*
 @(#)Copyright:  Copyright (c) 2012-2024 All right reserved.
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.folderscanner;

import com.girbola.Main;
import com.girbola.controllers.main.ModelMain;
import com.girbola.messages.Messages;
import com.girbola.sql.FolderScannerSQL;
import common.utils.FileUtils;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.sql.Connection;
import java.util.Iterator;

import static com.girbola.messages.Messages.sprintf;

public class SelectedFoldersController {
    private final String ERROR = SelectedFoldersController.class.getName();

    /*private ScheduledService<Void> scanner;*/

    private ModelMain model_main;
    private ModelFolderScanner model_folderScanner;

    //@formatter:off
    @FXML private TableColumn<SelectedFolder, Boolean> folder_selected_col;
    @FXML private TableColumn<SelectedFolder, String> folder_col;
    @FXML private TableColumn<SelectedFolder, Boolean> folder_connected_col;
    @FXML private TableColumn<SelectedFolder, Boolean> hasMedia_col;

    @FXML private Button selectedFolders_ok;
    @FXML private Button selectedFolders_remove;
    @FXML private Button selectedFolders_select_folder;
    @FXML private TableView<SelectedFolder> selectedFolder_TableView;
    //@formatter:on

    Callback<TableColumn<SelectedFolder, Boolean>, TableCell<SelectedFolder, Boolean>> selectedFoldersCellFactory = p -> new CheckBoxSelectFolderTableCell(model_main, model_folderScanner);

    @FXML
    private void selectedFolders_ok_action(ActionEvent event) {
        Messages.sprintf("selectedFolders_ok_action pressed");
        model_folderScanner.getScanDrives().stop();
        model_main.getMonitorExternalDriveConnectivity().cancel();

        com.girbola.sql.FolderScannerSQL.saveSelectedFolder(model_main);

        model_main.getMonitorExternalDriveConnectivity().cancel();

        model_main.populate().populateTablesFolderScannerList(Main.scene_Switcher.getWindow());

        Stage stage = (Stage) selectedFolders_ok.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void selectedFolders_remove_action(ActionEvent event) {
        sprintf("selectedFolders_remove_action  pressed");
        removeFromTable(selectedFolder_TableView);
    }

    @FXML
    private void selectedFolders_select_folder_action(ActionEvent event) {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setInitialDirectory(new File(System.getProperty("user.home")));
        File folder = dc.showDialog(selectedFolders_select_folder.getScene().getWindow());
        if (folder != null) {
            if(Main.conf.getWorkDir().contains(folder.toString())) {
                Messages.warningText(Main.bundle.getString("workDirConflict"));
            } else {
                model_main.getSelectedFolders().add(new SelectedFolder(true, true,folder.getAbsolutePath(), FileUtils.getHasMedia(folder)));
            }
        }
    }

    public void setDeleteKeyPressed() {
        selectedFolder_TableView.setOnKeyPressed((KeyEvent event) -> {
            if (event.getCode() == (KeyCode.DELETE)) {
                removeFromTable(selectedFolder_TableView);
            }
        });
    }

    private void removeFromTable(TableView<SelectedFolder> table) {
        Messages.sprintf("RemoveFromtable started");

        Iterator<SelectedFolder> selectedFolderScannerObs = model_main.getSelectedFolders().getSelectedFolderScanner_obs().iterator();
        while(selectedFolderScannerObs.hasNext()) {
            SelectedFolder selectedFolder = selectedFolderScannerObs.next();
        }
        Connection connection = null;

        ObservableList<SelectedFolder> selectedItems = table.getSelectionModel().getSelectedItems();

        for (SelectedFolder selectedItem : selectedItems) {

        }

        FolderScannerSQL.removeSelectedFolders(model_main);

        table.getItems().removeAll(selectedItems);
        table.getSelectionModel().clearSelection();

        table.getSelectionModel().clearSelection();
    }

    public void init(ModelMain aModel_main, ModelFolderScanner aModel_folderScanner) {
        this.model_main = aModel_main;
        this.model_folderScanner = aModel_folderScanner;

        selectedFolder_TableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        //model_folderScanner.setDeleteKeyPressed(selectedFolder_TableView);
        selectedFolder_TableView.setOnKeyPressed((KeyEvent event) -> {
            if (event.getCode() == (KeyCode.DELETE)) {
                removeFromTable(selectedFolder_TableView);
            }
        });

        folder_selected_col.setCellFactory(selectedFoldersCellFactory);
        folder_selected_col.setCellValueFactory((TableColumn.CellDataFeatures<SelectedFolder, Boolean> cellData) -> new SimpleObjectProperty<>(cellData.getValue().isSelected()));

        folder_col.setCellValueFactory((TableColumn.CellDataFeatures<SelectedFolder, String> cellData) -> new SimpleObjectProperty<>(cellData.getValue().getFolder()));

        folder_connected_col.setCellValueFactory((TableColumn.CellDataFeatures<SelectedFolder, Boolean> cellData) -> new SimpleObjectProperty<>(cellData.getValue().isConnected()));

        hasMedia_col.setCellValueFactory((TableColumn.CellDataFeatures<SelectedFolder, Boolean> cellData) -> new SimpleObjectProperty<>(cellData.getValue().isMedia()));

        selectedFolder_TableView.setItems(this.model_main.getSelectedFolders().getSelectedFolderScanner_obs());
        Messages.sprintf("getFolderScanner lldlflfl" + this.model_main.getSelectedFolders().getSelectedFolderScanner_obs());

      /*  scanner = new ScheduledService<Void>() {

            @Override
            protected Task createTask() {
                return new Task<Integer>() {
                    @Override
                    protected Integer call() throws Exception {
                        for (SelectedFolder sf : model_main.getSelectedFolders().getSelectedFolderScanner_obs()) {
                            sf.setConnected(Files.exists(Paths.get(sf.getFolder())));
                            Messages.sprintf("SelectedFolder: " + sf.getFolder() + " isConnected?: " + sf.isConnected());
                        }
                        return null;
                    }
                };
            }
        };

        scanner.setPeriod(Duration.seconds(10));*/
    }

    public void start() {
//        this.scanner.start();
    }

    public void restart() {
//        this.scanner.restart();
    }

    public void stop() {
//        this.scanner.cancel();
    }

}
