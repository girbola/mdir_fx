package com.girbola.fxml.conflicttableview;

import com.girbola.Main;
import com.girbola.controllers.main.Model_main;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.fileinfo.FileInfo;
import com.girbola.fxml.operate.OperateFiles;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class ConflictTableViewController {

	private Model_main model_Main;
	private ObservableList<ConflictFile> list = FXCollections.observableArrayList();

	@FXML
	private Button fixConflicts_btn;
	@FXML
	private TableView<ConflictFile> tableView;

	@FXML
	private TableColumn<ConflictFile, String> folderName;

	@FXML
	private TableColumn<ConflictFile, String> destination;

	@FXML
	private TableColumn<ConflictFile, String> workDir;

	@FXML
	private TableColumn<ConflictFile, Boolean> canCopy;

	@FXML
	private void fixConflicts_btn_action(ActionEvent event) {
		for (ConflictFile cf : list) {
			if (Main.conf.getWorkDir() != cf.getWorkDir()) {
				cf.setWorkDir(Main.conf.getWorkDir());
				cf.getFileInfo().setWorkDir(Main.conf.getWorkDir());
			}
		}
	}

	@FXML
	private void copy_btn_action(ActionEvent event) {
//		Task<Boolean> task = new OperateFiles(list, close, aModel_main, scene_NameType)
//aerg;
		
	}

	@FXML
	private void apply_btn_action(ActionEvent event) {

	}

	@FXML
	private void close_btn_action(ActionEvent event) {
		Platform.runLater(() -> {
			Stage stage = (Stage) ((Node) (event.getSource())).getScene().getWindow();
			stage.close();
		});

	}

	public void init(Model_main model_Main, ObservableList<ConflictFile> list) {
		this.model_Main = model_Main;
		this.list = list;
		folderName.setCellValueFactory(
				(TableColumn.CellDataFeatures<ConflictFile, String> cellData) -> new SimpleObjectProperty<>(
						cellData.getValue().getFolderName()));
		destination.setCellValueFactory(
				(TableColumn.CellDataFeatures<ConflictFile, String> cellData) -> new SimpleObjectProperty<>(
						cellData.getValue().getDestination()));

		workDir.setCellValueFactory(
				(TableColumn.CellDataFeatures<ConflictFile, String> cellData) -> new SimpleObjectProperty<>(
						cellData.getValue().getWorkDir()));

		canCopy.setCellValueFactory(
				(TableColumn.CellDataFeatures<ConflictFile, Boolean> cellData) -> new SimpleObjectProperty<>(
						cellData.getValue().getCanCopy()));

		tableView.setItems(list);
		// obs.add(new ConflictFile("folderName", "destination", "workDir", true));
		// populateTable();
	}

	public void populateTable() {
		for (FolderInfo folderInfo : model_Main.tables().getSorted_table().getItems()) {
			for (FileInfo fileInfo : folderInfo.getFileInfoList()) {

			}
		}
	}

}
