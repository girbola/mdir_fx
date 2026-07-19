
package com.girbola.controllers.main;


import com.girbola.LoadFileInfosBackToTableViews;
import com.girbola.Main;
import com.girbola.concurrency.ConcurrencyUtils;
import com.girbola.controllers.folderscanner.FolderScannerController;
import com.girbola.controllers.folderscanner.folderpicker.FolderSelectionService;
import com.girbola.controllers.main.selectedfolder.SelectedFolderScanner;
import com.girbola.controllers.main.sql.ConfigurationSQLHandler;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.controllers.main.tables.model.FolderInfoStatus;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.dialogs.Dialogs;
import com.girbola.drive.DriveInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.persistence.fileinfo.FileInfoDao;
import com.girbola.persistence.folderinfo.FolderInfoDao;
import com.girbola.sql.SQL_Utils;
import com.girbola.persistence.configuration.ConfigurationSavedFoldersDao;
import com.girbola.persistence.selectedfolderinfo.SelectedFolderInfoDao;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.event.EventHandler;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import static com.girbola.Main.bundle;
import static com.girbola.Main.conf;
import static com.girbola.messages.Messages.sprintf;


public class ModelMain {

    private final String ERROR = ModelMain.class.getSimpleName();

    private AnchorPane main_container;
    private BottomController bottomController;
    private FolderScannerController folderScannerController;
    private Buttons buttons;
    private Populate populate;
    private ScheduledService<Void> monitorExternalDriveConnectivity;
    private SelectedFolderScanner selectedFolders;
    private FolderSelectionService folderSelectionService;
    private StringProperty table_root_hbox_width = new SimpleStringProperty();
    private TablePositionHolder tablePositionHolder;
    private TabPane tabPaneMain;
    private Tables tables;
    private VBox main_vbox;
//    private WorkDirSQL workDirSQL; //TODO Move this to SQLHandler when it is ready
//    private SQLConfigurationHandler sqlConfigurationHandler;

    private List<DriveInfo> driveInfos = new ArrayList<>();

    public FolderSelectionService getFolderSelectionService() {
        return folderSelectionService;
    }

//    public void setFolderSelectionService(FolderSelectionService folderSelectionService) {
//        this.folderSelectionService = folderSelectionService;
//    }

