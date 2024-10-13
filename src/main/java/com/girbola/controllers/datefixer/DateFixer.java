/*
 @(#)Copyright:  Copyright (c) 2012-2024 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.datefixer;

import com.girbola.MDir_Stylesheets_Constants;
import com.girbola.Main;
import com.girbola.controllers.loading.LoadingProcessTask;
import com.girbola.controllers.main.Model_main;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import common.utils.FileUtils;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

import static com.girbola.Main.bundle;
import static com.girbola.Main.conf;
import static com.girbola.messages.Messages.sprintf;

public class DateFixer extends Task<Void> {

    private final String ERROR = DateFixer.class.getSimpleName();

    private Path currentPath;
    private FolderInfo folderInfo;
    private Model_main model_main;
    private Model_datefix model_datefix;
    private Parent parent = null;
    private DateFixerController dateFixerController = null;
    private boolean isImported;

    public DateFixer(Path aCurrentPath, FolderInfo aFolderInfo, Model_main aModel_main, boolean isImported) {
        this.folderInfo = aFolderInfo;
        this.currentPath = aCurrentPath;
        this.model_main = aModel_main;
        this.isImported = isImported;
        this.model_datefix = new Model_datefix(model_main, currentPath);
    }

    @Override
    protected Void call() throws Exception {
        sprintf("loading datefix fxmlloader");
        Platform.runLater(() -> {
            try {

                FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/datefixer/DateFix.fxml"), bundle);
                parent = loader.load();

                dateFixerController = loader.getController();
                Scene scene_dateFixer = new Scene(parent, (Misc.getScreenBounds().getWidth()),
                        (Misc.getScreenBounds().getHeight() - 50));
                dateFixerController.init(model_datefix, model_main, currentPath, folderInfo, isImported);

                scene_dateFixer.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.ENTER && event.isAltDown()) {
                        Stage stage = (Stage) Main.scene_Switcher.getScene_dateFixer().getWindow();
                        stage.setFullScreen(true);
                    }
                });

                scene_dateFixer.setOnMouseClicked(event -> {
                    Messages.sprintf("scene_dateFixer mouse clicked event.getTarget(): " + event.getTarget());
                    if (event.getTarget() instanceof VBox node) {
                        if (node.getParent() instanceof HBox && node.lookupAll("#imageView") != null) {
                            model_datefix.getSelectionModel().addWithToggle(node);
                            if (event.getClickCount() == 2) {
                                FileInfo fileInfo = (FileInfo) node.getParent().getUserData();
                                if (FileUtils.supportedImage(Paths.get(fileInfo.getOrgPath()))
                                        || FileUtils.supportedRaw(Paths.get(fileInfo.getOrgPath()))) {
                                    ImageUtils.view(model_datefix.getFolderInfo_full().getFileInfoList(), fileInfo,
                                            Main.scene_Switcher.getScene_dateFixer().getWindow());
                                } else if (FileUtils.supportedVideo(Paths.get(fileInfo.getOrgPath()))) {
                                    ImageUtils.playVideo(Paths.get(fileInfo.getOrgPath()), node);
                                }
                            }
                        }
                    }
                });

                sprintf("conf.getThemePath(): " + conf.getThemePath());
                scene_dateFixer.getStylesheets().add(
                        Main.class.getResource(conf.getThemePath() + MDir_Stylesheets_Constants.DATEFIXER.getType()).toExternalForm());

                Main.scene_Switcher.setScene_dateFixer(scene_dateFixer);
                Main.scene_Switcher.getWindow().setScene(Main.scene_Switcher.getScene_dateFixer());

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        Thread.sleep(1000);
        return null;
    }

    @Override
    protected void failed() {
        Messages.errorSmth(ERROR, "DateFixer Failed!", null, Misc.getLineNumber(), true);
    }

    @Override
    protected void cancelled() {
        Messages.sprintf("DateFix cancelled");
    }

    @Override
    protected void succeeded() {
        sprintf("dateFixLoader.setOnSucceeded");

        LoadingProcessTask loadingProcess_task = new LoadingProcessTask(Main.scene_Switcher.getWindow());

        Task<ObservableList<Node>> dateFixPopulateTask = new DateFixPopulateQuickPick(Main.scene_Switcher.getScene_dateFixer(),
                model_datefix, model_datefix.getTilePane(), loadingProcess_task);
        dateFixPopulateTask.setOnCancelled(event -> {
            sprintf("dateFixPopulateGridPane.setOnCancelled");
            loadingProcess_task.closeStage();
        });
        dateFixPopulateTask.setOnSucceeded(event -> {
            sprintf("dateFixPopulateGridPane.setOnSucceeded");

            try {
                ObservableList<Node> nodes = dateFixPopulateTask.get();
                Platform.runLater(() -> {
                    model_datefix.getTilePane().getChildren().addAll(nodes);
                    model_datefix.setAllNodes(nodes);
                    loadingProcess_task.closeStage();
                });
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }

//            model_datefix.getTilePane().getChildren().addAll(model_datefix.getAllNodes());
            //DateFixLoadingProcessLoader.runUpdateTask(model_datefix, loadingProcess_task);
//            DateFixLoadingProcessLoader.reNumberTheFrames(model_datefix, loadingProcess_task);
        });
        dateFixPopulateTask.setOnFailed(event -> {
            sprintf("dateFixPopulateGridPane.setOnFailed");
            loadingProcess_task.closeStage();
        });

        loadingProcess_task.setTask(dateFixPopulateTask);
        Thread dateFixPopulateGridPane_th = new Thread(dateFixPopulateTask, "dateFixPopulateTask_thread");
        dateFixPopulateGridPane_th.start();
    }
}
