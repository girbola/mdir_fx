package com.girbola.controllers.main;

import com.girbola.Main;
import com.girbola.concurrency.ConcurrencyUtils;
import com.girbola.controllers.folderscanner.SelectedFolder;
import com.girbola.controllers.folderscanner.SelectedFolderUtils;
import com.girbola.controllers.loading.LoadingProcessTask;
import com.girbola.controllers.main.folderinfoscan.FolderInfoScanner;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.TableView;
import javafx.stage.Window;

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
        sprintf("populateTablesFolderScannerList action started: " + modelMain.getSelectedFolders().getSelectedFolderScanner_obs().size());
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
                if (sf.isConnected() && sf.isSelected()) {
                    boolean selectedFolderExists = SelectedFolderUtils.tableHasFolder(modelMain.tables(), Paths.get(sf.getFolder()));
                    if (!selectedFolderExists) {
                        selectedFolders.add(Paths.get(sf.getFolder()));
                        sprintf("!selectedFolderExists Path is: " + sf.getFolder() + " isConnected: " + sf.isConnected());
                    }
                }
            } else {
                Messages.sprintf("##### FOLDER IGNORED!!!!: " + sf.getFolder());
            }
        }


        if (selectedFolders.isEmpty()) {
            sprintf("selectedFolders is null or empty");
            return;
        }

        Messages.sprintf("populateTablesFolderScannerList action ended. selectedFolders.size():::: " + selectedFolders.size());
        Thread createFileListThread = createFileListProcessingThread(owner, selectedFolders);
        createFileListThread.start();
    }

    private Thread createFileListProcessingThread(Window owner, List<Path> selectedFolders) {
        LoadingProcessTask loadingProcessTask = new LoadingProcessTask(owner);

        /*
         * First load selectedFolders sublist Then add walkfiletree per folder to scan
         * if there are more folders. If there are no more folders, then load the rest
         * of the selectedFolders list
         *
         * List<Path> selectedFolders = new ArrayList<>(); // first add happens in
         * populateTablesFolderScannerList()
         *
         * // then createFileListProcessingThread() adds all selected folders again for
         * (SelectedFolder sf :
         * modelMain.getSelectedFolders().getSelectedFolderScanner_obs()) { if
         * (sf.isSelected()) { if (sf.isConnected()) {
         * selectedFolders.add(Paths.get(sf.getFolder())); } } } for (Path path :
         * selectedFolders) { ArrayList<Path> mediaFilesInCurrentFolder =
         * GetAllMediaFiles.getAllMediaFiles(path); for (Path mediaFile :
         * mediaFilesInCurrentFolder) { Messages.sprintf("mediaFile: " + mediaFile); }
         * }
         */

        Task<List<Path>> createFileList = new SubList(selectedFolders);
        createFileList.setOnSucceeded(event -> handleFileListSuccess(createFileList, loadingProcessTask, selectedFolders));
        createFileList.setOnCancelled(event -> Messages.sprintf("CreateFileList cancelled"));
        createFileList.setOnFailed(event -> {
            loadingProcessTask.closeStage();
            Messages.sprintf("Populate CreateFileList failed CreateFileList failed");
        });

        return new Thread(createFileList, "createFileList_th");
    }

