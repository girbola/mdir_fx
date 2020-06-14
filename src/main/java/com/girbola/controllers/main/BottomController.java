/*
 @(#)Copyright:  Copyright (c) 2012-2020 All right reserved.
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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.girbola.Main;
import com.girbola.controllers.folderscanner.FolderScannerController;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.controllers.workdir.WorkDirController;
import com.girbola.fileinfo.FileInfo;
import com.girbola.fxml.datestreetableview.DatesTreeTableViewController;
import com.girbola.media.collector.Collector;
import com.girbola.messages.Messages;
import com.girbola.messages.html.HTMLClass;
import com.girbola.misc.Misc;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
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
	private HBox drive_pane;

	@FXML
	private Button removeDuplicates_btn;
	private AtomicInteger duplicateCounter = new AtomicInteger(0);
	private AtomicInteger folderCounter = new AtomicInteger(0);
	private AtomicInteger fileCounter = new AtomicInteger(0);
	private AtomicInteger folderEnterCounter = new AtomicInteger(0);
	private AtomicInteger fileEnterCounter = new AtomicInteger(0);
	private AtomicBoolean folderNeedsToUpdate = new AtomicBoolean(false);

	@FXML
	private void removeDuplicates_btn_action(ActionEvent event) {
		removeTableDuplicates(model_main.tables().getSorted_table(), model_main.tables().getSorted_table());
		removeTableDuplicates(model_main.tables().getSorted_table(), model_main.tables().getSortIt_table());

	}

	private void removeTableDuplicates(TableView<FolderInfo> table, TableView<FolderInfo> tableToSearch) {

//		model_main.tables().getSorted_table()
		folderNeedsToUpdate.set(false);
		for (FolderInfo folderInfo : table.getItems()) {
			folderCounter.incrementAndGet();
			for (FileInfo fileInfoToFind : folderInfo.getFileInfoList()) {
				if (fileInfoToFind.isIgnored() == false) {
					if (fileInfoToFind.isTableDuplicated() == false) {
						Messages.sprintf(
								"fileInfoToFind " + fileInfoToFind + " dup? " + fileInfoToFind.isTableDuplicated());
						findDuplicate(fileInfoToFind, tableToSearch);
					}
				}
			}

//		Messages.sprintf("Updated folderInfo: " + folderInfo);
		}
		if (folderNeedsToUpdate.get() == true) {
			List<FolderInfo> toRemove = new ArrayList<>();
			Iterator<FolderInfo> foi = model_main.tables().getSortIt_table().getItems().iterator();
			while (foi.hasNext()) {
				FolderInfo folderInfo = foi.next();
				TableUtils.updateFolderInfos_FileInfo(folderInfo);
				if (folderInfo.getFolderFiles() <= 0) {
					toRemove.add(folderInfo);
				}
				TableUtils.refreshTableContent(model_main.tables().getSorted_table());
				TableUtils.refreshTableContent(model_main.tables().getSortIt_table());
			}
			Main.setChanged(true);
			if (!toRemove.isEmpty()) {
				model_main.tables().getSortIt_table().getItems().removeAll(toRemove);
			}
		}
		folderNeedsToUpdate.set(false);
		Messages.warningText("There were " + duplicateCounter + " duplicateCounter " + " folderCounter " + folderCounter
				+ " fileEnterCounter " + fileEnterCounter);
		duplicateCounter.set(0);
		folderCounter.set(0);
		fileCounter.set(0);
		folderEnterCounter.set(0);
		fileEnterCounter.set(0);
	}

	private void findDuplicate(FileInfo fileInfoToFind, TableView<FolderInfo> table) {
		for (FolderInfo folderInfo : table.getItems()) {
			if (folderInfo.getFolderFiles() > 0) {
				for (FileInfo fileInfoSearch : folderInfo.getFileInfoList()) {
					fileEnterCounter.incrementAndGet();
					if (!fileInfoSearch.isTableDuplicated()) {
						if (fileInfoSearch.getOrgPath() != fileInfoToFind.getOrgPath()) {
							if (fileInfoSearch.getDate() == fileInfoToFind.getDate()
									&& fileInfoSearch.getSize() == fileInfoToFind.getSize()) {
								fileInfoSearch.setTableDuplicated(true);
								duplicateCounter.incrementAndGet();
//								if ((folderInfo.getFolderFiles() - 1) > 0) {
//									folderInfo.setFolderFiles(folderInfo.getFolderFiles() - 1);
//								}
								if (!folderNeedsToUpdate.get()) {
									folderNeedsToUpdate.set(true);
								}
								Messages.sprintf("sortit FOUND fileInfoToFind: " + fileInfoToFind
										+ "  fileInfoToFind.isTableDuplicated() " + fileInfoToFind.isTableDuplicated()
										+ " DUPLICATED file: " + fileInfoSearch.getOrgPath()
										+ " fileInfoSearch.isTableDuplicated() " + fileInfoSearch.isTableDuplicated()
										+ " folderNeedsToUpdate? " + folderNeedsToUpdate);
							}
						}
					}
				}
			}
		}
	}

	@FXML
	private Button dates_ttv_btn;

	@FXML
	private void dates_ttv_btn_action(ActionEvent event) {
		try {
			Parent parent = null;
			FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/DatesTreeTableView.fxml"), Main.bundle);
			parent = loader.load();
			DatesTreeTableViewController datesTreeTableViewController = (DatesTreeTableViewController) loader.getController();
			datesTreeTableViewController.init(model_main);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void collect_action(ActionEvent event) {
		Collector collect = new Collector();
		collect.collectAll(model_main.tables());
		collect.listMap(model_main.tables());

//		drive_pane.visibleProperty().model_main

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
//		fc_stage.setWidth(conf.getScreenBounds().getWidth());
//		fc_stage.setHeight(conf.getScreenBounds().getHeight() / 1.3);
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

	}

	private void findSameDateFileInfosFromSortedAndWorkdir(Tables tables) {
		// TEsting possible move from sortit to sorted
		for (FolderInfo folderInfo : tables.getSortIt_table().getItems()) {
			for (FileInfo fileInfo : folderInfo.getFileInfoList()) {
				FileInfo possibleDuplicate = model_main.getWorkDir_Handler().exists(fileInfo);
				if (possibleDuplicate != null) {
					Messages.sprintf(
							"File were already copied to destination: " + possibleDuplicate.getDestination_Path());
//					fileInfo.setDestination_Path(possibleDuplicate.getDestination_Path());
				} else {
					TableUtils.findPossibleNewDestinationByDate(fileInfo, tables);
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
		Messages.warningTextHelp(
				"Drag and drop folders to left \"SortIt\" which are not created by you or you want them to be sorted manualy",
				HTMLClass.help_html + "#sorter");
	}

	@FXML
	private void options_btn_action(ActionEvent event) {

	}

	@FXML
	private void start_copyBatch_btn_action(ActionEvent event) {
		Main.setProcessCancelled(false);
		model_main.getMonitorExternalDriveConnectivity().cancel();

		if (Main.conf.getWorkDir().equals("null")) {
			Messages.warningText(Main.bundle.getString("workDirHasNotBeenSet"));
			return;
		}
		if (!Main.conf.getDrive_connected() || !Files.exists(Paths.get(Main.conf.getWorkDir()))) {
			Messages.warningText(Main.bundle.getString("workDirHasNotConnected"));
			return;
		}
		Messages.sprintf("workDir: " + Main.conf.getWorkDir());
		/*
		 * List files and handle actions with lists. For example ok files, conflict
		 * files(Handle this before ok files), bad files(Handle this before okfiles)
		 * When everything are good will be operateFiles starts. Notice! Everything are
		 * in memory already so concurrency can be used to prevent the lagging.
		 */

		CopyBatch copyBatch = new CopyBatch(model_main);
		copyBatch.start();

		// check Destinatiin duplicates/existens
		/*
		 * Main.conf.work if(!copyBatch.getConflictList().isEmpty()) {
		 * copyBatch.getConflictList(); }
		 */
//TODO Testaan ensin workdir konfliktien varalta ennen kopiointia. Täytyy pystyy korjaavaaman ne ennen kopintia. cantcopyt tulee errori 

	}

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
		drive_pane.visibleProperty().bindBidirectional(Main.conf.drive_connected_property());
		Main.conf.drive_connected_property().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue == true) {
					drive_connected.setStyle("-fx-background-color: green;");
					start_copyBatch_btn.setDisable(false);

				} else if (newValue == false) {

					drive_connected.setStyle("-fx-background-color: red;");
					start_copyBatch_btn.setDisable(true);
				}
				Messages.sprintf("drive connected? " + newValue);
			}

		});
		Main.conf.setDrive_connected(true);
		Main.conf.setDrive_connected(false);
	}

	public void init(Model_main aModel_main) {
		this.model_main = aModel_main;
		sprintf("bottom controller...");
//		Main.conf.drive_property
//		
//		drive_connected.textProperty().bindBidirectional(Main.conf.drive_connected_property());
	}

}
