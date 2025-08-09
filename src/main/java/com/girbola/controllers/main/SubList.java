
package com.girbola.controllers.main;

import com.girbola.Main;
import com.girbola.filelisting.SubFolders;
import com.girbola.filelisting.ValidatePathUtils;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import javafx.concurrent.Task;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.List;

import static com.girbola.messages.Messages.sprintf;


public class SubList extends Task<List<Path>> {
    private final String ERROR = SubList.class.getSimpleName();

    private static List<Path> list = new ArrayList<>();
    private List<Path> selectedFolderScanner_list;

    public SubList(List<Path> selectedFolderScanner_list) {
        this.selectedFolderScanner_list = selectedFolderScanner_list;
    }

    private static void calculate(Path p) throws IOException {
        if (Files.isReadable(p)) {
            sprintf("IS Readable. SubList - calculate: " + p);
        }
        printFileInfo(p);

//        SubFolders subFolders = new SubFolders();
        List<Path> list = SubFolders.subFolders(p);

//		DirectoryStream<Path> ds = FileUtils.createDirectoryStream(p, FileUtils.filter_directories);
//		if(ds == null) {
//			Messages.sprintfError("Calculate has failed. Cannot read folder: " + p);
//		}
        for (Path path : list) {
            if (Main.getProcessCancelled()) {
                break;
            }
            if (ValidatePathUtils.validFolder(path)) {
                sprintf("----calculating: " + path);
                if (!SubList.list.contains(path)) {
                    SubList.list.add(path);
                    calculate(path);
                }
            }
        }
    }

    public static void printFileInfo(Path path) {
        try {
            // Check if the file exists
            if (Files.exists(path) && path.toString().contains("photos")) {
                System.out.println("File: " + path);

                // Check if it is readable
                System.out.println("Readable: " + Files.isReadable(path));

                // Check if it is writable
                System.out.println("Writable: " + Files.isWritable(path));

                // Check if it is executable
                System.out.println("Executable: " + Files.isExecutable(path));

                // Check if it is a regular file
                System.out.println("Regular file: " + Files.isRegularFile(path));

                // Check if it is a directory
                System.out.println("Directory: " + Files.isDirectory(path));

                // Check if it is a symbolic link
                System.out.println("Symbolic link: " + Files.isSymbolicLink(path));

                // Fetch file size
                System.out.println("File size: " + Files.size(path) + " bytes");

                // Get file owner
                System.out.println("Owner: " + Files.getOwner(path));

                // Get last modified time
                System.out.println("Last modified: " + Files.getLastModifiedTime(path));

                // Fetch file permissions (POSIX only)
                try {
                    PosixFileAttributes posixAttrs = Files.readAttributes(path, PosixFileAttributes.class);
                    System.out.println("Permissions: " + PosixFilePermissions.toString(posixAttrs.permissions()));
                } catch (UnsupportedOperationException e) {
                    System.out.println("POSIX attributes not supported on this system.");
                }

                // Check file status (e.g., hidden)
                System.out.println("Hidden: " + Files.isHidden(path));

            } else {
                System.out.println("The file " + path + " does not exist.");
            }

        } catch (IOException e) {
            System.err.println("Error retrieving file information: " + e.getMessage());
        }
    }

    @Override
    protected List<Path> call() throws Exception {
        for (Path p : selectedFolderScanner_list) {
            Messages.sprintf("PATHHHTHTH: " + p.toString());
            if (Main.getProcessCancelled()) {
                break;
            }
            if (ValidatePathUtils.hasMediaFilesInFolder(p)) {
                list.add(p);
            }
            try {
                calculate(p);
            } catch (IOException ex) {
                Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
            }
        }
        return list;
    }
}
