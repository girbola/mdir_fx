package com.girbola.controllers.folderscanner;

import com.girbola.Main;
import com.girbola.concurrency.ConcurrencyUtils;
import com.girbola.controllers.loading.LoadingProcessTask;
import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.tables.FolderInfoUtils;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.dialogs.Dialogs;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.persistence.selectedfolderinfo.SelectedFolderInfoDao;
import com.girbola.utils.FileInfoUtils;
import com.girbola.utils.folderscanner.FolderScanner;
import common.utils.FileUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.DirectoryChooser;
import javafx.util.Callback;

import static com.girbola.Main.bundle;
import static com.girbola.controllers.main.tables.TableUtils.resolveTableTypeByPath;
import static com.girbola.messages.Messages.sprintf;
import static com.girbola.utils.FileInfoUtils.createFileInfo;

public class SelectedFoldersController {

    private final String ERROR = SelectedFoldersController.class.getName();

    private ModelMain modelMain;
    private ModelFolderScanner model_folderScanner;

    //@formatter:off
    @FXML private TableColumn<SelectedFolder, Boolean> folder_selected_col;
    @FXML private TableColumn<SelectedFolder, String> folder_col;
    @FXML private TableColumn<SelectedFolder, Boolean> folder_connected_col;
    @FXML private TableColumn<SelectedFolder, Boolean> hasMedia_col;
    @FXML private TableColumn<SelectedFolder, Boolean> remove_row_col;

    @FXML private Button selectedFolders_ok_btn;
    @FXML private Button selectedFolders_cancel_btn;
    @FXML private Button selectedFolders_select_folder;
    @FXML private TableView<SelectedFolder> selectedFolder_TableView;
    //@formatter:on
    private List<SelectedFolder> selectedFolderScannerOriginal = new ArrayList<>();

    Callback<TableColumn<SelectedFolder, Boolean>, TableCell<SelectedFolder, Boolean>> selectedFoldersCellFactory = p -> new CheckBoxSelectFolderTableCell(modelMain.getSelectedFolders().getSelectedFolderScannerOriginal(), model_folderScanner);
    Callback<TableColumn<SelectedFolder, Boolean>, TableCell<SelectedFolder, Boolean>> removeRowCellFactory = p -> new CheckBoxRemoveRowTableCell(modelMain, model_folderScanner);


