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
            if (selectedFolder.isConnected() && !selectedFolder.isIgnored() && areSamePath(itemPath, selectedFolder.getFolder())) {
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
            if (!selectedFolder.isConnected() || selectedFolder.isIgnored()) {
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
            if (areSamePath(pathString, selectedFolder.getFolder())) {
                return true;
            }
        }
        return false;
    }

    /** Returns {@code true} when {@code itemPath} is in obs with {@code ignored=true}. */
    private boolean isExactPathIgnored(String itemPath) {
        if (modelMain == null || itemPath == null) {
            return false;
        }
        for (SelectedFolder sf : modelMain.getSelectedFolders().getSelectedFolderScanner_obs()) {
            if (sf.isConnected() && sf.isIgnored() && areSamePath(itemPath, sf.getFolder())) {
                return true;
            }
        }
        return false;
    }

    private boolean areSamePath(String leftPath, String rightPath) {
        if (leftPath == null || rightPath == null) {
            return false;
        }
        try {
            return Paths.get(leftPath).normalize().equals(Paths.get(rightPath).normalize());
        } catch (Exception _) {
            return leftPath.equals(rightPath);
        }
    }

    /**
     * Returns {@code true} when any ancestor of {@code path} is present in obs as a
     * regular (non-ignored) selection, meaning {@code path} is already covered by a
     * parent selection and should be treated as "ignored" rather than "selected" if the
     * user explicitly checks it.
     */
    private boolean isAncestorSelectedInModel(Path path) {
        if (modelMain == null || path == null) {
            return false;
        }
        Path ancestor = path.normalize().getParent();
        while (ancestor != null) {
            for (SelectedFolder sf : modelMain.getSelectedFolders().getSelectedFolderScanner_obs()) {
                if (!sf.isConnected() || sf.isIgnored()) {
                    continue;
                }
                try {
                    if (ancestor.equals(Paths.get(sf.getFolder()).normalize())) {
                        return true;
                    }
                } catch (Exception _) {
                    if (ancestor.toString().equals(sf.getFolder())) {
                        return true;
                    }
                }
            }
            ancestor = ancestor.getParent();
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
                // Parent is directly selected — purge stale non-ignored descendants from obs,
                // visually clear children (non-cascading), then re-affirm parent.
                modelMain.getSelectedFolders().getSelectedFolderScanner_obs()
                        .removeIf(sf -> {
                            try {
                                Path sfPath = Paths.get(sf.getFolder());
                                return !sf.isIgnored()
                                        && !sfPath.equals(parentPath)
                                        && sfPath.startsWith(parentPath);
                            } catch (Exception _) {
                                return false;
                            }
                        });
                setChildrenSelected(parent, false);
                parent.setIndeterminate(false);
                parent.setSelected(true);
            } else if (areAllDirectChildrenCoveredInModel(parent)) {
                // Every direct tree-child is fully covered by the model — collapse them
                // into the parent (this works even while internalSelectionUpdate is true
                // because obs changes are not gated by that flag).
                modelMain.getSelectedFolders().getSelectedFolderScanner_obs()
                        .removeIf(sf -> {
                            try {
                                return !sf.isIgnored()
                                        && Paths.get(sf.getFolder()).startsWith(parentPath);
                            } catch (Exception _) {
                                return false;
                            }
                        });
                if (!isPathInSelectedFolders(parentString)) {
                    modelMain.getSelectedFolders().getSelectedFolderScanner_obs()
                            .add(SelectedFolder.create(parentString, true, true,
                                    FileUtils.getHasMedia(parentString), false));
                }
                // Non-cascading: visually clear children, then re-affirm collapsed parent.
                setChildrenSelected(parent, false);
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

    /**
     * Returns {@code true} when every direct tree-child of {@code parent} is already
     * fully covered by an entry in {@code selectedFolderScanner_obs} (i.e. the child
     * itself, or one of its ancestors, is in the list).  Returns {@code false} for
     * leaf-only parents (no children in the tree) to avoid premature collapsing.
     */
    private boolean areAllDirectChildrenCoveredInModel(CheckBoxTreeItem<Path> parent) {
        if (parent == null || parent.getChildren().isEmpty()) {
            return false;
        }
        for (TreeItem<Path> child : parent.getChildren()) {
            if (child.getValue() == null) {
                return false;
            }
            if (!isPathCoveredByModel(child.getValue())) {
                return false;
            }
        }
        return true;
    }

    private void setChildrenSelected(CheckBoxTreeItem<Path> parent, boolean selected) {
        for (TreeItem<Path> child : parent.getChildren()) {
            if (child instanceof CheckBoxTreeItem<Path> cb) {
                cb.setIndeterminate(false);
                if (!selected && cb.getValue() != null && isExactPathIgnored(cb.getValue().toString())) {
                    // Ignored child: keep it checked under a selected parent.
                    // When the parent is deselected, ignored entries are removed from obs
                    // before this method is called, so isExactPathIgnored returns false
                    // there and the item is properly cleared.
                    cb.setSelected(true);
                } else {
                    cb.setSelected(selected);
                }
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
            String pathString = value != null ? value.toString() : null;
            Messages.sprintf("#######checkboxTreeItemValue: " + value);
            if (value != null) {
                if (Boolean.TRUE.equals(newV)) {
                    Messages.sprintf("Adding path to selected folders: " + pathString);
                    Path newPath = Paths.get(pathString);
                    if (isAncestorSelectedInModel(newPath)) {
                        // Parent coverage already includes this node: prevent checkbox
                        // based sibling selection. Ignore exclusions are handled by
                        // the dedicated ignore toggle button.
                        internalSelectionUpdate = true;
                        try {
                            item.setIndeterminate(false);
                            item.setSelected(false);
                        } finally {
                            internalSelectionUpdate = false;
                        }
                        updateParents(item);
                        return;
                    } else {
                        // Normal selection: remove non-ignored descendants (they are
                        // now covered by this parent), then add as selected.
                        modelMain.getSelectedFolders().getSelectedFolderScanner_obs()
                                .removeIf(sf -> {
                                    try {
                                        Path sfPath = Paths.get(sf.getFolder());
                                        return !sf.isIgnored()
                                                && !sfPath.equals(newPath)
                                                && sfPath.startsWith(newPath);
                                    } catch (Exception _) {
                                        return false;
                                    }
                                });
                        if (!isPathInSelectedFolders(pathString)) {
                            modelMain.getSelectedFolders().getSelectedFolderScanner_obs()
                                    .add(SelectedFolder.create(pathString, true, true,
                                            FileUtils.getHasMedia(pathString), false));
                        }
                        consolidateAncestors(pathString);
                    }
                } else {
                    Messages.sprintf("Removing path from selected folders: " + pathString);
                    // Remove this item and any ignored descendants — they were only
                    // meaningful as exclusions under this path's coverage.
                    modelMain.getSelectedFolders().getSelectedFolderScanner_obs()
                            .removeIf(sf -> {
                                try {
                                    if (pathString.equals(sf.getFolder())) return true;
                                    if (sf.isIgnored()) {
                                        return Paths.get(sf.getFolder())
                                                .startsWith(Paths.get(pathString));
                                    }
                                    return false;
                                } catch (Exception _) {
                                    return pathString.equals(sf.getFolder());
                                }
                            });
                }
            }

            internalSelectionUpdate = true;
            try {
                // Non-cascading model: only items explicitly in obs carry a checkmark.
                // Always clear all descendants visually first.
                setChildrenSelected(item, false);
                if (Boolean.TRUE.equals(newV)) {
                    if (isPathInSelectedFolders(pathString)) {
                        // Item is still in obs after any consolidation — re-affirm it as
                        // checked (CheckBoxTreeItem's upward auto-propagation may have
                        // cleared it when children were set to false above).
                        item.setIndeterminate(false);
                        item.setSelected(true);
                    } else {
                        // Item was consolidated into an ancestor — mark it as unchecked
                        // (it is implicitly covered by the ancestor's checkmark).
                        item.setIndeterminate(false);
                        item.setSelected(false);
                    }
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
                                // Non-cascading: apply model state to the new child.
                                // Even if parent is selected, the child is not independently
                                // checked — applyModelSelectionToItem handles all cases.
                                applyModelSelectionToItem(cb);
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

        // If this item is directly selected or ignored, applyModelSelectionToItem already
        // cleared all descendants via setChildrenSelected — no need to recurse further.
        if (item.getValue() != null) {
            String v = item.getValue().toString();
            if (isExactPathSelected(v) || isExactPathIgnored(v)) {
                return;
            }
        }

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

        if (isExactPathIgnored(itemPath)) {
            // This path is explicitly excluded under a selected ancestor — show it as
            // checked so the user can see it is in the exclusion list.
            item.setIndeterminate(false);
            item.setSelected(true);
            setChildrenSelected(item, false);
            item.setIndeterminate(false);
            item.setSelected(true);
            expandParents(item);
            return;
        }

        if (isExactPathSelected(itemPath)) {
            // Non-cascading: mark this item as checked, then visually clear all its
            // descendants (they are implicitly covered — not independently checked).
            item.setIndeterminate(false);
            item.setSelected(true);
            setChildrenSelected(item, false);
            // Re-affirm: CheckBoxTreeItem's upward auto-propagation from children→parent
            // may have overridden setSelected(true) when children transitioned to false.
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

    /**
     * After adding {@code pathString} to the selection, walk up its ancestor chain.
     * If any ancestor is already in the model (meaning it was selected and covers this
     * path), remove the just-added entry and keep only the ancestor.
     * Also, if every child of an ancestor tree-node is now represented in the model
     * (directly or via their own sub-trees), remove those children and add the ancestor
     * instead — this is the "bubble up siblings → replace with closest covered parent"
     * behaviour.
     */
    private void consolidateAncestors(String pathString) {
        if (modelMain == null || pathString == null) {
            return;
        }
        Path path;
        try {
            path = Paths.get(pathString);
        } catch (Exception e) {
            return;
        }

        // Walk up: if any ancestor is already selected it covers us — remove us
        Path ancestor = path.getParent();
        while (ancestor != null) {
            final String ancestorStr = ancestor.toString();
            boolean ancestorSelected = modelMain.getSelectedFolders()
                    .getSelectedFolderScanner_obs()
                    .stream()
                    .anyMatch(sf -> sf.isConnected() && !sf.isIgnored() && ancestorStr.equals(sf.getFolder()));
            if (ancestorSelected) {
                // Ancestor covers us; remove only non-ignored descendants
                // (ignored entries are intentional exclusions — keep them).
                modelMain.getSelectedFolders().getSelectedFolderScanner_obs()
                        .removeIf(sf -> {
                            try {
                                Path sfPath = Paths.get(sf.getFolder());
                                Path anc = Paths.get(ancestorStr);
                                return !sf.isIgnored() && !sfPath.equals(anc) && sfPath.startsWith(anc);
                            } catch (Exception _) {
                                return false;
                            }
                        });
                return;
            }
            ancestor = ancestor.getParent();
        }

        // Bubble up: check whether all tree children of our direct parent are now
        // covered by the model, so we can replace them with the parent.
        bubbleUpIfAllSiblingsCovered(path);
    }

    /**
     * If every child directory of {@code path}'s parent is already covered by an
     * entry in selectedFolderScanner_obs, replace all those sibling entries with a
     * single entry for the parent — then recurse upward.
     */
    private void bubbleUpIfAllSiblingsCovered(Path path) {
        if (path == null) {
            return;
        }
        Path parent = path.getParent();
        if (parent == null) {
            return;
        }

        // Find the tree node for 'parent'
        CheckBoxTreeItem<Path> parentItem = findTreeItemByPath(
                modelMain.getFolderScannerController().getDrives_rootItem(), parent);
        if (parentItem == null || parentItem.getChildren().isEmpty()) {
            return;
        }

        // Check if every child of that tree node is "fully covered" in the model
        for (TreeItem<Path> child : parentItem.getChildren()) {
            if (child.getValue() == null) {
                return;
            }
            if (!isPathCoveredByModel(child.getValue())) {
                return; // at least one sibling is not yet covered
            }
        }

        // All children are covered — remove non-ignored obs entries and add parent instead
        String parentStr = parent.toString();
        modelMain.getSelectedFolders().getSelectedFolderScanner_obs()
                .removeIf(sf -> {
                    try {
                        Path sfPath = Paths.get(sf.getFolder());
                        return !sf.isIgnored() && sfPath.startsWith(parent);
                    } catch (Exception _) {
                        return false;
                    }
                });
        if (!isPathInSelectedFolders(parentStr)) {
            modelMain.getSelectedFolders().getSelectedFolderScanner_obs()
                    .add(SelectedFolder.create(parentStr, true, true,
                            FileUtils.getHasMedia(parentStr), false));
        }

        // Recurse upward
        bubbleUpIfAllSiblingsCovered(parent);
    }

    /**
     * Returns true when {@code path} itself, or any ancestor of {@code path}, is
     * present and connected in selectedFolderScanner_obs (meaning the path is fully
     * covered by the current selection).
     */
    private boolean isPathCoveredByModel(Path path) {
        if (modelMain == null || path == null) {
            return false;
        }
        for (SelectedFolder sf : modelMain.getSelectedFolders().getSelectedFolderScanner_obs()) {
            if (!sf.isConnected() || sf.isIgnored()) {
                continue;
            }
            try {
                Path sfPath = Paths.get(sf.getFolder());
                // sfPath covers 'path' if sfPath == path OR path starts with sfPath
                if (path.equals(sfPath) || path.startsWith(sfPath)) {
                    return true;
                }
            } catch (Exception ignored) {
            }
        }
        return false;
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
