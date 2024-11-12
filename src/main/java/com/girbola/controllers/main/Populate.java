/*
 @(#)Copyright:  Copyright (c) 2012-2024 All right reserved.
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.main;

import com.girbola.Main;
import com.girbola.controllers.folderscanner.SelectedFolder;
import com.girbola.controllers.folderscanner.SelectedFolderUtils;
import com.girbola.controllers.loading.LoadingProcessTask;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
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

public class Populate {

    private static final String ERROR = Populate.class.getSimpleName();

    private ModelMain modelMain;

    private IntegerProperty total = new SimpleIntegerProperty();

    private AtomicInteger processAtomicInteger = new AtomicInteger(0);

    public Populate(ModelMain model) {
        this.modelMain = model;
        Messages.sprintf("Populate initialized");
    }

    public void populateTablesFolderScannerList(Window owner) {
        sprintf("SorterTest action started");
        Main.setProcessCancelled(false);
        modelMain.getMonitorExternalDriveConnectivity().cancel();
        if (modelMain.getSelectedFolders().getSelectedFolderScanner_obs().isEmpty()) {
            sprintf("getSelection_FolderScanner list were empty");
            return;
        }

        /*
         * Load from selectedFolder list Sort to tables Calculate tables content
         */
        List<Path> selectedFolders = new ArrayList<>();
        for (SelectedFolder sf : modelMain.getSelectedFolders().getSelectedFolderScanner_obs()) {
            if (!hasInIgnoredListMain(Main.conf.getIgnoredFoldersScanList(), sf.getFolder()) && sf.isSelected()) {
                if (sf.isConnected()) {
                    boolean selectedFolderExists = SelectedFolderUtils.tableHasFolder(modelMain.tables(), Paths.get(sf.getFolder()));
                    if (!selectedFolderExists) {
                        selectedFolders.add(Paths.get(sf.getFolder()));
                        sprintf("! selectedFolderExists Path is: " + sf.getFolder() + " isConnected: " + sf.isConnected());
                    }
                }
            }
        }

        if (selectedFolders.isEmpty()) return;

        Thread createFileListThread = getThread(owner, selectedFolders);
        sprintf("createFileListThread.getName(): " + createFileListThread.getName());
        createFileListThread.start();
    }

    /**
     * Retrieves a thread for processing file list creation and sorting.
     *
     * @param owner           The owning window of the thread
     * @param selectedFolders The list of selected folders
     * @return The created thread for file list processing
     */
    private Thread getThread(Window owner, List<Path> selectedFolders) {
        LoadingProcessTask loadingProcessTask = new LoadingProcessTask(owner);
        Task<List<Path>> createFileList = new SubList(selectedFolders);

        createFileList.setOnSucceeded(succeeded -> {
            List<Path> fileList = null;
            try {
                fileList = createFileList.get();

                if (fileList == null || fileList.isEmpty()) {
                    Messages.warningText("List is empty at Populate class. Cancelling");
                    return;
                }

                Collections.sort(fileList);

                Task<Integer> sorterTask = new Sorter(modelMain, fileList);
                loadingProcessTask.setTask(sorterTask);
                sorterTask.setOnSucceeded(sorterSuccess -> {
                    Task<Void> calculateFolderContent = loadContentToContainer(loadingProcessTask, sorterTask);
                    exec[getExecCounter()].submit(calculateFolderContent);
                });
                sorterTask.setOnCancelled(sorterCancelled -> {
                    loadingProcessTask.setMessage("CANCELLED...");
                    sprintf("sorterTask.setOnCancelled");
                    loadingProcessTask.closeStage();
                });
                sorterTask.setOnFailed(sorterFailed -> {
                    loadingProcessTask.setMessage("FAILED...");
                    sprintf("sorterTask.setOnFailed");
                    loadingProcessTask.closeStage();
                });

                Thread sorterThread = new Thread(sorterTask, "sorter_th");
                sprintf("sorter_th: " + sorterThread.getName());
                sorterThread.start();

            } catch (InterruptedException ex) {
                Messages.sprintfError("Something went wrong with creating filelist InterruptedException: " + ex.getMessage());
                Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
            } catch (ExecutionException ex) {
                Messages.sprintfError("Something went wrong with creating filelist ExecutionException" + ex.getMessage());
                Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
            }
        });

        createFileList.setOnCancelled(createFileListCancelled -> Messages.sprintf("CreateFileList cancelled"));
        createFileList.setOnFailed(createFileListFailed -> Messages.sprintf("CreateFileList failed"));

        return new Thread(createFileList, "createFileList_th");
    }

    public Task<Void> loadContentToContainer(LoadingProcessTask loadingProcessTask, Task<Integer> sorter) {
        total.set(modelMain.tables().getAsItIs_table().getItems().size()
                + modelMain.tables().getSortIt_table().getItems().size()
                + modelMain.tables().getSorted_table().getItems().size());
        processAtomicInteger.set(total.get());
        sprintf("sorter.setOnSucceeded total: " + total);
        loadingProcessTask.setTask(sorter);
        loadingProcessTask.setMessage("Sorter");
        //Folder content checkki tähän. TArkistaa että onko tiedostot olemassa. Ja palauttaa varoituksen jos ei löydy tiedostoja.

        Task<Void> calculateFolderContent = new CalculateFolderContent(modelMain, loadingProcessTask, total);
        loadingProcessTask.setTask(calculateFolderContent);
        calculateFolderContent.setOnSucceeded(calculateFolderContentSuccess -> {
            try {
                processAtomicInteger.set(processAtomicInteger.get() - 1);
                loadingProcessTask.setMessage("Saving...");
                loadingProcessTask.closeStage();
                modelMain.getMonitorExternalDriveConnectivity().restart();

                sprintf("calculateFolderContent setOnSucceeded: " + sorter.get());
            } catch (InterruptedException | ExecutionException ex) {
                Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
            }
        });
        calculateFolderContent.setOnCancelled(calculateFolderContentCancelled -> Messages.sprintf("calculateFolderContent setOnCancelled"));
        calculateFolderContent.setOnFailed(failed -> {
            sprintf("calculateFolderContent setOnFailed");
            loadingProcessTask.setMessage("FAILED...");
            Messages.errorSmth(ERROR, "", null, Misc.getLineNumber(), true);
            loadingProcessTask.closeStage();
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
