/*
 @(#)Copyright:  Copyright (c) 2012-2020 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.fileinfo;

import java.time.LocalDateTime;

/**
 *
 * @author Marko Lokka
 */
public class FileInfo extends Metadata {
	private final int fileInfo_version = 2;
	/**
	 * FileInfo version 1 ================== fileInfo_version number created
	 * fileName changed from thumbfile.xml to fileinfo.dat
	 * 
	 * FileInfo version 2 ==================
	 * workDirDriveSerialNumber added
	 */
	private boolean bad;
	private boolean confirmed;
	private boolean copied;
	private boolean good;
	private boolean ignored;
	private boolean image;
	private boolean raw;
	private boolean suggested;
	private boolean tableDuplicated;
	private boolean video;
	
	private int fileInfo_id;
	private int orientation;
	private int thumb_length;
	private int thumb_offset;
	private LocalDateTime localDateTime;
	private long date;
	private long size;
	private long timeShift;
	private String camera_model;
	private String destination_Path;
	private String event;
	private String location;
	private String orgPath;
	private String tags;
	private String user;
	private String workDir;
	private String workDirDriveSerialNumber;

	public String getWorkDirDriveSerialNumber() {
		return workDirDriveSerialNumber;
	}

	public void setWorkDirDriveSerialNumber(String workDirSerial) {
		this.workDirDriveSerialNumber = workDirSerial;
	}

	
	public LocalDateTime getLocalDateTime() {
		return localDateTime;
	}

	public void setLocalDateTime(LocalDateTime localDateTime) {
		this.localDateTime = localDateTime;
	}


