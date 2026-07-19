package com.girbola.controllers.operate;

public enum CopyState {
	COPY("COPY"),
    RENAME("RENAME"),
    DUPLICATE("DUPLICATE"),
	BROKENFILE("BROKENFILE");
	
    private String type;

    CopyState(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
