
package com.girbola.drive;

import com.girbola.Main;
import com.girbola.controllers.folderscanner.SelectedFolder;
import com.girbola.messages.Messages;
import com.girbola.persistence.drive.DriveInfoDao;
import common.utils.OSHI_Utils;
import java.nio.file.Files;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import oshi.hardware.HWDiskStore;


public class DriveInfoUtils {

	private final String ERROR = DriveInfoUtils.class.getSimpleName();

	private DriveInfoDao driveInfoDao;
	private static ObservableList<DriveInfo> drivesList_obs = FXCollections.observableArrayList();

	public DriveInfoUtils() {
		this.driveInfoDao = new DriveInfoDao();
	}


	public DriveInfoUtils(DriveInfoDao driveInfoDao) {
		this.driveInfoDao = driveInfoDao;
	}

	public static boolean hasDrivePath(List<DriveInfo> driveInfos, String drivePath, String driveSerialNumber) {
		for(DriveInfo driveInfo : driveInfos) {
			if(driveInfo.getDrivePath().equals(drivePath) && driveInfo.getSerial().equals(driveSerialNumber)) {
				return true;
			}
		}
		return false;
	}

    public static List<DriveInfo> getCurrentDriveInfos() {
        List<HWDiskStore> hardwareDisks = OSHI_Utils.getHWDiskStore();
        List<DriveInfo> currentDrives = new ArrayList<>();
        for (Path root : FileSystems.getDefault().getRootDirectories()) {
            if (!Files.exists(root)) {
                continue;
            }

            HWDiskStore diskStore = OSHI_Utils.findDiskStoreForRoot(root, hardwareDisks);
            if (diskStore == null) {
                continue;
            }
            OSHI_Utils.DriveIdentity identity = OSHI_Utils.resolveDriveIdentity(root.toString(), hardwareDisks);
            DriveInfo driveInfo = new DriveInfo(root.toString(), diskStore.getSize(), true, false,
                    identity.identifier(), identity.serial());
            driveInfo.setPartitionUuid(identity.partitionUuid());
            driveInfo.setPartitionIdentification(identity.partitionIdentification());
            driveInfo.setPartitionMountPoint(identity.partitionMountPoint());
            currentDrives.add(driveInfo);
        }
        return currentDrives;
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
//		Connection connection = FileInfoSqlConnectionFactory.connector(Main.conf.getAppDataPath(), Main.conf.getConfiguration_db_fileName());
//
//		boolean driveInfoLoaded = DriveInfoDao.loadDriveInfo(model_folderScanner);
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
		driveInfoDao.addDriveInfos(drivesList_obs);
	}

	public boolean isDriveAlreadyInRegister(String drive) {
		for (DriveInfo driveInfo : drivesList_obs) {
			if (driveInfo.getDrivePath().equals(drive)) {
				return true;
			}
		}
		return false;
	}
//
//	public void createDriveInfo_old(String path, boolean selected) {
//		if(Main.getProcessCancelled()) {
//			return;
//		}
//		boolean found = false;
//		File file = new File(path);
//		if (!drivesList_obs.isEmpty()) {
//			for (DriveInfo driveInfo : drivesList_obs) {
//				if (driveInfo.getDrivePath().equals(file.toString())) {
//					driveInfo.setSelected(selected);
//					found = true;
//					break;
//				} else {
//					found = false;
//				}
//			}
//			if (!found) {
//				Messages.sprintf("2createDriveInfo path: " + path);
//				String driveSerialNumber = OSHI_Utils.getDriveSerialNumber(path);
////				if(driveSerialNumber == null) {
////					Messages.errorSmth(ERROR, "Drive serial number can't be read", null, Misc.getLineNumber(), false);
////				}
//				drivesList_obs.add(new DriveInfo(file.toString(), file.getTotalSpace(), file.exists(), selected, driveSerialNumber));
//			}
//		} else {
//			Messages.sprintf("3createDriveInfo path: " + path);
//			String driveSerialNumber = OSHI_Utils.getDriveSerialNumber(path);
//			drivesList_obs.add(new DriveInfo(file.toString(), file.getTotalSpace(), file.exists(), selected, driveSerialNumber));
//			Messages.sprintf("drivesList size: " + drivesList_obs.size());
//
//		}
//	}

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

    public static Path changeDriveLetterWithPath(Path originalPath, String newDriveLetter) {
        String selectedFolderPathWithoutDriveLetter = parseDrivePathFromPath(originalPath);
		String replacementDriveRoot = parseDriveLetterFromPath(Paths.get(newDriveLetter));

		return Paths.get(replacementDriveRoot, selectedFolderPathWithoutDriveLetter);
    }

    public static String parseDrivePathFromPath(Path drivePath) {
        Path root = drivePath.getRoot();
        if (root == null) {
            throw new IllegalArgumentException("The provided path does not have a root: " + drivePath);
        }
        return drivePath.toString().replace(root.toString(), "");
    }

    public static String parseDriveLetterFromPath(Path drivePath) {
        Messages.sprintf("parseDriveLetterFromPath: " + drivePath);
        return drivePath.getRoot().toString();
    }

    /**
	 * Helper method to create and add a new DriveInfo object to the drives list.
	 */
	private void addDriveInfo(File file, boolean selected) {
		String drivePath = file.toString();
		OSHI_Utils.DriveIdentity driveIdentity = OSHI_Utils.resolveDriveIdentity(drivePath);

		// Log the path for debugging purposes
		Messages.sprintf("Adding drive info for path: " + drivePath);

		// Create and add the DriveInfo object to the list
		DriveInfo driveInfo = new DriveInfo(
				drivePath,
                driveIdentity.diskSize() > 0 ? driveIdentity.diskSize() : file.getTotalSpace(),
				file.exists(),
				selected,
                driveIdentity.identifier(),
                driveIdentity.serial()
		);
		driveInfo.setPartitionUuid(driveIdentity.partitionUuid());
		driveInfo.setPartitionIdentification(driveIdentity.partitionIdentification());
		driveInfo.setPartitionMountPoint(driveIdentity.partitionMountPoint());
		drivesList_obs.add(driveInfo);

		// Optional logging for the size of the list
		Messages.sprintf("drivesList size after addition: " + drivesList_obs.size());
	}

    public static Path resolveConnectivityForSelectedFolder(SelectedFolder selectedFolder, DriveInfo currentDriveDriveInfo) {

        if (isWindows()) {
            Messages.sprintf("SelectedFolderUtils selectedFolder.getFolder() is on Windows. Attempting to remap drive letter for folder: " + selectedFolder.getFolder() + " currentDriveDriveInfo: " + currentDriveDriveInfo.getDrive().toString());
            String selectedFolderPathWithoutDriveLetter = parseDrivePathFromPath(Paths.get(selectedFolder.getFolder()));
            if (selectedFolderPathWithoutDriveLetter.isEmpty()) {
                Messages.sprintfError("SelectedFolderUtils selectedFolderPathWithoutDriveLetter is empty for folder: " + selectedFolder.getFolder());
                return null;
            }
            Path thePath = Paths.get(currentDriveDriveInfo.getDrive().toString(), selectedFolderPathWithoutDriveLetter);
            Messages.sprintf("SelectedFolderUtils thePath: " + thePath);
            if (Files.exists(thePath)) {
                Messages.sprintf("###Drive letter changed from selectedFolder.folder: " + selectedFolder.getFolder() + " to " + thePath + " selectedFolderPathWithoutDriveLetter: " + selectedFolderPathWithoutDriveLetter);
                return thePath;
            }
        }

        return null;

    }

    public static boolean isWindows() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.contains("win");
    }

}
