package com.girbola.controllers.folderscanner;

import com.girbola.Main;
import com.girbola.controllers.main.ModelMain;
import com.girbola.drive.DriveInfo;
import com.girbola.drive.DrivesListHandler;
import com.girbola.filelisting.ValidatePathUtils;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import common.utils.FileUtils;
import common.utils.OSHI_Utils;
import java.util.Arrays;
import java.util.Collections;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static com.girbola.messages.Messages.sprintf;

public class ScanDrives {

    private CheckBoxTreeItem<File> rootItem;
    private ObservableList<Path> driveListSelectedObs;
    private ModelFolderScanner modelFolderScanner;
    private Set<DriveInfo> rootDrives = new HashSet<>();
    private int i = 0;
    private int rootCount = 0;
    private DrivesListHandler driveListHandler;
    private final static String ERROR = ScanDrives.class.getSimpleName();
    private ModelMain modelMain;

    public ScanDrives(ModelMain modelMain, CheckBoxTreeItem<File> rootItem, ObservableList<Path> driveListSelectedObs,
                      DrivesListHandler driveListHandler, ModelFolderScanner modelFolderScanner) {
        this.modelMain = modelMain;
        this.rootItem = rootItem;
        this.driveListSelectedObs = driveListSelectedObs;
        this.driveListHandler = driveListHandler;
        this.modelFolderScanner = modelFolderScanner;
        scanner.setPeriod(Duration.seconds(10));
    }

    public void restart() {
        scanner.restart();
    }

    public void stop() {
        scanner.cancel();
        sprintf("Scanning cancelled? " + scanner.isRunning());
    }

    ScheduledService<Void> scanner = new ScheduledService<Void>() {

        @Override
        protected Task createTask() {
            return new Task<Integer>() {
                @Override
                protected Integer call() throws Exception {

                    File[] listOfRoots = null;
                    if (com.sun.jna.Platform.isWindows()) {
                        listOfRoots = File.listRoots();
                    } else if (com.sun.jna.Platform.isMac()) {
                        File media = new File(File.separator + "/Volumes");
                        listOfRoots = media.listFiles();
                    } else if (com.sun.jna.Platform.isLinux()) {
                        File media = new File(File.separator + "media");
                        listOfRoots = media.listFiles();
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
                    }
                    return null;
                }


            };
        }

    };

    private CheckBoxTreeItem<File> createBranch(File fileName) {
        CheckBoxTreeItem<File> cb = new CheckBoxTreeItem<>(fileName);
        cb.setExpanded(true);
        cb.selectedProperty().addListener((observable, oldValue, newValue) -> handleSelectionChange(cb, newValue));
        initializeCheckBoxSelection(cb, fileName);
        return cb;
    }

    private void handleSelectionChange(CheckBoxTreeItem<File> cb, Boolean isSelected) {
        Path selectedPath = Paths.get(cb.getValue().toString());
        sprintf("cb.selectedProperty path is: " + selectedPath);

        if (Boolean.TRUE.equals(isSelected)) {
            if (Main.conf.getWorkDir().equals(selectedPath.toString())) {
                handleWorkDirConflict(cb, selectedPath);
            } else {
                processSelectedPath(selectedPath, cb);
            }
        } else {
            processDeselectedPath(cb, selectedPath);
        }
        driveListHandler.createDriveInfo(cb.getValue().toString(), isSelected);
    }

    private void handleWorkDirConflict(CheckBoxTreeItem<File> cb, Path selectedPath) {
        Platform.runLater(() -> {
            cb.setSelected(false);
            Messages.warningText(Main.bundle.getString("workDirConflict"));
            driveListSelectedObs.remove(selectedPath);
        });
    }

    private void processSelectedPath(Path selectedPath, CheckBoxTreeItem<File> cb) {
        if (Files.exists(selectedPath) && !selectedFolderHasValue(selectedPath)) {
            boolean hasMedia = FileUtils.getHasMedia(selectedPath.toFile());
            modelMain.getSelectedFolders().getSelectedFolderScanner_obs()
                    .add(new SelectedFolder(true, true, selectedPath.toString(), hasMedia));
        }
        modelFolderScanner.getSelectedDrivesFoldersListObs().add(selectedPath);
        sprintf("drive selected: " + cb.getValue());
    }

