package com.girbola.fxml.conflicttableview;

import com.girbola.controllers.main.Model_main;
import com.girbola.controllers.main.tables.FolderInfo;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class ConflictTableViewController {

	private Model_main model_Main;
	private ObservableList<ConflictFile> obs = FXCollections.observableArrayList();

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

	public void init(Model_main model_Main) {
		this.model_Main = model_Main;
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

		tableView.setItems(obs);
		obs.add(new ConflictFile("folderName", "destination", "workDir", true));

	}

	public void populateTable() {

	}

}
