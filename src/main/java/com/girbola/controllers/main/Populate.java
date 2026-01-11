
package com.girbola.controllers.main;

import com.girbola.Main;
import com.girbola.concurrency.ConcurrencyUtils;
import com.girbola.controllers.folderscanner.SelectedFolder;
import com.girbola.controllers.folderscanner.SelectedFolderUtils;
import com.girbola.controllers.loading.LoadingProcessTask;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.TableView;
import javafx.stage.Window;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
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
            }
        }



        // Add checked in datefixer


//        Set<Path> acceptedFolders = new HashSet<>();
//        for(SelectedFolder selectedFolder : modelMain.getSelectedFolders().getSelectedFolderScanner_obs()) {
//
//            if(selectedFolder.isSelected()) {
//                sprintf("-----Selected folder: " + selectedFolder.getFolder());
//                List<Path> paths = SubFolders.subFolders(Paths.get(selectedFolder.getFolder()));// This is just to initialize the SubFolders class, if needed.
//                if (paths == null) {
//                    sprintf("paths is null");
//                    continue;
//                }
//                for (Path p : paths) {
//                    if (!acceptedFolders.contains(p)) {
//                        Messages.sprintf("acceptedFolders initialized with path: " + p);
//                        acceptedFolders.add(p);
//                    } else {
//                        sprintf("acceptedFolders already contains path: " + p);
//                    }
//                }
//            }
//        }
//
//        for(Path path : acceptedFolders) {
//            sprintf("acceptedFolders initialized with path: " + path);
//        }

        Messages.sprintf("populateTablesFolderScannerList action ended. selectedFolders.size():::: " + selectedFolders.size());
        Thread createFileListThread = createFileListProcessingThread(owner, selectedFolders);
        createFileListThread.start();
    }

    private Thread createTableViewContent(List<Path> selectedFolders) {
        LoadingProcessTask loadingProcessTask = new LoadingProcessTask(null);
        for(Path selectedFolder : selectedFolders) {
            sprintf("selectedFolder to process in createTableViewContent: " + selectedFolder);

        }
return null;
    }

    private Thread createFileListProcessingThread(Window owner, List<Path> selectedFolders) {
        LoadingProcessTask loadingProcessTask = new LoadingProcessTask(owner);
        Task<List<Path>> createFileList = new SubList(selectedFolders);

        createFileList.setOnSucceeded(event -> handleFileListSuccess(createFileList, loadingProcessTask, selectedFolders));
        createFileList.setOnCancelled(event -> Messages.sprintf("CreateFileList cancelled"));
        createFileList.setOnFailed(event -> {
            loadingProcessTask.closeStage();
            Messages.sprintf("CreateFileList failed");
        });

        return new Thread(createFileList, "createFileList_th");
    }

    private Thread createFileListProcessingThread_old(Window owner, List<Path> selectedFolders) {
        LoadingProcessTask loadingProcessTask = new LoadingProcessTask(owner);
        Task<List<Path>> createFileList = new SubList(selectedFolders);

        createFileList.setOnSucceeded(event -> handleFileListSuccess(createFileList, loadingProcessTask, selectedFolders));
        createFileList.setOnCancelled(event -> Messages.sprintf("CreateFileList cancelled"));
        createFileList.setOnFailed(event -> {
            loadingProcessTask.closeStage();
            Messages.sprintf("CreateFileList failed");
        });

        return new Thread(createFileList, "createFileList_th");
    }

    private void handleFileListSuccess(Task<List<Path>> createFileList, LoadingProcessTask loadingProcessTask, List<Path> selectedFolders) {
        List<Path> fileList;
        try {
            fileList = createFileList.get();
            if (fileList == null || fileList.isEmpty()) {
                handleEmptyFileList(loadingProcessTask, createFileList);
                return;
            }

            Collections.sort(fileList);
            removeDuplicateFolders(fileList);

            if (fileList.isEmpty()) {
                handleEmptyFileList(loadingProcessTask, createFileList);
                return;
            }

            appendMissingSelectedFolders(selectedFolders);

            Task<Integer> sorterTask = new Sorter(modelMain, fileList);
            loadingProcessTask.setTask(sorterTask);
            setSorterTaskHandlers(sorterTask, loadingProcessTask);

            Thread sorterThread = new Thread(sorterTask, "sorter_th");
            sprintf("sorter_th: " + sorterThread.getName());
            sorterThread.start();

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

    private void setSorterTaskHandlers(Task<Integer> sorterTask, LoadingProcessTask loadingProcessTask) {
        sorterTask.setOnSucceeded(event -> {
            Task<Void> calculateFolderContent = loadContentToContainer(loadingProcessTask, sorterTask);
            if (exec[getExecCounter()].isShutdown() || exec[getExecCounter()].isTerminated()) {
                ConcurrencyUtils.initNewSingleExecutionService();
                Messages.sprintf("initNewSingleExecutionService NEW one");
            }
            exec[getExecCounter()].submit(calculateFolderContent);
        });
        sorterTask.setOnCancelled(event -> {
            loadingProcessTask.setMessage("CANCELLED...");
            sprintf("sorterTask.setOnCancelled");
            loadingProcessTask.closeStage();
        });
        sorterTask.setOnFailed(event -> {
            loadingProcessTask.setMessage("FAILED...");
            sprintf("sorterTask.setOnFailed");
            loadingProcessTask.closeStage();
        });
    }


    /**
     * Retrieves a thread for processing file list creation and sorting.
     *
     * @param owner           The owning window of the thread
     * @param selectedFolders The list of selected folders
     * @return The created thread for file list processing
     */
    @Deprecated
    private Thread createFileListProcessingThread_(Window owner, List<Path> selectedFolders) {
        LoadingProcessTask loadingProcessTask = new LoadingProcessTask(owner);
        Task<List<Path>> createFileList = new SubList(selectedFolders);

        createFileList.setOnSucceeded(succeeded -> {
            List<Path> fileList = null;
            try {
                fileList = createFileList.get();

                if (fileList == null || fileList.isEmpty()) {
                    Messages.sprintf("List is empty at Populate class. Cancelling");
                    Platform.runLater(loadingProcessTask::closeStage);
                    createFileList.cancel();
                    return;
                }

                Collections.sort(fileList);

                List<TableView<FolderInfo>> allTables = TableUtils.getAllTables(modelMain.tables());
                List<Path> findDuplicates = new ArrayList<>();
                for(Path path : fileList) {
                    if(TableUtils.tableHasFolder(allTables, path)) {
                        findDuplicates.add(path);
                    }
                }
                fileList.removeAll(findDuplicates);
                if(fileList.isEmpty()) {
                    Messages.sprintf("List is empty at Populate class. Cancelling");
                    Platform.runLater(loadingProcessTask::closeStage);
                    createFileList.cancel();
                    return;
                }
                appendMissingSelectedFolders(selectedFolders);
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


                Task<Integer> sorterTask = new Sorter(modelMain, fileList);
                loadingProcessTask.setTask(sorterTask);
                sorterTask.setOnSucceeded(sorterSuccess -> {
                    Task<Void> calculateFolderContent = loadContentToContainer(loadingProcessTask, sorterTask);
                    if(exec[getExecCounter()].isShutdown() || exec[getExecCounter()].isTerminated()) {
                        ConcurrencyUtils.initNewSingleExecutionService();
                        Messages.sprintf("initNewSingleExecutionService NEW one");
                    }

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
        createFileList.setOnFailed(createFileListFailed -> {
            loadingProcessTask.closeStage();
            Messages.sprintf("CreateFileList failed");
        });

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
