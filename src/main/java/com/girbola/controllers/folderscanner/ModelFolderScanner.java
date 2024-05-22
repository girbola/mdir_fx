/*
 @(#)Copyright:  Copyright (c) 2012-2024 All right reserved.
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.folderscanner;

import com.girbola.Main;
import com.girbola.controllers.folderscanner.choosefolders.ChooseFoldersController;
import com.girbola.controllers.main.Model_main;
import com.girbola.drive.DrivesListHandler;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ModelFolderScanner {

	//	@SuppressWarnings("unused")
	private Model_main model_main;

//	private Connection connection = SqliteConnection.connector(Main.conf.getAppDataPath(), Main.conf.getFolderInfos_db_fileName());

	private ScanDrives scanDrives;
	private DrivesListHandler drivesListHandler = new DrivesListHandler();

	private ChooseFoldersController chooseFoldersController;

	private List<TreeItem<FolderInfoTable>> analyzeList_selected = new ArrayList<>();
	private ObservableList<Path> selectedDrivesFoldersList_obs = FXCollections.observableArrayList();
	private VBox analyzeList_vbox;
	private CheckBoxTreeItem<File> drives_rootItem;

	public ScanDrives getScanDrives() {
		return scanDrives;
	}

	public DrivesListHandler drive() {
		return this.drivesListHandler;
	}

	public List<TreeItem<FolderInfoTable>> getAnalyzeList_selected() {
		return analyzeList_selected;
	}

	public void setAnalyzeList_selected(List<TreeItem<FolderInfoTable>> analyzeList_selected) {
		this.analyzeList_selected = analyzeList_selected;
	}

	public VBox getAnalyzeList_vbox() {
		return analyzeList_vbox;
	}

	public void setAnalyzeList_vbox(VBox analyzeList_vbox) {
		this.analyzeList_vbox = analyzeList_vbox;
	}

	public ObservableList<Path> getSelectedDrivesFoldersList_obs() {
		return selectedDrivesFoldersList_obs;
	}

	// public void setDrivesList(ObservableList<SelectedFolder> drivesList) {
	// this.drivesList = drivesList;
	// }

	void setChooseFoldersController(ChooseFoldersController chooseFoldersController) {
		this.chooseFoldersController = chooseFoldersController;
	}

	public ChooseFoldersController getChooseFoldersController() {
		return chooseFoldersController;
	}

	public void setDeleteKeyPressed(TableView<SelectedFolder> table) {
		table.setOnKeyPressed((KeyEvent event) -> {
			if (event.getCode() == (KeyCode.DELETE)) {
				ObservableList<SelectedFolder> table_row_list = table.getSelectionModel().getSelectedItems();

				List<SelectedFolder> listToRemove = new ArrayList<>();

				for (SelectedFolder folderInfo : table_row_list) {
					listToRemove.add(folderInfo);
				}

				for (SelectedFolder folderInfo : listToRemove) {
					table.getItems().remove(folderInfo);
				}
				listToRemove.clear();

				table.getSelectionModel().clearSelection();
				Main.setChanged(true);
			}
		});
	}

	public void init(Model_main aModel_main, CheckBoxTreeItem<File> aDrives_rootItem) {
		this.model_main = aModel_main;
		this.drives_rootItem = aDrives_rootItem;
		drivesListHandler.loadList(this);
		scanDrives = new ScanDrives(this.model_main, drives_rootItem, selectedDrivesFoldersList_obs, drivesListHandler, this);
		
		scanDrives.restart();
	}
//
//	public Connection getConnection() {
//		return connection;
//	}
}