    public ModelMain() {
        sprintf("Model instantiated...");
        if (Main.conf.getWorkDir().trim().isEmpty()) {
            Messages.sprintfError("workdir were empty");
        }
        if (conf == null) {
            Messages.sprintfError("conf were null!!!!!!!!!!!!!!!!!!");
        }
//        workDirSQL = new WorkDirSQL();
//        sqlConfigurationHandler = new SQLConfigurationHandler(conf);
        buttons = new Buttons(this);
        monitorExternalDriveConnectivity = new MonitorExternalDriveConnectivity(this);
        monitorExternalDriveConnectivity.setPeriod(Duration.seconds(15));
        populate = new Populate(this);
        selectedFolders = new SelectedFolderScanner();
        tablePositionHolder = new TablePositionHolder(this);
        tables = new Tables(this);
        folderSelectionService = new FolderSelectionService(this);
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

    public TabPane getTabPaneMain() { return tabPaneMain; }

    public void setTabPaneMain(TabPane tabPaneMain) { this.tabPaneMain = tabPaneMain;}

    public boolean saveAllTableContents() {
        Messages.sprintf("saveAllTableContents started");
        Connection connection = null;
        boolean success = true;

        try {
            connection = ConfigurationSQLHandler.getConnection();
            if (!SQL_Utils.isDbConnected(connection)) {
                Messages.warningText(Main.bundle.getString("cannotConnectConfigurationTable"));
                return false;
            }

            SQL_Utils.setAutoCommit(connection, false);

            // Create the necessary tables first
            SelectedFolderInfoDao.createSelectedFoldersDBTable(connection);

            // Save each table's content and track success
            boolean sorted = saveTableContent(connection, tables().getSorted_table().getItems(), TableType.SORTED.getType());
            if (sorted) {
                Messages.sprintf("sorted were saved successfully");
            } else {
                Messages.sprintf("Failed to save sorted table");
                success = false;
            }

            boolean sortit = saveTableContent(connection, tables().getSortIt_table().getItems(), TableType.SORTIT.getType());
            if (sortit) {
                Messages.sprintf("sortit were saved successfully");
            } else {
                Messages.sprintf("Failed to save sortit table");
                success = false;
            }

            boolean asitis = saveTableContent(connection, tables().getAsItIs_table().getItems(), TableType.ASITIS.getType());
            if (asitis) {
                Messages.sprintf("asitis were saved successfully");
            } else {
                Messages.sprintf("Failed to save asitis table");
                success = false;
            }

            // Commit only if all operations were successful
            if (success) {
                SQL_Utils.commitChanges(connection);
                Main.setChanged(false);
            } else {
                SQL_Utils.rollBackConnection(connection);
            }

            return success;
        } catch (Exception e) {
            Messages.sprintfError("Error in saveAllTableContents: " + e.getMessage());
            if (connection != null) {
                SQL_Utils.rollBackConnection(connection);
            }
            return false;
        } finally {
            SQL_Utils.closeConnection(connection);
        }
    }

    /**
     * Saves the content of the provided folder information list into the database based on the specified table type.
     * This method handles saving each folder's information, inserts associated file information into the database,
     * and commits the changes to the database.
     *
     * @param connectionConfiguration the database connection configuration to be used for saving the data
     * @param items                   the list of {@code FolderInfo} objects representing folder details to be saved
     * @param tableType               the type of table where the data should be saved
     * @return {@code true} if the data was saved and committed successfully, {@code false} otherwise
     */
    public synchronized boolean saveTableContent(Connection connectionConfiguration, ObservableList<FolderInfo> items, String tableType) {
        Messages.sprintf("saveTableContent started: " + tableType + " items: " + items.size() + " connectionConfiguration: " + connectionConfiguration);
        if (items.isEmpty()) {
            Messages.sprintf("saveTableContent items list were empty. tabletype: " + tableType);
            return false;
        }

        FileInfoDao fileInfoDao = new FileInfoDao();

        for (FolderInfo folderInfo : items) {
            Messages.sprintf("Saving folderInfo at: " + folderInfo.getFolderPath() + " folder size: " + folderInfo.getFileInfoList().size());
        }

//        Connection configurationConnection = ConfigurationSQLHandler.getConnection();
//        configurationConnection.folder

        for (FolderInfo folderInfo : items) {
            Messages.sprintf("Saving folderInfo at: " + folderInfo.getFolderPath() + " folder size: " + folderInfo.getFileInfoList().size());
            if (!folderInfo.getFileInfoList().isEmpty()) {
                Messages.sprintf("Saving table content: " + folderInfo.getFolderPath());
                folderInfo.setTableType(tableType);
                try {

                    FolderInfoStatus folderInfoStatus = new FolderInfoStatus(folderInfo.getFolderPath(), tableType, folderInfo.getJustFolderName(), folderInfo.isConnected());

                    Messages.sprintf("Saving folderInfo: " + folderInfoStatus.getFolderPath() + " tableType: " + folderInfoStatus.getTableType() + " justFolderName: " + folderInfoStatus.getJustFolderName() + " connected: " + folderInfoStatus.isConnected() + " size: " + folderInfo.getFileInfoList().size());

                    boolean addingToFolderInfos = ConfigurationSavedFoldersDao.insertSavedFoldersIntoConfigurationDatabase(connectionConfiguration, folderInfoStatus);  // Saving folderinfo current state to to configure database
                    if (!addingToFolderInfos) {
                        Platform.runLater(() -> {
                            Messages.sprintfError("Something went wrong when saving folderinfo into configuration file: " + folderInfo.getFolderPath());
                        });
                    }
                    SQL_Utils.commitChanges(connectionConfiguration);

                    /*
                     * Adds FolderInfo into table folderInfo.db. Stores: FolderPath, TableType and
                     * Connection status when this was saved Connects to current folder for existing
                     * or creates new one called fileinfo.db
                     */

                    FolderInfoDao.saveConfigurationFolderInfoStateToDatabase(connectionConfiguration, folderInfo, true);

                    // Inserts all data info fileinfo.db
                    FolderInfoDao.insertFileInfoListToFileInfoDatabase(folderInfo, false);
//                    SQL_Utils.commitChanges(connectionConfiguration);

                } catch (Exception e) {
                    Messages.sprintfError("Something went wrong with writing folderinfo into database at line: "
                            + Misc.getLineNumber() + " folderInfo path was: " + folderInfo.getFolderPath() + " exception: " + e.getMessage());
                    return false;
                }
            } else {
                Messages.sprintf("No stuff to print");
            }
        }
        if (SQL_Utils.isDbConnected(connectionConfiguration)) {
            SQL_Utils.closeConnection(connectionConfiguration);
        }
//        SQL_Utils.closeConnection(connectionConfiguration);
        return true;
    }

    public void exitProgram_NOSAVE() {
        ConcurrencyUtils.stopExecThreadNow();
        Platform.exit();
    }

    public EventHandler<WindowEvent> exitProgram = event -> {
        Messages.sprintf("exitProgram");
        exitProgram();
        Messages.sprintf("exitProgram ended");
    };

    public EventHandler<WindowEvent> dontExitWindow = event -> event.consume();

    public void exitProgram() {
        sprintf("exitProgram()");
        // TODO laita savetus daemon päälle jottei tallennukset keskeytyisi
        // Platform.setImplicitExit(false)
        // Misc_GUI.saveObject(TablePositionHolder, folder);
        // saveTablePositions();
        // erg;
        Messages.sprintf("Exiting program");
        ConfigurationSQLHandler.updateConfiguration();
//        getSqlConfigurationHandler().getConfigurationSQLHandler().updateConfiguration();

        if (Main.getChanged()) {
            Dialog<ButtonType> dialog = Dialogs.createDialog_YesNoCancel(Main.sceneManager.getWindow(),
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

    public void load() {
        Connection connection = ConfigurationSQLHandler.getConnection();
//        Connection connection = getSqlConfigurationHandler().getConfigurationSQLHandler().getConnection();
        if (SQL_Utils.isDbConnected(connection)) {
            TableUtils.clearTablesContents(tables());
            LoadFileInfosBackToTableViews loadFileInfosBackToTableViews = new LoadFileInfosBackToTableViews(this, connection);
            loadFileInfosBackToTableViews.setOnSucceeded(event -> {
                Messages.sprintf("LoadFileInfosBackToTableViews succeeded");
                SQL_Utils.closeConnection(connection);
            });
            loadFileInfosBackToTableViews.setOnFailed(event -> {
                Messages.sprintf("LoadFileInfosBackToTableViews failed");
                SQL_Utils.rollBackConnection(connection);
            });

            loadFileInfosBackToTableViews.setOnCancelled(event -> {
                Messages.sprintf("LoadFileInfosBackToTableViews cancelled");
                SQL_Utils.rollBackConnection(connection);
            });

            loadFileInfosBackToTableViews.start();
            /*Thread loadFileInfosBackToTableViewsThread = new Thread(load_FileInfosBackToTableViews, "Loading folderinfos Thread");
            loadFileInfosBackToTableViewsThread.start();*/
        } else {
            Messages.sprintf("Can't load folderinfos back to tables because the database were not connected");
        }
    }

//    public SQLConfigurationHandler getSqlConfigurationHandler() {return this.sqlConfigurationHandler;}
//
//    public WorkDirSQL getWorkDirSQL() {return this.workDirSQL;}

    public ScheduledService<Void> getMonitorExternalDriveConnectivity() {
        return monitorExternalDriveConnectivity;
    }

    public void setBottomController(BottomController bottomController) {
        this.bottomController = bottomController;
    }

    public BottomController getBottomController() {
        return this.bottomController;
    }

    public List<DriveInfo> driveInfos() {
        return this.driveInfos;
    }

    public void setFolderScannerController(FolderScannerController folderScannerController) {
        this.folderScannerController = folderScannerController;
    }

    public FolderScannerController getFolderScannerController() {
        return this.folderScannerController;
    }
}
