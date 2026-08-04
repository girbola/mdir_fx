package com.girbola.controllers.folderscanner;

import com.girbola.controllers.main.ModelMain;
import com.girbola.messages.Messages;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.TreeItem;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomFolderTreeCell extends CheckBoxTreeCell<Path> {

    // ── Style constants ──────────────────────────────────────────────────────
    private static final String TRANSPARENT_BG = "-fx-background-color: transparent; -fx-alignment: center;";
    private static final String CB_NORMAL_STYLE = "-fx-background-color: transparent; -fx-alignment: center;";
    private static final String LABEL_IGNORED_STYLE = "-fx-text-fill: -mdir-folder-label-ignored; -fx-strikethrough: true;";
    private static final String LABEL_SELECTED_STYLE = "-fx-text-fill: -mdir-folder-label-selected;";
    private static final String LABEL_DESELECTED_STYLE = "-fx-text-fill: -mdir-folder-label-deselected;";
    private static final String TOGGLE_IGNORED_STYLE = "-fx-background-color: -mdir-folder-toggle-ignored;";
    private static final String TOGGLE_NORMAL_STYLE = TRANSPARENT_BG;
    private static final String IGNORE_ICON_LITERAL = "bi-snow";
    private static final String ICON_FONT_STYLE = "-fx-font-family: 'bootstrap-icons';";
    private static final Color ICON_NORMAL_COLOR = Color.WHITESMOKE;
    private static final Color ICON_IGNORED_COLOR = Color.DARKGRAY;

    // ── UI components ────────────────────────────────────────────────────────
    private final ToggleButton ignoreToggleButton = new ToggleButton();
    private final Label label = new Label();
    private final HBox hBox = new HBox(10);
    private FontIcon ignoreIcon = new FontIcon();
    private ModelMain modelMain;
    private final Map<String, Boolean> standaloneIgnoredByPath = new HashMap<>();

    // ── Per-cell state (reset on recycle) ────────────────────────────────────
    private SelectedFolder currentFolder;
    private CheckBox currentCb;

    /**
     * Fires whenever folder.ignoredProperty or folder.selectedProperty changes.
     */
    private final ChangeListener<Boolean> ignoredListener = (obs, old, nv) -> refreshStyles();
    private final ChangeListener<Boolean> selectedListener = (obs, old, nv) -> refreshStyles();

    // ────────────────────────────────────────────────────────────────────────
    public CustomFolderTreeCell(ModelMain modelMain, Object modelFolderScanner) {
        this.modelMain = modelMain;

        ignoreIcon.setIconLiteral(IGNORE_ICON_LITERAL);
        ignoreIcon.setIconSize(10);
        ignoreIcon.setIconColor(ICON_NORMAL_COLOR);
        if (!ignoreIcon.getStyleClass().contains("ikonli-font-icon")) {
            ignoreIcon.getStyleClass().add("ikonli-font-icon");
        }
        ignoreIcon.setStyle(ICON_FONT_STYLE);

        ignoreToggleButton.setFocusTraversable(false);
        ignoreToggleButton.setGraphic(ignoreIcon);
        ignoreToggleButton.setStyle(TOGGLE_NORMAL_STYLE);
        ignoreToggleButton.setMinWidth(24);
        ignoreToggleButton.setPrefWidth(24);
        ignoreToggleButton.setMaxWidth(24);


        HBox.setHgrow(label, Priority.ALWAYS);
        label.setMaxWidth(Double.MAX_VALUE);
    }

    // ── Cell lifecycle ───────────────────────────────────────────────────────
    @Override
    public void updateItem(Path item, boolean empty) {
        super.updateItem(item, empty);
        detachCurrentFolder();

        if (empty || item == null) {
            setGraphic(null);
            setText(null);
            return;
        }

        if (!(getTreeItem() instanceof javafx.scene.control.CheckBoxTreeItem<Path>)) {
            //setGraphic(null);
            Messages.sprintf("getTreeItem checkboxtreeitem!!!:_ ");
            setText(item.getFileName() == null ? item.toString() : item.getFileName().toString());
            return;
        }

        label.setText(item.getFileName() == null ? item.toString() : item.getFileName().toString());

        CheckBox cb = extractCheckBoxFromGraphic(getGraphic());
        if (cb == null) {
            return;
        }

        TreeItem<Path> treeItem = getTreeItem();
        if(treeItem != null && treeItem.isLeaf()) {
            Messages.sprintf("getTreeItem ISLEAF!");
            javafx.scene.Node checkBox = getGraphic();

            if (checkBox instanceof javafx.scene.control.CheckBox) {
                Node lehti = treeItem.getGraphic();
                Messages.sprintf("getTreeItem ISLEAF! checkBox instanceof CheckBox: " + lehti);
                cb = (CheckBox) checkBox;
            } else {
                Messages.sprintf("getTreeItem ISLEAF! checkBox NOT instanceof CheckBox");
            }

        }

        Messages.sprintf("Checkbox were not null");
        Node graphic = getGraphic();
        Messages.sprintf("Graphic were not null: " + graphic);
        Node leafGraphic;
        if (getGraphic() == null) {
            leafGraphic = null;
            Messages.sprintf("Leafgraphic were null");
        } else {
            leafGraphic = getGraphic();
            Messages.sprintf("Leafgraphic were not null: "  + leafGraphic);

        }

        Messages.sprintf("leafGraphic:::: " + leafGraphic);
        setStyle(TRANSPARENT_BG);
        assembleHBox(cb, leafGraphic);
        setGraphic(hBox);
        setText(null);
//TODO Tee tämä kokonaan uusiksil, koska on olemassa
        SelectedFolder folder = findSelectedFolder(item.toString());
        if (folder == null) {
            Messages.sprintf("No SelectedFolder for: " + item);
            attachStandaloneToggle(cb, item.toString());
            return;
        }

        Messages.sprintf("**************About to attachFolder " + folder);
        attachFolder(folder, cb);
    }

    // ── Folder attach / detach ───────────────────────────────────────────────
    private void attachFolder(SelectedFolder folder, CheckBox cb) {
        Messages.sprintf("Attaching Folder: " + folder);
        currentFolder = folder;
        currentCb = cb;

        // Listen to model property changes so styles update reactively
        folder.ignoredProperty().addListener(ignoredListener);
        folder.selectedProperty().addListener(selectedListener);

        // Keep button always active: when user toggles, mirror state back to model.
        ignoreToggleButton.setDisable(false);
        ignoreToggleButton.setOnAction(e -> folder.setIgnored(ignoreToggleButton.isSelected()));

        // Sync toggle visual state from the model.
        ignoreToggleButton.setSelected(folder.isIgnored());

        // Apply initial styles
        refreshStyles();
    }

    private void detachCurrentFolder() {
        if (currentFolder != null) {
            currentFolder.ignoredProperty().removeListener(ignoredListener);
            currentFolder.selectedProperty().removeListener(selectedListener);
            currentFolder = null;
        }
        // Null out the CheckBox reference (do NOT unbind — we use setDisable explicitly now)
        currentCb = null;
        // Clear the per-folder action so a recycled cell's stale lambda never fires
        ignoreToggleButton.setOnAction(null);
        ignoreToggleButton.setSelected(false);
        ignoreToggleButton.setStyle(TOGGLE_NORMAL_STYLE);
        ignoreIcon.setIconColor(ICON_NORMAL_COLOR);
    }

    private void attachStandaloneToggle(CheckBox cb, String pathStr) {
        currentCb = cb;
        ignoreToggleButton.setDisable(false);
        boolean ignored = standaloneIgnoredByPath.getOrDefault(pathStr, false);
        ignoreToggleButton.setSelected(ignored);
        ignoreToggleButton.setOnAction(e -> {
            boolean newIgnored = ignoreToggleButton.isSelected();
            standaloneIgnoredByPath.put(pathStr, newIgnored);
            applyStandaloneStyles(newIgnored);
        });
        applyStandaloneStyles(ignored);
    }

    private void applyStandaloneStyles(boolean ignored) {
        if (currentCb == null) {
            return;
        }

        ensureIgnoreIconLiteral();

        currentCb.setDisable(ignored);
        currentCb.setStyle(CB_NORMAL_STYLE);
        setStyle(TRANSPARENT_BG);

        if (ignored) {
            label.setStyle(LABEL_IGNORED_STYLE);
            ignoreToggleButton.setStyle(TOGGLE_IGNORED_STYLE);
            ignoreIcon.setIconColor(ICON_IGNORED_COLOR);
        } else {
            label.setStyle(LABEL_DESELECTED_STYLE);
            ignoreToggleButton.setStyle(TOGGLE_NORMAL_STYLE);
            ignoreIcon.setIconColor(ICON_NORMAL_COLOR);
        }
    }

    // ── Styling ──────────────────────────────────────────────────────────────
    private void refreshStyles() {
        if (currentFolder == null || currentCb == null) return;

        if (ignoreToggleButton.isSelected() != currentFolder.isIgnored()) {
            ignoreToggleButton.setSelected(currentFolder.isIgnored());
        }

        // Explicitly enable/disable the checkbox to match the ignored state.
        // We do NOT use disableProperty().bind() because CheckBoxTreeCell's superclass
        // may interfere with the property binding.
        currentCb.setDisable(currentFolder.isIgnored());

        if (currentFolder.isIgnored()) {
            applyIgnoredStyle();
            return;
        }
        if (currentFolder.isSelected()) {
            applySelectedStyle();
        } else {
            applyDeselectedStyle();
        }
    }

    /**
     * Toggle ON → greyed-out, strikethrough label, red toggle, checkbox disabled
     */
    private void applyIgnoredStyle() {
        ensureIgnoreIconLiteral();
        setStyle(TRANSPARENT_BG);
        currentCb.setStyle(CB_NORMAL_STYLE);
        label.setStyle(LABEL_IGNORED_STYLE);
        ignoreToggleButton.setStyle(TOGGLE_IGNORED_STYLE);
        ignoreIcon.setIconColor(ICON_IGNORED_COLOR);
    }

    /**
     * Toggle OFF + folder selected → yellow label, checkbox enabled
     */
    private void applySelectedStyle() {
        Messages.sprintf("Selected Folder: " + currentFolder.getFolder());
        ensureIgnoreIconLiteral();
        setStyle(TRANSPARENT_BG);
        currentCb.setStyle(CB_NORMAL_STYLE);
        label.setStyle(LABEL_SELECTED_STYLE);
        ignoreToggleButton.setStyle(TOGGLE_NORMAL_STYLE);
        ignoreIcon.setIconColor(ICON_NORMAL_COLOR);
    }

    /**
     * Toggle OFF + folder not selected → cyan label, checkbox enabled
     */
    private void applyDeselectedStyle() {
        Messages.sprintf("Deselected Folder: " + currentFolder.getFolder());
        ensureIgnoreIconLiteral();
        setStyle(TRANSPARENT_BG);
        currentCb.setStyle(CB_NORMAL_STYLE);
        label.setStyle(LABEL_DESELECTED_STYLE);
        ignoreToggleButton.setStyle(TOGGLE_NORMAL_STYLE);
        ignoreIcon.setIconColor(ICON_NORMAL_COLOR);

    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private void assembleHBox(CheckBox cb, Node leafGraphic) {
        ensureIgnoreIconLiteral();
        List<Node> nodes = new ArrayList<>(4);
        nodes.add(cb);
        if (leafGraphic != null && leafGraphic != cb && leafGraphic != ignoreToggleButton && leafGraphic != label) {
            nodes.add(leafGraphic);
        }
        nodes.add(ignoreToggleButton);
        nodes.add(label);

        for (Node n : nodes) {
            Messages.sprintf("-----assembleHBox nodes: " + n);
        }
        hBox.getChildren().setAll(nodes);
    }

    private void ensureIgnoreIconLiteral() {
        if (!IGNORE_ICON_LITERAL.equals(ignoreIcon.getIconLiteral())) {
            ignoreIcon.setIconLiteral(IGNORE_ICON_LITERAL);
        }
        ignoreIcon.setStyle(ICON_FONT_STYLE);
    }


    private CheckBox extractCheckBoxFromGraphic(Node graphic) {
        if (graphic instanceof CheckBox cb) {
            return cb;
        }
        if (graphic instanceof HBox box) {
            for (Node child : box.getChildren()) {
                if (child instanceof CheckBox cb) {
                    return cb;
                }
            }
        }
        return null;
    }

    private SelectedFolder findSelectedFolder(String pathStr) {
        if (modelMain == null || modelMain.getSelectedFolders() == null) return null;
        return modelMain.getSelectedFolders().getSelectedFolderScanner_obs().stream()
                .filter(sf -> pathStr.equals(sf.getFolder()))
                .findFirst()
                .orElse(null);
    }
}
