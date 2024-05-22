package com.girbola.controllers.folderscanner;


import com.girbola.controllers.main.Model_main;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.messages.Messages;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckBoxSelectFolderTableCell extends TableCell<SelectedFolder, Boolean> {

    private Logger logger = LoggerFactory.getLogger(CheckBoxSelectFolderTableCell.class);

    private Model_main modelMain;
    private ModelFolderScanner modelFolderScanner;

    private CheckBox checkBox;

    public CheckBoxSelectFolderTableCell(Model_main modelMain, ModelFolderScanner modelFolderScanner) {
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
/*                    if(newValue==true) {
                        modelMain.getSelectedFolders().getSelectedFolderScanner_obs().remove(selectedFolder);
                    } else {
                        modelMain.getSelectedFolders().getSelectedFolderScanner_obs().add(selectedFolder);
                    }*/



                  /*  for(SelectedFolder selectedItems : ) {
                        Messages.sprintf("selectedItems: " + selectedItems.getFolder());
                        if (newValue) {
                            Platform.runLater(() -> {
                                Messages.sprintf("CHECKBOX IS SELECT?: " + newValue + " selectedFolder: " + selectedItems);
                            });
                        } else {
                            Platform.runLater(() -> {
                                Messages.sprintf("CHECKBOX IS SELECT?: " + newValue + " selectedFolder: " + selectedItems);
                            });
                        }

                    }*/

                }
            });
        }
    }

    private Boolean getValue() {
        return getItem() == null ? false : getItem();
    }
}
