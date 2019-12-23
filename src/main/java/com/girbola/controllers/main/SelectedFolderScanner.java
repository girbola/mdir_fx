/*
 @(#)Copyright:  Copyright (c) 2012-2019 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.main;

import java.sql.Connection;

import org.sqlite.SQLiteConnection;

import com.girbola.Main;
import com.girbola.controllers.folderscanner.SelectedFolder;
import com.girbola.controllers.misc.Misc_GUI;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.sql.SQL_Utils;
import com.girbola.sql.SqliteConnection;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class SelectedFolderScanner {

	private ObservableList<SelectedFolder> selectedFolderScanner_obs = FXCollections.observableArrayList();
	private final String ERROR = SelectedFolderScanner.class.getSimpleName();

	public ObservableList<SelectedFolder> getSelectedFolderScanner_obs() {
		return this.selectedFolderScanner_obs;
	}

	public void setSelectedFolderScanner_obs(ObservableList<SelectedFolder> selectedFolderScanner_list) {
		this.selectedFolderScanner_obs = selectedFolderScanner_list;
	}

	public void save_SelectedFolders_toSQL() {
		Connection connection = SqliteConnection.connector(Main.conf.getAppDataPath(), Main.conf.getFolderInfo_db_fileName());
		SQL_Utils.createFolderInfoDatabase(connection);
		SQL_Utils.createSelectedFoldersTable(connection);
		if(!SQL_Utils.isDbConnected(connection)) {
			Messages.errorSmth(ERROR, "Can't connect to " + Main.conf.getFolderInfo_db_fileName() + " database", null, Misc.getLineNumber(), false);
			return;
		}
		SQL_Utils.insertSelectedFolders_List_ToDB(connection, selectedFolderScanner_obs);
		if (connection != null) {
			try {
				connection.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public boolean load_SelectedFolders_UsingSQL(Model_main model_Main) {
		Connection connection = SqliteConnection.connector(Main.conf.getAppDataPath(), Main.conf.getFolderInfo_db_fileName());
		if (connection == null) {
			return false;
		}
		if (SQL_Utils.isDbConnected(connection)) {
			Messages.sprintf("load_SelectedFolders_UsingSQL loading....");
			SQL_Utils.loadFolders_list(connection, model_Main);
			return true;
		} else {
			Messages.sprintf("Nothing to load");
			return false;
		}
	}

}
