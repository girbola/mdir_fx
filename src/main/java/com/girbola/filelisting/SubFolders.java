package com.girbola.filelisting;
/*
@(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
@(#)Author:     Marko Lokka
@(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
@(#)Purpose:    To help to organize images and video files in your harddrive with less pain
*/

import static com.girbola.messages.Messages.errorSmth;
import static com.girbola.messages.Messages.sprintf;
import static com.girbola.misc.Misc.getLineNumber;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.TERMINATE;

/**
*
* @author Marko
*/
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import com.girbola.Main;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;

public class SubFolders {

	private static final String ERROR = SubFolders.class.getSimpleName();

	public static List<Path> subFolders(Path path) {

//		sprintf("getSubFolders started: " + path);
		if (Main.getProcessCancelled()) {
			return null;
		}
		if (!Files.exists(path)) {
			errorSmth(ERROR, "", null, getLineNumber(), true);
		}

		Folders subFolders = new Folders();

		List<Path> foundFiles = subFolders.foundPaths;

		if (!Files.exists(path) || path == null || path.toString().length() <= 0) {
			Messages.errorSmth(ERROR, "", null, Misc.getLineNumber(), true);
		}
//		sprintf("walkfiletree path: " + path);
		try {
			Files.walkFileTree(path, subFolders);
		} catch (Exception ex) {
			sprintf("walking file tree: " + ex + " path: " + path);
			Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
		}
		if (foundFiles.isEmpty()) {
			errorSmth(ERROR, "", null, getLineNumber(), true);
		}
		return foundFiles;

	}

}

class Folders extends SimpleFileVisitor<Path> {

	public ArrayList<Path> foundPaths = new ArrayList<>();

	@Override
	public FileVisitResult postVisitDirectory(Path t, IOException ioe) throws IOException {
		if (ValidatePathUtils.validFolder(t)) {
			foundPaths.add(t);
			return CONTINUE;
		} else {
			return FileVisitResult.SKIP_SIBLINGS;
		}
	}

	@Override
	public FileVisitResult visitFileFailed(Path t, IOException ioe) throws IOException {
		if (Main.getProcessCancelled()) {
			return FileVisitResult.TERMINATE;
		}
		return CONTINUE;
	}

	@Override
	public FileVisitResult preVisitDirectory(Path t, BasicFileAttributes bfa) throws IOException {
		if (Main.getProcessCancelled()) {
			return FileVisitResult.TERMINATE;
		}
		return CONTINUE;
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
		if (Main.getProcessCancelled()) {
			return TERMINATE;
		}
		return FileVisitResult.CONTINUE;
	}
}
