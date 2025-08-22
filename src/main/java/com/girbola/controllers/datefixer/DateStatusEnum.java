package com.girbola.controllers.datefixer;

public enum DateStatusEnum {

    DATE_BAD("bad"),
    DATE_GOOD("good"),
    DATE_SUGGESTED("suggested"),
    DATE_VIDEO("video");

    private String type;

    DateStatusEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }
}