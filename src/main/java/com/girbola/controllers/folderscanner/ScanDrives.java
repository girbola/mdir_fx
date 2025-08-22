package com.girbola.controllers.folderscanner;

import com.girbola.Main;
import com.girbola.controllers.main.ModelMain;
import com.girbola.drive.DriveInfo;
import com.girbola.drive.DriveInfoUtils;
import com.girbola.filelisting.ValidatePathUtils;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import common.utils.FileUtils;
import common.utils.OSHI_Utils;
import java.util.Arrays;

import javafx.application.Platform;
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

    private final String ERROR = ScanDrives.class.getSimpleName();

    private CheckBoxTreeItem<File> rootItem;
    private ObservableList<Path> driveListSelectedObs;
    private ModelFolderScanner modelFolderScanner;
    private Set<DriveInfo> rootDrives = new HashSet<>();
    private int i = 0;
    private int rootCount = 0;
    private DriveInfoUtils driveInfoUtils;
    private ModelMain modelMain;

    public ScanDrives(ModelMain modelMain, CheckBoxTreeItem<File> rootItem, ObservableList<Path> driveListSelectedObs,
                      DriveInfoUtils driveInfoUtils, ModelFolderScanner modelFolderScanner) {
        this.modelMain = modelMain;
        this.rootItem = rootItem;
        this.driveListSelectedObs = driveListSelectedObs;
        this.driveInfoUtils = driveInfoUtils;
        this.modelFolderScanner = modelFolderScanner;
        scanner.setPeriod(Duration.seconds(10));
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
            File media = new File(File.separator + "Volumes");
            return media.listFiles();
        } else if (osName.contains("nix") || osName.contains("nux")) {
            File media = new File(File.separator + "media");
            return media.listFiles();
        } else {
            throw new UnsupportedOperationException("Unsupported platform: " + osName);
        }
    }

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
        if(cb.isIndeterminate()) {
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

    private void handleWorkDirConflict(CheckBoxTreeItem<File> cb, Path selectedPath) {
        Platform.runLater(() -> {
            cb.setSelected(false);
            Messages.warningText(Main.bundle.getString("workDirConflict"));
            driveListSelectedObs.remove(selectedPath);
        });
    }

    private void processSelectedPath(CheckBoxTreeItem<File> cb, Path selectedPath) {
        if (Files.exists(selectedPath) && !selectedFolderHasValue(selectedPath)) {
            boolean hasMedia = FileUtils.getHasMedia(selectedPath.toFile());
            modelMain.getSelectedFolders().getSelectedFolderScanner_obs()
                    .add(new SelectedFolder(true, true, selectedPath.toString(), hasMedia));
        }
        modelFolderScanner.getSelectedDrivesFoldersListObs().add(selectedPath);
        sprintf("111drive selected: " + cb.getValue());
    }

    private void processDeselectedPath(CheckBoxTreeItem<File> cb, Path selectedPath) {
        sprintf("cb.selectedProperty de-selected path is: " + selectedPath);
        remove(cb.getValue().toString());
        driveListSelectedObs.remove(selectedPath);
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
        if(Main.getProcessCancelled()) {
            Messages.sprintfError("redrawRootFolders method stopped. Process cancelled");
            return;
        }
        for (DriveInfo driveInfo : rootDrives) {

            if(Main.getProcessCancelled()) {
                Messages.sprintfError("Iterating driveInfo were stopped. Process cancelled");
                break;
            }

            Messages.sprintf("Iterating root drives: " + driveInfo.getDrivePath() + " drive serial: "
                    + driveInfo.getIdentifier());

            File drive = new File(driveInfo.getDrivePath());

            CheckBoxTreeItem<File> checkBoxTreeItem = createBranch(drive);
            DirectoryStream<Path> stream = FileUtils.createDirectoryStream(Paths.get(driveInfo.getDrivePath()));
            if(stream == null) {
                Messages.sprintfError("Stream were null");
                return;
            }

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
                            Platform.runLater(() -> checkBoxTreeItem2.setSelected(true));
                            break;
                        }
                    }
                    checkBoxTreeItem2.selectedProperty().addListener((observable, oldValue, newValue) -> {
                        if (Boolean.TRUE.equals(newValue)) {
                            processSelectedPath(checkBoxTreeItem2,f);
                        } else {
                            processDeselectedPath(checkBoxTreeItem2, f);
                        }
                        modelFolderScanner.getSelectedDrivesFoldersListObs().add(f);
                        sprintf("222drive selected: " + f);
                        if (newValue == null) {
                            sprintf("null value");
                        }
                    });
                }
            }
            rootItem.getChildren().add(checkBoxTreeItem);
        }
    }

    private boolean updateRootDrives(File[] listOfRoots) {
        Set<DriveInfo> setOfRootDrives = new HashSet<>();
        modelMain.driveInfos();
        for (int i = 0; i < listOfRoots.length; i++) {
            if (Main.getProcessCancelled()) {
                break;
            }
    //TODO driveinfos ei huomioi olemassa olevia lisättyjä drivejnfoja vaan se lisää listaan kokoajan uutta.
            String serial = OSHI_Utils.getDriveSerialNumber(listOfRoots[i].toString());

            Messages.sprintf("seriallllllll: " + serial + " drive: " + listOfRoots[i].toString());
            DriveInfo driveInfo = new DriveInfo(listOfRoots[i].toString(), listOfRoots[i].getTotalSpace(), listOfRoots[i].exists(), false, serial);

            Messages.sprintf("Method… modelMain.driveInfos().size():  " + modelMain.driveInfos().size());
//            if(DriveInfoUtils.hasDrivePath(modelMain.driveInfos(),driveInfo.getDrivePath())) {
//                Messages.sprintf("Drive already in list: " + listOfRoots[i].toString());
//                continue;
//            } else {
//                Messages.sprintf("Drive is NEW: " + listOfRoots[i].toString());
//            }

            modelMain.driveInfos().add(driveInfo);

            setOfRootDrives.add(new DriveInfo(listOfRoots[i].toString(), listOfRoots[i].getTotalSpace(), listOfRoots[i].exists(), false, serial));

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