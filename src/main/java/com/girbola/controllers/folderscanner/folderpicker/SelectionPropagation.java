package com.girbola.controllers.folderscanner.folderpicker;

import com.girbola.controllers.folderscanner.SelectedFolder;
import com.girbola.controllers.main.ModelMain;
import common.utils.FileUtils;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;

import java.nio.file.Path;
import java.nio.file.Paths;

public class SelectionPropagation {

    private static ModelMain modelMain;

    public static void installSelectionPropagation(CheckBoxTreeItem<Path> root, ModelMain modelMain) {
        SelectionPropagation.modelMain = modelMain;
        addRecursiveListener(root, (item, selected) -> {
            // Downward propagation: selecting a non-indeterminate parent sets children
            if (!item.isIndeterminate()) {
                setChildrenSelected(item, selected);
            }
            // Upward recompute
            updateParents(item);
        });
    }

    private static void addRecursiveListener(CheckBoxTreeItem<Path> item,
                                             TreeItemSelectionConsumer onChange) {
        ChangeListener<Boolean> selectedListener = (obs, oldV, newV) -> {
            if(newV) {
                modelMain.getSelectedFolders().getSelectedFolderScanner_obs().add(new SelectedFolder(item.getValue()));
            } else {
                modelMain.getSelectedFolders().getSelectedFolderScanner_obs().removeIf(sf -> FileUtils.equals(Paths.get(sf.getFolder()), item.getValue()));

            }
            onChange.accept(item, newV);
        };

        ChangeListener<Boolean> listener = (obs, oldV, newV) -> onChange.accept(item, newV);

        item.selectedProperty().addListener(listener);
        item.indeterminateProperty().addListener((obs, o, n) -> {
            updateParents(item);
        // boolean selected, boolean connected, String folder, boolean media
     //        modelMain.getSelectedFolders().getSelectedFolderScanner_obs().add(new SelectedFolder(item.getValue()));
        });

        // Attach to existing children
        for (TreeItem<Path> child : item.getChildren()) {
            if (child instanceof CheckBoxTreeItem<Path> cb) {
                addRecursiveListener(cb, onChange);
            }
        }

        // Attach to children as they are lazily added
        item.getChildren().addListener((ListChangeListener<TreeItem<Path>>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    for (TreeItem<Path> added : c.getAddedSubList()) {
                        if (added instanceof CheckBoxTreeItem<Path> cb) {
                            addRecursiveListener(cb, onChange);
                            // Inherit selection from parent if parent is fully selected
                            if (item.isSelected() && !item.isIndeterminate()) {
                                cb.setIndeterminate(false);
                                cb.setSelected(true);
                            }
                        }
                    }
                }
            }
        });
    }

    private static void setChildrenSelected(CheckBoxTreeItem<Path> parent, boolean selected) {
        for (TreeItem<Path> child : parent.getChildren()) {
            if (child instanceof CheckBoxTreeItem<Path> cb) {
                cb.setIndeterminate(false);
                cb.setSelected(selected);
                setChildrenSelected(cb, selected);
            }
        }
    }

    private static void updateParents(CheckBoxTreeItem<Path> item) {
        TreeItem<Path> p = item.getParent();
        while (p instanceof CheckBoxTreeItem<Path> parent) {
            int selectedCount = 0;
            int indeterminateCount = 0;
            int total = 0;

            for (TreeItem<Path> ch : parent.getChildren()) {
                if (ch instanceof CheckBoxTreeItem<Path> cb) {
                    total++;
                    if (cb.isIndeterminate()) indeterminateCount++;
                    else if (cb.isSelected()) selectedCount++;
                }
            }

            if (total == 0) {
                parent.setIndeterminate(false);
            } else if (selectedCount == total) {
                parent.setIndeterminate(false);
                parent.setSelected(true);
            } else if (selectedCount == 0 && indeterminateCount == 0) {
                parent.setIndeterminate(false);
                parent.setSelected(false);
            } else {
                parent.setIndeterminate(true);
                parent.setSelected(false);
            }

            p = parent.getParent();
        }
    }

    @FunctionalInterface
    private interface TreeItemSelectionConsumer {
        void accept(CheckBoxTreeItem<Path> item, boolean selected);
    }
}