    @FXML
    private void selectedFolders_ok_action(ActionEvent event) throws ExecutionException, InterruptedException, IOException {
        Messages.sprintf("selectedFolders_ok_action pressed");
        modelMain.getTabPaneMain().getSelectionModel().select(0); // Selecting tabMain

        model_folderScanner.getScanDrives().stop();
        modelMain.getMonitorExternalDriveConnectivity().cancel();

        modelMain.getSelectedFolders().restore();

        modelMain.getSelectedFolders().getSelectedFolderScanner_obs().forEach(selectedFolder -> {
            Messages.sprintf("Selected folder to scan: " + selectedFolder.getFolder() + " isSelected: " + selectedFolder.isSelected());
        });

        boolean removeNotSelectedFromTables = TableUtils.removeNotSelectedFromTables(modelMain);

        if (modelMain.tables().getSorted_table().getItems().isEmpty() && modelMain.tables().getSorted_table().getItems().isEmpty()) {
            Messages.warningText(bundle.getString("noFoldersSelected"));
            return;
        }

        ConcurrencyUtils.stopExecThreadNow();

        // Create background task
        Task<List<FolderInfo>> scanTask = new Task<>() {
            @Override
            protected List<FolderInfo> call() throws Exception {

                List<FolderInfo> folderInfos = new ArrayList<>();

                List<Path> newLists = new ArrayList<>();
                List<Path> updateLists = new ArrayList<>();
                for (SelectedFolder sf : modelMain.getSelectedFolders().getSelectedFolderScanner_obs()) {
                    if (!hasInIgnoredListMain(Main.conf.getIgnoredFoldersScanList(), sf.getFolder()) &&
                            sf.isSelected() &&
                            Files.exists(Paths.get(sf.getFolder()))) {
                        List<Path> paths = FolderScanner.scanFolders(Paths.get(sf.getFolder()));
                        for (Path p : paths) {
                            if (!TableUtils.checkTableDuplicates(modelMain.tables(), p)) {
                                //add as new
                                newLists.add(p);
                                Messages.sprintf("##########newLists Selected folder to scan: " + p);
                            } else {
                                //updates folder content for new content
                                updateLists.add(p);
                                Messages.sprintf("##########updateLists Selected folder to scan: " + p);
                            }
                        }

                    }
                }

                for (Path path : newLists) {
                    sprintf("#### FOLDER IS NEW and SELECTED: " + path);

                    TableType tableType = resolveTableTypeByPath(path);
                    FolderInfo folderInfo = new FolderInfo(path);
                    folderInfo.setTableType(tableType.getType());

                    // Heavy I/O operation
                    List<FileInfo> fileInfoList = FileInfoUtils.createFileInfo_list(folderInfo);
                    if (fileInfoList != null && !fileInfoList.isEmpty()) {
                        folderInfo.setFileInfoList(fileInfoList);
                        FolderInfoUtils.calculateFolderInfoStatus(folderInfo);
                        folderInfo.setChanged(true);
                        Main.setChanged(true);

                        // Store with table type info
                        folderInfos.add(folderInfo);
                    }
                }
                for (Path p : updateLists) {
                    FolderInfo folderInfo = TableUtils.findTableValues(p, modelMain.tables());
                    Messages.sprintf("##########updateLists folderInfo: " + folderInfo);
                    if (folderInfo != null && folderInfo.getFileInfoList() != null && !folderInfo.getFileInfoList().isEmpty()) {
                        List<FileInfo> fileInfoList = folderInfo.getFileInfoList();
                        if (folderInfo.getFolderPath().equals("C:\\Users\\marko\\Pictures\\mdir - Copy")) {
                            Messages.sprintf("FOUND!!!");
                        }
                        List<Path> currentFolderMediaFilesOnly = FileUtils
                                .getCurrentFolderMediaFilesOnly(p, FileUtils.filter_directories);

                        Messages.sprintf("!#!SIZE BEFORE REMOVED: "+ currentFolderMediaFilesOnly.size());
                        // Delete if from List<FileInfo> has not findRemovedFiles
                        Iterator<Path> iterator = currentFolderMediaFilesOnly.iterator();
                        while (iterator.hasNext()) {
                            Path findRemovedFiles = iterator.next();
                            boolean hasFile = FileInfoUtils.fileInfoHasCurrentFile(fileInfoList, findRemovedFiles);

                            if (!hasFile) {
                                Messages.sprintf("findRemovedFiles fileInfoHasCurrentFile hasFile?: " + findRemovedFiles);
                                iterator.remove();
                            }
                        }

                        Messages.sprintf("SIZE AFTER REMOVED: "+ currentFolderMediaFilesOnly.size());

                        Iterator<Path> iterator1 = currentFolderMediaFilesOnly.iterator();

                        // Create if currenFile has not in List<Path> currentFolderMediaFilesOnly
                        while (iterator1.hasNext()) {
                            Path currentFile = iterator1.next();

                            sprintf("##########create new folderInfo currentFile folderFile: " + currentFile);

                            /*
                            scenario 1) file exists in fileinfo, continue
                            scenario 2) file does not exists in fileinfo, create fileinfo
                            scenario 3) currentFile does not has fileinfo list file anymore

                            if file exists in fileinfo or not
                            if new file if in currentFile it should be added as new fileinfo to FolderInfo.getFileLists().add(newFileInfo);
                             */
                            boolean hasFile = FileInfoUtils.hasCurrentFile(fileInfoList, currentFile);

                            if (!hasFile) {
                                folderInfo.getFileInfoList().remove(currentFile);
                                fileInfoList.removeIf(fileInfo -> currentFile.toString().equals(fileInfo.getOrgPath()));
                                folderInfo.setChanged(true);
                                Main.setChanged(true);
                            } else {
                                FileInfo newFileInfo = createFileInfo(currentFile);
                                folderInfo.getFileInfoList().add(newFileInfo);
                                folderInfo.setChanged(true);
                                Main.setChanged(true);
                            }

                            Messages.sprintf("*** ended folderFile: " + currentFile);
                        }
                        //FolderInfoUtils.calculateFolderInfoStatus(folderInfo);
                    } else {
                        Messages.sprintfError("Cannot find correct folderinfo: " + p);
                    }
                }


//                List<FolderInfo> newFolderInfos = new ArrayList<>();
//
//                // Scan all folders (heavy operation)
//                List<Path> allPaths = new ArrayList<>();
//                for (Path path : newLists) {
//                    if (Main.getProcessCancelled() || isCancelled()) {
//                        cancel();
//                        return null;
//                    }
//                    List<Path> paths = FolderScanner.scanFolders(path);
//                    allPaths.addAll(paths);
//                }
//
//                for (Path p : allPaths) {
//                    Messages.sprintf("########allPaths Selected folder to scan: " + p);
//                }

                // Process selected folders (heavy operation)
//                for (Path path : allPaths) {
//                    if (!TableUtils.checkTableDuplicates(modelMain.tables(), path)) {
//
//                        sprintf("#### FOLDER IS NEW and SELECTED: " + path);
//
//                        TableType tableType = resolveTableTypeByPath(path);
//                        FolderInfo folderInfo = new FolderInfo(path);
//                        folderInfo.setTableType(tableType.getType());
//
//                        // Heavy I/O operation
//                        List<FileInfo> fileInfoList = FileInfoUtils.createFileInfo_list(folderInfo);
//                        if (fileInfoList != null && !fileInfoList.isEmpty()) {
//                            folderInfo.setFileInfoList(fileInfoList);
//                            FolderInfoUtils.calculateFolderInfoStatus(folderInfo);
//
//                            // Store with table type info
//                            newFolderInfos.add(folderInfo);
//                        }
//                    } else {
//                        sprintf("#### FOLDER WAS AT THE TABLE and SELECTED: " + path);
//                    }
//                }


                return folderInfos;

            }

            @Override
            protected void succeeded() {
                super.succeeded();
                List<FolderInfo> folderInfos = getValue();

                // Update UI on JavaFX Application Thread
                for (FolderInfo folderInfo : folderInfos) {
                    String tableType = folderInfo.getTableType();

                    if (tableType.equals(TableType.SORTIT.getType())) {
                        modelMain.tables().getSortIt_table().getItems().add(folderInfo);
                    } else if (tableType.equals(TableType.SORTED.getType())) {
                        modelMain.tables().getSorted_table().getItems().add(folderInfo);
                    } else if (tableType.equals(TableType.ASITIS.getType())) {
                        modelMain.tables().getAsItIs_table().getItems().add(folderInfo);
                    }
                }
            }

            @Override
            protected void failed() {
                super.failed();
                Throwable exception = getException();
                Messages.sprintfError("Folder scanning failed: " + exception.getMessage());
                exception.printStackTrace();
            }

            @Override
            protected void cancelled() {
                super.cancelled();
                Messages.sprintf("Folder scanningn cancelled");
            }


        };

// Optional: Show progress indicator
        LoadingProcessTask loadingProcessTask = new LoadingProcessTask(Main.sceneManager.getWindow());
//        scanTask.setOnRunning(e -> loadingProcessTask.loadGUI());

        scanTask.setOnSucceeded(e -> loadingProcessTask.closeStage());
        scanTask.setOnFailed(e -> loadingProcessTask.closeStage());
        scanTask.setOnCancelled(e -> loadingProcessTask.closeStage());

// Start the task in a background thread
        Thread scanThread = new Thread(scanTask);
        scanThread.setDaemon(true);
        scanThread.start();
//
//        for (Path p : selectedFolders) {
//            Messages.sprintf("------------***********allPaths Selected folder to scan: " + p);
//        }
//
//        // Scan subfolders and accept only folders which has more than one mediaFiles
//        Set<Path> selectedSubFolders = new HashSet<>();
//
//        for (SelectedFolder sf : modelMain.getSelectedFolders().getSelectedFolderScanner_obs()) {
//
//            //List<Path> paths = SubFolders.subFolders(Paths.get(sf.getFolder()));
//
//            List<Path> paths = MediaFolderScanner.findMediaFolders(Paths.get(sf.getFolder()));
//            if (!paths.isEmpty()) {
//                selectedSubFolders.addAll(paths);
//            }
//
////
////            boolean tableHasFolder = SelectedFolderUtils.tableHasFolder(modelMain.tables(), Paths.get(sf.getFolder()));
////            if (!tableHasFolder) {
////                selectedSubFolders.add(Paths.get(sf.getFolder()));
////            }
//
//
//        }
////populate<
//        LoadingProcessTask loadingProcessTask = new LoadingProcessTask(Main.sceneManager.getWindow());
//
//        List<ScanJob> list = new ArrayList<>();
//
//        for (Path path : selectedSubFolders) {
//
//            //!SelectedFolderUtils.tableHasFolder(modelMain.tables(), Paths.get(sf.getFolder()))
//            ObservableList<FolderInfo> tableIteratorSortit = modelMain.tables().getSortIt_table().getItems();
//            ObservableList<FolderInfo> tableIteratorSorted = modelMain.tables().getSorted_table().getItems();
//
//            for (FolderInfo folderInfo : tableIteratorSortit) {
//                if (folderInfo.getFolderPath().equals(path.toAbsolutePath().toString())) {
//                    // Update and see if there were changes
//                }
//            }
//
//            //createNewFolderInfo
//        }

    }

