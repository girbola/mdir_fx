/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.loading;

import com.girbola.Main;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseEvent;
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
		Platform.runLater(() -> {
			modelLoading.getProgressBar().setProgress(ProgressBar.INDETERMINATE_PROGRESS);
		});
		if (current_Task == null) {
			Messages.sprintf("LoadingProcess_Task Task were set to null!!");
//			return;
		} else {
			if (!Main.getProcessCancelled()) {
				if (Main.scene_Switcher.getWindow_loadingprogress() != null) {
					if (Main.scene_Switcher.getWindow_loadingprogress().isShowing()) {
						modelLoading.setTask(current_Task);
						bind();
					}
				} else {
					modelLoading.setTask(current_Task);
					bind();
					loadGUI();
				}
			} else {
				closeStage();
			}
		}
	}

	public void loadGUI() {
		Platform.runLater(() -> {
			FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/loading/LoadingProcess.fxml"), bundle);
			try {
				parent = loader.load();

			} catch (IOException ex) {
				Logger.getLogger(LoadingProcessTask.class.getName()).log(Level.SEVERE, null, ex);
				Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
			}
			LoadingProcessController lpc = (LoadingProcessController) loader.getController();
			lpc.init(modelLoading);
			loadingScene = new Scene(parent);
			loadingStage = new Stage();

			if (owner != null) {
				loadingStage.initOwner(owner);
			}
			loadingStage.initStyle(StageStyle.UNDECORATED);
			Messages.sprintf("Owner is: " + loadingStage.getOwner());
//		loadingStage.setX(Main.conf.getWindowStartPosX());
			loadingStage.setTitle("loadingprocess_task: " + Main.conf.getWindowStartPosX());
			loadingScene.getStylesheets()
					.add(getClass().getResource(conf.getThemePath() + "loadingprocess.css").toExternalForm());

			xOffset = loadingStage.getX();
			yOffset = loadingStage.getY();

			Main.centerWindowDialog(loadingStage);
			loadingScene.setOnMousePressed(new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent event) {
					xOffset = (loadingStage.getX() - event.getScreenX());
					yOffset = (loadingStage.getY() - event.getScreenY());
					sprintf("yOffset: " + yOffset);
				}
			});

			loadingScene.setOnMouseDragged(new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent event) {
					loadingStage.setX(event.getScreenX() + xOffset);
					if (event.getScreenY() <= 0) {
						loadingStage.setY(0);
					} else {
						loadingStage.setY(event.getScreenY() + yOffset);
					}

					sprintf("event.getScreenY(); = " + event.getScreenY());
				}
			});

//		loadingStage.initStyle(StageStyle.UTILITY);

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
		if (modelLoading.getTask() != null) {
			sprintf("progressBar or task arent null");
			modelLoading.getTask().progressProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
					modelLoading.getProgressBar().setProgress((double) newValue);
				}
			});
			modelLoading.getTask().messageProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
					modelLoading.getMessages_lbl().setText(newValue);
				}
			});

		} else {
			sprintf("task were null BIND()");
		}
	}

	public void closeStage() {
		Messages.sprintf("Closing window or not?");

		stopTask();
		unbind();
		Timeline timeline = new Timeline();
		KeyFrame key = new KeyFrame(Duration.millis(2000),
				new KeyValue(Main.scene_Switcher.getScene_loading().getRoot().opacityProperty(), 0));
		timeline.getKeyFrames().add(key);
		timeline.setOnFinished(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				Main.scene_Switcher.getWindow_loadingprogress().close();
			}
		});
		timeline.play();

		Platform.runLater(() -> {
			Main.scene_Switcher.getWindow_loadingprogress().close();
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
