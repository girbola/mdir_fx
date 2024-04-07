/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.datefixer;

import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

import static com.girbola.messages.Messages.sprintf;

/**
 * @author Marko Lokka
 */
public class CheckBoxCell_Cameras extends TableCell<EXIF_Data_Selector, Boolean> {

    private final String ERROR = CheckBoxCell_Cameras.class.getSimpleName();
    private CheckBox checkBox;
    private Model_datefix model_DateFix;

    public CheckBoxCell_Cameras(Model_datefix model_DateFix) {
        this.model_DateFix = model_DateFix;
    }

    @Override
    protected void updateItem(Boolean item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            this.setGraphic(null);
            this.setText(null);
        } else {
            paintCell();
        }
    }

    private void paintCell() {
        if (checkBox == null) {
            checkBox = new CheckBox();
            checkBox.setSelected(getValue());
            checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    // setItem(newValue);
                    Task<ObservableList<Node>> updateCamera_Task = new Task<ObservableList<Node>>() {
                        @Override
                        protected ObservableList<Node> call() throws Exception {
                            model_DateFix.getCameras_TableView().setDisable(true);
                            model_DateFix.getDates_TableView().setDisable(true);
                            model_DateFix.getEvents_TableView().setDisable(true);
                            model_DateFix.getLocations_TableView().setDisable(true);
                            EXIF_Data_Selector cameras_data = getTableView().getItems().get(getIndex());
                            cameras_data.setIsShowing(newValue);

                            ObservableList<Node> theList = FXCollections.observableArrayList();
                            List<String> listOfCameras = new ArrayList<>();
                            if (newValue == true) {
                                for (EXIF_Data_Selector cameras : model_DateFix.getCameras_TableView().getItems()) {
                                    if (cameras.isShowing()) {
                                        listOfCameras.add(cameras.getInfo());
                                    }
                                }
                            } else {
                                for (EXIF_Data_Selector cameras : model_DateFix.getCameras_TableView().getItems()) {
                                    if (!cameras.isShowing()) {
                                        listOfCameras.add(cameras.getInfo());
                                    }
                                }
                            }
                            if (listOfCameras.isEmpty()) {
                                return theList;
                            }
                            int counter = 0;
                            if (newValue == true) {
                                for (Node node : model_DateFix.getGridPane().getChildren()) {
                                    if (node instanceof VBox && node.getId().equals("imageFrame")) {
                                        FileInfo fi = (FileInfo) node.getUserData();
                                        if (fi.getCamera_model() == null) {
                                            fi.setCamera_model(new String("Unknown"));
                                        }
                                        if (fi.getCamera_model().length() == 0 || fi.getCamera_model().isEmpty()) {
                                            fi.setCamera_model("Unknown");
                                        }
                                        if (has_cameraModel(fi.getCamera_model(), listOfCameras)) {
                                            theList.add(node);
                                            model_DateFix.getSelectionModel().addOnly(node);
                                        }
                                        counter++;
                                    }
                                }
                            } else if (newValue == false) {
                                for (Node node : model_DateFix.getGridPane().getChildren()) {
                                    if (node instanceof VBox && node.getId().equals("imageFrame")) {
                                        FileInfo fi = (FileInfo) node.getUserData();
                                        if (fi.getCamera_model() == null) {
                                            fi.setCamera_model(new String("Unknown"));
                                        }
                                        if (fi.getCamera_model().length() == 0 || fi.getCamera_model().isEmpty()) {
                                            fi.setCamera_model("Unknown");
                                        }
                                        if (has_cameraModel(fi.getCamera_model(), listOfCameras)) {
                                            theList.add(node);
                                            model_DateFix.getSelectionModel().remove(node);
                                        }
                                        counter++;
                                    }
                                }
                            }
                            return theList;
                        }
                    };
                    updateCamera_Task.setOnSucceeded((WorkerStateEvent event) -> {
                        model_DateFix.getCameras_TableView().setDisable(false);
                        model_DateFix.getDates_TableView().setDisable(false);
                        model_DateFix.getEvents_TableView().setDisable(false);
                        model_DateFix.getLocations_TableView().setDisable(false);
                    });

                    updateCamera_Task.setOnCancelled((WorkerStateEvent event) -> {
                        sprintf("updateCamera_Task cancelled");
                    });

                    updateCamera_Task.setOnFailed((WorkerStateEvent event) -> {
                        sprintf("updateCamera_Task failed");
                        Messages.errorSmth(ERROR, "", null, Misc.getLineNumber(), true);
                    });
                    Thread updateCamera_Task_th = new Thread(updateCamera_Task, "updateCamera_Task Thread");
                    updateCamera_Task_th.start();
                }

                private boolean has_cameraModel(String format, List<String> listOfCameras) {
                    for (String str : listOfCameras) {
                        if (str.equals(format)) {
                            Messages.sprintf("has format: " + str);
                            return true;
                        }
                        if (str.isEmpty() || str.length() <= 0) {
                            if (str.equals("Unknown")) {
                                Messages.sprintf("Unknown has format match");
                                return true;
                            }
                        }
                    }
                    return false;
                }
            });
        }
        checkBox.setSelected(getValue());
        setText(null);
        setGraphic(checkBox);
    }

    private Boolean getValue() {
        return getItem() == null ? false : getItem();
    }

}
