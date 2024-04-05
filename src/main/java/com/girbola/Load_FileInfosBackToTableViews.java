package com.girbola;

import com.girbola.controllers.main.Model_main;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.sql.*;
import javafx.concurrent.Task;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Load_FileInfosBackToTableViews extends Task<Boolean> {
	private Model_main model_main;
	private Connection connection;

	public Load_FileInfosBackToTableViews(Model_main aModel_Main, Connection aConnection) {
		this.model_main = aModel_Main;
		this.connection = aConnection;
	}

	@Override
	protected Boolean call() throws Exception {
		Messages.sprintf("Load_FileInfosBackToTableViews starts "
				+ Paths.get(Main.conf.getAppDataPath() + File.separator + Main.conf.getConfiguration_db_fileName()));

		if (!Files.exists(
				Paths.get(Main.conf.getAppDataPath() + File.separator + Main.conf.getConfiguration_db_fileName()))) {
			Messages.sprintf("Can't find "
					+ (Main.conf.getAppDataPath() + File.separator + Main.conf.getConfiguration_db_fileName()));
			return false;
		}

		List<FolderState> folderState_list = SQL_Utils.loadFoldersStateTo_Tables(connection, model_main);
		if(folderState_list == null) {
			return false;
		}
		if (connection == null) {
			Messages.sprintf("Connection were null at " + Load_FileInfosBackToTableViews.class.getName());
			return false;
		}
		if (folderState_list.isEmpty()) {
			Messages.sprintf("folderInfo_list were empty" + Load_FileInfosBackToTableViews.class.getName());
			return false;
		}
		if (!folderState_list.isEmpty()) {
			for (FolderState folderState : folderState_list) {
				FolderInfo folderInfo = FolderInfo_SQL.loadFolderInfo(folderState.getPath());
				boolean loadFileInfoIntoFolderInfo = FileInfo_SQL.loadFileInfoDatabase(folderInfo);
				if (!loadFileInfoIntoFolderInfo) {
					Messages.sprintfError("Something went wrong with loading folderstates!");
					cancel();
					Main.setProcessCancelled(true);
				}
//
//				if (folderState.getTableType().equals(TableType.SORTIT.getType())) {
//					if (!TableUtils.checkAllTablesForDuplicates(folderInfo, model_main.tables())) {
//						model_main.tables().getSortIt_table().getItems().add(folderInfo);
//					}
//				} else if (folderState.getTableType().equals(TableType.SORTED.getType())) {
//					if (!TableUtils.checkAllTablesForDuplicates(folderInfo, model_main.tables())) {
//						model_main.tables().getSorted_table().getItems().add(folderInfo);
//					}
//				} else if (folderState.getTableType().equals(TableType.ASITIS.getType())) {
//					if (!TableUtils.checkAllTablesForDuplicates(folderInfo, model_main.tables())) {
//						model_main.tables().getAsItIs_table().getItems().add(folderInfo);
//					}
//				} else {
//					Messages.sprintf("TableType were unknown!");
//				}
			}
		} else {
			if (!model_main.tables().getSortIt_table().getItems().isEmpty()) {
				populateTable(model_main.tables().getSortIt_table().getItems());
			}
			if (!model_main.tables().getSorted_table().getItems().isEmpty()) {
				populateTable(model_main.tables().getSorted_table().getItems());
			}
			if (!model_main.tables().getAsItIs_table().getItems().isEmpty()) {
				populateTable(model_main.tables().getAsItIs_table().getItems());
			}
		}

		return true;
	}

	private boolean populateTable(List<FolderInfo> folderInfo_list) {

		for (FolderInfo folderInfo : folderInfo_list) {
			if (folderInfo.getFolderPath()
					.equals("C:\\Users\\marko_000\\Pictures\\2018\\Väinön rippijuhlat\\Väinön kuvat UUDET\\Editoi")) {
				Messages.sprintf("Väinö found!");
			}
			boolean addTable = populateTable(folderInfo);
			if (!addTable) {
				Messages.sprintf("Skipping folder scan: " + folderInfo.getFolderPath());
			}
		}
		return true;
	}

	private boolean populateTable(FolderInfo folderInfo) {
		Messages.sprintf("populateTable getFolderFiles() is: " + folderInfo.getFolderFiles() + " connected? "
				+ folderInfo.isConnected());
		if (folderInfo.getJustFolderName().contains("Juhon vanhojen tanssit")) {
			Messages.sprintf("HMMMMMMMMM");
		}
		if (folderInfo.isConnected()) {
			Connection connection = null;
			Path path = Paths.get(folderInfo.getFolderPath());

			if (Files.exists(path)) {
				Messages.sprintf("Populating: " + path);
				connection = SqliteConnection.connector(Paths.get(folderInfo.getFolderPath()),
						Main.conf.getMdir_db_fileName());
				List<FileInfo> list = FileInfo_SQL.loadFileInfoDatabase(connection);
				int counter = 0;
				List<FileInfo> fileInfoList = new ArrayList<>();

				if (!list.isEmpty()) {
//					Messages.sprintf("FolderInfo loading: " + folderInfo.getFolderPath() + " files == " + list.size());
					for (FileInfo fileInfo : list) {
						if (fileInfo.isTableDuplicated() || fileInfo.isIgnored()) {
							counter++;
						} else {
							fileInfoList.add(fileInfo);
						}
					}
					if (fileInfoList.size() > 0) {
						folderInfo.getFileInfoList().addAll(fileInfoList);
						TableUtils.updateFolderInfo(folderInfo);
						Messages.sprintf("Counter" + counter + " fileInfoList.size() " + fileInfoList.size()
								+ " List were empty. Path" + folderInfo.getFolderPath() + " files == "
								+ folderInfo.getFolderFiles());
					} else {
						return false;
					}

				} else {
					Messages.sprintf("Counter" + counter + " fileInfoList.size() " + fileInfoList.size()
							+ " List were empty. Path" + folderInfo.getFolderPath() + " files == "
							+ folderInfo.getFolderFiles());
				}
			}
			return SQL_Utils.isDbConnected(connection);
		}
		return false;
	}

	@Override
	protected void cancelled() {
		super.cancelled();
		Messages.sprintfError("Load fileinfo back to table cancelled!");
		Main.setChanged(false);
	}

	@Override
	protected void failed() {
		super.failed();
		Messages.sprintfError("Load fileinfo back to table FAILED!");
		Main.setChanged(false);
	}

	@Override
	protected void succeeded() {
		super.succeeded();
		Messages.sprintf("Load fileinfo back to table SUCCEEDED!");
		TableUtils.refreshTableContent(model_main.tables().getSortIt_table());
		TableUtils.refreshTableContent(model_main.tables().getSorted_table());
		TableUtils.refreshTableContent(model_main.tables().getAsItIs_table());
		Main.setChanged(false);
	}

}
