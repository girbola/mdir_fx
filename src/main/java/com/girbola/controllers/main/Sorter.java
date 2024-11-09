/*
 @(#)Copyright:  Copyright (c) 2012-2024 All right reserved.
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.main;

import com.girbola.Main;
import com.girbola.controllers.main.tasks.AddToTable;
import com.girbola.messages.Messages;
import javafx.concurrent.Task;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.girbola.concurrency.ConcurrencyUtils.exec;
import static com.girbola.concurrency.ConcurrencyUtils.getExecCounter;
import static com.girbola.messages.Messages.sprintf;

/**
 *
 * @author Marko Lokka
 */
public class Sorter extends Task<Integer> {

	private List<Path> selectedList;
	private ModelMain model;
	private AtomicInteger counter = new AtomicInteger(0);

	public Sorter(ModelMain model, List<Path> selectedList) {
		this.model = model;
		this.selectedList = selectedList;
	}

	@Override
	protected Integer call() throws Exception {
		if (!selectedList.isEmpty()) {
			for (Path folder : selectedList) {
				sprintf("Adding folder: " + folder);
				if (Main.getProcessCancelled()) {
					Messages.sprintf("Sorter process were cancelled");
					exec[getExecCounter()].shutdownNow();
					break;
				}
				Task<Integer> addToTable = new AddToTable(folder, model);
				addToTable.setOnSucceeded(e -> Messages.sprintf("Sorter addToTable Sorter done! " + folder));
				addToTable.setOnFailed(e -> Messages. sprintf("Sorter addToTable.setOnFailed: " + folder));
				addToTable.setOnCancelled(e -> Messages.sprintf("Sorter addToTable.setOnCancelled: " + folder));

				counter.incrementAndGet();

				exec[getExecCounter()].submit(addToTable);
			}
		} else {
			sprintf("list was empty!");
		}

		return counter.get();
	}
}
