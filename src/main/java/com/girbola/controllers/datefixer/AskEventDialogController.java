package com.girbola.controllers.datefixer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.girbola.Main;
import com.girbola.Scene_NameType;
import com.girbola.controllers.datefixer.ObservableHandler.ObservabeleListType;
import com.girbola.controllers.importimages.AutoCompleteComboBoxListener;
import com.girbola.controllers.main.Model_main;
import com.girbola.controllers.main.UpdateFolderInfoContent;
import com.girbola.events.GUI_Events;
import com.girbola.fileinfo.FileInfo;
import com.girbola.fxml.operate.OperateFiles;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;

import common.utils.FileUtils;
import common.utils.date.DateUtils;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class AskEventDialogController {

	private final String ERROR = AskEventDialogController.class.getSimpleName();

	private Model_main model_main;
	private Model_datefix model_dateFix;

	@FXML
	private Label event_lbl;
	@FXML
	private ComboBox<String> event_cmb;
	@FXML
	private Label location_lbl;
	@FXML
	private ComboBox<String> location_cmb;
	@FXML
	private ComboBox<String> user_cmb;

	@FXML
	private Button apply_btn;
	@FXML
	private Button apply_and_copy_btn;
	@FXML
	private Button cancel_btn;

	private List<FileInfo> applyChanges(String workDir) {
		List<FileInfo> list = new ArrayList<>();
		if (!event_cmb.getEditor().getText().isEmpty() || !location_cmb.getEditor().getText().isEmpty()) {
			String location_str = "";
			String event_str = "";
			for (Node n : model_dateFix.getSelectionModel().getSelectionList()) {
				FileInfo fileInfo = (FileInfo) n.getUserData();
				Messages.sprintf("222Fileinfo: " + fileInfo);

				if (!event_cmb.getEditor().getText().equals(fileInfo.getEvent())) {
					fileInfo.setEvent(event_cmb.getEditor().getText());
					Main.setChanged(true);
				}
				if (!location_cmb.getEditor().getText().equals(fileInfo.getLocation())) {
					fileInfo.setLocation(location_cmb.getEditor().getText());
					Main.setChanged(true);
				}

				if (fileInfo.getEvent().isEmpty() && !fileInfo.getLocation().isEmpty()) {
					location_str = " - " + fileInfo.getLocation();
				} else if (!fileInfo.getEvent().isEmpty() && fileInfo.getLocation().isEmpty()) {
					event_str = " - " + fileInfo.getEvent();
				} else {
					location_str = " - " + fileInfo.getLocation();
					event_str = " - " + fileInfo.getEvent();
				}

				LocalDate ld = DateUtils.longToLocalDateTime(fileInfo.getDate()).toLocalDate();
				Messages.sprintf("location were= '" + location_str + "'");
				Messages.sprintf("evemt were= '" + event_str + "'");
				list.add(fileInfo);
				// I:\\2017\\2017-06-23 Merikarvia - Kalassa äijien kanssa
				// I:\\2017\\2017-06-24 Merikarvia - Kalassa äijien kanssa
				String fileName = DateUtils.longToLocalDateTime(fileInfo.getDate())
						.format(Main.simpleDates.getDtf_ymd_hms_minusDots_default());
				Path destPath = Paths.get(
						File.separator + ld.getYear() + File.separator + ld + location_str + event_str + File.separator
								+ fileName + "." + FileUtils.getFileExtension(Paths.get(fileInfo.getOrgPath())));
				if (!destPath.toString().equals(fileInfo.getDestination_Path())) {
					fileInfo.setWorkDir(workDir);
					fileInfo.setWorkDirDriveSerialNumber(Main.conf.getWorkDirSerialNumber());
					fileInfo.setDestination_Path(destPath.toString());
					fileInfo.setCopied(false);
					Main.setChanged(true);
				}
				Messages.sprintf("Destination path would be: " + fileInfo.getDestination_Path());
				location_str = "";
				event_str = "";
			}
		}
		return list;
	}

	@FXML
	private void apply_btn_action(ActionEvent event) {
		Messages.sprintf("apply_btn_action started");
		if (apply_and_copy_btn == null) {
			Messages.errorSmth(ERROR, "ok_btn were null!", null, Misc.getLineNumber(), true);
		}
		applyChanges(Main.conf.getWorkDir());
		Stage stage = (Stage) apply_btn.getScene().getWindow();
		stage.close();

	}

	@FXML
	private void apply_and_copy_btn_action(ActionEvent event) {
		Messages.sprintf("apply_and_copy_btn_action started");
		if (apply_and_copy_btn == null) {
			Messages.errorSmth(ERROR, "ok_btn were null!", null, Misc.getLineNumber(), true);
		}
		List<FileInfo> listOfApplyedChanges = applyChanges(Main.conf.getWorkDir());
		if (listOfApplyedChanges.isEmpty()) {
			Messages.errorSmth(ERROR, "No files were selected", null, Misc.getLineNumber(), true);
			return;
		}
		Messages.sprintf("starting opening scene");

		Scene scene = apply_and_copy_btn.getScene();
		Stage askStage = (Stage) scene.getWindow();
		// askStage.setAlwaysOnTop(true);
		askStage.centerOnScreen();
		askStage.close();

		Task<Boolean> operateFiles = new OperateFiles(listOfApplyedChanges, true, model_main,
				Scene_NameType.DATEFIXER.getType());
		operateFiles.setOnSucceeded((workerStateEvent) -> {
			Messages.sprintf("operateFiles Succeeded");
			UpdateFolderInfoContent ufic = new UpdateFolderInfoContent(model_dateFix.getFolderInfo_full());
		});
		operateFiles.setOnCancelled((workerStateEvent) -> {
			Messages.sprintf("operateFiles CANCELLED");
		});
		operateFiles.setOnFailed((workerStateEvent) -> {
			Messages.sprintf("operateFiles FAILED");
			Main.setProcessCancelled(true);
		});
		try {
			if (!Files.exists(Paths.get(Main.conf.getWorkDir()).toRealPath())) {
				Messages.warningText(Main.bundle.getString("cannotFindWorkDir"));
				Messages.sprintfError(Main.bundle.getString("cannotFindWorkDir"));
			} else {
				Thread operateFiles_th = new Thread(operateFiles, "operateFiles_th");
				operateFiles_th.setDaemon(true);
				operateFiles_th.start();
			}

		} catch (IOException ex) {
			ex.printStackTrace();
			Messages.warningText_title(ex.getMessage(), Main.bundle.getString("cannotFindWorkDir"));
			Messages.errorSmth(ERROR, ex.getMessage(), ex, Misc.getLineNumber(), false);
		}

		Messages.sprintf("OperateFiles instance ended?");
	}

	@FXML
	private void cancel_btn_action(ActionEvent event) {
		Messages.sprintf("Cancel pressed");
		Stage stage = (Stage) cancel_btn.getScene().getWindow();
		stage.close();

	}

	public void init(Model_main aModel_main, Model_datefix model_dateFix) {
		this.model_main = aModel_main;
		this.model_dateFix = model_dateFix;

		apply_and_copy_btn.disableProperty().bind(Bindings.isEmpty(event_cmb.getEditor().textProperty())
				.and(Bindings.isEmpty(location_cmb.getEditor().textProperty())));
		// model_dateFix.getWorkDir_obs();

		for (Node n : model_dateFix.getSelectionModel().getSelectionList()) {
			FileInfo fileInfo = (FileInfo) n.getUserData();
			if (!fileInfo.getEvent().isEmpty()) {
				model_dateFix.getObservableHandler().addIfExists(ObservabeleListType.EVENT.getType(),
						fileInfo.getEvent());
			}
			if (!fileInfo.getLocation().isEmpty()) {
				model_dateFix.getObservableHandler().addIfExists(ObservabeleListType.LOCATION.getType(),
						fileInfo.getLocation());
			}
		}
		new AutoCompleteComboBoxListener<>(location_cmb);
		GUI_Events.textField_file_listener(location_cmb.getEditor());
		new AutoCompleteComboBoxListener<>(event_cmb);
		GUI_Events.textField_file_listener(event_cmb.getEditor());
		new AutoCompleteComboBoxListener<>(user_cmb);
		GUI_Events.textField_file_listener(user_cmb.getEditor());

		event_cmb.setItems(model_dateFix.getObservableHandler().getEvent_obs());
		user_cmb.setItems(model_dateFix.getObservableHandler().getUser_obs());

	}

}
