/*
 @(#)Copyright:  Copyright (c) 2012-2024 All right reserved.
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.utils;

import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifThumbnailDirectory;
import com.girbola.MDir_Constants;
import com.girbola.Main;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.fileinfo.FileInfo;
import com.girbola.filelisting.ValidatePathUtils;
import com.girbola.fxml.possiblefolderchooser.PossibleFolderChooserController;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import common.media.DateTaken;
import common.media.VideoDateFinder;
import common.utils.Conversion;
import common.utils.FileNameParseUtils;
import common.utils.FileUtils;
import common.utils.ImageUtils;
import common.utils.date.DateUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

import static com.girbola.messages.Messages.sprintf;
import static common.media.DateTaken.getMetaDataCreationDate;
import static common.media.DateTaken.readMetaData;
import static common.utils.FileUtils.*;

/**
 * @author Marko Lokka
 */
public class FileInfoUtils {

    private final static String ERROR = FileInfoUtils.class.getSimpleName();

    public static String findFileInfoByDate(FileInfo fileInfoToSearch, TableView<FolderInfo> table) {
        LocalDate yearAndMonthToSearch = DateUtils.longToLocalDateTime(fileInfoToSearch.getDate()).toLocalDate();

        String year = Conversion.stringWithDigits(yearAndMonthToSearch.getYear(), 4);
        String month = Conversion.stringWithDigits(yearAndMonthToSearch.getMonthValue(), 2);
        String day = Conversion.stringWithDigits(yearAndMonthToSearch.getDayOfMonth(), 2);
        Messages.sprintf("year: " + year + " month " + month + " day" + day);
        List<FileInfo> list = Main.conf.getModel().getWorkDir_Handler()
                .findPossibleExistsFoldersInWorkdir(fileInfoToSearch);
        if (!list.isEmpty()) {
            FXMLLoader loader = null;
            Parent parent = null;
            try {
                loader = new FXMLLoader(Main.class.getResource("fxml/possibleFolderChooser.FXML"), Main.bundle);
                parent = loader.load();
                PossibleFolderChooserController pfcc = (PossibleFolderChooserController) loader.getController();
                SimpleStringProperty path = new SimpleStringProperty();
                pfcc.init(path);
                Stage stage = new Stage();
                Scene scene = new Scene(parent);
                stage.setScene(scene);
                stage.showAndWait();
                return path.get();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;

    }

    public static FileInfo createFileInfo(Path fileName) throws IOException {

        FileInfo fileInfo = null;
        if (FileUtils.supportedImage(fileName)) {
            fileInfo = new FileInfo(fileName.toString(), Main.conf.getId_counter().incrementAndGet());
            setImage(fileInfo);
            handleImageMetadata(fileName, fileInfo);
            String imageDifferenceHash = ImageUtils.calculateImagePHash(fileName.toAbsolutePath());
            Messages.sprintf("fileName.toAbsolutePath(): " + fileName.toAbsolutePath() + " 333imageDifferenceHash: " + imageDifferenceHash);
            fileInfo.setSize(Files.size(fileName));
            fileInfo.setImageDifferenceHash(imageDifferenceHash);
        } else if (FileUtils.supportedVideo(fileName)) {
            fileInfo = new FileInfo(fileName.toString(), Main.conf.getId_counter().incrementAndGet());
            setVideo(fileInfo);
            fileInfo.setSize(Files.size(fileName));
            boolean metaDataFound = getVideoDateTaken(fileName, fileInfo);
            if (!metaDataFound) {
                fileInfo.setBad(true);
            }
        } else if (FileUtils.supportedRaw(fileName)) {
            fileInfo = new FileInfo(fileName.toString(), Main.conf.getId_counter().incrementAndGet());
            setRaw(fileInfo);
            handleImageMetadata(fileName, fileInfo);
            fileInfo.setSize(Files.size(fileName));
            String imageDifferenceHash = ImageUtils.calculateRAWImagePHash(fileName.toAbsolutePath());
            fileInfo.setImageDifferenceHash(imageDifferenceHash);
        }
        return fileInfo;
    }

    public static void handleImageMetadata(Path fileName, FileInfo fileInfo) throws IOException {
        boolean metaDataFound = setImageMetadata(fileName, fileInfo);
        if (!metaDataFound) {
            boolean tryFileNameDate = tryFileNameDate(fileName, fileInfo);
            if (!tryFileNameDate) {
                setBad(fileInfo);
                fileInfo.setDate(0);
            }
        }
    }

    public static boolean getVideoDateTaken(Path path, FileInfo fileInfo) {
        if (!Files.exists(path)) {
            sprintf("File does not exists: " + path + " returning....");
            return false;
        }
        long date = 0;
        if (FileUtils.supportedVideo(path)) {
            setVideo(fileInfo);
            Metadata metaData = readMetaData(path);
            if (metaData != null) {

                date = getMetaDataCreationDate(metaData, path);
                if (date >= 1) {
                    FileInfoUtils.setGood(fileInfo);
                    fileInfo.setDate(date);
                    getImageThumb_Offset_Length(metaData, fileInfo);
                    return true;
                } else {
                    FileInfoUtils.setBad(fileInfo);
                    fileInfo.setDate(0);
                }
                // getImageThumb_Offset_Length(metaData, fileInfo);
            }

            boolean externalImageMetaDataFound = getDateThumbFileForVideo(path, fileInfo);
            if (externalImageMetaDataFound) {
                return true;
            } else {
                date = FileNameParseUtils.hasFileNameDate(path);
                if (date >= 1) {
                    fileInfo.setDate(date);
                    setGood(fileInfo);
                    fileInfo.setSuggested(true);
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * This method checks if the given video file has a corresponding thumbnail file with metadata,
     * and populates the FileInfo object with the metadata if found.
     *
     * @param path The path of the video file.
     * @param
     * @return
     */

    public static boolean getDateThumbFileForVideo(Path path, FileInfo fileInfo) {
        if (supportedVideo(path)) {
            Path THM_path = VideoDateFinder.hasTHMFile(path);
            if (THM_path == null) {
                return false;
            }
            if (Files.exists(THM_path)) {
                boolean metaDataFound = setImageMetadata(THM_path, fileInfo);
                if (metaDataFound) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean getImageThumb_Offset_Length(Metadata metaData, FileInfo fileInfo) {
        if (metaData == null) {
            return false;
        }
        ExifThumbnailDirectory directory = metaData.getFirstDirectoryOfType(ExifThumbnailDirectory.class);
        if (directory != null) {
            try {
                int offset = -1;
                int length = -1;
                if (directory.getInteger(ExifThumbnailDirectory.TAG_THUMBNAIL_OFFSET) == null) {
                    return false;
                } else {
                    offset = directory.getInteger(ExifThumbnailDirectory.TAG_THUMBNAIL_OFFSET);
                }
                if (directory.getInteger(ExifThumbnailDirectory.TAG_THUMBNAIL_LENGTH) == null) {
                    return false;
                } else {
                    length = directory.getInteger(ExifThumbnailDirectory.TAG_THUMBNAIL_LENGTH);
                }
                if (offset < 0 || length < 0) {
                    Messages.sprintfError("offset were: " + offset + " length: " + length);
                    return false;
                } else {
                    fileInfo.setThumb_offset(offset);
                    fileInfo.setThumb_length(length);
                    return true;
                }

            } catch (Exception e) {
                fileInfo.setThumb_offset(-1);
                fileInfo.setThumb_length(-1);
                return false;
            }
        }
        return false;
    }

    private static boolean tryFileNameDate(Path path, FileInfo fileInfo) {
        long fileNameDate = FileNameParseUtils.hasFileNameDate(path);
        if (fileNameDate != 0) {
            FileInfoUtils.setSuggested(fileInfo);
            fileInfo.setDate(fileNameDate);
            return true;
        } else {
            FileInfoUtils.setBad(fileInfo);
            fileInfo.setDate(0);
            fileNameDate = 0;
            return false;
        }
    }

    public static List<FileInfo> createFileInfo_list(FolderInfo folderInfo) {
        long start = System.currentTimeMillis();
        List<FileInfo> fileInfo_list = new ArrayList<>();
        DirectoryStream<Path> list = FileUtils.createDirectoryStream(Paths.get(folderInfo.getFolderPath()), filter_directories);

        Iterator<Path> listIterator = list.iterator();

        sprintf("newDirStream created took: " + (System.currentTimeMillis() - start));
        while (listIterator.hasNext()) {
            Path path = listIterator.next();
            if (Main.getProcessCancelled()) {
                // cancel();
                break;
            }

            try {
                if (ValidatePathUtils.validFile(path)) {
                    Messages.sprintf("validFile: " + path.toString());

                    FileInfo fileInfo = createFileInfo(path);
                    if (fileInfo != null) {
                        fileInfo_list.add(fileInfo);
                    } else {
                        Messages.sprintfError("fileInfo were null!");
                    }
                }
            } catch (IOException ex) {
                Messages.sprintf("createFileInfo_list had some issue(s): " + ex.getMessage());
                Messages.errorSmth(ERROR, "createFileInfo_list had some issue(s)", ex, Misc.getLineNumber(), true);
                Main.setProcessCancelled(true);
                break;
            }
        }
        sprintf("=======entire createFileInfo_list took: " + (System.currentTimeMillis() - start));
        return fileInfo_list;
    }

    public static void setSuggested(FileInfo fileInfo) {
        fileInfo.setSuggested(true);
        fileInfo.setBad(false);
        fileInfo.setGood(false);
    }

    public static void setGood(FileInfo fileInfo) {
        fileInfo.setSuggested(false);
        fileInfo.setBad(false);
        fileInfo.setGood(true);
        fileInfo.setSuggested(false);
    }

    public static void setConfirmable(FileInfo fileInfo) {
        // srth;
    }

    public static void setBad(FileInfo fileInfo) {
        fileInfo.setSuggested(false);
        fileInfo.setBad(true);
        fileInfo.setGood(false);
        fileInfo.setSuggested(false);
    }

    public static void setVideo(FileInfo fileInfo) {
        fileInfo.setImage(false);
        fileInfo.setRaw(false);
        fileInfo.setVideo(true);
    }

    public static void setImage(FileInfo fileInfo) {
        fileInfo.setImage(true);
        fileInfo.setRaw(false);
        fileInfo.setVideo(false);
    }

    public static void setRaw(FileInfo fileInfo) {
        fileInfo.setImage(false);
        fileInfo.setRaw(true);
        fileInfo.setVideo(false);
    }

    public static void sortByDate_Ascending(List<FileInfo> list) {
        Collections.sort(list, new Comparator<FileInfo>() {
            @Override
            public int compare(FileInfo fileInfo, FileInfo fileInfo2) {
                if (fileInfo.getDate() < fileInfo2.getDate()) {
                    return -1;
                } else if (fileInfo.getDate() > fileInfo2.getDate()) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });

        // TODO Auto-generated method stub
    }

    public static boolean setImageMetadata(Path path, FileInfo fileInfo) {

        long creationDate = 0;
        int orientation = 0;
        int width = 0;
        int height = 0;
        String camera_model = MDir_Constants.UNKNOWN.getType();
        Metadata metaData = null;
        fileInfo.setCamera_model(camera_model);
        try {
            metaData = readMetaData(path);
        } catch (Exception e) {
            return false;
        }
        if (metaData != null) {
            creationDate = getMetaDataCreationDate(metaData, path);
            if (creationDate != 0) {
                FileInfoUtils.setGood(fileInfo);
                fileInfo.setDate(creationDate);
            } else {
                FileInfoUtils.setBad(fileInfo);
                fileInfo.setDate(0);
            }
            orientation = DateTaken.getMetaDataOrientation(metaData);
            if (orientation != 0) {
                fileInfo.setOrientation(orientation);
            } else {
                fileInfo.setOrientation(0);
            }
            camera_model = DateTaken.getCameraModel(metaData);
            if (camera_model != null) {
                if (!camera_model.isEmpty()) {
                    fileInfo.setCamera_model(camera_model);
                }
            }
            getImageThumb_Offset_Length(metaData, fileInfo);

            if (creationDate != 0) {
                creationDate = 0;
                orientation = 0;
                camera_model = null;
                return true;
            } else {
                creationDate = 0;
                orientation = 0;
                camera_model = null;
                return false;
            }
        }
        creationDate = 0;
        orientation = 0;
        camera_model = null;
        return false;
    }

    public static Path renameFile(FileInfo fileInfoSrc, FolderInfo folderInfoDest) {
        final String prefix = "_";

        for (FileInfo fileInfoDest : folderInfoDest.getFileInfoList()) {
//            if (fileInfoSrc.getOrgPath().equals(fileInfoDest.getOrgPath()) && (fileInfoSrc.getSize() != fileInfoDest.getSize())) {
            if (!fileInfoSrc.getImageDifferenceHash().isEmpty() && !fileInfoDest.getImageDifferenceHash().isEmpty() && !fileInfoSrc.getImageDifferenceHash().equals(fileInfoDest.getImageDifferenceHash())) {

                Path sourceFile = Paths.get(fileInfoDest.getOrgPath());
                Path destFolder = Paths.get(fileInfoDest.getOrgPath()).getParent();

                Path destFile = null;

                String fileNameWithoutExtension = getFilenameWithoutExtension(sourceFile);
                String fileExtension = getFileExtension(sourceFile);

                for (int runningNumber = 2; runningNumber < folderInfoDest.getFileInfoList().size() + 3; runningNumber++) {
                    destFile = Paths.get(destFolder.toString(), fileNameWithoutExtension + prefix + "" + runningNumber + "." + fileExtension);
                    if (!Files.exists(destFile)) {
                        return destFile;
                    }
                }
            }
        }
        String fileName = Paths.get(fileInfoSrc.getOrgPath()).getFileName().toString();
        return Paths.get(folderInfoDest.getFolderPath(), fileName);
    }

    private static String getFilenameWithoutExtension(Path path) {
        return getFilenameWithoutExtension(path.getFileName().toString());
    }

    private static String getFilenameWithoutExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? filename : filename.substring(0, dotIndex);
    }

    private static String getFileExtension(Path path) {
        return getExtension(path.getFileName().toString());
    }

    private static String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? "" : filename.substring(dotIndex + 1);
    }

    /**
     * @param path
     * @param fileInfo
     * @return
     */
    public static Path renameFileToDate(Path path, FileInfo fileInfo) {
        // C:\Temp\image.jpg C:\Temp + 2019-09-03 12.31.22.jpg

        Path source = Paths
                .get(path.getParent().toString() + File.separator
                        + DateUtils.longToLocalDateTime(fileInfo.getDate())
                        .format(Main.simpleDates.getDtf_ymd_hms_minusDots_default())
                        + "." + FileUtils.getExtension(path));
        Messages.sprintf("source would be after: " + path + " to: " + source);
        return source;
    }

    // @formatter:off
	/**
	 * Returns 0 if good Returns 1 if conflict with workdir Returns 2 if copying is
	 * not possible because fileinfo workDir is null OR drive is not connected
	 * Returns 3
	 * 
	 * @param fileInfo
	 * @return
	 */
	// @formatter:on
    public static int checkWorkDir(FileInfo fileInfo) {
        if (Main.conf.getDrive_connected()) {
            if (!fileInfo.getWorkDir().equals("null")) {
                if (fileInfo.getWorkDir().contains(Main.conf.getWorkDir())
                        && Main.conf.getWorkDirSerialNumber().equals(fileInfo.getWorkDirDriveSerialNumber())) {
                    if (Files.exists(Paths.get(fileInfo.getWorkDir()))) {
                        return 0;
                    } else {
                        return 2;
                    }
                } else {
                    return 1;
                }
            } else {
                return 2;
            }
        } else {
            return 2;
        }
    }

    public static void ignoreFileInfoFromList(FileInfo fileInfo, FolderInfo folderInfo) {
        fileInfo.setIgnored(true);
    }

    public static boolean findDuplicates(FileInfo fileInfo, FolderInfo folderInfoList) {
        for (FileInfo fileInfo_dest : folderInfoList.getFileInfoList()) {
            if (Paths.get(fileInfo.getOrgPath()).getParent().toString()
                    .equals(Paths.get(fileInfo_dest.getOrgPath()).getParent().toString())) {
                if (fileInfo.getSize() == fileInfo_dest.getSize()) {
                    Messages.sprintf("fileInfo src: " + fileInfo.getOrgPath() + " size = " + fileInfo.getSize()
                            + " fileInfoDEST: " + fileInfo_dest.getOrgPath() + " size = " + fileInfo_dest.getSize());
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Find possible duplicates from FolderInfo's List<FileInfo>
     *
     * @param file
     * @param folderInfo
     * @return
     */
    public static FileInfo findFileInFolderInfo(Path file, FolderInfo folderInfo) {
        for (FileInfo fileInfo : folderInfo.getFileInfoList()) {
            if (file.toString().equals(fileInfo.getOrgPath())) {
                if (file.toFile().length() == fileInfo.getSize()) {
                    return fileInfo;
                }
            }
        }
        return null;
    }

    /**
     * Define if dest folder has duplicate files
     *
     * @param fileInfo
     * @param dest
     * @return
     */
    public static boolean defineDuplicateFile(FileInfo fileInfo, Path dest) {
        Messages.sprintf("DEST: " + dest + " dest is dir? " + dest.toFile().isDirectory());
        if (dest.toFile().isFile()) {
            Path tmp = dest.getParent();
            dest = tmp;
        }
        if (Files.exists(dest.getParent())) {
            File[] fileList = dest.toFile().listFiles();
            File fileToFind = new File(fileInfo.getOrgPath());
            for (File file : fileList) {
                Messages.sprintf("File is: " + file);
                if (file.length() == fileToFind.length()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Moves file according the FileInfo workdir path.
     *
     * @param fileInfo
     * @return
     */
    public static boolean moveFileToWorkDir(FileInfo fileInfo) {
        boolean skip = true;
        Path source = Paths.get(fileInfo.getOrgPath());
        Messages.sprintf("DEBUG1: " + source);

        if (!Files.exists(source) && !Files.exists(Paths.get(fileInfo.getWorkDir()))
                || Main.getProcessCancelled()) {
            Main.setProcessCancelled(true);
            return false;
        }
        if (fileInfo.getDestination_Path().isBlank() || fileInfo.getDestination_Path().isEmpty()) {
            Messages.warningText("Destination path is not definied");
            return false;
        }

        Path dest = FileUtils.getFileNameDate(fileInfo, fileInfo.getWorkDir());

        if (!Files.exists(dest.getParent())) {
            try {
                Files.createDirectories(dest.getParent());
            } catch (Exception e) {
                e.printStackTrace();
                Messages.sprintfError("Can't create directories: " + dest.getParent());
                Messages.errorSmth(ERROR, "Can't create directories", e, Misc.getLineNumber(), true);
                return false;
            }
        }
        try {
            if (Files.exists(dest) && Files.size(dest) != Files.size(source)) {
                try {
                    Path dest_temp = FileUtils.renameFile(source, dest);
                    if (dest_temp != null && !Files.exists(dest_temp)) {
                        dest = dest_temp;
                        skip = false;
                    } else {
                        skip = true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Main.setProcessCancelled(true);
                    return false;
                }
            } else {
                skip = false;
                Messages.sprintf("DST: " + dest + " source: " + source + " size: " + Files.size(source));
            }
        } catch (IOException e1) {
            e1.printStackTrace();
            return false;
        }

        if (!skip) {
            if (source != null && dest != null)
                try {
                    Path target = Files.move(source, dest);
                    Messages.sprintf("Source dest: " + source + " TARGET: " + target);
                } catch (IOException e) {
                    e.printStackTrace();
                    Main.setProcessCancelled(true);
                    return false;
                }
            return true;
        }
        return false;
    }

    public static String getFolderName(FileInfo fileInfo_ToFind) {
        Path path = Paths.get(fileInfo_ToFind.getOrgPath());
        if (!Files.isDirectory(path)) {
            return path.getParent().getFileName().toString();
        }
        return null;
    }

    public static boolean compareImageHashes(FileInfo fileInfo, FolderInfo folderInfo) {
        Iterator<FileInfo> iterator = folderInfo.getFileInfoList().iterator();
        while (iterator.hasNext()) {
            FileInfo fileInfo_dest = iterator.next();
            if (fileInfo.getOrgPath().equals(fileInfo_dest.getOrgPath())) {
                return true;
            }
            if (fileInfo.getImageDifferenceHash() == fileInfo_dest.getImageDifferenceHash()) {
                return true;
            }
        }
        return false;
    }


}