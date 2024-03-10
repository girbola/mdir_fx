/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.folderscanner;

import com.girbola.Main;
import com.girbola.controllers.main.Model_main;
import com.girbola.controllers.main.SQL_Enums;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.sql.SQL_Utils;
import com.girbola.sql.SqliteConnection;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;

import static com.girbola.Main.conf;
import static com.girbola.messages.Messages.sprintf;

public class SelectedFoldersController {
    private final String ERROR = SelectedFoldersController.class.getName();

    private ScheduledService<Void> scanner;

    private ObservableList<SelectedFolder> selectedFolder = FXCollections.observableArrayList();
    private Model_main model_main;
    private Model_folderScanner model_folderScanner;

    @FXML
    private TableColumn<SelectedFolder, String> folder_col;
    @FXML
    private TableColumn<SelectedFolder, Boolean> folder_connected_col;
    @FXML
    private Button selectedFolders_ok;
    @FXML
    private Button selectedFolders_remove;
    @FXML
    private Button selectedFolders_select_folder;
    @FXML
    private TableView<SelectedFolder> selectedFolder_TableView;

    @FXML
    private void selectedFolders_ok_action(ActionEvent event) {
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

        if (connection != null) {
            try {
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
                Messages.errorSmth(ERROR, "Something went wrong with selecting folders", e, Misc.getLineNumber(), true);
            }
        }
//		TODO korjaa tämä järkevämmäksi. Osais mm huomioida jo olemassa olevat kansiot.

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
        File folder = null;
        DirectoryChooser dc = new DirectoryChooser();
        dc.setInitialDirectory(new File(System.getProperty("user.home")));
        folder = dc.showDialog(selectedFolders_select_folder.getScene().getWindow());
        if (folder != null) {
            if (!model_main.getSelectedFolders().getSelectedFolderScanner_obs().contains(folder.toPath())) {
                if (folder.toString().contains(conf.getWorkDir())) {
                    Messages.warningText("Cannot be same folder with com.girbola.workdir!");
                } else {
                    model_main.getSelectedFolders().getSelectedFolderScanner_obs()
                            .add(new SelectedFolder(true, folder.toString()));
                }
            } else {
                sprintf("Won't be added because it already exists! " + folder.toPath());
            }
            // model.getSelection_FolderScanner().getSelectedFolderScanner_list().add(folder.toPath());
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
        ArrayList<SelectedFolder> listToRemove = new ArrayList<>();
        Connection connection = SqliteConnection.connector(Main.conf.getAppDataPath(),
                Main.conf.getConfiguration_db_fileName());

        ObservableList<SelectedFolder> rows = table.getSelectionModel().getSelectedItems();
        for (SelectedFolder rm : rows) {
            listToRemove.add(rm);
        }
        boolean removed = SQL_Utils.removeAllData_list(connection, listToRemove, SQL_Enums.SELECTEDFOLDERS.getType());
        if (removed) {
            Messages.sprintf("Table data removed: ");
        } else {
            Messages.sprintf("Table data not removed ");
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception e) {
            }
        }
        for (SelectedFolder r : listToRemove) {
            sprintf("sorted value remove: " + r);
            table.getItems().remove(r);
        }
        listToRemove.clear();
        table.getSelectionModel().clearSelection();
    }

    public void init(Model_main aModel_main, Model_folderScanner aModel_folderScanner) {
        this.model_main = aModel_main;
        this.model_folderScanner = aModel_folderScanner;

        selectedFolder_TableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        model_folderScanner.setDeleteKeyPressed(selectedFolder_TableView);
        folder_col.setCellValueFactory(
                (TableColumn.CellDataFeatures<SelectedFolder, String> cellData) -> new SimpleObjectProperty<>(
                        cellData.getValue().getFolder()));
        folder_connected_col.setCellValueFactory(
                (TableColumn.CellDataFeatures<SelectedFolder, Boolean> cellData) -> new SimpleObjectProperty<>(
                        cellData.getValue().isConnected()));

        selectedFolder_TableView.setItems(this.model_main.getSelectedFolders().getSelectedFolderScanner_obs());
        Messages.sprintf(
                "getFolderScanner lldlflfl" + this.model_main.getSelectedFolders().getSelectedFolderScanner_obs());
        scanner = new ScheduledService<Void>() {

            @Override
            protected Task createTask() {
                return new Task<Integer>() {
                    @Override
                    protected Integer call() throws Exception {
                        for (SelectedFolder sf : model_main.getSelectedFolders().getSelectedFolderScanner_obs()) {
                            sf.setConnected(Files.exists(Paths.get(sf.getFolder())));
                            Messages.sprintf("sggg: " + sf.getFolder() + " isConnected?: " + sf.isConnected());
                        }
                        return null;
                    }
                };
            }
        };

        scanner.setPeriod(Duration.seconds(10));
    }

    public void start() {
        this.scanner.start();
    }

    public void restart() {
        this.scanner.restart();
    }

    public void stop() {
        this.scanner.cancel();
    }

}
