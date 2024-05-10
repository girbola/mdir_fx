package com.girbola.controllers.main;

import com.girbola.controllers.main.tables.FolderInfo;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.TableView;

public class MainWindowSizing {

    private Model_main model_main;


    private SimpleDoubleProperty sorted;
    private SimpleDoubleProperty sortid;
    private SimpleDoubleProperty asitis;

    public MainWindowSizing(Model_main model_main) {
        this.model_main = model_main;
        sorted = new SimpleDoubleProperty(-1);
        sortid = new SimpleDoubleProperty(-1);
        asitis = new SimpleDoubleProperty(-1);
    }

    public void handleWidth() {
        model_main.tables().getSortIt_table();
        model_main.tables().getSorted_table();
        model_main.tables().getAsItIs_table();

    }
    public double getSorted() {
        return sorted.get();
    }

    public SimpleDoubleProperty sortedProperty() {
        return sorted;
    }

    public void setSorted(double sorted) {
        this.sorted.set(sorted);
    }

    public double getSortid() {
        return sortid.get();
    }

    public SimpleDoubleProperty sortidProperty() {
        return sortid;
    }

    public void setSortid(double sortid) {
        this.sortid.set(sortid);
    }

    public double getAsitis() {
        return asitis.get();
    }

    public SimpleDoubleProperty asitisProperty() {
        return asitis;
    }

    public void setAsitis(double asitis) {
        this.asitis.set(asitis);
    }

}
