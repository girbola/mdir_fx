package com.girbola.controllers.folderscanner;

import com.girbola.Main;
import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.selectedfolder.SelectedFolderScanner;
import com.girbola.messages.Messages;
import common.utils.FileUtils;
import java.util.Iterator;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBoxTreeItem;

import java.nio.file.Path;

import static com.girbola.messages.Messages.sprintf;

public class CustomCheckBoxTreeItem<S> extends CheckBoxTreeItem<String> {
    private ModelMain modelMain;
    private final Path path;

    public CustomCheckBoxTreeItem(ModelMain modelMain, Path path) {
        super(path.getFileName().toString());
        this.modelMain = modelMain;
        this.path = path;
        // Update the item when the checkbox is selected/deselected
       // this.selectedProperty().addListener((observable, oldValue, newValue) -> updateItem(newValue));
    }

    private void handleWorkDirConflict() {
        Platform.runLater(() -> {
            setSelected(false);
            Messages.warningText(Main.bundle.getString("workDirConflict"));
            modelMain.getSelectedFolders().getSelectedFolderScanner_obs().remove(path);
        });
    }

    private void handleSelection(boolean hasMedia) {
        modelMain.getSelectedFolders().add(new SelectedFolder(true, true, path.toString(), hasMedia));
        Messages.sprintf("Selected: " + path.toFile().getAbsolutePath());

        if (!hasSelectedFolder(path)) {
            modelMain.getSelectedFolders().add(new SelectedFolder(true, true, path.toString(), FileUtils.getHasMedia(path.toString())));
            Messages.sprintf("Folder added: " + path.toString());
        } else {
            Messages.sprintf("Folder already exists: " + path.toString());
        }

    }

    private boolean hasSelectedFolder(Path path) {
        for (SelectedFolder selectedFolders : modelMain.getSelectedFolders().getSelectedFolderScanner_obs()) {
            Messages.sprintf("SEFFF: " + selectedFolders.getFolder() + " TO FIND IS: " + path.toFile().getAbsolutePath());
            if (path.toFile().getAbsolutePath().equals(selectedFolders.getFolder())) {
                Messages.sprintf("Deselect path found: " + selectedFolders.getFolder() + " Gonna remove it");
                return true;
            }
        }
        return false;
    }

    private void handleDeselection() {
        Messages.sprintf("Deselected: " + path.toFile().getAbsolutePath());

//        modelMain.getSelectedFolders().getSelectedFolderScanner_obs().remove(path.toFile().getAbsolutePath());

        Iterator<SelectedFolder> it = modelMain.getSelectedFolders().getSelectedFolderScanner_obs().iterator();
        while (it.hasNext()) {
            SelectedFolder selectedFolders = it.next();
            if (path.toFile().getAbsolutePath().equals(selectedFolders.getFolder())) {
                Messages.sprintf("Deselect path found: " + selectedFolders.getFolder() + " Gonna remove it");
                it.remove();
            }
        }
    }

    // Custom update logic
    private void updateItem(Boolean isSelected) {
        if (Main.conf.getWorkDir().contains(path.toString())) {
            handleWorkDirConflict();
            return;
        }

        sprintf("cb.selectedProperty path is: " + isSelected);

        if (isSelected) {
            SelectedFolderScanner selectedFolders = modelMain.getSelectedFolders();
            for (SelectedFolder sef : selectedFolders.getSelectedFolderScanner_obs()) {
                Messages.sprintf("SEFFF: " + sef.getFolder());
            }
            boolean hasMedia = FileUtils.getHasMedia(path.toFile());
            handleSelection(hasMedia);
        } else {
            handleDeselection();
        }
    }
}