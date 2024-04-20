package com.girbola.sql;

public class FolderInfos {
	private String folderPath;
	private String tableType;
	private String justFolderName;
	private boolean isConnected;

	public FolderInfos(String folderPath, String tableType, String justFolderName, boolean isConnected) {
		super();
		this.folderPath = folderPath;
		this.tableType = tableType;
		this.justFolderName = justFolderName;
		this.isConnected = isConnected;
	}

	public String getFolderPath() {
		return folderPath;
	}

	public void setFolderPath(String folderPath) {
		this.folderPath = folderPath;
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
