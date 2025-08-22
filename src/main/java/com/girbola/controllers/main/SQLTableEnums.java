package com.girbola.controllers.main;

public enum SQLTableEnums {
	CONFIGURATION("configuration"),
	FOLDERINFO("folderinfo"),
	FILEINFO("fileinfo"),
	SELECTEDFOLDERS("selectedfolders"),
	REGISTEREDDRIVES("registereddrives"),
	DRIVEINFO("driveinfo"),
	THUMBINFO("thumbinfo"),
	WORKDIR("workdir"),
	IGNOREDLIST("ignoredlist"),
	CONFIG("config"),
	TABLES_COLS("tables_cols"),
	FOLDERINFOS("folderinfos");

	private String type;

	SQLTableEnums(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
}
