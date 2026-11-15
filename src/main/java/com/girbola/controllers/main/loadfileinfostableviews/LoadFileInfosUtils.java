package com.girbola.controllers.main.loadfileinfostableviews;

import com.girbola.Main;
import com.girbola.controllers.folderscanner.SelectedFolder;
import com.girbola.controllers.folderscanner.SelectedFolderUtils;
import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.controllers.main.tables.model.SavedFolderInfos;
import com.girbola.drive.DriveInfo;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.persistence.folderinfo.FolderInfoDao;
import com.girbola.persistence.fileinfo.FileInfoSqlConnectionFactory;
import com.girbola.sql.SQL_Utils;
import com.girbola.utils.FileInfoUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;

public final class LoadFileInfosUtils {

	private static final String PROCESS_STATE_NEW = "NEW";
	private static final String PROCESS_STATE_DONE = "DONE";
	private static final String PROCESS_STATE_INTERRUPTED = "INTERRUPTED";

	public enum LoadSavedFoldersResult {
		COMPLETED,
		CANCELLED
	}

	@FunctionalInterface
	public interface ProgressListener {
		void onProgress(int done, int total, String folderPath);
	}

//	private LoadFileInfosUtils() {
//		// Utility class
//	}
//
//	public static LoadSavedFoldersResult loadSavedFoldersToTableViews(
//			ModelMain modelMain,
//			List<SavedFolderInfos> savedFolders,
//			List<DriveInfo> currentDrives,
//			BooleanSupplier processCancelled) {
//		return loadSavedFoldersToTableViews(modelMain, savedFolders, currentDrives, processCancelled, null);
//	}

	public static LoadSavedFoldersResult loadSavedFoldersToTableViews(
			ModelMain modelMain,
			List<SavedFolderInfos> savedFolders,
			List<DriveInfo> currentDrives,
			BooleanSupplier processCancelled,
			ProgressListener progressListener) {

		List<SavedFolderInfos> folderExists = savedFolders.stream()
				.filter(savedFolderInfos -> savedFolderInfos.resolveCurrentFolderPath(currentDrives).isPresent())
				.toList();
		List<SavedFolderInfos> folderNotExists = savedFolders.stream()
				.filter(savedFolderInfos -> savedFolderInfos.resolveCurrentFolderPath(currentDrives).isEmpty())
				.toList();

		for (SavedFolderInfos savedFolderInfos : folderNotExists) {
			Messages.sprintfError("####SavedFolderInfos folder does NOT exist: " + savedFolderInfos.getFolderPath());
		}

		int total = folderExists.size();
		for (int i = 0; i < total; i++) {
			SavedFolderInfos savedFolderInfos = folderExists.get(i);
			Messages.sprintf("###### Loading SavedFolders savedFolderInfos: " + savedFolderInfos.getFolderPath());
			if (processCancelled.getAsBoolean()) {
				Messages.sprintf("SavedFolderInfos process cancelled");
				return LoadSavedFoldersResult.CANCELLED;
			}
			if (progressListener != null) {
				progressListener.onProgress(i, total, savedFolderInfos.getFolderPath());
			}

			try {
				loadSavedFolderToTable(modelMain, savedFolderInfos, currentDrives);
			} catch (RuntimeException e) {
				Messages.sprintfError("Failed to load saved folder " + savedFolderInfos.getFolderPath()
						+ ": " + e.getMessage());
			}
		}
		if (progressListener != null) {
			progressListener.onProgress(total, total, "");
		}
		return LoadSavedFoldersResult.COMPLETED;
	}

	public static boolean loadSavedFolderToTable(
			ModelMain modelMain,
			SavedFolderInfos savedFolderInfos,
			List<DriveInfo> currentDrives) {

		Path originalPath = toNormalizedPath(savedFolderInfos.getFolderPath()).orElse(null);
		if (originalPath == null) {
			Messages.sprintfError("SavedFolderInfos path was invalid: " + savedFolderInfos.getFolderPath());
			return false;
		}

		Optional<Path> resolvedPath = savedFolderInfos.resolveCurrentFolderPath(currentDrives);
		if (resolvedPath.isEmpty() && savedFolderInfos.hasStableDriveIdentity()) {
			Messages.sprintf("No current drive matches saved folder identity: " + originalPath);
			return false;
		}

		Path folderPath = resolvedPath.orElse(originalPath);
		if (!folderPath.equals(originalPath)) {
			savedFolderInfos.findMatchingDrive(currentDrives)
					.ifPresent(drive -> remapSelectedFolder(modelMain, originalPath, drive));
			Messages.sprintf("Resolved saved folder path from " + originalPath + " to " + folderPath
					+ " using the saved drive identity");
		}

		Messages.sprintf("=============LoadFileInfosBackToTableViews: " + folderPath
				+ " connected " + savedFolderInfos.isConnected() + " LINE::: " + Misc.getLineNumber());
		if (!SelectedFolderUtils.startsWithSelectedFolders(
				modelMain.getSelectedFolders().getSelectedFolderScanner_obs(), folderPath)) {
			return false;
		}

		SelectedFolder selectedFolder = SelectedFolderUtils.getSelectedFolderParent(folderPath.toString(), modelMain);
		if (selectedFolder == null) {
			Messages.sprintf("SelectedFolder was NULL: " + folderPath + " at LINE::: " + Misc.getLineNumber());
			return false;
		}

		FolderInfo folderInfo = loadOrCreateFolderInfo(savedFolderInfos, folderPath);
		if (folderInfo == null) {
			return false;
		}

		if (recoverProcessState(folderInfo)) {
			persistRecoveredProcessState(folderInfo);
		}

		boolean folderInfoAddedToTable = modelMain.tables().addToTable(folderInfo);
		if (folderInfoAddedToTable) {
			Messages.sprintf("FolderInfo was added to table: " + folderInfo.getFolderPath() + " LINE::: " + Misc.getLineNumber());
		} else {
			Messages.sprintfError("FolderInfo were NOT able to add to table: " + folderInfo.getFolderPath() + " LINE::: " + Misc.getLineNumber());
		}
		return folderInfoAddedToTable;
	}

