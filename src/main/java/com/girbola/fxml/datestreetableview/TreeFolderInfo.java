package com.girbola.fxml.datestreetableview;

import com.girbola.fileinfo.FileInfo;
import javafx.beans.property.SimpleStringProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class TreeFolderInfo {
	public List<FileInfo> getFileInfoList() {
		return fileInfoList;
	}

	public void setFileInfoList(List<FileInfo> fileInfoList) {
		this.fileInfoList = fileInfoList;
	}

	private SimpleStringProperty year = new SimpleStringProperty("");
	private SimpleStringProperty month = new SimpleStringProperty("");
	private SimpleStringProperty day = new SimpleStringProperty("");
	private Set<String> possibleEvents = new TreeSet<>();
	
	public Set<String> getPossibleEvents() {
		return possibleEvents;
	}

	public void setPossibleEvents(Set<String> possibleEvents) {
		this.possibleEvents = possibleEvents;
	}

	public SimpleStringProperty getDay() {
		return day;
	}

	public void setDay(SimpleStringProperty day) {
		this.day = day;
	}

	private SimpleStringProperty files = new SimpleStringProperty("");
	private List<FileInfo> fileInfoList = new ArrayList<>();

	public SimpleStringProperty getYear() {
		return year;
	}

	public void setYear(SimpleStringProperty year) {
		this.year = year;
	}

	public SimpleStringProperty getMonth() {
		return month;
	}

	public void setMonth(SimpleStringProperty month) {
		this.month = month;
	}

	public SimpleStringProperty getFiles() {
		return files;
	}

	public void setFiles(SimpleStringProperty files) {
		this.files = files;
	}
}
