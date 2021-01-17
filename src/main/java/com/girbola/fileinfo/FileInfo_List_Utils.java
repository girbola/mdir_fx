package com.girbola.fileinfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.girbola.fileinfo.FileInfo;
import com.girbola.fileinfo.FileInfo_Utils;
import com.girbola.messages.Messages;

public class FileInfo_List_Utils {

	public static boolean cleanFileInfoList(List<Path> rootFileList, List<FileInfo> sourceFileList) throws IOException {
		List<Path> addToList = new ArrayList<>();
		List<Path> removeFromList = new ArrayList<>();
		boolean changed = false;
		for (Path rootFile : rootFileList) {
			boolean exists = false;
			for (FileInfo fileInfo : sourceFileList) {
				Path orgFile = Paths.get(fileInfo.getOrgPath());
				if (Files.exists(orgFile)) {
					if (Files.size(rootFile) == Files.size(orgFile)) {
						exists = true;
						break;
					}
				}
			}
			if (!exists) {
				addToList.add(rootFile);
				changed = true;
			}
		}

		// Create FileInfo for new files
		for (Path addToListFile : addToList) {
			FileInfo fileInfo = FileInfo_Utils.createFileInfo(addToListFile);
			sourceFileList.add(fileInfo);
		}

		// Check if folder has removed files
		for (FileInfo fileInfo : sourceFileList) {
			boolean exists = false;

			Path orgFile = Paths.get(fileInfo.getOrgPath());
			for (Path rootFile : rootFileList) {
				if (Files.exists(orgFile)) {
					if (Files.size(rootFile) == Files.size(orgFile)) {
						exists = true;
						break;
					}
				}
			}
			if (!exists) {
				removeFromList.add(Paths.get(fileInfo.getOrgPath()));
			}
		}

		// Remove marked files
		Iterator<FileInfo> sourceFileListIT = sourceFileList.iterator();
		while (sourceFileListIT.hasNext()) {
			FileInfo fileInfo = sourceFileListIT.next();
			if (fileInfo != null) {
				for (Path fileToRemove : removeFromList) {
					if (fileToRemove.equals(Paths.get(fileInfo.getOrgPath()))) {
						sourceFileListIT.remove();
					}
				}
			}
		}

		for (Path path : removeFromList) {
			Messages.sprintfError("SOME FILE WERE NOT ABLE TO REMOVE: " + path);
		}
		if (!removeFromList.isEmpty()) {
			Messages.warningText("SOME FILE WERE NOT ABLE TO REMOVE!!");
		}
		return changed;
	}

}
