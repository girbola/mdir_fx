package com.girbola;

import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.sql.ConfigurationSQLHandler;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.controllers.main.tables.model.FolderInfoStatus;
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

public class LoadFileInfosBackToTableViews extends Service<Boolean> {
    private ModelMain modelMain;
    private Connection connection;

    public LoadFileInfosBackToTableViews(ModelMain modelMain, Connection connection) {
        this.modelMain = modelMain;
        this.connection = connection;
    }

    @Override
    protected Task<Boolean> createTask() {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                Messages.sprintf("LoadFileInfosBackToTableViews starts " + Paths.get(Main.conf.getAppDataPath() + File.separator + Main.conf.getConfiguration_db_fileName()));

                if (!SQL_Utils.isDbConnected(connection)) {
                    ConfigurationSQLHandler.checkConnection();
                }

                List<FolderInfoStatus> folderInfoStatuses = SavedFolderInfosSQL.fetchAllSavedFolderInfosFromDatabase(connection, modelMain);
                Messages.sprintf("LoadFileInfosBackToTableViews savedFolderInfoStatuses: " + folderInfoStatuses.size());
                if (folderInfoStatuses == null || folderInfoStatuses.isEmpty()) {
                    Messages.sprintf("There were no data available for loading" + LoadFileInfosBackToTableViews.class.getName());
                    cancel();
                    return false;
                } else {
                    for (FolderInfoStatus folderInfoStatus : folderInfoStatuses) {
                        Messages.sprintf("-----folderInfoStatus: " + folderInfoStatus.getFolderPath());
                        if (Main.getProcessCancelled()) {
                            cancel();
                            return false;
                        }
                        Messages.sprintf("=============SavedFolderInfoStatus: " + folderInfoStatus.getFolderPath() + " savedFolderInfoStatus " + folderInfoStatus);

                        FolderInfo folderInfo = FolderInfo_SQL.loadFolderInfo(folderInfoStatus.getFolderPath());
                        if (folderInfo == null) {
                            continue;
                        }
                        Messages.sprintf("-----------------folderInfo table type:::: " + folderInfo.getTableType());

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