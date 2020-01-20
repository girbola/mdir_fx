package com.girbola.fxml.conflicttableview;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

public class ConflictFile {

	private SimpleStringProperty folderName;
	private SimpleStringProperty destination;
	private SimpleStringProperty workDir;
	private SimpleBooleanProperty canCopy;

	public ConflictFile(String folderName, String destination, String workDir, boolean canCopy) {

		this.folderName = new SimpleStringProperty(folderName);
		this.destination = new SimpleStringProperty(destination);
		this.workDir = new SimpleStringProperty(workDir);
		this.canCopy = new SimpleBooleanProperty(canCopy);
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

	public SimpleStringProperty workDir_property() {
		return workDir;
	}

	public String getWorkDir() {
		return workDir.get();
	}

	public void setWorkDir(SimpleStringProperty workDir) {
		this.workDir = workDir;
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
