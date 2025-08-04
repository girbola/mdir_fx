
package common.utils;

import com.girbola.Main;
import com.girbola.fileinfo.FileInfo;
import com.girbola.filelisting.CheckMediaExistenceInFolder;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import common.utils.date.DateUtils;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Iterator;

import static com.girbola.messages.Messages.sprintf;

/**
 * @author Marko
 */
public class FileUtils {

    private final static String[] SUPPORTED_VIDEO_FORMATS = {"3gp", "avi", "mov", "mp4", "mpg", "mkv"};
    private final static String[] SUPPORTED_IMAGE_FORMATS = {"png", "jpg", "jpeg", "gif", "bmp", "tiff", "tif"};
    private final static String[] SUPPORTED_RAW_FORMATS = {"cr2", "nef"};

    private final static String[] IGNORED_FORMATS = {"ini", "db", "exe", "sh", "dll", "sys", "java", "jar"};


    public static boolean getHasMedia(String folder) {
        return getHasMedia(Paths.get(folder).toFile());
    }

    public static boolean getHasMedia(File folder) {
        return CheckMediaExistenceInFolder.getAllMediaFiles(folder.toPath());
    }

    /**
     * fileSeparator hack replaces file paths "/" separator to "\\" using
     * replaceAll("/", "\\\\") method
     *
     * @param path
     * @return
     */
    public static String fileSeparatorMRL(Path path) {
        String value = path.toString();
        String newPath = "";
        if (Misc.isWindows()) {
            newPath = value.replaceAll("/", "\\\\");
        } else {
            newPath = path.toString();
        }
        return newPath;
    }

    /**
     * Renames a file from source path to destination path.
     *
     * @param srcFile the source file path
     * @param destFile the destination file path
     * @return the new path after renaming
     * @throws IOException if an I/O error occurs
     * @throws IllegalArgumentException if source or destination paths are null
     */
    public static Path renameFile(Path srcFile, Path destFile) throws IOException {
        if (srcFile == null || destFile == null) {
            throw new IllegalArgumentException("Source or destination file path cannot be null");
        }

        Path parentPath = destFile.getParent();
        if (parentPath == null || !Files.exists(parentPath)) {
            throw new IOException("Destination parent directory does not exist");
        }

        Messages.sprintf("Renaming file - source: " +  srcFile + Files.size(srcFile) + destFile + Files.size(destFile));

        if (Files.exists(destFile)) {
            if (Files.size(srcFile) == Files.size(destFile)) {
                throw new IOException(String.format("File already exists at destination: %s", destFile));
            }
            return rename(srcFile, destFile);
        }

        return Files.move(srcFile, destFile);
    }

    /**
     * Rename file to new file name if file exists and it is different size example:
     * IMG_2000.jpg would be IMG_2000_1.jpg or IMG_2000_2.jpg and so on
     *
     * @param srcFile
     * @param destFile
     * @return
     * @throws java.io.IOException
     */
    public static Path renameFile_(Path srcFile, Path destFile) throws IOException {

        Messages.sprintf("Renaming srcFile: " + srcFile.toFile().length() + " destFile: " + destFile.toFile().length());

//        String srcImageHash = ImageUtils.calculateImagePHash(srcFile);
//
//        String destImageHash = ImageUtils.calculateImagePHash(destFile);

        if(Files.exists(destFile.getParent())) {
            // Get FolderInfo
            // If not exists return null
        }
        Messages.sprintf("srcFile: " + srcFile + " destFile: " + destFile + "");

        if(Files.exists(destFile)) {
            if(Files.size(srcFile) == Files.size(destFile)) {
                Messages.sprintf("file did already exists at destination folder: " + srcFile + " destImageHash; " + destFile);
                return null;
            } else {
                Messages.sprintf("Files have same name but they differ with sizes");
                return rename(srcFile, destFile);
            }
        }
//        if (Files.size(srcFile) != Files.size(destFile) && Files.size(destFile)>0) {
//            Messages.sprintf("Files have same name but they differ with sizes");
//            return rename(srcFile, destFile);
//        } else {
//            Messages.sprintf("file did already exists at destination folder: " + srcFile + " destImageHash; " + destFile);
//            return null;
//        }
        return null;
    }

