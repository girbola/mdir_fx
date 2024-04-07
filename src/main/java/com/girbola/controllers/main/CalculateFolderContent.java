/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.main;

import com.girbola.Main;
import com.girbola.controllers.loading.LoadingProcessTask;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.sql.SQL_Utils;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Task;
import javafx.scene.control.TableView;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.girbola.Main.conf;
import static com.girbola.fileinfo.FileInfoUtils.createFileInfo_list;
import static com.girbola.messages.Messages.sprintf;

/**
 *
 * @author Marko Lokka
 */
public class CalculateFolderContent extends Task<Void> {

	private final String ERROR = CalculateFolderContent.class.getSimpleName();

	private Model_main model;
	private IntegerProperty total;
	private IntegerProperty counter = new SimpleIntegerProperty();

	public CalculateFolderContent(Model_main aModel, LoadingProcessTask aLoadingProcess_Task, IntegerProperty total) {
		this.model = aModel;
		this.total = total;
		this.counter = this.total;
	}

	@Override
	protected Void call() throws Exception {
		init();
		return null;
	}

	private void loopFolderInfos(final String tableType) {
		TableView<FolderInfo> tableView;

		if (Main.getProcessCancelled()) {
			cancel();
			return;
		}
		if (tableType.equals(TableType.SORTED.getType())) {
			tableView = model.tables().getSorted_table();
		} else if (tableType.equals(TableType.SORTIT.getType())) {
			tableView = model.tables().getSortIt_table();
		} else if (tableType.equals(TableType.ASITIS.getType())) {
			tableView = model.tables().getAsItIs_table();
		} else {
			Messages.errorSmth(ERROR, "Can't find Tabletype: " + tableType, null, Misc.getLineNumber(), true);
			return;
		}
		for (FolderInfo folderInfo : tableView.getItems()) {
			sprintf("tableType is: " + tableType + " createFileInfo_list: " + folderInfo.getFolderPath());
			if (Main.getProcessCancelled()) {
				cancel();
				break;
			}
			handleFolderInfo(folderInfo, tableView);
			folderInfo.setTableType(tableType);
		}
		TableUtils.refreshTableContent(tableView);
	}

	private void init() {
		checkIFCancelled();
		updateMessage(TableType.SORTIT.getType());
		loopFolderInfos(TableType.SORTIT.getType());
		
		checkIFCancelled();
		updateMessage(TableType.SORTED.getType());
		loopFolderInfos(TableType.SORTED.getType());
		
		checkIFCancelled();
		updateMessage(TableType.ASITIS.getType());
		loopFolderInfos(TableType.ASITIS.getType());
		checkIFCancelled();
	}

	private void checkIFCancelled() {
		if (Main.getProcessCancelled()) {
			cancel();
			return;
		}
	}

	private void handleFolderInfo(FolderInfo folderInfo, TableView<FolderInfo> tableView) {
		Path mdirDatabaseFilePath = Paths
				.get(folderInfo.getFolderPath() + File.separator + conf.getMdir_db_fileName());
		Messages.sprintf("handleFolderInfo trying to find folderinfo path: " + mdirDatabaseFilePath);
		if (Files.exists(mdirDatabaseFilePath)) {

			FolderInfo loaded_FolderInfo = null;
			try {
				loaded_FolderInfo = SQL_Utils.loadFolderInfoCurrentDir(mdirDatabaseFilePath.getParent());
			if (loaded_FolderInfo == null) {
					Messages.sprintf(
							"loaded_FolderInfo were null at: " + mdirDatabaseFilePath + " Creating new one");
					List<FileInfo> listOfFileInfos = createFileInfo_list(folderInfo);
					loaded_FolderInfo = new FolderInfo(mdirDatabaseFilePath.getParent());
					folderInfo.setFileInfoList(listOfFileInfos);
					if (!folderInfo.getFileInfoList().isEmpty()) {
						TableUtils.updateFolderInfo(folderInfo);
						counter.set(counter.get() - 1);
						updateProgress(counter.get(), total.get());
						updateMessage(folderInfo.getFolderPath());
						TableUtils.refreshTableContent(tableView);
						return;
					}
				}
				folderInfo.setBadFiles(loaded_FolderInfo.getBadFiles());
				if (loaded_FolderInfo.getFileInfoList() == null) {
					List<FileInfo> li = createFileInfo_list(folderInfo);
					folderInfo.setFileInfoList(li);
					if (!folderInfo.getFileInfoList().isEmpty()) {
						TableUtils.updateFolderInfo(folderInfo);
						counter.set(counter.get() - 1);
						updateProgress(counter.get(), total.get());
						updateMessage(folderInfo.getFolderPath());
						TableUtils.refreshTableContent(tableView);
						return;
					}
				}
			} catch (Exception ex) {
				Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
			}
			if (loaded_FolderInfo != null) {
				folderInfo.setFileInfoList(loaded_FolderInfo.getFileInfoList());
				TableUtils.updateFolderInfo(folderInfo);
				Messages.sprintf("folderInfo were not zero: " + folderInfo.getFolderPath());
			} else {
				Messages.sprintf("folderInfo were were zero: " + mdirDatabaseFilePath);
			}
			counter.set(counter.get() - 1);
			updateProgress(counter.get(), total.get());
			updateMessage(folderInfo.getFolderPath());
			TableUtils.refreshTableContent(tableView);

		} else {
			List<FileInfo> li = createFileInfo_list(folderInfo);
			folderInfo.setFileInfoList(li);
			if (!folderInfo.getFileInfoList().isEmpty()) {
				TableUtils.updateFolderInfo(folderInfo);
				counter.set(counter.get() - 1);
				updateProgress(counter.get(), total.get());
				updateMessage(folderInfo.getFolderPath());
				TableUtils.refreshTableContent(tableView);

			}
		}

	}
}
