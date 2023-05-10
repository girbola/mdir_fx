package com.girbola.fxml.main.merge.move;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.girbola.controllers.main.Model_main;
import com.girbola.controllers.main.Tables;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.controllers.main.tables.FolderInfo_Utils;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;

import common.utils.FileUtils;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MergeMoveDialogController {

	final private org.slf4j.Logger ERROR = org.slf4j.LoggerFactory.getLogger(getClass());

	//@formatter:off
	@FXML private Button cancel_btn;
	@FXML private Button move_accepted_btn;
	@FXML private ListView<FolderInfo> move_accepted_listView;
	//@formatter:on

	private Model_main model_Main;

	private FolderInfo selectedFolder;

	private String selectedTableType;

	private TableView<FolderInfo> table;

	private Tables tables;

	private String tableType;

	@FXML
	private void cancel_btn_action(ActionEvent event) {
		Stage stage = (Stage) cancel_btn.getScene().getWindow();
		stage.close();
	}

	@FXML
	private void move_accepted_btn_action(ActionEvent event) throws IOException {

		model_Main.getMonitorExternalDriveConnectivity().cancel();

		Task<Void> move_task = new Task<>() {

			@Override
			protected Void call() throws Exception {

				List<FileInfo> fileInfo_list = new ArrayList<>();

				Path dest = Paths.get(selectedFolder.getFolderPath());

				Path realPath = dest.toRealPath();
				
				Messages.sprintf("DEST WILL BE: " + dest + " real: " + realPath);

				if (Files.exists(realPath)) {
					Messages.sprintf("DEST Folder exists: " + realPath);

					FolderInfo destFolderInfo = null;

					for (FolderInfo folderInfo : selectedFolderInfo_List) {
						if (folderInfo.getFolderPath().equals(dest.toString())) {
							destFolderInfo = folderInfo;
						}
					}
					if (destFolderInfo == null) {
						Messages.warningText("Can't find destination!!");
						this.cancel();
						return null;
					}

					for (FolderInfo folderInfo : selectedFolderInfo_List) {
						Messages.sprintf("Selected folderInfo: " + folderInfo.getFolderPath() + " DEST IS NOW: " +  dest);
						Messages.sprintf("destFolderInfo: " + destFolderInfo.getFolderPath());
						if (!folderInfo.getFolderPath().equals(dest.toString())) {
							move(folderInfo, destFolderInfo);
						} else {
							Messages.sprintf("File already exists at destination" + folderInfo.getFolderPath()
									+ " dest.toString: " + dest.toString());
							this.cancel();
						}
					}

					for (FileInfo fileInfo : fileInfo_list) {
						Path fileName = Paths.get(fileInfo.getOrgPath()).getFileName();
						Path root = Paths.get(fileInfo.getOrgPath()).getParent();
						Messages.sprintf("SRC: " + fileInfo.getOrgPath() + " newDestination: " + dest);
					}

					// Update fileinfo

					// Remove folder which were not selected.

				}
				return null;

			}

		};

		move_task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

			@Override
			public void handle(WorkerStateEvent event) {
				// Save changes
				Stage stage = (Stage) move_accepted_btn.getScene().getWindow();
				// do what you have to do
				stage.close();
				model_Main.getMonitorExternalDriveConnectivity().start();
			}
		});

		move_task.setOnFailed(new EventHandler<WorkerStateEvent>() {

			@Override
			public void handle(WorkerStateEvent event) {
				// Save changes
				Stage stage = (Stage) move_accepted_btn.getScene().getWindow();
				// do what you have to do
				stage.close();
				model_Main.getMonitorExternalDriveConnectivity().start();
			}
		});

		Thread move_th = new Thread(move_task, "move_task");
		move_th.start();

	}

	private void move(FolderInfo srcFolderInfo, FolderInfo destFolderInfo) throws IOException {
		Iterator<FileInfo> srcFolderInfo_it = srcFolderInfo.getFileInfoList().iterator();

		// Check if files exists already on destination
		List<FileInfo> exists_map = checkIfFilesExistsAtDestination(srcFolderInfo);

		StringBuilder stb = new StringBuilder();
		for (FileInfo fileInfo : exists_map) {
			stb.append(fileInfo.getOrgPath());
			stb.append("\n");
		}
		Messages.sprintf("exists STB: " + stb);

		if (!stb.isEmpty()) {
			Messages.warningText("file exists at dest: \n" + stb);
		}

		// Merge files
		srcFolderInfo_it = srcFolderInfo.getFileInfoList().iterator();

		while (srcFolderInfo_it.hasNext()) {
			FileInfo fileInfo = srcFolderInfo_it.next();

			Path sourceFilePath = Paths.get(fileInfo.getOrgPath());
			Path destFolder = Paths.get(destFolderInfo.getFolderPath() + File.separator + sourceFilePath.getFileName());

			Path destinationPath = FileUtils.renameFile(sourceFilePath, destFolder);
			Messages.sprintf("====RENAMED: rcFile Current file  is: " + sourceFilePath
					+ " renamed destinationPath: " + destinationPath);
			Messages.sprintf("Current file: " + fileInfo.getOrgPath() + " destinationPath: " + destinationPath);

			if (destinationPath != null) {
				Messages.sprintf("srcFile is: " + sourceFilePath + " destinationPath: " + destinationPath);

//				Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
				Messages.sprintf("Moving file FROM: " + sourceFilePath + " to TARGET: " + destinationPath);

				final String fileInfoOrgSrc = fileInfo.getOrgPath();

				destFolderInfo.getFileInfoList().add(fileInfo);

				fileInfo.setOrgPath(destinationPath.toString());

				Messages.sprintf("fileInfo.toString();: " + fileInfo.toString());

				destFolderInfo.getFileInfoList().add(fileInfo);
				Path move = Files.move(Paths.get(fileInfo.getOrgPath()), destinationPath);
				Messages.sprintf("MOVEEEEEEEEEEEE::::::::::::::: " + move);
				srcFolderInfo_it.remove();
			}
		}

		srcFolderInfo.setChanged(true);
		destFolderInfo.setChanged(true);

//		TableUtils.saveChangesContentsToTables(model_Main.tables());

	}

	private List<FileInfo> checkIfFilesExistsAtDestination(FolderInfo srcFolderInfo) {
		List<FileInfo> duplicate_List = new ArrayList<>();

		Iterator<FileInfo> srcFolderInfo_it = srcFolderInfo.getFileInfoList().iterator();

		while (srcFolderInfo_it.hasNext()) {
			FileInfo fileInfo = srcFolderInfo_it.next();

			for (FileInfo fileInfoList : selectedFolder.getFileInfoList()) {

				if (fileInfo.getSize() == fileInfoList.getSize()) {
					duplicate_List.add(fileInfo);
				}
			}

//			Iterator<FileInfo> destFolder = selectedFolder.getFileInfoList().iterator();

//			Path destFile= Paths.get(fileInfo.getOrgPath());
		}
		return duplicate_List;

	}

	private ObservableList<FolderInfo> selectedFolderInfo_List;

	public void init(Model_main model_Main, Tables tables, TableView<FolderInfo> table, String tableType) {
		this.model_Main = model_Main;
		this.tables = tables;
		this.table = table;
		this.tableType = tableType;
		this.selectedFolderInfo_List = FXCollections.observableArrayList();
		selectedFolderInfo_List.addAll(tables.getSorted_table().getSelectionModel().getSelectedItems());

		move_accepted_listView.setItems(selectedFolderInfo_List);
		move_accepted_listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		move_accepted_listView.setCellFactory(param -> extracted());

		move_accepted_listView.getSelectionModel().selectedItemProperty()
				.addListener((ChangeListener<FolderInfo>) (observable, oldValue, newValue) -> {
					selectedFolder = newValue;
					selectedTableType = newValue.getTableType();
					Messages.sprintf("folderInfoooo: " + newValue.getFolderPath());
				});
		populateList();
	}

	private ListCell<FolderInfo> extracted() {
		return new ListCell<FolderInfo>() {
			@Override
			protected void updateItem(FolderInfo item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null || item.getJustFolderName() == null) {
					setText(null);
				} else {
					setText(item.getJustFolderName());
				}
			}
		};
	}

	private void populateList() {
		selectedFolderInfo_List = tables.getSorted_table().getSelectionModel().getSelectedItems();
	}

}
