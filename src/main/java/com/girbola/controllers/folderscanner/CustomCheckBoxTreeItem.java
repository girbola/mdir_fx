package com.girbola.controllers.folderscanner;

import com.girbola.Main;
import com.girbola.controllers.main.Model_main;
import com.girbola.messages.Messages;
import common.utils.FileUtils;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBoxTreeItem;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.girbola.messages.Messages.sprintf;

public class CustomCheckBoxTreeItem<S> extends CheckBoxTreeItem<String> {
    private Model_main modelMain;
    private final Path path;

    public CustomCheckBoxTreeItem(Model_main modelMain, Path path) {
        super(path.getFileName().toString());
        this.modelMain = modelMain;
        this.path = path;
        // Update the item when the checkbox is selected/deselected
        this.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                updateItem(newValue);
            }
        });
    }

    // Custom update logic
    private void updateItem(Boolean selected) {
        if (Main.conf.getWorkDir().contains(path.toString())) {
            Platform.runLater(() -> {
                setSelected(false);
                Messages.warningText(Main.bundle.getString("workDirConflict"));
                modelMain.getSelectedFolders().getSelectedFolderScanner_obs().remove(path);
            });
        } else {
            sprintf("cb.selectedProperty path is: " + selected);
            if (selected) {
                boolean hasMedia = FileUtils.getHasMedia(path.toFile());
                modelMain.getSelectedFolders().add(new SelectedFolder(true, true, path.toString(), hasMedia));
            }
        }

        if (selected) {
            // Perform some action when selected
            System.out.println("Selected: " + path.toFile().getAbsolutePath());
        } else {
            // Perform some action when deselected
            System.out.println("Deselected: " + path.toFile().getAbsolutePath());
        }
    }
}