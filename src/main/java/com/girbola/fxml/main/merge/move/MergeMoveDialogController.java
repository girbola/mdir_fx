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
import javafx.beans.value.ObservableValue;
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

	@FXML
	private Button cancel_btn;

	@FXML
	private Button move_accepted_btn;

	@FXML
	private ListView<FolderInfo> move_accepted_listView;

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

		Task<Void> move_task = new Task<>() {

			@Override
			protected Void call() throws Exception {
				// TODO Auto-generated method stub

				List<FileInfo> fileInfo_list = new ArrayList<>();

				Path dest = Paths.get(selectedFolder.getFolderPath());

				Path realPath = dest.toRealPath();
				if (Files.exists(realPath)) {
					Messages.sprintf("DEST Folder exists: " + realPath);

					FolderInfo destFolderInfo = null;

					for (FolderInfo folderInfo : folderInfo_List) {
						if (folderInfo.getFolderPath().equals(dest.toString())) {
							destFolderInfo = folderInfo;
						}
					}

					for (FolderInfo folderInfo : folderInfo_List) {
						Messages.sprintf("====folderInfo IS: " + folderInfo.getFolderPath() + " destiiii: "
								+ dest.toString() + " dest info: " + destFolderInfo.getFolderPath());
						if (!folderInfo.getFolderPath().equals(dest.toString())) {
							move(folderInfo, destFolderInfo);
							Messages.sprintf(
									"Moving content: " + folderInfo + " text: " + destFolderInfo.getFolderPath());
						} else {
							Messages.sprintf("folderInfo.getFolderPath " + folderInfo.getFolderPath()
									+ " dest.toString: " + dest.toString());
						}
					}

					for (FileInfo fileInfo : fileInfo_list) {
//				Messages.sprintf("File to be copied: " + fileInfo.getOrgPath());
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
			}
		});

		move_task.setOnFailed(new EventHandler<WorkerStateEvent>() {

			@Override
			public void handle(WorkerStateEvent event) {
				// Save changes
				Stage stage = (Stage) move_accepted_btn.getScene().getWindow();
				// do what you have to do
				stage.close();
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
			Messages.sprintf("====RENAMED: rcFile Current file  is: " + sourceFilePath + " maybe renamed destinationPath: " + destinationPath);
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

	private ObservableList<FolderInfo> folderInfo_List;

	public void init(Model_main model_Main, Tables tables, TableView<FolderInfo> table, String tableType) {
		this.model_Main = model_Main;
		this.tables = tables;
		this.table = table;
		this.tableType = tableType;
		this.folderInfo_List = FXCollections.observableArrayList();
		folderInfo_List.addAll(tables.getSorted_table().getSelectionModel().getSelectedItems());

		move_accepted_listView.setItems(folderInfo_List);
		move_accepted_listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		move_accepted_listView.setCellFactory(param -> new ListCell<FolderInfo>() {
			@Override
			protected void updateItem(FolderInfo item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null || item.getJustFolderName() == null) {
					setText(null);
				} else {
					setText(item.getJustFolderName());
				}
			}
		});

		move_accepted_listView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<FolderInfo>() {

			@Override
			public void changed(ObservableValue<? extends FolderInfo> observable, FolderInfo oldValue,
					FolderInfo newValue) {
				selectedFolder = newValue;
				selectedTableType = newValue.getTableType();
				Messages.sprintf("folderInfoooo: " + newValue.getFolderPath());
			}
		});
		populateList();
	}

	private void populateList() {
		folderInfo_List = tables.getSorted_table().getSelectionModel().getSelectedItems();
	}

}
