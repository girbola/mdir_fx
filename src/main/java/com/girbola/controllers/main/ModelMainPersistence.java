package com.girbola.controllers.main;

import com.girbola.app.ApplicationContext;
import com.girbola.controllers.main.loadfileinfostableviews.LoadFileInfosBackToTableViews;
import com.girbola.controllers.main.sql.ConfigurationSQLHandler;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.controllers.main.tables.model.SavedFolderInfos;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.persistence.configuration.ConfigurationSavedFoldersDao;
import com.girbola.persistence.folderinfo.FolderInfoDao;
import com.girbola.persistence.selectedfolderinfo.SelectedFolderInfoDao;
import com.girbola.sql.SQL_Utils;
import java.sql.Connection;
import javafx.application.Platform;
import javafx.collections.ObservableList;

final class ModelMainPersistence {

    private final ApplicationContext applicationContext;
    private final Tables tables;

    ModelMainPersistence(ApplicationContext applicationContext, Tables tables) {
        this.applicationContext = applicationContext;
        this.tables = tables;
    }

    boolean saveAllTableContents() {
        Messages.sprintf("saveAllTableContents started");
        Connection connection = null;
        boolean success = true;

        try {
            connection = ConfigurationSQLHandler.getConnection();
            if (!SQL_Utils.isDbConnected(connection)) {
                Messages.warningText(applicationContext.getBundle().getString("cannotConnectConfigurationTable"));
                return false;
            }

            SQL_Utils.setAutoCommit(connection, false);
            SelectedFolderInfoDao.createSelectedFoldersDBTable(connection);

            success = saveTableAndLog(connection, tables.getSorted_table().getItems(), TableType.SORTED, "sorted");
            success &= saveTableAndLog(connection, tables.getSortIt_table().getItems(), TableType.SORTIT, "sortit");
            success &= saveTableAndLog(connection, tables.getAsItIs_table().getItems(), TableType.ASITIS, "asitis");

            if (success) {
                SQL_Utils.commitChanges(connection);
                applicationContext.setChanged(false);
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

    synchronized boolean saveTableContent(
            Connection connectionConfiguration, ObservableList<FolderInfo> items, String tableType) {
        Messages.sprintf("saveTableContent started: " + tableType + " items: " + items.size()
                + " connectionConfiguration: " + connectionConfiguration);
        if (items.isEmpty()) {
            Messages.sprintf("saveTableContent items list were empty. tabletype: " + tableType);
            return false;
        }

        for (FolderInfo folderInfo : items) {
            Messages.sprintf("Saving folderInfo at: " + folderInfo.getFolderPath()
                    + " folder size: " + folderInfo.getFileInfoList().size());
        }

        for (FolderInfo folderInfo : items) {
            Messages.sprintf("Saving folderInfo at: " + folderInfo.getFolderPath()
                    + " folder size: " + folderInfo.getFileInfoList().size());
            if (!folderInfo.getFileInfoList().isEmpty()) {
                Messages.sprintf("Saving table content: " + folderInfo.getFolderPath());
                folderInfo.setTableType(tableType);
                try {
                    SavedFolderInfos savedFolderInfos = SavedFolderInfos.capture(
                            folderInfo.getFolderPath(), tableType, folderInfo.getJustFolderName(),
                            folderInfo.isConnected());

                    Messages.sprintf("Saving folderInfo: " + savedFolderInfos.getFolderPath()
                            + " tableType: " + savedFolderInfos.getTableType()
                            + " justFolderName: " + savedFolderInfos.getJustFolderName()
                            + " connected: " + savedFolderInfos.isConnected()
                            + " size: " + folderInfo.getFileInfoList().size());

                    boolean added = ConfigurationSavedFoldersDao.insertSavedFoldersIntoConfigurationDatabase(
                            connectionConfiguration, savedFolderInfos);
                    if (!added) {
                        Platform.runLater(() -> Messages.sprintfError(
                                "Something went wrong when saving folderinfo into configuration file: "
                                        + folderInfo.getFolderPath()));
                    }
                    SQL_Utils.commitChanges(connectionConfiguration);
                    FolderInfoDao.saveConfigurationFolderInfoStateToDatabase(
                            connectionConfiguration, folderInfo, true);
                    FolderInfoDao.insertFileInfoListToFileInfoDatabase(folderInfo, false);
                } catch (Exception e) {
                    Messages.sprintfError("Something went wrong with writing folderinfo into database at line: "
                            + Misc.getLineNumber() + " folderInfo path was: " + folderInfo.getFolderPath()
                            + " exception: " + e.getMessage());
                    return false;
                }
            } else {
                Messages.sprintf("No stuff to print");
            }
        }
        if (SQL_Utils.isDbConnected(connectionConfiguration)) {
            SQL_Utils.closeConnection(connectionConfiguration);
        }
        return true;
    }

    private boolean saveTableAndLog(Connection connection, ObservableList<FolderInfo> items,
                                    TableType tableType, String label) {
        boolean saved = saveTableContent(connection, items, tableType.getType());
        Messages.sprintf(saved ? label + " were saved successfully" : "Failed to save " + label + " table");
        return saved;
    }

    void load(ModelMain modelMain) {
        Connection connection = ConfigurationSQLHandler.getConnection();
        if (SQL_Utils.isDbConnected(connection)) {
            TableUtils.clearTablesContents(tables);
            LoadFileInfosBackToTableViews loader = new LoadFileInfosBackToTableViews(modelMain, connection);
            loader.setOnSucceeded(event -> {
                Messages.sprintf("LoadFileInfosBackToTableViews succeeded");
                SQL_Utils.closeConnection(connection);
            });
            loader.setOnFailed(event -> {
                Messages.sprintf("LoadFileInfosBackToTableViews failed");
                SQL_Utils.rollBackConnection(connection);
            });
            loader.setOnCancelled(event -> {
                Messages.sprintf("LoadFileInfosBackToTableViews cancelled");
                SQL_Utils.rollBackConnection(connection);
            });
            loader.start();
        } else {
            Messages.sprintf("Can't load folderinfos back to tables because the database were not connected");
        }
    }
}
