/*
 @(#)Copyright:  Copyright (c) 2012-2019 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.fileinfo;

import static com.girbola.messages.Messages.sprintf;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 *
 * @author Marko Lokka
 */
public class BatchMap {

    private SimpleStringProperty src;
    private SimpleStringProperty dest;
    private SimpleBooleanProperty selected;

    public BatchMap(String src, String dest, boolean selected) {
        this.src = new SimpleStringProperty(src);
        this.dest = new SimpleStringProperty(dest);
        this.selected = new SimpleBooleanProperty(selected);
        this.selected.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                sprintf("BM - isSelected? " + newValue);
            }
        });
    }

    public String getDest() {
        return this.dest.get();
    }

    public String getSrc() {
        return this.src.get();
    }

    public void setSrc(String src) {
        this.src.set(src);
    }

    public void setDest(String dest) {
        this.dest.set(dest);
    }

    public boolean isSelected() {
        return this.selected.get();
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    public SimpleBooleanProperty selectedProperty() {
        return this.selected;
    }

}
