package com.girbola.sql;

import java.nio.file.Path;

public class FolderState {
	private String path;
	private String tableType;
	private String justFolderName;
	private boolean isConnected;

	public FolderState(String path, String tableType, String justFolderName, boolean isConnected) {
		super();
		this.path = path;
		this.tableType = tableType;
		this.justFolderName = justFolderName;
		this.isConnected = isConnected;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getTableType() {
		return tableType;
	}

	public void setTableType(String tableType) {
		this.tableType = tableType;
	}

	public String getJustFolderName() {
		return justFolderName;
	}

	public void setJustFolderName(String justFolderName) {
		this.justFolderName = justFolderName;
	}

	public boolean isConnected() {
		return isConnected;
	}

	public void setConnected(boolean isConnected) {
		this.isConnected = isConnected;
	}

}
