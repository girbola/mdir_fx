package com.girbola.controllers.main;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.girbola.Main;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.fileinfo.FileInfo;
import com.girbola.fileinfo.FileInfo_Utils;
import com.girbola.messages.Messages;
import com.girbola.sql.SQL_Utils;

import common.utils.Conversion;
import javafx.application.Platform;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.control.TableView;

public class MonitorConnectivity extends ScheduledService<Void> {

	private Model_main model_Main;

	public MonitorConnectivity(Model_main model_Main) {
		this.model_Main = model_Main;
	}

	@Override
	protected Task<Void> createTask() {
		return new Task<Void>() {

			@Override
			protected Void call() throws Exception {

				checkTableConnectivity(model_Main.tables().getSortIt_table());
				checkTableConnectivity(model_Main.tables().getSorted_table());
				checkTableConnectivity(model_Main.tables().getAsItIs_table());
				checkWorkDirConnected();
				return null;
			}

			@Override
			protected void cancelled() {
				super.cancelled();
				Messages.sprintf("Cancelled check");
			}

			@Override
			protected void failed() {
				super.failed();
				Messages.sprintf("Failed check");
			}

			@Override
			protected void succeeded() {
				super.succeeded();
//				Messages.sprintf("Succedeed check");
			}

		};
	}

	private void checkWorkDirConnected() {
		if (Main.conf.getWorkDir().equals("null")) {
			Platform.runLater(() -> {
				Messages.sprintf("checkWorkDirConnected null");
				Main.conf.setDrive_connected(false);
				Main.conf.setDrive_space("");
				Main.conf.setDrive_spaceLeft("");
				Main.conf.setDrive_name("");
			});
			return;
		}
		if (!Files.exists(Paths.get(Main.conf.getWorkDir()))) {
			Platform.runLater(() -> {
				Messages.sprintf("checkWorkDirConnected not connected");
				Main.conf.setDrive_connected(false);
				Main.conf.setDrive_space("");
				Main.conf.setDrive_spaceLeft("");
				Main.conf.setDrive_name("");
			});
		} else {
			Platform.runLater(() -> {
				try {
//					Messages.sprintf("checkWorkDirConnected connected");
					Main.conf.setDrive_space("" + Conversion.convertToSmallerConversion(
							Files.getFileStore(Paths.get(Main.conf.getWorkDir()).toRealPath()).getTotalSpace()));
					Main.conf.setDrive_spaceLeft("" + Conversion.convertToSmallerConversion(
							Files.getFileStore(Paths.get(Main.conf.getWorkDir()).toRealPath()).getUsableSpace()));
					Main.conf.setDrive_connected(true);
					Main.conf.setDrive_name(Main.conf.getWorkDir());
				} catch (Exception e) {
					Platform.runLater(() -> {
						Messages.sprintf("checkWorkDirConnected connected ERROR");
						Main.conf.setDrive_connected(false);
					});
				}
			});
		}

	}

	private void checkTableConnectivity(TableView<FolderInfo> table) {
		for (FolderInfo folderInfo : table.getItems()) {
			if (Files.exists(Paths.get(folderInfo.getFolderPath()))) {
				if (!folderInfo.isConnected()) {
					folderInfo.setConnected(true);
					Messages.sprintf("===Updating folder exists: " + folderInfo.isConnected());
					boolean loaded = SQL_Utils.loadFileInfoDatabase(folderInfo);
					if (!loaded) {
						List<FileInfo> list = FileInfo_Utils.createFileInfo_list(folderInfo);
						folderInfo.getFileInfoList().addAll(list);
						TableUtils.updateFolderInfos_FileInfo(folderInfo);
						Main.setChanged(true);
						folderInfo.setChanged(true);
						folderInfo.setState("*");
					}
					TableUtils.refreshAllTableContent(model_Main.tables());
				}
			} else {
				Messages.sprintf("folder doesn't exists: " + folderInfo.isConnected());
				if (folderInfo.isConnected()) {
					folderInfo.setConnected(false);
					Messages.sprintf("===Updating folder doesn't exists: " + folderInfo.isConnected());
					folderInfo.getFileInfoList().clear();
					TableUtils.updateFolderInfos_FileInfo(folderInfo);
					TableUtils.refreshAllTableContent(model_Main.tables());
				}
			}
		}
	}

}
