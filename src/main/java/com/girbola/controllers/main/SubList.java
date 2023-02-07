/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.main;

import static com.girbola.messages.Messages.sprintf;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.girbola.Main;
import com.girbola.filelisting.ValidatePathUtils;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;

/**
 *
 * @author Marko Lokka
 */
public class SubList extends Task<List<Path>> {
	private final String ERROR = SubList.class.getSimpleName();
	private List<Path> selectedFolderScanner_list;

	private static List<Path> list = new ArrayList<>();

	public SubList(ObservableList<Path> selectedFolderScanner_list) {
		this.selectedFolderScanner_list = selectedFolderScanner_list;
		// list.addAll(selectedFolderScanner_list);
	}

	public SubList(List<Path> selectedFolderScanner_list) {
		this.selectedFolderScanner_list = selectedFolderScanner_list;
	}

	private static void calculate(Path p) throws IOException {
		sprintf("SubList - calculate: " + p);
		DirectoryStream<Path> ds = Files.newDirectoryStream(p);
		for (Path path : ds) {
			if(Main.getProcessCancelled()) {
				break;
			}
			if (ValidatePathUtils.validFolder(path)) {
				sprintf("----calculating: " + path);
				if (!list.contains(path)) {
					list.add(path);
					calculate(path);
				}
			}
		}
	}

	@Override
	protected List<Path> call() throws Exception {
		for (Path p : selectedFolderScanner_list) {
			if(Main.getProcessCancelled()) {
				break;
			}
			if (ValidatePathUtils.hasMediaFilesInFolder(p)) {
				list.add(p);
			}
			try {
				calculate(p);
			} catch (IOException ex) {
				Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
			}
		}
		return list;
	}
}
