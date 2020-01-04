/*
 @(#)Copyright:  Copyright (c) 2012-2020 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.imagehandling;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.girbola.fileinfo.ThumbInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;

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

/**
 *
 * @author Marko Lokka
 */
public class ConvertVideo_Byte extends Task<List<BufferedImage>> {

	private final String ERROR = ConvertVideo_Byte.class.getSimpleName();
	private final Path fileName;
	private final double image_width;
	private final ThumbInfo thumbInfo;
	private final ImageView imageView;
	private List<BufferedImage> bufferedImageList = new ArrayList<>();
	private Timeline timeLine;

	public ConvertVideo_Byte(Path aFileName, ThumbInfo aThumbInfo, double aImage_width, ImageView aImageView) {
		this.fileName = aFileName;
		this.thumbInfo = aThumbInfo;
		this.image_width = aImage_width;
		this.imageView = aImageView;
	}

	@Override
	protected List<BufferedImage> call() throws Exception {
		try {
			for (byte[] imageInByte : thumbInfo.getThumbs()) {
				if (imageInByte == null) {
					Messages.sprintf("There were no more images in byte[] array");
					cancel();
					break;
				}
				InputStream in = new ByteArrayInputStream(imageInByte);

				BufferedImage bufferedImage = ImageIO.read(in);
				bufferedImageList.add(bufferedImage);
			}
		} catch (Exception ex) {
			Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), false);
			return null;
		}

		return bufferedImageList;
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
			System.err.println("video thumblist were null. returning: " + fileName);
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
