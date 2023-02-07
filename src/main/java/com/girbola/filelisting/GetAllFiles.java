/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.filelisting;

import static com.girbola.messages.Messages.sprintf;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;

/**
 *
 * @author Marko Lokka
 */
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import com.girbola.messages.Messages;
import com.girbola.misc.Misc;

public class GetAllFiles {
	private final static String ERROR = GetAllFiles.class.getSimpleName();

	public static List<Path> getAllFiles(Path path) {

		AllFiles fv = new AllFiles();

		List<Path> foundFiles = fv.foundPaths;

		try {
			Files.walkFileTree(path, fv);
		} catch (IOException ex) {
			Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
		}
		return foundFiles;
	}
}

class AllFiles extends SimpleFileVisitor<Path> {

	public List<Path> foundPaths = new ArrayList<>();

	@Override
	public FileVisitResult postVisitDirectory(Path t, IOException ioe) throws IOException {
		return CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(Path t, IOException ioe) throws IOException {
		sprintf("DEBUG - getallfiles visitFilefailed getallfiles: " + t);
		return FileVisitResult.SKIP_SIBLINGS;
	}

	@Override
	public FileVisitResult preVisitDirectory(Path t, BasicFileAttributes bfa) throws IOException {
		return CONTINUE;
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
		try {
			if (ValidatePathUtils.validFile(file)) {
				foundPaths.add(file);
			}
		} catch (IOException ex) {
			sprintf("DEBUG - IOException: " + file);
			return SKIP_SUBTREE;
		}
		return CONTINUE;
	}
}
