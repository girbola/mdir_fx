package com.girbola.fxml.conflicttableview;

import com.girbola.fileinfo.FileInfo;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

public class ConflictFile {

	private SimpleStringProperty folderName;
	private SimpleStringProperty destination;
	private SimpleStringProperty workDir;
	private SimpleStringProperty workDirSerial;
	private SimpleBooleanProperty canCopy;
	private FileInfo fileInfo;

	public ConflictFile(FileInfo fileInfo, String folderName, String destination, String workDir, String aWorkDirSerial,
			boolean canCopy) {
		this.fileInfo = fileInfo;
		this.folderName = new SimpleStringProperty(folderName);
		this.destination = new SimpleStringProperty(destination);
		this.workDir = new SimpleStringProperty(workDir);
		this.workDirSerial = new SimpleStringProperty(aWorkDirSerial);
		this.canCopy = new SimpleBooleanProperty(canCopy);
	}

	public FileInfo getFileInfo() {
		return fileInfo;
	}

	public SimpleStringProperty folderName_property() {
		return folderName;
	}

	public String getFolderName() {
		return this.folderName.get();
	}

	public void setFolderName(String folderName) {
		this.folderName.set(folderName);
	}

	public SimpleStringProperty destination_property() {
		return destination;
	}

	public String getDestination() {
		return destination.get();
	}

	public void setDestination(SimpleStringProperty destination) {
		this.destination = destination;
	}

	public String getWorkDirSerial() {
		return workDirSerial.get();
	}

	public SimpleStringProperty workDir_property() {
		return workDir;
	}

	public String getWorkDir() {
		return workDir.get();
	}

	public void setWorkDir(String workDir) {
		this.workDir.set(workDir);
	}

	public SimpleBooleanProperty canCopy_property() {
		return canCopy;
	}

	public boolean getCanCopy() {
		return canCopy.get();
	}

	public void setCanCopy(boolean canCopy) {
		this.canCopy.set(canCopy);
	}

}
