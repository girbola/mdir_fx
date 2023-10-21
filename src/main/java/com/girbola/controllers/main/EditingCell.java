package com.girbola.controllers.main;

import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.controllers.main.tables.FolderInfo_Utils;
import com.girbola.events.GUI_Events;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EditingCell extends TableCell<FolderInfo, String> {
    private TextField textField;
    private Model_main model_Main;
    private TableColumn<FolderInfo, String> tableColumn;
    private FolderInfo folderInfo;

    public EditingCell(Model_main aModel_Main, TableColumn<FolderInfo, String> aTableColumn) {
        this.model_Main = aModel_Main;
        this.tableColumn = aTableColumn;
    }

    @Override
    public void startEdit() {
        if (!isEmpty()) {
            super.startEdit();
            createTextField();
            setText(null);
            setGraphic(textField);
            textField.selectAll();
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();

        setText((String) getItem());
        setGraphic(null);
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (textField != null) {
                    FolderInfo folderInfo = tableColumn.getTableView().getSelectionModel().getSelectedItem();
                    if (!textField.getText().isBlank()) {
                        if (folderInfo.getFolderPath().equals(textField.getText())) {
                            Path checkFolderPath = Paths.get(folderInfo.getFolderPath());
                            if (Files.exists(checkFolderPath)) {
                                textField.getStyleClass().add("tableTextField_bad");
                            } else {
                                textField.getStyleClass().add("tableTextField");
                            }
                        }
                    } else {
                        textField.getStyleClass().add("tableTextField_bad");
                    }
//					textField.setText(getString());
                }
                setText(null);
                setGraphic(textField);
            } else {
                setText(getString());
                setGraphic(null);
            }
        }
    }

    private void createTextField() {
        textField = new TextField(getString());
        textField.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!Text_Utils.isValidFileOrFolderName(newValue)) {
                    textField.setText(oldValue);
                }
            }
        });
        textField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent e) {
                if (e.getCode().equals(KeyCode.ENTER)) {
//					setFocused(false);
                    textField.getParent().requestFocus();
                    Messages.sprintf("Enter pressed: " + e.getCode());
                }
            }
        });
        Messages.sprintf("createTextField: " + textField.getParent());
        textField.getStyleClass().add("tableTextField");

        GUI_Events.textField_file_listener(textField);
        textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
        textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> value, Boolean old, Boolean newValue) {
                if (!newValue) {
                    String newPath = textField.getText();
                    FolderInfo selectedItem = (FolderInfo) tableColumn.getTableView().getSelectionModel().getSelectedItem();
                    Path pathParent = Paths.get(selectedItem.getFolderPath()).getParent();
                    Path destination = Paths.get(pathParent.toString() + File.separator + newPath);
                    FolderInfo_Utils.renameSourcePathToNewLocation(selectedItem, destination);
//
//                    Messages.sprintf("SOURCE PATH IS: " + newPath);
//                    Iterator<FileInfo> iterator = selectedItem.getFileInfoList().iterator();
//
//                    while (iterator.hasNext()) {
//                        FileInfo fileInfo = iterator.next();
//                        Path orgPath = Paths.get(fileInfo.getOrgPath());
//                        Path fileName = orgPath.getFileName();
//                        Path parent = Paths.get(orgPath.getParent().getParent().toString() + File.separator + newPath + File.separator + fileName);
//                        Messages.sprintf("New PATAATHATHATH is: " + parent);
//                    }
//
//                    folderInfo.setFolderPath(Paths.get(Paths.get(selectedItem.getFolderPath()).getParent().toString() + File.separator + newPath).toString());
//                    folderInfo.setChanged(true);

                    commitEdit(textField.getText());

//                    Path folderPath = Paths.get(Paths.get(selectedItem.getFolderPath()).getParent().toString() + File.separator + newPath);
//
//                    Iterator<FileInfo> it = selectedItem.getFileInfoList().iterator();
//
//                    FolderInfo destFolderInfoDest = FolderInfo_Utils.getFolderInfo(folderPath);
//                    List<FileInfo> movedList = new ArrayList<>();

                    // Move Operation
//                    while (it.hasNext()) {
//                        FileInfo fileInfo = it.next();
//                        boolean moved = move(src, dest);
//                        if (moved) {
//                            movedList.add(fileInfo);
//                            folderInfo.getFileInfoList().remove(fileInfo);
//                        }
//
//                        Messages.sprintf("FolderPath would be then: " + folderPath);
//                    }

                    //Update fileinfo list source and destination


                    // Create new folderinfo if not exists at new folder



                }
            }
        });
    }

    private String getString() {
         return getItem() == null ? "" : getItem();
    }

}
