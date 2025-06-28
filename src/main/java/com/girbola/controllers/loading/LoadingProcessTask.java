/*
 @(#)Copyright:  Copyright (c) 2012-2025 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.loading;

import com.girbola.MDir_Stylesheets_Constants;
import com.girbola.Main;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import java.io.File;
import java.net.URL;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.girbola.Main.bundle;
import static com.girbola.Main.conf;
import static com.girbola.messages.Messages.sprintf;
import static com.girbola.messages.Messages.sprintfError;

public class LoadingProcessTask {

    private final String ERROR = LoadingProcessTask.class.getSimpleName();
    private ModelLoading modelLoading = new ModelLoading();
    private double xOffset;
    private double yOffset;
    private Parent parent = null;
    private Window owner;

    private Scene loadingScene;
    private Stage loadingStage;

    public LoadingProcessTask(Window owner) {
        this.owner = owner;
        loadGUI();

    }

    /*
     * private Stage loadingStage; private Scene loadingScene;
     */
    public void setProgressBar(double progress) {
        modelLoading.getProgressBar().setProgress(progress);
    }

    public void setTask(Task<?> current_Task) {
        if (modelLoading == null) {
            Messages.sprintfError("ModelLoading is not initialized");
            return;
        }

        if (current_Task == null) {
            Messages.sprintf("LoadingProcess_Task Task were set to null!!");
            return;
        }

//        Platform.runLater(() -> {
//            try {
//                modelLoading.getProgressBar().setProgress(ProgressBar.INDETERMINATE_PROGRESS);
//                if (!Main.getProcessCancelled()) {
//                    if (Main.scene_Switcher.getWindow_loadingprogress() != null
//                            && Main.scene_Switcher.getWindow_loadingprogress().isShowing()) {
//                        modelLoading.setTask(current_Task);
//                        bind();
//                    } else {
//                        modelLoading.setTask(current_Task);
//                        bind();
//                        loadGUI();
//                    }
//                } else {
//                    closeStage();
//                }
//            } catch (Exception ex) {
//                Messages.sprintfError("Error setting task: " + ex.getMessage());
//
//                Logger.getLogger(LoadingProcessTask.class.getName()).log(Level.SEVERE, null, ex);
//                Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
//            }
//            if (Main.getProcessCancelled()) {}
//        });
    }

    public void loadGUI() {
        Platform.runLater(() -> {
            FXMLLoader loader = null;
//            URL fxmlLocation = getClass().getResource("/fxml/loading/LoadingProcess.fxml");

      /*      if(bundle == null) {
                Messages.sprintfError("Bundle was null");
                Platform.exit();
            }*/
         /*URL fxmlLocation = getClass().getResource("fxml/loading/LoadingProcess.fxml");*/
            /*URL fxmlLocation = getClass().getResource("../../fxml/");*/
            /*URL fxmlLocation = getClass().getResource("/fxml/loading/LoadingProcess.fxml");*/
//            URL fxmlLocation = Main.class.getResource("/com/girbola/fxml/loading/LoadingProcess.fxml");
            URL fxmlLocation = Main.class.getResource("/com/girbola/fxml/loading/LoadingProcess.fxml");
           /* File file = new File(fxmlLocation.getFile());
            for(File f : file.listFiles()) {
                Messages.sprintf("====File: " + f.getAbsolutePath());
            }*/
            Messages.sprintf("fxmlLocation: " + fxmlLocation);
            if(fxmlLocation == null) {
                Messages.sprintfError("FXML resource not found: " + fxmlLocation);
                Platform.exit();
                return;
            }

            /*FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/loading/LoadingProcess.fxml"), bundle);*/


            try {
                loader = new FXMLLoader(fxmlLocation, bundle);
                parent = loader.load();
            } catch (IOException ex) {
                Logger.getLogger(LoadingProcessTask.class.getName()).log(Level.SEVERE, null, ex);
                Messages.errorSmth(ERROR, "Failed to load FXML", ex, Misc.getLineNumber(), true);
                return;
            }

            LoadingProcessController lpc = (LoadingProcessController) loader.getController();
            lpc.init(modelLoading);
            loadingScene = new Scene(parent);
            loadingStage = new Stage();
            loadingStage.setWidth(500);
            loadingStage.setMinWidth(500);
            loadingStage.setMaxWidth(500);
            loadingStage.setHeight(400);
            loadingStage.setMinHeight(400);
            loadingStage.setMaxHeight(400);

            if (owner != null) {
                loadingStage.initOwner(owner);
            }
//			loadingStage.initStyle(StageStyle.UNDECORATED);
            Messages.sprintf("Owner is: " + loadingStage.getOwner());
//		loadingStage.setX(Main.conf.getWindowStartPosX());
            loadingStage.setTitle("loadingprocess_task: " + Main.conf.getWindowStartPosX());
            loadingScene.getStylesheets()
                    .add(getClass().getResource(conf.getThemePath() + MDir_Stylesheets_Constants.LOADINGPROCESS.getType()).toExternalForm());

            xOffset = loadingStage.getX();
            yOffset = loadingStage.getY();

            Main.centerWindowDialog(loadingStage);
            loadingScene.setOnMousePressed(event -> {
                xOffset = (loadingStage.getX() - event.getScreenX());
                yOffset = (loadingStage.getY() - event.getScreenY());
                sprintf("yOffset: " + yOffset);
            });

            loadingScene.setOnMouseDragged(event -> {
                loadingStage.setX(event.getScreenX() + xOffset);
                if (event.getScreenY() <= 0) {
                    loadingStage.setY(0);
                } else {
                    loadingStage.setY(event.getScreenY() + yOffset);
                }

                sprintf("event.getScreenY(); = " + event.getScreenY());
            });

            loadingStage.setScene(loadingScene);
            loadingStage.setAlwaysOnTop(true);

            loadingStage.show();
            Main.scene_Switcher.setWindow_loadingprogress(loadingStage);
            Main.scene_Switcher.setScene_loading(loadingScene);
        });

    }

    private void unbind() {
        if (modelLoading.getTask() != null) {
            modelLoading.getProgressBar().progressProperty().unbind();
            modelLoading.getMessages_lbl().textProperty().unbind();
        }
    }

    private void bind() {
        if (modelLoading.getTask() != null && modelLoading.getProgressBar() != null) {
            Platform.runLater(() -> {
                modelLoading.getProgressBar().progressProperty()
                        .bind(modelLoading.getTask().progressProperty());
                modelLoading.getMessages_lbl().textProperty()
                        .bind(modelLoading.getTask().messageProperty());
            });
        } else {
            sprintf("task or progress bar were null in BIND()");
        }
    }
    
    public void closeStage() {
        Messages.sprintf("closeStage is closing window");

        stopTask();
        unbind();

        Platform.runLater(() -> {
            if (Main.scene_Switcher.getScene_loading() != null &&
                    Main.scene_Switcher.getScene_loading().getRoot() != null &&
                    Main.scene_Switcher.getWindow_loadingprogress() != null) {

                Timeline timeline = new Timeline();
                KeyFrame key = new KeyFrame(Duration.millis(2000),
                        new KeyValue(Main.scene_Switcher.getScene_loading().getRoot().opacityProperty(), 0));
                timeline.getKeyFrames().add(key);
                timeline.setOnFinished(event -> Main.scene_Switcher.getWindow_loadingprogress().close());
                timeline.play();
            } else {
                // Fallback if scene or root is null
                if (Main.scene_Switcher.getWindow_loadingprogress() != null) {
                    Main.scene_Switcher.getWindow_loadingprogress().close();
                }
            }
        });
    }

    private void stopTask() {
        if (modelLoading.getTask() != null) {
            if (modelLoading.getTask().isRunning()) {
                modelLoading.getTask().cancel();
            }
        }
    }

    public void showLoadStage() {
        if (Main.scene_Switcher.getWindow_loadingprogress().isShowing()) {
            Messages.sprintf("Window is already showing!!");
            return;
        }
        if (modelLoading.getTask() == null) {
            Messages.sprintf("Task were null!");
            Platform.runLater(() -> {
                modelLoading.getProgressBar().setProgress(ProgressBar.INDETERMINATE_PROGRESS);
            });
        }

        Messages.sprintf("Showing stage!");

        Stage stage = Main.scene_Switcher.getWindow_loadingprogress();
        /*
         * if (owner != null) { stage.initOwner(owner); }
         */
        if (stage != null) {

            stage.show();

        } else {
            Messages.errorSmth(ERROR, "Loading scene haven't been initialisiz. It was null null!!!", null,
                    Misc.getLineNumber(), true);
        }

    }

    public void setMessage(String message) {
        Messages.sprintf("LoadingProcess_Task message= " + message);
        if (Main.scene_Switcher.getWindow_loadingprogress() != null) {
            if (Main.scene_Switcher.getWindow_loadingprogress().isShowing()) {
                Platform.runLater(() -> {
                    modelLoading.getMessages_lbl().setText(message);
                });
            }
        }
    }

}
