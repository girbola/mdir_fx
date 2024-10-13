package com.girbola.fxml.main.collect;

import com.girbola.Main;
import com.girbola.SceneNameType;
import com.girbola.controllers.importimages.AutoCompleteComboBoxListener;
import com.girbola.controllers.main.Model_main;
import com.girbola.controllers.main.Tables;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.controllers.main.tables.FolderInfo_Utils;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.events.GUI_Events;
import com.girbola.fileinfo.FileInfo;
import com.girbola.fxml.operate.OperateFiles;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import common.utils.FileUtils;
import common.utils.date.DateUtils;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Collect_DialogController {

	private final String ERROR = Collect_DialogController.class.getSimpleName();

	private Model_main model_main;
	private Model_CollectDialog model_CollectDialog;

	private Tables tables;
	private TableView<FolderInfo> table;
	private String tableType;

	/*
	 * Must include in project!
	 * 
	 * @FXML Collect_DateTimeAdjusterController collect_DateTimeAdjusterController;
	 */
   @FXML Collect_DateTimeAdjusterController collect_DateTimeAdjusterController;

   @FXML private Label event_lbl;
   @FXML private ComboBox<FileInfo_Event> event_cmb;
   @FXML private Label location_lbl;
   @FXML private ComboBox<FileInfo> location_cmb;
   @FXML private ComboBox<FileInfo> user_cmb;

   @FXML private Button apply_btn;
   @FXML private Button apply_and_copy_btn;
   @FXML private Button cancel_btn;
   @FXML private CheckBox addEverythingInsameDir_chb;

	private void close() {
		Stage stage = (Stage) cancel_btn.getScene().getWindow();
		stage.close();
	}

   @FXML private void apply_btn_action(ActionEvent event) {
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
			if (FolderInfo_Utils.hasBadFiles(folderInfo)) {
				continue;
			}
			// TODO extract to method
			if (Main.getProcessCancelled()) {
				Messages.errorSmth(ERROR, Main.bundle.getString("creatingDestinationDirFailed"), null,
						Misc.getLineNumber(), true);
				break;
			}
			for (FileInfo fileInfo : folderInfo.getFileInfoList()) {
				if (Main.getProcessCancelled()) {
					Messages.errorSmth(ERROR, Main.bundle.getString("creatingDestinationDirFailed"), null,
							Misc.getLineNumber(), true);
					break;
				}
				fileInfo.setEvent(eventName);
				fileInfo.setLocation(locationName);
				fileInfo.setUser(userName);

				// I:\\2017\\2017-06-23 Merikarvia - Kalassa äijien kanssa
				// I:\\2017\\2017-06-24 Merikarvia - Kalassa äijien kanssa

//				Path destinationPath = FileUtils.getFileNameDateWithEventAndLocation(fileInfo, Main.conf.getWorkDir());
//				if (!Files.exists(destinationPath)) {
//					Messages.sprintfError(Main.bundle.getString("creatingDestinationDirFailed") + " File destination: "
//							+ destinationPath);
//					Main.setProcessCancelled(true);
//					break;
//				}
				Main.setChanged(true);

				Messages.sprintf("Destination path would be: " + fileInfo.getDestination_Path());
			}

		}
//		FolderInfo_Utils.moveToAnotherTable(tables, table, tableType);
		TableUtils.refreshAllTableContent(tables);
		close();
	}

   @FXML private void apply_and_copy_btn_action(ActionEvent event) {
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
			if (FolderInfo_Utils.hasBadFiles(folderInfo)) {
				continue;
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
					if (addEverythingInsameDir_chb.isSelected()) {
						if (folderInfo.getJustFolderName() != eventName) {
							folderInfo.setJustFolderName(eventName);
						}
					}
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
			if (FolderInfo_Utils.hasBadFiles(folderInfo)) {
				continue;
			}
			list.addAll(folderInfo.getFileInfoList());
		}

		Task<Boolean> operate = new OperateFiles(list, true, model_main, SceneNameType.MAIN.getType());

		operate.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				for (FolderInfo folderInfo : table.getSelectionModel().getSelectedItems()) {
					FolderInfo_Utils.calculateFileInfoStatuses(folderInfo);
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

   @FXML private void cancel_btn_action(ActionEvent event) {
		close();
	}

	public void init(Model_main aModel_main, Model_CollectDialog aModel_CollectDialog, TableView<FolderInfo> aTable,
			String aTableType) {
		this.model_main = aModel_main;
		this.model_CollectDialog = aModel_CollectDialog;
		this.tables = aModel_main.tables();
		this.table = aTable;
		this.tableType = aTableType;
		collect_DateTimeAdjusterController.init(this.model_main, this.model_CollectDialog);
		event_cmb.setItems(model_CollectDialog.obs_Events);

		new AutoCompleteComboBoxListener<>(event_cmb);
		GUI_Events.textField_file_listener(event_cmb.getEditor());

//		event_cmb.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<FileInfo>() {
//
//			@Override
//			public void changed(ObservableValue<? extends FileInfo> observable, FileInfo oldValue, FileInfo newValue) {
//				// TODO Auto-generated method stub
//				
//			}
//		});

//		.addListener(((options, oldValue, newValue) -> {
//			FileInfo f = (FileInfo) newValue.getFileInfo();
//			Messages.sprintf(f.getOrgPath());
//		}));
		location_cmb.setItems(model_CollectDialog.obs_Location);
//		event_cmb.set

	}
}
