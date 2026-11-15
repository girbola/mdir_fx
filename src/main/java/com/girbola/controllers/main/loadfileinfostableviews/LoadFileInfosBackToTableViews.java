package com.girbola.controllers.main.loadfileinfostableviews;

import com.girbola.Main;
import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.sql.ConfigurationSQLHandler;
import com.girbola.controllers.main.tables.model.SavedFolderInfos;
import com.girbola.controllers.loading.LoadingProcessTask;
import com.girbola.drive.DriveInfoUtils;
import com.girbola.messages.Messages;
import com.girbola.sql.SQL_Utils;
import com.girbola.persistence.configuration.ConfigurationSavedFoldersDao;
import java.io.File;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.List;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class LoadFileInfosBackToTableViews extends Service<Boolean> {
    private final ModelMain modelMain;
    private final Connection connection;
    private final LoadingProcessTask loadingProcessTask;

    public LoadFileInfosBackToTableViews(ModelMain modelMain, Connection connection) {
        this(modelMain, connection, null);
    }

    public LoadFileInfosBackToTableViews(
            ModelMain modelMain, Connection connection, LoadingProcessTask loadingProcessTask) {
        this.modelMain = modelMain;
        this.connection = connection;
        this.loadingProcessTask = loadingProcessTask;
    }

    @Override
    protected Task<Boolean> createTask() {
        if (loadingProcessTask != null) {
            loadingProcessTask.bindService(this);
        }

        return new Task<>() {
            @Override
            protected Boolean call() {
                Messages.sprintf("LoadFileInfosBackToTableViews starts " + Paths.get(Main.conf.getAppDataPath() + File.separator + Main.conf.getConfiguration_db_fileName()));

                if (!SQL_Utils.isDbConnected(connection)) {
                    ConfigurationSQLHandler.checkConnection();
                }

                List<SavedFolderInfos> savedFolders = ConfigurationSavedFoldersDao.loadSavedFolderDetails(connection, modelMain);
                if (savedFolders == null) {
                    Messages.sprintfError("Unable to load saved folder entries");
                    return false;
                }

                Messages.sprintf("LoadFileInfosBackToTableViews savedFolderInfos " + savedFolders.size());
                if (savedFolders.isEmpty()) {
                    Messages.sprintf("There were no data available for loading" + LoadFileInfosBackToTableViews.class.getName());
                    cancel();
                    return false;
                }

                LoadFileInfosUtils.LoadSavedFoldersResult loadResult = LoadFileInfosUtils.loadSavedFoldersToTableViews(
                        modelMain,
                        savedFolders,
                        DriveInfoUtils.getCurrentDriveInfos(),
                        Main::getProcessCancelled,
                        (done, total, folderPath) -> {
                            updateProgress(done, total);
                            if (folderPath == null || folderPath.isBlank()) {
                                updateMessage("Loading folders " + done + "/" + total);
                                if (loadingProcessTask != null) {
                                    loadingProcessTask.updateTextArea("Finished loading folders.");
                                }
                                return;
                            }

                            int currentFolder = done + 1;
                            updateMessage("Loading folders " + currentFolder + "/" + total);
                            if (loadingProcessTask != null) {
                                loadingProcessTask.updateTextArea(folderPath);
                            }
                        });
                if (loadResult == LoadFileInfosUtils.LoadSavedFoldersResult.CANCELLED) {
                    cancel();
                    return false;
                }
                return true;
            }
        };
    }

}
