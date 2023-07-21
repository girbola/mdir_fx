/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved.  
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.fxml.misc;

import com.girbola.Main;
import com.girbola.controllers.main.Model_main;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.stage.Stage;

import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;

public class ViewIgnoredListController implements Initializable {

	@FXML
	private Button cancel_btn;

	private ObservableList<Path> ignoredList = FXCollections.observableArrayList();

	@FXML
	private Button apply_btn;

	@FXML
	private ListView<Path> ignoredList_lv;

	private Model_main model_main;
	@FXML
	private Button remove_btn;

	@FXML
	private void apply_btn_action(ActionEvent event) {
		Main.conf.setIgnoredFoldersScanList(ignoredList);
		Stage stage = (Stage) cancel_btn.getScene().getWindow();
		stage.close();
	}

	@FXML
	private void cancel_btn_action(ActionEvent event) {
		Stage stage = (Stage) cancel_btn.getScene().getWindow();
		stage.close();
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ignoredList.clear();
		ignoredList.addAll(Main.conf.getIgnoredFoldersScanList());
		ignoredList_lv.setItems(ignoredList);
		ignoredList_lv.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
	}

	@FXML
	private void remove_btn_action(ActionEvent event) {
		List<Path> toRemoved = ignoredList_lv.getSelectionModel().getSelectedItems();
		ignoredList_lv.getItems().removeAll(toRemoved);
	}
}
