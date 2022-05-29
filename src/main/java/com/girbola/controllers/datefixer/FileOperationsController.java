/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.datefixer;

import static com.girbola.Main.bundle;
import static com.girbola.messages.Messages.sprintf;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.girbola.Main;
import com.girbola.concurrency.ConcurrencyUtils;
import com.girbola.controllers.main.Model_main;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.fileinfo.FileInfo;
import com.girbola.fileinfo.FileInfo_Utils;
import com.girbola.fxml.move.MoveController;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.sql.FolderInfo_SQL;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.StringConverter;

public class FileOperationsController {

	private final String ERROR = FileOperationsController.class.getSimpleName();

	private Model_datefix model_datefix;
	private Model_main model_main;
	private ObservableList<Folder> comboList = FXCollections.observableArrayList();

	@FXML
	private Button mixDates_btn;

	@FXML
	private Button fixBadDates_btn;

	@FXML
	private Button thisFolderIsOk_btn;

	@FXML
	private Button splitFolder_SortIt_btn;

	@FXML
	private Button splitFolder_Sorted_btn;

	@FXML
	private ComboBox<Folder> move_comboBox;

	@FXML
	private void fixBadDates_btn_action(ActionEvent event) {
		Messages.warningText("fixBadDates_btn_action NOT READY YET");
	}

	@FXML
	private void thisFolderIsOk_btn_action(ActionEvent event) {
		Messages.warningText("thisFolderIsOk_btn_action NOT READY YET");
	}

