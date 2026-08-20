package com.girbola.controllers.folderscanner;

import com.girbola.Main;
import com.girbola.controllers.folderscanner.folderpicker.LazyDirTreeItem;
import com.girbola.controllers.main.ModelMain;
import com.girbola.drive.DriveInfo;
import com.girbola.drive.DriveInfoUtils;
import com.girbola.filelisting.ValidatePathUtils;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import common.utils.FileUtils;
import common.utils.OSHI_Utils;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.Separator;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.girbola.messages.Messages.sprintf;

public class ScanDrives {

    private final String ERROR = ScanDrives.class.getSimpleName();

    private AtomicInteger redraw = new AtomicInteger(0);

    private CheckBoxTreeItem<Path> rootItem;
    private ObservableList<Path> driveListSelectedObs;
    private ModelFolderScanner modelFolderScanner;
    private Set<DriveInfo> rootDrives = new HashSet<>();
    private int i = 0;
    private int rootCount = 0;
    private DriveInfoUtils driveInfoUtils;
    private ModelMain modelMain;

    public ScanDrives(ModelMain modelMain, CheckBoxTreeItem<Path> rootItem, ObservableList<Path> driveListSelectedObs,
                      DriveInfoUtils driveInfoUtils, ModelFolderScanner modelFolderScanner) {
        this.modelMain = modelMain;
        this.rootItem = rootItem;
        this.driveListSelectedObs = driveListSelectedObs;
        this.driveInfoUtils = driveInfoUtils;
        this.modelFolderScanner = modelFolderScanner;
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
        protected Task createTask() {
            return new Task<Integer>() {
                @Override
                protected Integer call() throws Exception {

                    File[] listOfRoots = getListOfRoots();

                    if(listOfRoots.length == 0) {
                        Messages.sprintf("Drive count changed from: " + rootCount + " to: " + listOfRoots.length);
                        return null;
                    }
                    Arrays.sort(listOfRoots, (File f1, File f2) -> f1.getAbsolutePath().compareToIgnoreCase(f2.getAbsolutePath()));

                    if (listOfRoots != null) {
                        if (updateRootDrives(listOfRoots)) {
                            Messages.sprintf("Updating root drives: " + rootDrives.size());
                            rootItem.getChildren().clear();
                            rootCount = listOfRoots.length;

                            redrawRootFolders();
                        }
                    } else {
                        Messages.errorSmth(ERROR, "Listing Drives list were null", null, Misc.getLineNumber(), false);
                        Main.setProcessCancelled(true);
                    }
                    return null;
                }


            };
        }

    };