    @FXML
    private void selectedFolders_cancel_action(ActionEvent event) {
        sprintf("selectedFolders_cancel_action  pressed");

//        SelectionPropagation.syncTreeFromModel(modelMain);

        modelMain.getSelectedFolders().restore();

        for (SelectedFolder sf : modelMain.getSelectedFolders().getSelectedFolderScannerOriginal()) {
            if (sf.isSelected()) {
                Messages.sprintf("111##########getSelectedFolderScannerOriginal folder: " + sf.getFolder() + " is selected ");
            }
        }

        for (SelectedFolder sf : modelMain.getSelectedFolders().getSelectedFolderScanner_obs()) {
            if (sf.isSelected()) {
                Messages.sprintf("222##########getSelectedFolderScanner_obs folder: " + sf.getFolder() + " is selected ");
            }
        }

        //modelMain.getSelectedFolders().getSelectedFolderScanner_obs().setAll(selectedFolderScannerOriginal);

//        boolean loadSelectedFolders = SelectedFolderInfoDao.loadSelectedFolders(modelMain);
//        if (!loadSelectedFolders) {
//            Messages.warningText(bundle.getString("errorLoadingSelectedFolders"));
//        }
//
//        selectedFolder_TableView.setItems(modelMain.getSelectedFolders().getSelectedFolderScanner_obs());
//        SelectionPropagation.syncTreeFromModel(modelMain);
        //  modelMain.getTabPaneMain().getSelectionModel().select(0); // Selecting tabMain
//        selectedFolder_TableView.setItems(model_main.getSelectedFolders().getSelectedFolderScanner_obs());
//        SelectionPropagation.syncTreeFromModel(model_main);
        //  model_main.getTabPaneMain().getSelectionModel().select(0); // Selecting tabMain
    }

