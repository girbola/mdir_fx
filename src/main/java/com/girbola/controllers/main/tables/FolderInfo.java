/*
 @(#)Copyright:  Copyright (c) 2012-2020 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.main.tables;

import static com.girbola.Main.simpleDates;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.girbola.fileinfo.FileInfo;

import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author Marko Lokka
 */
public class FolderInfo implements TableValues_inf {

	private IntegerProperty status;
	private List<FileInfo> fileInfoList;
	private SimpleBooleanProperty changed;
	private SimpleBooleanProperty connected;
	private SimpleBooleanProperty ignored;
	private SimpleDoubleProperty dateDifference;
	private SimpleIntegerProperty badFiles;
	private SimpleIntegerProperty confirmed;
	private SimpleIntegerProperty copied;
	private SimpleIntegerProperty folderFiles;
	private SimpleIntegerProperty folderImageFiles;
	private SimpleIntegerProperty folderRawFiles;
	private SimpleIntegerProperty folderVideoFiles;
	private SimpleIntegerProperty goodFiles;
	private SimpleIntegerProperty suggested;
	private SimpleLongProperty folderSize;
	private SimpleStringProperty justFolderName;
	private SimpleStringProperty folderPath;
	private SimpleStringProperty maxDate;
	private SimpleStringProperty minDate;
	private SimpleStringProperty state;
	private SimpleStringProperty tableType;

	public FolderInfo() {
		this.badFiles = new SimpleIntegerProperty(0);
		this.changed = new SimpleBooleanProperty(false);
		this.confirmed = new SimpleIntegerProperty(0);
		this.connected = new SimpleBooleanProperty(false);
		this.copied = new SimpleIntegerProperty(0);
		this.dateDifference = new SimpleDoubleProperty(0);
		this.folderFiles = new SimpleIntegerProperty(0);
		this.folderImageFiles = new SimpleIntegerProperty(0);
		this.justFolderName = new SimpleStringProperty("");
		this.folderPath = new SimpleStringProperty("");
		this.folderRawFiles = new SimpleIntegerProperty(0);
		this.folderSize = new SimpleLongProperty(0);
		this.folderVideoFiles = new SimpleIntegerProperty(0);
		this.goodFiles = new SimpleIntegerProperty(0);
		this.ignored = new SimpleBooleanProperty(false);
		this.maxDate = new SimpleStringProperty("0");
		this.minDate = new SimpleStringProperty("0");
		this.state = new SimpleStringProperty("");
		this.status = new SimpleIntegerProperty(0);
		this.suggested = new SimpleIntegerProperty(0);
		this.tableType = new SimpleStringProperty("");
		//		Bindings.subtract(folderFiles, copied);
		Bindings.subtract(folderFiles, badFiles);

	}

	/**
	 *
	 * @param folderPath
	 */
	public FolderInfo(Path folderPath, String tableType, boolean connected) {
		this.badFiles = new SimpleIntegerProperty(0);
		this.changed = new SimpleBooleanProperty(false);
		this.confirmed = new SimpleIntegerProperty(0);
		this.connected = new SimpleBooleanProperty(connected);
		this.copied = new SimpleIntegerProperty(0);
		this.dateDifference = new SimpleDoubleProperty(0);
		this.fileInfoList = new ArrayList<>();
		this.folderFiles = new SimpleIntegerProperty(0);
		this.folderImageFiles = new SimpleIntegerProperty(0);
		this.justFolderName = new SimpleStringProperty(folderPath.getFileName().toString());
		this.folderPath = new SimpleStringProperty(folderPath.toString());
		this.folderRawFiles = new SimpleIntegerProperty(0);
		this.folderSize = new SimpleLongProperty(0);
		this.folderVideoFiles = new SimpleIntegerProperty(0);
		this.goodFiles = new SimpleIntegerProperty(0);
		this.ignored = new SimpleBooleanProperty(false);
		this.maxDate = new SimpleStringProperty("0");
		this.minDate = new SimpleStringProperty("0");
		this.state = new SimpleStringProperty("");
		this.status = new SimpleIntegerProperty(0);
		this.suggested = new SimpleIntegerProperty(0);
		this.tableType = new SimpleStringProperty(tableType);
		//		Bindings.subtract(folderFiles, copied);
		Bindings.subtract(folderFiles, badFiles);
	}

