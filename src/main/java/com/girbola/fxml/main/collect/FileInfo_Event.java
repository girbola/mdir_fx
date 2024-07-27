package com.girbola.fxml.main.collect;

import com.girbola.fileinfo.FileInfo;

public class FileInfo_Event {

	private FileInfo fileInfo;
	private String event;

	public FileInfo getFileInfo() {
		return fileInfo;
	}

	public void setFileInfo(FileInfo fileInfo) {
		this.fileInfo = fileInfo;
	}

	public String getEvent() {
		return event;
	}

	public void setEvents(String events) {
		this.event = events;
	}

	public FileInfo_Event(FileInfo fileInfo) {
		super();
		this.fileInfo = fileInfo;
		this.event = fileInfo.getEvent();
	}

	@Override
	public String toString() {
		return fileInfo.getEvent();
	}
}
