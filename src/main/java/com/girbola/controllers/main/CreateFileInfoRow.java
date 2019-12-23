/*
 @(#)Copyright:  Copyright (c) 2012-2019 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.main;

import static com.girbola.fileinfo.FileInfo_Utils.createFileInfo_list;
import static com.girbola.messages.Messages.sprintf;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.girbola.Main;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;

import javafx.concurrent.Task;
import javafx.scene.control.TableView;

/**
 *
 * @author Marko Lokka
 */
public class CreateFileInfoRow extends Task<Void> {

	private AtomicInteger counter = new AtomicInteger(0);
	private final String ERROR = CreateFileInfoRow.class.getSimpleName();
	private Model_main model_Main;
	private TableView<FolderInfo> table;

	CreateFileInfoRow(Model_main aModel, TableView<FolderInfo> aTable) {
		this.model_Main = aModel;
		this.table = aTable;
	}

	@Override
	protected Void call() throws Exception {
		updateProgress(counter.get(), table.getSelectionModel().getSelectedItems().size());
		sprintf("Update Progress: " + counter.get() + " max " + table.getSelectionModel().getSelectedItems().size());

		for (FolderInfo folderInfo : table.getSelectionModel().getSelectedItems()) {
			if (Main.getProcessCancelled() || isCancelled()) {
				// cancel();
				break;

			}
			if (folderInfo.isConnected()) {
				sprintf("Reloading: " + folderInfo.getFolderPath());
				long start = System.currentTimeMillis();
				List<FileInfo> list = createFileInfo_list(folderInfo);
				Messages.sprintf("creating fileinfo list took: " + (System.currentTimeMillis() - start) + " list size: " + list.size());
				if (list != null) {
					folderInfo.setFileInfoList(list);
					TableUtils.updateFolderInfos_FileInfo(folderInfo);
					TableUtils.refreshTableContent(table);

					updateProgress(counter.incrementAndGet(), table.getSelectionModel().getSelectedItems().size());
					updateMessage(folderInfo.getFolderPath());
					sprintf("Update Progress: " + counter.get() + " max " + table.getSelectionModel().getSelectedItems().size());
				} else {
					Messages.errorSmth(ERROR, "", null, Misc.getLineNumber(), true);
				}
			}
		}
		return null;
	}

}
