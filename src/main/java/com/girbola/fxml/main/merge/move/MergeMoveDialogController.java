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
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;

import common.utils.FileUtils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class MergeMoveDialogController {

	final private org.slf4j.Logger ERROR = org.slf4j.LoggerFactory.getLogger(getClass());
	
//	private final String ERROR = MergeMoveDialogController.class.getSimpleName();

	private Model_main model_Main;

	@FXML
	private ListView<FolderInfo> move_accepted_listView;

	@FXML
	private Button cancel_btn;

	@FXML
	private Button move_accepted_btn;

	private Tables tables;

	private TableView<FolderInfo> table;

	private String tableType;

	private String selectedFolder;

	private String selectedTableType;

	@FXML
	private void cancel_btn_action(ActionEvent event) {
		Stage stage = (Stage) cancel_btn.getScene().getWindow();
		stage.close();
	}

	@FXML
	private void move_accepted_btn_action(ActionEvent event) throws IOException {
		Path dest = Paths.get(selectedFolder);
		List<FileInfo> fileInfo_list = new ArrayList<>();

		Messages.sprintf("DEST Folder exists: " + dest);

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
				if (!folderInfo.getFolderPath().equals(dest.toString())) {
					move(folderInfo, destFolderInfo);
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

			// Save changes
		}
	}

	private void move(FolderInfo srcFolderInfo, FolderInfo destFolderInfo) throws IOException {
		Iterator<FileInfo> srcFolderInfo_it = srcFolderInfo.getFileInfoList().iterator();
		
		Map<String, String> exists_map = checkIfFilesExistsAtDestination(srcFolderInfo);
		
		// Check if files exists already on destination
		
		srcFolderInfo_it = srcFolderInfo.getFileInfoList().iterator();
		
		while (srcFolderInfo_it.hasNext()) {
			FileInfo fileInfo = srcFolderInfo_it.next();

			Path srcFile = Paths.get(fileInfo.getOrgPath());
			Path destFolder = Paths.get(destFolderInfo.getFolderPath() + File.separator + srcFile.getFileName());

			Path renameFileToDate = FileUtils.renameFile(srcFile, destFolder);
			if (renameFileToDate != null) {
				Messages.sprintf("srcFile is: " + srcFile + " renameFile: " + renameFileToDate);

				Files.move(srcFile, renameFileToDate, StandardCopyOption.REPLACE_EXISTING);

				final String fileInfoOrgSrc = fileInfo.getOrgPath();

				destFolderInfo.getFileInfoList().add(fileInfo);

				fileInfo.setOrgPath(renameFileToDate.toString());

				Messages.sprintf("fileInfo.toString();: " + fileInfo.toString());

				destFolderInfo.getFileInfoList().add(fileInfo);
				Files.move(Paths.get(fileInfo.getOrgPath()), destFolder);

				srcFolderInfo_it.remove();
			}
		}

		srcFolderInfo.setChanged(true);
		destFolderInfo.setChanged(true);

		TableUtils.saveChangesContentsToTables(model_Main.tables());

	}

	private Map<String, String> checkIfFilesExistsAtDestination(FolderInfo srcFolderInfo, Path destFolder) {
		Map<String, String> duplicate_List = new HashMap<>();
		
		Iterator<FileInfo> srcFolderInfo_it = srcFolderInfo.getFileInfoList().iterator();
		
		while (srcFolderInfo_it.hasNext()) {
			FileInfo fileInfo = srcFolderInfo_it.next();
			Path destFile= Paths.get(fileInfo.getOrgPath());
		}
		return null;
		

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
				selectedFolder = newValue.getFolderPath();
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
