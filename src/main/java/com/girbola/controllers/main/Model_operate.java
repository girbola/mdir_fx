/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.main;

import com.girbola.Main;
import com.girbola.Scene_NameType;
import com.girbola.fxml.operate.CopyProcess_Properties;
import com.girbola.messages.Messages;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Model_operate {
	private final String ERROR = Model_operate.class.getSimpleName();

	private CopyProcess_Properties copyProcess_values = new CopyProcess_Properties();

	private Button cancel_btn;
	private Button start_btn;
	private ProgressBar copy_progressBar;
	private ProgressIndicator progress;

	private Timeline timeline;

	public Model_operate() {

		timeline = new Timeline(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				updateTimer();
			}
		}));
		timeline.setCycleCount(Timeline.INDEFINITE);
	}

	public Timeline getTimeline() {
		return timeline;
	}

	public ProgressBar getCopy_progressBar() {
		return copy_progressBar;
	}

	public void setCopy_progressBar(ProgressBar aCopy_progressBar) {
		this.copy_progressBar = aCopy_progressBar;
	}

	public void setCancelButton(Button aCancel_btn) {
		this.cancel_btn = aCancel_btn;
	}

	public CopyProcess_Properties getCopyProcess_values() {
		return copyProcess_values;
	}

	public void setTotalFilesProgress(ProgressIndicator progress) {
		this.progress = progress;
	}

	public ProgressIndicator getProgress() {
		return this.progress;
	}

	public void setStartButton(Button aStart_btn) {
		this.start_btn = aStart_btn;
	}

	public Button getCancel_btn() {
		return cancel_btn;
	}

	public Button getStart_btn() {
		return start_btn;
	}

	public void closeWindow() {
		Stage stage = (Stage) this.start_btn.getScene().getWindow();
		stage.close();
	}

	/**
	 * @param start_btn the start_btn to set
	 */
	public void setStart_btn(Button start_btn) {
		this.start_btn = start_btn;
	}

	/**
	 * @param progress the progress to set
	 */
	public void setProgress(ProgressIndicator progress) {
		this.progress = progress;
	}

	// timer.setCycleCount(Timeline.INDEFINITE);

	public void updateTimer() {
//		Messages.sprintf("Start timer started");
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				getCopyProcess_values().update();
			}
		});
	}

	public void doneButton(String scene_NameType, boolean close) {

		if (scene_NameType.equals(Scene_NameType.DATEFIXER.getType())) {
			Messages.sprintf("done byn datefix");
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					getStart_btn().setDisable(false);
					getCancel_btn().setDisable(true);
					getStart_btn().setText(Main.bundle.getString("close"));
					getStart_btn().setOnAction(done_btnEvent_scene_dateFixer(close));

				}
			});
		} else if (scene_NameType.equals(Scene_NameType.MAIN.getType())) {
			Messages.sprintf("done byn MAIN");
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					getStart_btn().setDisable(false);
					getCancel_btn().setDisable(true);
					getStart_btn().setText(Main.bundle.getString("close"));
					getStart_btn().setOnAction(done_btnEvent_scene_Main(close));

				}
			});
		}

	}

	public EventHandler<ActionEvent> done_btnEvent_scene_dateFixer(boolean close) {
		EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Messages.sprintf("done button done_btnEvent_scene_dateFixer");
				Platform.runLater(() -> {
					Main.scene_Switcher.getWindow().setScene(Main.scene_Switcher.getScene_dateFixer());
				});
			}
		};
		return event;
	}

	public EventHandler<ActionEvent> cancel_btnEvent_scene_dateFixer() {
		EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				((Stage) Main.scene_Switcher.getScene_operate().getWindow()).close();
			}
		};

		return event;
	}

	public EventHandler<ActionEvent> done_btnEvent_scene_Main(boolean close) {
		EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Messages.sprintf("done button activated");
				Platform.runLater(() -> {
					Main.scene_Switcher.getWindow_loadingprogress().close();
					Main.scene_Switcher.getWindow().setScene(Main.scene_Switcher.getScene_main());

				});
			}
		};
		return event;
	}

	public EventHandler<ActionEvent> cancel_btnEvent_scene_Main() {
		EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				((Stage) Main.scene_Switcher.getScene_operate().getWindow()).close();
			}
		};

		return event;
	}

	public void stopTimeLine() {
		getTimeline().stop();
		getCopyProcess_values().update();
	}

}
