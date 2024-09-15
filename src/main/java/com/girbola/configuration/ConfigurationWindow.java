package com.girbola.configuration;

import javafx.beans.property.SimpleDoubleProperty;

public class ConfigurationWindow extends Configuration_GetsSets {

    private SimpleDoubleProperty imageViewXProperty = new SimpleDoubleProperty(0);
    private SimpleDoubleProperty imageViewYProperty = new SimpleDoubleProperty(0);

    public double getImageViewXPosition() {
        return imageViewXProperty.get();
    }

    public void setImageViewXProperty(double value) {
        this.imageViewXProperty.set(value);
    }

    public double getImageViewYPosition() {
        return imageViewYProperty.get();
    }

    public void setImageViewYProperty(double value) {
        this.imageViewYProperty.set(value);
    }

}
