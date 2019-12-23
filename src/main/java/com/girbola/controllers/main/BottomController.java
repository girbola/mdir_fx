/*
 @(#)Copyright:  Copyright (c) 2012-2019 All right reserved.
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.main;

import static com.girbola.Main.bundle;
import static com.girbola.Main.conf;
import static com.girbola.messages.Messages.sprintf;
import static com.girbola.messages.Messages.warningText;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.girbola.Main;
import com.girbola.Scene_NameType;
import com.girbola.controllers.folderscanner.FolderScannerController;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.controllers.workdir.WorkDirController;
import com.girbola.fileinfo.FileInfo;
import com.girbola.fxml.operate.OperateFiles;
import com.girbola.media.collector.Collector;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;

import common.utils.date.DateUtils;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class BottomController {

	private final String ERROR = BottomController.class.getSimpleName();
	private Model_main model_main;

	@FXML
	private Button addFolders_btn;
	@FXML
	private Button copy_ok_date_btn;
	@FXML
	private Button copySelected_btn;
	@FXML
	private Button help_btn;
	@FXML
	private Button options_btn;
	@FXML
	private Button start_copyBatch_btn;
	@FXML
	private Button workDir_btn;
	@FXML
	private Button collect;
	@FXML
	private Label drive_name;
	@FXML
	private Label drive_space;
	@FXML
	private Label drive_spaceLeft;
	@FXML
	private Label drive_connected;

	@FXML
	private void collect_action(ActionEvent event) {
		Collector collect = new Collector();
		collect.collectAll(model_main.tables());
		collect.listMap();
	}

	@FXML
	private void workDir_btn_action(ActionEvent event) {
		Messages.sprintf("Not ready yet!");
		Parent parent = null;
		FXMLLoader workDirLoader = new FXMLLoader(Main.class.getResource("fxml/main/Main.fxml"), bundle);
		sprintf("main_loader location: " + workDirLoader.getLocation());
		// TODO Tarkista tämä
		WorkDirController workDirController = (WorkDirController) workDirLoader.getController();
		// try {
		// parent = workDirLoader.load();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		Scene workDir_scene = new Scene(parent);
		Stage workDir_stage = new Stage();
		workDir_stage.setScene(workDir_scene);
		workDir_stage.show();

	}

	@FXML
	private void addFolders_btn_action(ActionEvent action) {

		Messages.sprintf("locale is: " + Main.bundle.getLocale().toString());
		FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/folderscanner/FolderScanner.fxml"),
				Main.bundle);

		Parent parent = null;
		FolderScannerController folderScannerController = null;
		try {
			parent = loader.load();
			folderScannerController = (FolderScannerController) loader.getController();
		} catch (Exception ex) {
			ex.printStackTrace();
			Messages.errorSmth(ERROR,
					"Country= " + Main.bundle.getLocale().getCountry() + " location?\n: " + Main.bundle.getLocale(), ex,
					Misc.getLineNumber(), true);
		}
		Stage fc_stage = new Stage();
		fc_stage.setWidth(conf.getScreenBounds().getWidth());
		fc_stage.setHeight(conf.getScreenBounds().getHeight() / 1.3);
		fc_stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				fc_stage.close();
			}
		});

		Scene fc_scene = new Scene(parent, 800, 400);
		fc_scene.getStylesheets()
				.add(Main.class.getResource(conf.getThemePath() + "folderChooser.css").toExternalForm());
		folderScannerController.setStage(fc_stage);
		folderScannerController.setScene(fc_scene);
		folderScannerController.init(model_main);
		fc_stage.setScene(fc_scene);

		fc_stage.show();

	}

	@FXML
	private void copy_ok_date_btn_action(ActionEvent event) {
		Main.setProcessCancelled(false);
		try {
			if (!Files.exists(Paths.get(conf.getWorkDir()).toRealPath())) {
				warningText(bundle.getString("cannotFindWorkDir"));
				return;
			}
		} catch (IOException ex) {
			warningText(bundle.getString("cannotFindWorkDir"));
			return;
		}
	}

	/*
	 * 
	 * Check if file exists: - Workdir - Check if file exists already in different
	 * time - Copy to destination
	 */

	/**
	 * Copy selected files
	 * 
	 * @param event
	 */
	@FXML
	private void copySelected_btn_action(ActionEvent event) {
		Main.setProcessCancelled(false);
		try {
			if (!Files.exists(Paths.get(conf.getWorkDir()).toRealPath())) {
				warningText(bundle.getString("cannotFindWorkDir"));
				return;
			}
		} catch (IOException ex) {
			warningText(bundle.getString("cannotFindWorkDir"));
			return;
		}

		copyTables(model_main.tables(), model_main.tables().getSorted_table(), TableType.SORTED.getType());
		copyTables(model_main.tables(), model_main.tables().getSortIt_table(), TableType.SORTIT.getType());
		// copyTables(workDir_Handler, model_main.tables(),
		// model_main.tables().getAsItIs_table(), TableType.ASITIS.getType());

	}

	private void copyTables(Tables tables, TableView<FolderInfo> table, String tableType) {
		for (FolderInfo folderInfo : table.getSelectionModel().getSelectedItems()) {
			int badFiles = 0;

			for (FileInfo fileInfo : folderInfo.getFileInfoList()) {
				Path src = Paths.get(fileInfo.getOrgPath());
				FileInfo possibleDuplicate = model_main.getWorkDir_Handler().exists(fileInfo);
				if (possibleDuplicate == null) {
					fileInfo.setCopied(false);
				} else {
					fileInfo.setCopied(true);
					sprintf("File existed already: " + fileInfo.getOrgPath());
				}
			}
		}
	}

	private boolean checkDuplicates(TableView<FolderInfo> table) {
		if (table.getId().equals(TableType.SORTED.getType())) {
			for (FolderInfo folderInfo : table.getItems()) {
				for (FileInfo f_search : folderInfo.getFileInfoList()) {
					FileInfo fileInfo = model_main.getWorkDir_Handler().exists(f_search);
					if (fileInfo != null) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@FXML
	private void help_btn_action(ActionEvent event) {
	}

	@FXML
	private void options_btn_action(ActionEvent event) {

	}

	@FXML
	private void start_copyBatch_btn_action(ActionEvent event) {
		Main.setProcessCancelled(false);
		List<String> notFound = new ArrayList<>();
		
		if (!Main.conf.getDrive_connected() || !Files.exists(Paths.get(Main.conf.getWorkDir()))) {
			Messages.warningText(Main.bundle.getString("workDirHasNotConnected"));
			return;
		}
		Task<List<FileInfo>> task = new Task<List<FileInfo>>() {
			@Override
			protected List<FileInfo> call() throws Exception {
				List<FileInfo> list = new ArrayList<>();
				for (FolderInfo folderInfo : model_main.tables().getSorted_table().getItems()) {
					for (FileInfo fileInfo : folderInfo.getFileInfoList()) {
						if (!fileInfo.getDestinationPath().isEmpty() && !fileInfo.isCopied()) {
							if (fileInfo.getDestinationPath().toString().contains(Main.conf.getWorkDir())) {

								list.add(fileInfo);
								Messages.sprintf("Sorted Batch file found: " + fileInfo);
							}
							notFound.add(fileInfo.getDestinationPath());
							Messages.sprintf("Sorted file workdir were not found: " + fileInfo.getDestinationPath());
						}
					}
				}

				for (FolderInfo folderInfo : model_main.tables().getSortIt_table().getItems()) {
					for (FileInfo fileInfo : folderInfo.getFileInfoList()) {
						if (!fileInfo.getDestinationPath().isEmpty() && !fileInfo.isCopied()) {
							if (fileInfo.getDestinationPath().toString().contains(Main.conf.getWorkDir())) {
								Messages.sprintf("SortIt Batch file found: " + fileInfo);
								list.add(fileInfo);
							} else {
								notFound.add(fileInfo.getDestinationPath());
								Messages.sprintf(
										"SortIt file workdir were not found: " + fileInfo.getDestinationPath());
							}
						}
					}
				}
				if(notFound.isEmpty()) {
					return null;
				}
				return list;
			}

		};
		task.setOnSucceeded((event2) -> {
			Messages.sprintf("Making list were made successfully");
			try {
				if(task.get() == null) {
					Messages.warningTextList("There were files which had bad destination paths", notFound);
				}
				Task<Boolean> operateFiles = new OperateFiles(task.get(), false, model_main,
						Scene_NameType.MAIN.getType());
				operateFiles.setOnSucceeded((workerStateEvent) -> {
					Messages.sprintf("operateFiles Succeeded");
				});
				operateFiles.setOnCancelled((workerStateEvent) -> {
					Messages.sprintf("operateFiles CANCELLED");
				});
				operateFiles.setOnFailed((workerStateEvent) -> {
					Messages.sprintf("operateFiles FAILED");
					Main.setProcessCancelled(true);
				});
				Thread operateFiles_th = new Thread(operateFiles, "operateFiles_th");
				operateFiles_th.setDaemon(true);
				operateFiles_th.start();
			} catch (Exception e) {
				e.printStackTrace();
			}

		});
		task.setOnCancelled((event2) -> {
			Messages.sprintf("Making list were cancelled");
		});
		task.setOnFailed((event2) -> {
			Messages.sprintf("Making list were made cancelled");
		});
		Thread th = new Thread(task, "Start Threads");
		th.start();

	}

	private void handleSortIt(ObservableList<FolderInfo> sortit, FileInfo fi_src) {

		// Iterator<FolderInfo> it = sortit.iterator();
		for (FolderInfo foi : sortit) {
			Messages.sprintf("Finding files: " + foi.getFolderPath());
			LocalDate min = DateUtils.parseLocalDateTimeFromString(foi.getMinDate()).toLocalDate().minusDays(1);
			LocalDate max = DateUtils.parseLocalDateTimeFromString(foi.getMinDate()).toLocalDate().plusDays(1);
			LocalDate run = DateUtils.longToLocalDateTime(fi_src.getDate()).toLocalDate();
			if (run.isAfter(min) && run.isBefore(max)) {
				Messages.sprintf("Date were between this date scane");
				// foi.getFileInfo().parallelStream().filter(fi_src);
				for (FileInfo fileInfo : foi.getFileInfoList()) {
					// Messages.sprintf("----ffff: " + fileInfo);
					if (fileInfo.getDate() == fi_src.getDate() && fileInfo.getSize() == fi_src.getSize()) {
						Messages.sprintf("Duplicated value found at sortit: " + fileInfo.getOrgPath());
						foi.getFileInfoList().remove(fileInfo);
						Platform.runLater(new Runnable() {

							@Override
							public void run() {
								if (foi.getChanged() == false) {
									foi.setChanged(true);
								}
							}
						});
					}

				}
			}
			min = null;
			max = null;
			run = null;

		}
	}

	// private boolean workDirExists(FileInfo fi, Path dest) {
	// Path path = Paths.get(conf.getWorkDir() + File.separator +
	// simpleDates.getSdf_Year().format(fi.getDate()) + File.separator +
	// simpleDates.getSdf_Month().format(fi.getDate()) + File.separator);
	// if (Files.exists(path)) {
	//
	// }
	// String year = "";
	// String month = "";
	//
	// return false;
	// }
	// private static String getCurrentTimezoneOffset(long date) {
	//
	// TimeZone tz = TimeZone.getDefault();
	// // Calendar cal = GregorianCalendar.getInstance(tz);
	// int offsetInMillis = tz.getOffset(date);
	//
	// String offset = String.format("%02d:%02d", Math.abs(offsetInMillis /
	// 3600000), Math.abs((offsetInMillis / 60000) % 60));
	// offset = (offsetInMillis >= 0 ? "+" : "-") + offset;
	//
	// return offset;
	// }
	public static boolean hasCheckWorkDirConflict(ObservableList<FolderInfo> obs) {
		for (FolderInfo fi : obs) {
			if (Paths.get(fi.getFolderPath()).getParent().toString().equals(conf.getWorkDir())) {
				return true;
			}
		}
		return false;
	}

	@FXML
	public void initialize() {
		sprintf("bottom controller...");
	}

	public void initBottomWorkdirMonitors() {
		assertNotNull(drive_name);
		assertNotNull(drive_space);
		assertNotNull(drive_spaceLeft);
		assertNotNull(drive_connected);

		Messages.sprintf("initBottomWorkdirMonitors started");
		drive_name.textProperty().bindBidirectional(Main.conf.drive_name_property());
		drive_space.textProperty().bindBidirectional(Main.conf.drive_space_property());
		drive_spaceLeft.textProperty().bindBidirectional(Main.conf.drive_spaceLeft_property());
		Platform.runLater(() -> {
			drive_name.setText("filli");
		});
		Main.conf.drive_connected_property().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue == true) {
					drive_connected.setStyle("-fx-background-color: green;");
				} else {
					drive_connected.setStyle("-fx-background-color: red;");
				}
			}

		});
	}

	public void init(Model_main aModel_main) {
		this.model_main = aModel_main;
		sprintf("bottom controller...");
//		Main.conf.drive_property
//		
//		drive_connected.textProperty().bindBidirectional(Main.conf.drive_connected_property());
	}

}
