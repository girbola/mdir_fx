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
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameGrabber;

@Slf4j
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
			list = JCodecVideoThumb.getList(new File(fileInfo.getOrgPath()));
			if (list == null) {
				return null;
			}
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
		try {
			org.bytedeco.javacv.FrameGrabber fr = FrameGrabber.createDefault(fileInfo.getOrgPath());
			Messages.sprintf("FrameGrabberFrameGrabberFrameGrabber: " + fr.toString());

		} catch (FFmpegFrameGrabber.Exception e) {
			FrameGrabber grabber = new OpenCVFrameGrabber(fileInfo.getOrgPath());
			Messages.sprintf("1GARBBERRRERBAERB: " + grabber.getFormat());
			Messages.sprintf("2AERAERAGERA: " + grabber.toString());
			throw new RuntimeException(e);
		} catch (FrameGrabber.Exception e) {
			FrameGrabber grabber = new OpenCVFrameGrabber(fileInfo.getOrgPath());
			Messages.sprintf("3GARBBERRRERBAERB: " + grabber.getFormat());
			Messages.sprintf("4AERAERAGERA: " + grabber.toString());
			throw new RuntimeException(e);
		}
		if (list == null) {
			System.err.println("VideoThumbMaker video thumblist were null. returning: " + fileInfo.getOrgPath());
			FrameGrabber grabber = new OpenCVFrameGrabber(fileInfo.getOrgPath());
			Messages.sprintf("GARBBERRRERBAERB: " + grabber.getFormat());
			Messages.sprintf("AERAERAGERA: " + grabber.toString());
//				FrameGrabber frameG = FrameGrabber.createDefault(fileInfo.getOrgPath());
////				int lengthInFrames = ffMpegVideo.getLengthInFrames();
//				Messages.sprintf("===lengthInFrames: " + frameG.toString());

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
		rootPane.setOnMouseEntered(event -> {
			System.out.println("m e");
			timeLine = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
				Image image = SwingFXUtils.toFXImage(videoPreview.showNextImage(), null);
				Platform.runLater(() -> imageView.setImage(image));
			}));
			timeLine.setCycleCount(6);
			timeLine.play();
		});
		rootPane.setOnMouseExited(event -> {
			if (timeLine != null) {
				timeLine.stop();
			}
			imageView.setImage(videoPreview.getImage(0));
		});
	}

	private FFmpegFrameGrabber createVideoThumb(String fileName) throws FFmpegFrameGrabber.Exception {
		FFmpegFrameGrabber frameGrabber = FFmpegFrameGrabber.createDefault(fileName);
		if (frameGrabber != null) {

//	int lengthInFrames = frameGrabber.getLengthInFrames();
			Messages.sprintf("=======lengthInFrames" + frameGrabber.toString());
		}
		return null;
	}

	@Override
	protected void cancelled() {
	}

	@Override
	protected void failed() {
	}

}
