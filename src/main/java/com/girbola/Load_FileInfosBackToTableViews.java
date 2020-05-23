package com.girbola;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
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
			populateTable(model_main.tables().getSortIt_table().getItems().iterator());
		}
		if (!model_main.tables().getSorted_table().getItems().isEmpty()) {
			populateTable(model_main.tables().getSorted_table().getItems().iterator());
		}
		if (!model_main.tables().getAsItIs_table().getItems().isEmpty()) {
			populateTable(model_main.tables().getAsItIs_table().getItems().iterator());
		}

		return true;
	}

	private boolean populateTable(Iterator<FolderInfo> folderInfo_it) {
		Iterator<FolderInfo> sortit = folderInfo_it;
		while (sortit.hasNext()) {
			FolderInfo folderInfo = sortit.next();
			boolean addTable = populateTable(folderInfo);
			if (!addTable) {
				sortit.remove();
				return false;
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
				int counter = 0;
				List<FileInfo> fileInfoList = new ArrayList<>();

				if (!list.isEmpty()) {
//					Messages.sprintf("FolderInfo loading: " + folderInfo.getFolderPath() + " files == " + list.size());
					for (FileInfo fileInfo : list) {
						if (fileInfo.isTableDuplicated() || fileInfo.isCopied() || fileInfo.isIgnored()) {
							counter++;
						} else {
							fileInfoList.add(fileInfo);
						}
					}
					if (fileInfoList.size() > 0) {

						folderInfo.getFileInfoList().addAll(fileInfoList);
						TableUtils.updateFolderInfos_FileInfo(folderInfo);
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