    private static Path rename(Path srcFile, Path destFile) {

        String prefix = "_";
        String fileName = "";
        String ext = getExtension(destFile);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(destFile.getParent(), FileUtils.filter_directories)) {

            int counter = 1;

            Iterator<Path> it = stream.iterator();

            while (it.hasNext()) {
                Path path = Paths.get(destFile.getParent().toString(), destFile.getFileName().toString()
                                .substring(0, destFile.getFileName().toString().lastIndexOf(".")) + prefix + String.valueOf(counter) + "." + ext);

                Messages.sprintf("fileName testing starting: " + path);

                counter++;

                if (Files.exists(path)) {
                    if (Files.size(srcFile) == Files.size(path)) {
                        Messages.sprintf("DUPLICATED. File existed: " + destFile + " filename: " + fileName);
                        return null;
                    }
                } else {
                    return path;
                }
            }
        } catch (IOException e) {
            Messages.sprintfError("Can't read directory: " + destFile);
            return null;
        }
        return srcFile;
    }

    public static DirectoryStream<Path> createDirectoryStream(Path path) {

        try {
            DirectoryStream<Path> paths = Files.newDirectoryStream(path);
            return paths;
        } catch (IOException ex) {
            Messages.sprintfError("folderHasFiles cannot read directory: " + path.toString());
            Messages.warningText("Cannot read directory: " + path.toString());
            return null;
        }
    }
    public static DirectoryStream<Path> createDirectoryStream(Path path, DirectoryStream.Filter<Path> filter_directories) {
        try {
            return Files.newDirectoryStream(path, filter_directories);
        } catch (IOException ex) {
            Messages.sprintfError("folderHasFiles cannot read directory: " + path.toString());
            Messages.warningText("Cannot read directory: " + path.toString());
            return null;
        }
    }

    public static DirectoryStream.Filter<Path> filter_directories = path -> !Files.isDirectory(path) && supportedMediaFormat(path.toFile());

    public static boolean compareFilesChecksums(Path src, Path dest) {
        try {
            String checkSumSrc = getCheckSumFromFile(src);
            String checkSumDest = getCheckSumFromFile(dest);

            if (checkSumSrc.equals(checkSumDest)) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public static String getCheckSumFromFile(Path file) {
        try {
            byte[] data = Files.readAllBytes(file);
            byte[] hash = MessageDigest.getInstance("MD5").digest(data);

            return new BigInteger(1, hash).toString(16);
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }/* FILE FORMATS START */

    /**
     * Checks if file supports image, raw or video formats
     *
     * @param file
     * @return
     */
    public static boolean supportedMediaFormat(File file) {
        String result;
        result = getExtension(file.getName());
        // sprintf("extension result is: " +result);
        for (String s : SUPPORTED_VIDEO_FORMATS) {
            if (result.toLowerCase().equals(s.toLowerCase())) {
                return true;
            }
        }
        for (String s : SUPPORTED_IMAGE_FORMATS) {
            if (result.toLowerCase().equals(s.toLowerCase())) {
                return true;
            }
        }

        for (String s : SUPPORTED_RAW_FORMATS) {
            if (result.toLowerCase().equals(s.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /* FILE FORMATS START */
    public static boolean supportedImage(File file) {
        String result = getExtension(file.getName());
        for (String s : SUPPORTED_IMAGE_FORMATS) {
            if (result.toLowerCase().equals(s.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public static boolean supportedImage(Path path) {
        String result = getExtension(path.getFileName());
        for (String s : SUPPORTED_IMAGE_FORMATS) {
            if (result.equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isTiff(File file) {
        String result;
        result = getExtension(file.getName());
        if (result.equalsIgnoreCase("tiff") || result.equalsIgnoreCase("tif")) {
            return true;
        }
        return false;
    }

    public static boolean ignoredFormats(File file) { // PNG, JPEG, BMP
        String result = getExtension(file.getName());
        for (String s : IGNORED_FORMATS) {
            if (result.toLowerCase().equals(s.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    public static boolean supportedRaw(File file) { // PNG, JPEG, BMP
        String result;
        result = getExtension(file.getName());
        for (String s : SUPPORTED_RAW_FORMATS) {
            if (result.toLowerCase().equals(s.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public static boolean supportedRaw(Path path) { // PNG, JPEG, BMP
        String result = getExtension(path.getFileName());
        for (String s : SUPPORTED_RAW_FORMATS) {
            if (result.toLowerCase().equals(s.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public static boolean otherFormat(File file) {
        String result;
        result = getExtension(file.getName());
        if (result.equals("ini") || result.equals("lnk") || result.equals("db")) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean otherImageFormats(File file) {
        String result;
        result = getExtension(file.getName());
        if (result.equals("")) {
            return true;
        }
        return false;
    }

    public static boolean supportedVideo(Path path) {
        String result = "";
        result = getExtension(path);
        for (String s : SUPPORTED_VIDEO_FORMATS) {
            if (result.toLowerCase().equals(s.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public static boolean nonsupportedVideoThumb(File file) {
        String result;
        result = getExtension(file.getName());
        if (result.equals("3gp")) {
            return true;
        }
        return false;
    }

    public static boolean videoFormat(File file) {
        String result;
        result = getExtension(file.getName());
        if (result.equals("3g2") || result.equals("3gp") || result.equals("asf") || result.equals("asx")
                || result.equals("avi") || result.equals("flv") || result.equals("mov") || result.equals("mp4")
                || result.equals("mpg") || result.equals("rm") || result.equals("swf") || result.equals("vob")
                || result.equals("wmv") || result.equals("mkv")) {
            return true;
        }
        return false;
    }

    public static boolean allFormats(File file) {

        if (supportedMediaFormat(file)) {
            return true;
        } else if (supportedRaw(file)) {
            return true;
        } else if (videoFormat(file)) {
            return true;
        }
        return false;
    }

    /**
     * Returns file extension name in lowercase
     *
     * @param path
     * @return
     */
    public static String getExtension(Path path) {
        int mid = path.getFileName().toString().lastIndexOf(".");
        return path.getFileName().toString().substring(mid + 1, path.getFileName().toString().length()).toLowerCase();
        // return ext;
    }

    /**
     * Returns file extension name in original casesensitiveness
     *
     * @param path
     * @return
     */
    public static String getFileExtension(Path path) {
        // int mid =
        return path.getFileName().toString().substring(path.getFileName().toString().lastIndexOf(".") + 1,
                path.getFileName().toString().length());
        // return ext;
    }

    /**
     * Returns file extension name in lowercase
     *
     * @param fileName
     * @return
     */
    public static String getExtension(String fileName) {
        File file = new File(fileName);
        String sourceFileName = file.getName();
        int mid = sourceFileName.lastIndexOf(".");
        String ext = sourceFileName.substring(mid + 1, sourceFileName.length()).toLowerCase();
        // String result = ext.toLowerCase();

        return ext;
    }

    /**
     * Returns just filename without extension e.x. C:\Temp\IMG_1234.JPG to IMG_1234
     *
     * @param path
     * @return
     */
    public static String parseExtension(Path path) {
        String file = path.getFileName().toString();
        int mid = file.lastIndexOf(".");
        String finalName = file.substring(0, mid);
        sprintf("parseExtension fileName: " + finalName);
        return finalName;
    }

    /**
     * Replaces from filepath a workdir to none. For example
     * c:\pictures\2019\09\pictures001.jpg to \2019\09\pictures001.jpg
     *
     * @param dest
     * @param workDir
     */
    public static String parseWorkDir(String dest, String workDir) {
        String parsedWorkDir_FileName = dest.replace(workDir, "");
        Messages.sprintf("parseWorkDir is: " + parsedWorkDir_FileName);
        return parsedWorkDir_FileName;
    }

    /**
     * @param fileInfo
     * @param workDir
     * @return
     */
    public static Path getFileNameDate(FileInfo fileInfo, String workDir) {
        LocalDate localDate = DateUtils.longToLocalDateTime(fileInfo.getDate()).toLocalDate();

        String fileName = DateUtils.longToLocalDateTime(fileInfo.getDate())
                .format(Main.simpleDates.getDtf_ymd_hms_minusDots_default());
        Path path = Paths.get(File.separator + localDate.getYear() + File.separator + fileName + "."
                + FileUtils.getFileExtension(Paths.get(fileInfo.getOrgPath())));
        fileInfo.setWorkDir(workDir);
        fileInfo.setDestination_Path(path.toString());

        return path;
    }

    /**
     * Generates an destination folder path IMG.JPG becomes yyyy-MM-dd HH.mm.ss -
     * <Location> - <Event>.JPG Also sets fileInfo as not copied
     *
     * @param fileInfo
     * @param workDir
     * @return
     */
    public static Path getFileNameDateWithEventAndLocation(FileInfo fileInfo, String workDir) {
        LocalDate localDate = DateUtils.longToLocalDateTime(fileInfo.getDate()).toLocalDate();
        String location_str = "";
        String event_str = "";
        fileInfo.setWorkDir(workDir);
        fileInfo.setWorkDirDriveSerialNumber(Main.conf.getWorkDirSerialNumber());

        // Location = "KAINUU" Event = ""
        if (!fileInfo.getLocation().isEmpty() && fileInfo.getEvent().isEmpty()) {
            location_str = " - " + fileInfo.getLocation();
            // Event = "KALASSA" Location = ""
        } else if (!fileInfo.getEvent().isEmpty() && fileInfo.getLocation().isEmpty()) {
            event_str = " - " + fileInfo.getEvent();
            // Event = "" Location = ""
        } else if (!fileInfo.getEvent().isEmpty() && !fileInfo.getLocation().isEmpty()) {
            location_str = " - " + fileInfo.getLocation();
            event_str = " - " + fileInfo.getEvent();
        }

        String fileName = DateUtils.longToLocalDateTime(fileInfo.getDate())
                .format(Main.simpleDates.getDtf_ymd_hms_minusDots_default());
        Path destPath = Paths.get(File.separator + localDate.getYear() + File.separator + localDate + location_str
                + event_str + File.separator + fileName + "."
                + FileUtils.getFileExtension(Paths.get(fileInfo.getOrgPath())));
        fileInfo.setDestination_Path(destPath.toString());

        return destPath;
    }

    public static void moveFile(FileInfo fileInfo, Path sourceFile, Path newPath) throws IOException {

        Path success = Files.move(sourceFile, newPath);
        if (Files.exists(success)) {
            sprintf("Showing ALL fileinfo values: " + fileInfo.showAllValues());
        }
    }

    public static boolean createFolders(Path newDestinationPath) {
        try {
            Files.createDirectories(newDestinationPath);
        } catch (IOException e) {
            boolean writable = Files.isWritable(newDestinationPath);
            Messages.warningText("Cannot create folders: " + newDestinationPath + " write access to folder is: " + writable);
            return false;
        }
        return true;
    }

    public static boolean setWritable(Path createFile, boolean b) {
        return createFile.toFile().setWritable(true);
    }

    public  static boolean setReadable(Path createFile, boolean b) {
        return createFile.toFile().setReadable(true);
    }
}
