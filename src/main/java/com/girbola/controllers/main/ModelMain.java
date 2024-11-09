/*
 @(#)Copyright:  Copyright (c) 2012-2024 All right reserved.
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.main;


import com.girbola.Load_FileInfosBackToTableViews;
import com.girbola.Main;
import com.girbola.concurrency.ConcurrencyUtils;
import com.girbola.configuration.Configuration_SQL_Utils;
import com.girbola.controllers.main.selectedfolder.SelectedFolderScanner;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.dialogs.Dialogs;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.sql.*;
import com.girbola.workdir.WorkDirHandler;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.event.EventHandler;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.nio.file.Paths;
import java.sql.Connection;
import java.util.Iterator;
import java.util.Optional;

import static com.girbola.Main.bundle;
import static com.girbola.Main.conf;
import static com.girbola.messages.Messages.sprintf;

/**
 * @author Marko Lokka
 */
public class ModelMain {

    private AnchorPane main_container;
    private BottomController bottomController;
    private Buttons buttons;
    private Populate populate;
    private ScheduledService<Void> monitorExternalDriveConnectivity;
    private SelectedFolderScanner selectedFolders;
    private StringProperty table_root_hbox_width = new SimpleStringProperty();
    private TablePositionHolder tablePositionHolder;
    private Tables tables;
    private VBox main_vbox;
    private WorkDirHandler workDirHandler = new WorkDirHandler();

    private final String ERROR = ModelMain.class.getSimpleName();

    public ModelMain() {
        sprintf("Model instantiated...");

        buttons = new Buttons(this);
        monitorExternalDriveConnectivity = new MonitorExternalDriveConnectivity(this);
        monitorExternalDriveConnectivity.setPeriod(Duration.seconds(15));
        populate = new Populate(this);
        selectedFolders = new SelectedFolderScanner();
        tablePositionHolder = new TablePositionHolder(this);
        tables = new Tables(this);

        tables.init();

    }

    public StringProperty getTable_root_hbox_width() {
        return table_root_hbox_width;
    }

    public SelectedFolderScanner getSelectedFolders() {
        return selectedFolders;
    }

    public Buttons buttons() {
        return this.buttons;
    }

    void setMainContainer(AnchorPane main_container) {
        this.main_container = main_container;
    }

    void setMainVBox(VBox main_vbox) {
        this.main_vbox = main_vbox;
    }

    public Tables tables() {
        return this.tables;
    }

    public Populate populate() {
        return this.populate;
    }

    public boolean saveAllTableContents() {
        Messages.sprintf("save started");

        Connection connection = SqliteConnection.connector(Main.conf.getAppDataPath(),
                Main.conf.getConfiguration_db_fileName()); // folderInfo.db
        if (!SQL_Utils.isDbConnected(connection)) {
            Messages.warningText(bundle.getString("cannotOpenConnectionToDatabase") + Main.conf.getAppDataPath() + " fileName: " + Main.conf.getConfiguration_db_fileName());
        }
        SQL_Utils.setAutoCommit(connection, false);

        SQL_Utils.clearTable(connection, SQL_Enums.FOLDERINFOS.getType()); // clear table folderInfo.db
        SQL_Utils.createFolderInfosDatabase(connection); // create new folderinfodatabase folderInfo.db

        boolean sorted = saveTableContent(connection, tables().getSorted_table().getItems(),
                TableType.SORTED.getType());
        if (sorted) {
            Messages.sprintf("sorted were saved successfully");
        }
        boolean sortit = saveTableContent(connection, tables().getSortIt_table().getItems(),
                TableType.SORTIT.getType());
        if (sortit) {
            Messages.sprintf("sortit were saved successfully");
        }
        boolean asitis = saveTableContent(connection, tables().getAsItIs_table().getItems(),
                TableType.ASITIS.getType());
        if (asitis) {
            Messages.sprintf("asitis were saved successfully");
        }

        SQL_Utils.commitChanges(connection);
        boolean closeConnection = SQL_Utils.closeConnection(connection);
        if (!closeConnection) {
            return false;
        }


        Main.setChanged(true);
        return true;
    }

