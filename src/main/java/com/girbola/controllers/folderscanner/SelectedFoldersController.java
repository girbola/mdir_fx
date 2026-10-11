package com.girbola.controllers.folderscanner;

import com.girbola.Main;
import com.girbola.concurrency.ConcurrencyUtils;
import com.girbola.configuration.ConfigurationUtils;
import com.girbola.controllers.folderscanner.folderpicker.SelectionPropagation;
import com.girbola.controllers.loading.LoadingProcessTask;
import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.tables.FolderInfoUtils;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.dialogs.Dialogs;
import com.girbola.drive.DriveInfo;
import com.girbola.drive.DriveScanner;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.messages.customdialogs.DisconnectedDrivesDialog;
import com.girbola.misc.Misc;
import com.girbola.persistence.folderinfo.FolderInfoDao;
import com.girbola.persistence.selectedfolderinfo.SelectedFolderInfoDao;
import com.girbola.utils.FileInfoUtils;
import com.girbola.utils.folderscanner.FolderScanner;
import common.utils.FileUtils;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javafx.application.Platform;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import javafx.util.Callback;

import static com.girbola.Main.bundle;
import static com.girbola.controllers.main.tables.TableUtils.resolveTableTypeByPath;
import static com.girbola.messages.Messages.sprintf;

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

    Callback<TableColumn<SelectedFolder, Boolean>, TableCell<SelectedFolder, Boolean>> selectedFoldersCellFactory = p -> new CheckBoxSelectFolderTableCell(modelMain, model_folderScanner);
    Callback<TableColumn<SelectedFolder, Boolean>, TableCell<SelectedFolder, Boolean>> removeRowCellFactory = p -> new CheckBoxRemoveRowTableCell(modelMain, model_folderScanner);

    @FXML
    private void selectedFolders_ok_action(ActionEvent event) {
        Messages.sprintf("selectedFolders_ok_action pressed");
        modelMain.getTabPaneMain().getSelectionModel().select(0); // Selecting tabMain
        LoadingProcessTask loadingProcessTask = new LoadingProcessTask(Main.sceneManager.getWindow());
        prepareForFolderScan();
        Task<List<FolderInfo>> scanTask = createScanTask(loadingProcessTask);
        loadingProcessTask.bind(scanTask.messageProperty());

        Thread scanThread = new Thread(scanTask, "selected-folders-scan");
//        scanTask.setOnSucceeded(e -> {
//            Messages.sprintf("SUCCEEDEDDDDD::::::::::::: " + scanTask.getValue().size() + " new folders added to tables");
//            model_folderScanner.getScanDrives().restart();
//        });
        scanThread.setDaemon(true);
        scanThread.start();
    }

    private void prepareForFolderScan() {
        if (model_folderScanner != null && model_folderScanner.getScanDrives() != null) {
            model_folderScanner.getScanDrives().stop();
        }
        if (modelMain != null && modelMain.getMonitorExternalDriveConnectivity() != null) {
            modelMain.getMonitorExternalDriveConnectivity().cancel();
        }

        for (SelectedFolder sf : modelMain.getSelectedFolders().getSelectedFolderScanner_obs()) {
            Messages.sprintf("----selectedFolders size" + sf.getFolder());
        }
        SelectedFolderInfoDao.saveSelectedFoldersToConfigDb(modelMain);

        modelMain.getSelectedFolders().getSelectedFolderScanner_obs().forEach(selectedFolder ->
            Messages.sprintf("getSelectedFolders Selected folder to scan: " + selectedFolder.getFolder()
                + " isSelected: " + selectedFolder.isSelected()
                + " isIgnored: " + selectedFolder.isIgnored()
                + " isConnected: " + selectedFolder.isConnected()
                + " hasMedia: " + selectedFolder.isMedia())
        );

        ConcurrencyUtils.stopExecThreadNow();
    }

    private Task<List<FolderInfo>> createScanTask(LoadingProcessTask loadingProcessTask) {
        return new Task<>() {
            @Override
            protected List<FolderInfo> call() throws Exception {
                Messages.sprintf("createScanTask start");
                List<FolderInfo> folderInfos = new ArrayList<>();
                Consumer<String> status = message -> {
                    updateMessage(message);
                    loadingProcessTask.updateTextArea(message);
                };

                status.accept("Scanning folders...");
                ScanBuckets buckets = collectScanBuckets();
                //notifyDisconnectedIfNeeded(buckets.notConnected);

                status.accept("Iterating through media files...");
                processRemovedFolders(buckets.removedList);
                processNewFolders(buckets.newLists, buckets.updateLists, folderInfos, status);

                status.accept("Iterating through existing table media files...");
                processUpdatedFolders(buckets.updateLists);

                return folderInfos;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                updateMessage("Finished scanning folders. Adding new media files to tables...");
                loadingProcessTask.updateTextArea("Finished scanning folders. Adding new media files to tables...");

                addNewFolderInfosToTables(getValue());

                ConfigurationUtils.saveTablesToConfigurationDatabase(modelMain, loadingProcessTask, true);
                updateMessage("Finished scanning folders. Saving tables to configuration");
                loadingProcessTask.updateTextArea("Finished scanning folders. Saving tables to configuration");
            }

            @Override
            protected void failed() {
                super.failed();
                Throwable exception = getException();
                Messages.sprintfError("Folder scanning failed: " + exception.getMessage());
                updateMessage("Folder scanning failed: " + exception.getMessage());
                loadingProcessTask.closeStage();
                exception.printStackTrace();
            }

            @Override
            protected void cancelled() {
                super.cancelled();
                Messages.sprintf("Folder scanningn cancelled");
                loadingProcessTask.closeStage();
            }
        };
    }

    private ScanBuckets collectScanBuckets() throws Exception {
        List<Path> newLists = new ArrayList<>();
        List<Path> updateLists = new ArrayList<>();
        List<Path> notConnected = new ArrayList<>();
        List<Path> removedList = new ArrayList<>();

        Messages.sprintf("collectScanBuckets() selectedFolders size: " + modelMain.getSelectedFolders().getSelectedFolderScanner_obs().size());

        for (SelectedFolder sf : modelMain.getSelectedFolders().getSelectedFolderScanner_obs()) {
            Messages.sprintf("##########getSelectedFolderScanner_obs folder: " + sf.getFolder() + " isSelected: " + sf.isSelected());

            Path resolvedPath = resolveSelectedFolderPathBySerial(sf);
            sf.setConnected(resolvedPath != null);

            if (sf.isSelected() && !sf.isIgnored()) {
                if (resolvedPath != null) {
                    addScanTargetsForFolder(sf, resolvedPath, newLists, updateLists);
                } else {
                    notConnected.add(Paths.get(sf.getFolder()));
                }
            } else {
                Messages.sprintf("added to removedList: " + sf.getFolder());
                removedList.add(Paths.get(sf.getFolder()));
            }
        }

        return new ScanBuckets(newLists, updateLists, notConnected, removedList);
    }

    private void addScanTargetsForFolder(SelectedFolder selectedFolder, Path resolvedPath, List<Path> newLists, List<Path> updateLists) throws Exception {
        if (hasInIgnoredListMain(Main.conf.getIgnoredFoldersScanList(), selectedFolder.getFolder())) {
            return;
        }

        List<Path> subFolders = FolderScanner.scanFolders(resolvedPath);
        for (Path subFolder : subFolders) {
            Messages.sprintf("---***SELECTED folder to scan: " + subFolder);
            if (!TableUtils.checkTableDuplicates(modelMain.tables(), subFolder)) {
                newLists.add(subFolder);
                Messages.sprintf("##########newLists Selected folder to scan: " + subFolder);
            } else {
                updateLists.add(subFolder);
                Messages.sprintf("##########updateLists Selected folder to scan: " + subFolder);
            }
        }
    }

    private void notifyDisconnectedIfNeeded(List<Path> notConnected) {
        if (!notConnected.isEmpty()) {
            List<Path> disconnectedPaths = new ArrayList<>(new LinkedHashSet<>(notConnected));
            Platform.runLater(() -> showDisconnectedDrivesDialog(disconnectedPaths));
        }
    }

    private void processRemovedFolders(List<Path> removedList) {
        for (Path path : removedList) {
            Messages.sprintf("##########removedList Selected folder to scan: " + path);
            TableUtils.removeFolderFromTables(modelMain.tables(), path);
        }
    }

    private void processNewFolders(List<Path> newLists, List<Path> updateLists, List<FolderInfo> folderInfos, Consumer<String> status) {
        for (Path path : newLists) {
            status.accept("Iterating through media files...: " + path.getFileName() + "\n");
            Messages.sprintf("#### FOLDER IS NEW and SELECTED: " + path);

            TableType tableType = resolveTableTypeByPath(path);
            FolderInfo folderInfo = FolderInfoDao.loadFolderInfo(path);
            if (folderInfo == null) {
                folderInfo = new FolderInfo(path);
                folderInfo.setTableType(tableType.getType());

                List<FileInfo> fileInfoList = FileInfoUtils.createFileInfo_list(folderInfo);
                if (fileInfoList != null && !fileInfoList.isEmpty()) {
                    folderInfo.setFileInfoList(fileInfoList);
                    FolderInfoUtils.calculateFolderInfoStatus(folderInfo);
                    folderInfo.setChanged(true);
                    Main.setChanged(true);
                    folderInfos.add(folderInfo);
                }
            } else {
                boolean added = modelMain.tables().addToTable(folderInfo);
                if (added) {
                    updateLists.add(path);
                }
            }
        }
    }

    private void processUpdatedFolders(List<Path> updateLists) throws Exception {
        for (Path updateFile : updateLists) {
            Messages.sprintf("##########updateLists Selected folder to scan: " + updateFile);
            FolderInfo folderInfo = TableUtils.findTableValues(updateFile, modelMain.tables());

            Messages.sprintf("##########updateLists folderInfo: " + folderInfo);
            if (folderInfo != null && folderInfo.getFileInfoList() != null && !folderInfo.getFileInfoList().isEmpty()) {
                List<FileInfo> existsFileInfos = new ArrayList<>();
                List<FileInfo> fileInfoList = folderInfo.getFileInfoList();
                List<Path> currentFolderMediaFilesOnly = FileUtils.getCurrentFolderMediaFilesOnly(updateFile, FileUtils.filter_directories);

                Iterator<Path> mediaFiles = currentFolderMediaFilesOnly.iterator();
                while (mediaFiles.hasNext()) {
                    Path currentFile = mediaFiles.next();
                    Optional<FileInfo> fileInfo = FileInfoUtils.findFileInfo(currentFile, fileInfoList);
                    if (fileInfo != null && fileInfo.isPresent()) {
                        Messages.sprintf("FileInfo already exists for: " + currentFile);
                        existsFileInfos.add(fileInfo.get());
                    } else {
                        FileInfo newFileInfo = FileInfoUtils.createFileInfo(currentFile);
                        if (newFileInfo != null) {
                            existsFileInfos.add(newFileInfo);
                            folderInfo.setChanged(true);
                            Messages.sprintf("FileInfo does not exist for: " + currentFile);
                        } else {
                            Messages.sprintfError("Could not create fileinfo: " + currentFile);
                        }
                    }
                    Messages.sprintf("*** ended folderFile: " + currentFile);
                }
            } else {
                Messages.sprintfError("Cannot find correct folderinfo: " + updateFile);
            }
        }
    }

    private void addNewFolderInfosToTables(List<FolderInfo> newFolderInfos) {
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
    }

    private static final class ScanBuckets {
        private final List<Path> newLists;
        private final List<Path> updateLists;
        private final List<Path> notConnected;
        private final List<Path> removedList;

        private ScanBuckets(List<Path> newLists, List<Path> updateLists, List<Path> notConnected, List<Path> removedList) {
            this.newLists = newLists;
            this.updateLists = updateLists;
            this.notConnected = notConnected;
            this.removedList = removedList;
        }
    }

    private void showDisconnectedDrivesDialog(List<Path> missingPaths) {
        if (missingPaths == null || missingPaths.isEmpty()) {
            return;
        }


        DisconnectedDrivesDialog dialog = new DisconnectedDrivesDialog(missingPaths, model_folderScanner.getScanDrives());
        dialog.showAndWait().ifPresent(response -> {
            if (response.getButtonData() == ButtonBar.ButtonData.OK_DONE) {

                List<Path> stillMissing = rescanAndCollectDisconnectedPaths();
                if (!stillMissing.isEmpty()) {
                    showDisconnectedDrivesDialog(stillMissing);
                }

            }
        });
    }

    private List<Path> rescanAndCollectDisconnectedPaths() {
        if (model_folderScanner != null && model_folderScanner.getScanDrives() != null) {
            model_folderScanner.getScanDrives().restart();
        }
        if (modelMain != null && modelMain.getMonitorExternalDriveConnectivity() != null) {
            modelMain.getMonitorExternalDriveConnectivity().restart();
        }
        return refreshConnectivityAndCollectDisconnectedPaths();
    }

    private List<Path> refreshConnectivityAndCollectDisconnectedPaths() {
        List<Path> disconnected = new ArrayList<>();

        for (SelectedFolder sf : modelMain.getSelectedFolders().getSelectedFolderScanner_obs()) {
            if (!sf.isSelected() || sf.isIgnored()) {
                continue;
            }

            Path resolvedPath = resolveSelectedFolderPathBySerial(sf);
            boolean connected = resolvedPath != null;
            sf.setConnected(connected);

            if (!connected) {
                disconnected.add(Paths.get(sf.getFolder()));
            }
        }

        if (selectedFolder_TableView != null) {
            selectedFolder_TableView.refresh();
        }

        return new ArrayList<>(new LinkedHashSet<>(disconnected));
    }

    private Path resolveSelectedFolderPathBySerial(SelectedFolder sf) {
        Path currentPath = Paths.get(sf.getFolder());
        if (Files.exists(currentPath)) {
            return currentPath;
        }

        String serial = sf.getDriveSerialNumber();
        if (serial == null || serial.isBlank()) {
            return null;
        }

        Path oldRoot = currentPath.getRoot();
        if (oldRoot == null) {
            return null;
        }

        Path relativePath;
        try {
            relativePath = oldRoot.relativize(currentPath);
        } catch (IllegalArgumentException ex) {
            return null;
        }

        for (DriveInfo driveInfo : modelMain.driveInfos()) {
            if (!driveInfo.isConnected() || !matchesDriveIdentity(serial, driveInfo)) {
                continue;
            }

            Path candidate = Paths.get(driveInfo.getDrivePath()).resolve(relativePath).normalize();
            if (Files.exists(candidate) && !oldRoot.toString().equalsIgnoreCase(driveInfo.getDrivePath())) {
                if (askUserForDriveRemapApproval(sf.getFolder(), candidate.toString())) {
                    sf.setFolder(candidate.toString());
                    return candidate;
                }
                return null;
            }
        }
        return null;
    }

    private boolean matchesDriveIdentity(String selectedIdentity, DriveInfo driveInfo) {
        if (selectedIdentity == null || selectedIdentity.isBlank() || driveInfo == null) {
            return false;
        }
        return selectedIdentity.equals(Objects.toString(driveInfo.getIdentifier(), "").trim())
                || selectedIdentity.equals(Objects.toString(driveInfo.getPartitionUuid(), "").trim())
                || selectedIdentity.equals(Objects.toString(driveInfo.getPartitionIdentification(), "").trim())
                || selectedIdentity.equals(Objects.toString(driveInfo.getSerial(), "").trim());
    }

    private boolean askUserForDriveRemapApproval(String oldPath, String newPath) {
        AtomicReference<Boolean> userApproved = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            String message = String.format(
                "Drive location has changed:\n\n"
                + "From: %s\n"
                + "To:   %s\n\n"
                + "Do you want to use the new location?",
                oldPath, newPath
            );

            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION
            );
            alert.setTitle("Drive Location Changed");
            alert.setHeaderText("Folder Location Remapped");
            alert.setContentText(message);

            Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
                userApproved.set(true);
            } else {
                userApproved.set(false);
            }
            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return false;
        }

        return Boolean.TRUE.equals(userApproved.get());
    }

    @FXML
    private void selectedFolders_cancel_action(ActionEvent event) {
        sprintf("selectedFolders_cancel_action  pressed");

        SelectionPropagation.syncTreeFromModel(modelMain);

        modelMain.getSelectedFolders().restore();

        for (SelectedFolder sf : modelMain.getSelectedFolders().getSelectedFolderScannerOriginal()) {
            Messages.sprintf("##########getSelectedFolderScannerOriginal folder: " + sf.getFolder() + " isSelected: " + sf.isSelected() + " isIgnored: " + sf.isIgnored() + " isConnected: " + sf.isConnected() + " hasMedia: " + sf.isMedia());
        }

        for (SelectedFolder sf : modelMain.getSelectedFolders().getSelectedFolderScanner_obs()) {
            Messages.sprintf("##########getSelectedFolderScanner_obs folder: " + sf.getFolder() + " isSelected: " + sf.isSelected() + " isIgnored: " + sf.isIgnored() + " isConnected: " + sf.isConnected() + " hasMedia: " + sf.isMedia());
        }

        List<SelectedFolder> modifiedFolders = findModifiedFolders(modelMain.getSelectedFolders().getSelectedFolderScannerOriginal(), modelMain.getSelectedFolders().getSelectedFolderScanner_obs());
        for (SelectedFolder sf : modifiedFolders) {
            Messages.sprintf("DIFF found: " + sf.getFolder() + " isSelected: " + sf.isSelected() + " isIgnored: " + sf.isIgnored() + " isConnected: " + sf.isConnected() + " hasMedia: " + sf.isMedia());
        }

//        modelMain.getSelectedFolders().getSelectedFolderScanner_obs().setAll(selectedFolderScannerOriginal);

//        boolean loadSelectedFolders = SelectedFolderInfoDao.loadSelectedFolders(modelMain);
//        if (!loadSelectedFolders) {
//            Messages.warningText(bundle.getString("errorLoadingSelectedFolders"));
//        }
//
        selectedFolder_TableView.setItems(modelMain.getSelectedFolders().getSelectedFolderScanner_obs());
        SelectionPropagation.syncTreeFromModel(modelMain);
        //  modelMain.getTabPaneMain().getSelectionModel().select(0); // Selecting tabMain
//        selectedFolder_TableView.setItems(model_main.getSelectedFolders().getSelectedFolderScanner_obs());
//        SelectionPropagation.syncTreeFromModel(model_main);
        modelMain.getTabPaneMain().getSelectionModel().select(0); // Selecting tabMain
    }

    public static List<SelectedFolder> findModifiedFolders(List<SelectedFolder> obs1, List<SelectedFolder> obs2) {
        if (obs1 == null || obs2 == null) return Collections.emptyList();

        // 1. Map obs2 by its unique ID for O(1) lookups
        Map<String, SelectedFolder> obs2Map = obs2.stream()
                .collect(Collectors.toMap(SelectedFolder::getFolder, Function.identity(), (a, b) -> a));

        // 2. Filter obs1 for items that exist in obs2 but have different boolean values
        return obs1.stream()
                .filter(f1 -> {
                    SelectedFolder f2 = obs2Map.get(f1.getFolder());
                    if (f2 == null) return false; // Exclude if it was deleted/not in obs2

                    // Check for boolean differences here
                    return f1.isSelected() != f2.isSelected() || f1.isMedia() != f2.isMedia();
                })
                .toList(); // Use .collect(Collectors.toList()) if you are on Java 11 or below
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
                modelMain.getSelectedFolders().add(SelectedFolder.create(folder.getAbsolutePath(), true, true, FileUtils.getHasMedia(folder.toString()), false));
            }
            Messages.sprintf("foldersAdded: " + foldersAdded + "  vs size: " + modelMain.getSelectedFolders().getSelectedFolderScanner_obs().size());
            if (foldersAdded != modelMain.getSelectedFolders().getSelectedFolderScanner_obs().size() || modelMain.getSelectedFolders().getSelectedFolderScanner_obs().isEmpty()) {
                modelMain.getSelectedFolders().getSelectedFolderScanner_obs().add(SelectedFolder.create(folder.getAbsolutePath(), true, true, FileUtils.getHasMedia(folder.toString()), false));
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

        selectedFolder_TableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                Messages.sprintf("Selected item: " + newSelection.getFolder());
                modelMain.getFolderSelectionService().focusFolder(newSelection);
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
        Messages.sprintf("getFolderScanner obs size: " + this.modelMain.getSelectedFolders().getSelectedFolderScanner_obs().size());

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
