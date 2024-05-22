/*
 @(#)Copyright:  Copyright (c) 2012-2024 All right reserved.
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */

package com.girbola.controllers.folderscanner;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

public class SelectedFolder {

    private SimpleStringProperty folder;
    private SimpleBooleanProperty selected;
    private SimpleBooleanProperty connected;

    public SelectedFolder(boolean selected, boolean connected, String folder) {
		this.selected = new SimpleBooleanProperty(selected);
		this.connected = new SimpleBooleanProperty(connected);
        this.folder = new SimpleStringProperty(folder);
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

	public boolean isSelected() { return selected.get(); }

	public SimpleBooleanProperty selectedProperty() { return selected; }

	public void setSelected(boolean selected) {	this.selected.set(selected); }

}
