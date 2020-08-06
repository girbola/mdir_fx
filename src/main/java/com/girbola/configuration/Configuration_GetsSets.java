/*
 @(#)Copyright:  Copyright (c) 2012-2020 All right reserved. 
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

	private final String userHome = System.getProperty("user.home");

	private Path saveFolder = Paths.get(appDataPath.toString());
	private String iniFile = appDataPath + File.separator + programName.toLowerCase() + "." + "dat";

	public String getConfiguration_db_fileName() {
		return configuration_db_fileName;
	}

	private Path ignoreListPath = Paths.get(appDataPath + File.separator + "ignoredList.dat");

	private ObservableList<Path> ignoredList = FXCollections.observableArrayList();

	private final String selectedFolders_db_fileName = "selectedFolders.db";
	private final String configuration_db_fileName = "configuration.db";
	private final String mdir_db_fileName = "mdir.db";

	private ObservableList<Path> ignoredFoldersScanList = FXCollections.observableArrayList();

	public Path getIgnoreListPath() {
		return ignoreListPath;
	}

	public void setIgnoreListPath(Path ignoreListPath) {
		this.ignoreListPath = ignoreListPath;
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
	 * @return the fileInfo_db_fileName
	 */
	public final String getMdir_db_fileName() {
		return mdir_db_fileName;
	}

	public String getSelectedFolders_db_fileName() {
		return selectedFolders_db_fileName;
	}

	public String getUserHome() {
		return userHome;
	}
}