    @FXML
    private void selectedFolders_select_folder_action(ActionEvent event) {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setInitialDirectory(new File(System.getProperty("user.home")));
        dc.setTitle(Main.bundle.getString("selectFolderForScanning"));
        File folder = dc.showDialog(selectedFolders_select_folder.getScene().getWindow());
        Messages.sprintf("##########Selected folder: " + folder);

//        modelMain.getSelectedFolders().getSelectedFolderScanner_obs().add(new SelectedFolder(true, true, folder.getAbsolutePath(), true));
        if (Main.conf.getWorkDir().contains(folder.toString())) {
            Messages.warningText(Main.bundle.getString("workDirConflict"));
            return;
        }

        if (folder != null) {
            int foldersAdded = modelMain.getSelectedFolders().getSelectedFolderScanner_obs().size();

            if (!modelMain.getSelectedFolders().getSelectedFolderScanner_obs().isEmpty()) {
                Messages.sprintf("modelMain.getSelectedFolders():::::::::: " + modelMain.getSelectedFolders().getSelectedFolderScanner_obs().size());
                for (SelectedFolder selectedFolder : modelMain.getSelectedFolders().getSelectedFolderScanner_obs()) {
                    Messages.sprintf("Selected folder in the list: " + selectedFolder.getFolder());
                    if (!selectedFolder.getFolder().equals(folder.getAbsolutePath())) {
                        Messages.sprintf("Adding folder: " + folder.getAbsolutePath());
                    }
                }
                modelMain.getSelectedFolders().add(new SelectedFolder(true, true, folder.getAbsolutePath(), true));
            }
            Messages.sprintf("foldersAdded: " + foldersAdded + "  vs size: " + modelMain.getSelectedFolders().getSelectedFolderScanner_obs().size());
            if (foldersAdded != modelMain.getSelectedFolders().getSelectedFolderScanner_obs().size() || modelMain.getSelectedFolders().getSelectedFolderScanner_obs().size() == 0) {

                modelMain.getSelectedFolders().getSelectedFolderScanner_obs().add(new SelectedFolder(true, true, folder.getAbsolutePath(), true));
//                modelMain.getSelectedFolders().add(new SelectedFolder(true, true, folder.getAbsolutePath(), true));
                SelectedFolderInfoDao.saveSelectedFoldersToConfigDb(modelMain);
            }

        } else {
            Messages.sprintfError("Folder is null");
            Messages.warningText(Main.bundle.getString("folderNotFound"));
        }
    }

