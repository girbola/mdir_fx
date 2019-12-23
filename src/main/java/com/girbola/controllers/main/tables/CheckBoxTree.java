/*
 @(#)Copyright:  Copyright (c) 2012-2019 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.main.tables;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;

import static com.girbola.messages.Messages.sprintf;

import com.girbola.fileinfo.BatchMap;

/**
 *
 * @author Marko Lokka
 */
public class CheckBoxTree extends TableCell<BatchMap, Boolean> {

    private CheckBox checkBox;

    public CheckBoxTree() {
        checkBox = new CheckBox();
        checkBox.setSelected(true);
        checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue == true) {
                    sprintf("checkbox is selected: " + newValue);
                    BatchMap bm = (BatchMap) getTableView().getItems().get(getIndex());
                    bm.setSelected(true);
                    sprintf("batchmap value is: " + bm.getSrc() + " dest is: " + bm.getDest() + " isSelected? " + bm.isSelected());
                } else {
                    BatchMap bm = (BatchMap) getTableView().getItems().get(getIndex());
                    bm.setSelected(false);
                    sprintf("Not selected batchmap value is: " + bm.getSrc() + " dest is: " + bm.getDest() + " isSelected? " + bm.isSelected());
                }

            }
        });

        this.setGraphic(checkBox);
        this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    @Override
    public void updateItem(Boolean item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            sprintf("checkboxtree was empty");
            this.setGraphic(null);
            this.setText(null);
        } else {
            BatchMap bm = (BatchMap) getTableView().getItems().get(getIndex());
            sprintf("batchmap value is: " + bm.getSrc() + " dest is: " + bm.getDest() + " isSelected? " + bm.isSelected());
            this.setGraphic(checkBox);
            this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }
    }
}