    private File[] getListOfRoots() {

        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            return File.listRoots();
        } else if (osName.contains("mac")) {
//            Map<CommonUserFolders.Kind, Path> resolve = CommonUserFolders.resolve();
//            File[] userFolders = new File[resolve.size()];
//            userFolders.addAll(resolve.values().stream().map(Path::toFile).toList());
//            return userFolders;
//
            File media = new File(File.separator + "Volumes");
            return media.listFiles();
        } else if (osName.contains("nix") || osName.contains("nux")) {
            File media = new File(File.separator + "media");
            return media.listFiles();
        } else {
            throw new UnsupportedOperationException("Unsupported platform: " + osName);
        }
    }

    private CheckBoxTreeItem<Path> createBranch(Path fileName) {
        CheckBoxTreeItem<Path> cb = new CheckBoxTreeItem<>(fileName);
        cb.setExpanded(true);
        cb.selectedProperty().addListener((observable, oldValue, newValue) -> handleSelectionChange(cb, newValue));

//        initializeCheckBoxSelection(cb, fileName);
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
        Platform.runLater(() -> {
//            remove(cb.getValue().toString());
            driveListSelectedObs.remove(selectedPath);
        });
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

    private String getDriveKey(DriveInfo driveInfo) {
        return Objects.toString(driveInfo.getIdentifier(), "") + "|"
                + Objects.toString(driveInfo.getDrivePath(), "") + "|"
                + driveInfo.getDriveTotalSize();
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

    private boolean updateRootDrives(File[] roots) {
        Set<DriveInfo> latestRootDrives = new HashSet<>();
        Set<String> latestDriveKeys = new HashSet<>();
        boolean changed = false;

        for (int i = 0; i < roots.length; i++) {
            if (Main.getProcessCancelled()) {
                break;
            }
            String serial = OSHI_Utils.getDriveSerialNumber(roots[i].toString());

            Messages.sprintf("ROOT DRIVE: " + roots[i] +  " seriallllllll: " + serial + " drive: " + roots[i].toString());
            DriveInfo driveInfo = new DriveInfo(roots[i].toString(), roots[i].getTotalSpace(), roots[i].exists(), false, serial);
            String driveKey = getDriveKey(driveInfo);
            latestRootDrives.add(driveInfo);
            latestDriveKeys.add(driveKey);

            if (!hasDriveInfo(driveInfo, modelMain.driveInfos())) {
                Messages.sprintf("New drive detected: " + driveInfo.getDrivePath() + " serial: " + driveInfo.getIdentifier());
                driveInfo.setSelected(false);
                driveInfo.setConnected(true);
                modelMain.driveInfos().add(driveInfo);
                changed = true;
            } else {
                for (DriveInfo existing : modelMain.driveInfos()) {
                    Messages.sprintf("Existing Drive: " + existing.getDrivePath() + " serial: " + existing.getIdentifier());
                    if (getDriveKey(existing).equals(driveKey)) {
                        existing.setConnected(true);
                        break;
                    }
                }
            }
Messages.sprintf("selectedFolderScanner osb size::: ");
            Messages.sprintf("selectedFolderScanner osb size::: " + modelMain.getSelectedFolders().getSelectedFolderScanner_obs().size());

            for(SelectedFolder sf : modelMain.getSelectedFolders().getSelectedFolderScanner_obs()) {
                Messages.sprintf("sf.selectedFolders size " + sf.getFolder() + " rootFolder: " + driveInfo.getIdentifier() + " driveInfo getDrive: " + driveInfo.getDrive() );
                if(sf.isSelected()) {
                    if (driveInfo.isConnected() && sf.getDriveSerialNumber().equals(driveInfo.getIdentifier())) {
                        Messages.sprintf("drive connected!!::  " + driveInfo.getDrivePath());
                        sf.setConnected(true);
                    } else if (!driveInfo.isConnected() && sf.getDriveSerialNumber().equals(driveInfo.getIdentifier())) {
                        Messages.sprintf("drive not connected!!::  " + driveInfo.getDrivePath());
                        sf.setConnected(false);
                    }
                }
            }
        }

        for (DriveInfo driveInfo : modelMain.driveInfos()) {
            boolean isConnectedNow = latestDriveKeys.contains(getDriveKey(driveInfo));
            driveInfo.setConnected(isConnectedNow);
        }

        Set<String> currentDriveKeys = new HashSet<>();
        for (DriveInfo driveInfo : rootDrives) {
            currentDriveKeys.add(getDriveKey(driveInfo));
        }

        if (!currentDriveKeys.equals(latestDriveKeys)) {
            changed = true;
        }

        if (changed) {
            rootDrives.clear();
            rootDrives.addAll(latestRootDrives);
            Messages.sprintf("Updated root drives, count: " + rootDrives.size());
        }

        return changed;
    }

    private boolean hasDriveInfo(DriveInfo driveInfo, List<DriveInfo> driveInfos) {

        for (DriveInfo driveInfoToSearch : driveInfos) {
            if (getDriveKey(driveInfoToSearch).equals(getDriveKey(driveInfo))) {
                Messages.sprintf("Right identifier found!" + driveInfo.getDrivePath());
                return true;
            }
        }
        return false;

    }

    private void remove(String value) {
        Iterator<SelectedFolder> it = modelMain.getSelectedFolders().getSelectedFolderScanner_obs().iterator();
        while (it.hasNext()) {
            SelectedFolder selectedFolder = it.next();
            if (selectedFolder.getFolder().equals(value)) {
                it.remove();
                break;
            }
        }
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
