

package com.girbola.controllers.folderscanner;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

import static com.girbola.controllers.folderscanner.SelectedFolderUtils.getDriveSerialNumberFromPath;

public class SelectedFolder {

    private SimpleStringProperty folder;
    private SimpleBooleanProperty selected;
    private SimpleBooleanProperty connected;
    private SimpleBooleanProperty media;
    private SimpleStringProperty driveSerialNumber;

    public SelectedFolder(boolean selected, boolean connected, String folder, boolean media) {
        this.selected = new SimpleBooleanProperty(selected);
        this.connected = new SimpleBooleanProperty(connected);
        this.folder = new SimpleStringProperty(folder);
        this.media = new SimpleBooleanProperty(media);
		this.driveSerialNumber = new SimpleStringProperty(getDriveSerialNumberFromPath(folder));
    }


    public SelectedFolder(boolean selected, boolean connected, String folder, boolean media, String driveSerialNumber) {
        this.selected = new SimpleBooleanProperty(selected);
        this.connected = new SimpleBooleanProperty(connected);
        this.folder = new SimpleStringProperty(folder);
        this.media = new SimpleBooleanProperty(media);

        if(driveSerialNumber == null) {
            driveSerialNumber = getDriveSerialNumberFromPath(folder);
			this.driveSerialNumber = new SimpleStringProperty(driveSerialNumber);
			return;
		}
        this.driveSerialNumber = new SimpleStringProperty(driveSerialNumber);
    }

    //@formatter:off
	public SimpleStringProperty folder_property() {
		return folder;
	}
	public String getFolder() { return folder.get(); }
	public void setFolder(String folder) { this.folder.set(folder); }

	public BooleanProperty connected_property() {return connected; }
	public boolean isConnected() { return connected.get(); }
	public void setConnected(boolean connected) { this.connected.set(connected); }

	public SimpleBooleanProperty mediaProperty() { return media; }
	public boolean isMedia() { return this.media.get(); }
	public void setMedia(boolean media) { this.media.set(media); }

	public SimpleBooleanProperty selectedProperty() { return selected; }
	public boolean isSelected() { return selected.get(); }
	public void setSelected(boolean selected) {	this.selected.set(selected); }

	public SimpleStringProperty driveSerialNumberProperty() { return driveSerialNumber; }
	public String getDriveSerialNumber() { return driveSerialNumber.get(); }
	public void setDriveSerialNumber(String driveSerialNumber) { this.driveSerialNumber.set(driveSerialNumber); }

}
