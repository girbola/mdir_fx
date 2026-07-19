
package com.girbola.controllers.main.tables;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import static com.girbola.messages.Messages.sprintf;


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
