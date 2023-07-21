/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.filelisting;

import com.girbola.Main;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.girbola.messages.Messages.sprintf;
import static java.nio.file.FileVisitResult.*;

/**
 * List only media files in root folder. Subfolder scan will be ignored 
 * @author marko_000
 *
 */
public class GetRootFiles {
//	private final static String ERROR = GetRootFiles.class.getSimpleName();

	public static List<Path> getRootFiles(Path path) throws IOException {
		Main.setProcessCancelled(false);
		sprintf("getRootFiles started: " + path);
		RootFiles fv = new RootFiles(path);

		List<Path> foundFiles = fv.foundPaths;

		Files.walkFileTree(path, fv);

		if (Main.getProcessCancelled()) {
			sprintf("getRootFiles has cancelled and terminated");
			return null;
		}

		return foundFiles;
	}
}

class RootFiles extends SimpleFileVisitor<Path> {
	private final static String ERROR = RootFiles.class.getSimpleName();

	private Path orgPath;

	public List<Path> foundPaths = new ArrayList<>();

	public RootFiles(Path orgPath) {
		this.orgPath = orgPath;
	}

	@Override
	public FileVisitResult postVisitDirectory(Path t, IOException ioe) throws IOException {
		if (Main.getProcessCancelled()) {
			sprintf("getRootFiles has cancelled and terminated");
			return TERMINATE;
		}
		return SKIP_SUBTREE;

	}

	@Override
	public FileVisitResult preVisitDirectory(Path t, BasicFileAttributes bfa) {
		if (orgPath.equals(t)) {
			return CONTINUE;
		}
		if (Main.getProcessCancelled()) {
			sprintf("getRootFiles has cancelled and terminated");
			return TERMINATE;
		}
		return SKIP_SUBTREE;
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {

		try {
			if (ValidatePathUtils.validFile(file)) {
				foundPaths.add(file);
			} else {

			}
		} catch (IOException ex) {
			Logger.getLogger("Actual class is GetRootFiles" + RootFiles.class.getName()).log(Level.SEVERE, null, ex);
			Messages.errorSmth(ERROR, "FileVisitResult caused an error with file: " + file + "\n\n", ex, Misc.getLineNumber(), true);
			return SKIP_SIBLINGS;
		}
		if (Main.getProcessCancelled()) {
			sprintf("getRootFiles has cancelled and terminated");
			return TERMINATE;
		}
		return CONTINUE;
	}

	// If there is some error accessing
	// the file, let the user know.
	// If you don't override this method
	// and an error occurs, an IOException
	// is thrown.
	@Override
	public FileVisitResult visitFileFailed(Path file, IOException exc) {
		System.err.println(exc);
		return CONTINUE;
	}

}
