/*
 @(#)Copyright:  Copyright (c) 2012-2020 All right reserved.
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */

package com.girbola.drive;

import java.nio.file.FileStore;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DriveInfo {

	private FileStore fileSystem;
	private boolean connected;
	private boolean selected;
	private String drivePath;
	private long driveTotalSize;
	private String identifier;
	private boolean isWritable;
	private List<Path> selectedFolders = new ArrayList<>();
	private Path drive;
	private String serial;

	/**
	 * 
	 * @param aDrivePath
	 * @param aDriveTotalSize
	 * @param aConnected
	 * @param aSelected
	 * @param aIdentifier
	 */
	public DriveInfo(String aDrivePath, long aDriveTotalSize, boolean aConnected, boolean aSelected,
			String aIdentifier) {
		this.drivePath = aDrivePath;
		this.driveTotalSize = aDriveTotalSize;
		this.connected = aConnected;
		this.selected = aSelected;
		this.identifier = aIdentifier;
//		this.serial = aSerial;
	}

	public boolean getSelected() {
		return this.selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public String getDrivePath() {
		return drivePath;
	}

	public boolean isConnected() {
		return connected;
	}

	public void setDrivePath(String driveLetter) {
		this.drivePath = driveLetter;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	public long getDriveTotalSize() {
		return driveTotalSize;
	}

	public void setDriveTotalSize(long driveTotalSize) {
		this.driveTotalSize = driveTotalSize;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String value) {
		this.identifier = value;
	}

	public boolean isWritable() {
		return isWritable;
	}

	public List<Path> getSelectedFolders() {
		return selectedFolders;
	}

	public void setWritable(boolean isWritable) {
		this.isWritable = isWritable;
	}

	public void setSelectedFolders(List<Path> selectedFolders) {
		this.selectedFolders = selectedFolders;
	}

	public Path getDrive() {
		return drive;
	}

	public void setDrive(Path drive) {
		this.drive = drive;
	}

}
