/*
 @(#)Copyright:  Copyright (c) 2012-2025 All right reserved.
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */

package com.girbola.drive;

import java.nio.file.FileStore;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DriveInfo {

	private FileStore fileSystem;
	private List<Path> selectedFolders = new ArrayList<>();
	private Path drive;
	private String drivePath;
	private String identifier;
	private String serial;
	private boolean connected;
	private boolean isWritable;
	private boolean selected;
	private long driveTotalSize;



	/**
	 * Constructs a DriveInfo object with the given drive details.
	 *
	 * @param drivePath the path to the drive.
	 * @param driveTotalSize the total size of the drive in bytes.
	 * @param connected indicates whether the drive is currently connected.
	 * @param selected indicates whether the drive is selected.
	 * @param identifier a unique identifier for the drive, such as a serial number.
	 */
	public DriveInfo(String drivePath, long driveTotalSize, boolean connected, boolean selected,
			String identifier) {
		this.drivePath = drivePath;
		this.driveTotalSize = driveTotalSize;
		this.connected = connected;
		this.selected = selected;
		this.identifier = identifier;
	}

}
