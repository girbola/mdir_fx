package com.girbola.fxml.operate;

public enum Copy_State { 
	COPY("COPY"),
    RENAME("RENAME"),
    DUPLICATE("DUPLICATE"),
	BROKENFILE("BROKENFILE");
	
    private String type;

    Copy_State(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
