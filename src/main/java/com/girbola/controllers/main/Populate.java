/*
 @(#)Copyright:  Copyright (c) 2012-2024 All right reserved.
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.main;

import com.girbola.Main;
import com.girbola.concurrency.ConcurrencyUtils;
import com.girbola.controllers.folderscanner.SelectedFolder;
import com.girbola.controllers.folderscanner.SelectedFolderUtils;
import com.girbola.controllers.loading.LoadingProcessTask;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.stage.Window;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static com.girbola.concurrency.ConcurrencyUtils.exec;
import static com.girbola.concurrency.ConcurrencyUtils.getExecCounter;
import static com.girbola.messages.Messages.sprintf;

/**
 * @author Marko Lokka
 */
public class Populate {

    private static final String ERROR = Populate.class.getSimpleName();

    private Model_main model_main;

    private IntegerProperty total = new SimpleIntegerProperty();

    private AtomicInteger processAtomicInteger = new AtomicInteger(0);

    public Populate(Model_main model) {
        this.model_main = model;
        Messages.sprintf("Populate initialized");
    }

    public void populateTables_FolderScanner_list(Window owner) {
        sprintf("SorterTest action started");
        Main.setProcessCancelled(false);
        model_main.getMonitorExternalDriveConnectivity().cancel();
        if (model_main.getSelectedFolders().getSelectedFolderScanner_obs().isEmpty()) {
            sprintf("getSelection_FolderScanner list were empty");
            return;
        }

        /*
         * Load from selectedFolder list Sort to tables Calculate tables content
         */
        List<Path> selectedFolders = new ArrayList<>();
        for (SelectedFolder sf : model_main.getSelectedFolders().getSelectedFolderScanner_obs()) {
            if (!hasInIgnoredListMain(Main.conf.getIgnoredFoldersScanList(), sf.getFolder()) && sf.isSelected()) {
                if (sf.isConnected()) {
                    boolean selectedFolderExists = SelectedFolderUtils.tableHasFolder(model_main.tables(), Paths.get(sf.getFolder()));
                    if(!selectedFolderExists) {
                        selectedFolders.add(Paths.get(sf.getFolder()));
                        sprintf("! selectedFolderExists Path is: " + sf.getFolder() + " isConnected: " + sf.isConnected());
                    }
                }
            }
        }
        if (selectedFolders.isEmpty()) {
            /*Messages.warningText("Some folder(s) did exists already");*/
            return;
        }

        Thread createFileList_th = getThread(owner, selectedFolders);
        sprintf("createFileList_th.getName(): " + createFileList_th.getName());
        createFileList_th.start();
    }

    /**
     * Retrieves a thread for processing file list creation and sorting.
     *
     * @param owner The owning window of the thread
     * @param selectedFolders The list of selected folders
     * @return The created thread for file list processing
     */
    private Thread getThread(Window owner, List<Path> selectedFolders) {
        LoadingProcessTask loadingProcess_task = new LoadingProcessTask(owner);
        Task<List<Path>> createFileList = new SubList(selectedFolders);

        createFileList.setOnSucceeded(succeeded -> {
            List<Path> fileList = null;
            try {
                fileList = createFileList.get();
            } catch (Exception ex) {
                Messages.sprintfError("Something went wrong with createing filelist");
                Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
            }
            if (fileList == null || fileList.isEmpty()) {
                Messages.warningText("List is empty at Populate class. Cancelling");
                return;
            }

            Collections.sort(fileList);

            Task<Integer> sorterTask = new Sorter(model_main, fileList);
            loadingProcess_task.setTask(sorterTask);
            sorterTask.setOnSucceeded(sorterSuccess -> {
                Task<Void> calculateFolderContent = loadContentToContainer(loadingProcess_task, sorterTask);
                exec[getExecCounter()].submit(calculateFolderContent);
            });
            sorterTask.setOnCancelled(sorterCancelled -> {
                loadingProcess_task.setMessage("CANCELLED...");
                sprintf("sorterTask.setOnCancelled");
                loadingProcess_task.closeStage();
            });
            sorterTask.setOnFailed(sorterFailed -> {
                loadingProcess_task.setMessage("FAILED...");
                sprintf("sorterTask.setOnFailed");
                loadingProcess_task.closeStage();
            });

            Thread sorter_th = new Thread(sorterTask, "sorter_th");
            sprintf("sorter_th: " + sorter_th.getName());
            sorter_th.start();
        });

        createFileList.setOnCancelled(createFileListCancelled -> Messages.sprintf("CreateFileList cancelled"));
        createFileList.setOnFailed(createFileListFailed -> Messages.sprintf("CreateFileList failed"));

        return new Thread(createFileList, "createFileList_th");
    }

    public Task<Void> loadContentToContainer(LoadingProcessTask loadingProcess_task, Task<Integer> sorter) {
        total.set(model_main.tables().getAsItIs_table().getItems().size()
                + model_main.tables().getSortIt_table().getItems().size()
                + model_main.tables().getSorted_table().getItems().size());
        processAtomicInteger.set(total.get());
        sprintf("sorter.setOnSucceeded total: " + total);
        loadingProcess_task.setTask(sorter);
        loadingProcess_task.setMessage("Sorter");
        //Folder content checkki tähän. TArkistaa että onko tiedostot olemassa. Ja palauttaa varoituksen jos ei löydy tiedostoja.

        Task<Void> calculateFolderContent = new CalculateFolderContent(model_main, loadingProcess_task, total);
        loadingProcess_task.setTask(calculateFolderContent);
        calculateFolderContent.setOnSucceeded(calculateFolderContentSuccess -> {
            try {
                processAtomicInteger.set(processAtomicInteger.get() - 1);
                loadingProcess_task.setMessage("Saving...");
                // XMLFunctions.saveAll(model.getTables());
                loadingProcess_task.closeStage();
                model_main.getMonitorExternalDriveConnectivity().restart();

                sprintf("calculateFolderContent setOnSucceeded: " + sorter.get());
            } catch (InterruptedException | ExecutionException ex) {
                Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
            }
        });
        calculateFolderContent.setOnCancelled(calculateFolderContentCancelled -> Messages.sprintf("calculateFolderContent setOnCancelled"));
        calculateFolderContent.setOnFailed(failed -> {
            sprintf("calculateFolderContent setOnFailed");
            loadingProcess_task.setMessage("FAILED...");
            Messages.errorSmth(ERROR, "", null, Misc.getLineNumber(), true);
            loadingProcess_task.closeStage();
        });
        return calculateFolderContent;
    }

    private boolean hasInIgnoredListMain(ObservableList<Path> ignoredList, String path) {
        for (Path ignored : ignoredList) {
            if (ignored.toString().equals(path)) {
                return true;
            }
        }
        return false;
    }
}
