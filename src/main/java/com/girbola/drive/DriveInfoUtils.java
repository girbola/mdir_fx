/*
 @(#)Copyright:  Copyright (c) 2012-2025 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.drive;

import com.girbola.Main;
import com.girbola.configuration.Configuration_SQL_Utils;
import com.girbola.controllers.folderscanner.ModelFolderScanner;
import com.girbola.messages.Messages;
import com.girbola.sql.DriveInfoSQL;
import com.girbola.sql.SQL_Utils;
import com.girbola.sql.SqliteConnection;
import common.utils.OSHI_Utils;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.sql.Connection;

public class DriveInfoUtils {

	private final String ERROR = DriveInfoUtils.class.getSimpleName();

	private ObservableList<DriveInfo> drivesList_obs = FXCollections.observableArrayList();

	public static boolean hasDrivePath(List<DriveInfo> driveInfos, String drivePath, String driveSerialNumber) {
		for(DriveInfo driveInfo : driveInfos) {
			if(driveInfo.getDrivePath().equals(drivePath) && driveInfo.getSerial().equals(driveSerialNumber)) {
				return true;
			}
		}
		return false;
	}

	public boolean exists(DriveInfo driveInfoToSearch) {
		for (DriveInfo driveInfo : drivesList_obs) {
			if (driveInfo.getDrivePath().equals(driveInfoToSearch.getDrivePath())) {
				return true;
			}
		}
		return false;
	}
//
//	public boolean loadDrives(ModelFolderScanner model_folderScanner) {
//		Connection connection = SqliteConnection.connector(Main.conf.getAppDataPath(), Main.conf.getConfiguration_db_fileName());
//
//		boolean driveInfoLoaded = DriveInfoSQL.loadDriveInfo(model_folderScanner);
//		if (driveInfoLoaded) {
//			return true;
//		} else {
//			return false;
//		}
//	}

	public ObservableList<DriveInfo> getDrivesList_obs() {
		return this.drivesList_obs;
	}

	public void saveList() {
		Connection connectionConfiguration = SqliteConnection.connector(Main.conf.getAppDataPath(), Main.conf.getConfiguration_db_fileName());
		if(!SQL_Utils.isDbConnected(connectionConfiguration)) {
			Configuration_SQL_Utils.createFolderInfosDatabase(connectionConfiguration);
			Messages.sprintf("createFolderInfoDatabase created");
		}

		DriveInfoSQL.addDriveInfos(drivesList_obs);
		try {
			connectionConfiguration.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isDriveAlreadyInRegister(String drive) {
		for (DriveInfo driveInfo : drivesList_obs) {
			if (driveInfo.getDrivePath().equals(drive)) {
				return true;
			}
		}
		return false;
	}

	public void createDriveInfo(String path, boolean selected) {
		if(Main.getProcessCancelled()) {
			return;
		}
		boolean found = false;
		File file = new File(path);
		if (!drivesList_obs.isEmpty()) {
			for (DriveInfo driveInfo : drivesList_obs) {
				if (driveInfo.getDrivePath().equals(file.toString())) {
					driveInfo.setSelected(selected);
					found = true;
					break;
				} else {
					found = false;
				}
			}
			if (!found) {
				Messages.sprintf("2createDriveInfo path: " + path);
				String driveSerialNumber = OSHI_Utils.getDriveSerialNumber(path);
//				if(driveSerialNumber == null) {
//					Messages.errorSmth(ERROR, "Drive serial number can't be read", null, Misc.getLineNumber(), false);
//				}
				drivesList_obs.add(new DriveInfo(file.toString(), file.getTotalSpace(), file.exists(), selected, driveSerialNumber));
			}
		} else {
			Messages.sprintf("3createDriveInfo path: " + path);
			String driveSerialNumber = OSHI_Utils.getDriveSerialNumber(path);
			drivesList_obs.add(new DriveInfo(file.toString(), file.getTotalSpace(), file.exists(), selected, driveSerialNumber));
			Messages.sprintf("drivesList size: " + drivesList_obs.size());

		}
	}
}
