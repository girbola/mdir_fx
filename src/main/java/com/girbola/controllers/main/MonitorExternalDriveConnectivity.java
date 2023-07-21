package com.girbola.controllers.main;

import com.girbola.Main;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.fileinfo.FileInfo;
import com.girbola.fileinfo.FileInfoUtils;
import com.girbola.messages.Messages;
import com.girbola.sql.FileInfo_SQL;
import common.utils.Conversion;
import common.utils.OSHI_Utils;
import javafx.application.Platform;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.control.TableView;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class MonitorExternalDriveConnectivity extends ScheduledService<Void> {

	private Model_main model_Main;

	public MonitorExternalDriveConnectivity(Model_main model_Main) {
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
			}

		};
	}

	private void checkWorkDirConnected() {
		if (Main.conf.getWorkDir().equals("null")) {
			disConnectedDrive("checkWorkDirConnected WorkDir is null");
			return;
		}
		if (Main.conf.getWorkDir().isEmpty()) {
			disConnectedDrive("checkWorkDirConnected WorkDir is empty");
			return;
		}

		if (!Files.exists(Paths.get(Main.conf.getWorkDir()))) {
			disConnectedDrive("checkWorkDirConnected destination drive is not connected");
		} else {
			String driveSerialNumber = OSHI_Utils.getDriveSerialNumber(Paths.get(Main.conf.getWorkDir()).getRoot().toString());
//			Messages.sprintf("testSerial is: " + driveSerialNumber);
			if (Main.conf.getWorkDirSerialNumber().equals(driveSerialNumber)) {
				driveConnected();
				Messages.sprintf("Workd dir is connected: " + Main.conf.getWorkDir());
			} else {
				disConnectedDrive("checkWorkDirConnected not connected. Drive serial number has been changed since last check");
			}
		}
	}

	private void driveConnected() {
		/*
		 * Shows connected workdir drive's state
		 * 
		 */
		Platform.runLater(() -> {
			try {
				Main.conf.setDrive_space("" + Conversion.convertToSmallerConversion(
						Files.getFileStore(Paths.get(Main.conf.getWorkDir()).toRealPath()).getTotalSpace()));
				Main.conf.setDrive_spaceLeft("" + Conversion.convertToSmallerConversion(
						Files.getFileStore(Paths.get(Main.conf.getWorkDir()).toRealPath()).getUsableSpace()));
				Main.conf.setDrive_connected(true);
				Main.conf.setDrive_name(Main.conf.getWorkDir());
			} catch (Exception e) {
				Platform.runLater(() -> {
					Messages.sprintf("driveConnected connected ERROR");
					Main.conf.setDrive_connected(false);
				});
			}
		});

	}

	private void disConnectedDrive(String debugMessage) {
		Platform.runLater(() -> {
			Messages.sprintf(debugMessage);
			Main.conf.setDrive_connected(false);
			Main.conf.setDrive_space("");
			Main.conf.setDrive_spaceLeft("");
			Main.conf.setDrive_name("");
		});
	}

	private void checkTableConnectivity(TableView<FolderInfo> table) {
		for (FolderInfo folderInfo : table.getItems()) {
			if (Files.exists(Paths.get(folderInfo.getFolderPath()))) {
				if (!folderInfo.isConnected()) {
					folderInfo.setConnected(true);
					Messages.sprintf("===Updating folder exists: " + folderInfo.isConnected());
					boolean loaded = FileInfo_SQL.loadFileInfoDatabase(folderInfo);
					if (!loaded) {
						List<FileInfo> list = FileInfoUtils.createFileInfo_list(folderInfo);
						folderInfo.getFileInfoList().addAll(list);
						TableUtils.updateFolderInfo(folderInfo);
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
					TableUtils.updateFolderInfo(folderInfo);
					TableUtils.refreshAllTableContent(model_Main.tables());
				}
			}
		}
	}

}
