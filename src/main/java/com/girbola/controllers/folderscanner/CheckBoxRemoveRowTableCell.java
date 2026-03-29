package com.girbola.controllers.folderscanner;

import com.girbola.controllers.main.ModelMain;
import com.girbola.messages.Messages;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;

public class CheckBoxRemoveRowTableCell extends TableCell<SelectedFolder, Boolean> {
    private final Button deleteButton;
    private final ModelMain modelMain;
    private final ModelFolderScanner modelFolderScanner;

    public CheckBoxRemoveRowTableCell(ModelMain modelMain, ModelFolderScanner modelFolderScanner) {
        this.modelMain = modelMain;
        this.modelFolderScanner = modelFolderScanner;
        this.deleteButton = new Button();
        FontIcon fontIcon = new FontIcon();
        fontIcon.setIconLiteral("bi-x");
        fontIcon.setIconSize(20);
        fontIcon.setIconColor(Color.DARKRED);
        deleteButton.setGraphic(fontIcon);

        this.deleteButton.setOnAction(event -> {
            Messages.sprintf("deleteButton.setOnAction");
            SelectedFolder selectedFolder = getTableView().getItems().get(getIndex());

            if (selectedFolder != null) {
//                modelMain.getSelectedFolders().getSelectedFolderScanner_obs().remove(selectedFolder);
                modelMain.getFolderSelectionService().remove(selectedFolder);
                getTableView().getItems().remove(selectedFolder);
            }
        });
    }

    @Override
    protected void updateItem(Boolean item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setGraphic(null);
        } else {
            setGraphic(deleteButton);
        }
    }
}
