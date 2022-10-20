package com.girbola.fxml.main.merge.move;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.girbola.controllers.main.Model_main;
import com.girbola.controllers.main.Tables;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.controllers.main.tables.tabletype.TableType;
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

	private final String ERROR = MergeMoveDialogController.class.getSimpleName();

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

			for (FolderInfo folderInfo : folderInfo_List) {
				if (!folderInfo.getFolderPath().equals(dest.toString())) {
					Messages.sprintf("<<<<<<folderInfo: + " + folderInfo.getFolderPath());
					for (FileInfo fileInfo : folderInfo.getFileInfoList()) {
						fileInfo_list.add(fileInfo);
					}
				}
			}

			for (FileInfo fileInfo : fileInfo_list) {
//				Messages.sprintf("File to be copied: " + fileInfo.getOrgPath());
				Path fileName = Paths.get(fileInfo.getOrgPath()).getFileName();
				Path root = Paths.get(fileInfo.getOrgPath()).getParent();
				Path newDestination = Paths.get(root.toString() + File.separator + fileName.toString());
				Messages.sprintf("SRC: " + fileInfo.getOrgPath() + " newDestination: " + newDestination);

			}

			// Update fileinfo

			// Remove folder which were not selected.

			// Save changes
		}
	}

	private ObservableList<FolderInfo> folderInfo_List;

	public void init(Model_main model_Main, Tables tables, TableView<FolderInfo> table, String tableType) {
		this.model_Main = model_Main;
		this.tables = tables;
		this.table = table;
		this.tableType = tableType;
		this.folderInfo_List = FXCollections.observableArrayList();
		folderInfo_List.addAll(tables.getSorted_table().getSelectionModel().getSelectedItems());
//		folderInfo_List.addAll(tables.getSortIt_table().getSelectionModel().getSelectedItems());

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
