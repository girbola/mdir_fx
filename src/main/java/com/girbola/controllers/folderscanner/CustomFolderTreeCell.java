package com.girbola.controllers.folderscanner;

import com.girbola.controllers.folderscanner.SelectedFolder;
import com.girbola.controllers.main.ModelMain;
import com.girbola.messages.Messages;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.layout.HBox;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import java.nio.file.Path;
import javafx.scene.shape.SVGPath;

public class CustomFolderTreeCell extends CheckBoxTreeCell<Path> {
    private final ToggleButton toggleButton = new ToggleButton("I");
    private final Label label = new Label();

    // HBox sisältää ToggleButtonin ja Labelin.
    private final HBox hBox = new HBox(10, toggleButton, label);

    // Luodaan universaali "Kielletty / Ignore" SVG-ikoni
    private final SVGPath ignoreIcon = new SVGPath();

    private final ModelMain modelMain;
    private final Object model_folderScanner;

    public CustomFolderTreeCell(ModelMain modelMain, Object model_folderScanner) {
        this.modelMain = modelMain;
        this.model_folderScanner = model_folderScanner;

        toggleButton.setFocusTraversable(false);


        // Määritetään ympyrän ja poikkiviivan SVG-polku (proportionaalisesti siisti)
        ignoreIcon.setContent("M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.42 0-8-3.58-8-8 0-1.85.63-3.55 1.69-4.9L16.9 18.31C15.55 19.37 13.85 20 12 20zm6.31-4.9L7.1 5.69C8.45 4.63 10.15 4 12 4c4.42 0 8 3.58 8 8 0 1.85-.63 3.55-1.69 4.9z");

        // Pakotetaan ikoni sopivan pikkuruiseksi (esim. 14x14px) puunäkymään
        ignoreIcon.setScaleX(0.6);
        ignoreIcon.setScaleY(0.6);

        // Asetetaan ikoni ToggleButtonin sisällöksi
        toggleButton.setGraphic(ignoreIcon);

        toggleButton.setTooltip(new Tooltip("Ignore folder from scanning"));

        // KORJAUS 1: Pakotetaan HBox keskittämään itsensä ja kaikki sisältönsä pystysuunnassa
//        hBox.setAlignment(Pos.CENTER_LEFT);
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

                String name = item.getFileName() == null ? item.toString() : item.getFileName().toString();
                label.setText(name);

                // Haetaan puun oletusvalintaruutu
                Node defaultCheckBox = getGraphic();

                // KORJAUS 2: VÄRJÄTÄÄN VALINTARUUTU SYAANIKSI (CYAN) JA KESKITETÄÄN SE
                if (defaultCheckBox instanceof CheckBox cb) {
                    // Keskitetään CheckBox pystysuunnassa suhteessa HBoxiin
                    cb.setAlignment(Pos.CENTER_LEFT);

                    // Tyylitellään CheckBoxin sisäinen laatikko (.box) ja valintamerkkien taustat syaaniksi inline-koodilla
                    cb.setStyle(
                            "-fx-alignment: center-left; " +
                                    "-fx-vertical-alignment: center; " +
                                    "-fx-background-color: -fx-mid-base; " +       // Käytetään tummaa pohjaa syaanitilalla
                                    "-fx-border-color: -fx-mid-border-color; " +  // Teeman oma reunaväri
                                    "-fx-border-radius: 3px; " +
                                    "-fx-background-radius: 3px;"
                    );
                }

                String currentPathStr = item.toString();
                SelectedFolder selectedFolder = findSelectedFolder(currentPathStr);

                if (selectedFolder != null) {
                    toggleButton.setDisable(false);
                    toggleButton.setSelected(selectedFolder.isIgnored());
                    // OHJELMALLINEN VÄRITYS: Ikoni tottelee suoraan teemasi base-värejä
                    if (selectedFolder.isIgnored()) {
                        // Kun kansio on ohitettu, ikoni loistaa kirkkaana tekstivärinä
                        ignoreIcon.setStyle("-fx-fill: -fx-light-text-color;");
                        toggleButton.setStyle("-fx-background-color: -fx-base-accent;"); // Painike painettuna
                    } else {
                        // Kun kansio on mukana skannauksessa, ikoni on himmeä/sulautunut
                        ignoreIcon.setStyle("-fx-fill: derive(-fx-base, 60%);");
                        toggleButton.setStyle("-fx-background-color: transparent;"); // Painike lepotilassa
                    }
                    //toggleButton.setText(selectedFolder.isIgnored() ? "I" : "I");

                    if (defaultCheckBox != null) {
                        defaultCheckBox.disableProperty().bind(toggleButton.selectedProperty());
                    }

                    toggleButton.setOnAction(e -> {
                        boolean isIgnored = toggleButton.isSelected();
                        selectedFolder.setIgnored(isIgnored);
                        toggleButton.setText(isIgnored ? "I" : "I");
                        Messages.sprintf("Kansio: " + currentPathStr + " -> setIgnored(" + isIgnored + ")");
                    });
                } else {
                    toggleButton.setSelected(false);
                    toggleButton.setText("I");
                    toggleButton.setDisable(true);

                    if (defaultCheckBox != null) {
                        defaultCheckBox.disableProperty().unbind();
                        defaultCheckBox.setDisable(false);
                    }
                }

                // Yhdistetään puun oma CheckBox meidän HBoxiimme ensimmäiseksi
                if (defaultCheckBox != null && !hBox.getChildren().contains(defaultCheckBox)) {
                    hBox.getChildren().add(0, defaultCheckBox);
                }

                // KORJAUS 3: Pakotetaan HBoxille reunaviiva ja pystysuuntainen keskitys koodissa
                hBox.setStyle(
                        "-fx-alignment: center-left; " +
                                "-fx-border-color: -fx-dark-border-color; " + // Just ja just näkyvä tumma reuna
                                "-fx-border-width: 1px; " +                    // Ohut 1 pystysuora pikseli
                                "-fx-border-style: solid; " +
                                "-fx-border-radius: 4px; " +
                                "-fx-padding: 4px 8px;"
                );

                setGraphic(hBox);
                setText(null);
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
}
