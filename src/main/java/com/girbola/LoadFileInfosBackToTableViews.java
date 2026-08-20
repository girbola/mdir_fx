package com.girbola;

import com.girbola.controllers.folderscanner.SelectedFolder;
import com.girbola.controllers.folderscanner.SelectedFolderUtils;
import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.sql.ConfigurationSQLHandler;
import com.girbola.controllers.main.tables.FolderInfoUtils;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.controllers.main.tables.model.FolderInfoStatus;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.persistence.folderinfo.FolderInfoDao;
import com.girbola.sql.SQL_Utils;
import com.girbola.persistence.configuration.ConfigurationSavedFoldersDao;
import com.girbola.utils.FileInfoUtils;
import java.io.File;
import java.nio.file.Files;
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
                assert savedFolders != null;

                Messages.sprintf("LoadFileInfosBackToTableViews savedFolderInfoStatuses" + savedFolders.size());
                if (savedFolders == null || savedFolders.isEmpty()) {
                    Messages.sprintf("There were no data available for loading" + LoadFileInfosBackToTableViews.class.getName());
                    cancel();
                    return false;
                } else {
                    for (FolderInfoStatus folderInfoStatus : savedFolders) {
                        Messages.sprintf("###### Loading SavedFolders folderInfoStatus: " + folderInfoStatus.getFolderPath());
                        if (Main.getProcessCancelled()) {
                            Messages.sprintf("FolderInfoStatus Process cancelled");
                            cancel();
                            return false;
                        }
                        if (folderInfoStatus.getFolderPath().equals("E:\\M-Drive\\Documents\\Kuviloi")) {
                            Messages.sprintf("FolderInfo was found: " + folderInfoStatus.getFolderPath() + " LINE::: " + Misc.getLineNumber());
                            for (SelectedFolder selectedFolder : modelMain.getSelectedFolders().getSelectedFolderScanner_obs()) {
                                Messages.sprintf("111111111111111111111selectedFolder: " + selectedFolder.getFolder() + " LINE::: " + Misc.getLineNumber());
                            }
                        }
                        Messages.sprintf("=============LoadFileInfosBackToTableViews: " + folderInfoStatus.getFolderPath() + " savedFolderInfoStatus " + folderInfoStatus);


                        if (SelectedFolderUtils.startsWithSelectedFolders(modelMain.getSelectedFolders().getSelectedFolderScanner_obs(), Paths.get(folderInfoStatus.getFolderPath()))) {

                            SelectedFolder selectedFolder = SelectedFolderUtils.getSelectedFolderParent(folderInfoStatus.getFolderPath(), modelMain);
                            if (selectedFolder == null) {
                                Messages.sprintf("SelectedFolder was found: " + selectedFolder.getFolder() + " LINE::: " + Misc.getLineNumber());
                                selectedFolder.setConnected(false);
                                continue;
                            }

                            FolderInfo folderInfo = FolderInfoDao.loadFolderInfo(folderInfoStatus.getFolderPath());
                            if (folderInfo == null) { // Not connected if not found in database. This is because the folder might have been deleted or moved to another location.
                                if (Files.exists(Paths.get(folderInfoStatus.getFolderPath()))) {
                                    folderInfo = new FolderInfo(Paths.get(folderInfoStatus.getFolderPath()));
                                    FileInfoUtils.createFileInfo_list(folderInfo);

                                    folderInfoStatus.setConnected(true);
                                    Messages.sprintf("FolderInfo was null for some reason: " + folderInfoStatus.getFolderPath() + " LINE::: " + Misc.getLineNumber());
                                } else {
                                    folderInfoStatus.setConnected(false);
                                    Messages.sprintf("FolderInfo was null for some reason: " + folderInfoStatus.getFolderPath() + " LINE::: " + Misc.getLineNumber());
                                    continue;
                                }
                            }

                            boolean folderInfoAddedToTable = modelMain.tables().addToTable(folderInfo);
                            if (folderInfoAddedToTable) {
                                Messages.sprintf("FolderInfo was added to table: " + folderInfo.getFolderPath() + " LINE::: " + Misc.getLineNumber());
                            } else {
                                Messages.sprintfError("FolderInfo were NOT able to add to table: " + folderInfo.getFolderPath() + " LINE::: " + Misc.getLineNumber());
                            }

                        }
                    }
                }
                return true;
            }
        };
    }

}
