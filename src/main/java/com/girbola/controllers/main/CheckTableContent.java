package com.girbola.controllers.main;

import java.util.ArrayList;
import java.util.List;

import com.girbola.Main;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.fileinfo.FileInfo;
import com.girbola.fileinfo.FileInfo_Utils;
import com.girbola.messages.Messages;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

public class CheckTableContent {
	private TableView<FolderInfo> table;
	private Model_main model_Main;

	private ObservableList<FileInfo> conflictWithWorkdir_list = FXCollections.observableArrayList();
	private ObservableList<FileInfo> cantCopy_list = FXCollections.observableArrayList();
	private ObservableList<FileInfo> okFiles_list = FXCollections.observableArrayList();
	private List<FileInfo> fileInfoList = new ArrayList<>();

	public List<FileInfo> getFileInfoList() {
		return fileInfoList;
	}

	public CheckTableContent(TableView<FolderInfo> table, Model_main model_Main) {
		super();
		this.table = table;
		this.model_Main = model_Main;
	}

	private void checkTables() {
		if (!Main.conf.getDrive_connected()) {
			Messages.warningText("Destination drive is not connected. Connect drive and try again");
		}
		for (FolderInfo folderInfo : table.getItems()) {
			for (FileInfo fileInfo : folderInfo.getFileInfoList()) {
				if (!fileInfo.getDestination_Path().isEmpty() && !fileInfo.isCopied()) {
					/*
					 * 0 = is good, 1 = is conflict with workdir. Workdir (destination path) is not
					 * connected, 2 if copying is not possible
					 */
					int status = FileInfo_Utils.checkWorkDir(fileInfo);
					if (status == 0) {
						okFiles_list.add(fileInfo);
						Messages.sprintf(
								"okFiles: " + fileInfo.getDestination_Path() + " isCopied? " + fileInfo.isCopied());
					} else if (status == 1) {
						conflictWithWorkdir_list.add(fileInfo);
						Messages.sprintf(
								"conflicts: " + fileInfo.getDestination_Path() + " isCopied? " + fileInfo.isCopied());
					} else if (status == 2) {
						cantCopy_list.add(fileInfo);
						Messages.sprintf(
								"can't copy: " + fileInfo.getDestination_Path() + " isCopied? " + fileInfo.isCopied());
					} else if (status == 3) {
						// Workdir is not connected
//						Messages.errorSmth(className, message, exception, line, exit);
						cantCopy_list.add(fileInfo);
						Messages.sprintf(
								"can't copy: " + fileInfo.getDestination_Path() + " isCopied? " + fileInfo.isCopied());
					} else {
						Messages.sprintf("something else?!?!?!: " + fileInfo.getDestination_Path() + " isCopied? "
								+ fileInfo.isCopied());
					}
				}
			}
		}

	}

	private void showConflictFileInfos() {
		Messages.sprintf("showConflictFileInfos: ");
		for (FileInfo fileInfo : conflictWithWorkdir_list) {
			conflictWithWorkdir_list.add(fileInfo);
		}
	}

	private void showCantCopyFileInfos() {
		Messages.sprintf("showCantCopyFileInfos: ");
		for (FileInfo fileInfo : cantCopy_list) {
			cantCopy_list.add(fileInfo);
		}
	}
}
