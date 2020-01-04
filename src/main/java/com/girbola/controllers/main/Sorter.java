/*
 @(#)Copyright:  Copyright (c) 2012-2020 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.main;

import static com.girbola.concurrency.ConcurrencyUtils.exec;
import static com.girbola.concurrency.ConcurrencyUtils.getExecCounter;
import static com.girbola.messages.Messages.sprintf;

import java.nio.file.Path;
import java.util.List;

import com.girbola.Main;
import com.girbola.controllers.main.tasks.AddToTable;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

/**
 *
 * @author Marko Lokka
 */
public class Sorter extends Task<Integer> {

	private List<Path> selectedList;
	private Model_main model;
	private IntegerProperty counter = new SimpleIntegerProperty(0);

	public Sorter(Model_main model, List<Path> selectedList) {
		this.model = model;
		this.selectedList = selectedList;
	}

	@Override
	protected Integer call() throws Exception {
		if (!selectedList.isEmpty()) {
			for (Path folder : selectedList) {
				// sprintf("Adding folder: " + folder);
				if (Main.getProcessCancelled()) {
					exec[getExecCounter()].shutdownNow();
					break;
				}
				Task<Integer> addToTable = new AddToTable(folder, model);
				addToTable.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
					@Override
					public void handle(WorkerStateEvent event) {
						// sprintf("Sorter done!");
					}
				});
				addToTable.setOnFailed(new EventHandler<WorkerStateEvent>() {
					@Override
					public void handle(WorkerStateEvent event) {
						sprintf("addToTable.setOnFailed: " + folder);
					}
				});
				addToTable.setOnCancelled(new EventHandler<WorkerStateEvent>() {
					@Override
					public void handle(WorkerStateEvent event) {
						sprintf("addToTable.setOnCancelled: " + folder);
					}
				});
				counter.set(counter.get() + 1);

				exec[getExecCounter()].submit(addToTable);
			}
		} else {
			sprintf("list was empty!");
		}

		return counter.get();
	}
}
