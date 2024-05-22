package com.girbola.controllers.folderscanner;

import com.girbola.controllers.main.Tables;
import com.girbola.controllers.main.tables.FolderInfo;

import java.io.File;
import java.util.List;

public class SelectedFolderUtils {

    public static boolean contains(List<SelectedFolder> selectedFolders, File folder) {
        for (SelectedFolder selectedFolder : selectedFolders) {
            if (selectedFolder.getFolder().equals(folder.toString())) {
                return true;
            }
        }
        return false;
    }

    public static boolean tableHasFolder(Tables tables, File folder) {
        for (FolderInfo folderInfo : tables.getSortIt_table().getItems()) {
            if (folderInfo.getFolderPath().equals(folder.toString())) {
                return true;
            }
        }
        for (FolderInfo folderInfo : tables.getSorted_table().getItems()) {
            if (folderInfo.getFolderPath().equals(folder.toString())) {
                return true;
            }
        }
        for (FolderInfo folderInfo : tables.getAsItIs_table().getItems()) {
            if (folderInfo.getFolderPath().equals(folder.toString())) {
                return true;
            }
        }
        return false;
    }
}
