package com.girbola.controllers.main.tables;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.girbola.fileinfo.FileInfo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

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

	public static void addToObservableFileInfoList(ObservableList<FileInfo> list, String string, FileInfo fileInfo_ToFind) {
		
		for (FileInfo fileInfo : list) {
			if(fileInfo != fileInfo_ToFind) {
				if(fileInfo_ToFind.getEvent().equals(fileInfo.getEvent())) {
					list.add(fileInfo);
				}
			}
		}
	}

	public static List<FolderInfo> getLocation(List<FolderInfo> folderInfo_list) {

		return null;
	}

//	public static void moveToAnotherTable_(Tables tables, TableView<FolderInfo> table, String tableType) {
//
//		if(tableType.equals(TableType.SORTIT.getType())) {
//			tables.getSorted_table().getItems().addAll(table.getSelectionModel().getSelectedItems());
//			tables.getSortIt_table().getItems().removeAll(table.getSelectionModel().getSelectedItems());
//		}
//		
//	}

}
