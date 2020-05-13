/*
 @(#)Copyright:  Copyright (c) 2012-2020 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.fileinfo;

import static com.girbola.messages.Messages.sprintf;
import static common.media.DateTaken.getMetaDataCreationDate;
import static common.media.DateTaken.readMetaData;
import static common.utils.FileUtils.filter_directories;
import static common.utils.FileUtils.supportedVideo;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifThumbnailDirectory;
import com.girbola.Main;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.filelisting.ValidatePathUtils;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;

import common.media.DateTaken;
import common.media.VideoDateFinder;
import common.utils.FileNameParseUtils;
import common.utils.FileUtils;
import common.utils.date.DateUtils;

/**
 *
 * @author Marko Lokka
 */
public class FileInfo_Utils {

	private final static String ERROR = FileInfo_Utils.class.getSimpleName();

	public static FileInfo createFileInfo(Path fileName) throws IOException {
		// sprintf("createFileInfo started: " + path);

		FileInfo fileInfo = null;
		if (FileUtils.supportedImage(fileName)) {
			fileInfo = new FileInfo(fileName.toString(), Main.conf.getId_counter().incrementAndGet());
			setImage(fileInfo);
			fileInfo.setSize(Files.size(fileName));
			boolean metaDataFound = getImageMetadata(fileName, fileInfo);
			boolean tryFileNameDate;
			if (!metaDataFound) {
				tryFileNameDate = tryFileNameDate(fileName, fileInfo);
				if (!tryFileNameDate) {
					setBad(fileInfo);
					fileInfo.setDate(0);
				}
			}
		} else if (FileUtils.supportedVideo(fileName)) {
			fileInfo = new FileInfo(fileName.toString(), Main.conf.getId_counter().incrementAndGet());
			setVideo(fileInfo);
			fileInfo.setSize(Files.size(fileName));
			boolean metaDataFound = getVideoDateTaken(fileName, fileInfo);
			if (!metaDataFound) {
				fileInfo.setBad(true);
			}
		} else if (FileUtils.supportedRaw(fileName)) {
			fileInfo = new FileInfo(fileName.toString(), Main.conf.getId_counter().incrementAndGet());
			setRaw(fileInfo);
			fileInfo.setSize(Files.size(fileName));
			boolean metaDataFound = getImageMetadata(fileName, fileInfo);
			boolean tryFileNameDate;
			if (!metaDataFound) {
				tryFileNameDate = tryFileNameDate(fileName, fileInfo);
				if (!tryFileNameDate) {
					setBad(fileInfo);
					fileInfo.setDate(0);
				}
			}
		}
		return fileInfo;
	}

	public static boolean getVideoDateTaken(Path path, FileInfo fileInfo) {
		if (!Files.exists(path)) {
			sprintf("File does not exists: " + path + " returning....");
			return false;
		}
		long date = 0;
		if (FileUtils.supportedVideo(path)) {
			setVideo(fileInfo);
			Metadata metaData = readMetaData(path);
			if (metaData != null) {

				date = getMetaDataCreationDate(metaData, path);
				if (date >= 1) {
					FileInfo_Utils.setGood(fileInfo);
					fileInfo.setDate(date);
					getImageThumb_Offset_Length(metaData, fileInfo);
					return true;
				} else {
					FileInfo_Utils.setBad(fileInfo);
					fileInfo.setDate(0);
				}
				// getImageThumb_Offset_Length(metaData, fileInfo);
			}

			boolean externalImageMetaDataFound = getDateThumbFileForVideo(path, fileInfo);
			if (externalImageMetaDataFound) {
				return true;
			} else {
				date = FileNameParseUtils.hasFileNameDate(path);
				if (date >= 1) {
					fileInfo.setDate(date);
					setGood(fileInfo);
					fileInfo.setSuggested(true);
					return true;
				} else {
					return false;
				}
			}
		}
		return false;
	}