	static boolean recoverProcessState(FolderInfo folderInfo) {
		if (!PROCESS_STATE_NEW.equals(folderInfo.getProcessState())
				|| folderInfo.getFileInfoList() == null) {
			return false;
		}

		List<FileInfo> eligibleFiles = folderInfo.getFileInfoList().stream()
				.filter(fileInfo -> !fileInfo.isIgnored() && !fileInfo.isTableDuplicated())
				.toList();
		if (eligibleFiles.isEmpty()) {
			return false;
		}

		long copiedFiles = eligibleFiles.stream().filter(FileInfo::isCopied).count();
		if (copiedFiles == 0) {
			return false;
		}

		String recoveredState = copiedFiles == eligibleFiles.size()
				? PROCESS_STATE_DONE
				: PROCESS_STATE_INTERRUPTED;
		folderInfo.setProcessState(recoveredState);
		Messages.sprintf("Recovered folder process state as " + recoveredState
				+ ": " + folderInfo.getFolderPath() + " (" + copiedFiles
				+ "/" + eligibleFiles.size() + " eligible files copied)");
		return true;
	}

	private static void persistRecoveredProcessState(FolderInfo folderInfo) {
		Connection connection = FileInfoSqlConnectionFactory.connectToDatabase(
				folderInfo.getFolderPath(), Main.conf.getMdir_db_fileName());
		if (connection == null) {
			Messages.sprintfError("Could not persist recovered process state for "
					+ folderInfo.getFolderPath() + ": database connection failed");
			return;
		}

		try {
			if (!FolderInfoDao.saveFolderInfo(connection, folderInfo)) {
				Messages.sprintfError("Could not persist recovered process state for "
						+ folderInfo.getFolderPath());
			}
		} finally {
			SQL_Utils.closeConnection(connection);
		}
	}

	public static void remapSelectedFolder(ModelMain modelMain, Path savedFolderPath, DriveInfo currentDrive) {
		Path savedRoot = savedFolderPath.getRoot();
		Path currentDrivePath;
		try {
			currentDrivePath = Paths.get(currentDrive.getDrivePath()).toAbsolutePath().normalize();
		} catch (RuntimeException e) {
			Messages.sprintfError("Invalid current drive path: " + currentDrive.getDrivePath()
					+ " error: " + e.getMessage());
			return;
		}
		Path currentRoot = currentDrivePath.getRoot();
		if (savedRoot == null || currentRoot == null) {
			return;
		}

		for (SelectedFolder selectedFolder : modelMain.getSelectedFolders().getSelectedFolderScanner_obs()) {
			Path selectedPath;
			try {
				selectedPath = Paths.get(selectedFolder.getFolder()).toAbsolutePath().normalize();
			} catch (RuntimeException e) {
				continue;
			}
			if (!savedFolderPath.startsWith(selectedPath) || !savedRoot.equals(selectedPath.getRoot())) {
				continue;
			}

			Path rebasedPath = currentRoot.resolve(savedRoot.relativize(selectedPath)).normalize();
			if (Files.isDirectory(rebasedPath)) {
				selectedFolder.setFolder(rebasedPath.toString());
				selectedFolder.setDriveSerialNumber(currentDrive.getIdentifier());
				selectedFolder.setConnected(true);
			}
			return;
		}
	}

	private static FolderInfo loadOrCreateFolderInfo(SavedFolderInfos savedFolderInfos, Path folderPath) {
		FolderInfo folderInfo = FolderInfoDao.loadFolderInfo(folderPath.toString());
		if (folderInfo != null) {
			return folderInfo;
		}

		if (Files.exists(folderPath)) {
			folderInfo = new FolderInfo(folderPath);
			FileInfoUtils.createFileInfo_list(folderInfo);

			savedFolderInfos.setConnected(true);
			Messages.sprintf("FolderInfo was null for some reason: " + folderPath + " LINE::: " + Misc.getLineNumber());
			return folderInfo;
		}

		savedFolderInfos.setConnected(false);
		Messages.sprintf("FolderInfo was null for some reason: " + folderPath + " LINE::: " + Misc.getLineNumber());
		return null;
	}

	private static Optional<Path> toNormalizedPath(String path) {
		if (path == null || path.isBlank()) {
			return Optional.empty();
		}

		try {
			return Optional.of(Paths.get(path).toAbsolutePath().normalize());
		} catch (RuntimeException e) {
			return Optional.empty();
		}
	}
}
