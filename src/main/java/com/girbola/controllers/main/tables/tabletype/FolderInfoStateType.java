package com.girbola.controllers.main.tables.tabletype;

public enum FolderInfoStateType {
	CHANGED("CHANGED"), OK("OK"), READYTOCOPY("READY TO COPY");

	private String type;

	FolderInfoStateType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
}
