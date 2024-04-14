/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.main;

import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.fileinfo.FileInfo;
import javafx.collections.ObservableList;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.girbola.messages.Messages.sprintf;

/**
 *
 * @author Marko Lokka
 */
public class FolderAnalyzer {

	private Map<FileInfo, FileInfo> duplicatedFolders = new HashMap<>();
	private boolean firstRun;
	private FolderInfo current_FolderInfo;
	private ObservableList<FolderInfo> listSorted;

	public FolderAnalyzer(ObservableList<FolderInfo> listSorted) {
		this.listSorted = listSorted;
	}

	public void addToMap(FileInfo fileInfo, FileInfo fileInfo_current) {
		for (Entry<FileInfo, FileInfo> entry : duplicatedFolders.entrySet()) {
			if (entry.getKey().equals(fileInfo) && entry.getValue().equals(fileInfo_current)) {
				return;
			}
		}
		duplicatedFolders.put(fileInfo, fileInfo_current);
	}

	public void analyzeFolderInfo(FolderInfo current_FolderInfo) {
		for (FolderInfo folderInfo : listSorted) {
			if (!folderInfo.getFolderPath().equals(current_FolderInfo.getFolderPath())) {
				// date
				List<FileInfo> listSorted_fileInfo = folderInfo.getFileInfoList();
				for (FileInfo fileInfo : listSorted_fileInfo) {
					Path src = Paths.get(fileInfo.getOrgPath());
					for (FileInfo fileInfo_current : current_FolderInfo.getFileInfoList()) {
						Path dest = Paths.get(fileInfo_current.getOrgPath());
						if (src.getFileName().equals(dest.getFileName()) && fileInfo.getSize() != fileInfo_current.getSize()) {
							addToMap(fileInfo, fileInfo_current);
							sprintf("Adding to map: " + fileInfo.getOrgPath());
						}
					}
				}

			}

		}
	}

	public Map<FileInfo, FileInfo> getDuplicatedFolders() {
		return duplicatedFolders;
	}

	public void clear() {
		duplicatedFolders.clear();
	}

}
