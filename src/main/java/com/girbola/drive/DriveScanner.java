package com.girbola.drive;

import static com.girbola.messages.Messages.sprintf;

import com.girbola.Main;
import com.girbola.controllers.folderscanner.ModelFolderScanner;
import com.girbola.controllers.folderscanner.SelectedFolder;
import com.girbola.controllers.folderscanner.folderpicker.LazyDirTreeItem;
import com.girbola.controllers.main.ModelMain;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.persistence.drive.DriveInfoDao;
import common.utils.FileUtils;
import common.utils.OSHI_Utils;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.util.Duration;
import oshi.hardware.HWDiskStore;


public class DriveScanner {

    private static final String ERROR = DriveScanner.class.getSimpleName();

    private final CheckBoxTreeItem<Path> rootItem;
    private final ObservableList<Path> driveListSelectedObs;
    private final ObservableList<HWDiskStore> rootDrives;
    private final List<String> rootDrivePaths = new ArrayList<>();
    private final ModelFolderScanner modelFolderScanner;
    private final DriveInfoUtils driveInfoUtils;
    private final ModelMain modelMain;
    private final SelectedFolderDriveManager selectedFolderDriveManager;
    private final DriveInventoryScanner driveInventoryScanner = new DriveInventoryScanner();

    private int rootCount;

    public DriveScanner(ModelMain modelMain,
                        CheckBoxTreeItem<Path> rootItem,
                        ObservableList<Path> driveListSelectedObs,
                        DriveInfoUtils driveInfoUtils,
                        ModelFolderScanner modelFolderScanner) {
        this.modelMain = modelMain;
        this.rootItem = rootItem;
        this.rootDrives = FXCollections.observableArrayList();
        this.driveListSelectedObs = driveListSelectedObs;
        this.driveInfoUtils = driveInfoUtils;
        this.modelFolderScanner = modelFolderScanner;
        this.selectedFolderDriveManager = new SelectedFolderDriveManager(modelMain);
        scanner.setPeriod(Duration.seconds(30));
    }

    public void restart() {
        scanner.restart();
    }

    public void stop() {
        scanner.cancel();
        sprintf("Scanning cancelled and it shouldn't run? " + scanner.isRunning());
    }

    ScheduledService<Void> scanner = new ScheduledService<>() {
        @Override
        protected Task<Void> createTask() {
            return new Task<>() {
                @Override
                protected Void call() {
                    List<Path> listOfRoots = new ArrayList<>();
                    for (Path root : FileSystems.getDefault().getRootDirectories()) {
                        listOfRoots.add(root);
                    }

                    if (listOfRoots.isEmpty()) {
                        Messages.sprintf("No drives found. Drive count changed from: " + rootCount + " to: " + listOfRoots.size());
                        return null;
                    }

                    listOfRoots.sort((p1, p2) -> p1.toAbsolutePath().toString().compareToIgnoreCase(p2.toAbsolutePath().toString()));

                    if (updateRootDrives(listOfRoots)) {
                        Messages.sprintf("Updating root drives: " + rootDrives.size());
                        rootItem.getChildren().clear();
                        rootCount = listOfRoots.size();

                        try {
                            redrawRootFolders();
                        } catch (IOException e) {
                            Messages.errorSmth(ERROR, "Unable to redraw root folders", e, Misc.getLineNumber(), false);
                        }
                        checkConnectivityOfSelectedFolders();
                    }

                    return null;
                }
            };
        }

    };

    private void checkConnectivityOfSelectedFolders() {
        selectedFolderDriveManager.updateConnectivity(rootDrives);
    }

    private CheckBoxTreeItem<Path> createBranch(Path fileName) {
        CheckBoxTreeItem<Path> cb = new CheckBoxTreeItem<>(fileName);
        cb.setExpanded(true);
        cb.selectedProperty().addListener((observable, oldValue, newValue) -> handleSelectionChange(cb, newValue));
        return cb;
    }

    private void handleSelectionChange(CheckBoxTreeItem<Path> cb, Boolean isSelected) {
        Path selectedPath = Paths.get(cb.getValue().toString());
        sprintf("cb.selectedProperty path is: " + selectedPath);
        if (cb.isIndeterminate()) {
            return;
        }
        if (Boolean.TRUE.equals(isSelected)) {
            if (Main.conf.getWorkDir().equals(selectedPath.toString())) {
                handleWorkDirConflict(cb, selectedPath);
            } else {
                processSelectedPath(cb, selectedPath);
            }
        } else {
            processDeselectedPath(cb, selectedPath);
        }
        driveInfoUtils.createDriveInfo(cb.getValue().toString(), isSelected);
    }

