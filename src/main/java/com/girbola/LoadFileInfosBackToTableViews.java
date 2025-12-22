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
import com.girbola.sql.ConfigurationSavedFoldersDao;
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

                List<FolderInfoStatus> savedFolders = ConfigurationSavedFoldersDao.loadSavedFolderDetails(connection, modelMain);
                Messages.sprintf("LoadFileInfosBackToTableViews savedFolderInfoStatuses: " + savedFolders.size());
                if (savedFolders == null || savedFolders.isEmpty()) {
                    Messages.sprintf("There were no data available for loading" + LoadFileInfosBackToTableViews.class.getName());
                    cancel();
                    return false;
                } else {
                    for (FolderInfoStatus folderInfoStatus : savedFolders) {
                        Messages.sprintf("-----folderInfoStatus: " + folderInfoStatus.getFolderPath());
                        if (Main.getProcessCancelled()) {
                            cancel();
                            return false;
                        }
                        //Messages.sprintf("=============SavedFolderInfoStatus: " + folderInfoStatus.getFolderPath() + " savedFolderInfoStatus " + folderInfoStatus);

                        FolderInfo folderInfo = FolderInfo_SQL.loadFolderInfo(folderInfoStatus.getFolderPath());
                        if (folderInfo == null) {
                            Messages.sprintf("FolderInfo was null for some reason: " + folderInfoStatus.getFolderPath() + " " + Misc.getLineNumber());
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