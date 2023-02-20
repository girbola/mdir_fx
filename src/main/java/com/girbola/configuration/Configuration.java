/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.configuration;

import static com.girbola.Main.conf;
import static com.girbola.messages.Messages.sprintf;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;

import com.girbola.Main;
import com.girbola.controllers.main.Model_main;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.sql.SQL_Utils;
import com.girbola.sql.SqliteConnection;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

/**
 *
 * @author Marko Lokka
 */
public class Configuration extends Configuration_defaults {

	private static final String ERROR = Configuration.class.getSimpleName();

	private Model_main model_Main;

	private final String programName = "MDir - Image and Video Organizer";

	protected SimpleDoubleProperty imageViewXProperty = new SimpleDoubleProperty(0);
	protected SimpleDoubleProperty imageViewYProperty = new SimpleDoubleProperty(0);

	public double getImageViewXPosition() {
		return imageViewXProperty.get();
	}

	public void setImageViewXProperty(double value) {
		this.imageViewXProperty.set(value);
	}

	public double getImageViewYPosition() {
		return imageViewYProperty.get();
	}

	public void setImageViewYProperty(double value) {
		this.imageViewYProperty.set(value);
	}

	public String getProgramName() {
		return programName;
	}

	public Configuration() {
		sprintf("Configuration instantiated...");
	}

	public void loadConfig() {
		if (!Files.exists(Paths.get(getAppDataPath() + File.separator + Main.conf.getConfiguration_db_fileName()))) {
			boolean sqlDatabaseCreated = conf.createConfiguration_db();
			if (!sqlDatabaseCreated) {
				Messages.errorSmth(ERROR, "Could not be able to create configuration file", null, Misc.getLineNumber(),
						true);
				Messages.sprintf("Could not be able to create configuration file failed");
			} else {
				Messages.sprintf("Configuration databases were created successfully");
			}
		} else {
			Messages.sprintf("LOADING CONFIGURATION DATABASE");
			try {
				conf.loadConfig_SQL();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean createConfiguration_db() {
		Messages.sprintf("creatingConfiguration_DB at: " + Main.conf.getAppDataPath() + File.separator + Main.conf.getConfiguration_db_fileName());
		Connection connection = SqliteConnection.connector(Main.conf.getAppDataPath(),
				Main.conf.getConfiguration_db_fileName());
		try {
			connection.setAutoCommit(false);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// Create configuration table_cols which keeps tableview's widths
		Configuration_SQL_Utils.createConfigurationTable_properties(connection);
		// Create configuration for programs config like themePath, workDir, vlcPath
		// etc.
		Configuration_SQL_Utils.createConfiguration_Table(connection);
		try {
			connection.commit();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// Inserts default params to configuration
		Configuration_SQL_Utils.insert_Configuration(connection, this);
		Configuration_SQL_Utils.createIgnoredListTable(connection);
		SQL_Utils.createFoldersStatesDatabase(connection);

		try {
			connection.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}

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

		ObservableList<Screen> screensForRectangle = Screen.getScreensForRectangle(
				Main.scene_Switcher.getScene_main().getX(), Main.scene_Switcher.getScene_main().getY(),
				Main.scene_Switcher.getScene_main().getWidth(), Main.scene_Switcher.getScene_main().getHeight());
		return screensForRectangle.get(0).getBounds();
//			return Screen.getPrimary().getBounds();
	}

	public void loadConfig_GUI() {
		Messages.sprintf("loadConfig_GUI Started: " + Main.conf.getAppDataPath());
		Connection connection = null;
		boolean createDatabase = false;
		Path configFile = Paths
				.get(Main.conf.getAppDataPath() + File.separator);
		configFile.toFile().setWritable(true);
		configFile.toFile().setReadable(true);
		
		if (!Files.exists(configFile) && Files.isWritable(configFile)) {
			try {
				Path createDirectories = Files.createDirectories(configFile);
				Path createFile = Files.createFile(Paths.get(configFile.toString() + Main.conf.getConfiguration_db_fileName()));
				createFile.toFile().setWritable(true);
				createFile.toFile().setReadable(true);
				if (!Files.exists(createDirectories)) {
					Messages.errorSmth(ERROR, Main.bundle.getString("cannotCreateConfigFile"), null,
							Misc.getLineNumber(), true);
				} else {
					createDatabase = true;
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			connection = SqliteConnection.connector(Main.conf.getAppDataPath(),
					Main.conf.getConfiguration_db_fileName());

			if (createDatabase) {
				Configuration_SQL_Utils.createConfiguration_Table(connection);
			} else {
				Configuration_SQL_Utils.loadTables(connection, model_Main.tables());
			}
//			try {
//				connection.close();
//			} catch (Exception e) {
//				Messages.sprintfError("loadConfig_GUI error with closing SQL database");
//			}
		}
	}

	public boolean loadConfig_SQL() throws SQLException {
		Messages.sprintf("Loading SQL config: " + Main.conf.getWorkDir());
		Connection connection = SqliteConnection.connector(Main.conf.getAppDataPath(),
				Main.conf.getConfiguration_db_fileName());
		if (SQL_Utils.isDbConnected(connection)) {
			connection.setAutoCommit(false);
			Configuration_SQL_Utils.loadConfiguration(connection, Main.conf);
			Messages.sprintf("Loading stopped. SQL config: " + Main.conf.getWorkDir());
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

	public void setModel(Model_main model) {
		this.model_Main = model;
	}

	public Model_main getModel() {
		return this.model_Main;
	}
}
