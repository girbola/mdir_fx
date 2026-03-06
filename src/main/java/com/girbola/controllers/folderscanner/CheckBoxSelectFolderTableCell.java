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
                    for(SelectedFolder selectedFolder1 : modelMain.getSelectedFolders().getSelectedFolderScanner_obs()) {
                        if(selectedFolder1.getFolder().equals(selectedFolder.getFolder())) {
                            selectedFolder1.setSelected(newValue);
                            if(newValue) {
                                // Check if folder exists and then check for media
                                if(selectedFolder1.getFolder() != null && Files.exists(Paths.get(selectedFolder1.getFolder()))) {
                                    boolean hasMedia = FileUtils.getHasMedia(selectedFolder1.getFolder());
                                    selectedFolder.setMedia(hasMedia);
                                } else {
                                    selectedFolder.setMedia(false);
                                }
                            }
                            Messages.sprintf("Selected folder changed to: " + newValue);
                        }
                    }
                }
            });
        }
    }

    private Boolean getValue() {
        return getItem() == null ? false : getItem();
    }
}
