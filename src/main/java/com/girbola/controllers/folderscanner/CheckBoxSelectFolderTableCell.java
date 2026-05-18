package com.girbola.controllers.folderscanner;


import com.girbola.controllers.main.ModelMain;
import com.girbola.messages.Messages;
import common.utils.FileUtils;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.robot.Robot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckBoxSelectFolderTableCell extends TableCell<SelectedFolder, Boolean> {

    private Logger logger = LoggerFactory.getLogger(CheckBoxSelectFolderTableCell.class);

    //private ModelMain modelMain;
    private ModelFolderScanner modelFolderScanner;
    private List<SelectedFolder> selectedFolderScanner;

    private CheckBox checkBox;

    public CheckBoxSelectFolderTableCell(List<SelectedFolder> selectedFolderScanner, ModelFolderScanner modelFolderScanner) {
        //this.modelMain = modelMain;
        this.selectedFolderScanner = selectedFolderScanner;
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

                for(SelectedFolder selectedFolder1 : selectedFolderScanner) {
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
