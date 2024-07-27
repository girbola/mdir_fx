package com.girbola;

public enum MDir_Constants {

    UNKNOWN("Unknown");

    MDir_Constants(String type) {
        this.type = type;
    }

    private String type;

    public String getType() {
        return this.type;
    }

}
