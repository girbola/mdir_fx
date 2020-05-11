package com.girbola;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.girbola.controllers.main.Model_main;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.sql.SQL_Utils;
import com.girbola.sql.SqliteConnection;

import javafx.concurrent.Task;

public class Load_FileInfosBackToTableViews extends Task<Boolean> {
	private Model_main model_main;
	private Connection connection;

	public Load_FileInfosBackToTableViews(Model_main aModel_Main, Connection aConnection) {
		this.model_main = aModel_Main;
		this.connection = aConnection;
	}

	@Override
	protected Boolean call() throws Exception {
		Messages.sprintf("Load_FileInfosBackToTableViews starts ");

		if (!Files.exists(
				Paths.get(Main.conf.getAppDataPath() + File.separator + Main.conf.getFolderInfo_db_fileName()))) {
			Messages.sprintf("Can't find "
					+ (Main.conf.getAppDataPath() + File.separator + Main.conf.getFolderInfo_db_fileName()));
			return false;
		}

		List<FolderInfo> folderInfo_list = SQL_Utils.loadFolderInfoTo_Tables(connection, model_main);
		if (connection == null) {
			Messages.sprintf("Connection were null at " + Load_FileInfosBackToTableViews.class.getName());
			return false;
		}
		if (folderInfo_list.isEmpty()) {
			Messages.sprintf("folderInfo_list were empty" + Load_FileInfosBackToTableViews.class.getName());
			return false;
		}
		if (!folderInfo_list.isEmpty()) {
			for (FolderInfo folderInfo : folderInfo_list) {
				if (folderInfo.getTableType().equals(TableType.SORTIT.getType())) {
					if (!TableUtils.checkAllTablesForDuplicates(folderInfo, model_main.tables())) {
						model_main.tables().getSortIt_table().getItems().add(folderInfo);
					}
				} else if (folderInfo.getTableType().equals(TableType.SORTED.getType())) {
					if (!TableUtils.checkAllTablesForDuplicates(folderInfo, model_main.tables())) {
						model_main.tables().getSorted_table().getItems().add(folderInfo);
					}
				} else if (folderInfo.getTableType().equals(TableType.ASITIS.getType())) {
					if (!TableUtils.checkAllTablesForDuplicates(folderInfo, model_main.tables())) {
						model_main.tables().getAsItIs_table().getItems().add(folderInfo);
					}
				} else {
					Messages.sprintf("TableType were unknown!");
				}
			}
		}
		if (!model_main.tables().getSortIt_table().getItems().isEmpty()) {
			for (FolderInfo folderInfo : model_main.tables().getSortIt_table().getItems()) {
				populateTable(folderInfo);
			}
		}
		if (!model_main.tables().getSorted_table().getItems().isEmpty()) {
			for (FolderInfo folderInfo : model_main.tables().getSorted_table().getItems()) {
				populateTable(folderInfo);
			}
		}
		if (!model_main.tables().getAsItIs_table().getItems().isEmpty()) {
			for (FolderInfo folderInfo : model_main.tables().getAsItIs_table().getItems()) {
				populateTable(folderInfo);
			}
		}

		return true;
	}

	private boolean populateTable(FolderInfo folderInfo) {
		Messages.sprintf("populateTable: " + folderInfo.getFolderFiles() + " connected? " + folderInfo.isConnected());

		if (folderInfo.isConnected()) {
			Connection connection = null;
			Path path = Paths.get(folderInfo.getFolderPath());

			if (Files.exists(path)) {
				Messages.sprintf("Populating: " + path);
				connection = SqliteConnection.connector(Paths.get(folderInfo.getFolderPath()),
						Main.conf.getFileInfo_db_fileName());
				List<FileInfo> list = SQL_Utils.loadFileInfoDatabase(connection);
				if (!list.isEmpty()) {
					folderInfo.getFileInfoList().addAll(list);
					TableUtils.updateFolderInfos_FileInfo(folderInfo);
				}
			}
			if (connection != null) {
				try {
					connection.close();
					return true;
				} catch (SQLException e) {
					e.printStackTrace();
					return false;
				}
			}
		}
		return false;
	}

	@Override
	protected void cancelled() {
		super.cancelled();
		Messages.sprintfError("Load fileinfo back to table cancelled!");
	}

	@Override
	protected void failed() {
		super.failed();
		Messages.sprintfError("Load fileinfo back to table FAILED!");
	}

	@Override
	protected void succeeded() {
		super.succeeded();
		Messages.sprintf("Load fileinfo back to table SUCCEEDED!");
		TableUtils.refreshTableContent(model_main.tables().getSortIt_table());
		TableUtils.refreshTableContent(model_main.tables().getSorted_table());
		TableUtils.refreshTableContent(model_main.tables().getAsItIs_table());
	}

}
