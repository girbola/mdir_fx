

package com.girbola.controllers.folderscanner;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

public class SelectedFolder {

    private SimpleStringProperty folder;
    private SimpleBooleanProperty selected;
    private SimpleBooleanProperty connected;
	private SimpleBooleanProperty media;

	public SelectedFolder(boolean selected, boolean connected, String folder, boolean media) {
		this.selected = new SimpleBooleanProperty(selected);
		this.connected = new SimpleBooleanProperty(connected);
        this.folder = new SimpleStringProperty(folder);
		this.media = new SimpleBooleanProperty(media);
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

}
