/*
 @(#)Copyright:  Copyright (c) 2012-2019 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.configuration;

import static com.girbola.Main.conf;
import static com.girbola.configuration.Configuration_Type.WORKDIR;
import static com.girbola.messages.Messages.sprintf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.Properties;

import com.girbola.Main;
import com.girbola.controllers.main.Model_main;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.messages.Messages;
import com.girbola.sql.SQL_Utils;
import com.girbola.sql.SqliteConnection;

import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.TableColumn;
import javafx.stage.Screen;

/**
 *
 * @author Marko Lokka
 */
public class Configuration extends Configuration_defaults {

	private static final String ERROR = Configuration.class.getSimpleName();

	private Model_main model;

	private Properties prop = new Properties();
	private final String programName = "MDir - Image and Video organizer";

	public String getProgramName() {
		return programName;
	}

	public Configuration() {
		sprintf("Configuration instantiated...");
	}

	public void loadConfig() {

		if (!Files.exists(Paths.get(getAppDataPath() + File.separator + Main.conf.getConfiguration_db_fileName()))) {
			boolean sqlDatabase = conf.createConfiguration_db();
			Messages.sprintf("Configuration databases were created successfully");
		} else {
			Messages.sprintf("LOADING CONFIGURATION DATABASE");
			conf.loadConfig_SQL();
//			loadIgnored()
		}

		/*
		 * List<Path> list = ArrayUtils.readFileToArray(conf.getIgnoreListPath()); if
		 * (list.size() > 1) { for (Path file : list) {
		 * 
		 * conf.addToIgnoredList(file); } }
		 */

	}

	public boolean createConfiguration_db() {
		Connection connection = SqliteConnection.connector(Main.conf.getAppDataPath(),
				Main.conf.getConfiguration_db_fileName());
		// Create configuration table_cols which keeps tableview's widths
		Configuration_SQL_Utils.createConfigurationTable_properties(connection);
		// Create configuration for programs config like themePath, workDir, vlcPath
		// etc.
		Configuration_SQL_Utils.createConfiguration_columns(connection);
		// Inserts default params to configuration
		Configuration_SQL_Utils.insert_Configuration(connection, this);
		Configuration_SQL_Utils.createIgnoredListTable(connection);
		SQL_Utils.createFolderInfoDatabase();

		try {
			connection.close();
			return true;
		} catch (Exception e) {
			System.err.println("Can't close database file at: " + Main.conf.getAppDataPath());
			e.printStackTrace();
			return false;
		}

	}

	public Rectangle2D getScreenBounds() {
		return Screen.getPrimary().getBounds();
	}

	public void loadConfig_GUI() {
		Messages.sprintf("loadConfig_GUI Started");

		Connection connection = SqliteConnection.connector(Main.conf.getAppDataPath(),
				Main.conf.getConfiguration_db_fileName());

		Configuration_SQL_Utils.loadTables(connection, model.tables());
		try {
			connection.close();
		} catch (Exception e) {
			Messages.sprintfError("loadConfig_GUI error with closing SQL database");
		}
	}

	public boolean loadConfig_SQL() {
		Connection connection = SqliteConnection.connector(Main.conf.getAppDataPath(),
				Main.conf.getConfiguration_db_fileName());
		if (SQL_Utils.isDbConnected(connection)) {
			Configuration_SQL_Utils.loadConfiguration(connection, this);
			try {
				connection.close();
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		} else {
			System.err.println("Couldn't connect to database");
			return false;
		}

	}

	public boolean loadPropertyToMem_() throws IOException, FileNotFoundException {
		Path path = Paths.get(getIniFile());
		if (!Files.exists(path)) {
			sprintf("Config file doesn't exists");
			return true;
		}
		sprintf("loadconfig path: " + path);
		prop = new Properties();
		FileInputStream fi = new FileInputStream(path.toFile());
		prop.load(fi);

		if (prop.containsKey(WORKDIR.getType())) {
			sprintf("com.girbola.workdir exists: " + prop.getProperty(WORKDIR.getType()));
			if (!prop.getProperty(WORKDIR.getType()).isEmpty()) {
				setWorkDir(prop.getProperty(WORKDIR.getType()));
			}
		}
		return false;
	}

	private void saveTableWidths(ObservableList<TableColumn<FolderInfo, ?>> columns) {
		for (TableColumn tc : columns) {
			if (tc.getId() != null) {
				prop.setProperty(tc.getId(), String.valueOf(tc.getWidth()));
			}
		}
	}

	private void loadTableWidths(ObservableList<TableColumn<FolderInfo, ?>> columns, Properties prop) {
		for (TableColumn tc : columns) {
			if (prop.containsKey(tc.getId())) {
				tc.setPrefWidth(Double.parseDouble(prop.getProperty(tc.getId())));
			}
		}
	}

	public void setModel(Model_main model) {
		this.model = model;
	}

	public Model_main getModel() {
		return this.model;
	}
}
