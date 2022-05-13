/*
 @(#)Copyright:  Copyright (c) 2012-2020 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.filelisting;

import static com.girbola.messages.Messages.errorSmth;
import static com.girbola.misc.Misc.getLineNumber;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.girbola.messages.Messages;
import com.girbola.misc.Misc;

import common.utils.FileUtils;

/**
 *
 * @author Marko Lokka
 */
public class ValidatePathUtils {

	private final static String ERROR = ValidatePathUtils.class.getSimpleName();
	private static long FILE_MIN_SIZE = (1 * 1024);

	public final static String[] skippedFolderList_UNIX = {"bin","dev","lib","libx32","root","snap","swapfile","usr","boot","etc","lib32","lost+found","opt","run","some","sys","var","cdrom","lib64","media","proc","sbin","srv" };
	public final static String[] skippedFolderList_WIN = { "$SysReset", "$Recycle.Bin", "RECYCLER", ".Trash", "Android", "AppData", "Boot", "Default", "Efi", "Intel", "Java", "NetBeansProjects", "OEM", "PerfLogs", "Program Files (x86)", "Program Files", "ProgramData", "Recycle", "Resource",
			"System Volume Information", "Windows", "source" };

	public static boolean hasMediaFilesInFolder(Path path) {
		DirectoryStream<Path> directoryStream = null;
		try {
			directoryStream = Files.newDirectoryStream(path, FileUtils.filter_directories);
		} catch (IOException ex) {
			Messages.errorSmth(ERROR, "" , ex,Misc.getLineNumber(), true);
		}
		for (Path file : directoryStream) {
			try {
				if (ValidatePathUtils.validFile(file)) {
					return true;
				}
			} catch (IOException ex) {
				Messages.errorSmth(ERROR, "" , ex,Misc.getLineNumber(), true);
				// Logger.getLogger(ValidatePathUtils.class.getName()).log(Level.SEVERE, null,
				// ex);
			}
		}
		return false;
	}

	public static boolean isRootDisk(Path path) {
		if (path.getRoot().equals(Paths.get(System.getProperty("user.home")).getRoot())) {
			return true;
		}
		return false;
	}

	/**
	 * isInSkippedFolderList check if current folder is in skipped list
	 *
	 * @param file
	 * @return
	 */
	public static boolean isInSkippedFolderList(Path file) {
		if (Misc.isWindows()) {
			if (file.getFileName().toString().substring(0, 1).equals(".")) {
				return true;
			}
			if (file.getFileName().toString().toLowerCase().contains("app")) {
				return true;
			}
			for (String list : skippedFolderList_WIN) {
				if (file.getFileName().toString().toLowerCase().equals(list.toLowerCase())) {
					return true;
				}
			}
			return false;
		} else if (Misc.isUnix()) {
			for (String list : skippedFolderList_UNIX) {
				if (file.getFileName().toString().toLowerCase().equals(list.toLowerCase())) {
					return true;
				}
			}
			return false;
		} else {
			String os = "isWindows: " + Misc.isWindows() + " UNIX: " + Misc.isUnix() + " Mac: " + Misc.isMac() + " UNIII: " + System.getProperty("os.name");
			Messages.sprintf("OS: " + os);
			errorSmth(ERROR, os, null, getLineNumber(), true);
		}

		return false;
	}

	public static boolean validFile(Path f) throws IOException {
		return Files.isReadable(f) && !Files.isHidden(f) && Files.size(f) > FILE_MIN_SIZE && Files.exists(f) && FileUtils.supportedMediaFormat(f.toFile());
	}

	public static boolean validFolder(Path f) throws IOException {
		return Files.isDirectory(f) && Files.exists(f) && Files.isReadable(f) && !Files.isHidden(f) && !isInSkippedFolderList(f);
	}

}
