/*
 @(#)Copyright:  Copyright (c) 2012-2019 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.configuration;

import static com.girbola.Main.bundle;
import static com.girbola.Main.conf;
import static com.girbola.configuration.Configuration_Type.BETTERQUALITYTHUMBS;
import static com.girbola.configuration.Configuration_Type.CONFIRMONEXIT;
import static com.girbola.configuration.Configuration_Type.ID_COUNTER;
import static com.girbola.configuration.Configuration_Type.SAVEFOLDER;
import static com.girbola.configuration.Configuration_Type.SAVETHUMBS;
import static com.girbola.configuration.Configuration_Type.SHOWFULLPATH;
import static com.girbola.configuration.Configuration_Type.SHOWHINTS;
import static com.girbola.configuration.Configuration_Type.SHOWTOOLTIPS;
import static com.girbola.configuration.Configuration_Type.THEMEPATH;
import static com.girbola.configuration.Configuration_Type.VLCPATH;
import static com.girbola.configuration.Configuration_Type.VLCSUPPORT;
import static com.girbola.configuration.Configuration_Type.WORKDIR;
import static com.girbola.messages.Messages.sprintf;
import static com.girbola.misc.Misc.getLineNumber;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.girbola.Main;
import com.girbola.controllers.main.Model_main;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.controllers.misc.Misc_GUI;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.sql.SQL_Utils;
import com.girbola.sql.SqliteConnection;

import common.utils.ArrayUtils;
import javafx.beans.property.Property;
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

	private void addProperty(Configuration_Type THEMEPATH1, String type) {
		prop.setProperty(type, type);
	}

	public Rectangle2D getScreenBounds() {
		return Screen.getPrimary().getBounds();
	}

	public void saveConfig_() throws FileNotFoundException {
		sprintf("saveConfig");
		Path iniFile = Paths.get(getIniFile());
		if (Files.exists(iniFile)) {
			PrintWriter pw = new PrintWriter(iniFile.toFile());
			pw.write("");
			pw.flush();
			pw.close();

			sprintf("File is empty now and closed");
		} else {
			sprintf("Config file did not exists");
		}

		prop.setProperty(WORKDIR.getType(), getWorkDir());
		prop.setProperty(THEMEPATH.getType(), getThemePath());
		prop.setProperty(VLCPATH.getType(), getVlcPath());
		prop.setProperty(SAVEFOLDER.getType(), getSaveFolder().toString());

		prop.setProperty(SHOWHINTS.getType(), Boolean.toString(isShowHints()));
		prop.setProperty(SAVETHUMBS.getType(), Boolean.toString(isSavingThumb()));
		prop.setProperty(BETTERQUALITYTHUMBS.getType(), Boolean.toString(isBetterQualityThumbs()));

		prop.setProperty(CONFIRMONEXIT.getType(), Boolean.toString(isConfirmOnExit()));
		prop.setProperty(SHOWFULLPATH.getType(), Boolean.toString(isShowFullPath()));
		prop.setProperty(SHOWTOOLTIPS.getType(), Boolean.toString(isShowTooltips()));
		prop.setProperty(VLCSUPPORT.getType(), Boolean.toString(isVlcSupport()));
		prop.setProperty(ID_COUNTER.getType(), Integer.toString(getId_counter().get()));

		// saveTableWidths(model.tables().getSortIt_table().getColumns());
		// saveTableWidths(model.tables().getSorted_table().getColumns());
		// saveTableWidths(model.tables().getAsItIs_table().getColumns());

		Configuration_SQL_Utils.saveTableWidths(model.tables());

		ArrayUtils.saveList(Main.conf.getIgnoredFoldersScanList(), Main.conf.getIgnoreListPath());
		saveConfigToFile(prop);
	}

	/**
	 *
	 * @param prop
	 */
	private void saveConfigToFile(Properties prop) {

		FileOutputStream out = null;
		try {
			out = new FileOutputStream(getIniFile());
			sprintf("saveConfigToFile: " + getIniFile());
		} catch (FileNotFoundException ex) {
			Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
			Messages.errorSmth(ERROR, bundle.getString("cannotSaveConfigFile"), ex, Misc.getLineNumber(), true);
		}

		try {
			prop.store(out, bundle.getString("configFileChangeWarning"));
		} catch (IOException ex) {
			Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
			Messages.errorSmth(ERROR, bundle.getString("cannotPreferFileAction"), ex, Misc.getLineNumber(), true);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException ex) {
					Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
					Messages.errorSmth(ERROR, bundle.getString("cannotPreferFileAction"), ex, getLineNumber(), true);
				}
			}
		}
	}

	public void loadConfig_GUI() {
		Messages.sprintf("loadConfig_GUI Started");
		if (prop == null) {
			try {
				if (loadPropertyToMem()) {
					return;
				}
			} catch (IOException ex) {
				Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		if (model.tables().getSortIt_table() == null) {
			Messages.sprintf("model.tables().getSortIt_table() were null!!");
			Misc_GUI.fastExit();
		}
		Connection connection = SqliteConnection.connector(Main.conf.getAppDataPath(),
				Main.conf.getConfiguration_db_fileName());
		Configuration_SQL_Utils.loadTables(connection, model.tables());
		if (SQL_Utils.isDbConnected(connection)) {
			try {
				connection.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// loadTableWidths(model.tables().getSortIt_table().getColumns(), prop);
		// loadTableWidths(model.tables().getSorted_table().getColumns(), prop);
		// loadTableWidths(model.tables().getAsItIs_table().getColumns(), prop);

		List<Path> ignoredList = ArrayUtils.readFileToArray(conf.getIgnoreListPath());
		for (Path file : ignoredList) {
			conf.addToIgnoredList(file);
		}
		ignoredList = null;
	}

	public boolean loadConfig_SQL() {
		Connection connection = SqliteConnection.connector(Main.conf.getAppDataPath(),
				Main.conf.getConfiguration_db_fileName());
		if (SQL_Utils.isDbConnected(connection)) {
			Configuration_SQL_Utils.loadConfig(connection, this);
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

	public void loadConfig() throws IOException {
		if (loadPropertyToMem()) {
			sprintf("loadPropertyToMem succeeded");
			return;
		}
		if (prop == null) {
			sprintf("loadConfig prop were null");
			return;
		}
		if (prop.containsKey(THEMEPATH.getType())) {
			sprintf("ThemePath is: " + prop.getProperty(THEMEPATH.getType()));
			if (!prop.getProperty(THEMEPATH.getType()).isEmpty()) {
				setThemePath(prop.getProperty(THEMEPATH.getType()));
			} else {
				addProperty(THEMEPATH, getThemePath());
			}
		}
		if (prop.containsKey(VLCPATH.getType())) {
			Messages.sprintf("vlcpath: " + getVlcPath());

			if (!prop.getProperty(VLCPATH.getType()).isEmpty()) {
				setVlcPath(prop.getProperty(VLCPATH.getType()));
				Messages.sprintf("vlcpath: " + getVlcPath());
				if (Files.exists(Paths.get(getVlcPath()))) {
					VLCJDiscovery.initVlc();
					VLCJDiscovery.discovery(Paths.get(getVlcPath()));
				} else {
					VLCJDiscovery.discovery(null);
				}

			}
		} else {
			Messages.sprintf("vlcpath: " + getVlcPath());
			addProperty(VLCPATH, getVlcPath());
		}
		if (prop.containsKey(SAVEFOLDER.getType())) {
			if (!prop.getProperty(SAVEFOLDER.getType()).isEmpty()) {
				setSaveFolder(Paths.get(prop.getProperty(SAVEFOLDER.getType())));
			}
		} else {
			addProperty(SAVEFOLDER, getSaveFolder().toString());
		}

		if (prop.containsKey(SHOWHINTS.getType())) {
			if (!prop.getProperty(SHOWHINTS.getType()).isEmpty()) {
				setShowHints(Boolean.parseBoolean(prop.getProperty(SHOWHINTS.getType())));
			}
		} else {
			addProperty(SHOWHINTS, String.valueOf(isShowHints()));
		}
		if (prop.containsKey(SAVETHUMBS.getType())) {
			if (!prop.getProperty(SAVETHUMBS.getType()).isEmpty()) {
				setSavingThumb(Boolean.parseBoolean(prop.getProperty(SAVETHUMBS.getType())));
			}
		} else {
			addProperty(SAVETHUMBS, String.valueOf(isSavingThumb()));
		}
		if (prop.containsKey(CONFIRMONEXIT.getType())) {
			if (!prop.getProperty(CONFIRMONEXIT.getType()).isEmpty()) {
				setConfirmOnExit(Boolean.parseBoolean(prop.getProperty(CONFIRMONEXIT.getType())));
			}
		} else {
			addProperty(CONFIRMONEXIT, String.valueOf(isConfirmOnExit()));
		}

		if (prop.containsKey(SHOWFULLPATH.getType())) {
			if (!prop.getProperty(SHOWFULLPATH.getType()).isEmpty()) {
				setShowFullPath(Boolean.parseBoolean(prop.getProperty(SHOWFULLPATH.getType())));
				sprintf("isShowFullPAth: " + isShowFullPath());
			}
		} else {
			addProperty(SHOWFULLPATH, String.valueOf(isShowFullPath()));
		}
		if (prop.containsKey(SHOWTOOLTIPS.getType())) {
			if (!prop.getProperty(SHOWTOOLTIPS.getType()).isEmpty()) {
				setShowTooltips(Boolean.parseBoolean(prop.getProperty(SHOWTOOLTIPS.getType())));
			}
		} else {
			addProperty(SHOWTOOLTIPS, String.valueOf(isShowTooltips()));
		}
		if (prop.containsKey(BETTERQUALITYTHUMBS.getType())) {
			if (!prop.getProperty(BETTERQUALITYTHUMBS.getType()).isEmpty()) {
				setShowTooltips(Boolean.parseBoolean(prop.getProperty(BETTERQUALITYTHUMBS.getType())));
			}
		} else {
			addProperty(BETTERQUALITYTHUMBS, String.valueOf(isBetterQualityThumbs()));
		}

		if (prop.containsKey(VLCSUPPORT.getType())) {
			if (!prop.getProperty(VLCSUPPORT.getType()).isEmpty()) {
				setVlcSupport(Boolean.parseBoolean(prop.getProperty(VLCSUPPORT.getType())));
			}
		} else {
			addProperty(VLCSUPPORT, String.valueOf(isVlcSupport()));
		}
	}

	public boolean loadPropertyToMem() throws IOException, FileNotFoundException {
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

	public boolean createSQL_Databases() {
		if (!Files.exists(Paths.get(
				Main.conf.getAppDataPath().toString() + File.separator + Main.conf.getConfiguration_db_fileName()))) {

			Connection connection = SqliteConnection.connector(Main.conf.getAppDataPath(),
					Main.conf.getConfiguration_db_fileName());
			Configuration_SQL_Utils.createConfigurationTable_properties(connection);
			Configuration_SQL_Utils.createConfiguration(connection);

			Configuration_SQL_Utils.insert_Configuration(connection, this);
//		Configuration_SQL_Utils.insert_ConfigurationTables(connection, this);

			if (Configuration_SQL_Utils.createConfiguration(connection)) {
				try {
					connection.close();
				} catch (Exception e) {
					System.err.println("Can't close database file at: " + Main.conf.getAppDataPath());
					e.printStackTrace();
					return false;
				}
				return true;
			} else {
				sprintf("Can't create sql configuration database!");
				try {
					connection.close();
				} catch (Exception e) {
					System.err.println("Can't close database file at: " + Main.conf.getAppDataPath());
					e.printStackTrace();
					return false;
				}
				return false;
			}

		} else {
			Messages.sprintf("Configuration database did exists.");
			return false;
		}
	}

}
