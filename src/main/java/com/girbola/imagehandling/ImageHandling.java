/*
 @(#)Copyright:  Copyright (c) 2012-2024 All right reserved.  
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.imagehandling;

import com.drew.metadata.Metadata;
import com.girbola.Main;
import com.girbola.controllers.datefixer.DateFixerController;
import com.girbola.fileinfo.FileInfo;
import com.girbola.utils.FileInfoUtils;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import common.media.DateTaken;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.girbola.messages.Messages.sprintf;

/**
 *
 * @author Marko Lokka
 */
public class ImageHandling {

	final private static String ERROR = ImageHandling.class.getName();

	public static Task<Image> handleBufferedThumb(FileInfo fileInfo, double thumb_x_MAX, ImageView imageView) {
		Task<Image> convert = new Task<Image>() {
			@Override
			protected Image call() throws Exception {
				sprintf("handleBufferedThumb: " + fileInfo.getOrgPath());
				long start = System.currentTimeMillis();
				BufferedImage bi = ImageIO.read(new File(fileInfo.getOrgPath()));
				Messages.sprintf("Creating bufferedimage took: " + (System.currentTimeMillis() - start));
				Image image = SwingFXUtils.toFXImage(bi, null);
				if (image != null) {
					if (imageView != null) {
						imageView.setImage(image);
						Messages.sprintf("Setting image to imageview took: " + (System.currentTimeMillis() - start));
					}
					return image;
				}
				Messages.sprintf("returning null from handleBufferedThumb");
				return null;
			}
		};

		return convert;
	}

