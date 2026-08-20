package com.girbola.controllers.folderscanner.folderpicker;

import com.girbola.controllers.folderscanner.SelectedFolder;
import com.girbola.controllers.main.ModelMain;
import com.girbola.messages.Messages;
import common.utils.FileUtils;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;

public class FolderSelectionService {

    private ModelMain modelMain;
    private boolean internalSelectionUpdate = false;

    public FolderSelectionService(ModelMain modelMain) {
        this.modelMain = modelMain;
    }

    public void selectFolder(SelectedFolder selectedFolder) {
        if (modelMain == null || selectedFolder == null || selectedFolder.getFolder() == null) {
            return;
        }
        syncTreeFromModel(modelMain);
        applySelectionToTree(modelMain.getFolderScannerController().getDrives_rootItem(), Paths.get(selectedFolder.getFolder()));
    }

    public void selectFolder(Path path) {
        if (modelMain == null || path == null) {
            return;
        }

        syncTreeFromModel(modelMain);
        applySelectionToTree(modelMain.getFolderScannerController().getDrives_rootItem(), path);
    }

    public boolean remove(SelectedFolder selectedFolder) {
        Messages.sprintf("Removing selected folder: " + selectedFolder);
        if (modelMain == null || selectedFolder == null || selectedFolder.getFolder() == null) {
            return false;
        }

        boolean removed = modelMain.getSelectedFolders().getSelectedFolderScanner_obs()
                .removeIf(sf -> selectedFolder.getFolder().equals(sf.getFolder()));

        if (removed) {
            syncTreeFromModel(modelMain);
        }

        return removed;
    }

    private void applySelectionToTree(CheckBoxTreeItem<Path> root, Path targetPath) {
        if (root == null || targetPath == null) {
            return;
        }

        if (root.getValue() != null && root.getValue().equals(targetPath)) {
            internalSelectionUpdate = true;
            try {
                root.setIndeterminate(false);
                root.setSelected(true);
                setChildrenSelected(root, true);
                expandParents(root);
                updateParents(root);
            } finally {
                internalSelectionUpdate = false;
            }
            return;
        }

        for (TreeItem<Path> child : root.getChildren()) {
            if (child instanceof CheckBoxTreeItem<Path> cb) {
                applySelectionToTree(cb, targetPath);
            }
        }
    }

    private boolean isExactPathSelected(String itemPath) {
        if (modelMain == null || itemPath == null) {
            return false;
        }

        for (SelectedFolder selectedFolder : modelMain.getSelectedFolders().getSelectedFolderScanner_obs()) {
            if (selectedFolder.isConnected() && itemPath.equals(selectedFolder.getFolder())) {
                return true;
            }
        }
        return false;
    }

