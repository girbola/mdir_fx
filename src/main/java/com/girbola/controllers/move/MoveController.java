package com.girbola.controllers.move;

import com.girbola.controllers.datefixer.ModelDatefix;
import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.tables.FolderInfoUtils;
import com.girbola.fileinfo.FileInfo;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class MoveController {

   @FXML private ResourceBundle resources;

   @FXML private URL location;

   @FXML private Label folderName_lbl;

   @FXML private ComboBox<FileInfo> folderName_cmb;

   @FXML private Label location_lbl;

   @FXML private ComboBox<String> event_cmb;

   @FXML private ComboBox<String> location_cmb;

   @FXML private ComboBox<String> user_cmb;

   @FXML private Label event_lbl;

   @FXML private Label user_lbl;

   @FXML private CheckBox addEverythingInsameDir_chb;

   @FXML private Button apply_btn;

   @FXML private Button apply_and_copy_btn;

   @FXML private Button cancel_btn;

   @FXML void apply_and_move_btn_action(ActionEvent event) {

	}

   @FXML void apply_btn_action(ActionEvent event) {

	}

   @FXML void cancel_btn_action(ActionEvent event) {

	}

   @FXML void initialize() {
		assert folderName_lbl != null
				: "fx:id=\"folderName_lbl\" was not injected: check your FXML file 'SplitFolderToNewFolderName.fxml'.";
		assert folderName_cmb != null
				: "fx:id=\"folderName_cmb\" was not injected: check your FXML file 'SplitFolderToNewFolderName.fxml'.";
		assert location_lbl != null
				: "fx:id=\"location_lbl\" was not injected: check your FXML file 'SplitFolderToNewFolderName.fxml'.";
		assert location_cmb != null
				: "fx:id=\"location_cmb\" was not injected: check your FXML file 'SplitFolderToNewFolderName.fxml'.";
		assert event_lbl != null
				: "fx:id=\"event_lbl\" was not injected: check your FXML file 'SplitFolderToNewFolderName.fxml'.";
		assert event_cmb != null
				: "fx:id=\"event_cmb\" was not injected: check your FXML file 'SplitFolderToNewFolderName.fxml'.";
		assert user_lbl != null
				: "fx:id=\"user_lbl\" was not injected: check your FXML file 'SplitFolderToNewFolderName.fxml'.";
		assert user_cmb != null
				: "fx:id=\"user_cmb\" was not injected: check your FXML file 'SplitFolderToNewFolderName.fxml'.";
		assert addEverythingInsameDir_chb != null
				: "fx:id=\"addEverythingInsameDir_chb\" was not injected: check your FXML file 'SplitFolderToNewFolderName.fxml'.";
		assert apply_btn != null
				: "fx:id=\"apply_btn\" was not injected: check your FXML file 'SplitFolderToNewFolderName.fxml'.";
		assert apply_and_copy_btn != null
				: "fx:id=\"apply_and_copy_btn\" was not injected: check your FXML file 'SplitFolderToNewFolderName.fxml'.";
		assert cancel_btn != null
				: "fx:id=\"cancel_btn\" was not injected: check your FXML file 'SplitFolderToNewFolderName.fxml'.";

		ObservableList<FileInfo> obs_event = FolderInfoUtils.getEvents(model_datefix.getFolderInfo_full());
		folderName_cmb.setItems(obs_event);
		
//		List<FolderInfo> folderInfo_location = FolderInfoUtils.getLocation(model_datefix.getFolderInfo_full());
	}

	private ModelMain model_main;
	private ModelDatefix model_datefix;

	public void init(ModelMain model_main, ModelDatefix model_datefix) {
		this.model_main = model_main;
		this.model_datefix = model_datefix;
	}
}
