package com.girbola.controllers.operate;

import com.girbola.*;
import com.girbola.controllers.main.*;
import com.girbola.controllers.main.sql.WorkDirSQL;
import com.girbola.fileinfo.*;
import com.girbola.messages.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import javafx.application.*;
import javafx.concurrent.*;
import javafx.fxml.*;
import javafx.scene.*;

public class OperateFiles {

    private final String ERROR = OperateFiles.class.getSimpleName();

    //    private WorkDirSQL workDirSQL;
    private boolean close;
    private ModelOperate modelOperate = new ModelOperate();
    private List<FileInfo> list;
    private ModelMain modelMain;
    private String sceneNameType;

    public OperateFiles(List<FileInfo> list, boolean close, ModelMain modelMain, String sceneNameType) {
        Messages.sprintf("OperateFiles starting...");
        this.list = list;
        this.close = close;
        this.modelMain = modelMain;
        this.sceneNameType = sceneNameType;
        try {
            init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void init() {
        Main.setProcessCancelled(false);

        try {
            Path workDir = Paths.get(Main.conf.getWorkDir());

            if (!Files.exists(workDir.toRealPath())) {
                Messages.warningText(Main.bundle.getString("cannotFindWorkDir"));
                return;
            }

            Parent parent = null;
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/operate/OperateDialog.fxml"), Main.bundle);
            parent = loader.load();

            OperateDialogController operateDialogController = (OperateDialogController) loader.getController();
            operateDialogController.init(modelOperate);

            Scene operateScene = new Scene(parent);
            operateScene.getStylesheets()
                    .add(Main.class.getResource(Main.conf.getThemePath() + MDir_Stylesheets_Constants.MAINSTYLE.getType()).toExternalForm());
            Platform.runLater(() -> {
                Main.sceneManager.getWindow().setScene(operateScene);
            });

            Main.sceneManager.getWindow().setOnCloseRequest(event -> {
                ModelMain model_Main = (ModelMain) Main.getMain_stage().getUserData();
                Main.sceneManager.getWindow().setScene(Main.sceneManager.getScene_main());
                Main.getMain_stage().setOnCloseRequest(model_Main.exitProgram);
                event.consume();
            });

            initButtons();
        } catch (IOException ex) {
            Messages.warningText_title(ex.getMessage(), Main.bundle.getString("cannotFindWorkDir"));
        }

    }

    /**
     * Initializes the start and cancel buttons for the file copying process based on the current state of the file list.
     * <p>
     * If the file list is not empty, the start button is enabled and its action is set up to start the copy process.
     * The copy process involves setting up a Copy task and handling its events such as succeeded, failed, and cancelled states.
     * Additionally, it performs necessary checks for the initial state of the model and connection to the working directory.
     * <p>
     * If the file list is empty, a warning message is displayed, and the method execution is halted.
     * <p>
     * The cancel button is also configured to handle the cancellation of the current file copying process,
     * setting the process as cancelled and performing necessary clean-up actions.
     */
    private void initButtons() {
        Messages.sprintf("stage is showing");
        if (!list.isEmpty()) {
            Platform.runLater(() -> {
                modelOperate.getStart_btn().setDisable(false);
            });

        } else {
            Messages.warningText("List were empty!");
            return;
        }
        modelOperate.getCopyProcess_values().setTotalFiles(String.valueOf(list.size()));



        long totalSize = 0;
        for (FileInfo fileInfo : list) {
            totalSize += fileInfo.getSize();
        }

        Platform.runLater(() -> {

            modelOperate.getStart_btn().setOnAction(event -> {
                if (modelMain == null) {
                    Messages.sprintfError("model main is null");
                }
                if (modelMain.getMonitorExternalDriveConnectivity() == null) {
                    Messages.sprintfError("model main getMonitorExternalDriveConnectivity is null");
                }
                modelMain.getMonitorExternalDriveConnectivity().cancel();

                WorkDirSQL.loadWorkDir();

/*
if(sortedTable) {
2024-01-01 - 2024-01-02 ratio event place
 find duplicates in the workdir and get folder path
 - if sortedTable set the destination by date range
 - if sortItTable find destination by date range
}
 - find destination by date range 2010/01/01 - 2010/01/01 +1 day ratio
 - if sortedTable set the destination by date range
 - if sortItTable find destination by date range
}

find duplicates in the workdir
 - if duplicates found, set the fileInfo state accordingly
find destination by date range 2010/01/01 - 2010/01/01 +1 day ratio
 - if sortedTable set the destination by date range
 - if sortItTable find destination by date range



 */

                for (FileInfo fileInfo : list) {
                    CopyState duplicates = WorkDirSQL.findDuplicates(fileInfo);
                }

                Task<Integer> copy = new Copy(list, modelOperate, modelMain, sceneNameType, close);
                copy.setOnSucceeded((WorkerStateEvent eventWorker) -> Messages.sprintf("copy succeeded"));
                copy.setOnFailed((WorkerStateEvent eventWorker) -> Messages.sprintf("copy failed"));
                copy.setOnCancelled((WorkerStateEvent eventWorker) -> {
                    modelOperate.getCancel_btn().setText(Main.bundle.getString("close"));
                    modelOperate.doneButton(sceneNameType, close);
                    Messages.sprintf("copy cancelled");
                });

                Thread copyThread = new Thread(copy, "Copy Thread");
                copyThread.start();

            });
            modelOperate.getCancel_btn().setOnAction(event -> {
                Main.setProcessCancelled(true);
                Messages.sprintf("Current file cancelled is: " + modelOperate.getCopyProcess_values().getCopyTo());
                modelOperate.stopTimeLine();
                Main.setProcessCancelled(true);
            });
        });

        Platform.runLater(() -> {
            modelOperate.getStart_btn().setDisable(false);
        });
    }
}
