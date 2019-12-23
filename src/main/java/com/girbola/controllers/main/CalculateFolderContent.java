/*
 @(#)Copyright:  Copyright (c) 2012-2019 All right reserved.
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.main;

import static com.girbola.Main.conf;
import static com.girbola.fileinfo.FileInfo_Utils.createFileInfo_list;
import static com.girbola.messages.Messages.sprintf;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.girbola.Main;
import com.girbola.controllers.loading.LoadingProcess_Task;
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

/**
 *
 * @author Marko Lokka
 */
public class CalculateFolderContent extends
		Task<Void> {

	private final String ERROR = CalculateFolderContent.class.getSimpleName();

	private Model_main model;
	private IntegerProperty total;
	private IntegerProperty counter = new SimpleIntegerProperty();

	public CalculateFolderContent(Model_main aModel, LoadingProcess_Task aLoadingProcess_Task, IntegerProperty total) {
		this.model = aModel;
		this.total = total;
		this.counter = this.total;
	}

	@Override
	protected Void call() throws Exception {
		init();
		return null;
	}

	private void init() {
		if (Main.getProcessCancelled()) {
			return;
		}
		updateMessage(": SortIt");
		// lpt.setMessage("SortIt");
		for (FolderInfo folderInfo : model.tables().getSortIt_table().getItems()) {
			sprintf("sortit createFileInfo_list: " + folderInfo.getFolderPath());
			if (Main.getProcessCancelled()) {
				cancel();
				break;
			}
			handleFolderInfo(folderInfo, model.tables().getSortIt_table());
			folderInfo.setTableType(TableType.SORTIT.getType());
		}
		TableUtils.refreshTableContent(model.tables().getSortIt_table());

		if (Main.getProcessCancelled()) {
			cancel();
			return;
		}
		sprintf("Starting Sorted");
		updateMessage(" Sorted");
		for (FolderInfo folderInfo : model.tables().getSorted_table().getItems()) {
			sprintf("sorted createFileInfo_list: " + folderInfo.getFolderPath());
			if (Main.getProcessCancelled()) {
				cancel();
				break;
			}
			handleFolderInfo(folderInfo, model.tables().getSorted_table());
			folderInfo.setTableType(TableType.SORTED.getType());
		}
		TableUtils.refreshTableContent(model.tables().getSorted_table());
		if (Main.getProcessCancelled()) {
			return;
		}
		for (FolderInfo folderInfo : model.tables().getAsItIs_table().getItems()) {
			sprintf("asitis createFileInfo_list: " + folderInfo.getFolderPath());

			if (Main.getProcessCancelled()) {
				cancel();
				break;
			}
			handleFolderInfo(folderInfo, model.tables().getAsItIs_table());
			folderInfo.setTableType(TableType.ASITIS.getType());
		}
		TableUtils.refreshTableContent(model.tables().getAsItIs_table());

	}

	private void handleFolderInfo(FolderInfo folderInfo, TableView<FolderInfo> tableView) {
		Path thumbFilePath = Paths.get(folderInfo.getFolderPath() + File.separator + conf.getFolderInfo_FileName());
		Messages.sprintf("sortit thumbFilePath is: " + thumbFilePath);
		if (Files.exists(thumbFilePath)) {

			FolderInfo loaded_FolderInfo = null;
			try {
				loaded_FolderInfo = SQL_Utils.loadFolderInfo(thumbFilePath, SQL_Enums.FOLDERINFO.getType(),
						Main.conf.getFolderInfo_db_fileName());
				//XMLFunctions.loadXMLData(thumbFilePath);
				if (loaded_FolderInfo == null) {
					Messages.sprintf("loaded_FolderInfo were null!");
					List<FileInfo> li = createFileInfo_list(folderInfo);
					loaded_FolderInfo = new FolderInfo(thumbFilePath.getParent());
					folderInfo.setFileInfoList(li);
					if (!folderInfo.getFileInfoList().isEmpty()) {
						TableUtils.updateFolderInfos_FileInfo(folderInfo);
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
						TableUtils.updateFolderInfos_FileInfo(folderInfo);
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
				TableUtils.updateFolderInfos_FileInfo(folderInfo);
				Messages.sprintf("folderInfo were not zero: " + folderInfo.getFolderPath());
			} else {
				Messages.sprintf("folderInfo were were zero: " + thumbFilePath);
			}
			counter.set(counter.get() - 1);
			updateProgress(counter.get(), total.get());
			updateMessage(folderInfo.getFolderPath());
			TableUtils.refreshTableContent(tableView);

		} else {
			List<FileInfo> li = createFileInfo_list(folderInfo);
			folderInfo.setFileInfoList(li);
			if (!folderInfo.getFileInfoList().isEmpty()) {
				TableUtils.updateFolderInfos_FileInfo(folderInfo);
				counter.set(counter.get() - 1);
				updateProgress(counter.get(), total.get());
				updateMessage(folderInfo.getFolderPath());
				TableUtils.refreshTableContent(tableView);

			}
		}

	}
}
