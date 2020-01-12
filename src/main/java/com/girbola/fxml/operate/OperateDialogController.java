/*
 @(#)Copyright:  Copyright (c) 2012-2020 All right reserved.
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.fxml.operate;

import java.util.ResourceBundle;

import com.girbola.Main;
import com.girbola.controllers.main.Model_operate;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.util.converter.NumberStringConverter;

/**
 * FXML Controller class
 *
 * @author Marko Lokka
 */
public class OperateDialogController {

	private final String ERROR = OperateDialogController.class.getSimpleName();

	@FXML
	ResourceBundle bundle;
	@FXML
	private Label cp_copyTo_Input;
	@FXML
	private Label cp_copyFrom_Input;
	@FXML
	private Label cp_copied_Input;
	@FXML
	private Label cp_renamedFiles_Input;
	@FXML
	private Label cp_totalFiles_Input;
	@FXML
	private Label cp_filesLeft_Input;
	@FXML
	private Label cp_transferRate_Input;
	@FXML
	private Label cp_timeElapsed_Input;
	@FXML
	private Label cp_timeLeft_Input;
	@FXML
	private Label cp_duplicatedFiles_Input;
	@FXML
	private ProgressBar totalProgressBar;
	@FXML
	private Button start_btn;
	@FXML
	private Button cancel_btn;
	@FXML
	private Label current_workdir;

	@FXML
	private Button listOfWorkDir;
	@FXML
	private ProgressBar copyProgressBar;

	private Model_operate model_operate;

	public void init(Model_operate aModel_operate) {
		this.model_operate = aModel_operate;

		Main.setProcessCancelled(false);
		cp_copyFrom_Input.textProperty().bindBidirectional(model_operate.getCopyProcess_values().copyFrom_property());
		cp_copyTo_Input.textProperty().bindBidirectional(model_operate.getCopyProcess_values().copyTo_property());
		cp_filesLeft_Input.textProperty().bindBidirectional(model_operate.getCopyProcess_values().filesLeft_property(), new NumberStringConverter());
		cp_copied_Input.textProperty().bindBidirectional(model_operate.getCopyProcess_values().copied_property(), new NumberStringConverter());
		cp_renamedFiles_Input.textProperty().bindBidirectional(model_operate.getCopyProcess_values().renamed_property(), new NumberStringConverter());
		cp_timeLeft_Input.textProperty().bindBidirectional(model_operate.getCopyProcess_values().timeLeft_property());
		cp_timeElapsed_Input.textProperty().bindBidirectional(model_operate.getCopyProcess_values().timeElapsed_property());
		cp_totalFiles_Input.textProperty().bindBidirectional(model_operate.getCopyProcess_values().totalFiles_property());
		cp_transferRate_Input.textProperty().bindBidirectional(model_operate.getCopyProcess_values().transferRate_property());
		cp_duplicatedFiles_Input.textProperty().bindBidirectional(model_operate.getCopyProcess_values().duplicated_property(),
				new NumberStringConverter());
		copyProgressBar.progressProperty().bindBidirectional(model_operate.getCopyProcess_values().copyProgress_property());
		totalProgressBar.progressProperty().bindBidirectional(model_operate.getCopyProcess_values().totalProgress_property());

		this.model_operate.setStartButton(start_btn);
		this.model_operate.setCancelButton(cancel_btn);
		this.model_operate.setTotalFilesProgress(totalProgressBar);
		this.model_operate.setCopy_progressBar(copyProgressBar);
		current_workdir.setText(Main.conf.getWorkDir());
	}
}