    private boolean hasSelectedDescendantInModel(String itemPath) {
        if (modelMain == null || itemPath == null) {
            return false;
        }

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

    private boolean isPathInSelectedFolders(String pathString) {
        if (modelMain == null || pathString == null) {
            return false;
        }

        for (SelectedFolder selectedFolder : modelMain.getSelectedFolders().getSelectedFolderScanner_obs()) {
            if (pathString.equals(selectedFolder.getFolder())) {
                return true;
            }
        }
        return false;
    }

    private void updateParents(CheckBoxTreeItem<Path> item) {
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

    private void setChildrenSelected(CheckBoxTreeItem<Path> parent, boolean selected) {
        for (TreeItem<Path> child : parent.getChildren()) {
            if (child instanceof CheckBoxTreeItem<Path> cb) {
                cb.setIndeterminate(false);
                cb.setSelected(selected);
                setChildrenSelected(cb, selected);
            }
        }
    }

    public void installSelectionPropagation() {
        if (modelMain == null) {
            Messages.sprintfError("modelMain was null");
            return;
        }

        CheckBoxTreeItem<Path> root = modelMain.getFolderScannerController().getDrives_rootItem();
        addRecursiveListener(root);

        Platform.runLater(() -> syncTreeFromModel(modelMain));
    }

    private void addRecursiveListener(CheckBoxTreeItem<Path> item) {
        Messages.sprintf("addRecursiveListener item: " + item.getValue());
        if (item == null) {
            return;
        }

        ChangeListener<Boolean> selectedListener = (obs, oldV, newV) -> {
            if (newV == null || internalSelectionUpdate) {
                return;
            }

            Path value = item.getValue();
            Messages.sprintf("#######checkboxTreeItemValue: " + value);
            if (value != null) {
                String pathString = value.toString();
                if (Boolean.TRUE.equals(newV)) {
                    if (!isPathInSelectedFolders(pathString)) {
                        modelMain.getSelectedFolders().getSelectedFolderScanner_obs()
                                .add(SelectedFolder.create(pathString, true, true, FileUtils.getHasMedia(pathString),false));
                    }
                } else {
                    modelMain.getSelectedFolders().getSelectedFolderScanner_obs()
                            .removeIf(selectedFolder -> pathString.equals(selectedFolder.getFolder()));
                }
            }

            internalSelectionUpdate = true;
            try {
                if (!item.isIndeterminate()) {
                    setChildrenSelected(item, Boolean.TRUE.equals(newV));
                }
                updateParents(item);
            } finally {
                internalSelectionUpdate = false;
            }
        };

        item.selectedProperty().addListener(selectedListener);
        item.indeterminateProperty().addListener((obs, o, n) -> {
            if (internalSelectionUpdate) {
                return;
            }
            internalSelectionUpdate = true;
            try {
                updateParents(item);
            } finally {
                internalSelectionUpdate = false;
            }
        });

        for (TreeItem<Path> child : item.getChildren()) {
            if (child instanceof CheckBoxTreeItem<Path> cb) {
                addRecursiveListener(cb);
                applyModelSelectionToItem(cb);
            }
        }

        item.getChildren().addListener((ListChangeListener<TreeItem<Path>>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    for (TreeItem<Path> added : c.getAddedSubList()) {
                        if (added instanceof CheckBoxTreeItem<Path> cb) {
                            addRecursiveListener(cb);

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

    public void syncTreeFromModel(ModelMain modelMain) {
        this.modelMain = modelMain;

        CheckBoxTreeItem<Path> root = modelMain.getFolderScannerController().getDrives_rootItem();
        internalSelectionUpdate = true;
        try {
            applyModelSelectionRecursively(root);
        } finally {
            internalSelectionUpdate = false;
        }
    }

    private void applyModelSelectionRecursively(CheckBoxTreeItem<Path> item) {
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

    private void applyModelSelectionToItem(CheckBoxTreeItem<Path> item) {
        if (item == null || item.getValue() == null || modelMain == null) {
            return;
        }

        String itemPath = item.getValue().toString();

        if (isExactPathSelected(itemPath)) {
            item.setIndeterminate(false);
            item.setSelected(true);
            expandParents(item);
            updateParents(item);
            return;
        }

        if (hasSelectedDescendantInModel(itemPath)) {
            item.setSelected(false);
            item.setIndeterminate(true);
            expandParents(item);
            updateParents(item);
            return;
        }

        item.setIndeterminate(false);
        item.setSelected(false);
    }

    private void expandParents(CheckBoxTreeItem<Path> item) {
        TreeItem<Path> current = item;
        while (current != null) {
            current.setExpanded(true);
            current = current.getParent();
        }
    }

    public void focusFolder(SelectedFolder selectedFolder) {
        if (modelMain == null || selectedFolder == null || selectedFolder.getFolder() == null) {
            return;
        }
        focusFolder(Paths.get(selectedFolder.getFolder()));
    }

    public void focusFolder(Path folderPath) {
        if (modelMain == null || folderPath == null) {
            return;
        }

        CheckBoxTreeItem<Path> root = modelMain.getFolderScannerController().getDrives_rootItem();
        CheckBoxTreeItem<Path> targetItem = findTreeItemByPath(root, folderPath);

        if (targetItem != null) {
            expandParents(targetItem);

            Platform.runLater(() -> {
                javafx.scene.control.TreeView<Path> treeView = modelMain.getFolderScannerController().getDrivesTreeView();
                if (treeView != null) {
                    treeView.getSelectionModel().select(targetItem);
                    treeView.scrollTo(treeView.getRow(targetItem));
                    treeView.requestFocus();
                }
            });
        }
    }

    private CheckBoxTreeItem<Path> findTreeItemByPath(CheckBoxTreeItem<Path> root, Path targetPath) {
        if (root == null || targetPath == null) {
            return null;
        }

        if (root.getValue() != null && root.getValue().equals(targetPath)) {
            return root;
        }

        for (TreeItem<Path> child : root.getChildren()) {
            if (child instanceof CheckBoxTreeItem<Path> cb) {
                CheckBoxTreeItem<Path> found = findTreeItemByPath(cb, targetPath);
                if (found != null) {
                    return found;
                }
            }
        }

        return null;
    }
}
