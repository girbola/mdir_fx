
package com.girbola.controllers.datefixer.table;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;


public class EXIF_Data_Selector {

    private SimpleIntegerProperty count;
    private SimpleStringProperty info;
    private SimpleBooleanProperty isShowing;

    @Override
    public String toString() {
        return "EXIF_Data_Selector{" + "count=" + count + ", info=" + info + ", isShowing=" + isShowing + '}';
    }

    public EXIF_Data_Selector(boolean isShowing, String info, int count) {
        this.count = new SimpleIntegerProperty(count);
        this.info = new SimpleStringProperty(info);
        this.isShowing = new SimpleBooleanProperty(isShowing);
    }

    public SimpleIntegerProperty count_property() {
        return count;
    }

    public int getCount() {
        return count.get();
    }

    public void setCount(SimpleIntegerProperty count) {
        this.count = count;
    }

    public SimpleStringProperty info_property() {
        return info;
    }

    public String getInfo() {
        return info.get();
    }

    public void setInfo(SimpleStringProperty info) {
        this.info = info;
    }

    public BooleanProperty isShowing_property() {
        return isShowing;
    }

    public boolean isShowing() {
        return isShowing.get();
    }

    public void setIsShowing(boolean isShowing) {
        this.isShowing.set(isShowing);
    }
}