    public void setDeleteKeyPressed() {
        selectedFolder_TableView.setOnKeyPressed((KeyEvent event) -> {
            if (event.getCode() == (KeyCode.DELETE)) {
                removeFromTable(selectedFolder_TableView);
            }
        });
    }

    private void removeFromTable(TableView<SelectedFolder> table) {
        //TODO Finish removeFromTable
        Messages.warningText("RemoveFromTable option is NOT READY");

        Iterator<SelectedFolder> selectedFolderScannerObs = modelMain.getSelectedFolders().getSelectedFolderScanner_obs().iterator();
        while (selectedFolderScannerObs.hasNext()) {
            SelectedFolder selectedFolder = selectedFolderScannerObs.next();
            Messages.sprintf("RemoveFromTable selectedFolder: " + selectedFolder.getFolder());
        }
        Connection connection = null;

        ObservableList<SelectedFolder> selectedItems = table.getSelectionModel().getSelectedItems();

        for (SelectedFolder selectedItem : selectedItems) {
            Messages.sprintf("RemoveFromTable selectedItem: " + selectedItem.getFolder());
        }

        Dialog<ButtonType> changesDialog = Dialogs.createDialog_YesNo(Main.sceneManager.getScene_dateFixer().getWindow(), bundle.getString("removeAsWellFromTables"));

        Optional<ButtonType> result = changesDialog.showAndWait();
        if (result.get().getButtonData().equals(ButtonBar.ButtonData.YES)) {
            SelectedFolderInfoDao.removeFromTable(selectedItems);
        } else if (result.get().getButtonData().equals(ButtonBar.ButtonData.NO)) {
            SelectedFolderInfoDao.removeFromTable(selectedItems);
            SelectedFolderInfoDao.clearSelectedFolders(modelMain);
            table.getItems().removeAll(selectedItems);
            table.getSelectionModel().clearSelection();
        } else {
            Messages.sprintf("RemoveFromTable result: " + result.get().getButtonData());
            return;
        }

    }