	/**
	 * getDateThumbFileForVideo will try to find original thumbnail file e.x some
	 * camera brands records video file called IMG_5555.MOV and it saves
	 * thumbnailfile into same folder as IMG_5555.THM
	 *
	 * @param path
	 * @return
	 */
	public static boolean getDateThumbFileForVideo(Path path, FileInfo fileInfo) {
		if (supportedVideo(path)) {
			Path THM_path = VideoDateFinder.hasTHMFile(path);
			if (THM_path == null) {
				return false;
			}
			if (Files.exists(THM_path)) {
				boolean metaDataFound = getImageMetadata(THM_path, fileInfo);
				if (metaDataFound) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean getImageThumb_Offset_Length(Metadata metaData, FileInfo fileInfo) {
		if (metaData == null) {
			return false;
		}
		ExifThumbnailDirectory directory = metaData.getFirstDirectoryOfType(ExifThumbnailDirectory.class);
		if (directory != null) {
			try {
				int offset = -1;
				int length = -1;
				if (directory.getInteger(ExifThumbnailDirectory.TAG_THUMBNAIL_OFFSET) == null) {
					return false;
				} else {
					offset = directory.getInteger(ExifThumbnailDirectory.TAG_THUMBNAIL_OFFSET);
				}
				if (directory.getInteger(ExifThumbnailDirectory.TAG_THUMBNAIL_LENGTH) == null) {
					return false;
				} else {
					length = directory.getInteger(ExifThumbnailDirectory.TAG_THUMBNAIL_LENGTH);
				}
				if (offset < 0 || length < 0) {
					Messages.sprintfError("offset were: " + offset + " length: " + length);
					return false;
				} else {
					fileInfo.setThumb_offset(offset);
					fileInfo.setThumb_length(length);
					return true;
				}

			} catch (Exception e) {
				fileInfo.setThumb_offset(-1);
				fileInfo.setThumb_length(-1);
				return false;
			}
		}
		return false;
	}

	private static boolean tryFileNameDate(Path path, FileInfo fileInfo) {
		long fileNameDate = FileNameParseUtils.hasFileNameDate(path);
		if (fileNameDate != 0) {
			FileInfo_Utils.setSuggested(fileInfo);
			fileInfo.setDate(fileNameDate);
			return true;
		} else {
			FileInfo_Utils.setBad(fileInfo);
			fileInfo.setDate(0);
			fileNameDate = 0;
			return false;
		}
	}

	public static List<FileInfo> createFileInfo_list(FolderInfo folderInfo) {
		long start = System.currentTimeMillis();
		List<FileInfo> fileInfo_list = new ArrayList<>();
		DirectoryStream<Path> list = null;
		try {
			list = Files.newDirectoryStream(Paths.get(folderInfo.getFolderPath()), filter_directories);
		} catch (IOException ex) {
			Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
		}
		sprintf("newDirStream created took: " + (System.currentTimeMillis() - start));
		for (Path p : list) {
			// start = System.currentTimeMillis();
			if (Main.getProcessCancelled()) {
				// cancel();
				break;
			}
			try {
				if (ValidatePathUtils.validFile(p)) {
					FileInfo fileInfo = null;
					try {
						fileInfo = createFileInfo(p);
					} catch (IOException ex) {
						Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
					}
					if (fileInfo != null) {
						fileInfo_list.add(fileInfo);
					}
				}
			} catch (IOException ex) {
				Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
			}
		}
		sprintf("=======entire createFileInfo_list took: " + (System.currentTimeMillis() - start));
		return fileInfo_list;
	}

	public static void setSuggested(FileInfo fileInfo) {
		fileInfo.setSuggested(true);
		fileInfo.setBad(false);
		fileInfo.setGood(false);
	}

	public static void setGood(FileInfo fileInfo) {
		fileInfo.setSuggested(false);
		fileInfo.setBad(false);
		fileInfo.setGood(true);
		fileInfo.setSuggested(false);
	}

	public static void setConfirmable(FileInfo fileInfo) {
		// srth;
	}

	public static void setBad(FileInfo fileInfo) {
		fileInfo.setSuggested(false);
		fileInfo.setBad(true);
		fileInfo.setGood(false);
		fileInfo.setSuggested(false);
	}

	public static void setVideo(FileInfo fileInfo) {
		fileInfo.setImage(false);
		fileInfo.setRaw(false);
		fileInfo.setVideo(true);
	}

	public static void setImage(FileInfo fileInfo) {
		fileInfo.setImage(true);
		fileInfo.setRaw(false);
		fileInfo.setVideo(false);
	}

	public static void setRaw(FileInfo fileInfo) {
		fileInfo.setImage(false);
		fileInfo.setRaw(true);
		fileInfo.setVideo(false);
	}

	public static void sortByDate_Ascending(List<FileInfo> list) {
		Collections.sort(list, new Comparator<FileInfo>() {
			@Override
			public int compare(FileInfo fileInfo, FileInfo fileInfo2) {
				if (fileInfo.getDate() < fileInfo2.getDate()) {
					return -1;
				} else if (fileInfo.getDate() > fileInfo2.getDate()) {
					return 1;
				} else {
					return 0;
				}
			}
		});

		// TODO Auto-generated method stub
	}

	public static boolean getImageMetadata(Path path, FileInfo fileInfo) {

		long creationDate = 0;
		int orientation = 0;
		int width = 0;
		int height = 0;
		String camera_model = "Unknown";
		Metadata metaData = null;
		fileInfo.setCamera_model(camera_model);
		try {
			metaData = readMetaData(path);
		} catch (Exception e) {
			return false;
		}
		if (metaData != null) {
			creationDate = getMetaDataCreationDate(metaData, path);
			if (creationDate != 0) {
				FileInfo_Utils.setGood(fileInfo);
				fileInfo.setDate(creationDate);
			} else {
				FileInfo_Utils.setBad(fileInfo);
				fileInfo.setDate(0);
			}
			orientation = DateTaken.getMetaDataOrientation(metaData);
			if (orientation != 0) {
				fileInfo.setOrientation(orientation);
			} else {
				fileInfo.setOrientation(0);
			}
			camera_model = DateTaken.getCameraModel(metaData);
			if (camera_model != null) {
				if (!camera_model.isEmpty()) {
					fileInfo.setCamera_model(camera_model);
				}
			}
			boolean imageThumbOffsets = getImageThumb_Offset_Length(metaData, fileInfo);

			if (creationDate != 0) {
				creationDate = 0;
				orientation = 0;
				camera_model = null;
				return true;
			} else {
				creationDate = 0;
				orientation = 0;
				camera_model = null;
				return false;
			}
		}
		creationDate = 0;
		orientation = 0;
		camera_model = null;
		return false;
	}

	public static Path renameFileToDate(Path path, FileInfo fileInfo) {
		// C:\Temp\image.jpg C:\Temp + 2019-09-03 12.31.22.jpg

		Path source = Paths
				.get(path.getParent().toString() + File.separator
						+ DateUtils.longToLocalDateTime(fileInfo.getDate())
								.format(Main.simpleDates.getDtf_ymd_hms_minusDots_default())
						+ "." + FileUtils.getExtension(path));
		Messages.sprintf("source would be after: " + path + " to: " + source);
		return source;
	}

	//@formatter:off
	/**
	 * Returns 0 if good 
	 * Returns 1 if conflict with workdir 
	 * Returns 2 if copying is not possible because fileinfo workDir is null OR drive is not connected
	 *  Returns 3 
	 * @param fileInfo
	 * @return
	 */
	//@formatter:on
	public static int checkWorkDir(FileInfo fileInfo) {
		if (Main.conf.getDrive_connected()) {
			if (!fileInfo.getWorkDir().equals("null")) {
				if (fileInfo.getWorkDir().contains(Main.conf.getWorkDir())
						&& Main.conf.getWorkDirSerialNumber().equals(fileInfo.getWorkDirDriveSerialNumber())) {
					if (Files.exists(Paths.get(fileInfo.getWorkDir()))) {
						return 0;
					} else {
						return 2;
					}
				} else {
					return 1;
				}
			} else {
				return 2;
			}

		} else {
			return 2;
		}
	}


	public static void removeFileInfoFromList(List<FileInfo> fileInfoList, Path p) {

		
	}
}