	public static Task<Image> handleTiffThumb(FileInfo fileInfo, double thumb_x_MAX, ImageView imageView) {
		Task<Image> convert = new Task<Image>() {
			@Override
			protected Image call() throws Exception {
				if (isCancelled()) {
					Main.setProcessCancelled(true);
					return null;
				}
				if (Main.getProcessCancelled()) {
					Main.setProcessCancelled(true);
					cancel();
					return null;
				}
				ImageInputStream input = ImageIO.createImageInputStream(new File(fileInfo.getOrgPath()));

				try {
					// Get the reader
					Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
					Messages.sprintf("Debugging tiff converter: ");
					if (!readers.hasNext()) {
						throw new IllegalArgumentException("No reader for: " + fileInfo.getOrgPath());
					}

					ImageReader reader = readers.next();
					if (Main.getProcessCancelled()) {
						Main.setProcessCancelled(true);
						cancel();
						reader.dispose();
						return null;
					}
					try {
						reader.setInput(input);
						// Optionally, read thumbnails, meta data, etc...
						int numThumbs = reader.getNumThumbnails(0);
						Messages.sprintf("Number of thumbs: " + numThumbs);
						if (numThumbs > 0) {
							long start = System.currentTimeMillis();
							ImageReadParam param = reader.getDefaultReadParam();
							Messages.sprintf("Tiff reader found number of thumbs: " + numThumbs);
							BufferedImage buff = reader.read(numThumbs, param);
							Messages.sprintf("TIFF BUFFIMAGE");
							Image image = SwingFXUtils.toFXImage(buff, null);
							if (image != null) {
								if (imageView != null) {
									imageView.setImage(image);
									Messages.sprintf(
											"Setting image to imageview took: " + (System.currentTimeMillis() - start));
								}
								return image;
							}
						} else {
							if(Main.getProcessCancelled()) {
								Main.setProcessCancelled(true);
								cancel();
								return null;
							}
							ImageReadParam param = reader.getDefaultReadParam();
							long start = System.currentTimeMillis();
							// Finally read the image, using settings from param
							BufferedImage buff = reader.read(0, param);
							Messages.sprintf("TIFF BUFFIMAGE");
							Image image = SwingFXUtils.toFXImage(buff, null);
							if (image != null) {
								if (imageView != null) {
									imageView.setImage(image);
									Messages.sprintf(
											"Setting image to imageview took: " + (System.currentTimeMillis() - start));
								}
								return image;
							}
						}
						// ...
					} finally {
						// Dispose reader in finally block to avoid memory leaks
						reader.dispose();
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					// Close stream in finally block to avoid resource leaks
					input.close();
				}
				sprintf("handleBufferedThumb: " + fileInfo.getOrgPath());
				return null;
			}
		};

		return convert;
	}

	public static Task<Image> handleImageThumb(FileInfo fileInfo, double width, ImageView imageView) {
		// sprintf("handleImageThumb: " + Paths.get(fileInfo.getOrgPath()));
		Task<Image> convr = new ConvertImage(Paths.get(fileInfo.getOrgPath()), width, imageView);
		return convr;
	}

	public static Task<Image> handleRawImageThumb(FileInfo fileInfo, double width, ImageView imageView) {
		// sprintf("handleRawImageThumb: " + Paths.get(fileInfo.getOrgPath()));
		if (fileInfo.getThumb_offset() != 0 && fileInfo.getThumb_length() != 0) {
			Task<Image> convertImage_offset_task = convertImage_offset(Paths.get(fileInfo.getOrgPath()),
					fileInfo.getThumb_offset(), fileInfo.getThumb_length(), width, imageView);
			return convertImage_offset_task;
		} else {
			Metadata metaData = DateTaken.readMetaData(Paths.get(fileInfo.getOrgPath()));
			if (metaData != null) {
				FileInfoUtils.getImageThumb_Offset_Length(metaData, fileInfo);
				if (fileInfo.getThumb_offset() != 0 && fileInfo.getThumb_length() != 0) {
					Task<Image> convertImage_offset_task = convertImage_offset(Paths.get(fileInfo.getOrgPath()),
							fileInfo.getThumb_offset(), fileInfo.getThumb_length(), width, imageView);
					return convertImage_offset_task;
				}
			}
			return null;
		}
	}

	public static Task<Image> handleTiffThumb(FileInfo fileInfo) {
		Path path = Paths.get(fileInfo.getOrgPath());

		Task<Image> image = new Task<Image>() {

			@Override
			protected Image call() throws Exception {

				ImageInputStream is = null;

				try {
					is = ImageIO.createImageInputStream(path.toFile());
					if (is == null || is.length() == 0) {
						Messages.sprintf("Image is null");
					}

					Iterator<ImageReader> iterator = ImageIO.getImageReaders(is);
					if (iterator == null || !iterator.hasNext()) {
						throw new IOException("Image file format not supported by ImageIO: " + path);
					}
					ImageReader reader = (ImageReader) iterator.next();
					reader.setInput(is);
					int nbPages = reader.getNumImages(true);
					Messages.sprintf("nhPages are in tiff file: " + nbPages);
					BufferedImage bf = reader.read(0); // 1st page of tiff file
					BufferedImage bf1 = reader.read(1); // 2nd page of tiff file
					WritableImage wr = null;
					WritableImage wr1 = null;
					if (bf != null) {
						wr = SwingFXUtils.toFXImage(bf, null);
						Messages.sprintf("br were not null: " + bf.getWidth());
					}
					if (bf1 != null) {
						wr = SwingFXUtils.toFXImage(bf1, null);
						Messages.sprintf("br1 were not null: " + bf1.getWidth());
					}
				} catch (FileNotFoundException ex) {
					Logger.getLogger(ImageHandling.class.getName()).log(Level.SEVERE, null, ex);
				} catch (IOException ex) {
					Logger.getLogger(ImageHandling.class.getName()).log(Level.SEVERE, null, ex);
				}
				return null;
			}
		};
		return image;
	}

	@Deprecated
	public static BufferedImage scale(BufferedImage bufferedImage_src, int wanted_WIDTH, int wanted_HEIGHT) {
		BufferedImage img = new BufferedImage(wanted_WIDTH, wanted_HEIGHT, BufferedImage.TYPE_INT_RGB);
		int x, y;
		int ww = bufferedImage_src.getWidth();
		int hh = bufferedImage_src.getHeight();
		int[] ys = new int[wanted_HEIGHT];
		Messages.sprintf("YS is: " + ys);
		for (y = 0; y < wanted_HEIGHT; y++) {
			// y 1 hh 3168 ys[y] wanted_HEIGHT 7542
			// 1 * 3168 / 7542
			ys[y] = y * hh / wanted_HEIGHT;
			// Messages.sprintf(" y " + y + " hh " + hh + " wanted_HEIGHT " +
			// wanted_HEIGHT + " ys[y] " + ys[y]);
		}
		for (x = 0; x < wanted_WIDTH; x++) {
			// x 1 ww 4752 wanted_WIDTH 100 newX is: 47
			// 1 * 4752 / 100 = 47
			// 2 * 4752 / 100 =
			/*
			 * 0-99
			 */
			int newX = x * ww / wanted_WIDTH;
			for (y = 0; y < wanted_HEIGHT; y++) {
				int col = bufferedImage_src.getRGB(newX, ys[y]);
				img.setRGB(x, y, col);
			}
		}
		return img;
	}
	/**
	 * Converts RAW image to Image using JRawio library. It is inherited with
	 * ImageIO.
	 *
	 * @param fileInfo
	 * @param imageView
	 * @return
	 */
	@Deprecated
	public static Task<Image> convertRAWImageUsingJRawio_(FileInfo fileInfo, ImageView imageView) {

        return new Task<Image>() {
			@Override
			protected Image call() throws Exception {
				sprintf("convertRAWImageUsingJRawio: " + fileInfo.getOrgPath());
				BufferedImage bi = ImageIO.read(new File(fileInfo.getOrgPath()));
				Image image = SwingFXUtils.toFXImage(bi, null);
				if (image != null) {
					if (imageView != null) {
						imageView.setImage(image);
					}
					return image;
				}
				return null;
			}
		};
	}

	/**
	 *
	 * @param image_width
	 * @param thumbImage
	 * @param imageView
	 *
	 * @return
	 */
	public static Task<Image> convertImage(Path thumbImage, double image_width, ImageView imageView) {

		sprintf("convertImage: " + thumbImage.toString());

		Task<Image> image_task = new Task<Image>() {
			@Override
			protected Image call() throws Exception {
				try {
					sprintf("converting image");
					Image image = new Image(thumbImage.toUri().toString(), image_width, 0, true, true, false);
					image.progressProperty().addListener(new ChangeListener<Number>() {
						@Override
						public void changed(ObservableValue<? extends Number> observable, Number oldValue,
								Number newValue) {
							if ((double) newValue == 1.0) {
								sprintf("Image loaded successfully: " + thumbImage);
							} else {
								sprintf("Image loading: " + thumbImage);
							}
						}
					});
					if (imageView != null) {
						imageView.setImage(image);
					}
				} catch (Exception ex) {
					Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), false);
					return null;
				}
				return null;
			}
		};
		return image_task;
	}

	/**
	 *
	 * @param fileName
	 * @param image_width
	 * @param offset
	 * @param length
	 * @param imageView
	 * @return
	 */
	public static Task<Image> convertImage_offset(Path fileName, int offset, int length, double image_width,
			ImageView imageView) {

        return new Task<Image>() {
			@Override
			protected Image call() throws Exception {

				byte[] data = null;
				try {
					data = Files.readAllBytes(fileName);
				} catch (Exception ex) {
					Messages.sprintfError(
							"Can't Files.readAllBytes: " + fileName + "\nError message: " + ex.getMessage());
					return null;
				}

				if (data == null) {
					sprintf("Cannot readAllByte to data. returning->");
					return null;
				}
				sprintf("data size is: " + data.length + " length: " + length + " offset: " + offset);
				byte[] slice = null;
				try {
					slice = Arrays.copyOfRange(data, offset, (offset + length));
				} catch (Exception ex) {
					return null;
				}

				ByteArrayInputStream in = new ByteArrayInputStream(slice);
				BufferedImage bufferedImage = null;
				try {
					bufferedImage = ImageIO.read(in);
				} catch (IOException ex) {
					return null;
				}
				try {
					Image image = SwingFXUtils.toFXImage(bufferedImage, null);
					if (image != null) {
						if (imageView != null) {
							imageView.setImage(image);
						}
					}
					return image;
				} catch (Exception e) {
					sprintf("convertImage_offset exception: " + e.getMessage());
					return null;

				}
			}
		};
	}

}
