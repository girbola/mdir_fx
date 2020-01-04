/*
 @(#)Copyright:  Copyright (c) 2012-2020 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.main.tasks;

import static com.girbola.Main.conf;
import static com.girbola.messages.Messages.sprintf;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.girbola.Main;
import com.girbola.concurrency.ConcurrencyUtils;
import com.girbola.controllers.main.Model_main;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.filelisting.ValidatePathUtils;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;

import common.utils.FileUtils;
import javafx.concurrent.Task;
import javafx.scene.control.TableView;

/**
 *
 * @author Marko Lokka
 */
public class AddToTable extends Task<Integer> {

	private final String ERROR = AddToTable.class.getSimpleName();
	private List<Path> list;
	private Model_main model;
	private AtomicInteger counter = new AtomicInteger(0);

	public AddToTable(Path path, Model_main model) {
		this.list = new ArrayList<>();
		this.list.add(path);
		this.model = model;
	}

	public AddToTable(List<Path> list, Model_main model) {
		this.list = list;
		this.model = model;
	}

	@Override
	protected Integer call() throws Exception {
		for (Path p : list) {
			if (Main.getProcessCancelled()) {
				cancel();
				ConcurrencyUtils.stopExecThreadNow();
				break;
			}
			// TODO Tämä uusiksi prkl!

			if (folderHasFiles(p)) {
				TableType tableType = resolvePath(p);
				switch (tableType) {
				case SORTED: {
					FolderInfo folderInfo = new FolderInfo(p);
					if (!hasDuplicates(model.tables().getSorted_table(), folderInfo)
							|| !hasDuplicates(model.tables().getSortIt_table(), folderInfo)) {
						folderInfo.setTableType(TableType.SORTED.getType());
						model.tables().getSorted_table().getItems().add(folderInfo);
						TableUtils.refreshTableContent(model.tables().getSorted_table());
						counter.set(counter.get() + 1);
						sprintf("sorted: " + p + " c= " + counter.get());
					}
					break;
				}
				case SORTIT: {
					FolderInfo folderInfo = new FolderInfo(p);
					if (hasDuplicates(model.tables().getSortIt_table(), folderInfo)
							|| !hasDuplicates(model.tables().getSorted_table(), folderInfo)) {
						folderInfo.setTableType(TableType.SORTIT.getType());
						model.tables().getSortIt_table().getItems().add(folderInfo);
						counter.set(counter.get() + 1);
						sprintf("sortit: " + p + " c= " + counter.get());
						TableUtils.refreshTableContent(model.tables().getSortIt_table());
					}
					break;
				}
				default:
					sprintf("Can't find specific place to put this folder: " + p);
					break;
				}
				tableType = null;
			}
		}

		return counter.get();
	}

	private boolean hasDuplicates(TableView<FolderInfo> table, FolderInfo folderInfo) {
		for (FolderInfo src_folderInfo : table.getItems()) {
			if (src_folderInfo.getFolderPath().equals(folderInfo.getFolderPath())) {
				return true;
			}
		}
		return false;

	}

	private boolean folderHasFiles(Path p) {
		if (isIgnoredList(p)) {
			return false;
		}
		DirectoryStream<Path> list = null;
		try {
			list = Files.newDirectoryStream(p, FileUtils.filter_directories);
		} catch (IOException ex) {
			Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
		}

		for (Path path : list) {
			try {
				if (ValidatePathUtils.validFile(path) && FileUtils.supportedMediaFormat(path.toFile())) {
					return true;
				}
			} catch (IOException ex) {
				Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
			}
		}
		return false;
	}

	private boolean isIgnoredList(Path path) {
		for (Path file : conf.getIgnoredFoldersScanList()) {
			if (file.equals(path)) {
				return true;
			}
		}
		return false;
	}

	private TableType resolvePath(Path p) {
		// sprintf("REGULAR EXPRESSIONS STARTED");

		int numberTotal = 0;
		int letterTotal = 0;
		int characterTotal = 0;
		int spaceCount = 0;

		String path = p.getFileName().toString();
		if (path.contains("Pictures") || path.contains("Videos")) {
			return TableType.SORTIT;
		}
		for (int i = 0; i < path.length(); i++) {
			char c = path.charAt(i);
			if (Character.isLetter(c)) {
				letterTotal++;
			} else if (Character.isDigit(c)) {
				numberTotal++;
			} else if (c == ' ') {
				spaceCount++;
			} else {
				characterTotal++;
			}
		}

		/*
		 * Lisää 2014-12-11 ja 2012_12_05
		 */

		// 100Canon jne
		if (numberTotal == 3 && letterTotal == 5 && characterTotal == 0 && spaceCount == 0) { // The
			// most
			// common
			// format
			// 123Canon
			return TableType.SORTIT;

			// O'layreys pub 2013
		} else if (numberTotal >= 0 && letterTotal >= 1 && characterTotal >= 0 && spaceCount >= 0) { // Just
			// letters
			return TableType.SORTED;
		} else if (numberTotal >= 1 && letterTotal == 0 && characterTotal == 0 && spaceCount == 0) { // 1-9
			// numbers
			return TableType.SORTIT;
		} else {
			return TableType.SORTIT;
		}
	}
}
