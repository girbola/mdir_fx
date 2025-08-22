package com.girbola.utils;

import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifThumbnailDirectory;
import com.drew.metadata.file.FileSystemDirectory;
import com.girbola.Main;
import com.girbola.controllers.datefixer.utils.MetadataField;
import com.girbola.controllers.folderscanner.SelectedFolder;
import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.selectedfolder.SelectedFolderScanner;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.drive.DriveInfo;
import com.girbola.drive.DriveInfoUtils;
import com.girbola.fileinfo.FileInfo;
import com.girbola.filelisting.GetAllMediaFiles;
import com.girbola.filelisting.ValidatePathUtils;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.sql.DriveInfoSQL;
import common.media.DateTaken;
import common.media.VideoDateFinder;
import common.utils.FileNameParseUtils;
import common.utils.FileUtils;
import common.utils.ImageUtils;
import common.utils.date.DateUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

import static com.girbola.messages.Messages.sprintf;
import static common.media.DateTaken.getMetaDataCreationDate;
import static common.media.DateTaken.readMetaData;
import static common.utils.FileUtils.*;


public class FileInfoUtils {

    private final static String ERROR = FileInfoUtils.class.getSimpleName();

    public static FileInfo createFileInfo(Path fileName) throws IOException {

        if (!Files.isRegularFile(fileName)) {
            Messages.sprintf("File were not a regular file: " + fileName);
            return null;
        }

        try {
            FileInfo fileInfo = new FileInfo(fileName.toString(), Main.conf.getId_counter().incrementAndGet());

            if (FileUtils.supportedImage(fileName)) {
                setImage(fileInfo);
// Get Size FileName Date ModifiredDate TakenDate

                        tryToGetCreationDateTime(fileName, fileInfo);

//                String imageDifferenceHash = ImageUtils.calculateImagePHash(fileName);
                String imageDifferenceHash = "";
                long start = System.currentTimeMillis();
                Metadata metaData = DateTaken.getMetaData(fileName);
                Messages.sprintf("***********metaData: " + fileName);

                if(metaData == null) {
                    Messages.sprintf("metaData were null!");
                    return null;
                }
                for (Directory directory : metaData.getDirectories()) {
                    if(directory.getName().equals("File")) {
                        Messages.sprintf("directory: " + directory.getName());
                        try {
                            String fileNameeee = directory.getString(FileSystemDirectory.TAG_FILE_NAME);
                            String fSize = directory.getString(FileSystemDirectory.TAG_FILE_SIZE);
                            String modified = directory.getString(FileSystemDirectory.TAG_FILE_MODIFIED_DATE);
                            Messages.sprintf("00000Filename" + fileName + "fileNameeee:::: " + fileNameeee + " (Long.parseLong(fSize) == Files.size(fileName):::: " + (Long.parseLong(fSize) == Files.size(fileName)) + " modified:::: " + modified);
                        } catch (Exception e) {

                        }

                    } else {
                        Messages.sprintf("ELLLSEEE::::: directory: " + directory.getName());
                    }

//                    if (directory.containsTag(FileSystemDirectory.TAG_FILE_NAME)) {
//                        for (Tag tag : directory.getTags()) {
//                            Messages.sprintf("=====================tag: " + tag + " DIRECTORY::: " + directory.toString());
//                        }
//
//                    }
                }

//                Messages.sprintf("fileName.toAbsolutePath(): " + fileName.toAbsolutePath() + " 333imageDifferenceHash: " + imageDifferenceHash);
//                fileInfo.setMetadata(metaData);
                fileInfo.setSize(Files.size(fileName));
//                fileInfo.setImageDifferenceHash(imageDifferenceHash);
            } else if (FileUtils.supportedVideo(fileName)) {
                setVideo(fileInfo);
                fileInfo.setSize(Files.size(fileName));
                boolean metaDataFound = getVideoDateTaken(fileName, fileInfo);
                if (!metaDataFound) {
                    fileInfo.setBad(true);
                }
            } else if (FileUtils.supportedRaw(fileName)) {
                setRaw(fileInfo);
                tryToGetCreationDateTime(fileName, fileInfo);
                String imageDifferenceHash = ImageUtils.calculateRAWImagePHash(fileName.toAbsolutePath());
                fileInfo.setImageDifferenceHash(imageDifferenceHash);
                fileInfo.setSize(Files.size(fileName));
            } else {
                Messages.sprintf("Cannot create FileInfo: " + fileName);
                Messages.sprintfError("Something went wrong and this file can't be created: " + fileName);
                return null;
            }

            fileInfo.setFileHistories(Arrays.asList(LocalDateTime.now() + " FileInfo created. PATH=" + fileInfo.getOrgPath()));

            return fileInfo;
        } catch (IOException e) {
            Messages.sprintfError("IOException while processing file: " + fileName + " - " + e.getMessage());
            throw e;
        }
    }

