package com.girbola.filelisting;

import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import common.utils.FileUtils;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.girbola.messages.Messages.errorSmth;
import static com.girbola.misc.Misc.getLineNumber;


public class ValidatePathUtils {

    private final static String ERROR = ValidatePathUtils.class.getSimpleName();
    private static long FILE_MIN_SIZE = (1 * 1024);

    public final static String[] skippedFolderList_UNIX = {"bin", "dev", "lib", "libx32", "root", "snap", "swapfile", "usr", "boot", "etc", "lib32", "lost+found", "opt", "run", "some", "sys", "var", "cdrom", "lib64", "media", "proc", "sbin", "srv"};
    public final static String[] skippedFolderList_WIN = {"$SysReset", "$Recycle.Bin", "RECYCLER", ".Trash", "Android", "AppData", "Boot", "Default", "Efi", "Intel", "Java", "NetBeansProjects", "OEM", "PerfLogs", "Program Files (x86)", "Program Files", "ProgramData", "Recycle", "Resource", "System Volume Information", "Windows", "source"};
    public static final String[] skippedFolderList_OSX = {"$RECYCLE.BIN", ".DS_Store", "Applications", "Library", "Network", "Photos Library", "Photos Library.photoslibrary", "System Volume Information", "System", "Users", "Volumes", "bin", "cores", "dev", "etc", "home", "lost+found", "opt", "private", "sbin", "tmp", "usr", "var"};

    public static boolean hasMediaFilesInFolder(Path path) {
        DirectoryStream<Path> directoryStream = FileUtils.createDirectoryStream(path, FileUtils.filter_directories);
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
                // Logger.getLogger(ValidatePathUtils.class.getName()).log(Level.SEVERE, null,
                // ex);
            }
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

        // Check for Windows-specific conditions
        if (Misc.isWindows()) {
            if (isHiddenFile(fileName, HIDDEN_FILE_PREFIX) || containsIgnoreCase(fileName, APP_INDICATOR)) {
                return true;
            }
            return isInSkippedFolderList(file.toString(), List.of(skippedFolderList_WIN));
        }

        // Check for Unix-specific conditions
        if (Misc.isUnix()) {
            return isInSkippedFolderList(file.toString(), List.of(skippedFolderList_UNIX));
        }

        // Check for Mac-specific conditions
        if (Misc.isMac()) {
            return isInSkippedFolderListIgnoreCase(file.toString(), List.of(skippedFolderList_OSX));
        }

        // Log unsupported OS information
        logUnsupportedOS();
        return false;
    }

    // Utility method: checks for a hidden file
    private static boolean isHiddenFile(String fileName, char hiddenFilePrefix) {
        return fileName.charAt(0) == hiddenFilePrefix;
    }

    // Utility method: checks if a file is in a skipped folder list with exact match
    private static boolean isInSkippedFolderList(String filePath, List<String> skippedFolders) {
        for (String folder : skippedFolders) {
            if (filePath.equalsIgnoreCase(folder)) {
                return true;
            }
        }
        return false;
    }

    // Utility method: checks if a file is in a skipped folder list (case-insensitive contains)
    private static boolean isInSkippedFolderListIgnoreCase(String filePath, List<String> skippedFolders) {
        for (String folder : skippedFolders) {
            if (filePath.toLowerCase().contains(folder.toLowerCase())) {
                return true;
            }
        }
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

    /**
     * isInSkippedFolderList check if current folder is in skipped list
     *
     * @param file
     * @return
     */
    public static boolean isInSkippedFolderList_(Path file) {
        if (Misc.isWindows()) {
            if (file.getFileName().toString().charAt(0) == '.') {
                return true;
            }
            if (file.getFileName().toString().toLowerCase().contains("app")) {
                return true;
            }
            for (String skippedFolder : skippedFolderList_WIN) {
                if (file.toString().equalsIgnoreCase(skippedFolder)) {
                    return true;
                }
            }
            return false;
        } else if (Misc.isUnix()) {
            for (String skippedFolder : skippedFolderList_UNIX) {
                if (file.toString().equalsIgnoreCase(skippedFolder)) {
                    return true;
                }
            }
            return false;
        } else if (Misc.isMac()) {
            for (String skippedFolder : skippedFolderList_OSX) {
                if (file.toString().toLowerCase().contains(skippedFolder.toLowerCase())) {
                    return true;
                }
            }
            return false;
        } else {
            String os = "isWindows: " + Misc.isWindows() + " UNIX: " + Misc.isUnix() + " Mac: " + Misc.isMac() + " UNIII: " + System.getProperty("os.name");
            Messages.sprintf("OS: " + os);
            errorSmth(ERROR, os, null, getLineNumber(), true);
        }

        return false;
    }

    public static boolean validFile(Path f) throws IOException {
        return Files.isReadable(f) && !Files.isHidden(f) && Files.size(f) > FILE_MIN_SIZE && Files.exists(f) && FileUtils.supportedMediaFormat(f.toFile());
    }

    public static boolean validFolder(Path f) throws IOException {
        return Files.isDirectory(f) && Files.exists(f) && Files.isReadable(f) && !Files.isHidden(f) && !isInSkippedFolderList(f);
    }

}
