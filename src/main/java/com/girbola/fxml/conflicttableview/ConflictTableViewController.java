package com.girbola.fxml.conflicttableview;

import com.girbola.controllers.main.Model_main;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class ConflictTableViewController {

	private Model_main model_Main;

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
	}
	
	public void populateTable() {
		
	}

}
