package com.girbola.controllers.main;

public enum SQL_Enums {
	CONFIGURATION("configuration"),
	FOLDERINFO("folderinfo"),
	FILEINFO("fileinfo"),
	SELECTEDFOLDERS("selectedfolders"),
	DRIVEINFO("driveinfo"),
	THUMBINFO("thumbinfo"),
	WORKDIR("workdir"),
	IGNOREDLIST("ignoredlist"),
	CONFIG("config"),
	TABLES_COLS("tables_cols"),
	FOLDERINFOS("folderinfos");

	private String type;

	SQL_Enums(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
}
