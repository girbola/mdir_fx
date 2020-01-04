/*
 @(#)Copyright:  Copyright (c) 2012-2020 All right reserved.
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package common.utils;

import static com.girbola.messages.Messages.sprintf;

import java.io.File;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.jcodec.common.logging.Message;

import com.girbola.messages.Messages;
import com.girbola.misc.Misc;

/**
 *
 * @author Marko
 */
public class FileUtils {

	private final static String[] SUPPORTED_VIDEO_FORMATS = { "3gp", "avi", "mov", "mp4", "mpg", "mkv" };
	private final static String[] SUPPORTED_IMAGE_FORMATS = { "png", "jpg", "jpeg", "gif", "bmp", "tiff", "tif" };
	private final static String[] SUPPORTED_RAW_FORMATS = { "cr2", "nef" };

	private final static String[] IGNORED_FORMATS = { "ini", "db", "exe", "sh", "dll", "sys", "java", "jar" };

	/**
	 * fileSeparator hack replaces file paths "/" separator to "\\" using
	 * replaceAll("/", "\\\\") method
	 *
	 * @param path
	 * @return
	 */
	public static String fileSeparator_mrl(Path path) {
		String value = path.toString();
		String newPath = "";
		if (Misc.isWindows()) {
			newPath = value.replaceAll("/", "\\\\");
		} else {
			newPath = path.toString();
		}
		return newPath;
	}

	//	public static void renameExistingFile(Path orgFilePath) {
	//		String filePath = orgFilePath.getParent().toString();
	//		String fileName = orgFilePath.getFileName().toString();
	//
	//		String extension = getExtension(orgFilePath.getFileName().toString());
	//		File[] list = new File(orgFilePath.getParent().toString()).listFiles();
	//		for (int i = 0; i < list.length; i++) {
	////			String file = filePath + File.separator + fileName + "." + extension;
	//			sprintf("Listing files: " + list[i]);
	//			if (list[i].equals(orgFilePath.toFile())) {
	//				sprintf("File found!");
	//			}
	//		}
	//		sprintf("filePath is: " + filePath + File.separator + fileName + "." + extension);
	//	}

	/**
	 * Rename file to new file name if file exists and it is different size example:
	 * IMG_2000.jpg would be IMG_2000_1.jpg or IMG_2000_2.jpg etc
	 *
	 * @param srcFile
	 * @param destFile
	 * @return
	 * @throws java.io.IOException
	 */
	public static Path renameFile(Path srcFile, Path destFile) throws IOException {

		String prefix = "_";
		String fileName = "";
		String ext = getExtension(destFile);
		// sprintf("src= " + srcFile + " size " + Files.size(srcFile) + " dest "
		// +
		// destFile + " size " + Files.size(destFile));
		if (Files.exists(destFile) && Files.size(destFile) != Files.size(srcFile)) {
			sprintf("file name exists but they are different size: ");
			File[] fileList = destFile.getParent().toFile().listFiles();
			for (int i = 1;
					i < fileList.length + 1;
					i++) {
				fileName = destFile.getParent().toString() + File.separator + (destFile.getFileName().toString().substring(0, destFile.getFileName().toString().lastIndexOf("."))) + prefix + i + "."
						+ ext;
				sprintf("fileName testing starting: " + i + " fileName: " + fileName);
				if (Files.exists(Paths.get(fileName))) {
					sprintf("--->File name exists: " + fileName + "size is: " + Files.size(Paths.get(fileName)) + "dest : " + destFile + " size: " + Files.size(destFile));
					if (Files.size(srcFile) == Files.size(Paths.get(fileName))) {
						sprintf("File existed!: " + destFile + " filename: " + fileName);
						return null;
					}
				} else {
					return Paths.get(fileName);
				}
			}
		} else {
			Messages.sprintf("file did exists at destination folder");
			return null;
		}
		sprintf("REturning destfile: " + destFile);
		return destFile;
	}

	public static DirectoryStream.Filter<Path> filter_directories = new DirectoryStream.Filter<Path>() {
		@Override
		public boolean accept(Path path) throws IOException {
			return !Files.isDirectory(path) && supportedMediaFormat(path.toFile()); // Failed to determine if it's a directory.
		}
	};

