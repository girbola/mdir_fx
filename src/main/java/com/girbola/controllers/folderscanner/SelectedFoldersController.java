/*
 @(#)Copyright:  Copyright (c) 2012-2024 All right reserved.
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.folderscanner;

import com.girbola.Main;
import com.girbola.controllers.main.Model_main;
import com.girbola.controllers.main.Populate;
import com.girbola.controllers.main.SQL_Enums;
import com.girbola.filelisting.GetAllMediaFiles;
import com.girbola.filelisting.SubFolders;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.sql.SQL_Utils;
import com.girbola.sql.SqliteConnection;
import common.utils.FileUtils;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import static com.girbola.Main.conf;
import static com.girbola.messages.Messages.sprintf;

public class SelectedFoldersController {
    private final String ERROR = SelectedFoldersController.class.getName();

    /*private ScheduledService<Void> scanner;*/

    private Model_main model_main;
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

        // Load FolderInfo from database
        Connection connection = SqliteConnection.connector(Main.conf.getAppDataPath(),
                Main.conf.getConfiguration_db_fileName());
        if (connection == null) {
            Messages.sprintfError("Could not connect: " + Main.conf.getConfiguration_db_fileName());
        }

        //createFolderInfoDatabase
        SQL_Utils.createSelectedFoldersTable(connection);

        SQL_Utils.insertSelectedFolders_List_ToDB(connection, model_main.getSelectedFolders().getSelectedFolderScanner_obs());

        SQL_Utils.closeConnection(connection);

//		TODO korjaa tämä järkevämmäksi. Osais mm huomioida jo olemassa olevat kansiot.

        model_main.getMonitorExternalDriveConnectivity().cancel();

        model_main.populate().populateTables_FolderScanner_list(Main.scene_Switcher.getWindow());

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
            addNewFolder(folder);
        }
    }

    /**
     * Adds a new folder to the list of selected folders.
     *
     * @param folder The folder to be added.
     */
    private void addNewFolder(File folder) {
        Messages.sprintf("addNewFolder: " + folder);
        if (folder == null) {
            Messages.errorSmth(ERROR, "Something went wrong while loading folders", null, Misc.getLineNumber(), true);
        }
        List<SelectedFolder> existingFolders = model_main.getSelectedFolders().getSelectedFolderScanner_obs();
        if (!SelectedFolderUtils.contains(existingFolders, folder)) {
            if (conf.getWorkDir().contains(folder.toString())) {
                Messages.warningText(Main.bundle.getString("workDirConflict"));
            } else {
                boolean folderExists = SelectedFolderUtils.tableHasFolder(model_main.tables(), folder.toPath());
                if (!folderExists) {
                    SelectedFolder selectedFolder = new SelectedFolder(true, true, folder.toString(), FileUtils.getHasMedia(folder));
                    Messages.sprintf("PATH: " + selectedFolder.getFolder() + " Media is selected? " + selectedFolder.isMedia());
                    existingFolders.add(selectedFolder);
                }
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
        Connection connection = null;

        ObservableList<SelectedFolder> selectedItems = table.getSelectionModel().getSelectedItems();

        SQL_Utils.removeAllData_list(connection, new ArrayList<>(selectedItems), SQL_Enums.SELECTEDFOLDERS.getType());

        table.getItems().removeAll(selectedItems);
        table.getSelectionModel().clearSelection();


        table.getSelectionModel().clearSelection();
    }

    public void init(Model_main aModel_main, ModelFolderScanner aModel_folderScanner) {
        this.model_main = aModel_main;
        this.model_folderScanner = aModel_folderScanner;

        selectedFolder_TableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        model_folderScanner.setDeleteKeyPressed(selectedFolder_TableView);
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