    private void processDeselectedPath(CheckBoxTreeItem<File> cb, Path selectedPath) {
        sprintf("cb.selectedProperty de-selected path is: " + selectedPath);
        remove(cb.getValue().toString());
        driveListSelectedObs.remove(selectedPath);
    }

    private void initializeCheckBoxSelection(CheckBoxTreeItem<File> cb, File fileName) {
        if (driveListHandler.isDriveAlreadyInRegister(fileName.toString())) {
            driveListHandler.getDrivesList_obs().stream()
                    .filter(driveInfo -> driveInfo.getDrivePath().equals(fileName.toString()))
                    .findFirst()
                    .ifPresent(driveInfo -> cb.setSelected(driveInfo.isSelected()));
        } else {
            Messages.sprintf("has no drive in list");
            driveListHandler.createDriveInfo(cb.getValue().toString(), false);
        }
    }

    private boolean findDuplicateDrive(DriveInfo driveInfoToSearch) {
        for (DriveInfo driveInfo : rootDrives) {
            if (Main.getProcessCancelled()) {
                break;
            }
            Messages.sprintf("222driveInfo: " + driveInfo.getDrivePath() + " serial: " + driveInfo.getIdentifier());

            if (driveInfo.getIdentifier().equals(driveInfoToSearch.getIdentifier())) {
                Messages.sprintf("Right identifier found!" + driveInfo.getDrivePath());
                return true;
            }
        }
        Messages.sprintf("Right identifier were NOT found!");
        return false;
    }

    private void redrawRootFolders() throws IOException {
        for (DriveInfo driveInfo : rootDrives) {

            Messages.sprintf("Iterating root drives: " + driveInfo.getDrivePath() + " drive serial: "
                    + driveInfo.getIdentifier());

            File drive = new File(driveInfo.getDrivePath());

            CheckBoxTreeItem<File> checkBoxTreeItem = createBranch(drive);
            DirectoryStream<Path> stream = FileUtils.createDirectoryStream(Paths.get(driveInfo.getDrivePath()));
            for (Path f : stream) {
                if (ValidatePathUtils.validFolder(f)) {
                    Messages.sprintf("==== validfolderstream file: " + f);

                    CustomCheckBoxTreeItem checkBoxTreeItem2 = new CustomCheckBoxTreeItem<>(modelMain, f);
                    checkBoxTreeItem.getChildren().add(checkBoxTreeItem2);

                    Iterator<SelectedFolder> iterator = modelMain.getSelectedFolders().getSelectedFolderScanner_obs().iterator();

                    while (iterator.hasNext()) {
                        SelectedFolder sf = iterator.next();
                        Messages.sprintf("==== validfolderstream file: " + f);
                        if (sf.getFolder().equals(f.toFile().getAbsolutePath())) {
                            Platform.runLater(() -> checkBoxTreeItem.setSelected(true));
                            break;
                        }
                    }
                }
            }
            rootItem.getChildren().add(checkBoxTreeItem);
        }
    }

    private boolean updateRootDrives(File[] listOfRoots) {
        Set<DriveInfo> setOfRootDrives = new HashSet<>();

        for (int i = 0; i < listOfRoots.length; i++) {
            if (Main.getProcessCancelled()) {
                break;
            }
            String serial = OSHI_Utils.getDriveSerialNumber(listOfRoots[i].toString());
            Messages.sprintf("seriallllllll: " + serial + " drive: " + listOfRoots[i].toString());
            modelMain.getWorkDirSQL().registerDrive(listOfRoots[i].toString(), serial);
            setOfRootDrives.add(new DriveInfo(listOfRoots[i].toString(), listOfRoots[i].getTotalSpace(),
                    listOfRoots[i].exists(), false, serial));
        }

        for (DriveInfo driveInfo : setOfRootDrives) {
            if (Main.getProcessCancelled()) {
                break;
            }
            if (!findDuplicateDrive(driveInfo)) {
                Messages.sprintf("Adding all to root Drives. DriveInfo: " + driveInfo.getDrivePath()
                        + " serial: " + driveInfo.getIdentifier() + " setOfRootDrives size: "
                        + setOfRootDrives.size());
                rootDrives.clear();
                rootDrives.addAll(setOfRootDrives);
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