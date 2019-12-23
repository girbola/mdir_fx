/*
 @(#)Copyright:  Copyright (c) 2012-2019 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.imagehandling;

import static com.girbola.messages.Messages.sprintf;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;

import com.girbola.fileinfo.ThumbInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;

import common.utils.FileUtils;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 *
 * @author Marko Lokka
 */
public class ConvertImage_Byte extends Task<Image> {

	private final String ERROR = ConvertImage_Byte.class.getSimpleName();
	private final Path fileName;
	private final double image_width;
	private final ThumbInfo thumbInfo;
	private final ImageView imageView;
	private Image image;

	public ConvertImage_Byte(Path aFileName, ThumbInfo aThumbInfo, double aImage_width, ImageView aImageView) {
		this.fileName = aFileName;
		this.thumbInfo = aThumbInfo;
		this.image_width = aImage_width;
		this.imageView = aImageView;
	}

	@Override
	protected Image call() throws Exception {
		try {
			image = new Image(new ByteArrayInputStream(thumbInfo.getThumbs().get(0)));
		} catch (Exception ex) {
			Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), false);
			return null;
		}

		return image;
	}

	@Override
	protected void failed() {
		sprintf("image loading failed: " + fileName);
		super.failed();
	}

	@Override
	protected void cancelled() {
		sprintf("image loading cancelled: " + fileName);
		super.cancelled();
	}

	@Override
	protected void succeeded() {
		if (image != null) {
			imageView.setImage(image);
		}
		super.succeeded();
	}

}