	@FXML
	private void splitFolder_Sorted_btn_action(ActionEvent event) {
//		aerg;
		ConcurrencyUtils.stopExecThread();
		Parent parent = null;
		// rgwerg;
		try {
			FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/move/Move.fxml"), bundle);
			parent = loader.load();
			
			Messages.warningText("model_main is null? " + (model_main == null ? true : false));
			MoveController moveController = (MoveController) loader.getController();
			moveController.init(model_main, model_datefix);

			Scene scene = new Scene(parent);
			scene.getStylesheets().add(Main.class.getResource(Main.conf.getThemePath() + "dateFixer.css").toExternalForm());
			Stage stage = new Stage();
			stage.setScene(scene);
			stage.show();
			stage.setOnHiding(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent event) {
					model_datefix.getSelectionModel().clearAll();
				}
			});
//			for (FileInfo fileInfo : model_main.getWorkDir_Handler().getWorkDir_List()) {
//				Messages.sprintf("===========WORKDIR::::: FileInfo: " + fileInfo.getOrgPath());
//			}
		} catch (Exception ex) {
			Logger.getLogger(DateFixerController.class.getName()).log(Level.SEVERE, null, ex);
			Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
		}
	}

	@FXML
	private void splitFolder_SortIt_btn_action(ActionEvent event) {

	}

	public void init(Model_datefix aModel_datefix, Model_main aModel_main) {
		this.model_datefix = aModel_datefix;
		this.model_main = aModel_main;

		for (FolderInfo folderInfo_dest : this.model_main.tables().getSorted_table().getItems()) {
			if (!model_datefix.getCurrentFolderPath().toString().equals(folderInfo_dest.getFolderPath())) {
				comboList.add(new Folder(Paths.get(folderInfo_dest.getFolderPath()), folderInfo_dest));

			}
		}
		move_comboBox.setConverter(new StringConverter<Folder>() {
			@Override
			public Folder fromString(String string) {
				return move_comboBox.getItems().stream().filter(ap -> ap.getName().equals(string)).findFirst()
						.orElse(null);
			}

			@Override
			public String toString(Folder object) {
				return object.getName();

			}
		});
		move_comboBox.valueProperty().addListener(new ChangeListener<Folder>() {
			@Override
			public void changed(ObservableValue<? extends Folder> observable, Folder oldValue, Folder newValue) {
				if (newValue != null) {
					sprintf("newValue: " + newValue.getPath());
				}
			}
		});
		move_comboBox.setItems(comboList);
	}

	@FXML
	private void mixDates_btn_action(ActionEvent event) {
	}

	@FXML
	private void move_comboBox_action(ActionEvent event) {
		sprintf("move_comboBox_action is under constructor");
//        ComboBox cb = (ComboBox) event.getSource();
		if (model_datefix.getSelectionModel().getSelectionList().isEmpty()) {
			Messages.warningText(bundle.getString("noSelectedFiles"));
			return;
		}

		Folder newDestinationFolder = (Folder) move_comboBox.getValue();
		if (!Files.exists(newDestinationFolder.getPath())) {
			Messages.warningText("Destination folder does not exists\n" + newDestinationFolder.getName() + " path: "
					+ newDestinationFolder.getPath());
			return;
		}

		FolderInfo folderInfo = model_datefix.getFolderInfo_full();

		List<FileInfo> selectedFileInfoList = new ArrayList<>();

		// Created selected files list from nodes fileinfos
		for (Node node : model_datefix.getSelectionModel().getSelectionList()) {
			if (node instanceof VBox) {
				if (node.getId().equals("imageFrame")) {
					FileInfo fileInfo = (FileInfo) node.getUserData();
					selectedFileInfoList.add(fileInfo);

					Path newFilePath = Paths.get(newDestinationFolder.getPath().toString() + File.separator
							+ Paths.get(fileInfo.getOrgPath()).getFileName().toString());
					fileInfo.setOrgPath(newFilePath.toString());

					newDestinationFolder.getFolderInfo().getFileInfoList().add(fileInfo);

					// Remove fileInfo from source
//					model_datefix.getFolderInfo_full().getFileInfoList().remove(fileInfo);
					model_datefix.getFolderInfo_full().setChanged(true);
				}
			}
		}

		// <Folder names> and <FileInfo list>
		Map<String, List<FileInfo>> map = new HashMap<>();

//		String currentFolderInfoFolder = "";

		// Create a map of folderinfo paths for updating current fileinfos to correct
		// folders
		boolean found = false;

		for (FileInfo fileInfo : selectedFileInfoList) {
			found = false;
			Path rootPath = Paths.get(fileInfo.getOrgPath()).getParent();
			for (Entry<String, List<FileInfo>> entry : map.entrySet()) {
				if (entry.getKey() == rootPath.toString()) {
					entry.getValue().add(fileInfo);
					found = true;
				}
			}
			if (!found || map.isEmpty()) {
				map.put(rootPath.toString(), new ArrayList<>(Arrays.asList(fileInfo)));
			}
		}
		boolean skip = false;
		// Move operation
		for (Entry<String, List<FileInfo>> entry : map.entrySet()) {

			if (Main.getProcessCancelled()) {
				break;
			}
			if (entry.getValue().size() > 0 || !Files.exists(Paths.get(entry.getKey()))) {

				for (FileInfo fileInfo : entry.getValue()) {
					boolean succeeded = FileInfo_Utils.moveFile(fileInfo);
					if (!succeeded) {
						Messages.sprintfError("File has been successfully moved: " + fileInfo.getOrgPath());
					}
					skip = false;
				}
			}
		}

		boolean update = false;
		for (Entry<String, List<FileInfo>> entry : map.entrySet()) {
			String folderSQLFileName = entry.getKey() + File.separator + Main.conf.getMdir_db_fileName();
//			if(folderSQLFileName)
			FolderInfo loadFolderInfo = FolderInfo_SQL.loadFolderInfo(folderSQLFileName);
//			if(loadFolderInfo == null) {
//				createNew
//			}
			for (FileInfo fileInfo : entry.getValue()) {
				if (!FileInfo_Utils.findDuplicates(fileInfo, loadFolderInfo)) {
					loadFolderInfo.getFileInfoList().add(fileInfo);
					if (!update) {
						update = true;
					}
				}
			}
			if (update) {
				TableUtils.updateFolderInfos_FileInfo(loadFolderInfo);
//				FolderInfo_SQL.saveFolderInfoToTable(connection_mdirFile, loadFolderInfo);
			}
		}

		// Remove moved fileinfos from original source
		folderInfo.getFileInfoList().removeAll(selectedFileInfoList);
		// Clean Source FolderInfo from moved FileInfos
		TableUtils.updateFolderInfos_FileInfo(folderInfo);

		folderInfo.getFileInfoList().removeAll(selectedFileInfoList);
		TableUtils.updateFolderInfos_FileInfo(folderInfo);

	}
}

class Folder {

	private Path path;
	private String name;
	private FolderInfo folderInfo;

	public FolderInfo getFolderInfo() {
		return folderInfo;
	}

	public void setFolderInfo(FolderInfo folderInfo) {
		this.folderInfo = folderInfo;
	}

	public Folder(Path path, FolderInfo folderInfo) {
		this.path = path;
		this.folderInfo = folderInfo;
		name = path.getFileName().toString();
	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
