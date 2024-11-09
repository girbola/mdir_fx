package com.girbola.controllers.datefixer;

import com.girbola.*;
import com.girbola.controllers.datefixer.ObservableHandler.*;
import com.girbola.controllers.importimages.*;
import com.girbola.controllers.main.*;
import com.girbola.events.*;
import com.girbola.fileinfo.*;
import com.girbola.fxml.operate.*;
import com.girbola.messages.*;
import com.girbola.misc.*;
import common.utils.*;
import java.nio.file.*;
import java.util.*;
import javafx.beans.binding.*;
import javafx.event.*;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.stage.*;

public class AskEventDialogController {

    private final String ERROR = AskEventDialogController.class.getSimpleName();

    private ModelMain model_main;
    private Model_datefix model_dateFix;

    //@formatter:off
   @FXML private Button apply_and_copy_btn;
   @FXML private Button apply_btn;
   @FXML private Button cancel_btn;
   @FXML private ComboBox<String> event_cmb;
   @FXML private ComboBox<String> location_cmb;
   @FXML private ComboBox<String> user_cmb;
   @FXML private Label event_lbl;
   @FXML private Label location_lbl;
	//@formatter:on

    private List<FileInfo> applyChanges(String workDir) {
        List<FileInfo> list = new ArrayList<>();
        if (!event_cmb.getEditor().getText().isEmpty() || !location_cmb.getEditor().getText().isEmpty()) {
            for (Node selected_Node : model_dateFix.getSelectionModel().getSelectionList()) {
                FileInfo fileInfo = (FileInfo) selected_Node.getUserData();
                Messages.sprintf("selected_Node.getUserData Fileinfo: " + fileInfo);

                if (!event_cmb.getEditor().getText().equals(fileInfo.getEvent())) {
                    fileInfo.setEvent(event_cmb.getEditor().getText());
                    Main.setChanged(true);
                }
                if (!location_cmb.getEditor().getText().equals(fileInfo.getLocation())) {
                    fileInfo.setLocation(location_cmb.getEditor().getText());
                    Main.setChanged(true);
                }

                Path destPath = FileUtils.getFileNameDateWithEventAndLocation(fileInfo, workDir);
                list.add(fileInfo);
                if (!destPath.toString().equals(fileInfo.getDestination_Path())) {
                    fileInfo.setWorkDir(workDir);
                    fileInfo.setWorkDirDriveSerialNumber(Main.conf.getWorkDirSerialNumber());
                    fileInfo.setDestination_Path(destPath.toString());
                    fileInfo.setCopied(false);
                    model_dateFix.getFolderInfo_full().setChanged(true);
                    Main.setChanged(true);
                }
                Messages.sprintf("Destination path would be: " + fileInfo.getDestination_Path());
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
    private void apply_and_copy_btn_action(ActionEvent event) throws Exception {
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

      OperateFiles operateFiles = new OperateFiles(listOfApplyedChanges, true, model_main,
                SceneNameType.DATEFIXER.getType());
      operateFiles.init();

//        operateFiles.setOnSucceeded((workerStateEvent) -> {
//            Messages.sprintf("operateFiles Succeeded");
//            UpdateFolderInfoContent ufic = new UpdateFolderInfoContent(model_dateFix.getFolderInfo_full());
//        });
//        operateFiles.setOnCancelled((workerStateEvent) -> {
//            Messages.sprintf("operateFiles CANCELLED");
//        });
//        operateFiles.setOnFailed((workerStateEvent) -> {
//            Messages.sprintf("operateFiles FAILED");
//            Main.setProcessCancelled(true);
//        });
//        try {
//            if (!Files.exists(Paths.get(Main.conf.getWorkDir()).toRealPath())) {
//                Messages.warningText(Main.bundle.getString("cannotFindWorkDir"));
//                Messages.sprintfError(Main.bundle.getString("cannotFindWorkDir"));
//            } else {
//                Thread operateFiles_th = new Thread(operateFiles, "operateFiles_th");
//                operateFiles_th.setDaemon(true);
//                operateFiles_th.start();
//            }
//
//        } catch (IOException ex) {
//            ex.printStackTrace();
//            Messages.warningText_title(ex.getMessage(), Main.bundle.getString("cannotFindWorkDir"));
//            Messages.errorSmth(ERROR, ex.getMessage(), ex, Misc.getLineNumber(), false);
//        }

        Messages.sprintf("OperateFiles instance ended?");
    }

    @FXML
    private void cancel_btn_action(ActionEvent event) {
        Messages.sprintf("Cancel pressed");
        Stage stage = (Stage) cancel_btn.getScene().getWindow();
        stage.close();

    }

    public void init(ModelMain aModel_main, Model_datefix model_dateFix) {
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
