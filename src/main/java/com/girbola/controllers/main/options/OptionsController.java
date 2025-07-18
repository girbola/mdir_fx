
package com.girbola.controllers.main.options;

import com.girbola.Main;
import com.girbola.controllers.main.sql.ConfigurationSQLHandler;
import com.girbola.configuration.VLCJDiscovery;
import com.girbola.messages.Messages;
import common.utils.OSHI_Utils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Files;

import static com.girbola.Main.bundle;
import static com.girbola.Main.conf;
import static com.girbola.messages.Messages.sprintf;
import static com.girbola.messages.Messages.warningText;

public class OptionsController {

   @FXML private Button chooseFolder_workDir;
   @FXML private CheckBox confirmOnExit;
   @FXML private CheckBox savingThumbs;
   @FXML private CheckBox betterQuality;
   @FXML private CheckBox showHints;
   @FXML private CheckBox showTooltips;
   @FXML private Button vlcPath_choose;
   @FXML private TextField vlcPath_input;
   @FXML private TextField workDir_input;

   @FXML private void chooseFolder_workDir_action(ActionEvent event) {
		Messages.sprintf("chooseFolder_workDir_action start");

		DirectoryChooser dc = new DirectoryChooser();
		File file = dc.showDialog(chooseFolder_workDir.getScene().getWindow());
		if (file != null) {
			if (Files.exists(file.toPath())) {

				Messages.sprintf("folder path is now: " + file.toString());
				Main.conf.setWorkDir(file.toString());

				Main.conf.setWorkDirSerialNumber(OSHI_Utils.getDriveSerialNumber((file.toPath().getRoot()).toString()));
				workDir_input.setText(Main.conf.getWorkDir());
				ConfigurationSQLHandler.updateConfiguration();

			} else {
				Messages.warningText("Can't find current path. Check you folder access");
			}
			/*
			 * TODO DriveInfo drive = new DriveInfo(aDrivePath, aDriveTotalSize, aConnected,
			 * aSelected, aIndentifier); workDir_input.setText(file.toString());
			 * conf.setWorkDir(file.toString()); testWritingToWorkdir. if it is possible
			 * then continue etc. Myös lisää DriveInfo tässä kohtaan ja tallenna FileSystem
			 * info jotta voi sitten testata myöhemmin kokoa. Esim jos laittaa uuden aseman
			 * niin se täsmää ja jos ei täsmää niin ilmoitus käyttäjälle? Testaa CD aseman
			 * HASHcode ja samoin tikun hashcode. Ehkä niitä voi hyödyntää etsimisessä?
			 */

		}
	}

	public void init() {
		sprintf("OptionsController init...");
		confirmOnExit.selectedProperty().bindBidirectional(conf.confirmOnExitProperty());
		showHints.selectedProperty().bindBidirectional(conf.showHintsProperties());
		showTooltips.selectedProperty().bindBidirectional(conf.showTooltipsProperty());
		if (savingThumbs == null || conf.savingThumbProperty() == null) {
			Messages.sprintfError("Something went wrooooont with options");
		}

		savingThumbs.selectedProperty().bindBidirectional(conf.savingThumbProperty());

		betterQuality.selectedProperty().bindBidirectional(conf.betterQualityThumbs_property());

		workDir_input.setText("" + conf.getWorkDir());

		vlcPath_input.textProperty().bindBidirectional(conf.vlcPath_property());
	}

   @FXML private void vlcPath_choose_action(ActionEvent event) {
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
