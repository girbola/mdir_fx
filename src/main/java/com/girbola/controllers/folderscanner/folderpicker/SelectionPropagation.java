package com.girbola.controllers.folderscanner.folderpicker;

import com.girbola.controllers.folderscanner.SelectedFolder;
import com.girbola.controllers.main.ModelMain;
import common.utils.FileUtils;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;

public class SelectionPropagation {

    private static ModelMain modelMain;
    private static boolean internalSelectionUpdate = false;

//    public static void syncTreeFromModel(ModelMain modelMain) {
//
//        CheckBoxTreeItem<Path> root = modelMain.getFolderScannerController().getDrives_rootItem();
//
//        SelectionPropagation.modelMain = modelMain;
//        addRecursiveListener(root, (item, selected) -> {
//            internalSelectionUpdate = true;
//            try {
//                if (!item.isIndeterminate()) {
//                    setChildrenSelected(item, selected);
//                }
//                updateParents(item);
//            } finally {
//                internalSelectionUpdate = false;
//            }
//        });
//    }

    public static void installSelectionPropagation(ModelMain modelMain) {
        CheckBoxTreeItem<Path> root = modelMain.getFolderScannerController().getDrives_rootItem();

        SelectionPropagation.modelMain = modelMain;
        addRecursiveListener(root, (item, selected) -> {
            internalSelectionUpdate = true;
            try {
                if (!item.isIndeterminate()) {
                    setChildrenSelected(item, selected);
                }
                updateParents(item);
            } finally {
                internalSelectionUpdate = false;
            }
        });
        Platform.runLater(() -> {
            // populate tree items first
            // root.getChildren().addAll(...);

            SelectionPropagation.syncTreeFromModel(modelMain);
        });
//        SelectionPropagation.syncTreeFromModel(modelMain);
    }

    public static void syncTreeFromModel(ModelMain modelMain) {
        CheckBoxTreeItem<Path> root = modelMain.getFolderScannerController().getDrives_rootItem();

        SelectionPropagation.modelMain = modelMain;
        internalSelectionUpdate = true;
        try {
            applyModelSelectionRecursively(root);
        } finally {
            internalSelectionUpdate = false;
        }
    }

    private static void applyModelSelectionRecursively(CheckBoxTreeItem<Path> item) {
        if (item == null) {
            return;
        }

        applyModelSelectionToItem(item);

        for (TreeItem<Path> child : item.getChildren()) {
            if (child instanceof CheckBoxTreeItem<Path> cb) {
                applyModelSelectionRecursively(cb);
            }
        }
    }

    public static void removeFromSelection(SelectedFolder selectedFolder) {
        if (selectedFolder == null || selectedFolder.getFolder() == null) {
            return;
        }

        String removedPath = selectedFolder.getFolder();

        Iterator<SelectedFolder> it = modelMain.getSelectedFolders().getSelectedFolderScanner_obs().iterator();
        while (it.hasNext()) {
            SelectedFolder current = it.next();
            String currentPath = current.getFolder();

            if (currentPath == null) {
                continue;
            }

            if (currentPath.equals(removedPath) || currentPath.startsWith(removedPath + java.io.File.separator)) {
                it.remove();
            }
        }

        // refresh tree so parents/intermediate folders update correctly
        SelectionPropagation.syncTreeFromModel(modelMain);
    }


