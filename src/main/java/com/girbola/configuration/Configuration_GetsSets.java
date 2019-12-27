/*
 @(#)Copyright:  Copyright (c) 2012-2019 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.configuration;

import static com.girbola.configuration.Configuration_defaults.programName;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Configuration_GetsSets extends Configuration_GUI {

    private Path appDataPath = Paths.get(System.getenv("APPDATA") + File.separator + programName);

    private Path saveFolder = Paths.get(appDataPath.toString());
    private String iniFile = appDataPath + File.separator + programName.toLowerCase() + "." + "dat";
    
    public String getConfiguration_db_fileName() {
        return configuration_db_fileName;
    }

    private Path ignoreListPath = Paths.get(appDataPath + File.separator + "ignoredList.dat");

    private ObservableList<Path> ignoredList = FXCollections.observableArrayList();

    // private final String driveinfo_db_fileName = "driveinfo.db";
    private final String selectedFolders_db_fileName = "selectedFolders.db";
    private final String configuration_db_fileName = "configuration.db";
    private final String folderInfo_db_fileName = "folderInfo.db";
    private final String fileInfo_db_fileName = "fileInfo.db";
    private final String thumbInfo_db_fileName = "thumbinfo.db";
  

//    private final String folderInfo_FileName = "folderInfo.xml";
    // private final String thumbFolderName = "m_images";
//    private final String thumbInfo_fileName = "thumbinfo.xml";

    private ObservableList<Path> ignoredFoldersScanList = FXCollections.observableArrayList();

//    public final String getFolderInfo_FileName() {
//	return folderInfo_FileName;
//    }

    public Path getIgnoreListPath() {
	return ignoreListPath;
    }

    public void setIgnoreListPath(Path ignoreListPath) {
	this.ignoreListPath = ignoreListPath;
    }

    public void addToIgnoredList(Path folderPath) {
	if (!ignoredList.contains(folderPath)) {
	    this.ignoredList.add(folderPath);
	}
	Collections.sort(ignoredList);
    }

    public ObservableList<Path> getIgnoredList() {
	Collections.sort(ignoredList);
	return this.ignoredList;
    }

    public ObservableList<Path> getIgnoredFoldersScanList() {
	Collections.sort(ignoredFoldersScanList);
	return this.ignoredFoldersScanList;
    }

    public void removeIgnoredList(Path path) {
	ignoredFoldersScanList.stream().filter((folder) -> (path.equals(folder))).forEachOrdered((_item) -> {
	    ignoredFoldersScanList.remove(path);
	});
    }

    public void setIgnoredFoldersScanList(ObservableList<Path> aIgnoredList) {
	this.ignoredFoldersScanList = aIgnoredList;
    }

    public String getIniFile() {
	return this.iniFile;
    }

    public Path getAppDataPath() {
	return this.appDataPath;
    }

    public void setAppDataPath(Path appDataPath) {
	this.appDataPath = appDataPath;
    }

    public Path getSaveFolder() {
	return this.saveFolder;
    }

    public void setSaveFolder(Path saveFolder) {
	this.saveFolder = saveFolder;
    }

    /**
     * @return the thumbInfo_fileName
     */
//    public String getThumbInfo_fileName() {
//	return thumbInfo_fileName;
//    }

    /**
     * @return the folderInfo_db_fileName
     */
    public final String getFolderInfo_db_fileName() {
	return folderInfo_db_fileName;
    }

    /**
     * @return the thumbInfo_db_fileName
     */
    public final String getThumbInfo_db_fileName() {
	return thumbInfo_db_fileName;
    }

    /**
     * @return the fileInfo_db_fileName
     */
    public final String getFileInfo_db_fileName() {
	return fileInfo_db_fileName;
    }

    public String getSelectedFolders_db_fileName() {
	return selectedFolders_db_fileName;
    }
}
