package com.girbola.controllers.folderscanner;


import com.girbola.controllers.main.ModelMain;
import com.girbola.messages.Messages;
import common.utils.FileUtils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
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
            checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
                    Messages.sprintf("CHECKBOX IS: " + newValue);
                    SelectedFolder selectedFolder = getTableView().getItems().get(getIndex());
                    selectedFolder.setSelected(newValue);
                    selectedFolder.setMedia(FileUtils.getHasMedia(selectedFolder.getFolder()));

                }
            });
        }
    }

    private Boolean getValue() {
        return getItem() == null ? false : getItem();
    }
}
