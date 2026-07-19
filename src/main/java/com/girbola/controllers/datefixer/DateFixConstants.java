package com.girbola.controllers.datefixer;

public enum DateFixConstants {

    DFTILEPANE("df_tilePane"),
    IMAGEFRAME("imageFrame"),
    IMAGEVIEW("imageView");
    private String type;

    DateFixConstants(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

}