    private static void addRecursiveListener(CheckBoxTreeItem<Path> item,
                                             TreeItemSelectionConsumer onChange) {
        ChangeListener<Boolean> selectedListener = (obs, oldV, newV) -> {
            if (newV == null) {
                return;
            }

            Path value = item.getValue();
            if (value != null) {
                String pathString = value.toString();

                if (!internalSelectionUpdate) {
                    if (newV) {
                        if (!isPathInSelectedFolders(pathString)) {
                            modelMain.getSelectedFolders().getSelectedFolderScanner_obs()
                                    .add(SelectedFolder.create(true, true, pathString, FileUtils.getHasMedia(pathString), false));
                        }
                    } else {
                        modelMain.getSelectedFolders().getSelectedFolderScanner_obs()
                                .removeIf(selectedFolder -> selectedFolder.getFolder().equals(pathString));
                    }
                }
            }

            if (internalSelectionUpdate) {
                return;
            }
            onChange.accept(item, newV);
        };

        item.selectedProperty().addListener(selectedListener);
        item.indeterminateProperty().addListener((obs, o, n) -> {
            internalSelectionUpdate = true;
            try {
                updateParents(item);
            } finally {
                internalSelectionUpdate = false;
            }
        });

        for (TreeItem<Path> child : item.getChildren()) {
            if (child instanceof CheckBoxTreeItem<Path> cb) {
                addRecursiveListener(cb, onChange);
                applyModelSelectionToItem(cb);
            }
        }

        item.getChildren().addListener((ListChangeListener<TreeItem<Path>>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    for (TreeItem<Path> added : c.getAddedSubList()) {
                        if (added instanceof CheckBoxTreeItem<Path> cb) {
                            addRecursiveListener(cb, onChange);

                            internalSelectionUpdate = true;
                            try {
                                if (item.isSelected() && !item.isIndeterminate()) {
                                    cb.setIndeterminate(false);
                                    cb.setSelected(true);
                                    expandParents(cb);
                                    updateParents(cb);
                                } else {
                                    applyModelSelectionToItem(cb);
                                }
                            } finally {
                                internalSelectionUpdate = false;
                            }
                        }
                    }
                }
            }
        });
    }

    private static void applyModelSelectionToItem(CheckBoxTreeItem<Path> item) {
        if (item == null || item.getValue() == null || modelMain == null) {
            return;
        }

        String itemPath = item.getValue().toString();

        if (isExactPathSelected(itemPath)) {
            Platform.runLater(() -> {

                item.setIndeterminate(false);
                item.setSelected(true);
                expandParents(item);
                updateParents(item);


            });
            return;
        }

        if (hasSelectedDescendantInModel(itemPath)) {
            Platform.runLater(() -> {

                item.setSelected(false);
                item.setIndeterminate(true);
                expandParents(item);
                updateParents(item);
            });
            return;
        }
        Platform.runLater(() -> {

            item.setIndeterminate(false);
            item.setSelected(false);

        });
    }

    private static boolean isExactPathSelected(String itemPath) {
        for (SelectedFolder selectedFolder : modelMain.getSelectedFolders().getSelectedFolderScanner_obs()) {
            if (selectedFolder.isConnected() && itemPath.equals(selectedFolder.getFolder())) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasSelectedDescendantInModel(String itemPath) {
        Path parentPath;
        try {
            parentPath = Paths.get(itemPath);
        } catch (Exception e) {
            return false;
        }

        for (SelectedFolder selectedFolder : modelMain.getSelectedFolders().getSelectedFolderScanner_obs()) {
            if (!selectedFolder.isConnected()) {
                continue;
            }

            try {
                Path selectedPath = Paths.get(selectedFolder.getFolder());
                if (!selectedPath.equals(parentPath) && selectedPath.startsWith(parentPath)) {
                    return true;
                }
            } catch (Exception ignored) {
            }
        }

        return false;
    }

    private static boolean isPathInSelectedFolders(String pathString) {
        for (SelectedFolder selectedFolder : modelMain.getSelectedFolders().getSelectedFolderScanner_obs()) {
            if (selectedFolder.getFolder().equals(pathString)) {
                return true;
            }
        }
        return false;
    }

    private static void expandParents(CheckBoxTreeItem<Path> item) {
        TreeItem<Path> current = item;
        while (current != null) {
            current.setExpanded(true);
            current = current.getParent();
        }
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
            Path parentPath = parent.getValue();

            if (parentPath == null) {
                p = parent.getParent();
                continue;
            }

            String parentString = parentPath.toString();

            if (isExactPathSelected(parentString)) {
                parent.setIndeterminate(false);
                parent.setSelected(true);
            } else if (hasSelectedDescendantInModel(parentString)) {
                parent.setSelected(false);
                parent.setIndeterminate(true);
            } else {
                parent.setIndeterminate(false);
                parent.setSelected(false);
            }

            p = parent.getParent();
        }
    }

    private static void findAndSelectPathWithVolume(CheckBoxTreeItem<Path> root, Path targetPath) {
        if (root == null || targetPath == null) {
            return;
        }

        Path volumeRoot = targetPath.getRoot();
        if (volumeRoot == null) {
            return;
        }

        CheckBoxTreeItem<Path> driveNode = null;
        for (TreeItem<Path> child : root.getChildren()) {
            if (child instanceof CheckBoxTreeItem<Path> cb && cb.getValue() != null) {
                if (cb.getValue().equals(volumeRoot)) {
                    driveNode = cb;
                    break;
                }
            }
        }

        if (driveNode == null) {
            return;
        }

        CheckBoxTreeItem<Path> currentNode = driveNode;
        int nameCount = targetPath.getNameCount();

        for (int i = 0; i < nameCount; i++) {
            Path segment = targetPath.getName(i);
            CheckBoxTreeItem<Path> foundChild = null;

            for (TreeItem<Path> child : currentNode.getChildren()) {
                if (child instanceof CheckBoxTreeItem<Path> cb && cb.getValue() != null) {
                    Path fileName = cb.getValue().getFileName();
                    if (fileName != null && fileName.equals(segment)) {
                        foundChild = cb;
                        break;
                    }
                }
            }

            if (foundChild == null) {
                return;
            }

            currentNode = foundChild;
        }

        currentNode.setIndeterminate(false);
        currentNode.setSelected(true);
        expandParents(currentNode);
        updateParents(currentNode);
    }

    private static void findAndSelectPaths(CheckBoxTreeItem<Path> item, Path targetPath) {
        if (item == null || targetPath == null) {
            return;
        }

        if (item.getValue() != null && item.getValue().equals(targetPath)) {
            item.setIndeterminate(false);
            item.setSelected(true);
            expandParents(item);
            updateParents(item);
            return;
        }

        for (TreeItem<Path> child : item.getChildren()) {
            if (child instanceof CheckBoxTreeItem<Path> cb) {
                findAndSelectPaths(cb, targetPath);
            }
        }
    }

    @FunctionalInterface
    private interface TreeItemSelectionConsumer {
        void accept(CheckBoxTreeItem<Path> item, boolean selected);
    }
}