    private void handleWorkDirConflict(CheckBoxTreeItem<Path> cb, Path selectedPath) {
        Platform.runLater(() -> {
            cb.setSelected(false);
            Messages.warningText(Main.bundle.getString("workDirConflict"));
            driveListSelectedObs.remove(selectedPath);
        });
    }

    private void processSelectedPath(CheckBoxTreeItem<Path> cb, Path selectedPath) {
        Messages.sprintf("cb.selectedProperty selected path is: " + selectedPath);
        if (Files.exists(selectedPath) && !selectedFolderHasValue(selectedPath)) {
            boolean hasMedia = FileUtils.getHasMedia(selectedPath.toFile());
            modelMain.getSelectedFolders().getSelectedFolderScanner_obs()
                    .add(SelectedFolder.create(selectedPath.toString(), true, true, hasMedia, false));
        } else {
            Messages.sprintf("processSelectedPath Folder already exists: " + selectedPath);
        }
        modelFolderScanner.getSelectedDrivesFoldersListObs().add(selectedPath);
        sprintf("111drive selected: " + cb.getValue());
    }

    private void processDeselectedPath(CheckBoxTreeItem<Path> cb, Path selectedPath) {
        sprintf("cb.selectedProperty de-selected path is: " + selectedPath);
        Platform.runLater(() -> driveListSelectedObs.remove(selectedPath));
    }

    private void initializeCheckBoxSelection(CheckBoxTreeItem<File> cb, File fileName) {
        if (driveInfoUtils.isDriveAlreadyInRegister(fileName.toString())) {
            driveInfoUtils.getDrivesList_obs().stream()
                    .filter(driveInfo -> driveInfo.getDrivePath().equals(fileName.toString()))
                    .findFirst()
                    .ifPresent(driveInfo -> cb.setSelected(driveInfo.isSelected()));
        } else {
            Messages.sprintf("has no drive in list");
            driveInfoUtils.createDriveInfo(cb.getValue().toString(), false);
        }
    }

    // Snapshot is exposed as a copy so callers can compare later without mutating scanner state.
    public List<HWDiskStore> getRootDrivesSnapshot() {
        return new ArrayList<>(rootDrives);
    }

    public List<String> getRootDriveKeysSnapshot() {
        return DriveScannerUtils.toDiskKeys(rootDrives);
    }

    private void redrawRootFolders() throws IOException {
        if (Main.getProcessCancelled()) {
            Messages.sprintfError("redrawRootFolders method stopped. Process cancelled");
            return;
        }

        for (Path r : FileSystems.getDefault().getRootDirectories()) {
            rootItem.getChildren().add(new LazyDirTreeItem(r));
        }
    }

    private boolean updateRootDrives(List<Path> roots) {
        List<HWDiskStore> hardwareDisks = OSHI_Utils.getHWDiskStore();
        DriveInventoryScanner.Result result = driveInventoryScanner.scanRoots(
                roots, hardwareDisks, modelMain.driveInfos(), Main::getProcessCancelled);

        selectedFolderDriveManager.refreshConnections(result.rootDrives());
        selectedFolderDriveManager.remapSelectedFoldersToCurrentDriveLetters(roots, hardwareDisks);
        DriveInventoryScanner.updateConnectivity(modelMain.driveInfos(), result.driveKeys());

        boolean changed = DriveInventoryScanner.hasChanged(
                result, DriveScannerUtils.toDiskKeys(rootDrives), rootDrivePaths);

        if (changed) {
            Platform.runLater(() -> {
                rootDrives.clear();
                rootDrives.addAll(result.rootDrives());
            });
            rootDrivePaths.clear();
            rootDrivePaths.addAll(result.rootPaths());

            DriveInfoDao.addDriveInfos(modelMain.driveInfos());
            Messages.sprintf("Updated root drives, count: " + rootDrives.size());
        }

        return changed;
    }


    private boolean selectedFolderHasValue(Path path) {
        for (SelectedFolder sf : modelMain.getSelectedFolders().getSelectedFolderScanner_obs()) {
            if (Paths.get(sf.getFolder()).equals(path)) {
                return true;
            }
        }
        return false;
    }


}
