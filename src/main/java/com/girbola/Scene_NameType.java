package com.girbola;

public enum Scene_NameType {
	MAIN("main"), DATEFIXER("datefixer"), LOADING("loading");

	private String type;

	Scene_NameType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
}
