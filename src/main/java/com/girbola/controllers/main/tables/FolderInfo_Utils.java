package com.girbola.controllers.main.tables;

import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.sql.FolderInfo_SQL;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

public class FolderInfo_Utils {

	private static final String ERROR = FolderInfo_Utils.class.getSimpleName();

	private static boolean findDuplicate(FolderInfo folderInfo, FileInfo fileInfo) {
		for (FileInfo findFileInfo : folderInfo.getFileInfoList()) {
			if (fileInfo.getOrgPath().equals(findFileInfo.getOrgPath())) {
				return true;
			}
		}
		return false;
	}

	public static boolean addFileInfoList(FolderInfo folderInfo, List<FileInfo> newList) {
		boolean changed = false;
		for (FileInfo fileInfo : newList) {
			boolean found = findDuplicate(folderInfo, fileInfo);
			if (!found) {
				folderInfo.getFileInfoList().add(fileInfo);
				changed = true;
			}
		}
		return changed;
	}

	/**
	 * Returns String(Event), List<FileInfo>
	 *
	 * @param folderInfo_list
	 * @return
	 */
	public static ObservableList<FileInfo> getEvents(FolderInfo folderInfo_list) {
		ObservableList<FileInfo> list = FXCollections.observableArrayList();

		for (FileInfo fileInfo : folderInfo_list.getFileInfoList()) {
			if (!fileInfo.getEvent().isEmpty()) {
				Path path = Paths.get(fileInfo.getOrgPath()).getParent();
				addToObservableFileInfoList(list, path.toString(), fileInfo);
			}
		}
		return list;
	}

	public static void addToObservableFileInfoList(
			ObservableList<FileInfo> list,
			String string,
			FileInfo fileInfo_ToFind) {

		for (FileInfo fileInfo : list) {
			if (fileInfo != fileInfo_ToFind) {
				if (fileInfo_ToFind.getEvent().equals(fileInfo.getEvent())) {
					list.add(fileInfo);
				}
			}
		}
	}

    public static FolderInfo getFolderInfo(Path folderPath) {
		FolderInfo folderInfo = FolderInfo_SQL.loadFolderInfo(folderPath.toString());
		return (folderInfo != null) ? folderInfo : new FolderInfo(folderPath);
    }

	public static void renameSourcePathToNewLocation(FolderInfo folderInfo, Path newLocation) {
		Iterator<FileInfo> iterator = folderInfo.getFileInfoList().iterator();

		while(iterator.hasNext()) {
			FileInfo fileInfo = iterator.next();
			Path originalPath = Paths.get(fileInfo.getOrgPath()).getFileName();
			fileInfo.setOrgPath(newLocation.toString() + File.separator + originalPath);
		}


		try {
            Path source = Paths.get(folderInfo.getFolderPath());
            folderInfo.setFolderPath(newLocation.toString());
            Messages.sprintf("Source path: " + " destPath: " + source + folderInfo.getFolderPath());
			Files.move(source, newLocation);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}


	}

}
