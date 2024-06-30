package com.girbola.controllers.main.tables;

import com.girbola.Main;
import com.girbola.controllers.main.Tables;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.sql.SQL_Utils;
import com.girbola.sql.SqliteConnection;
import common.utils.FileUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class FolderInfo_Utils {

    private static final String ERROR = FolderInfo_Utils.class.getSimpleName();

    private static boolean findDuplicate(FolderInfo folderInfo, FileInfo fileInfo) {
        for (FileInfo findFileInfo : folderInfo.getFileInfoList()) {
            if (fileInfo.getOrgPath().equals(findFileInfo.getOrgPath())) {
                return true;
            }
        }
        return false;
    }

    public static boolean addFileInfoList(FolderInfo folderInfo, List<FileInfo> newList) {
        boolean changed = false;
        for (FileInfo fileInfo : newList) {
            boolean found = findDuplicate(folderInfo, fileInfo);
            if (!found) {
                folderInfo.getFileInfoList().add(fileInfo);
                changed = true;
            }
        }
        return changed;
    }

    /**
     * Returns String(Event), List<FileInfo>
     *
     * @param folderInfo_list
     * @return
     */
    public static ObservableList<FileInfo> getEvents(FolderInfo folderInfo_list) {
        ObservableList<FileInfo> list = FXCollections.observableArrayList();

        for (FileInfo fileInfo : folderInfo_list.getFileInfoList()) {
            if (!fileInfo.getEvent().isEmpty()) {
                Path path = Paths.get(fileInfo.getOrgPath()).getParent();
                addToObservableFileInfoList(list, path.toString(), fileInfo);
            }
        }
        return list;
    }

    public static void addToObservableFileInfoList(ObservableList<FileInfo> list, String string,
                                                   FileInfo fileInfo_ToFind) {

        for (FileInfo fileInfo : list) {
            if (fileInfo != fileInfo_ToFind) {
                if (fileInfo_ToFind.getEvent().equals(fileInfo.getEvent())) {
                    list.add(fileInfo);
                }
            }
        }
    }

    public static boolean moveEntireFolderInfoSourceToDestination(Tables tables, List<FolderInfo> selected, FolderInfo folderInfoSrc, String destinationPath) throws IOException {

        List<FileInfo> removeList = new ArrayList<>();

        Path destination = Paths.get(destinationPath);
        String destinationFilePath = destination.getParent().toString();


        for (FolderInfo selectedFolderInfo : selected) {
            for (FileInfo fileInfo : selectedFolderInfo.getFileInfoList()) {
                Path fileName = Paths.get(fileInfo.getOrgPath()).getFileName();

                Path source = Paths.get(fileInfo.getOrgPath());
                Path destinationFinalPath = Paths.get(destinationFilePath, fileName.toString());

                Path renamed = FileUtils.renameFile(source, destinationFinalPath);

                if(renamed == null) {
                    Messages.sprintf("File did exists: " + source + " at destination: " + destinationFinalPath);
                    continue;
                }
                try {
                    Files.move(source, renamed, StandardCopyOption.REPLACE_EXISTING);

                    // Set the new file path for the fileInfo
                    fileInfo.setOrgPath(renamed.toString());
                    fileInfo.setDestination_Path(renamed.toString());
                    removeList.add(fileInfo);
                } catch (IOException e) {
                    Messages.sprintfError("Failed to move the file. Error: " + e.getMessage());
                    return false;
                }

            }
        }
        FolderInfo folderInfo = SQL_Utils.loadFolderInfo(destinationPath);
        if(folderInfo == null) {
            folderInfo = new FolderInfo();
        }
        folderInfo.getFileInfoList().addAll(removeList);

        folderInfoSrc.getFileInfoList().removeAll(removeList);

        if(!folderInfoSrc.getFileInfoList().isEmpty()) {
            Messages.warningText_title("All files were not able to move", "Not able to move");
        } else {
            TableView<FolderInfo> table = tables.getTableByType(folderInfoSrc.getTableType());
            boolean remove = table.getItems().remove(folderInfoSrc);
            if(remove) {

            }

        }

        return true;
    }

    public static boolean moveFolderInfoToDestination(FolderInfo folderInfoSrc, FolderInfo folderInfoDest) {

        if (folderInfoSrc.getFolderPath().equals(folderInfoDest.getFolderPath())) {
            Messages.warningText(Main.bundle.getString("sourceDestinationWereTheSame"));
            return false;
        }

        List<FileInfo> sourceFileInfoList = folderInfoSrc.getFileInfoList();

        List<FileInfo> destFileInfoList = folderInfoDest.getFileInfoList();
        List<FileInfo> destFileInfoListRemove = new ArrayList<>();

        for (FileInfo sourceFileInfo : sourceFileInfoList) {
            String sourceFileName = Paths.get(sourceFileInfo.getOrgPath()).getFileName().toString();
            Path destFolderPath = Paths.get(folderInfoDest.getFolderPath());

            try {
                FileUtils.renameFile(Paths.get(folderInfoDest.getFolderPath()), destFolderPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


        }

        for (FileInfo fileInfo : sourceFileInfoList) {
            boolean found = findDuplicate(folderInfoDest, fileInfo);
            if (!found) {
                destFileInfoList.add(fileInfo);
                destFileInfoListRemove.add(fileInfo);
            }
        }
        //sourceFileInfoList.removeAll(sourceFileInfoListRemove);


        return true;
    }

    public static boolean hasBadFiles(FolderInfo folderInfo) {
        if (folderInfo.getBadFiles() >= 1) {
            Messages.warningText(Main.bundle.getString("badDatesFound"));
            return false;
        }
        return true;
    }
}
