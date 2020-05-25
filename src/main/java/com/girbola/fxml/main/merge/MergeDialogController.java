package com.girbola.fxml.main.merge;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.girbola.Main;
import com.girbola.Scene_NameType;
import com.girbola.controllers.main.Model_main;
import com.girbola.controllers.main.Tables;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.fileinfo.FileInfo;
import com.girbola.fxml.operate.OperateFiles;
import com.girbola.messages.Messages;

import common.utils.FileUtils;
import common.utils.date.DateUtils;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class MergeDialogController {

	private final String ERROR = MergeDialogController.class.getSimpleName();

	private Model_main model_main;
	private Tables tables;
	private TableView<FolderInfo> table;
	private String tableType;

	@FXML
	private Label event_lbl;
	@FXML
	private ComboBox<String> event_cmb;
	@FXML
	private Label location_lbl;
	@FXML
	private ComboBox<String> location_cmb;
	@FXML
	private ComboBox<String> user_cmb;

	@FXML
	private Button apply_btn;
	@FXML
	private Button apply_and_copy_btn;
	@FXML
	private Button cancel_btn;

	private void close() {
		Stage stage = (Stage) cancel_btn.getScene().getWindow();
		stage.close();
	}

	@FXML
	private void apply_btn_action(ActionEvent event) {
		if (Main.conf.getWorkDir() == null) {
			Messages.warningText("copySelectedTableRows Workdir were null");
			return;
		}
		if (Main.conf.getWorkDir().isEmpty()) {
			Messages.warningText("copySelectedTableRows Workdir were empty");
			return;
		}
		String eventName = "";
		String locationName = "";
		String userName = "";

		if (!event_cmb.getEditor().getText().isEmpty()) {
			eventName = event_cmb.getEditor().getText();
		}
		if (!location_cmb.getEditor().getText().isEmpty()) {
			locationName = location_cmb.getEditor().getText();
		}
		if (!user_cmb.getEditor().getText().isEmpty()) {
			userName = user_cmb.getEditor().getText();
		}
		Messages.sprintf(
				"locationName were= '" + locationName + " eventName were= " + eventName + " userName: " + userName);

		for (FolderInfo folderInfo : table.getSelectionModel().getSelectedItems()) {
			if (folderInfo.getBadFiles() >= 1) {
				Messages.warningText(Main.bundle.getString("badDatesFound"));
				return;
			}

			for (FileInfo fileInfo : folderInfo.getFileInfoList()) {

				fileInfo.setEvent(eventName);
				fileInfo.setLocation(locationName);
				fileInfo.setUser(userName);
				String event_str = "";
				String location_str = "";

				if (fileInfo.getEvent().isEmpty() && !fileInfo.getLocation().isEmpty()) {
					location_str = " - " + fileInfo.getLocation();
				} else if (!fileInfo.getEvent().isEmpty() && fileInfo.getLocation().isEmpty()) {
					event_str = " - " + fileInfo.getEvent();
				} else {
					location_str = " - " + fileInfo.getLocation();
					event_str = " - " + fileInfo.getEvent();
				}

				if (!user_cmb.getEditor().getText().isEmpty()) {
					userName = user_cmb.getEditor().getText();
				}
				LocalDate ld = DateUtils.longToLocalDateTime(fileInfo.getDate()).toLocalDate();
				Messages.sprintf("locationName were= '" + locationName + "'");
				Messages.sprintf("eventName were= '" + eventName + "'");
				// I:\\2017\\2017-06-23 Merikarvia - Kalassa äijien kanssa
				// I:\\2017\\2017-06-24 Merikarvia - Kalassa äijien kanssa
				String fileName = DateUtils.longToLocalDateTime(fileInfo.getDate())
						.format(Main.simpleDates.getDtf_ymd_hms_minusDots_default());
				Path destPath = Paths.get(
						File.separator + ld.getYear() + File.separator + ld + location_str + event_str + File.separator
								+ fileName + "." + FileUtils.getFileExtension(Paths.get(fileInfo.getOrgPath())));
				fileInfo.setWorkDir(Main.conf.getWorkDir());
				fileInfo.setWorkDirDriveSerialNumber(Main.conf.getWorkDirSerialNumber());
				fileInfo.setDestination_Path(destPath.toString());
				fileInfo.setCopied(false);
				fileInfo.setUser(userName);
				Main.setChanged(true);
				location_str = "";
				event_str = "";
				Messages.sprintf("Destination path would be: " + fileInfo.getDestination_Path());
			}

		}
//		FolderInfo_Utils.moveToAnotherTable(tables, table, tableType);
		TableUtils.refreshAllTableContent(tables);
		close();
	}

	@FXML
	private void apply_and_copy_btn_action(ActionEvent event) {
		if (Main.conf.getWorkDir() == null) {
			Messages.warningText("copySelectedTableRows Workdir were null");
			return;
		}
		if (Main.conf.getWorkDir().isEmpty()) {
			Messages.warningText("copySelectedTableRows Workdir were empty");
			return;
		}
		String eventName = "";
		String locationName = "";
		String userName = "";

		if (!event_cmb.getEditor().getText().isEmpty()) {
			eventName = event_cmb.getEditor().getText();
		}
		if (!location_cmb.getEditor().getText().isEmpty()) {
			locationName = location_cmb.getEditor().getText();
		}
		if (!user_cmb.getEditor().getText().isEmpty()) {
			userName = user_cmb.getEditor().getText();
		}

		for (FolderInfo folderInfo : table.getSelectionModel().getSelectedItems()) {
			if (folderInfo.getBadFiles() >= 1) {
				Messages.warningText(Main.bundle.getString("badDatesFound"));
				return;
			}

			for (FileInfo fileInfo : folderInfo.getFileInfoList()) {
				fileInfo.setEvent(eventName);
				fileInfo.setLocation(locationName);
				fileInfo.setUser(userName);
				String event_str = "";
				String location_str = "";

				if (fileInfo.getEvent().isEmpty() && !fileInfo.getLocation().isEmpty()) {
					location_str = " - " + fileInfo.getLocation();
				} else if (!fileInfo.getEvent().isEmpty() && fileInfo.getLocation().isEmpty()) {
					event_str = " - " + fileInfo.getEvent();
				} else {
					location_str = " - " + fileInfo.getLocation();
					event_str = " - " + fileInfo.getEvent();
				}
				

				if (fileInfo.getEvent().isEmpty() && !fileInfo.getLocation().isEmpty()) {
					location_str = " - " + fileInfo.getLocation();
				} else if (!fileInfo.getEvent().isEmpty() && fileInfo.getLocation().isEmpty()) {
					event_str = " - " + fileInfo.getEvent();
				} else {
					location_str = " - " + fileInfo.getLocation();
					event_str = " - " + fileInfo.getEvent();
				}

				LocalDate ld = DateUtils.longToLocalDateTime(fileInfo.getDate()).toLocalDate();
				Messages.sprintf("location_str were= '" + location_str + "'");
				Messages.sprintf("event_str were= '" + event_str + "'");
				// I:\\2017\\2017-06-23 Merikarvia - Kalassa äijien kanssa
				// I:\\2017\\2017-06-24 Merikarvia - Kalassa äijien kanssa
				String fileName = DateUtils.longToLocalDateTime(fileInfo.getDate())
						.format(Main.simpleDates.getDtf_ymd_hms_minusDots_default());
				String destPath = (File.separator + ld.getYear() + File.separator + ld + location_str + event_str
						+ File.separator + fileName + "."
						+ FileUtils.getFileExtension(Paths.get(fileInfo.getOrgPath())));

				fileInfo.setWorkDir(Main.conf.getWorkDir());
				fileInfo.setWorkDirDriveSerialNumber(Main.conf.getWorkDirSerialNumber());
				fileInfo.setDestination_Path(destPath);
				fileInfo.setCopied(false);
				fileInfo.setUser(userName);
				Main.setChanged(true);
				Messages.sprintf("apply and copy Destination path would be: " + fileInfo.getDestination_Path()
						+ " fileInfo.getDestination_Path() " + fileInfo.getDestination_Path());
				location_str = "";
				event_str = "";

			}

		}
//		FolderInfo_Utils.moveToAnotherTable(tables, table, tableType);

		List<FileInfo> list = new ArrayList<>();
		ExecutorService exec = Executors.newSingleThreadExecutor();
		for (FolderInfo folderInfo : table.getSelectionModel().getSelectedItems()) {
			if (folderInfo.getBadFiles() >= 1) {
				Messages.warningText(Main.bundle.getString("badDatesFound"));
				return;
			}
			list.addAll(folderInfo.getFileInfoList());
		}

		Task<Boolean> operate = new OperateFiles(list, true, model_main, Scene_NameType.MAIN.getType());

		operate.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				for (FolderInfo folderInfo : table.getSelectionModel().getSelectedItems()) {
					TableUtils.updateFolderInfos_FileInfo(folderInfo);
				}
				TableUtils.refreshAllTableContent(tables);
				close();
			}
		});
		operate.setOnFailed(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				Messages.warningText("Copy process failed");
				close();
			}
		});

		operate.setOnCancelled(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				Messages.sprintf("Copy process were cancelled");
				close();
			}
		});

		Thread thread = new Thread(operate, "Operate Thread");
		exec.submit(thread);

	}

	@FXML
	private void cancel_btn_action(ActionEvent event) {
		close();
	}

	public void init(Model_main model_main, Tables tables, TableView<FolderInfo> table, String tableType) {
		this.model_main = model_main;
		this.tables = tables;
		this.table = table;
		this.tableType = tableType;
	}
}
