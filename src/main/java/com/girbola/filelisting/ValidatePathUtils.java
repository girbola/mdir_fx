package com.girbola.filelisting;

import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import common.utils.FileUtils;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import static com.girbola.messages.Messages.errorSmth;
import static com.girbola.misc.Misc.getLineNumber;


public class ValidatePathUtils {

    private final static String ERROR = ValidatePathUtils.class.getSimpleName();
    private static long FILE_MIN_SIZE = 102400; // 100 KB

    protected final static String[] skippedFolderList_UNIX = {"bin", "dev", "lib", "libx32", "root", "snap", "swapfile", "usr", "boot", "etc", "lib32", "lost+found", "opt", "run", "some", "sys", "var", "cdrom", "lib64", "media", "proc", "sbin", "srv"};
    protected final static String[] skippedFolderList_WIN = {"$SysReset", "$Recycle.Bin", "RECYCLER", ".Trash", "Android", "AppData", "Boot", "Default", "Efi", "Intel", "Java", "NetBeansProjects", "OEM", "PerfLogs", "Program Files (x86)", "Program Files", "ProgramData", "Recycle", "Resource", "System Volume Information", "Windows", "source"};

    protected final static String[] skippedFolderList_OSX = {"$RECYCLE.BIN", ".DS_Store", "Applications", "Library", "Network", "Photos Library.photoslibrary", "System Volume Information", "System", "Users", "Volumes", "bin", "cores", "dev", "etc", "home", "lost+found", "opt", "private", "sbin", "tmp", "usr", "var"};

    private static final Set<String> SKIPPED_FOLDER_SET_OSX = Set.of(
            "$RECYCLE.BIN", ".DS_Store", "Applications", "Library", "Network",
            "Photos Library.photoslibrary", "System Volume Information", "System",
            "Users", "Volumes", "bin", "cores", "dev", "etc", "home", "lost+found",
            "opt", "private", "sbin", "tmp", "usr", "var"
    );

    public static boolean hasMediaFilesInFolder(Path path) {
        try (DirectoryStream<Path> directoryStream = FileUtils.createDirectoryStream(path, FileUtils.filter_directories)) {
            if (directoryStream == null) {
                return false;
            }

            for (Path file : directoryStream) {
                try {
                    if (ValidatePathUtils.validFile(file)) {
                        return true;
                    }
                } catch (IOException ex) {
                    Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public static boolean isRootDisk(Path path) {
        if (path.getRoot().equals(Paths.get(System.getProperty("user.home")).getRoot())) {
            return true;
        }
        return false;
    }

    public static boolean isInSkippedFolderList(Path file) {
        final String APP_INDICATOR = "app";
        final char HIDDEN_FILE_PREFIX = '.';

        String fileName = file.getFileName().toString();
        if (Files.isRegularFile(file)) {
            return false;
        }
        //Messages.sprintf("----isInSkippedFolderList Checking file: " + file.toString());

        // Check for Windows-specific conditions
        if (Misc.isWindows()) {
            if (!fileName.isEmpty()) {

                if (isHiddenFile(fileName, HIDDEN_FILE_PREFIX) || containsIgnoreCase(fileName, APP_INDICATOR)) {
                    Messages.sprintf("Windows-specific conditions for file: " + file.toString());
                    return true;
                }
            }
//            return isInSkippedFolderList(file.toString(), List.of(skippedFolderList_WIN));
            return isInSkippedFolderList(fileName, List.of(skippedFolderList_WIN));
        }

        // Check for Unix-specific conditions
        if (Misc.isUnix()) {
            Messages.sprintf("Checking Unix-specific conditions for file: " + file.toString());
            return isInSkippedFolderList(file.toString(), List.of(skippedFolderList_UNIX));
        }

        // Check for Mac-specific conditions
        if (Misc.isMac()) {
            Messages.sprintf("Checking Mac-specific conditions for file: " + file.toString());
            return isInSkippedFolderList(file.toString(), List.of(skippedFolderList_OSX));
        }

        // Log unsupported OS information
        logUnsupportedOS();
        return false;
    }

    // Utility method: checks for a hidden file
    private static boolean isHiddenFile(String fileName, char hiddenFilePrefix) {
        return fileName.charAt(0) == hiddenFilePrefix;
    }

    private static boolean isInSkippedFolderList(String filePath, List<String> skippedFolders) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }

        Path path = Paths.get(filePath);
        //Messages.sprintf("Checking path: " + path.toString());

        // Test if filePath is file or folder
        if (Files.isRegularFile(path)) {
            path = path.getParent();
        }

        String partName = path.getFileName().toString();

        for (String filter : skippedFolders) {
            if (partName.equalsIgnoreCase(filter)) {
                Messages.sprintf("!!!!!!!!!!!!!Skipped folder name found!!--------- Part: " + partName + " contains in SKIPPED_FOLDER");
                return true;
            }
        }
//        Messages.sprintf("REturning FALSE with path: " + path + " - isFile: " + isFile + ", isDirectory: " + isDirectory + " partName: ");
        return false;
    }

    // Utility method to check if one string contains another, ignoring case
    private static boolean containsIgnoreCase(String str, String subStr) {
        return str.toLowerCase().contains(subStr.toLowerCase());
    }

    // Logs unsupported OS information
    private static void logUnsupportedOS() {
        String osInfo = String.format("OS Detection - Windows: %b, Unix: %b, Mac: %b, SystemOS: %s",
                Misc.isWindows(), Misc.isUnix(), Misc.isMac(), System.getProperty("os.name"));
        Messages.sprintf("OS: " + osInfo);
        errorSmth(ERROR, osInfo, null, getLineNumber(), true);
    }

    public static boolean validFile(Path f) throws IOException {
        if (!Files.isReadable(f)) {
            return false;
        }
        if (Files.isHidden(f)) {
            return false;
        }
        if (!Files.exists(f)) {
            return false;
        }
        if (Files.size(f) < FILE_MIN_SIZE) {
            return false;
        }
        if (!FileUtils.supportedMediaFormat(f.toFile())) {
            return false;
        }
        if (isInSkippedFolderList(f.toAbsolutePath())) {
            return false;
        }
        return true;
//        return Files.isReadable(f) && !Files.isHidden(f) && Files.size(f) > FILE_MIN_SIZE && Files.exists(f) && FileUtils.supportedMediaFormat(f.toFile()) && isInSkippedFolderList(f.toAbsolutePath());
    }

    public static boolean acceptedFolder(Path f) throws IOException {
        boolean isValid = Files.isDirectory(f) && Files.exists(f) && Files.isReadable(f) && !Files.isHidden(f) && !isInSkippedFolderList(f);
        Messages.sprintf("Validating folder: " + f.toString() + " - isValid: " + isValid);
        return isValid;
    }

    /**
     * Checks if the given folder is inside the user's home directory (e.g. /Users/<user>/ on Mac).
     *
     * @param folder Path to check
     * @return true if the folder is inside the user's home directory, false otherwise
     */
    public static boolean isInUserHomeFolder(Path folder) {
        Path home = Paths.get(System.getProperty("user.home")).toAbsolutePath();
        Path absFolder = folder.toAbsolutePath();
        return absFolder.startsWith(home);
    }
}
