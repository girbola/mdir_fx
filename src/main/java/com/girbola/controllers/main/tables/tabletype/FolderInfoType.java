package com.girbola.controllers.main.tables.tabletype;

public enum FolderInfoType {
	ADD("Added"), DONE("Done"), NOTCONNECTED("Not connected"), READY("Ready");

	private String type;

	FolderInfoType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
}
