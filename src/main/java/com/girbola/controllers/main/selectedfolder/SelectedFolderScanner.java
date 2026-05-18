
package com.girbola.controllers.main.selectedfolder;

import com.girbola.controllers.folderscanner.SelectedFolder;
import com.girbola.messages.Messages;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class SelectedFolderScanner {

    private ObservableList<SelectedFolder> homeDefaultsFolders_obs = FXCollections.observableArrayList();
    private ObservableList<SelectedFolder> selectedFolderScanner_obs = FXCollections.observableArrayList();
    private List<SelectedFolder> selectedFolderScannerOriginal = new ArrayList<>();

    public ObservableList<SelectedFolder> getSelectedFolderScanner_obs() {
        return this.selectedFolderScanner_obs;
    }

    public ObservableList<SelectedFolder> getHomeDefaultsFolders_obs() { return this.homeDefaultsFolders_obs; }

    public List<SelectedFolder> getSelectedFolderScannerOriginal() {
        return selectedFolderScannerOriginal;
    }

    public void backup() {
        selectedFolderScannerOriginal = new ArrayList<>(selectedFolderScanner_obs);
    }

    public void restore() {
        selectedFolderScanner_obs.clear();
        selectedFolderScanner_obs.setAll(selectedFolderScannerOriginal);
    }

    public void add(SelectedFolder selectedFolderToFind) {
        Messages.sprintf("::::::::::::::::::::add selectedFolderToFind: " + selectedFolderToFind.getFolder());;
        // Only add if selectedFolderToFind is not null and not already in the list
        if (selectedFolderToFind != null) {
            Iterator<SelectedFolder> iterator = selectedFolderScanner_obs.iterator();
            while (iterator.hasNext()) {
                SelectedFolder selectedFolder = iterator.next();
                Messages.sprintf("selectedFolder:::::::::::: " + selectedFolder.getFolder() + " selectedFolderToFind::::: " + selectedFolderToFind.getFolder());
                if (selectedFolder.getFolder().toString().equals(selectedFolderToFind.getFolder().toString())) {
                    Messages.sprintf("======Duplicate found!: " + selectedFolderToFind.getFolder() + " found in list " + " BREAKING!!!!");
                    break;
                } else {
                    Messages.sprintf("*************add new folder: " + selectedFolderToFind.getFolder() + " found in list");
                    Platform.runLater(() -> {
                        selectedFolderScanner_obs.add(selectedFolderToFind);
                    });
                }
            }
        }
    }


}