	/**
	 * 
	 * @param folderPath
	 */
	public FolderInfo(Path folderPath) {
		this.badFiles = new SimpleIntegerProperty(0);
		this.changed = new SimpleBooleanProperty(false);
		this.confirmed = new SimpleIntegerProperty(0);
		this.connected = new SimpleBooleanProperty(false);
		this.copied = new SimpleIntegerProperty(0);
		this.dateDifference = new SimpleDoubleProperty(0);
		this.fileInfoList = new ArrayList<>();
		this.folderFiles = new SimpleIntegerProperty(0);
		this.folderImageFiles = new SimpleIntegerProperty(0);
		this.justFolderName = new SimpleStringProperty(folderPath.getFileName().toString());
		this.folderPath = new SimpleStringProperty(folderPath.toString());
		this.folderRawFiles = new SimpleIntegerProperty(0);
		this.folderSize = new SimpleLongProperty(0);
		this.folderVideoFiles = new SimpleIntegerProperty(0);
		this.goodFiles = new SimpleIntegerProperty(0);
		this.ignored = new SimpleBooleanProperty(false);
		this.maxDate = new SimpleStringProperty("0");
		this.minDate = new SimpleStringProperty("0");
		this.state = new SimpleStringProperty("");
		this.status = new SimpleIntegerProperty(0);
		this.suggested = new SimpleIntegerProperty(0);
		this.tableType = new SimpleStringProperty("");
		//		Bindings.subtract(folderFiles, copied);
		Bindings.subtract(folderFiles, badFiles);

	}

	@Override
	public void setStatus(int value) {
		this.status.set(value);
	}

	@Override
	public int getStatus() {
		return this.status.get();
	}

	@Override
	public void setState(String value) {
		this.state.set(value);
	}

	@Override
	public String getState() {
		return this.state.get();
	}

	@Override
	public int getBadFiles() {
		return this.badFiles.get();
	}

	@Override
	public void setBadFiles(int value) {
		this.badFiles.set(value);
		TableUtils.updateStatus(status, this.folderFiles.get(), this.badFiles.get(), this.suggested.get());
	}

	@Override
	public double getDateDifferenceRatio() {
		return this.dateDifference.get();
	}

	@Override
	public void setDateDifferenceRatio(double value) {
		this.dateDifference.set(value);
	}

	@Override
	public int getFolderFiles() {
		return this.folderFiles.get();
	}

	@Override
	public void setFolderFiles(int value) {
		this.folderFiles.set(value);
	}

	@Override
	public int getFolderImageFiles() {
		return this.folderImageFiles.get();
	}

	@Override
	public void setFolderImageFiles(int value) {
		this.folderImageFiles.set(value);
		TableUtils.updateStatus(status, this.folderFiles.get(), this.badFiles.get(), this.suggested.get());
	}

	@Override
	public String getJustFolderName() {
		return this.justFolderName.get();
	}

	@Override
	public void setJustFolderName(String value) {
		this.justFolderName.set(value);
	}

	@Override
	public int getFolderRawFiles() {
		return this.folderRawFiles.get();
	}

	@Override
	public void setFolderRawFiles(int value) {
		this.folderRawFiles.set(value);
		TableUtils.updateStatus(status, this.folderFiles.get(), this.badFiles.get(), this.suggested.get());
	}

	@Override
	public long getFolderSize() {
		return this.folderSize.get();
	}

	@Override
	public void setFolderSize(long value) {
		this.folderSize.set(value);
	}

	@Override
	public int getFolderVideoFiles() {
		return this.folderVideoFiles.get();
	}

	@Override
	public void setFolderVideoFiles(int value) {
		this.folderVideoFiles.set(value);
		TableUtils.updateStatus(status, this.folderFiles.get(), this.badFiles.get(), this.suggested.get());
	}

	@Override
	public int getGoodFiles() {
		return this.goodFiles.get();
	}

	@Override
	public void setGoodFiles(int value) {
		this.goodFiles.set(value);
	}

	@Override
	public int getCopied() {
		return this.copied.get();
	}

	@Override
	public void setCopied(int value) {
		this.copied.set(value);
	}