//    private Thread createFileListProcessingThread_old(Window owner, List<Path> selectedFolders) {
//        LoadingProcessTask loadingProcessTask = new LoadingProcessTask(owner);
//        Task<List<Path>> createFileList = new SubList(selectedFolders);
//
//        createFileList.setOnSucceeded(event -> handleFileListSuccess(createFileList, loadingProcessTask, selectedFolders));
//        createFileList.setOnCancelled(event -> Messages.sprintf("CreateFileList cancelled"));
//        createFileList.setOnFailed(event -> {
//            loadingProcessTask.closeStage();
//            Messages.sprintf("CreateFileList failed");
//        });
//
//        return new Thread(createFileList, "createFileList_th");
//    }

    private void handleFileListSuccess(Task<List<Path>> createFileList, LoadingProcessTask loadingProcessTask, List<Path> selectedFolders) {
        List<Path> fileList;
        try {
            fileList = createFileList.get();
            if (fileList == null || fileList.isEmpty()) {
                Messages.sprintf("List is empty at Populate class. Cancelling");
                handleEmptyFileList(loadingProcessTask, createFileList);
                return;
            }
            Messages.sprintf("fileList.size(): " + fileList.size());
            for (Path path : fileList) {
                Messages.sprintf("!#!#!#!##!!path: " + path);
            }
            // TODO korjaa olemassa oleva lista, ettei sieltä poistu mitään enää vaan tarkastetaan, että onko tullut lisäyksiä
            /*
            Tarkista onko jo tablevieweissä nämä, jos on tarkista onko sisältö muuttunut
             */

            Collections.sort(fileList);
            removeDuplicateFolders(fileList);

            if (fileList.isEmpty()) {
                Messages.sprintf("List is empty at Populate class. Cancelling");
                handleEmptyFileList(loadingProcessTask, createFileList);
                return;
            }

            appendMissingSelectedFolders(selectedFolders);

            for (Path path : fileList) {
                Messages.sprintf("!#!#!#!##!! after appendMissingSelectedFolders path: " + path);
            }

            Task<Integer> folderInfoScanner = new FolderInfoScanner(modelMain.tables(), fileList);
            folderInfoScanner.setOnSucceeded(event -> {
                sprintf("FolderInfoScanner succeeded");
                loadingProcessTask.closeStage();
            });
            folderInfoScanner.setOnCancelled(event -> {
                Messages.sprintf("FolderInfoScanner cancelled");
                loadingProcessTask.closeStage();
            });
            folderInfoScanner.setOnFailed(event -> {
                Throwable exception = folderInfoScanner.getException();
                if (exception != null) {
                    loadingProcessTask.closeStage();
                    Messages.sprintfError("FolderInfoScanner failed: " + exception.getMessage());
                }
            });
            loadingProcessTask.setTask(folderInfoScanner);
            setFolderInfoScannerTaskHandlers(folderInfoScanner, loadingProcessTask);

            Thread folderInfoScannerThread = new Thread(folderInfoScanner, "folderInfoScanner_th");

            sprintf("folderInfoScanner_th: " + folderInfoScannerThread.getName());
            folderInfoScannerThread.start();

        } catch (InterruptedException | ExecutionException ex) {
            Messages.sprintfError("Something went wrong with creating filelist: " + ex.getMessage());
            Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
        }
    }

    private void handleEmptyFileList(LoadingProcessTask loadingProcessTask, Task<?> task) {
        Messages.sprintf("List is empty at Populate class. Cancelling");
        Platform.runLater(loadingProcessTask::closeStage);
        task.cancel();
    }

    private void removeDuplicateFolders(List<Path> fileList) {
        List<TableView<FolderInfo>> allTables = TableUtils.getAllTables(modelMain.tables());
        List<Path> duplicates = new ArrayList<>();
        for (Path path : fileList) {
            if (TableUtils.tableHasFolder(allTables, path)) {
                Messages.sprintf("#######Found duplicate folder: " + path);
                duplicates.add(path);
            }
        }
        fileList.removeAll(duplicates);
    }

    private void appendMissingSelectedFolders(List<Path> selectedFolders) {
        for (SelectedFolder sf : modelMain.getSelectedFolders().getSelectedFolderScanner_obs()) {
            if (!hasInIgnoredListMain(Main.conf.getIgnoredFoldersScanList(), sf.getFolder()) && sf.isSelected()) {
                if (sf.isConnected() && sf.isSelected()) {
                    boolean selectedFolderExists = SelectedFolderUtils.tableHasFolder(modelMain.tables(), Paths.get(sf.getFolder()));
                    if (!selectedFolderExists) {
                        selectedFolders.add(Paths.get(sf.getFolder()));
                        sprintf("! selectedFolderExists Path is: " + sf.getFolder() + " isConnected: " + sf.isConnected());
                    }
                }
            }
        }
    }

    private void setFolderInfoScannerTaskHandlers(Task<Integer> folderInfoScannerTask, LoadingProcessTask loadingProcessTask) {
        folderInfoScannerTask.setOnSucceeded(event -> {
            Task<Void> calculateFolderContent = loadContentToContainer(loadingProcessTask, folderInfoScannerTask);

            if (exec[getExecCounter()].isShutdown() || exec[getExecCounter()].isTerminated()) {
                ConcurrencyUtils.initNewSingleExecutionService();
                Messages.sprintf("initNewSingleExecutionService NEW one");
            }

            exec[getExecCounter()].submit(calculateFolderContent);
        });

        folderInfoScannerTask.setOnCancelled(event -> {
            loadingProcessTask.setMessage("CANCELLED...");
            sprintf("folderInfoScannerTask.setOnCancelled");
            loadingProcessTask.closeStage();
        });

        folderInfoScannerTask.setOnFailed(event -> {
            loadingProcessTask.setMessage("FAILED...");
            sprintf("folderInfoScannerTask.setOnFailed");
            loadingProcessTask.closeStage();

            Throwable exception = folderInfoScannerTask.getException();
            if (exception != null) {
                Messages.sprintfError("FolderInfoScanner failed: " + exception.getMessage());
            }
        });
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
