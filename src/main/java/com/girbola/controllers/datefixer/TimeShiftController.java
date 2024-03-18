/*
 @(#)Copyright:  Copyright (c) 2012-2024 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.datefixer;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;

public class TimeShiftController {

	private Model_datefix model_dateFix;
	@FXML
	private ToggleGroup plusMinus;

	@FXML
	private TextField timeShift_hour;
	@FXML
	private Button timeShift_hour_btn_down;
	@FXML
	private Button timeShift_hour_btn_up;
	@FXML
	private TextField timeShift_min;
	@FXML
	private Button timeShift_min_btn_down;
	@FXML
	private Button timeShift_min_btn_up;
	@FXML
	private TextField timeShift_sec;
	@FXML
	private Button timeShift_sec_btn_down;
	@FXML
	private Button timeShift_sec_btn_up;

	public void init(Model_datefix model_dateFix) {
		this.model_dateFix = model_dateFix;
	}

	@FXML
	private void timeShift_hour_action(ActionEvent event) {
	}

	@FXML
	private void timeShift_hour_btn_down_action(ActionEvent event) {
	}

	@FXML
	private void timeShift_hour_btn_up_action(ActionEvent event) {
	}

	@FXML
	private void timeShift_min_action(ActionEvent event) {
	}

	@FXML
	private void timeShift_min_btn_down_action(ActionEvent event) {
	}

	@FXML
	private void timeShift_min_btn_up_action(ActionEvent event) {
	}

	@FXML
	private void timeShift_sec_action(ActionEvent event) {
	}

	@FXML
	private void timeShift_sec_btn_down_action(ActionEvent event) {
	}

	@FXML
	private void timeShift_sec_btn_up_action(ActionEvent event) {
	}

}
