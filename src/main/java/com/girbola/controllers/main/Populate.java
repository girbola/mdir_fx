/*
 @(#)Copyright:  Copyright (c) 2012-2019 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.main;

import static com.girbola.concurrency.ConcurrencyUtils.exec;
import static com.girbola.concurrency.ConcurrencyUtils.getExecCounter;
import static com.girbola.messages.Messages.sprintf;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import com.girbola.Main;
import com.girbola.concurrency.ConcurrencyUtils;
import com.girbola.controllers.folderscanner.SelectedFolder;
import com.girbola.controllers.loading.LoadingProcess_Task;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

/**
 *
 * @author Marko Lokka
 */
public class Populate {

	private static final String ERROR = Populate.class.getSimpleName();

	private Model_main model_main;

	private IntegerProperty total = new SimpleIntegerProperty();

	private AtomicInteger process = new AtomicInteger(0);

	public Populate(Model_main model) {
		this.model_main = model;
		ConcurrencyUtils.initExecutionService();
	}

	public void populateTables_FolderScanner_list() {
		sprintf("SorterTest action started");
		Main.setProcessCancelled(false);

		model_main.tables().getSortIt_table().getItems().clear();
		model_main.tables().getSorted_table().getItems().clear();

		if (model_main.getSelectedFolders().getSelectedFolderScanner_obs().isEmpty()) {
			sprintf("getSelection_FolderScanner list were empty");
		}
		/*
		 * Load from selectedFolder list Sort to tables Calculate tables content
		 */
		List<Path> list = new ArrayList<>();
		for (SelectedFolder sf : model_main.getSelectedFolders().getSelectedFolderScanner_obs()) {
			if (!hasInIgnoredListMain(Main.conf.getIgnoredFoldersScanList(), sf.getFolder())) {
				if (sf.isConnected()) {
					list.add(Paths.get(sf.getFolder()));
					sprintf("Path is: " + sf + " isConnected: " + sf.isConnected());
				}
			}
		}
		if (list.isEmpty()) {
			Messages.warningText("List is empty at Populate class");
			return;
		}
		for (Path pt : list) {
			sprintf("SelectedFolder is: " + pt);
		}
		Task<List<Path>> createFileList = new SubList(list);
		LoadingProcess_Task loadingProcess_task = new LoadingProcess_Task();
		createFileList.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				List<Path> list = null;
				try {
					list = createFileList.get();
				} catch (InterruptedException ex) {
					Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
				} catch (ExecutionException ex) {
					Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
				}
				if (list.isEmpty()) {
					Messages.warningText("List is empty at Populate class");
					Main.setProcessCancelled(true);
					return;
				}
				Task<Integer> sorter = new Sorter(model_main, list);
				loadingProcess_task.setTask(sorter);
				sorter.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
					@Override
					public void handle(WorkerStateEvent event) {
						total.set(model_main.tables().getAsItIs_table().getItems().size()
								+ model_main.tables().getSortIt_table().getItems().size()
								+ model_main.tables().getSorted_table().getItems().size());
						process.set(total.get());
						sprintf("sorter.setOnSucceeded total: " + total);
						loadingProcess_task.setTask(sorter);
						loadingProcess_task.setMessage("Sorter");
						
						Task<Void> calculateFolderContent = new CalculateFolderContent(model_main, loadingProcess_task, total);
						loadingProcess_task.setTask(calculateFolderContent);
						calculateFolderContent.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
							@Override
							public void handle(WorkerStateEvent event) {
								try {
									process.set(process.get() - 1);
									sprintf("(double) process.get() / (double) total.get()); " + (double) process.get() / (double) total.get());
									sprintf("Saving...");
									loadingProcess_task.setMessage("Saving...");
									// XMLFunctions.saveAll(model.getTables());
									loadingProcess_task.closeStage();
									sprintf("calculateFolderContent setOnSucceeded: " + sorter.get());
								} catch (InterruptedException | ExecutionException ex) {
									Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
								}
							}
						});
						calculateFolderContent.setOnCancelled(new EventHandler<WorkerStateEvent>() {
							@Override
							public void handle(WorkerStateEvent event) {
								sprintf("calculateFolderContent setOnCancelled");
							}
						});
						calculateFolderContent.setOnFailed(new EventHandler<WorkerStateEvent>() {
							@Override
							public void handle(WorkerStateEvent event) {
								sprintf("calculateFolderContent setOnFailed");
								Messages.errorSmth(ERROR, "", null, Misc.getLineNumber(), true);
							}
						});
						exec[getExecCounter()].submit(calculateFolderContent);
					}
				});
				sorter.setOnCancelled(new EventHandler<WorkerStateEvent>() {
					@Override
					public void handle(WorkerStateEvent event) {
						sprintf("sorter.setOnCancelled");

					}
				});
				sorter.setOnFailed(new EventHandler<WorkerStateEvent>() {
					@Override
					public void handle(WorkerStateEvent event) {
						sprintf("sorter.setOnFailed");
					}
				});
				Thread sorter_th = new Thread(sorter, "sorter_th");
				sprintf("sorter_th: " + sorter_th.getName());
				sorter_th.start();
			}
		});
		Thread createFileList_th = new Thread(createFileList, "createFileList_th");
		sprintf("createFileList_th.getName(): " + createFileList_th.getName());
		createFileList_th.start();
	}

	private boolean hasInIgnoredListMain(ObservableList<Path> ignoredList, String path) {
		for (Path p : ignoredList) {
			if (p.toString().equals(path)) {
				return true;
			}
		}
		return false;
	}
}
