package com.girbola.controllers.datefixer;

public enum SelectorModelType {
	CAMERA("CAMERA"), DATE("DATE"), EVENT("EVENT"), LOCATION("LOCATION");

	private String type;

	SelectorModelType(String type) {
		this.type = type;
	}

	public String getType() {
		return this.type;
	}
}
