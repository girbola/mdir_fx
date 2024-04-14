/*
 @(#)Copyright:  Copyright (c) 2012-2024 All right reserved.
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.loading;

import com.girbola.Main;
import com.girbola.concurrency.ConcurrencyUtils;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import static com.girbola.concurrency.ConcurrencyUtils.exec;
import static com.girbola.messages.Messages.sprintf;

/**
 * FXML Controller class
 *
 * @author Marko Lokka
 */
public class LoadingProcessController {

	private final static String ERROR = LoadingProcessController.class.getSimpleName();

	@FXML
	private Button cancel_btn;
	@FXML
	private Label messages_lbl;

	private ModelLoading model_loading;

	@FXML
	private ProgressBar progressBar;

	@FXML
	private void cancel_btn_action(ActionEvent event) {
		sprintf("Cancel pressed!");
		Main.setProcessCancelled(true);
		closeWindow();
	}

	public void closeWindow() {
		// model.getTask().cancel();
		if (model_loading.getTask() != null) {
			if (model_loading.getTask().isRunning()) {
				model_loading.getTask().cancel();
			}
		}
		exec[ConcurrencyUtils.getExecCounter()].shutdownNow();
		// if (model.getTask() != null) {
		// model.getTask().cancel();
		// }
		Main.setProcessCancelled(true);
		Timeline timeline = new Timeline();
		sprintf("MODELLL: " + model_loading.toString());
		if (Main.scene_Switcher.getWindow_loadingprogress().getScene() == null) {
			sprintf("model.getStage().getScene(). WERE nULL");
		}
		KeyFrame key = new KeyFrame(Duration.millis(2000), new KeyValue(Main.scene_Switcher.getWindow_loadingprogress().getScene().getRoot().opacityProperty(), 0));
		timeline.getKeyFrames().add(key);
		timeline.setOnFinished(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Main.scene_Switcher.getWindow_loadingprogress().close();
			}
		});
		timeline.play();
	}

	public Button getCancel_btn() {
		return cancel_btn;
	}

	public Label getMessage_lbl() {
		return this.messages_lbl;
	}

	public void setCancel_btn(Button cancel_btn) {
		this.cancel_btn = cancel_btn;
	}

	public void init(ModelLoading model_loading) {
		this.model_loading = model_loading;
		this.model_loading.setMessages_lbl(messages_lbl);
		this.model_loading.setProgressBar(progressBar);
	}

	public ProgressBar getProgressBar() {
		return this.progressBar;
	}

	// Create a shadow effect as a halo around the pane and not within
	// the pane's content area.
	private void createShadowPane(Pane shadowPane) {
		double shadowSize = 1;
		// Pane shadowPane = new Pane();
		// a "real" app would do this in a CSS stylesheet.
		// shadowPane.getStylesheets().add("shadowPane");

		Rectangle innerRect = new Rectangle();
		Rectangle outerRect = new Rectangle();
		shadowPane.layoutBoundsProperty().addListener((observable, oldBounds, newBounds) -> {
			innerRect.relocate(newBounds.getMinX() + shadowSize, newBounds.getMinY() + shadowSize);
			innerRect.setWidth(newBounds.getWidth() - shadowSize * 2);
			innerRect.setHeight(newBounds.getHeight() - shadowSize * 2);

			outerRect.setWidth(newBounds.getWidth());
			outerRect.setHeight(newBounds.getHeight());

			Shape clip = Shape.subtract(outerRect, innerRect);
			shadowPane.setClip(clip);
		});

		// return shadowPane;
	}

}
