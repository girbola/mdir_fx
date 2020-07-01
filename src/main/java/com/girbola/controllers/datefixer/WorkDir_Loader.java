package com.girbola.controllers.datefixer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.girbola.Main;
import com.girbola.controllers.main.SQL_Enums;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.sql.FileInfo_SQL;
import com.girbola.sql.SQL_Utils;
import com.girbola.sql.SqliteConnection;

public class WorkDir_Loader {
	private Path folder;
	private List<FolderInfo> folderInfo_list = new ArrayList<>();
	private List<FileInfo> fileInfo_list = new ArrayList<>();

	public WorkDir_Loader(Path folder) {
		if(folder == null) {
			Messages.warningText("folder were null!!!");
		}
		if (Files.exists(folder)) {
			this.folder = folder;
		}
	}

	public void loadFileInfosToArray(Path workDir) {
		if (workDir == null) {
			Messages.warningText(Main.bundle.getString("workDirHasNotBeenSet"));
			return;
		}
		if (!Files.exists(workDir)) {
			Messages.warningText(Main.bundle.getString("workDirHasNotBeenSet"));
			return;
		}
		Connection connection = SqliteConnection.connector(workDir, SQL_Enums.WORKDIR.getType());
		if (fileInfo_list.addAll(FileInfo_SQL.loadFileInfoDatabase(connection))) {
			Messages.sprintf("workDir loaded: " + workDir);
		} else {
			Messages.sprintf("Can't find current workDir: " + workDir);
		}

	}

	public void loadFolderInfo(Path path) {

	}

	public List<FolderInfo> getFolderInfo_list() {
		return folderInfo_list;
	}
}
