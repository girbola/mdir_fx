package com.girbola;

import com.girbola.controllers.folderscanner.SelectedFolder;
import com.girbola.controllers.folderscanner.SelectedFolderUtils;
import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.sql.ConfigurationSQLHandler;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.controllers.main.tables.model.SavedFolderInfos;
import com.girbola.drive.DriveInfo;
import com.girbola.drive.DriveInfoUtils;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.persistence.folderinfo.FolderInfoDao;
import com.girbola.sql.SQL_Utils;
import com.girbola.persistence.configuration.ConfigurationSavedFoldersDao;
import com.girbola.utils.FileInfoUtils;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.List;
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
                } else {
                    List<DriveInfo> currentDrives = DriveInfoUtils.getCurrentDriveInfos();
                    for (SavedFolderInfos savedFolderInfos : savedFolders) {
                        Messages.sprintf("###### Loading SavedFolders savedFolderInfos: " + savedFolderInfos.getFolderPath());
                        if (Main.getProcessCancelled()) {
                            Messages.sprintf("SavedFolderInfos process cancelled");
                            cancel();
                            return false;
                        }

                        Path originalPath = Paths.get(savedFolderInfos.getFolderPath()).toAbsolutePath().normalize();
                        var resolvedPath = savedFolderInfos.resolveCurrentFolderPath(currentDrives);
                        if (resolvedPath.isEmpty() && savedFolderInfos.hasStableDriveIdentity()) {
                            Messages.sprintf("No current drive matches saved folder identity: " + originalPath);
                            continue;
                        }
                        Path folderPath = resolvedPath.orElse(originalPath);
                        if (!folderPath.equals(originalPath)) {
                            savedFolderInfos.findMatchingDrive(currentDrives)
                                    .ifPresent(drive -> remapSelectedFolder(originalPath, drive));
                            Messages.sprintf("Resolved saved folder path from " + originalPath + " to " + folderPath
                                    + " using the saved drive identity");
                        }

                        Messages.sprintf("=============LoadFileInfosBackToTableViews: " + folderPath
                                + " connected " + savedFolderInfos.isConnected() + " LINE::: " + Misc.getLineNumber());
                        if (SelectedFolderUtils.startsWithSelectedFolders(
                                modelMain.getSelectedFolders().getSelectedFolderScanner_obs(), folderPath)) {

                            SelectedFolder selectedFolder = SelectedFolderUtils.getSelectedFolderParent(folderPath.toString(), modelMain);
                            if (selectedFolder == null) {
                                Messages.sprintf("SelectedFolder was NULL: " + folderPath + " at LINE::: " + Misc.getLineNumber());
                                continue;
                            }

                            FolderInfo folderInfo = FolderInfoDao.loadFolderInfo(folderPath.toString());
                            if (folderInfo == null) { // Not connected if not found in database. This is because the folder might have been deleted or moved to another location.
                                if (Files.exists(folderPath)) {
                                    folderInfo = new FolderInfo(folderPath);
                                    FileInfoUtils.createFileInfo_list(folderInfo);

                                    savedFolderInfos.setConnected(true);
                                    Messages.sprintf("FolderInfo was null for some reason: " + folderPath + " LINE::: " + Misc.getLineNumber());
                                } else {
                                    savedFolderInfos.setConnected(false);
                                    Messages.sprintf("FolderInfo was null for some reason: " + folderPath + " LINE::: " + Misc.getLineNumber());
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

            private void remapSelectedFolder(Path savedFolderPath, DriveInfo currentDrive) {
                Path savedRoot = savedFolderPath.getRoot();
                Path currentDrivePath;
                try {
                    currentDrivePath = Paths.get(currentDrive.getDrivePath()).toAbsolutePath().normalize();
                } catch (RuntimeException e) {
                    Messages.sprintfError("Invalid current drive path: " + currentDrive.getDrivePath()
                            + " error: " + e.getMessage());
                    return;
                }
                Path currentRoot = currentDrivePath.getRoot();
                if (savedRoot == null || currentRoot == null) {
                    return;
                }

                for (SelectedFolder selectedFolder : modelMain.getSelectedFolders().getSelectedFolderScanner_obs()) {
                    Path selectedPath;
                    try {
                        selectedPath = Paths.get(selectedFolder.getFolder()).toAbsolutePath().normalize();
                    } catch (RuntimeException e) {
                        continue;
                    }
                    if (!savedFolderPath.startsWith(selectedPath) || !savedRoot.equals(selectedPath.getRoot())) {
                        continue;
                    }

                    Path rebasedPath = currentRoot.resolve(savedRoot.relativize(selectedPath)).normalize();
                    if (Files.isDirectory(rebasedPath)) {
                        selectedFolder.setFolder(rebasedPath.toString());
                        selectedFolder.setDriveSerialNumber(currentDrive.getIdentifier());
                        selectedFolder.setConnected(true);
                    }
                    return;
                }
            }
        };
    }

}