	/* FILE FORMATS START */
	/**
	 * Checks if file supports image, raw or video formats
	 *
	 * @param file
	 * @return
	 */
	public static boolean supportedMediaFormat(File file) {
		String result;
		result = getExtension(file.getName());
		// sprintf("extension result is: " +result);
		for (String s : SUPPORTED_VIDEO_FORMATS) {
			if (result.toLowerCase().equals(s.toLowerCase())) {
				return true;
			}
		}
		for (String s : SUPPORTED_IMAGE_FORMATS) {
			if (result.toLowerCase().equals(s.toLowerCase())) {
				return true;
			}
		}

		for (String s : SUPPORTED_RAW_FORMATS) {
			if (result.toLowerCase().equals(s.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	/* FILE FORMATS START */
	public static boolean supportedImage(File file) {
		String result = getExtension(file.getName());
		for (String s : SUPPORTED_IMAGE_FORMATS) {
			if (result.toLowerCase().equals(s.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	public static boolean supportedImage(Path path) {
		String result = getExtension(path.getFileName());
		for (String s : SUPPORTED_IMAGE_FORMATS) {
			if (result.toLowerCase().equals(s.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	public static boolean isTiff(File file) {
		String result;
		result = getExtension(file.getName());
		if (result.toLowerCase().equals("tiff".toLowerCase()) || result.toLowerCase().equals("tif".toLowerCase())) {
			return true;
		}
		return false;
	}

	public static boolean ignoredFormats(File file) { // PNG, JPEG, BMP
		String result = getExtension(file.getName());
		for (String s : IGNORED_FORMATS) {
			if (result.toLowerCase().equals(s.toLowerCase())) {
				return true;
			}
		}

		return false;
	}

	public static boolean supportedRaw(File file) { // PNG, JPEG, BMP
		String result;
		result = getExtension(file.getName());
		for (String s : SUPPORTED_RAW_FORMATS) {
			if (result.toLowerCase().equals(s.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	public static boolean supportedRaw(Path path) { // PNG, JPEG, BMP
		String result = getExtension(path.getFileName());
		for (String s : SUPPORTED_RAW_FORMATS) {
			if (result.toLowerCase().equals(s.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	public static boolean otherFormat(File file) {
		String result;
		result = getExtension(file.getName());
		if (result.equals("ini") || result.equals("lnk") || result.equals("db")) {
			return false;
		} else {
			return true;
		}
	}

	public static boolean otherImageFormats(File file) {
		String result;
		result = getExtension(file.getName());
		if (result.equals("")) {
			return true;
		}
		return false;
	}

	public static boolean supportedVideo(Path path) {
		String result = "";
		result = getExtension(path);
		for (String s : SUPPORTED_VIDEO_FORMATS) {
			if (result.toLowerCase().equals(s.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	public static boolean nonsupportedVideoThumb(File file) {
		String result;
		result = getExtension(file.getName());
		if (result.equals("3gp")) {
			return true;
		}
		return false;
	}

	public static boolean videoFormat(File file) {
		String result;
		result = getExtension(file.getName());
		if (result.equals("3g2") || result.equals("3gp") || result.equals("asf") || result.equals("asx") || result.equals("avi") || result.equals("flv") || result.equals("mov") || result.equals("mp4")
				|| result.equals("mpg") || result.equals("rm") || result.equals("swf") || result.equals("vob") || result.equals("wmv") || result.equals("mkv")) {
			return true;
		}
		return false;
	}

	public static boolean allFormats(File file) {

		if (supportedMediaFormat(file)) {
			return true;
		} else if (supportedRaw(file)) {
			return true;
		} else if (videoFormat(file)) {
			return true;
		}
		return false;
	}

	/**
	 * Returns file extension name in lowercase
	 *
	 * @param path
	 * @return
	 */
	public static String getExtension(Path path) {
		int mid = path.getFileName().toString().lastIndexOf(".");
		return path.getFileName().toString().substring(mid + 1, path.getFileName().toString().length()).toLowerCase();
		// return ext;
	}

	/**
	 * Returns file extension name in original casesensitiveness
	 *
	 * @param path
	 * @return
	 */
	public static String getFileExtension(Path path) {
		// int mid =
		return path.getFileName().toString().substring(path.getFileName().toString().lastIndexOf(".") + 1, path.getFileName().toString().length());
		// return ext;
	}

	/**
	 * Returns file extension name in lowercase
	 *
	 * @param fileName
	 * @return
	 */
	public static String getExtension(String fileName) {
		File file = new File(fileName);
		String sourceFileName = file.getName();
		int mid = sourceFileName.lastIndexOf(".");
		String ext = sourceFileName.substring(mid + 1, sourceFileName.length()).toLowerCase();
		// String result = ext.toLowerCase();

		return ext;
	}

	/**
	 * Returns just filename without extension e.x. C:\Temp\IMG_1234.JPG to IMG_1234
	 *
	 * @param path
	 * @return
	 */
	public static String parseExtension(Path path) {
		String file = path.getFileName().toString();
		int mid = file.lastIndexOf(".");
		String finalName = file.substring(0, mid);
		sprintf("parseExtension fileName: " + finalName);
		return finalName;
	}

	/**
	 * Compare files if they are same. Checking also if file size are different and
	 * file paths aren't the same
	 *
	 * @param source
	 * @param destination
	 * @return
	 * @throws IOException
	 */
	public static final boolean compareFiles(final Path source, final Path destination) throws IOException {
		if (Files.size(source) != Files.size(destination) && !source.equals(destination)) {
			return false;
		}
		final long size = Files.size(source);
		final int mapSpan = (4 * 1024 * 1024);

		try (FileChannel chana = (FileChannel) Files.newByteChannel(source);
				FileChannel chanb = (FileChannel) Files.newByteChannel(destination)) {

			for (long position = 0;
					position < size;
					position += mapSpan) {
				MappedByteBuffer mba = mapChannel(chana, position, size, mapSpan);
				MappedByteBuffer mbb = mapChannel(chanb, position, size, mapSpan);
				if (mba.compareTo(mbb) != 0) {
					return false;
				}
			}
		}
		return true;
	}

	private static MappedByteBuffer mapChannel(FileChannel channel, long position, long size, int mapspan) throws IOException {
		final long end = Math.min(size, position + mapspan);
		final long maplen = (int) (end - position);
		return channel.map(FileChannel.MapMode.READ_ONLY, position, maplen);
	}

	/**
	 * Replaces from filepath a workdir to none. For example c:\pictures\2019\09\pictures001.jpg to \2019\09\pictures001.jpg
	 * 
	 * @param dest
	 * @param workDir
	 */
	public static String parseWorkDir(String dest, String workDir) {
		String parsedWorkDir_FileName = dest.replace(workDir, "");
		Messages.sprintf("parseWorkDir is: " + parsedWorkDir_FileName);
		return parsedWorkDir_FileName;
	}

}
