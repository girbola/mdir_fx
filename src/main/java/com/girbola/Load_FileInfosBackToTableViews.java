package com.girbola;

import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.sql.ConfigurationSQLHandler;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.controllers.main.tables.model.StoredFolderInfoStatus;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.sql.FolderInfo_SQL;
import com.girbola.sql.SQL_Utils;
import com.girbola.sql.SavedFolderInfosSQL;
import java.io.File;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.List;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class Load_FileInfosBackToTableViews extends Service<Boolean> {
    private ModelMain modelMain;
    private Connection connection;

    public Load_FileInfosBackToTableViews(ModelMain modelMain, Connection connection) {
        this.modelMain = modelMain;
        this.connection = connection;
    }

    @Override
    protected Task<Boolean> createTask() {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                Messages.sprintf("Load_FileInfosBackToTableViews starts " + Paths.get(Main.conf.getAppDataPath() + File.separator + Main.conf.getConfiguration_db_fileName()));

                if (!SQL_Utils.isDbConnected(connection)) {
                    ConfigurationSQLHandler.checkConnection();
                }

                List<StoredFolderInfoStatus> storedFolderInfoStatuses = SavedFolderInfosSQL.fetchAllSavedFolderInfosFromDatabase(connection, modelMain);
                Messages.sprintf("Load_FileInfosBackToTableViews savedFolderInfoStatuses: " + storedFolderInfoStatuses.size());
                if (storedFolderInfoStatuses == null || storedFolderInfoStatuses.isEmpty()) {
                    Messages.sprintf("There were no data available for loading" + Load_FileInfosBackToTableViews.class.getName());
                    cancel();
                    return false;
                } else {
                    for (StoredFolderInfoStatus storedFolderInfoStatus : storedFolderInfoStatuses) {
                        if (Main.getProcessCancelled()) {
                            cancel();
                            return false;
                        }
                        Messages.sprintf("=============SavedFolderInfoStatus: " + storedFolderInfoStatus.getFolderPath() + " savedFolderInfoStatus " + storedFolderInfoStatus);
                        FolderInfo folderInfo = FolderInfo_SQL.loadFolderInfo(storedFolderInfoStatus.getFolderPath());

                        try {

                            if (folderInfo.getTableType().equalsIgnoreCase(TableType.SORTIT.getType())) {
                                modelMain.tables().getSortIt_table().getItems().add(folderInfo);
                            } else if (folderInfo.getTableType().equalsIgnoreCase(TableType.SORTED.getType())) {
                                modelMain.tables().getSorted_table().getItems().add(folderInfo);
                            } else if (folderInfo.getTableType().equalsIgnoreCase(TableType.ASITIS.getType())) {
                                modelMain.tables().getAsItIs_table().getItems().add(folderInfo);
                            } else {
                                Messages.sprintfError("FolderInfo tableType was not recognized: " + folderInfo.getTableType() + " " + Misc.getLineNumber());
                                Platform.exit();
                            }
                        } catch (Exception e) {
                            Messages.sprintfError("Error in tableType: " + folderInfo.getTableType() + " " + Misc.getLineNumber() + " " + e.getMessage());
                        }
                    }
                }
                return true;
            }
        };
    }

}