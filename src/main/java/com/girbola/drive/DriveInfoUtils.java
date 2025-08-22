
package com.girbola.drive;

import com.girbola.Main;
import com.girbola.messages.Messages;
import com.girbola.sql.DriveInfoSQL;
import common.utils.OSHI_Utils;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;

public class DriveInfoUtils {

	private final String ERROR = DriveInfoUtils.class.getSimpleName();

	private static ObservableList<DriveInfo> drivesList_obs = FXCollections.observableArrayList();

	public static boolean hasDrivePath(List<DriveInfo> driveInfos, String drivePath, String driveSerialNumber) {
		for(DriveInfo driveInfo : driveInfos) {
			if(driveInfo.getDrivePath().equals(drivePath) && driveInfo.getSerial().equals(driveSerialNumber)) {
				return true;
			}
		}
		return false;
	}

//	public boolean exists(DriveInfo driveInfoToSearch) {
//		for (DriveInfo driveInfo : drivesList_obs) {
//			if (driveInfo.getDrivePath().equals(driveInfoToSearch.getDrivePath())) {
//				return true;
//			}
//		}
//		return false;
//	}
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

		DriveInfoSQL.addDriveInfos(drivesList_obs);

	}

	public boolean isDriveAlreadyInRegister(String drive) {
		for (DriveInfo driveInfo : drivesList_obs) {
			if (driveInfo.getDrivePath().equals(drive)) {
				return true;
			}
		}
		return false;
	}

	public void createDriveInfo_old(String path, boolean selected) {
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
	public void createDriveInfo(String path, boolean selected) {
		if (Main.getProcessCancelled()) {
			return;
		}

		File file = new File(path);

		// Find the drive in the list
		for (DriveInfo driveInfo : drivesList_obs) {
			if (driveInfo.getDrivePath().equals(file.toString())) {
				driveInfo.setSelected(selected);
				return; // Exit if the drive is found and updated
			}
		}

		// If not found, add new DriveInfo
		addDriveInfo(file, selected);
	}

	/**
	 * Helper method to create and add a new DriveInfo object to the drives list.
	 */
	private void addDriveInfo(File file, boolean selected) {
		String drivePath = file.toString();
		String driveSerialNumber = OSHI_Utils.getDriveSerialNumber(drivePath);

		// Log the path for debugging purposes
		Messages.sprintf("Adding drive info for path: " + drivePath);

		// Create and add the DriveInfo object to the list
		drivesList_obs.add(new DriveInfo(
				drivePath,
				file.getTotalSpace(),
				file.exists(),
				selected,
				driveSerialNumber
		));

		// Optional logging for the size of the list
		Messages.sprintf("drivesList size after addition: " + drivesList_obs.size());
	}
}
