package com.girbola;

import com.girbola.controllers.folderscanner.SelectedFolder;
import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.selectedfolder.SelectedFolderScanner;
import com.girbola.controllers.main.sql.ConfigurationSQLHandler;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.controllers.main.tables.model.SavedFolderInfoStatus;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.drive.DriveInfo;
import com.girbola.drive.DriveInfoUtils;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.sql.FolderInfo_SQL;
import com.girbola.sql.SavedFolderInfosSQL;
import com.girbola.sql.SQL_Utils;
import java.nio.file.Path;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.List;

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

                List<SavedFolderInfoStatus> savedFolderInfoStatuses = SavedFolderInfosSQL.fetchAllSavedFolderInfosFromDatabase(connection, modelMain);
                Messages.sprintf("Load_FileInfosBackToTableViews savedFolderInfoStatuses: " + savedFolderInfoStatuses.size());
                if (savedFolderInfoStatuses == null || savedFolderInfoStatuses.isEmpty()) {
                    Messages.sprintfError("folderInfo_list were empty!!!!" + Load_FileInfosBackToTableViews.class.getName());
                    cancel();
                    return false;
                } else {
                    for (SavedFolderInfoStatus savedFolderInfoStatus : savedFolderInfoStatuses) {
                        if (Main.getProcessCancelled()) {
                            cancel();
                            return false;
                        }
                        Messages.sprintf("=============SavedFolderInfoStatus: " + savedFolderInfoStatus.getFolderPath() + " savedFolderInfoStatus " + savedFolderInfoStatus);
                        FolderInfo folderInfo = FolderInfo_SQL.loadFolderInfo(savedFolderInfoStatus.getFolderPath());

                        Messages.sprintf("FolderInfo= " + folderInfo.getFolderPath() + " folderInfo.size::::::::. " + folderInfo.getFileInfoList().size());

                        checkFolderPathChanges(folderInfo);

                        if (folderInfo.getTableType().equals(TableType.SORTIT.getType())) {
                            modelMain.tables().getSortIt_table().getItems().add(folderInfo);
                        }
                        if (folderInfo.getTableType().equals(TableType.SORTED.getType())) {
                            modelMain.tables().getSorted_table().getItems().add(folderInfo);
                        }
                        if (folderInfo.getTableType().equals(TableType.ASITIS.getType())) {
                            modelMain.tables().getAsItIs_table().getItems().add(folderInfo);
                        }

                    }
                }
                return true;
            }
        };
    }

    private void checkFolderPathChanges(FolderInfo folderInfo) {
        Messages.sprintf("checkFolderPathChanges started");
        String folderPath = folderInfo.getFolderPath();
        List<DriveInfo> driveInfoList = modelMain.getSqlHandler().getDriveInfoList();

        String sourceFolderSerialNumber = "";

        try {
            sourceFolderSerialNumber = folderInfo.getSourceFolderSerialNumber();
        } catch (Exception e) {
            Messages.sprintfError("sourceFolderSerialNumber was null or empty!");
            return;
        }


        if (sourceFolderSerialNumber == null || sourceFolderSerialNumber.isEmpty()) {
            Messages.sprintf("sourceFolderSerialNumber was null or empty!");
            Path rootPath = Paths.get(folderInfo.getFolderPath());
            // D:\UserPicturesUser1\Picture
            // E:\UserPicturesUser1\Picture

            // /media/
            SelectedFolderScanner selectedFolders = modelMain.getSelectedFolders();
            int folders = folderInfo.getFileInfoList().size();
            int counter = 0;
            for (FileInfo fileInfo : folderInfo.getFileInfoList()) {
                for (SelectedFolder selectedFolderInfo : selectedFolders.getSelectedFolderScanner_obs()) {
                    String parsedFileInfoPath = fileInfo.getOrgPath().replace(selectedFolderInfo.getFolder(), "");
                    if (fileInfo.getOrgPath().contains(selectedFolderInfo.getFolder())) {
                        Path path = Paths.get(selectedFolderInfo.getFolder(), parsedFileInfoPath);
                        if (Files.exists(path)) {
                            fileInfo.setOrgPath(path.toString());
                            folderInfo.setChanged(true);
                            counter++;
                        }
                    }
                }
            }
            if (counter == folders) {
                Messages.sprintf("All files were renamed to new path");
                folderInfo.setSourceFolderSerialNumber(rootPath.getFileSystem().toString());
                folderInfo.setChanged(true);
            }

        } else {
            Messages.sprintfError("sourceFolderSerialNumber was null or empty!");
            for (FileInfo fileInfo : folderInfo.getFileInfoList()) {
                if (!folderPath.equals(fileInfo.getOrgPath())) {
                    if (DriveInfoUtils.hasDrivePath(driveInfoList, fileInfo.getOrgPath(), sourceFolderSerialNumber)) {
                        fileInfo.setOrgPath(folderPath);
                        folderInfo.setChanged(true);
                    }
                }
            }
        }
        Messages.sprintf("checkFolderPathChanges finished");
    }
}
