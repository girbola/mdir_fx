/*
 @(#)Copyright:  Copyright (c) 2012-2024 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.configuration;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Configuration_GetsSets extends ConfigurationGui {

	private final String userHome = System.getProperty("user.home");

	private ObservableList<Path> ignoredFoldersScanList = FXCollections.observableArrayList();
	private ObservableList<Path> ignoredList = FXCollections.observableArrayList();
	private Path appDataPath = Paths.get(userHome + File.separator + ".mdir");
	private Path ignoreListPath = Paths.get(appDataPath + File.separator + "ignoredList.dat");
	private final String configuration_db_fileName = "configuration.db";
	private final String mdir_db_fileName = "mdir.db";
	private final String selectedFolders_db_fileName = "selectedFolders.db";

	public final String getMdir_db_fileName() { return mdir_db_fileName; }
	public ObservableList<Path> getIgnoredFoldersScanList() { return this.ignoredFoldersScanList; }
	public Path getAppDataPath() { return this.appDataPath; }
	public Path getIgnoreListPath() { return ignoreListPath; }
	public String getConfiguration_db_fileName() { return configuration_db_fileName; }
	public void setAppDataPath(Path appDataPath) { this.appDataPath = appDataPath; }
	public void setIgnoredFoldersScanList(ObservableList<Path> aIgnoredList) { this.ignoredFoldersScanList = aIgnoredList; }
	public void setIgnoreListPath(Path ignoreListPath) { this.ignoreListPath = ignoreListPath; }

}
