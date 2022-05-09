package com.girbola.controllers.main.tables;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.girbola.fileinfo.FileInfo;

public class Events {
	
	private Set<Events> eventList = new HashSet<>();

	public Set<Events> getEventList() {
		return eventList;
	}

	public void setEventList(Set<Events> eventList) {
		this.eventList = eventList;
	}

	public List<FileInfo> getFileInfo() {
		return fileInfo;
	}

	public void setFileInfo(List<FileInfo> fileInfo) {
		this.fileInfo = fileInfo;
	}

	private List<FileInfo> fileInfo =  new ArrayList<>();

	
	

}
