package com.girbola;

public enum SceneNameType {
    MAIN("main"), DATEFIXER("datefixer"), LOADING("loading");
    private String type;

    SceneNameType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

}
