package com.girbola.controllers.main.tables;

import java.util.List;

import com.girbola.fileinfo.FileInfo;

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

}
