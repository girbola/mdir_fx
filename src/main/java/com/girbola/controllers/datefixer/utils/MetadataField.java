package com.girbola.controllers.datefixer.utils;

public enum MetadataField {

    EVENT("Event"),
    LOCATION("Location"),
    CAMERA("Camera"),
    DATE("Date"),
    USER("User"),
    UNKNOWN("Unknown");

    private String type;

    MetadataField(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }


}
