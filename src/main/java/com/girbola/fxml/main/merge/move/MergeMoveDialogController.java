package com.girbola.fxml.main.merge.move;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
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
import com.girbola.misc.Misc;
import com.girbola.sql.FileInfo_SQL;
import com.girbola.sql.FolderInfo_SQL;
import com.girbola.sql.SqliteConnection;

import common.utils.FileUtils;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class MergeMoveDialogController {

	private final String ERROR = MergeMoveDialogController.class.getSimpleName();

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
	private Button apply_and_move_btn;
	@FXML
	private Button apply_btn;
	@FXML
	private Button apply_and_copy_btn;
	@FXML
	private Button cancel_btn;
	@FXML
	private CheckBox addEverythingInsameDir_chb;

	private void close() {
		Stage stage = (Stage) cancel_btn.getScene().getWindow();
		stage.close();
	}

	@FXML
	private void apply_and_move_btn_action(ActionEvent event) {
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
			eventName = event_cmb.getEditor().getText().trim();
		}
		if (!location_cmb.getEditor().getText().isEmpty()) {
			locationName = location_cmb.getEditor().getText().trim();
		}
		if (!user_cmb.getEditor().getText().isEmpty()) {
			userName = user_cmb.getEditor().getText().trim();
		}
		Messages.sprintf(
				"locationName were= '" + locationName + " eventName were= " + eventName + " userName: " + userName);

		FolderInfo newFolderInfo = new FolderInfo();
		String newDestinationPathForMoving = Main.conf.getUserHome() + File.separator + Main.conf.getPictures();

		String pathDefiner = definePathByEventLocation(eventName, locationName);

		Iterator<FolderInfo> it = table.getSelectionModel().getSelectedItems().iterator();

		while (it.hasNext()) {
			FolderInfo folderInfo = it.next();
			if (folderInfo.getBadFiles() >= 1) {
				Messages.warningText(Main.bundle.getString("badDatesFound"));
				return;
			}
			if (Main.getProcessCancelled()) {
				Messages.errorSmth(ERROR, Main.bundle.getString("creatingDestinationDirFailed"), null,
						Misc.getLineNumber(), true);
				break;
			}
			Connection connection = SqliteConnection.connector(folderInfo.getFolderPath(),
					Main.conf.getMdir_db_fileName());
			try {
				connection.setAutoCommit(false);
			} catch (Exception e) {
				// TODO: handle exception
			}
			Iterator<FileInfo> fileInfo_list_it = folderInfo.getFileInfoList().iterator();
			List<FileInfo> fileList = new ArrayList<>();
			while (fileInfo_list_it.hasNext()) {
				FileInfo fileInfo = fileInfo_list_it.next();
				if (Files.exists(Paths.get(fileInfo.getOrgPath()))) {

					fileInfo.setEvent(eventName);
					fileInfo.setLocation(locationName);
					fileInfo.setUser(userName);
					Path destinationPath = Paths.get(FileUtils
							.getFileNameDateWithEventAndLocation(fileInfo, newDestinationPathForMoving).toString());
					Path finalDest = Paths.get(newDestinationPathForMoving + destinationPath.toString());

					if (Files.exists(finalDest)) {
						try {

							Path finalDest_tmp = FileUtils.renameFile(Paths.get(fileInfo.getOrgPath()), finalDest);
							finalDest = finalDest_tmp;
							Messages.sprintfError("RENAMING! new dest path: " + finalDest);
						} catch (IOException e) {
							e.printStackTrace();
						}
					} else {
						Messages.sprintf(
								"+ fileInfo.SRC: " + fileInfo.toString() + "---->22222222new dest path: " + finalDest);
					}

					try {
						Files.createDirectories(finalDest.getParent());
						Files.move(Paths.get(fileInfo.getOrgPath()), finalDest);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.exit(0);
					}

					fileList.add(fileInfo);
//				fileInfo.setOrgPath(destinationPath.toString());
//				fileInfo.setDestination_Path("");
//				fileInfo.setWorkDir("");
//				fileInfo.setWorkDirDriveSerialNumber("");
//				folderInfo.getFileInfoList().add(fileInfo);
//				Messages.sprintf(" fileInfo: " + fileInfo.toString());
				}
				boolean deleteFileInfoListToDatabase = FileInfo_SQL.deleteFileInfoListToDatabase(connection, fileList);
				if (deleteFileInfoListToDatabase) {
					Messages.sprintf("Deleting fileInfo_list from database were success");
				} else {
					Messages.sprintfError("Bug sniffer when deleting files form database");
				}

				try {
					connection.commit();
					connection.close();

				} catch (SQLException e) {
					Messages.sprintfError("SQL Exception when deleting from table: " + e.getMessage());
					e.printStackTrace();
				}

			}

		}
//		for (FolderInfo folderInfo : table.getSelectionModel().getSelectedItems()) {
//			if (folderInfo.getBadFiles() >= 1) {
//				Messages.warningText(Main.bundle.getString("badDatesFound"));
//				return;
//			}
//			if (Main.getProcessCancelled()) {
//				Messages.errorSmth(ERROR, Main.bundle.getString("creatingDestinationDirFailed"), null,
//						Misc.getLineNumber(), true);
//				break;
//			}
//			for (FileInfo fileInfo : folderInfo.getFileInfoList()) {
//
//				if (Main.getProcessCancelled()) {
//					Messages.errorSmth(ERROR, Main.bundle.getString("creatingDestinationDirFailed"), null,
//							Misc.getLineNumber(), true);
//					break;
//				}
//				fileInfo.setEvent(eventName);
//				fileInfo.setLocation(locationName);
//				fileInfo.setUser(userName);
//
//				// I:\\2017\\2017-06-23 Merikarvia - Kalassa äijien kanssa
//				// I:\\2017\\2017-06-24 Merikarvia - Kalassa äijien kanssa
//				Path destinationPath = FileUtils.getFileNameDateWithEventAndLocation(fileInfo, Main.conf.getWorkDir());
//				if (!Files.exists(destinationPath)) {
//					Messages.sprintfError(Main.bundle.getString("creatingDestinationDirFailed") + " File destination: "
//							+ destinationPath);
//					Main.setProcessCancelled(true);
//					break;
//				}
//				fileInfo.setCopied(false);
//				folderInfo.setChanged(true);
//				Main.setChanged(true);
//				Messages.sprintf("Destination path would be: " + fileInfo.getDestination_Path());
//			}
//		}
		TableUtils.refreshAllTableContent(tables);
		close();

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
			eventName = event_cmb.getEditor().getText().trim();
		}
		if (!location_cmb.getEditor().getText().isEmpty()) {
			locationName = location_cmb.getEditor().getText().trim();
		}
		if (!user_cmb.getEditor().getText().isEmpty()) {
			userName = user_cmb.getEditor().getText().trim();
		}
		Messages.sprintf(
				"locationName were= '" + locationName + " eventName were= " + eventName + " userName: " + userName);

		for (FolderInfo folderInfo : table.getSelectionModel().getSelectedItems()) {
			if (folderInfo.getBadFiles() >= 1) {
				Messages.warningText(Main.bundle.getString("badDatesFound"));
				return;
			}
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
				Path destinationPath = FileUtils.getFileNameDateWithEventAndLocation(fileInfo, Main.conf.getWorkDir());
				if (!Files.exists(destinationPath)) {
					Messages.sprintfError(Main.bundle.getString("creatingDestinationDirFailed") + " File destination: "
							+ destinationPath);
					Main.setProcessCancelled(true);
					break;
				}
				fileInfo.setCopied(false);
				folderInfo.setChanged(true);
				Main.setChanged(true);
				Messages.sprintf("Destination path would be: " + fileInfo.getDestination_Path());
			}
		}
		TableUtils.refreshAllTableContent(tables);
		close();
	}

	private static String definePathByEventLocation(String locationName, String eventName) {

		String location_str = "", event_str = "";

		// Location = "KAINUU" Event = ""
		if (!locationName.isEmpty() && eventName.isEmpty()) {
			location_str = " - " + locationName;
			// Event = "KALASSA" Location = ""
		} else if (!eventName.isEmpty() && locationName.isEmpty()) {
			event_str = " - " + eventName;
			// Event = "" Location = ""
		} else if (!eventName.isEmpty() && !locationName.isEmpty()) {
			location_str = " - " + locationName;
			event_str = " - " + eventName;
		}

		return location_str + event_str + File.separator;
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
			if (Main.getProcessCancelled()) {
				Messages.sprintfError("Merging were cancelled");
				break;
			}

			for (FileInfo fileInfo : folderInfo.getFileInfoList()) {
				if (Main.getProcessCancelled()) {
					Messages.sprintfError("Merging were cancelled");
					break;
				}
				if (!fileInfo.getEvent().isEmpty()) {
					if (addEverythingInsameDir_chb.isSelected()) {
						if (folderInfo.getJustFolderName() != eventName) {
							folderInfo.setJustFolderName(eventName);
						}
					}
				}

				fileInfo.setEvent(eventName);
				fileInfo.setLocation(locationName);
				fileInfo.setUser(userName);

				Path destinationPath = FileUtils.getFileNameDateWithEventAndLocation(fileInfo, Main.conf.getWorkDir());
//				if (!Files.exists(destinationPath)) {
//					Messages.sprintfError(Main.bundle.getString("creatingDestinationDirFailed") + " File destination: "
//							+ destinationPath);
//					Main.setProcessCancelled(true);
//					break;
//				}
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
				TableUtils.saveChangesContentsToTables(model_main.tables());
				Main.setChanged(false);
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