    public void init(ModelMain aModel_main, ModelFolderScanner aModel_folderScanner) {
        this.modelMain = aModel_main;
        this.model_folderScanner = aModel_folderScanner;

        selectedFolder_TableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        //model_folderScanner.setDeleteKeyPressed(selectedFolder_TableView);
        selectedFolder_TableView.setOnKeyPressed((KeyEvent event) -> {
            if (event.getCode() == (KeyCode.DELETE)) {
                removeFromTable(selectedFolder_TableView);
            }
        });

        folder_selected_col.setCellFactory(selectedFoldersCellFactory);
        //folder_selected_col.setCellValueFactory((TableColumn.CellDataFeatures<SelectedFolder, Boolean> cellData) -> new SimpleObjectProperty<>(cellData.getValue().isSelected()));
        folder_selected_col.setCellValueFactory(cellData -> cellData.getValue().selectedProperty().asObject());

        remove_row_col.setCellFactory(removeRowCellFactory);
        remove_row_col.setCellValueFactory((TableColumn.CellDataFeatures<SelectedFolder, Boolean> cellData) -> new SimpleObjectProperty<>(false));


        //folder_col.setCellValueFactory((TableColumn.CellDataFeatures<SelectedFolder, String> cellData) -> new SimpleObjectProperty<>(cellData.getValue().getFolder()));
        folder_col.setCellValueFactory(cellData -> cellData.getValue().folder_property());

        folder_connected_col.setCellFactory(connected);
        //folder_connected_col.setCellValueFactory((TableColumn.CellDataFeatures<SelectedFolder, Boolean> cellData) -> new SimpleObjectProperty<>(cellData.getValue().isConnected()));
        folder_connected_col.setCellValueFactory(cellData -> cellData.getValue().connected_property().asObject());

        hasMedia_col.setCellFactory(hasMediaFiles);
        //hasMedia_col.setCellValueFactory((TableColumn.CellDataFeatures<SelectedFolder, Boolean> cellData) -> new SimpleObjectProperty<>(cellData.getValue().isMedia()));
        hasMedia_col.setCellValueFactory(cellData -> cellData.getValue().mediaProperty().asObject());

        selectedFolder_TableView.setItems(modelMain.getSelectedFolders().getSelectedFolderScanner_obs());
        Messages.sprintf("getFolderScanner lldlflfl" + this.modelMain.getSelectedFolders().getSelectedFolderScanner_obs().size());

        selectedFolderScannerOriginal.addAll(modelMain.getSelectedFolders().getSelectedFolderScanner_obs());

      /*  scanner = new ScheduledService<Void>() {

            @Override
            protected Task createTask() {
                return new Task<Integer>() {
                    @Override
                    protected Integer call() throws Exception {
                        for (SelectedFolder sf : modelMain.getSelectedFolders().getSelectedFolderScanner_obs()) {
                            sf.setConnected(Files.exists(Paths.get(sf.getFolder())));
                            Messages.sprintf("SelectedFolder: " + sf.getFolder() + " isConnected?: " + sf.isConnected());
                        }
                        return null;
                    }
                };
            }
        };

        scanner.setPeriod(Duration.seconds(10));*/
    }

    public Callback<TableColumn<SelectedFolder, Boolean>, TableCell<SelectedFolder, Boolean>> hasMediaFiles = selectedFolderBooleanTableColumn -> new TableCell_Media();

    public Callback<TableColumn<SelectedFolder, Boolean>, TableCell<SelectedFolder, Boolean>> connected = selectedFolderBooleanTableColumn -> new TableCell_Connected();


//    public Callback<TableColumn<SelectedFolder, Boolean>, TableCell<SelectedFolder, Boolean>> hasMedia_tableCell =
//            new Callback<SelectedFolder, TableCell<SelectedFolder, Boolean>>() {
//                @Override
//                public TableCell<SelectedFolder, Boolean> call(Object o) {
//                    return new TableCell_Media();
//                }
//            };


    public void start() {
//        this.scanner.start();
    }

    public void restart() {
//        this.scanner.restart();
    }

    public void stop() {
//        this.scanner.cancel();
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
