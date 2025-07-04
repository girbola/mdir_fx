
package com.girbola.controllers.main.selectedfolder;

import com.girbola.controllers.folderscanner.SelectedFolder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class SelectedFolderScanner {

    private ObservableList<SelectedFolder> selectedFolderScanner_obs = FXCollections.observableArrayList();

    public ObservableList<SelectedFolder> getSelectedFolderScanner_obs() {
        return this.selectedFolderScanner_obs;
    }

    public void add(SelectedFolder selectedFolder) {
        selectedFolderScanner_obs.add(selectedFolder);
    }

}
