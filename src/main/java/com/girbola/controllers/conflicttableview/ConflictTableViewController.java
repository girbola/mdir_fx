package com.girbola.controllers.conflicttableview;

import com.girbola.Main;
import com.girbola.SceneNameType;
import com.girbola.controllers.main.ModelMain;
import com.girbola.fileinfo.FileInfo;
import com.girbola.controllers.operate.OperateFiles;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class ConflictTableViewController {

	private ModelMain model_Main;
	private ObservableList<FileInfo> list = FXCollections.observableArrayList();

   @FXML private Button fixConflicts_btn;
   @FXML private TableView<FileInfo> tableView;

   @FXML private TableColumn<FileInfo, String> folderName;

   @FXML private TableColumn<FileInfo, String> destination;

   @FXML private TableColumn<FileInfo, String> workDir;

   @FXML private TableColumn<FileInfo, Boolean> canCopy;

   @FXML private void fixConflicts_btn_action(ActionEvent event) {
		for (FileInfo fileInfo : list) {
			if (Main.conf.getWorkDir() != fileInfo.getWorkDir()) {
				fileInfo.setWorkDir(Main.conf.getWorkDir());
				fileInfo.setWorkDirDriveSerialNumber(Main.conf.getWorkDirSerialNumber());
			}
		}
	}

   @FXML private void copy_btn_action(ActionEvent event) {

	   OperateFiles task = new OperateFiles(list, true, model_Main, SceneNameType.MAIN.getType());


	}

   @FXML private void apply_btn_action(ActionEvent event) {

	}

   @FXML private void close_btn_action(ActionEvent event) {
		Platform.runLater(() -> {
			Stage stage = (Stage) ((Node) (event.getSource())).getScene().getWindow();
			stage.close();
		});

	}

	public void init(ModelMain model_Main, ObservableList<FileInfo> list) {
		this.model_Main = model_Main;
		this.list = list;
		folderName.setCellValueFactory(
				(TableColumn.CellDataFeatures<FileInfo, String> cellData) -> new SimpleObjectProperty<>(
						cellData.getValue().getOrgPath()));
		destination.setCellValueFactory(
				(TableColumn.CellDataFeatures<FileInfo, String> cellData) -> new SimpleObjectProperty<>(
						cellData.getValue().getDestination_Path()));

		workDir.setCellValueFactory(
				(TableColumn.CellDataFeatures<FileInfo, String> cellData) -> new SimpleObjectProperty<>(
						cellData.getValue().getWorkDir()));

//		canCopy.setCellValueFactory(new PropertyValueFactory<>("You"));
		canCopy.setCellValueFactory(cellData -> {
			FileInfo fileInfo = cellData.getValue();
			if (Main.conf.getWorkDir() != null) {
				if (Main.conf.getWorkDir() != fileInfo.getWorkDir() && Main.conf.getWorkDirSerialNumber() != fileInfo.getWorkDirDriveSerialNumber()) {
					return new SimpleBooleanProperty(false);
				}
			}
			return new SimpleBooleanProperty(true);
		});

		tableView.setItems(list);
		// obs.add(new ConflictFile("folderName", "destination", "workDir", true));
		// populateTable();
	}

//	public void populateTable() {
//		for (FolderInfo folderInfo : model_Main.tables().getSorted_table().getItems()) {
//			for (FileInfo fileInfo : folderInfo.getFileInfoList()) {
//
//			}
//		}
//	}

}
