/*
 @(#)Copyright:  Copyright (c) 2012-2020 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.datefixer;

import static com.girbola.Main.bundle;
import static com.girbola.messages.Messages.sprintf;
import static com.girbola.messages.Messages.warningText;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.girbola.controllers.main.Model_main;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.fileinfo.FileInfo;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

public class FileOperationsController {

    private final String ERROR = FileOperationsController.class.getSimpleName();

    private Model_datefix model_datefix;
    private Model_main model_main;
    private ObservableList<Folder> comboList = FXCollections.observableArrayList();

    @FXML
    private Button mixDates_btn;
    
    @FXML
    private ComboBox<Folder> move_comboBox;
    
    

    public void init(Model_datefix aModel_datefix, Model_main aModel_main) {
        this.model_datefix = aModel_datefix;
        this.model_main = aModel_main;

        for (FolderInfo fi : this.model_main.tables().getSorted_table().getItems()) {
            if (!model_datefix.getCurrentFolderPath().toString().equals(fi.getFolderPath())) {
                comboList.add(new Folder(Paths.get(fi.getFolderPath())));
            }
        }
        move_comboBox.setConverter(new StringConverter<Folder>() {
            @Override
            public Folder fromString(String string) {
                return move_comboBox.getItems().stream().filter(ap
                        -> ap.getName().equals(string)).findFirst().orElse(null);
            }

            @Override
            public String toString(Folder object) {
                return object.getName();

            }
        });
        move_comboBox.valueProperty().addListener(new ChangeListener<Folder>() {
            @Override
            public void changed(ObservableValue<? extends Folder> observable, Folder oldValue, Folder newValue) {
                if (newValue != null) {
                    sprintf("newValue: " + newValue.getPath());
                }
            }
        });
        move_comboBox.setItems(comboList);
    }

    @FXML
    private void mixDates_btn_action(ActionEvent event) {
    }

    @FXML
    private void move_comboBox_action(ActionEvent event) {
        sprintf("ComboBox is under constructor");
        ComboBox cb = (ComboBox) event.getSource();
        if (model_datefix.getSelectionModel().getSelectionList().isEmpty()) {
            warningText(bundle.getString("noSelectedFiles"));
            return;
        }
        for (Node node : model_datefix.getSelectionModel().getSelectionList()) {
            if (node instanceof VBox) {
                if (node.getId().equals("imageFrame")) {
                    FileInfo fileInfo = (FileInfo) node.getUserData();
                    for (Node vbox : ((VBox) node).getChildren()) {
                        if (vbox instanceof TextField) {

//                        Path source = Paths.get(model.getCurrentFilePath() + File.separator + ((TextField) vbox).getText());
//                            Path source = Paths.get(fileInfo.getPath());
//                            Folder folder = (Folder) cb.getValue();
//                            Path destination = Paths.get(folder.getPath() + File.separator + ((TextField) vbox).getText());
//                            FileInfo fi_src = TableUtils.findFileInfo(TableType.SORTED.getType(), Paths.get(fileInfo.getPath()), model_main.getTables());
//                            if (fi_src == null) {
//                                sprintf("fi_src were null;");
//                                return;
//                            }
//                            sprintf("fi_src found!: " + fi_src.toString());
//                            TableValues fi_dest = TableUtils.findTableValues(source, model_main.getTables().get)(TableType.SORTED.getType(), folder.getPath(), model_main.getTables());
//                            if (fi_dest == null) {
//                                sprintf("fi_dest were null;");
//                                return;
//                            }
//
//                            sprintf("fi_src: " + fi_src.toString() + " fi_dest: " + fi_dest.toString());
//
//                            sprintf("NOT MOVING YET Moving from: " + source);
//                            sprintf("->> TO - >: " + destination);
//                        model.getSelectionModel().remove(node);
                        }
                    }
                }
            }
        }
    }
}

class Folder {

    private Path path;
    private String name;

    public Folder(Path path) {
        this.path = path;
        name = path.getFileName().toString();
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
