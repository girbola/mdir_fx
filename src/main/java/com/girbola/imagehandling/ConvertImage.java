/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.imagehandling;

import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.nio.file.Path;

import static com.girbola.messages.Messages.sprintf;
import static com.girbola.misc.Misc.getLineNumber;

/**
 *
 * @author Marko Lokka
 */
public class ConvertImage extends Task<Image> {

	private final String ERROR = ConvertImage.class.getSimpleName();

	private final Path thumbImage;
	private final double image_width;
	private final ImageView imageView;
	private Image image;

	public ConvertImage(Path thumbImage, double image_width, ImageView imageView) {
		this.thumbImage = thumbImage;
		this.image_width = image_width;
		this.imageView = imageView;
	}

	@Override
	protected Image call() throws Exception {
		try {
			image = new Image(thumbImage.toUri().toString(), image_width, 0, true, true, false);
		} catch (Exception ex) {
			Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), false);
			return null;
		}
		return null;
	}

	@Override
	protected void failed() {
		super.failed();
		sprintf("image loading failed: " + thumbImage);
	}

	@Override
	protected void cancelled() {
		super.cancelled();
		sprintf("image loading cancelled: " + thumbImage);
	}

	@Override
	protected void succeeded() {
		super.succeeded();
		if (image != null) {
			if (imageView != null) {
				imageView.setImage(image);
			}
		} else {
			Messages.errorSmth(ERROR, "Problem with adding image to ImageView", null, getLineNumber(), true);
		}

	}

}
