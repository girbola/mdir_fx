/*
 @(#)Copyright:  Copyright (c) 2012-2024 All right reserved.
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.filelisting;

import com.girbola.Main;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import javafx.concurrent.Task;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.girbola.messages.Messages.errorSmth;
import static com.girbola.messages.Messages.sprintf;
import static com.girbola.misc.Misc.getLineNumber;

public class GetSubFolders extends Task<List<Path>> {
	private Path path;

	GetSubFolders(Path path) {
		this.path = path;
	}

	private static final String ERROR = GetSubFolders.class.getSimpleName();

	@Override
	protected List<Path> call() throws Exception {

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