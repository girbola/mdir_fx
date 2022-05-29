/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.main.tables;

import java.util.List;
import java.util.Map;

import com.girbola.fileinfo.FileInfo;

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
public interface TableValues_Base {

	//@formatter:off
    public boolean getChanged();
    public boolean isConnected();
    public boolean getIgnored();
    public double getDateDifferenceRatio();
    public int getBadFiles();
    public int getConfirmed();
    public int getCopied();
    public int getFolderFiles();
    public int getFolderImageFiles();
    public int getFolderRawFiles();
    public int getFolderVideoFiles();
    public int getGoodFiles();
    public int getStatus();
    public int getSuggested();
    public IntegerProperty status_property();
    public List<FileInfo> getFileInfoList();
    public long getFolderSize();
    public Map<String, String> getFileList();
    public SimpleBooleanProperty connected_property();
    public SimpleBooleanProperty ignored_prop();
    public SimpleDoubleProperty dateDifferenceRatio_prop();
    public SimpleIntegerProperty badFiles_prop();
    public SimpleIntegerProperty copied_property();
    public SimpleIntegerProperty folderFiles_prop();
    public SimpleIntegerProperty folderImageFiles_prop();
    public SimpleIntegerProperty folderRawFiles_prop();
    public SimpleIntegerProperty folderVideoFiles_prop();
    public SimpleIntegerProperty goodFiles_prop();
    public SimpleIntegerProperty suggested_prop();
    public SimpleLongProperty folderSize_prop();
//    public SimpleStringProperty event_prop();
    public SimpleStringProperty folderPath_prop();
    public SimpleStringProperty maxDate_prop();
    public SimpleStringProperty minDate_prop();
    public SimpleStringProperty state_property();
//    public String getEvent();
    public String getFolderPath();
    public String getMaxDate();
    public String getMinDate();
    public String getState();
    public void setBadFiles(int value);
    public void setChanged(boolean changed);
    public void setConfirmed(int value);
    public void setConnected(boolean value);
    public void setCopied(int value);
    public void setDateDifferenceRatio(double value);
//    public void setEvent(String value);
    public void setFileInfoList(List<FileInfo> fileInfo);
    public void setFolderFiles(int value);
    public void setFolderImageFiles(int value);
    public void setFolderPath(String value);
    public void setFolderRawFiles(int value);
    public void setFolderSize(long value);
    public void setFolderVideoFiles(int value);
    public void setGoodFiles(int value);
    public void setIgnored(boolean value);
    public void setMaxDate(String value);
    public void setMinDate(String value);
    public void setState(String value);
    public void setStatus(int value);
    public void setSuggested(int value);
	public SimpleBooleanProperty changed_property();
	public SimpleIntegerProperty confirmed_property();
	public SimpleStringProperty tableType_property();
	public String getTableType();
	public void setTableType(String value);
	public String getJustFolderName();
	public void setJustFolderName(String value);



}
