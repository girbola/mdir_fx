/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.datefixer;

import com.girbola.MDir_Constants;
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
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

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
        Messages.sprintf("tabletype = " + this.folderInfo.getTableType());
        this.currentPath = aCurrentPath;
        this.model_main = aModel_main;
        this.isImported = isImported;
        model_datefix = new Model_datefix(model_main, currentPath);
    }

    @Override
    protected Void call() throws Exception {
        sprintf("loading datefix fxmlloader");
        try {

            FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/datefixer/DateFix.fxml"), bundle);
            try {
                parent = loader.load();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            dateFixerController = (DateFixerController) loader.getController();
            Scene scene_dateFixer = new Scene(parent, (conf.getScreenBounds().getWidth()),
                    (conf.getScreenBounds().getHeight() - 50));
            dateFixerController.init(model_datefix, model_main, currentPath, folderInfo, isImported);

            scene_dateFixer.setOnKeyPressed(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent event) {
                    if (event.getCode() == KeyCode.ENTER && event.isAltDown()) {
                        sprintf("Alt + ENTER pressed");
                        Stage stage = (Stage) Main.scene_Switcher.getScene_dateFixer().getWindow();
                        stage.setFullScreen(true);
                    }
                }
            });
            scene_dateFixer.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (event.getTarget() instanceof StackPane) {
                        sprintf("event StackPane target: " + event.getTarget());
                        StackPane node = (StackPane) event.getTarget();
                        if (node.getParent() instanceof VBox && node.lookupAll("#imageView") != null) {
                            model_datefix.getSelectionModel().addWithToggle(node.getParent());
                            if (event.getClickCount() == 2) {
                                FileInfo fileInfo = (FileInfo) node.getParent().getUserData();
                                sprintf("fileInfo: " + fileInfo.toString());
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
                }
            });

            sprintf("conf.getThemePath(): " + conf.getThemePath());
            scene_dateFixer.getStylesheets().add(
                    Main.class.getResource(conf.getThemePath() + MDir_Constants.DATEFIXER.getType()).toExternalForm());

            Platform.runLater(() -> {
                Main.scene_Switcher.setScene_dateFixer(scene_dateFixer);
                Main.scene_Switcher.getWindow().setScene(Main.scene_Switcher.getScene_dateFixer());
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
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
// Check if files are already in destination

        Task<Void> dateFixPopulateGridPane_task = new DateFixPopulateQuickPick(Main.scene_Switcher.getScene_dateFixer(),
                model_datefix, model_datefix.getTilePane(), loadingProcess_task);
        dateFixPopulateGridPane_task.setOnCancelled(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                sprintf("dateFixPopulateGridPane.setOnCancelled");
                loadingProcess_task.closeStage();
            }
        });
        dateFixPopulateGridPane_task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                LoadingProcessLoader.runUpdateTask(model_datefix);
            }
        });
        dateFixPopulateGridPane_task.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                sprintf("dateFixPopulateGridPane.setOnFailed");
                loadingProcess_task.closeStage();
            }
        });

        loadingProcess_task.setTask(dateFixPopulateGridPane_task);
        Thread dateFixPopulateGridPane_th = new Thread(dateFixPopulateGridPane_task, "dateFixPopulateGridPane_th");
        dateFixPopulateGridPane_th.start();

    }

    /**
     * @param fileInfo
     * @return
     */
    public String getStatus(FileInfo fileInfo) {
        if (fileInfo.isBad()) {
            return DateFixPopulateQuickPick.DATE_STATUS.DATE_BAD.getType();
        } else if (fileInfo.isGood()) {
            return DateFixPopulateQuickPick.DATE_STATUS.DATE_GOOD.getType();
        } else if (fileInfo.isSuggested()) {
            return DateFixPopulateQuickPick.DATE_STATUS.DATE_SUGGESTED.getType();
        } else if (fileInfo.isVideo()) {
            return DateFixPopulateQuickPick.DATE_STATUS.DATE_VIDEO.getType();
        }
        return null;
    }

}
