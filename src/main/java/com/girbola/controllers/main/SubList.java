
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

    @Override
    protected List<Path> call() throws Exception {
        Messages.sprintf("SubList.call()");

        if (selectedFolderScanner_list == null) {
            Messages.sprintfError("selectedFolderScanner_list is null.");
            return list;
        }

        for (Path p : selectedFolderScanner_list) {
            Messages.sprintf("PATHHHTHTH: " + p.toString());
            if (Main.getProcessCancelled()) {
                break;
            }
            if (ValidatePathUtils.hasMediaFilesInFolder(p) && ValidatePathUtils.acceptedFolder(p)) {
                list.add(p);
            }
            try {
                collectSubFoldersRecursively(p);
            } catch (IOException ex) {
                Messages.sprintfError("SubList.call() IOException: " + ex.getMessage());
                Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
            }
        }
        return list;
    }


    private static void collectSubFoldersRecursively(Path p) throws IOException {
        if (Files.isReadable(p)) {
            sprintf("IS Readable. SubList - calculate: " + p);
        }
//        printFileInfo(p);

//        SubFolders subFolders = new SubFolders();
        List<Path> list = SubFolders.subFolders(p);
        for (Path path : list) {
            if (Main.getProcessCancelled()) {
                break;
            }
            if (ValidatePathUtils.acceptedFolder(path)) {
                sprintf("----calculating: " + path);
                if (!SubList.list.contains(path)) {
                    SubList.list.add(path);
                    collectSubFoldersRecursively(path);
                }
            }
        }
    }

    @Override
    protected void succeeded() {
        Messages.sprintf("SubList.succeeded()");
        super.succeeded();
    }

    @Override
    protected void cancelled() {
        Messages.sprintf("SubList.cancelled()");
        super.cancelled();
    }

    @Override
    protected void failed() {
        Messages.sprintf("SubList.failed()");
        super.failed();
    }
}
