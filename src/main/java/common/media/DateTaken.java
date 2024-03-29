/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package common.media;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.mov.QuickTimeDirectory;
import com.drew.metadata.mp4.media.Mp4VideoDirectory;
import common.utils.FileNameParseUtils;
import common.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import static com.girbola.messages.Messages.sprintf;

/**
 *
 * @author Marko Lokka
 */
public class DateTaken {

	public static String getCameraModel(Metadata metaData) {
		Directory directory2 = metaData.getFirstDirectoryOfType(ExifIFD0Directory.class);
		String st = null;
		try {
			st = directory2.getString(0x110);
		} catch (Exception e) {
			// sprintf("getCameraModel Exception e: " + e.getMessage());
			return null;
		}
		// sprintf("Camera model is: " + st);
		return st;
	}

	public static int getMetaDataOrientation(Metadata metaData) {
		int orientation = 0;
		Directory directory = metaData.getFirstDirectoryOfType(ExifIFD0Directory.class);
		if (directory != null) {
			try {
				orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
			} catch (MetadataException ex) {
				return 0;
			}
		}
		return orientation;
	}

	public static long getCreationDate(Path path) {
		if (FileUtils.supportedImage(path)) {
			long date = getMetadataDateTaken(path);
			if (date != 0) {
				return date;
			}
		} else if (FileUtils.supportedVideo(path)) {
			String orgFileName = FileNameParseUtils.parseFileExtentension(path);
			Path thmFile = Paths.get(path.getParent() + File.separator + orgFileName + ".THM");
			sprintf("thmFile name is: " + thmFile);
			if (Files.exists(thmFile)) {
				sprintf("THM FILE FOUND: " + thmFile);
				// ongelma;
				long date = getMetadataDateTaken(thmFile);
				sprintf("getDateThumbFileForVideo date: " + date);
				return date;
			} else {
				sprintf("THM FILE NOT FOUND: " + thmFile);
				return 0;
			}
		}
		return 0;
	}

	public static long getMetadataDateTaken(Path path) {
		Metadata metaData = null;
		try {
			metaData = ImageMetadataReader.readMetadata(path.toFile());
		} catch (ImageProcessingException
				| IOException ex) {
			return 0;
		}
		Date date = null;
		Iterable<Directory> directories = metaData.getDirectories();

		for (Directory directory : directories) {
			if (directory != null) {
				date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
				if (date != null) {
					return date.getTime();
				}
			}
		}
		if (date == null) {
			return 0;
		}
		return 0;
	}

	public static long getMetaDataCreationDate(Metadata metaData, Path path) {
		// sprintf("getMetadataCreationDate: " + path);

		Iterable<Directory> directories = metaData.getDirectories();

		if (FileUtils.supportedImage(path.toFile()) || FileUtils.supportedRaw(path.toFile())) {
			for (Directory directory : directories) {
				if (directory != null) {
					Date imageDate_EXIF = null;
					imageDate_EXIF = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
					if (imageDate_EXIF != null) {
						return imageDate_EXIF.getTime();
					}
				}
			}
		} else if (FileUtils.supportedVideo(path)) {
			for (Directory directory : directories) {
				if (directory != null) {
					Date videoDate_EXIF = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
					if (videoDate_EXIF != null) {
						return videoDate_EXIF.getTime();
					}
					Date tag_creation_time_QUICKTIME = directory.getDate(QuickTimeDirectory.TAG_CREATION_TIME);
					if (tag_creation_time_QUICKTIME != null) {
						return tag_creation_time_QUICKTIME.getTime();
					}
					Date tag_creation_time_MP4 = directory.getDate(Mp4VideoDirectory.TAG_CREATION_TIME);
					if (tag_creation_time_MP4 != null) {
						return tag_creation_time_MP4.getTime();
					}
				}
			}
		}
		return 0;
	}

	public static Metadata readMetaData(Path path) {
		Metadata metaData = null;
		try {
			metaData = ImageMetadataReader.readMetadata(path.toFile());
		} catch (Exception e) {
			return null;
		}
		return metaData;
	}
}
