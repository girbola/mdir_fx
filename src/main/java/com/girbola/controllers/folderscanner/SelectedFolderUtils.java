package com.girbola.controllers.folderscanner;

import com.girbola.controllers.main.Tables;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.messages.Messages;
import javafx.scene.control.TableView;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class SelectedFolderUtils {

    /**
     * Checks if the specified folder is present in the list of selected folders.
     *
     * @param selectedFolders The list of SelectedFolder objects to check.
     * @param folder          The File object representing the folder to find.
     * @return true if the folder is present in the list of selected folders, false otherwise.
     */
    public static boolean contains(List<SelectedFolder> selectedFolders, File folder) {
        for (SelectedFolder selectedFolder : selectedFolders) {
            if (selectedFolder.getFolder().equals(folder.toString())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the given table has a folder with the specified path.
     *
     * @param tables The tables object representing the table.
     * @param folder The folder to check for existence in the table.
     * @return {@code true} if the table has the folder, {@code false} otherwise.
     */
    public static boolean tableHasFolder(Tables tables, Path folder) {

        List<TableView<FolderInfo>> allTables = TableUtils.getAllTables(tables);

        for (TableView<FolderInfo> tb : allTables) {
            for (FolderInfo folderInfo : tb.getItems()) {
                if (folderInfo.getFolderPath().equals(folder.toString())) {
                    Messages.sprintf("Folder did exists: " + folder);
                    return true;
                }
            }
        }
        return false;
    }
}
