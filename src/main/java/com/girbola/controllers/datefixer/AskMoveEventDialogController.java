package com.girbola.controllers.datefixer;

import com.girbola.Main;
import com.girbola.SceneNameType;
import com.girbola.controllers.importimages.AutoCompleteComboBoxListener;
import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.sql.WorkDirSQL;
import com.girbola.controllers.operate.OperateFiles;
import com.girbola.events.GUI_Events;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import java.awt.event.ActionEvent;
import java.nio.file.Paths;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class AskMoveEventDialogController {

    private final String ERROR = AskEventDialogController.class.getSimpleName();

    private ModelMain model_main;
    private ModelDatefix model_dateFix;

    //@formatter:off
    @FXML private Button move_and_copy_btn;
    @FXML private Button move_btn;
    @FXML private Button cancel_btn;
    @FXML private ComboBox<String> event_cmb;
    @FXML private ComboBox<String> location_cmb;
    @FXML private ComboBox<String> user_cmb;
    @FXML private Label event_lbl;
    @FXML private Label location_lbl;
    //@formatter:on

    public void init(ModelMain aModel_main, ModelDatefix model_dateFix) {
        this.model_main = aModel_main;
        this.model_dateFix = model_dateFix;

        move_and_copy_btn.disableProperty().bind(Bindings.isEmpty(event_cmb.getEditor().textProperty())
                .and(Bindings.isEmpty(location_cmb.getEditor().textProperty())));
        // model_dateFix.getWorkDir_obs();

        for (Node n : model_dateFix.getSelectionModel().getSelectionList()) {
            FileInfo fileInfo = (FileInfo) n.getUserData();
            if (!fileInfo.getEvent().isEmpty()) {
                model_dateFix.getObservableHandler().addIfExists(ObservableHandler.ObservabeleListType.EVENT.getType(),
                        fileInfo.getEvent());
            }
            if (!fileInfo.getLocation().isEmpty()) {
                model_dateFix.getObservableHandler().addIfExists(ObservableHandler.ObservabeleListType.LOCATION.getType(),
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

        WorkDirSQL workDirSQL = new WorkDirSQL(Paths.get(Main.conf.getWorkDir()));
//        List<String> filesInfoList = workDirSQL.findDuplicatesByDateRange();

    }

    @FXML
    public void move_btn_action(ActionEvent event) {
        Messages.sprintf("move_btn_action started");
        if (move_btn == null) {
            Messages.errorSmth(ERROR, "move_btn were null!", null, Misc.getLineNumber(), true);
        }
        model_main.getSelectedFolders().getSelectedFolderScanner_obs();
//     //   applyChanges(Main.conf.getWorkDir());
//        Stage stage = (Stage) move_btn.getScene().getWindow();
//        stage.close();
    }

    @FXML
    public void move_and_copy_btn_action(ActionEvent event) {
        Messages.sprintf("move_and_copy_btn_action started");
        if (move_and_copy_btn == null) {
            Messages.errorSmth(ERROR, "ok_btn were null!", null, Misc.getLineNumber(), true);
        }
//        List<FileInfo> listOfApplyedChanges = applyChanges(Main.conf.getWorkDir());
//        if (listOfApplyedChanges.isEmpty()) {
//            Messages.errorSmth(ERROR, "No files were selected", null, Misc.getLineNumber(), true);
//            return;
//        }
//        Messages.sprintf("starting opening scene");
//
//        Scene scene = move_and_copy_btn.getScene();
//        Stage askStage = (Stage) scene.getWindow();
//        // askStage.setAlwaysOnTop(true);
//        askStage.centerOnScreen();
//        askStage.close();
//
//        OperateFiles operateFiles = new OperateFiles(listOfApplyedChanges, true, model_main,
//                SceneNameType.DATEFIXER.getType());
//        operateFiles.init();
    }

    @FXML
    public void cancel_btn_action(ActionEvent event) {
        Messages.sprintf("Cancel pressed");
        Stage stage = (Stage) cancel_btn.getScene().getWindow();
        stage.close();
    }


}