	@Override
	public String getMaxDate() {
		return this.maxDate.get();
	}

	@Override
	public void setMaxDate(String value) {
		this.maxDate.set(value);
	}

	@Override
	public String getMinDate() {
		return this.minDate.get();
	}

	@Override
	public void setMinDate(String value) {
		this.minDate.set(value);
	}

	@Override
	public int getSuggested() {
		return this.suggested.get();
	}

	@Override
	public void setSuggested(int value) {
		this.suggested.set(value);
		TableUtils.updateStatus(status, this.folderFiles.get(), this.badFiles.get(), this.suggested.get());
	}

	@Override
	public String getFolderPath() {
		return this.folderPath.get();
	}

	@Override
	public void setFolderPath(String value) {
		this.folderPath.set(value);
	}

	@Override
	public Map<String,
			String> getFileList() {
		Map<String,
				String> map = new HashMap<>();
		Iterator<FileInfo> it = getFileInfoList().iterator();
		while (it.hasNext()) {
			FileInfo fileInfo = it.next();
			if (!fileInfo.isIgnored()) {
				map.put(fileInfo.getOrgPath(), simpleDates.getSdf_ymd_hms_minusDots_default().format(fileInfo.getDate()));
			}
		}
		return map;
	}

	@Override
	public List<FileInfo> getFileInfoList() {
		return this.fileInfoList;
	}

	@Override
	public void setFileInfoList(List<FileInfo> value) {
		this.fileInfoList = value;
	}

	@Override
	public SimpleIntegerProperty badFiles_prop() {
		return badFiles;
	}

	@Override
	public SimpleDoubleProperty dateDifferenceRatio_prop() {
		return dateDifference;
	}

	@Override
	public SimpleIntegerProperty folderFiles_prop() {
		return this.folderFiles;
	}

	@Override
	public SimpleIntegerProperty folderImageFiles_prop() {
		return this.folderImageFiles;
	}

	@Override
	public SimpleStringProperty folderPath_prop() {
		return this.folderPath;
	}

	@Override
	public SimpleIntegerProperty folderRawFiles_prop() {
		return this.folderRawFiles;
	}

	@Override
	public SimpleLongProperty folderSize_prop() {
		return this.folderSize;
	}

	@Override
	public SimpleIntegerProperty folderVideoFiles_prop() {
		return this.folderVideoFiles;
	}

	@Override
	public SimpleIntegerProperty goodFiles_prop() {
		return this.goodFiles;
	}

	@Override
	public SimpleIntegerProperty copied_property() {
		return this.copied;
	}

	@Override
	public SimpleStringProperty maxDate_prop() {
		return this.maxDate;
	}

	@Override
	public SimpleStringProperty minDate_prop() {
		return this.minDate;
	}

	@Override
	public IntegerProperty status_property() {
		return status;
	}

	@Override
	public SimpleStringProperty state_property() {
		return state;
	}

	@Override
	public SimpleIntegerProperty suggested_prop() {
		return this.suggested;
	}

	@Override
	public SimpleBooleanProperty changed_property() {
		return this.changed;
	}

	@Override
	public boolean getChanged() {
		return this.changed.get();
	}

	@Override
	public void setChanged(boolean changed) {
		this.changed.set(changed);
	}

	@Override
	public SimpleIntegerProperty confirmed_property() {
		return confirmed;
	}

	@Override
	public int getConfirmed() {
		return this.confirmed.get();
	}

	@Override
	public void setConfirmed(int value) {
		this.confirmed.set(value);
	}

	@Override
	public SimpleStringProperty tableType_property() {
		return tableType;
	}

	@Override
	public String getTableType() {
		return tableType.get();
	}

	@Override
	public void setTableType(String value) {
		this.tableType.set(value);
	}

	@Override
	public SimpleBooleanProperty connected_property() {
		return connected;
	}

	@Override
	public boolean isConnected() {
		return this.connected.get();
	}

	@Override
	public void setConnected(boolean connected) {
		this.connected.set(connected);
	}

	@Override
	public SimpleBooleanProperty ignored_prop() {
		return this.ignored;
	}

	@Override
	public boolean getIgnored() {
		return this.ignored.get();
	}

	@Override
	public void setIgnored(boolean value) {
		this.ignored.set(value);
	}

}
