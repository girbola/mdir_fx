/*
 @(#)Copyright:  Copyright (c) 2012-2020 All right reserved.
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.main.options;

import static com.girbola.Main.bundle;
import static com.girbola.Main.conf;
import static com.girbola.messages.Messages.sprintf;
import static com.girbola.messages.Messages.warningText;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;

import com.girbola.Main;
import com.girbola.configuration.Configuration_SQL_Utils;
import com.girbola.configuration.VLCJDiscovery;
import com.girbola.controllers.main.Model_main;
import com.girbola.controllers.main.SQL_Enums;
import com.girbola.drive.DriveInfo;
import com.girbola.messages.Messages;

import common.utils.OSHI_Utils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

public class OptionsController {

	@FXML
	private Button chooseFolder_workDir;
	@FXML
	private CheckBox confirmOnExit;
	@FXML
	private CheckBox savingThumbs;
	@FXML
	private CheckBox betterQuality;

	private Model_main model;
	@FXML
	private CheckBox showHints;
	@FXML
	private CheckBox showTooltips;
	@FXML
	private Button vlcPath_choose;
	@FXML
	private TextField vlcPath_input;
	@FXML
	private TextField workDir_input;

	@FXML
	private void chooseFolder_workDir_action(ActionEvent event) {
		Messages.sprintf("chooseFolder_workDir_action start");
		
		DirectoryChooser dc = new DirectoryChooser();
		File file = dc.showDialog(chooseFolder_workDir.getScene().getWindow());
		if (file != null) {
			if(Files.exists(file.toPath())) {
				if(!file.canWrite()) {
					Messages.warningText("Can't write to current path. Check you folder access");
				} else {
					
					Messages.sprintf("folder path is now: " + file.toString());
					Main.conf.setWorkDir(file.toString());
					
					Main.conf.setWorkDirSerialNumber(OSHI_Utils.getDriveSerialNumber((file.toPath().getRoot()).toString()));
					workDir_input.setText(Main.conf.getWorkDir());
					Configuration_SQL_Utils.update_Configuration();
				
				}
			} else {
				Messages.warningText("Can't find current path. Check you folder access");
			}
		/*	TODO
		    DriveInfo drive = new DriveInfo(aDrivePath, aDriveTotalSize, aConnected, aSelected, aIndentifier);
			workDir_input.setText(file.toString());
			conf.setWorkDir(file.toString());
			testWritingToWorkdir. if it is possible then continue etc.
			Myös lisää DriveInfo tässä kohtaan ja tallenna FileSystem info jotta voi sitten testata myöhemmin
			kokoa. Esim jos laittaa uuden aseman niin se täsmää ja jos ei täsmää niin ilmoitus käyttäjälle?
					Testaa CD aseman HASHcode ja samoin tikun hashcode. Ehkä niitä voi hyödyntää etsimisessä?*/
							
		}
	}

	public void init(Model_main model) {
		sprintf("OptionsController init...");
		this.model = model;
		confirmOnExit.selectedProperty().bindBidirectional(conf.confirmOnExit_property());
		showHints.selectedProperty().bindBidirectional(conf.showHints_properties());
		showTooltips.selectedProperty().bindBidirectional(conf.showTooltips_property());
		if (savingThumbs == null || conf.savingThumb_property() == null) {
			Messages.sprintfError("Something went wrooooont with options");
		}
		savingThumbs.selectedProperty().bindBidirectional(conf.savingThumb_property());
		betterQuality.selectedProperty().bindBidirectional(conf.betterQualityThumbs_property());
		workDir_input.textProperty().bindBidirectional(conf.workDir_property());
		vlcPath_input.textProperty().bindBidirectional(conf.vlcPath_propProperty());
	}

	@FXML
	private void vlcPath_choose_action(ActionEvent event) {
		FileChooser dc = new FileChooser();
		dc.getExtensionFilters().add(new FileChooser.ExtensionFilter("libvlc.dll", "libvlc.dll"));
		sprintf("Getting vlc path with filechooser");
		File fileChosen = dc.showOpenDialog(vlcPath_choose.getScene().getWindow());

		if (fileChosen != null) {
			boolean found = VLCJDiscovery.discovery(fileChosen.toPath());
			if (found) {
				vlcPath_input.setText(conf.getVlcPath());
				sprintf("vlcpath found: " + fileChosen);
			} else {
				warningText(bundle.getString("noValidVlcPathChosen"));
			}

		} else {
			warningText(bundle.getString("noValidVlcPathChosen"));
		}
	}

}
