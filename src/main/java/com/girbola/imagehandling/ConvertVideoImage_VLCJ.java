/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.imagehandling;

import com.girbola.Main;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import common.utils.FileUtils;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;

import static com.girbola.messages.Messages.sprintf;

/**
 * Converts video thumbnail using JCodec library
 *
 * @author Marko Lokka
 */
public class ConvertVideoImage_VLCJ extends Task<Image> {

	private final String ERROR = ConvertVideoImage_VLCJ.class.getSimpleName();
	private final Path fileName;
	private final double image_width;
	private final ImageView imageView;
	private Image image;
	private static final String[] VLC_ARGS = { "--intf", "dummy", /* no interface */
			"--vout", "dummy", /* we don't want video (output) */
			"--no-audio", /* we don't want audio (decoding) */
			"--no-snapshot-preview", /* no blending in dummy vout */
	};
	private static final float VLC_THUMBNAIL_POSITION = 30.0f / 100.0f;

	/**
	 *
	 * @param aFileName
	 * @param aImage_width
	 * @param aImageView
	 */
	public ConvertVideoImage_VLCJ(Path aFileName, double aImage_width, ImageView aImageView) {
		this.fileName = aFileName;
		this.image_width = aImage_width;
		this.imageView = aImageView;
	}

	@Override
	protected Image call() throws Exception {
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

		String mrl = "file:///" + FileUtils.fileSeparator_mrl(fileName);
		Messages.sprintf("MRL for video path: " + mrl);
		MediaPlayerFactory factory = new MediaPlayerFactory(VLC_ARGS);
		MediaPlayer mediaPlayer = factory.mediaPlayers().newMediaPlayer();

		final CountDownLatch inPositionLatch = new CountDownLatch(1);
		final CountDownLatch snapshotTakenLatch = new CountDownLatch(1);
		mediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
			@Override
			public void positionChanged(MediaPlayer mediaPlayer, float newPosition) {
				if (newPosition >= VLC_THUMBNAIL_POSITION * 0.9f) {
					/* 90% margin */
					inPositionLatch.countDown();
				}
			}

			@Override
			public void snapshotTaken(MediaPlayer mediaPlayer, String filename) {
				snapshotTakenLatch.countDown();
			}
		});
		BufferedImage buf = null;
		if (mediaPlayer.media().start(mrl)) {
			mediaPlayer.controls().setPosition(VLC_THUMBNAIL_POSITION);

			try {
				inPositionLatch.await(); // Might wait forever if error
			} catch (InterruptedException ex) {
				Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
			}

			buf = mediaPlayer.snapshots().get();
			Image image = SwingFXUtils.toFXImage(buf, null);
			imageView.setImage(image);

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
		sprintf("image loading failed: " + fileName);
	}

	@Override
	protected void cancelled() {
		sprintf("image loading cancelled: " + fileName);
	}

	@Override
	protected void succeeded() {
		if (image != null) {
			imageView.setImage(image);
		}
	}
}