    public boolean saveTableContent(Connection connection_Configuration, ObservableList<FolderInfo> items,
                                    String tableType) {
        if (items.isEmpty()) {
            Messages.sprintfError("saveTableContent items list were empty. tabletype: " + tableType);
            return false;
        }

        Connection fileList_connection = null;
        for (FolderInfo folderInfo : items) {
            if (folderInfo.getFolderFiles() >= 1) {
                Messages.sprintf("saveTableContent folderInfo: " + folderInfo.getFolderPath());
                folderInfo.setTableType(tableType);
                try {
                    FolderInfos folderInfos = new FolderInfos(folderInfo.getFolderPath(), tableType,
                            folderInfo.getJustFolderName(), folderInfo.isConnected());

                    boolean addingToFolderInfos = SQL_Utils.addToFolderInfosDB(connection_Configuration, folderInfos);
                    if (!addingToFolderInfos) {
                        Messages.sprintfError("Something went wrong with adding folderinfo configuration file");
                    }
                    /*
                     * Adds FolderInfo into table folderInfo.db. Stores: FolderPath, TableType and
                     * Connection status when this was saved Connects to current folder for existing
                     * or creates new one called fileinfo.db
                     */
                    fileList_connection = SqliteConnection.connector(Paths.get(folderInfo.getFolderPath()),
                            Main.conf.getMdir_db_fileName());
                    fileList_connection.setAutoCommit(false);
                    // Inserts all data info fileinfo.db
                    FileInfo_SQL.insertFileInfoListToDatabase(fileList_connection, folderInfo.getFileInfoList(), false);
                    SQL_Utils.commitChanges(fileList_connection);
                    FolderInfo_SQL.saveFolderInfoToTable(fileList_connection, folderInfo);
                    SQL_Utils.commitChanges(fileList_connection);
                    SQL_Utils.closeConnection(fileList_connection);

                } catch (Exception e) {
                    e.printStackTrace();
                    Messages.sprintfError("Something went wrong writing folderinfo to database at line: "
                            + Misc.getLineNumber() + " folderInfo path was: " + folderInfo.getFolderPath());
                    return false;
                }
            }
        }

        try {
            SQL_Utils.commitChanges(connection_Configuration);
            return true;
        } catch (Exception e) {
            Messages.sprintfError("Cannot commit to SQL database");
            Messages.errorSmth(ERROR, "Cannot commit to SQL database", e, Misc.getLineNumber(), true);
            return false;
        }
    }

    public void exitProgram_NOSAVE() {
        ConcurrencyUtils.stopExecThreadNow();
        Platform.exit();
    }

    public EventHandler<WindowEvent> exitProgram = new EventHandler<WindowEvent>() {
        @Override
        public void handle(WindowEvent event) {
            Messages.sprintf("exitProgram");
            exitProgram();
            Messages.sprintf("exitProgram ended");
        }
    };
    // model_main.getSelection_FolderScanner().save_Selection_FolderScannerDataTo_File(model_main.getSelection_FolderScanner().getSelectedFolderScanner_list());

    public EventHandler<WindowEvent> dontExit = new EventHandler<WindowEvent>() {
        @Override
        public void handle(WindowEvent event) {
            event.consume();
        }

    };

    public void exitProgram() {
        sprintf("exitProgram()");
        // TODO laita savetus daemon päälle jottei tallennukset keskeytyisi
        // Platform.setImplicitExit(false)
        // Misc_GUI.saveObject(TablePositionHolder, folder);
        // saveTablePositions();
        // erg;
        Messages.sprintf("Exiting program");
        Configuration_SQL_Utils.update_Configuration();

        if (Main.getChanged()) {
            Dialog<ButtonType> dialog = Dialogs.createDialog_YesNoCancel(Main.scene_Switcher.getWindow(),
                    bundle.getString("saveBeforeExit"));
            Messages.sprintf("dialog changesDialog width: " + dialog.getWidth());
            Iterator<ButtonType> iterator = dialog.getDialogPane().getButtonTypes().iterator();
            while (iterator.hasNext()) {
                ButtonType btn = iterator.next();
                Messages.sprintf("BTNNTTNNT: " + btn.getText());
            }
            Optional<ButtonType> result = dialog.showAndWait();
            if (result.get().getButtonData().equals(ButtonBar.ButtonData.YES)) {
                saveAllTableContents();
                getMonitorExternalDriveConnectivity().cancel();
            } else if (result.get().getButtonData().equals(ButtonBar.ButtonData.NO)) {
                Messages.sprintf("No answered. This is not finished!");
            } else if (result.get().getButtonData().equals(ButtonBar.ButtonData.CANCEL_CLOSE)) {
                Messages.sprintf("Cancel pressed. This is not finished!");
                return;
            }
        }
        if (conf.isConfirmOnExit()) {
            sprintf("isShowOnExit was true");
            ConcurrencyUtils.stopExecThreadNow();
            // save();
            getMonitorExternalDriveConnectivity().cancel();
            Platform.exit();
        } else {
            sprintf("isShowOnExit was false");
            ConcurrencyUtils.stopExecThreadNow();
            // save();
            getMonitorExternalDriveConnectivity().cancel();
            Platform.exit();
        }

    }

    /**
     * @return the workDir_Handler
     */
    public WorkDirHandler getWorkDir_Handler() {
        return this.workDirHandler;
    }

    public ScheduledService<Void> getMonitorExternalDriveConnectivity() {
        return monitorExternalDriveConnectivity;
    }

    public void setBottomController(BottomController bottomController) {
        this.bottomController = bottomController;
    }

    public BottomController getBottomController() {
        return this.bottomController;
    }

    public void load() {
        Connection connection = SqliteConnection.connector(Main.conf.getAppDataPath(),
                Main.conf.getConfiguration_db_fileName());
        if (SQL_Utils.isDbConnected(connection)) {
            TableUtils.clearTablesContents(tables());
            Load_FileInfosBackToTableViews load_FileInfosBackToTableViews = new com.girbola.Load_FileInfosBackToTableViews(
                    this, connection);

            Thread load_thread = new Thread(load_FileInfosBackToTableViews, "Loading folderinfos Thread");
            load_thread.start();
        } else {
            Messages.sprintf("Can't load folderinfos back to tables because the database were not connected");
        }
    }
}
