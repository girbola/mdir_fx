package com.girbola.thumbnailator;

import com.girbola.messages.Messages;
import net.coobird.thumbnailator.Thumbnails;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Thumbnailator class is using thumbnailator (https://github.com/coobird/thumbnailator)  library to make thumbnails
 * easier
 *
 */
public class Thumbnailator {
	
	/**
	 * Save thumbs using Thumbnailator library
	 * 
	 * @param source
	 * @param destination
	 * @param width
	 * @param rotate
	 * @return
	 */
	public static boolean saveThumbnail(Path source, Path destination, int width, double rotate) {
		long start = System.currentTimeMillis();
		try {
			Thumbnails.of(source.toFile()).width(width).keepAspectRatio(true).rotate(rotate).toFile(destination.toFile());
			Messages.sprintf("thumbnails done: " + (System.currentTimeMillis() - start) + " ms ");
			if (Files.exists(destination)) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			System.err.println("Can't create thumbnail: " + e);
			return false;
		}
	}

	/**
	 * Saving thumbnail using BufferedImage
	 * 
	 * @param source
	 * @param destination
	 * @param width
	 * @param rotate
	 * @return
	 */
	public static boolean saveThumbnail(BufferedImage source, Path destination, int width, double rotate) {
		long start = System.currentTimeMillis();
		try {
			Thumbnails.of(source).width(width).keepAspectRatio(true).rotate(rotate).toFile(destination.toFile());
			Messages.sprintf("thumbnails done: " + (System.currentTimeMillis() - start) + " ms ");
			if (Files.exists(destination)) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			System.err.println("Can't create thumbnail: " + e);
			return false;
		}
	}
}
