package com.girbola.controllers.folderscanner;


import com.girbola.controllers.main.ModelMain;
import com.girbola.messages.Messages;
import common.utils.FileUtils;
import java.nio.file.Files;
import java.nio.file.Paths;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.robot.Robot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckBoxSelectFolderTableCell extends TableCell<SelectedFolder, Boolean> {

    private Logger logger = LoggerFactory.getLogger(CheckBoxSelectFolderTableCell.class);

    private ModelMain modelMain;
    private ModelFolderScanner modelFolderScanner;

    private CheckBox checkBox;

    public CheckBoxSelectFolderTableCell(ModelMain modelMain, ModelFolderScanner modelFolderScanner) {
        this.modelMain = modelMain;
        this.modelFolderScanner = modelFolderScanner;
    }

    @Override
    protected void updateItem(Boolean item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            this.setGraphic(null);
            this.setText(null);
        } else {
            selectCell();

            setText(null);
            setGraphic(checkBox);
        }
    }

    private void selectCell() {
        if (checkBox == null) {
            checkBox = new CheckBox();
            checkBox.setSelected(getValue());
            checkBox.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
                Messages.sprintf("CheckBoxSelectFolderTableCell CHECKBOX IS: " + newValue);
                SelectedFolder selectedFolder = getTableView().getItems().get(getIndex());

                for(SelectedFolder selectedFolder1 : modelMain.getSelectedFolders().getSelectedFolderScanner_obs()) {
                    if(selectedFolder1.getFolder().equals(selectedFolder.getFolder())) {
                        selectedFolder1.setSelected(newValue);
                        selectedFolder1.setMedia(FileUtils.getHasMedia(selectedFolder1.getFolder()));
                        Messages.sprintf("--CHECKBOX IS: " + newValue + " selectedFolder: " + selectedFolder.getFolder() + " hasMedia? " + selectedFolder.isMedia());
                    }
                }
            });
        }
    }

    private Boolean getValue() {
        return getItem() == null ? false : getItem();
    }
}
