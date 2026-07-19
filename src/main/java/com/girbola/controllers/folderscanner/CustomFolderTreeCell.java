package com.girbola.controllers.folderscanner;

import com.girbola.controllers.main.ModelMain;
import com.girbola.messages.Messages;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import java.nio.file.Path;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import org.kordamp.ikonli.javafx.FontIcon;

public class CustomFolderTreeCell extends CheckBoxTreeCell<Path> {
    private final ToggleButton toggleButton = new ToggleButton();
    private final Label label = new Label();

    // HBox sisältää ToggleButtonin ja Labelin.
    private final HBox hBox = new HBox(10, toggleButton, label);

    // Luodaan universaali "Kielletty / Ignore" SVG-ikoni
//    private final SVGPath ignoreIcon = new SVGPath();
    FontIcon ignoreIcon = new FontIcon();
    private final ModelMain modelMain;

    private static final String TRANSPARENT_BG = "-fx-background-color: transparent; -fx-alignment: center;";

    public CustomFolderTreeCell(ModelMain modelMain, Object modelFolderScanner) {
        this.modelMain = modelMain;

        toggleButton.setFocusTraversable(false);

        ignoreIcon.setIconLiteral("bi-snow");
        ignoreIcon.setIconSize(10);
        ignoreIcon.setIconColor(Color.WHITESMOKE);

        // Määritetään ympyrän ja poikkiviivan SVG-polku (proportionaalisesti siisti)
//        ignoreIcon.setContent("M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.42 0-8-3.58-8-8 0-1.85.63-3.55 1.69-4.9L16.9 18.31C15.55 19.37 13.85 20 12 20zm6.31-4.9L7.1 5.69C8.45 4.63 10.15 4 12 4c4.42 0 8 3.58 8 8 0 1.85-.63 3.55-1.69 4.9z");

        // Pakotetaan ikoni sopivan pikkuruiseksi (esim. 14x14px) puunäkymään
//        ignoreIcon.setScaleX(0.6);
//        ignoreIcon.setScaleY(0.6);

        // Asetetaan ikoni ToggleButtonin sisällöksi
        toggleButton.setGraphic(ignoreIcon);

        // Make label grow to fill available space
        HBox.setHgrow(label, Priority.ALWAYS);
        label.setMaxWidth(Double.MAX_VALUE);
    }

    @Override
    public void updateItem(Path item, boolean empty) {
        super.updateItem(item, empty);

        toggleButton.setOnAction(null);

        if (empty || item == null) {
            Messages.sprintf("-----drives_treeView setCellFactory null: " + item + " boolean is: " + empty);
            setGraphic(null);
            setText(null);
        } else {
            Messages.sprintf("-----drives_treeView setCellFactory: " + item + " boolean is: " + empty);

            if (getTreeItem() instanceof javafx.scene.control.CheckBoxTreeItem<Path> cbItem) {
                Messages.sprintf("cbItem::: " + cbItem.getValue());
                String name = item.getFileName() == null ? item.toString() : item.getFileName().toString();
                label.setText(name);

                // Haetaan puun oletusvalintaruutu
                Node defaultCheckBox = getGraphic();

                if (defaultCheckBox instanceof CheckBox cb) {
                    setStyle(TRANSPARENT_BG);
                    setStyle("-fx-text-fill: cyan;");

                    String currentPathStr = item.toString();
                    SelectedFolder selectedFolder = findSelectedFolder(currentPathStr);

                    if (selectedFolder != null) {
                        // Keep toggle button always enabled so users can toggle it
                        toggleButton.setDisable(false);
                        defaultCheckBox.disableProperty().bind(toggleButton.selectedProperty());

                        // Apply initial styling based on folder state
                        applyFolderStyling(cb, selectedFolder);

                    // Listen for toggle button changes and update styling
                    toggleButton.selectedProperty().addListener((change, oldVal, newVal) -> {
                        Messages.sprintf("ToggleButton state changed for " + currentPathStr + ": " + newVal);
                        applyFolderStyling(cb, selectedFolder);
                    });
                    } else {
                        toggleButton.setSelected(false);
                        toggleButton.setDisable(true);
                        defaultCheckBox.disableProperty().unbind();
                        defaultCheckBox.setDisable(false);
                    }

                    // Yhdistetään puun oma CheckBox meidän HBoxiimme ensimmäiseksi
                    if (!hBox.getChildren().contains(defaultCheckBox)) {
                        hBox.getChildren().addFirst(defaultCheckBox);
                    }

                    // Ensure correct order: CheckBox -> ToggleButton -> Label
                    if (!hBox.getChildren().contains(toggleButton)) {
                        hBox.getChildren().add(toggleButton);
                    }
                    if (!hBox.getChildren().contains(label)) {
                        hBox.getChildren().add(label);
                    }

                    setGraphic(hBox);
                    setText(null);
                }
            } else {
                setGraphic(null);
                String name = item.getFileName() == null ? item.toString() : item.getFileName().toString();
                setText(name);
            }
        }
    }

    private SelectedFolder findSelectedFolder(String pathStr) {
        if (modelMain == null || modelMain.getSelectedFolders() == null) return null;
        return modelMain.getSelectedFolders().getSelectedFolderScanner_obs().stream()
                .filter(sf -> pathStr.equals(sf.getFolder()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Centralized method to apply consistent styling based on folder state
     */
    private void applyFolderStyling(CheckBox cb, SelectedFolder folder) {
        if (folder == null) {
            return;
        }

        boolean isIgnored = folder.isIgnored();
        boolean isSelected = folder.isSelected();

        if (isIgnored) {
            Messages.sprintf("Applying ignored styling for: " + folder.getFolder());
            setVisible(false);
            cb.setStyle(TRANSPARENT_BG);
            setStyle(TRANSPARENT_BG);
            label.setStyle("-fx-text-fill: white;");
        } else if (isSelected) {
            Messages.sprintf("Applying selected styling for: " + folder.getFolder());
            setVisible(true);
            cb.setStyle("-fx-background-color: -fx-base; -fx-alignment: center;");
            setStyle(TRANSPARENT_BG);
            label.setStyle("-fx-text-fill: yellow;");
        } else {
            Messages.sprintf("Applying default styling for: " + folder.getFolder());
            setVisible(true);
            cb.setStyle("-fx-background-color: -fx-base; -fx-alignment: center;");
            setStyle(TRANSPARENT_BG);
            label.setStyle("-fx-text-fill: yellow;");
        }
    }

}
