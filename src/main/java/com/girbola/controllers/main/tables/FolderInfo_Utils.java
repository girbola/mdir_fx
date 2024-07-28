package com.girbola.controllers.main.tables;

import com.girbola.Main;
import com.girbola.controllers.main.Tables;
import com.girbola.fileinfo.FileInfo;
import com.girbola.utils.FileInfoUtils;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.sql.SQL_Utils;
import common.utils.FileUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.*;

import static com.girbola.Main.simpleDates;
import static com.girbola.controllers.main.tables.TableUtils.calculateDateDifferenceRatio;

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

                if (renamed == null) {
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
        if (folderInfo == null) {
            folderInfo = new FolderInfo();
        }
        folderInfo.getFileInfoList().addAll(removeList);

        folderInfoSrc.getFileInfoList().removeAll(removeList);

        if (!folderInfoSrc.getFileInfoList().isEmpty()) {
            Messages.warningText_title("All files were not able to move", "Not able to move");
        } else {
            TableView<FolderInfo> table = tables.getTableByType(folderInfoSrc.getTableType());
            boolean remove = table.getItems().remove(folderInfoSrc);
            if (remove) {

            }

        }

        return true;
    }

    public static boolean moveFolderInfo(FolderInfo folderInfoSrc, Path dest) {

        return false;
    }

    public static boolean moveFolderInto(FolderInfo folderInfoSrc, FolderInfo folderInfoDest) {

        Path destPath = Paths.get(folderInfoDest.getFolderPath());

        List<FileInfo> fileInfoList = folderInfoDest.getFileInfoList();

        if (fileInfoList.isEmpty()) {
            return false;
        }

        for (Iterator<FileInfo> it = folderInfoSrc.getFileInfoList().iterator(); it.hasNext(); ) {
            FileInfo fileInfo = it.next();
            Path fileName = Paths.get(fileInfo.getOrgPath()).getFileName();
            Path changeDestPath = Paths.get(destPath.getParent().toString(), fileName.toString());
            if (!FileInfoUtils.compareImageHashes(fileInfo, folderInfoDest)) {

            }
            for (Iterator<FileInfo> it2 = folderInfoSrc.getFileInfoList().iterator(); it2.hasNext(); ) {
/*
            try {
                FileUtils.moveFile();
                Path changeDestPath1 = Files.move(Paths.get(fileInfo.getOrgPath()), changeDestPath);
                Messages.sprintf("changeDestPath1: " + changeDestPath1);

                fileInfo.setOrgPath(changeDestPath.toString());
                fileInfo.setDestination_Path(changeDestPath.toString());

                return true;
            } catch (IOException e) {
                Messages.errorSmth(ERROR, Main.bundle.getString("cannotMoveFile"),null, Misc.getLineNumber(), false);
                return false;
            }
*/


            }


        }
        return false;
    }

    public static boolean moveFolderInfoToDestination(List<FolderInfo> folderInfoSrcList, FolderInfo folderInfoDest) {

        /*
        event    =      hiking
        location =      mount everest
        C:\temp <-      absolutePath
        C:\kala\temp\a1 to be moved. Move files. Remove if empty
        C:\lohi\1       to be moved. Move files. Remove if empty

        Path absolutePath = C:\temp <- create new path to this. Move files under this folder.
        Path newAbsolutePath = C:\temp\hiking\mount everest\

        Path absolutePath = C:\temp <- create new path to this. Move files under this folder.
        Path path1ToMove = C:\kala\temp\a1 # Move files
        Path path2ToMove = C:\lohi\1       # Move files

         */




        Iterator<FolderInfo> folderInfoSrcIt = folderInfoSrcList.iterator();
        while(folderInfoSrcIt.hasNext()) {
            FolderInfo folderInfoSrc = folderInfoSrcIt.next();
            if (folderInfoSrc.getFolderPath().equals(folderInfoDest.getFolderPath())) {
                Messages.warningText(Main.bundle.getString("sourceDestinationWereTheSame"));
                return false;
            }
            Iterator<FileInfo> fileInfoSrcIt = folderInfoSrc.getFileInfoList().iterator();
            while(fileInfoSrcIt.hasNext()) {
                FileInfo fileInfoSrc = fileInfoSrcIt.next();

                boolean duplicates = FileInfoUtils.findDuplicates(fileInfoSrc, folderInfoDest);
                if(duplicates) {

                }
            }


        }

        /*
        Iterator<FileInfo> sourceFileInfoList = folderInfoSrc.getFileInfoList().iterator();

        List<FileInfo> destFileInfoList = folderInfoDest.getFileInfoList();
        List<FileInfo> sourceFileInfoListRemove = new ArrayList<>();

        for (Iterator<FileInfo> it = sourceFileInfoList; it.hasNext(); ) {
            FileInfo sourceFileInfo = it.next();
            String sourceFileName = Paths.get(sourceFileInfo.getOrgPath()).getFileName().toString();
            Path destFolderPath = Paths.get(folderInfoDest.getFolderPath(), sourceFileName);

            try {
                Path renamedFilePath = FileUtils.renameFile(Paths.get(sourceFileInfo.getOrgPath()), destFolderPath);
                if (renamedFilePath != null && Files.exists(renamedFilePath)) {
                    sourceFileInfo.setOrgPath(renamedFilePath.toString());
                    destFileInfoList.add(sourceFileInfo);
                    sourceFileInfoList.remove(sourceFileInfo);
                }
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
        }*/
        //sourceFileInfoList.removeAll(sourceFileInfoListRemove);


        return true;
    }

    public static void calculateFileInfoStatuses(FolderInfo folderInfo) {
        Messages.sprintf("calculateFileInfoStatuses updateFolderInfos_FileInfo: " + folderInfo.getFolderPath());

        int badFiles = 0;
        int goodFiles = 0;
        int imageFiles = 0;
        int rawFiles = 0;
        int videoFiles = 0;
        int confirmedFiles = 0;
        long sizeOfFiles = 0;
        int copiedFiles = 0;
        int ignoredFiles = 0;
        TreeMap<LocalDate, Integer> map = new TreeMap<>();

        List<Long> dateCounter_list = new ArrayList<>();
        if (folderInfo.getFileInfoList() == null) {
            Messages.sprintf("Somehow fileInfo list were null!!!");
            Main.setProcessCancelled(true);
            Messages.errorSmth(ERROR, "", null, Misc.getLineNumber(), true);
            return;
        }

        for (FileInfo fi : folderInfo.getFileInfoList()) {
            if (Main.getProcessCancelled()) {
                return;
            }
            if (fi.isIgnored() || fi.isTableDuplicated()) {
                Messages.sprintf("FileInfo were ignore or duplicated: " + fi.getOrgPath());
                ignoredFiles++;
            } else {
                sizeOfFiles += fi.getSize();
                if (fi.isCopied()) {
                    copiedFiles++;
                }
                if (fi.isBad()) {
                    badFiles++;
                }
                if (fi.isConfirmed()) {
                    confirmedFiles++;
                }
                if (fi.isGood()) {
                    goodFiles++;
                }

                if (fi.isRaw()) {
                    rawFiles++;
                }
                if (fi.isImage()) {
                    imageFiles++;
                }
                if (fi.isVideo()) {
                    videoFiles++;
                }
                if (fi.getDate() != 0) {
                    dateCounter_list.add(fi.getDate());
                } else {
                    fi.getDate();
                }

                LocalDate localDate = null;
                try {
                    localDate = LocalDate.of(Integer.parseInt(simpleDates.getSdf_Year().format(fi.getDate())), Integer.parseInt(simpleDates.getSdf_Month().format(fi.getDate())), Integer.parseInt(simpleDates.getSdf_Day().format(fi.getDate())));

                } catch (Exception ex) {
                    Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
                }

                map.put(localDate, 0);
            }
        }
        Messages.sprintf("SIZES: " + sizeOfFiles + " Copied: " + copiedFiles + " files: " + (imageFiles + rawFiles + videoFiles) + " ignoredFiles: " + ignoredFiles);
        folderInfo.setConfirmed(confirmedFiles);
        folderInfo.setFolderFiles(imageFiles + rawFiles + videoFiles);

        folderInfo.setBadFiles(badFiles);

        folderInfo.setFolderRawFiles(rawFiles);

        folderInfo.setFolderVideoFiles(videoFiles);

        folderInfo.setGoodFiles(goodFiles);

        folderInfo.setCopied(copiedFiles);

        folderInfo.setFolderSize(sizeOfFiles);
        folderInfo.setFolderImageFiles(imageFiles);
        folderInfo.setFolderVideoFiles(videoFiles);

        long min = 0;
        long max = 0;

        if (Files.exists(Paths.get(folderInfo.getFolderPath()))) {
            folderInfo.setConnected(true);
        } else {
            folderInfo.setConnected(false);
        }
        if (!dateCounter_list.isEmpty()) {
            Collections.sort(dateCounter_list);
            try {
                min = Collections.min(dateCounter_list);
                max = Collections.max(dateCounter_list);

            } catch (Exception ex) {
                Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
            }
        }

        folderInfo.setMinDate(simpleDates.getSdf_ymd_hms_minusDots_default().format(min));
        folderInfo.setMaxDate(simpleDates.getSdf_ymd_hms_minusDots_default().format(max));

        double dateDifferenceRatio = calculateDateDifferenceRatio(map);
        folderInfo.setDateDifferenceRatio(dateDifferenceRatio);
        // sprintf("Datedifference ratio completed");
        // folderInfo.setDateDifferenceRatio(0);
        dateCounter_list.clear();
        badFiles = 0;
        goodFiles = 0;
        imageFiles = 0;
        rawFiles = 0;
        videoFiles = 0;
        confirmedFiles = 0;

    }


    public static boolean hasBadFiles(FolderInfo folderInfo) {
        if (folderInfo.getBadFiles() >= 1) {
            Messages.warningText(Main.bundle.getString("badDatesFound"));
            return false;
        }
        return true;
    }
}
