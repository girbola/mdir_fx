package com.girbola.controllers.datefixer;

public enum DateFixConstants {

    IMAGEFRAME("imageFrame");

    private String type;

    DateFixConstants(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

}