	@Override
	public String toString() {
		return this.orgPath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String showAllValues() {
		return "FileInfo [orgPath=" + orgPath + ", workdir=" + workDir + ", workDirDriveSerialNumber="
				+ workDirDriveSerialNumber + ", destination_Path=" + destination_Path + ", fileInfo_version="
				+ fileInfo_version + ", event=" + event + ", location=" + location + ", tags=" + tags
				+ ", + fileInfo_id=" + fileInfo_id + ", camera_model=" + camera_model + ", bad=" + bad + ", confirmed="
				+ confirmed + ", copied=" + copied + ", good=" + good + ", ignored=" + ignored + ", image=" + image
				+ ", raw=" + raw + ", suggested=" + suggested + ", video=" + video + ", orientation=" + orientation
				+ ", thumb_length=" + thumb_length + ", timeShift=" + timeShift + ", date=" + date + ", size=" + size
				+ ", tableDuplicated=" + tableDuplicated + ", thumb_offset=" + thumb_offset + " user " + user + "]";
	}

	/**
	 * 
	 * @param aOrgPath
	 * @param fileInfo_id
	 */
	public FileInfo(String aOrgPath, int fileInfo_id) {
		this.orgPath = aOrgPath;
		this.fileInfo_id = fileInfo_id;
		this.destination_Path = "";
		this.event = "";
		this.location = "";
		this.tags = "";
		this.camera_model = "Unknown";
		this.orientation = 0;
		this.timeShift = 0;
		this.bad = false;
		this.good = false;
		this.suggested = false;
		this.confirmed = false;
		this.raw = false;
		this.image = false;
		this.video = false;
		this.ignored = false;
		this.tableDuplicated = false;
		this.date = 0;
		this.size = 0;
		this.thumb_offset = 0;
		this.thumb_length = 0;
		this.user = "";
		// this.thumbInfo = new ThumbInfo(orgPath, fileInfo_id);
	}

	/**
	 *
	 */
	public FileInfo() {
		this(null, null, null, null, null, null, null, null, null, 0, 0, 0, false, false, false, false, false, false,
				false, false, false, false, 0, 0, 0, 0);
	}

	/**
	 * 
	 * @param aOrgPath
	 * @param aDestinationPath
	 * @param aEvent
	 * @param aLocation
	 * @param aTags
	 * @param aCamera_model
	 * @param user
	 * @param aOrientation
	 * @param aTimeShift
	 * @param aFileInfo_id
	 * @param aBad
	 * @param aGood
	 * @param aSuggested
	 * @param aConfirmed
	 * @param aImage
	 * @param aRaw
	 * @param aVideo
	 * @param aIgnored
	 * @param aCopied
	 * @param aTableDuplicated
	 * @param aDate
	 * @param aSize
	 * @param aThumb_offset
	 * @param aThumb_length
	 */
	public FileInfo(String aOrgPath, String aWorkDir, String aWorkDirDriveSerialNumber, String aDestinationStructure,
			String aEvent, String aLocation, String aTags, String aCamera_model, String user, int aOrientation,
			long aTimeShift, int aFileInfo_id, boolean aBad, boolean aGood, boolean aSuggested, boolean aConfirmed,
			boolean aImage, boolean aRaw, boolean aVideo, boolean aIgnored, boolean aCopied, boolean aTableDuplicated,
			long aDate, long aSize, int aThumb_offset, int aThumb_length) {
		this.orgPath = aOrgPath;
		this.workDir = aWorkDir;
		this.workDirDriveSerialNumber = aWorkDirDriveSerialNumber;
		this.destination_Path = aDestinationStructure;
		this.event = aEvent;
		this.location = aLocation;
		this.tags = aTags;
		this.camera_model = aCamera_model;
		this.orientation = aOrientation;
		this.timeShift = aTimeShift;
		this.fileInfo_id = aFileInfo_id;
		this.date = aDate;
		this.size = aSize;
		this.bad = aBad;
		this.good = aGood;
		this.ignored = aIgnored;
		this.tableDuplicated = aTableDuplicated;
		this.suggested = aSuggested;
		this.confirmed = aConfirmed;
		this.raw = aRaw;
		this.image = aImage;
		this.video = aVideo;
		this.copied = aCopied;
		this.thumb_offset = aThumb_offset;
		this.thumb_length = aThumb_length;
		this.user = user;
	}

	public String getWorkDir() {
		return workDir;
	}

	public void setWorkDir(String workDir) {
		this.workDir = workDir;
	}

	public int getFileInfo_id() {
		return this.fileInfo_id;
	}

	public void setFileInfo_id(int id) {
		this.fileInfo_id = id;
	}

	public boolean isTableDuplicated() {
		return tableDuplicated;
	}

	public void setTableDuplicated(boolean tableDuplicated) {
		this.tableDuplicated = tableDuplicated;
	}

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public String getDestination_Path() {
		return destination_Path;
	}

	public void setDestination_Path(String destinationStructure) {
		this.destination_Path = destinationStructure;
	}

	public int getFileInfo_version() {
		return fileInfo_version;
	}

	public boolean isCopied() {
		return this.copied;
	}

	public void setCopied(boolean aCopied) {
		this.copied = aCopied;
	}

	public String getCamera_model() {
		return this.camera_model;
	}

	public void setCamera_model(String camera_model) {
		this.camera_model = camera_model;
	}

	public int getThumb_offset() {
		return this.thumb_offset;
	}

	public void setThumb_offset(int thumb_offset) {
		this.thumb_offset = thumb_offset;
	}

	public int getThumb_length() {
		return this.thumb_length;
	}

	public void setThumb_length(int thumb_length) {
		this.thumb_length = thumb_length;
	}

	public long getTimeShift() {
		return timeShift;
	}

	public void setTimeShift(long timeShift) {
		this.timeShift = timeShift;
	}

	public void setOrientation(int value) {
		this.orientation = value;
	}

	public int getOrientation() {
		return this.orientation;
	}

	public void setSuggested(boolean value) {
		this.suggested = value;
	}

	public boolean isSuggested() {
		return this.suggested;
	}

	public boolean isBad() {
		return this.bad;
	}

	public void setBad(boolean value) {
		this.bad = value;
	}

	public long getDate() {
		return this.date;
	}

	public void setDate(long value) {
		this.date = value;
	}

	public long getSize() {
		return this.size;
	}

	public void setSize(long value) {
		this.size = value;
	}

	public boolean isGood() {
		return this.good;
	}

	public void setGood(boolean value) {
		this.good = value;
	}

	public String getOrgPath() {
		return this.orgPath;
	}

	public void setOrgPath(String orgPath) {
		this.orgPath = orgPath;
	}

	public boolean isConfirmed() {
		return this.confirmed;
	}

	public void setConfirmed(boolean value) {
		this.confirmed = value;
	}

	public boolean isVideo() {
		return this.video;
	}

	public void setVideo(boolean value) {
		this.video = value;
	}

	public boolean isIgnored() {
		return this.ignored;
	}

	public void setIgnored(boolean value) {
		this.ignored = value;
	}

	public boolean isRaw() {
		return this.raw;
	}

	public void setRaw(boolean value) {
		this.raw = value;
	}

	public void setImage(boolean value) {
		this.image = value;
	}

	public String getLocation() {
		return this.location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getTags() {
		return this.tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public boolean isImage() {
		return this.image;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String photoTaker) {
		this.user = photoTaker;
	}
}
