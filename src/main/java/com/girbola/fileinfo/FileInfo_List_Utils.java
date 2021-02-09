package com.girbola.fileinfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.girbola.messages.Messages;
import com.girbola.sql.FileInfo_SQL;
import com.girbola.sql.SQL_Utils;
import com.girbola.sql.SqliteConnection;

public class FileInfo_List_Utils {

	public static boolean cleanFileInfoList(Connection connection, List<Path> rootFileList, List<FileInfo> sourceFileList) throws IOException {
		Messages.sprintfError("Connection: " + SQL_Utils.isDbConnected(connection));
//		List<Path> addToList = new ArrayList<>();
		List<Path> newFilesList = new ArrayList<>();
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
//				addToList.add(rootFile);
				FileInfo fileInfo = FileInfo_Utils.createFileInfo(rootFile);
				FileInfo_SQL.insertFileInfoToDatabase(connection, fileInfo);
				sourceFileList.add(fileInfo);
				changed = true;
			}
		}

		Messages.sprintfError("2Connection: " + SQL_Utils.isDbConnected(connection));
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
				newFilesList.add(Paths.get(fileInfo.getOrgPath()));
			}
		}

//		for (Path path : newFilesList) {
//			Messages.sprintf("newFilesList file: " + path + " file exists?: " + Files.exists(path));
//			FileInfo fileInfo = FileInfo_Utils.createFileInfo(path);
//			FileInfo_SQL.insertFileInfoToDatabase(connection, fileInfo);
//		}
		if (!newFilesList.isEmpty()) {
			Messages.sprintfError("SOME FILE WERE NOT ABLE TO REMOVE!!");
		}
		
		// Remove marked files
		Iterator<FileInfo> sourceFileListIT = sourceFileList.iterator();
		while (sourceFileListIT.hasNext()) {
			FileInfo fileInfo = sourceFileListIT.next();
			if (fileInfo != null) {
				for (Path fileToRemove : newFilesList) {
					if (fileToRemove.equals(Paths.get(fileInfo.getOrgPath()))) {
						sourceFileListIT.remove();
						FileInfo_SQL.deleteFileInfoToDatabase(connection, fileInfo);
					}
				}
			}
		}
		Messages.sprintfError("3Connection: " + SQL_Utils.isDbConnected(connection));
		
		
		return changed;
	}

}