    public static void tryToGetCreationDateTime(Path fileName, FileInfo fileInfo) {
        try {
            boolean metaDataFound = setImageMetadata(fileName, fileInfo);
            if (!metaDataFound) {
                boolean tryFileNameDate = tryFileNameDate(fileName, fileInfo);
                if (!tryFileNameDate) {
                    setBad(fileInfo);
                    fileInfo.setDate(0);
                }
            }
        } catch (Exception e) {
            setBad(fileInfo);
            fileInfo.setDate(0);
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
                return null;
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
        String camera_model = MetadataField.UNKNOWN.getType();
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

        Path source = Paths.get(path.getParent().toString() + File.separator + DateUtils.longToLocalDateTime(fileInfo.getDate()).format(Main.simpleDates.getDtf_ymd_hms_minusDots_default()) + "." + FileUtils.getExtension(path));
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
                if (fileInfo.getWorkDir().contains(Main.conf.getWorkDir().toString()) && Main.conf.getWorkDirSerialNumber().equals(fileInfo.getWorkDirDriveSerialNumber())) {
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
            if (Paths.get(fileInfo.getOrgPath()).getParent().toString().equals(Paths.get(fileInfo_dest.getOrgPath()).getParent().toString())) {
                if (fileInfo.getSize() == fileInfo_dest.getSize()) {
                    Messages.sprintf("fileInfo src: " + fileInfo.getOrgPath() + " size = " + fileInfo.getSize() + " fileInfoDEST: " + fileInfo_dest.getOrgPath() + " size = " + fileInfo_dest.getSize());
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
     * Moves file according to the FileInfo workdir path.
     *
     * @param fileInfo the file information containing source and workdir details
     * @return {@code true} if the file was moved successfully, {@code false} otherwise
     */
    public static boolean moveFileToWorkDir(FileInfo fileInfo) {
        Path originalPath = Paths.get(fileInfo.getOrgPath());

        // Check initial conditions
        if (!Files.exists(originalPath) && !Files.exists(Paths.get(fileInfo.getWorkDir())) || Main.getProcessCancelled()) {
            Main.setProcessCancelled(true);
            return false;
        }
        if (fileInfo.getDestination_Path().isBlank()) {
            Messages.warningText("Destination path is not defined");
            return false;
        }

        Path destinationPath = FileUtils.getFileNameDate(fileInfo, fileInfo.getWorkDir());
        createDirectoriesIfNotExists(destinationPath);

        try {
            if (!Files.exists(destinationPath) || Files.size(destinationPath) == Files.size(originalPath)) {
                Messages.sprintf("DST: " + destinationPath + " source: " + originalPath + " size: " + Files.size(originalPath));
                return moveFile(originalPath, destinationPath);
            } else {
                Path tempDestinationPath = FileUtils.renameFile(originalPath, destinationPath);
                if (tempDestinationPath != null && !Files.exists(tempDestinationPath)) {
                    return moveFile(originalPath, tempDestinationPath);
                }
            }
        } catch (IOException e) {
            Messages.sprintfError("IOException thrown: " + e.getMessage());
            Main.setProcessCancelled(true);
            return false;
        }

        return false;
    }

    private static void createDirectoriesIfNotExists(Path path) {
        if (!Files.exists(path.getParent())) {
            try {
                if (Files.isWritable(path.getFileName())) {
                    Files.createDirectories(path.getParent());
                }
            } catch (IOException e) {
                Messages.sprintfError("Can't create directories: " + path.getParent());
                Messages.errorSmth(ERROR, "Can't create directories", e, Misc.getLineNumber(), true);
            }
        }
    }

    private static boolean moveFile(Path source, Path destination) {
        try {
            Path target = Files.move(source, destination);
            Messages.sprintf("Source: " + source + " TARGET: " + target);
            return true;
        } catch (IOException e) {
            Messages.sprintfError("IOException thrown: " + e.getMessage());
            return false;
        }
    }

    public static String getFolderName(FileInfo fileInfoToFind) {
        Path path = Paths.get(fileInfoToFind.getOrgPath());
        if (!Files.isDirectory(path)) {
            return path.getParent().getFileName().toString();
        }
        return null;
    }

    public static boolean compareImagesMetadata(FileInfo fileInfo, FolderInfo folderInfo) {
        Iterator<FileInfo> iterator = folderInfo.getFileInfoList().iterator();
        while (iterator.hasNext()) {
            FileInfo fileInfoDest = iterator.next();
            if (fileInfo.getOrgPath().equals(fileInfoDest.getOrgPath())) {
                return true;
            }
            if (Objects.equals(fileInfo.getImageDifferenceHash(), fileInfoDest.getImageDifferenceHash())) {
                return true;
            }
        }
        return false;
    }


    public static boolean compareImagesMetadata(FileInfo fileInfo, FileInfo duplicateFileInfo) {
        if (!fileInfo.getImageDifferenceHash().isEmpty()) {
            if (!duplicateFileInfo.getImageDifferenceHash().isEmpty()) {
                if (fileInfo.getImageDifferenceHash().equals(duplicateFileInfo.getImageDifferenceHash())) {
                    return true;
                }
            }
        }
        return fileInfo.getSize() == duplicateFileInfo.getSize();
    }

    public static void cleanList(List<FileInfo> fileInfoList) {
        Iterator<FileInfo> iterator = fileInfoList.iterator();
        while (iterator.hasNext()) {
            FileInfo fileInfo = iterator.next();
            Path path = Paths.get(fileInfo.getOrgPath());
            if (Files.exists(path)) {
                iterator.remove();
            }
        }
    }

    public static List<Path> checkFolderForChangedFilesAndFolder(FolderInfo folderInfo) {
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

    private static boolean handleEmptySerialNumber(FolderInfo folderInfo) {
        boolean isDriveFound = false;
        try {
            List<DriveInfo> driveInfos = DriveInfoSQL.loadDriveInfos();
            if (driveInfos == null || driveInfos.isEmpty()) {
                return false;
            }

            Path folderToFindPath = Paths.get(folderInfo.getFolderPath());
            for (DriveInfo driveInfo : driveInfos) {
                if (hasPath(driveInfo, folderToFindPath)) {
                    isDriveFound = true;
                    break;
                }
            }
        } catch (Exception e) {
            Messages.sprintfError("Error in handleEmptySerialNumber: " + e.getMessage());
            return false;
        } finally {
            DriveInfoSQL.closeConnection();
        }

        return isDriveFound;
    }

    private static boolean hasPath(DriveInfo driveInfo, Path pathToFind) {
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


    /**
     * Checks and updates folder path changes for the given FolderInfo
     *
     * @param folderInfo The folder information to check and update
     */
    private boolean checkFolderPathChanges(FolderInfo folderInfo) {
        try {
            if (folderInfo.getIgnored()) {
                Messages.sprintf("checkFolderPathChanges was ignored");
                return false;
            }

            Messages.sprintf("checkFolderPathChanges started");

            Path folderInfoFolderPath = Paths.get(folderInfo.getFolderPath());
            if (!Files.exists(folderInfoFolderPath)) {
                Messages.sprintfError("Folder does not exist: " + folderInfoFolderPath);
                return false;
            }

            String sourceFolderSerialNumber = folderInfo.getSourceFolderSerialNumber();
            if (sourceFolderSerialNumber == null || sourceFolderSerialNumber.isEmpty()) {
                boolean hasEmptySerialNumber = handleEmptySerialNumber(folderInfo);
                if (hasEmptySerialNumber) {
                    folderInfo.setChanged(true);
                    return true;
                }
                return false;
            }

            return false;
        } catch (Exception e) {
            Messages.sprintfError("Error in checkFolderPathChanges for path " + folderInfo.getFolderPath() + ": " + e.getMessage());
            return false;
        } finally {
            Messages.sprintf("checkFolderPathChanges finished");
        }
    }

    private static void checkFolderPathChanges_(ModelMain modelMain, FolderInfo folderInfo) {
        Messages.sprintf("checkFolderPathChanges started");
        String folderPath = folderInfo.getFolderPath();
        List<DriveInfo> driveInfoList = DriveInfoSQL.loadDriveInfos();
//        modelMain.getSqlConfigurationHandler().getDriveInfoList();

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
