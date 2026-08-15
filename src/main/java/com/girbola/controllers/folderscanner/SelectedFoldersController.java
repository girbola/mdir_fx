package com.girbola.controllers.folderscanner;

import com.girbola.Main;
import com.girbola.concurrency.ConcurrencyUtils;
import com.girbola.configuration.ConfigurationUtils;
import com.girbola.controllers.loading.LoadingProcessTask;
import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.tables.FolderInfoUtils;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.dialogs.Dialogs;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.persistence.folderinfo.FolderInfoDao;
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
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
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
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
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
    //@formatter:on÷
    private List<SelectedFolder> selectedFolderScannerOriginal = new ArrayList<>();

    Callback<TableColumn<SelectedFolder, Boolean>, TableCell<SelectedFolder, Boolean>> selectedFoldersCellFactory = p -> new CheckBoxSelectFolderTableCell(modelMain.getSelectedFolders().getSelectedFolderScannerOriginal(), model_folderScanner);
    Callback<TableColumn<SelectedFolder, Boolean>, TableCell<SelectedFolder, Boolean>> removeRowCellFactory = p -> new CheckBoxRemoveRowTableCell(modelMain, model_folderScanner);

    @FXML
    private void selectedFolders_ok_action(ActionEvent event) throws ExecutionException, InterruptedException, IOException {
        Messages.sprintf("selectedFolders_ok_action pressed");
        modelMain.getTabPaneMain().getSelectionModel().select(0); // Selecting tabMain
        LoadingProcessTask loadingProcessTask = new LoadingProcessTask(Main.sceneManager.getWindow());

        model_folderScanner.getScanDrives().stop();
        modelMain.getMonitorExternalDriveConnectivity().cancel();
        for (SelectedFolder sf : modelMain.getSelectedFolders().getSelectedFolderScanner_obs()) {
            Messages.sprintf("----selectedFolders size" + sf.getFolder());
        }
        SelectedFolderInfoDao.saveSelectedFoldersToConfigDb(modelMain);

//        modelMain.getSelectedFolders().restore();

        modelMain.getSelectedFolders().getSelectedFolderScanner_obs().forEach(selectedFolder -> {
            Messages.sprintf("Selected folder to scan: " + selectedFolder.getFolder() + " isSelected: " + selectedFolder.isSelected() + " isIgnored: " + selectedFolder.isIgnored() + " isConnected: " + selectedFolder.isConnected() + " hasMedia: " + selectedFolder.isMedia());
        });

        boolean removeNotSelectedFromTables = TableUtils.removeNotSelectedFromTables(modelMain);
        if (!removeNotSelectedFromTables) {
            Messages.sprintf("There were nothing to remove from tables");
        }

        ConcurrencyUtils.stopExecThreadNow();

        // Create background task
        Task<List<FolderInfo>> scanTask = new Task<>() {
            @Override
            protected List<FolderInfo> call() throws Exception {

                List<FolderInfo> folderInfos = new ArrayList<>();

                List<Path> newLists = new ArrayList<>();
                List<Path> updateLists = new ArrayList<>();
                List<Path> notConnected = new ArrayList<>();
                updateMessage("Scanning folders...");
                loadingProcessTask.updateTextArea("Scanning folders...");
                for (SelectedFolder sf : modelMain.getSelectedFolders().getSelectedFolderScanner_obs()) {
                    Messages.sprintf("##########getSelectedFolderScanner_obs folder: " + sf.getFolder() + " isSelected: " + sf.isSelected());
                    if (sf.getFolder().equals("C:\\Users\\marko\\OneDrive\\Kuvat\\Ruotsin reissu")) {
                        Messages.sprintf("##########FFFFFFFFFFFFFFFFFFFFOUNDgetSelectedFolderScanner_obs folder: " + sf.getFolder() + " isSelected: " + sf.isSelected());
                    }
                    if (sf.isConnected()) {
                        if (sf.isSelected() && !sf.isIgnored()) {
                            if (Files.exists(Paths.get(sf.getFolder()))) {
                                if (!hasInIgnoredListMain(Main.conf.getIgnoredFoldersScanList(), sf.getFolder())) {
                                    List<Path> subFolders = FolderScanner.scanFolders(Paths.get(sf.getFolder()));

                                    for (Path subFolder : subFolders) {
                                        Messages.sprintf("---***SELECTED folder to scan: " + subFolder);
                                        if (subFolder.startsWith("C:\\Users\\marko\\OneDrive\\Kuvat\\100CANON")) {
                                            Messages.sprintf("---***SELECTED folder to scan: " + subFolder);
                                        }
                                        if (!TableUtils.checkTableDuplicates(modelMain.tables(), subFolder)) {
                                            //add as new
                                            newLists.add(subFolder);
                                            Messages.sprintf("##########newLists Selected folder to scan: " + subFolder);
                                        } else {
                                            //updates folder content for new content
                                            updateLists.add(subFolder);
                                            Messages.sprintf("##########updateLists Selected folder to scan: " + subFolder);
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        notConnected.add(Paths.get(sf.getFolder()));
                    }
                    if (!notConnected.isEmpty()) {
                        StringBuilder sb = new StringBuilder();
                        for (Path notConnectedPath : notConnected) {
                            sb.append(notConnectedPath).append("\n");
                        }
                        Messages.warningText(bundle.getString("selectedFoldersNotConnected") + ":\n" + sb.toString());
                    }
                }
                updateMessage("Iterating through media files...");
                loadingProcessTask.updateTextArea("Iterating through media files...");

                for (Path path : newLists) {
                    updateMessage("Iterating through media files...: " + path.getFileName() + "\n");
                    loadingProcessTask.updateTextArea("Iterating through media files...: " + path.getFileName() + "\n");

                    sprintf("#### FOLDER IS NEW and SELECTED: " + path);

                    TableType tableType = resolveTableTypeByPath(path);

                    FolderInfo folderInfo = FolderInfoDao.loadFolderInfo(path);
                    if (folderInfo == null) {
                        folderInfo = new FolderInfo(path);
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
                    } else {
                        boolean b = modelMain.tables().addToTable(folderInfo);
                        if (b) {
                            updateLists.add(path);
                        }
                    }
                }

                updateMessage("Iterating through existing table media files...");
                loadingProcessTask.updateTextArea("Iterating through existing table media files...");
                for (Path updateFile : updateLists) {
                    Messages.sprintf("##########updateLists Selected folder to scan: " + updateFile);
                    if (updateFile.startsWith("C:\\Users\\marko\\OneDrive\\Kuvat\\100CANON")) {
                        Messages.sprintf("##########updateLists Selected folder to scan: " + updateFile);
                    }
                    FolderInfo folderInfo = TableUtils.findTableValues(updateFile, modelMain.tables());

                    Messages.sprintf("##########updateLists folderInfo: " + folderInfo);
                    if (folderInfo != null && folderInfo.getFileInfoList() != null && !folderInfo.getFileInfoList().isEmpty()) {
                        //TODO existsFileInfos ei käytetä missään?!?!!?
                        List<FileInfo> existsFileInfos = new ArrayList<>();
                        List<FileInfo> fileInfoList = folderInfo.getFileInfoList();
                        if (folderInfo.getFolderPath().equals("C:\\Users\\marko\\Pictures\\mdir - Copy")) {
                            Messages.sprintf("FOUND!!!");
                        }
                        List<Path> currentFolderMediaFilesOnly = FileUtils.getCurrentFolderMediaFilesOnly(updateFile, FileUtils.filter_directories);

                        Iterator<Path> mediaFiles = currentFolderMediaFilesOnly.iterator();

                        // Create if currenFile has not in List<Path> currentFolderMediaFilesOnly
                        while (mediaFiles.hasNext()) {
                            Path currentFile = mediaFiles.next();
                            /*
                            scenario 1) file exists in fileinfo, continue
                            scenario 2) file does not exists in fileinfo, create fileinfo
                            scenario 3) currentFile does not has fileinfo list file anymore

                            if file exists in fileinfo or not
                            if new file if in currentFile it should be added as new fileinfo to FolderInfo.getFileLists().add(newFileInfo);
                             */
                            // 1. Call your find method (returns an Optional)
                            Optional<FileInfo> fileInfo = FileInfoUtils.findFileInfo(currentFile, fileInfoList);
                            if (fileInfo != null && fileInfo.isPresent()) {
                                Messages.sprintf("FileInfo already exists for: " + currentFile);
                                existsFileInfos.add(fileInfo.get());
                            } else {
                                FileInfo newFileInfo = FileInfoUtils.createFileInfo(currentFile);
                                if(newFileInfo != null) {
                                    existsFileInfos.add(newFileInfo);

                                folderInfo.setChanged(true);
                                Messages.sprintf("FileInfo does not exist for: " + currentFile);
                                } else {
                                    Messages.sprintfError("Could not create fileinfo: " + currentFile);
                                }
                            }
                            Messages.sprintf("*** ended folderFile: " + currentFile);
                        }
                        //FolderInfoUtils.calculateFolderInfoStatus(folderInfo);
                    } else {
                        Messages.sprintfError("Cannot find correct folderinfo: " + updateFile);
                    }
                }
                return folderInfos;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                updateMessage("Finished scanning folders. Adding new media files to tables...");
                loadingProcessTask.updateTextArea("Finished scanning folders. Adding new media files to tables...");
//                TableUtils.saveChangesContentsToTables(modelMain.tables());

                List<FolderInfo> newFolderInfos = getValue();

                // Update UI on JavaFX Application Thread
                for (FolderInfo newFolderInfo : newFolderInfos) {
                    String tableType = newFolderInfo.getTableType();
                    if (tableType.equals(TableType.SORTIT.getType())) {
                        modelMain.tables().getSortIt_table().getItems().add(newFolderInfo);
                    } else if (tableType.equals(TableType.SORTED.getType())) {
                        modelMain.tables().getSorted_table().getItems().add(newFolderInfo);
                    } else if (tableType.equals(TableType.ASITIS.getType())) {
                        modelMain.tables().getAsItIs_table().getItems().add(newFolderInfo);
                    }
                }
//                ConfigurationUtils.saveTablesToConfigurationDatabase(modelMain);

                ConfigurationUtils.saveTablesToConfigurationDatabase(modelMain, loadingProcessTask, true);
                updateMessage("Finished scanning folders. Saving tables to configuration");
                loadingProcessTask.updateTextArea("Finished scanning folders. Saving tables to configuration");
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
//        scanTask.setOnRunning(e -> loadingProcessTask.loadGUI());

//        scanTask.setOnSucceeded(e -> loadingProcessTask.closeStage());
//        scanTask.setOnFailed(e -> loadingProcessTask.closeStage());
//        scanTask.setOnCancelled(e -> loadingProcessTask.closeStage());
//
        loadingProcessTask.bind(scanTask.messageProperty());

//        loadingProcessTask.attach(scanTask);

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
                modelMain.getSelectedFolders().add(SelectedFolder.create(true, true, folder.getAbsolutePath(), false, true));
            }
            Messages.sprintf("foldersAdded: " + foldersAdded + "  vs size: " + modelMain.getSelectedFolders().getSelectedFolderScanner_obs().size());
            if (foldersAdded != modelMain.getSelectedFolders().getSelectedFolderScanner_obs().size() || modelMain.getSelectedFolders().getSelectedFolderScanner_obs().size() == 0) {

                modelMain.getSelectedFolders().getSelectedFolderScanner_obs().add(SelectedFolder.create(true, true, folder.getAbsolutePath(), false, true));
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
        Messages.sprintfError("start is not working yet: " + ERROR + " at line: " + Misc.getLineNumber());
    }

    public void restart() {
//        this.scanner.restart();
        Messages.sprintfError("restart is not working yet: " + ERROR + " at line: " + Misc.getLineNumber());
    }

    public void stop() {
//        this.scanner.cancel();
        Messages.sprintfError("stop is not working yet: " + ERROR + " at line: " + Misc.getLineNumber());
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
