package com.girbola;

import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.sql.ConfigurationSQLHandler;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.controllers.main.tables.model.FolderInfoStatus;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.persistence.folderinfo.FolderInfoDao;
import com.girbola.sql.SQL_Utils;
import com.girbola.persistence.configuration.ConfigurationSavedFoldersDao;
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
                Messages.sprintf("LoadFileInfosBackToTableViews savedFolderInfoStatuses: " + savedFolders.get(0).getFolderPath());
                if (savedFolders == null || savedFolders.isEmpty()) {
                    Messages.sprintf("There were no data available for loading" + LoadFileInfosBackToTableViews.class.getName());
                    cancel();
                    return false;
                } else {
                    for (FolderInfoStatus folderInfoStatus : savedFolders) {
                        Messages.sprintf("-----folderInfoStatus: " + folderInfoStatus.getFolderPath());
                        if (Main.getProcessCancelled()) {
                            Messages.sprintf("FolderInfoStatus Process cancelled");
                            cancel();
                            return false;
                        }
                        Messages.sprintf("=============SavedFolderInfoStatus: " + folderInfoStatus.getFolderPath() + " savedFolderInfoStatus " + folderInfoStatus);

                        FolderInfo folderInfo = FolderInfoDao.loadFolderInfo(folderInfoStatus.getFolderPath());
                        if (folderInfo == null) {
                            Messages.sprintf("FolderInfo was null for some reason: " + folderInfoStatus.getFolderPath() + " LINE::: " + Misc.getLineNumber());
                            continue;
                        }
                        if(!folderInfo.getFolderPath().contains("C:\\Users\\marko\\OneDrive\\Kuvat\\100CANON\\")) {
                            Messages.sprintf("Here we go!: " + folderInfo.getFolderPath());
                        }
                        Messages.sprintf("-----------------folderInfo getFolderPath:::: " + folderInfo.getFolderPath());

                        try {
                            if (folderInfo.getTableType().equalsIgnoreCase(TableType.SORTIT.getType())) {
                                modelMain.tables().getSortIt_table().getItems().add(folderInfo);
                                Messages.sprintf("SORTIT ADDED: " + folderInfo.getFolderPath() + " " + Misc.getLineNumber());
                            } else if (folderInfo.getTableType().equalsIgnoreCase(TableType.SORTED.getType())) {
                                modelMain.tables().getSorted_table().getItems().add(folderInfo);
                                Messages.sprintf("SORTED ADDED: " + folderInfo.getFolderPath() + " " + Misc.getLineNumber());
                            } else if (folderInfo.getTableType().equalsIgnoreCase(TableType.ASITIS.getType())) {
                                modelMain.tables().getAsItIs_table().getItems().add(folderInfo);
                                Messages.sprintf("ASITIS ADDED: " + folderInfo.getFolderPath() + " " + Misc.getLineNumber());
                            } else {
                                Messages.sprintfError("FolderInfo tableType was not recognized: " + folderInfo.getTableType() + " " + Misc.getLineNumber());
                                Platform.exit();
                            }
                            Messages.sprintf("FOLDER TO ITERATE NOW::::: " + folderInfo.getFolderPath() + " TYPEEE:::: " + folderInfo.getTableType() + " " + Misc.getLineNumber());
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