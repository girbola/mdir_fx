package com.girbola.controllers.folderscanner;

import java.io.File;
import java.util.List;

public class SelectedFolderUtils {

    public static boolean contains(List<SelectedFolder> selectedFolders, File folder) {
        for(SelectedFolder selectedFolder : selectedFolders) {
            if(selectedFolder.getFolder().equals(folder.toString())) {
                return true;
            }
        }
        return false;
    }

}
