package com.girbola.imagehandling;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class VideoThumbMaker extends Task<List<BufferedImage>> {

	private FileInfo fileInfo;
	private ImageView imageView;
	private double image_width;
	private Timeline timeLine;

	public VideoThumbMaker(FileInfo fileInfo, ImageView imageView, double image_width) {
		this.fileInfo = fileInfo;
		this.imageView = imageView;
		this.image_width = image_width;
	}

	@Override
	protected List<BufferedImage> call() throws Exception {
		List<BufferedImage> list = null;
		try {
			list = VideoThumb.getList(new File(fileInfo.getOrgPath()));
		} catch (Exception ex) {
			System.out.println("Exception ex: " + ex);
			return null;
		}
		System.out.println("list size is: " + list.size());
		return list;
	}

	@Override
	protected void succeeded() {
		List<BufferedImage> list = null;
		try {
			list = get();
		} catch (Exception e) {
			super.cancel();
			System.err.println("buffered failed: " + e);
			return;
		}

		StackPane pane = (StackPane) imageView.getParent();
		VBox rootPane = (VBox) pane.getParent();
		if (list == null) {
			System.err.println("video thumblist were null. returning: " + fileInfo.getOrgPath());
			pane.getChildren().add(new Label("Video. NP"));
			return;
		}
		if (list.isEmpty()) {
			Messages.sprintfError("list were empty");
			pane.getChildren().add(new Label("Video. NP"));
			return;
		} else {
			Label label = new Label("Video");
			label.setStyle("-fx-text-fill: orange;");
			label.setMouseTransparent(true);
			StackPane.setAlignment(label, Pos.TOP_CENTER);
			pane.getChildren().add(label);
		}
		VideoPreview videoPreview = new VideoPreview(list, imageView);
		imageView.setImage(videoPreview.getImage(0));
		imageView.setUserData(list);
		rootPane.setOnMouseEntered(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				System.out.println("m e");
				timeLine = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
					Image image = SwingFXUtils.toFXImage(videoPreview.showNextImage(), null);
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							imageView.setImage(image);
						}
					});
				}));
				timeLine.setCycleCount(6);
				timeLine.play();
			}
		});
		rootPane.setOnMouseExited(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (timeLine != null) {
					timeLine.stop();
				}
				imageView.setImage(videoPreview.getImage(0));
			}
		});
	}

	@Override
	protected void cancelled() {
	}

	@Override
	protected void failed() {
	}

}
