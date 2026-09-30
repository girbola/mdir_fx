
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
        //selectedFolderScannerOriginal = new ArrayList<>(selectedFolderScanner_obs);
        selectedFolderScannerOriginal = selectedFolderScanner_obs.stream()
                // Luodaan kokonaan uudet oliot, jotta viittaukset katkeavat
                .map(f -> new SelectedFolder(f.getFolder(), f.isSelected(), f.isConnected(), f.isMedia(), f.isIgnored()))
                // .toList() palauttaa unmodifiable (muuttumattoman) listan
                .toList();
        Messages.sprintf("selectedFolders_backup_action pressed");
        for(SelectedFolder sf : selectedFolderScanner_obs){
            Messages.sprintf("Backing up folder: " + sf.getFolder() + " selected: " + sf.isSelected() + " connected: " + sf.isConnected() + " media: " + sf.isMedia() + " ignored: " + sf.isIgnored());
        }
    }

    public void restore() {
        selectedFolderScanner_obs.clear();
        selectedFolderScanner_obs.setAll(selectedFolderScannerOriginal);
    }

    public void save() {

        for(SelectedFolder folderInObs : selectedFolderScanner_obs) {
            Messages.sprintf("NOT SAVE folderInObs selectedFolders_save_action pressed: " + folderInObs.getFolder() + " selected: " + folderInObs.isSelected() + " connected: " + folderInObs.isConnected() + " media: " + folderInObs.isMedia() + " ignored: " + folderInObs.isIgnored());
        }

        for(SelectedFolder folderToSave : selectedFolderScannerOriginal) {
            Messages.sprintf("SAVING selectedFolders_save_action pressed: " + folderToSave.getFolder() + " selected: " + folderToSave.isSelected() + " connected: " + folderToSave.isConnected() + " media: " + folderToSave.isMedia() + " ignored: " + folderToSave.isIgnored());
            saveValuesToObs(folderToSave);
        }
    }

    private boolean saveValuesToObs(SelectedFolder folderToSave) {
        for (SelectedFolder folderInObs : selectedFolderScanner_obs) {
            if (folderInObs.getFolder().toString().equals(folderToSave.getFolder().toString())) {
                folderInObs.setSelected(folderToSave.isSelected());
                folderInObs.setConnected(folderToSave.isConnected());
                folderInObs.setMedia(folderToSave.isMedia());
                folderInObs.setIgnored(folderToSave.isIgnored());
                Messages.sprintf("Saved values to obs for folder: " + folderInObs.getFolder() + " selected: " + folderInObs.isSelected() + " connected: " + folderInObs.isConnected() + " media: " + folderInObs.isMedia() + " ignored: " + folderInObs.isIgnored());
                return true;
            }
        }
        return false;
    }

    public void add(SelectedFolder selectedFolderToFind) {
        Messages.sprintf("::::::::::::::::::::add selectedFolderToFind: " + selectedFolderToFind.getFolder());
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
