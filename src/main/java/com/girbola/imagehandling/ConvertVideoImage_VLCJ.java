/*
 @(#)Copyright:  Copyright (c) 2012-2024 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.imagehandling;

import com.girbola.Main;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.videothumbnailing.JCodecVideoThumbUtils;
import common.utils.FileUtils;
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
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static com.girbola.messages.Messages.sprintf;

/**
 * Converts video thumbnail using JCodec library
 *
 * @author Marko Lokka
 */
public class ConvertVideoImage_VLCJ extends Task<List<BufferedImage>>  {

	private final String ERROR = ConvertVideoImage_VLCJ.class.getSimpleName();
	private final FileInfo fileInfo;
	private final double image_width;
	private final ImageView imageView;
	private Image image;
	private Timeline timeLine;
	private static final String[] VLC_ARGS = { "--intf", "dummy", /* no interface */
			"--vout", "dummy", /* we don't want video (output) */
			"--no-audio", /* we don't want audio (decoding) */
			"--no-snapshot-preview", /* no blending in dummy vout */
	};
	private static final float VLC_THUMBNAIL_POSITION = 30.0f / 100.0f;

	/**
	 *
	 * @param fileInfo
	 * @param aImage_width
	 * @param aImageView
	 */
	public ConvertVideoImage_VLCJ(FileInfo fileInfo, ImageView aImageView, double aImage_width) {
		this.fileInfo = fileInfo;
		this.imageView = aImageView;
		this.image_width = aImage_width;
	}

	@Override
	protected List<BufferedImage>  call() throws Exception {
		sprintf("convertVideoToThumb TASK STARTED");
		if (isCancelled()) {
			return null;
		}

		if (Main.getProcessCancelled()) {
			sprintf("GetVideoThumb process CANCELLED + " + Main.getProcessCancelled());
			cancel();
			cancelled();
			return null;
		}
		long start = System.currentTimeMillis();
		makeSnapShots();
		sprintf("makeSnashot took: " + (System.currentTimeMillis() - start));
		return null;
	}

	private void makeSnapShots() throws InterruptedException {
		if (isCancelled()) {
			sprintf("Process cancelled at " + ERROR);
			return;
		}
		if (Main.getProcessCancelled()) {
			sprintf("Process cancelled at " + ERROR);
			this.cancel();
			return;
		}

		List<BufferedImage> bufferedImageList = new ArrayList<>();

		long videoLength = VideoVLCJThumb.getVideoLength(Paths.get(fileInfo.getOrgPath()));

		Messages.sprintf("Video length: " + videoLength);

		String mrl = "file:///" + FileUtils.fileSeparator_mrl(Paths.get(fileInfo.getOrgPath()));
		Messages.sprintf("MRL for video path: " + mrl);
		MediaPlayerFactory factory = new MediaPlayerFactory(VLC_ARGS);
		MediaPlayer mediaPlayer = factory.mediaPlayers().newMediaPlayer();
		final CountDownLatch inPositionLatch = new CountDownLatch(1);
		final CountDownLatch snapshotTakenLatch = new CountDownLatch(5);
		mediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
			@Override
			public void positionChanged(MediaPlayer mediaPlayer, float newPosition) {
				if (newPosition >= VLC_THUMBNAIL_POSITION * 0.9f) {
					/* 90% margin */
					inPositionLatch.countDown();
					Messages.sprintf("inPositionLatch count: " + newPosition);
				}
				Messages.sprintf("VLCJ newPosition count: " + newPosition);
			}

			@Override
			public void snapshotTaken(MediaPlayer mediaPlayer, String filename) {
				snapshotTakenLatch.countDown();
			}
		});
		BufferedImage buf = null;
		if (mediaPlayer.media().start(mrl)) {
			long duration = mediaPlayer.status().length();
			List<Double> doubles = JCodecVideoThumbUtils.grabListOfTimeLine(duration);
			for(Double d : doubles) {
				Messages.sprintf("grabListOfTimeLine: " + d);
				mediaPlayer.controls().setPosition(d.floatValue());
				buf = mediaPlayer.snapshots().get();
				bufferedImageList.add(buf);
				Messages.sprintf("grabListOfTimeLine buf: " + buf.getWidth());
			}

			try {
				inPositionLatch.await(); // Might wait forever if error
			} catch (InterruptedException ex) {
				Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
			}

//			Image image = SwingFXUtils.toFXImage(buf, null);
//			imageView.setImage(image);
			StackPane pane = (StackPane) imageView.getParent();
			VBox rootPane = (VBox) pane.getParent();

			if (bufferedImageList == null || bufferedImageList.isEmpty()) {
				System.err.println("VideoThumbMaker video thumblist were null. returning: " + fileInfo.getOrgPath());
				pane.getChildren().add(new Label("Video. NP"));
				return;
			}

			Label label = new Label("Video");
			label.setStyle("-fx-text-fill: orange;");
			label.setMouseTransparent(true);
			StackPane.setAlignment(label, Pos.TOP_CENTER);
			pane.getChildren().add(label);

			VideoPreview videoPreview = new VideoPreview(bufferedImageList, imageView);
			imageView.setImage(videoPreview.getImage(0));
			imageView.setUserData(bufferedImageList);
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
			// mediaPlayer.saveSnapshot(snapshotFile.toFile(), imageWidth, 0);
			try {
				snapshotTakenLatch.await(); // Might wait forever if
			} catch (InterruptedException ex) {
				Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
			}
			mediaPlayer.controls().stop();
		}
	}

	@Override
	protected void failed() {
		sprintf("ConvertVideoImage_VLCJ image loading failed: " + fileInfo);
	}

	@Override
	protected void cancelled() {
		sprintf("ConvertVideoImage_VLCJ image loading cancelled: " + fileInfo);
	}

	@Override
	protected void succeeded() {
		if (image != null) {
			imageView.setImage(image);
		}
	}
}