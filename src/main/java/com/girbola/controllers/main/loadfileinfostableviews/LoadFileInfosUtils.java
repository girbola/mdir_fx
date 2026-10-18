package com.girbola.controllers.main.loadfileinfostableviews;

import com.girbola.controllers.folderscanner.SelectedFolder;
import com.girbola.controllers.folderscanner.SelectedFolderUtils;
import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.controllers.main.tables.model.SavedFolderInfos;
import com.girbola.drive.DriveInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.persistence.folderinfo.FolderInfoDao;
import com.girbola.utils.FileInfoUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;

public final class LoadFileInfosUtils {

	public enum LoadSavedFoldersResult {
		COMPLETED,
		CANCELLED
	}

	private LoadFileInfosUtils() {
		// Utility class
	}

	public static LoadSavedFoldersResult loadSavedFoldersToTableViews(
			ModelMain modelMain,
			List<SavedFolderInfos> savedFolders,
			List<DriveInfo> currentDrives,
			BooleanSupplier processCancelled) {

		List<SavedFolderInfos> folderExists = savedFolders.stream()
				.filter(savedFolderInfos -> savedFolderInfos.resolveCurrentFolderPath(currentDrives).isPresent())
				.toList();
		List<SavedFolderInfos> folderNotExists = savedFolders.stream()
				.filter(savedFolderInfos -> savedFolderInfos.resolveCurrentFolderPath(currentDrives).isEmpty())
				.toList();

		for (SavedFolderInfos savedFolderInfos : folderNotExists) {
			Messages.sprintfError("####SavedFolderInfos folder does NOT exist: " + savedFolderInfos.getFolderPath());
		}

		for (SavedFolderInfos savedFolderInfos : folderExists) {
			Messages.sprintf("###### Loading SavedFolders savedFolderInfos: " + savedFolderInfos.getFolderPath());
			if (processCancelled.getAsBoolean()) {
				Messages.sprintf("SavedFolderInfos process cancelled");
				return LoadSavedFoldersResult.CANCELLED;
			}

			loadSavedFolderToTable(modelMain, savedFolderInfos, currentDrives);
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

		boolean folderInfoAddedToTable = modelMain.tables().addToTable(folderInfo);
		if (folderInfoAddedToTable) {
			Messages.sprintf("FolderInfo was added to table: " + folderInfo.getFolderPath() + " LINE::: " + Misc.getLineNumber());
		} else {
			Messages.sprintfError("FolderInfo were NOT able to add to table: " + folderInfo.getFolderPath() + " LINE::: " + Misc.getLineNumber());
		}
		return folderInfoAddedToTable;
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
