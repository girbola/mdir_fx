package com.girbola;

import com.girbola.controllers.folderscanner.SelectedFolder;
import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.selectedfolder.SelectedFolderScanner;
import com.girbola.controllers.main.sql.ConfigurationSQLHandler;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.controllers.main.tables.model.StoredFolderInfoStatus;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.drive.DriveInfo;
import com.girbola.drive.DriveInfoUtils;
import com.girbola.fileinfo.FileInfo;
import com.girbola.filelisting.GetAllMediaFiles;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.sql.FolderInfo_SQL;
import com.girbola.sql.SQL_Utils;
import com.girbola.sql.SavedFolderInfosSQL;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
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
                    Messages.sprintfError("folderInfo_list were empty!!!!" + Load_FileInfosBackToTableViews.class.getName());
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

                        Messages.sprintf("FolderInfo= " + folderInfo.getFolderPath() + " folderInfo.size::::::::. " + folderInfo.getFileInfoList().size());

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

    /**
     * Checks and updates folder path changes for the given FolderInfo
     *
     * @param folderInfo The folder information to check and update
     */
    private void checkFolderPathChanges(FolderInfo folderInfo) {
        try {
            if (folderInfo.getIgnored()) {
                Messages.sprintf("checkFolderPathChanges was ignored");
                return;
            }

            Messages.sprintf("checkFolderPathChanges started");
            Path folderInfoFolderPath = Paths.get(folderInfo.getFolderPath());
            if (!Files.exists(folderInfoFolderPath)) {
                Messages.sprintfError("Folder does not exist: " + folderInfoFolderPath);
                return;
            }

            String sourceFolderSerialNumber = folderInfo.getSourceFolderSerialNumber();
            if (sourceFolderSerialNumber == null) {
                sourceFolderSerialNumber = "";
            }


            if (sourceFolderSerialNumber.isEmpty()) {
                boolean hasEmptySerialNumber = handleEmptySerialNumber(folderInfo);
                if (hasEmptySerialNumber) {
                    folderInfo.setChanged(true);
                }
            }

            ArrayList<Path> newFilesToAdd = checkFolderForChangedFilesAndFolder(folderInfo);


            Messages.sprintf("checkFolderPathChanges finished");
        } catch (Exception e) {
            Messages.sprintfError("Error in checkFolderPathChanges: " + e.getMessage() + " FOLDERINFO PATH WAS::::: " + folderInfo.toString());
            e.printStackTrace();
            return;
        }
    }

    private ArrayList<Path> checkFolderForChangedFilesAndFolder(FolderInfo folderInfo) {
        ArrayList<Path> newFilesToAdd = new ArrayList<>();
        ArrayList<Long> compareFolderInfoContentBySizes = new ArrayList<>();
        ArrayList<Long> compareMediaFilesContentBySizes = new ArrayList<>();

        Path folderInfoFolderPath = Paths.get(folderInfo.getFolderPath());
        ArrayList<Path> mediaFilesInCurrentFolder = GetAllMediaFiles.getAllMediaFiles(folderInfoFolderPath);


        for (FileInfo fileInfo : folderInfo.getFileInfoList()) {
            compareFolderInfoContentBySizes.add(fileInfo.getSize());
        }
        for (Path mediaFile : mediaFilesInCurrentFolder) {
            compareMediaFilesContentBySizes.add(mediaFile.toFile().length());
        }
        if (compareFolderInfoContentBySizes.size() != compareMediaFilesContentBySizes.size()) {
            Messages.warningText("FolderInfo and mediaFiles size are not equal! FolderInfo: " + compareFolderInfoContentBySizes.size() + " mediaFiles: " + compareMediaFilesContentBySizes.size());
            for (FileInfo fileInfo : folderInfo.getFileInfoList()) {

                for (Path mediaFile : mediaFilesInCurrentFolder) {
                    if (fileInfo.getOrgPath().equals(mediaFile.toString())) {
                        Messages.sprintf("checkFolderForChangedFilesAndFolder fileInfo: " + fileInfo);
                        Messages.sprintf("checkFolderForChangedFilesAndFolder mediaFile: " + mediaFile);
                        break;
                    }
                    newFilesToAdd.add(mediaFile);
                }
            }
        }
        Messages.sprintf("checkFolderForChangedFilesAndFolder newFilesToAdd: " + newFilesToAdd.size());
        return newFilesToAdd;
    }

    private boolean handleEmptySerialNumber(FolderInfo folderInfo) {
        List<DriveInfo> driveInfoList = modelMain.getSqlHandler().getDriveInfoList();
        Path folderToFindPath = Paths.get(folderInfo.getFolderPath());
        for (DriveInfo driveInfo : driveInfoList) {
            if (hasPath(driveInfo, folderToFindPath)) {
                return true;
            }
        }
        return false;

    }

    private boolean hasPath(DriveInfo driveInfo, Path pathToFind) {
        if (driveInfo == null || driveInfo.getDrive() == null || pathToFind == null) {
            return false;
        }

        try {
            Path rootPath = driveInfo.getDrive().getRoot();
            Path searchRoot = pathToFind.getRoot();

            if (rootPath == null || searchRoot == null) {
                return false;
            }

            Path relativePath = searchRoot.relativize(pathToFind);
            Path fullPath = rootPath.resolve(relativePath);

            return Files.exists(fullPath);
        } catch (Exception e) {
            // Consider proper logging here
            return false;
        }
    }

    private void checkFolderPathChanges_(FolderInfo folderInfo) {
        Messages.sprintf("checkFolderPathChanges started");
        String folderPath = folderInfo.getFolderPath();
        List<DriveInfo> driveInfoList = modelMain.getSqlHandler().getDriveInfoList();

        String sourceFolderSerialNumber = "";

        try {
            sourceFolderSerialNumber = folderInfo.getSourceFolderSerialNumber();
        } catch (Exception e) {
            Messages.sprintfError("Cannot get source folder serialnumber for recognize actual drive: " + Misc.getLineNumber());
            return;
        }

        if (sourceFolderSerialNumber == null || sourceFolderSerialNumber.isEmpty()) {
            Messages.sprintfError("Cannot get source folder serialnumber for recognize actual drive: " + Misc.getLineNumber());
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