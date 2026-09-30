package com.girbola.drive;

import com.girbola.Main;
import com.girbola.controllers.folderscanner.ModelFolderScanner;
import com.girbola.controllers.folderscanner.SelectedFolder;
import com.girbola.controllers.folderscanner.folderpicker.LazyDirTreeItem;
import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.persistence.folderinfo.FolderInfoDao;
import com.girbola.persistence.selectedfolderinfo.SelectedFolderInfoDao;
import common.utils.FileUtils;
import common.utils.OSHI_Utils;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TableView;
import javafx.util.Duration;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HWPartition;

import static com.girbola.messages.Messages.sprintf;

public class DriveScanner {

    private final String ERROR = DriveScanner.class.getSimpleName();

    private AtomicInteger redraw = new AtomicInteger(0);

    private CheckBoxTreeItem<Path> rootItem;

    private ObservableList<Path> driveListSelectedObs;
    private ObservableList<HWDiskStore> rootDrives;
    private List<String> rootDrivePaths = new ArrayList<>();

    private ModelFolderScanner modelFolderScanner;

    private int rootCount = 0;
    private DriveInfoUtils driveInfoUtils;
    private ModelMain modelMain;


    private int counter = 1;


    public DriveScanner(ModelMain modelMain, CheckBoxTreeItem<Path> rootItem, ObservableList<Path> driveListSelectedObs,
                        DriveInfoUtils driveInfoUtils, ModelFolderScanner modelFolderScanner) {
        this.modelMain = modelMain;
        this.rootItem = rootItem;
        this.rootDrives = FXCollections.observableArrayList();
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

                    List<Path> listOfRoots = new ArrayList<>();
                    for (Path root : FileSystems.getDefault().getRootDirectories()) {
                        listOfRoots.add(root);
                    }

                    if (listOfRoots.isEmpty()) {
                        Messages.sprintf("No drives found. Drive count changed from: " + rootCount + " to: " + listOfRoots.size());
                        return null;
                    }

                    listOfRoots.sort((Path p1, Path p2) -> p1.toAbsolutePath().toString().compareToIgnoreCase(p2.toAbsolutePath().toString()));

                    if (listOfRoots != null) {
                        if (updateRootDrives(listOfRoots)) {
                            Messages.sprintf("Updating root drives: " + rootDrives.size());
                            rootItem.getChildren().clear();
                            rootCount = listOfRoots.size();

                            redrawRootFolders();
                            checkConnectivityOfSelectedFolders();
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

    private void checkConnectivityOfSelectedFolders() {
        for (SelectedFolder sf : modelMain.getSelectedFolders().getSelectedFolderScanner_obs()) {
            if (!sf.isSelected()) {
                continue;
            }

            HWDiskStore matchingDrive = findMatchingDriveForSelectedFolder(sf, rootDrives);
            boolean connected = matchingDrive != null;
            String resolvedIdentity = connected ? resolveDiskIdentity(matchingDrive) : sf.getDriveSerialNumber();

            Platform.runLater(() -> {
                sf.setConnected(connected);
                if (connected && !Objects.equals(sf.getDriveSerialNumber(), resolvedIdentity)
                        && !resolvedIdentity.isBlank()) {
                    sf.setDriveSerialNumber(resolvedIdentity);
                }
            });
        }
    }

    private File[] getListOfRoots() {
        Set<Path> roots = new HashSet<>();

        // Always include JVM-visible roots (works on all OSes)
        for (Path root : FileSystems.getDefault().getRootDirectories()) {
            roots.add(root.toAbsolutePath().normalize());
        }

        // Add common Unix/macOS mount points
        addMountedChildren(roots, Paths.get("/Volumes")); // macOS
        addMountedChildren(roots, Paths.get("/media"));   // Linux common
        addMountedChildren(roots, Paths.get("/mnt"));     // Linux/custom mounts

        return roots.stream()
                .filter(Files::exists)
                .filter(Files::isDirectory)
                .map(Path::toFile)
                .sorted((a, b) -> a.getAbsolutePath().compareToIgnoreCase(b.getAbsolutePath()))
                .toArray(File[]::new);
    }

    private void addMountedChildren(Set<Path> roots, Path mountParent) {
        if (!Files.isDirectory(mountParent)) {
            return;
        }
        File[] children = mountParent.toFile().listFiles();
        if (children == null) {
            return;
        }
        for (File child : children) {
            if (child != null && child.exists() && child.isDirectory()) {
                roots.add(child.toPath().toAbsolutePath().normalize());
            }
        }
    }

    private File[] getListOfRoots_old() {

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
        return resolveDriveIdentity(driveInfo) + "|" + driveInfo.getDriveTotalSize();
    }

    private String getDiskKey(HWDiskStore diskStore) {
        return resolveDiskIdentity(diskStore) + "|" + diskStore.getSize();
    }

    private String resolveDriveIdentity(DriveInfo driveInfo) {
        String identifier = Objects.toString(driveInfo.getIdentifier(), "").trim();
        if (!identifier.isBlank()) {
            return identifier;
        }

        String partitionUuid = Objects.toString(driveInfo.getPartitionUuid(), "").trim();
        if (!partitionUuid.isBlank()) {
            return partitionUuid;
        }

        String partitionIdentification = Objects.toString(driveInfo.getPartitionIdentification(), "").trim();
        if (!partitionIdentification.isBlank()) {
            return partitionIdentification;
        }

        String serial = Objects.toString(driveInfo.getSerial(), "").trim();
        if (!serial.isBlank()) {
            return serial;
        }

        String drivePath = Objects.toString(driveInfo.getDrivePath(), "").trim();
        if (!drivePath.isBlank()) {
            String fallbackIdentity = Objects.toString(OSHI_Utils.getDriveSerialNumber(drivePath), "").trim();
            if (!fallbackIdentity.isBlank()) {
                return fallbackIdentity;
            }
            return drivePath;
        }

        return "";
    }

    private String resolveDiskIdentity(HWDiskStore diskStore) {
        String serial = Objects.toString(diskStore.getSerial(), "").trim();
        if (!serial.isBlank()) {
            return serial;
        }

        String name = Objects.toString(diskStore.getName(), "").trim();
        if (!name.isBlank()) {
            return name;
        }

        return Objects.toString(diskStore.getModel(), "").trim();
    }

    private void refreshSelectedFolderConnections(List<HWDiskStore> currentDrives) {
        List<SelectedFolderConnectionState> updates = new ArrayList<>();

        for (SelectedFolder sf : modelMain.getSelectedFolders().getSelectedFolderScanner_obs()) {
            if (!sf.isSelected()) {
                continue;
            }

            HWDiskStore matchingDrive = findMatchingDriveForSelectedFolder(sf, currentDrives);
            boolean connected = matchingDrive != null;
            String resolvedIdentity = connected ? resolveDiskIdentity(matchingDrive) : sf.getDriveSerialNumber();
            updates.add(new SelectedFolderConnectionState(sf, connected, resolvedIdentity));
        }

        Platform.runLater(() -> applySelectedFolderConnections(updates));
    }

    private void applySelectedFolderConnections(List<SelectedFolderConnectionState> updates) {
        for (SelectedFolderConnectionState update : updates) {
            update.selectedFolder.setConnected(update.connected);
            if (update.connected && !Objects.equals(update.selectedFolder.getDriveSerialNumber(), update.resolvedIdentity)
                    && !update.resolvedIdentity.isBlank()) {
                update.selectedFolder.setDriveSerialNumber(update.resolvedIdentity);
            }
        }
    }

    private HWDiskStore findMatchingDriveForSelectedFolder(SelectedFolder sf, List<HWDiskStore> currentDrives) {
        String selectedIdentity = Objects.toString(sf.getDriveSerialNumber(), "").trim();

        for (DriveInfo driveInfo : modelMain.driveInfos()) {
            if (!driveInfo.isConnected() || !matchesDriveIdentity(selectedIdentity, driveInfo)) {
                continue;
            }
            HWDiskStore diskStore = OSHI_Utils.findDiskStoreForRoot(Paths.get(driveInfo.getDrivePath()), currentDrives);
            if (diskStore != null) {
                return diskStore;
            }
        }

        for (HWDiskStore diskStore : currentDrives) {
            if (Objects.equals(sf.getDriveSerialNumber(), resolveDiskIdentity(diskStore))) {
                return diskStore;
            }

            if (matchesDriveByFolderStructure(sf.getFolder(), diskStore)) {
                return diskStore;
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

    private boolean matchesDriveByFolderStructure(String selectedFolderPath, HWDiskStore diskStore) {
        try {
            Path selectedPath = Paths.get(selectedFolderPath);
            Path selectedRoot = selectedPath.getRoot();
            if (selectedRoot == null || !selectedPath.startsWith(selectedRoot)) {
                return false;
            }

            Path relativeToRoot = selectedRoot.relativize(selectedPath);
            for (String mountPoint : getDiskMountPoints(diskStore)) {
                Path candidatePath = Paths.get(mountPoint).resolve(relativeToRoot).normalize();
                if (Files.exists(candidatePath)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            Messages.sprintfError("Unable to compare folder structure for path: " + selectedFolderPath + " disk: " + diskStore + " error: " + e.getMessage());
            return false;
        }
    }

    private static final class SelectedFolderConnectionState {
        private final SelectedFolder selectedFolder;
        private final boolean connected;
        private final String resolvedIdentity;

        private SelectedFolderConnectionState(SelectedFolder selectedFolder, boolean connected, String resolvedIdentity) {
            this.selectedFolder = selectedFolder;
            this.connected = connected;
            this.resolvedIdentity = Objects.toString(resolvedIdentity, "").trim();
        }
    }

    // Snapshot is exposed as a copy so callers can compare later without mutating scanner state.
    public List<HWDiskStore> getRootDrivesSnapshot() {
        return new ArrayList<>(rootDrives);
    }

    public List<String> getRootDriveKeysSnapshot() {
        return toDiskKeys(rootDrives);
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
        List<HWDiskStore> latestRootDrives = new ArrayList<>();
        Set<String> latestDriveKeys = new HashSet<>();
        boolean changed = false;
        List<HWDiskStore> hardwareDisks = OSHI_Utils.getHWDiskStore();

        for (Path rootPath : roots) {
            Messages.sprintf("##updateRootDrives Updating root drives from path: " + rootPath.toString());
            if (Main.getProcessCancelled()) {
                break;
            }
            if (!Files.exists(rootPath)) {
                Messages.sprintfError("Drive path does not exist: " + rootPath);
                continue;
            }

            HWDiskStore diskStore = OSHI_Utils.findDiskStoreForRoot(rootPath, hardwareDisks);
            if (diskStore == null) {
                Messages.sprintfError("No HWDiskStore match found for root path: " + rootPath);
                continue;
            }

            HWPartition matchingPartition = OSHI_Utils.findPartitionForRoot(rootPath, diskStore);
            String driveIdentifier = resolveDriveIdentifier(diskStore, matchingPartition);
            String diskSerial = Objects.toString(diskStore.getSerial(), "").trim();

            Messages.sprintf("ROOT DRIVE: " + rootPath + " identifier: " + driveIdentifier + " drive: " + rootPath);
            DriveInfo driveInfo = new DriveInfo(rootPath.toString(), diskStore.getSize(), rootPath.toFile().exists(), false, driveIdentifier);
            driveInfo.setDrive(rootPath);
            driveInfo.setIdentifier(driveIdentifier);
            driveInfo.setSerial(diskSerial);
            if (matchingPartition != null) {
                driveInfo.setPartitionUuid(Objects.toString(matchingPartition.getUuid(), "").trim());
                driveInfo.setPartitionIdentification(Objects.toString(matchingPartition.getIdentification(), "").trim());
                driveInfo.setPartitionMountPoint(Objects.toString(matchingPartition.getMountPoint(), "").trim());
            }

            String driveKey = getDriveKey(driveInfo);
            if (latestDriveKeys.contains(driveKey)) {
                continue;
            }
            latestRootDrives.add(diskStore);
            latestDriveKeys.add(driveKey);

            for (SelectedFolder sf : modelMain.getSelectedFolders().getSelectedFolderScanner_obs()) {
                Messages.sprintf("Counter:::: " + counter + " sf: " + sf.getFolder() + " drive serial: " + sf.getDriveSerialNumber() + " driveInfo serial: " + driveInfo.getIdentifier());
                counter++;
                if (sf.getDriveSerialNumber().isEmpty()) {
                    Messages.sprintf("SelectedFolderUtils sf.getDriveSerialNumber() is empty for folder: " + sf.getFolder());
                    continue;
                }
                if (sf.isSelected() && matchesDriveIdentity(sf.getDriveSerialNumber(), driveInfo)) {
                    // E:\, serial=abcde D:\, serial=abcde If drive size == sf.folders harddrive size then it must be match
                    if (isWindows()) {
                        String driveLetter = extractDriveLetter(rootPath.toString());
                        if (!driveLetter.isEmpty() && !sf.getFolder().startsWith(driveLetter)) {
                            Messages.sprintfError("Drive letter mismatch for selected folder: " + sf.getFolder() + " (expected " + driveLetter + " from drive: " + rootPath + "). External drive may have been remapped. Check folder connectivity.");

                        } else {
                            Messages.sprintf("Drive letter matches for selected folder: " + sf.getFolder() + " (drive: " + rootPath + ")");
                        }
                    }

                    //driveInfo.setSelected(true);
                    break;
                }
            }

            if (!hasDriveInfo(driveInfo, modelMain.driveInfos())) {
                Messages.sprintf("New drive detected: " + driveInfo.getDrivePath() + " serial: " + driveInfo.getIdentifier());
                driveInfo.setSelected(false);
                driveInfo.setConnected(true);
                Platform.runLater(() -> modelMain.driveInfos().add(driveInfo));
                changed = true;
            } else {
                for (DriveInfo existing : modelMain.driveInfos()) {
                    Messages.sprintf("Existing Drive: " + existing.getDrivePath() + " serial: " + existing.getIdentifier());
                    if (getDriveKey(existing).equals(driveKey)) {
                        existing.setDrive(rootPath);
                        existing.setDrivePath(rootPath.toString());
                        existing.setConnected(true);
                        break;
                    }
                }
            }
        }

        refreshSelectedFolderConnections(latestRootDrives);
        remapSelectedFoldersToCurrentDriveLetters(roots, hardwareDisks);

        for (DriveInfo driveInfo : modelMain.driveInfos()) {
            boolean isConnectedNow = latestDriveKeys.contains(getDriveKey(driveInfo));
            driveInfo.setConnected(isConnectedNow);
        }

        List<String> currentDriveKeys = toDiskKeys(rootDrives);
        List<String> latestDriveKeysList = toDiskKeys(latestRootDrives);

        List<String> latestRootPaths = roots.stream()
                .map(path -> path.toAbsolutePath().normalize().toString())
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
        if (!currentDriveKeys.equals(latestDriveKeysList) || !rootDrivePaths.equals(latestRootPaths)) {
            changed = true;
        }

        if (changed) {
            Platform.runLater(() -> {
                rootDrives.clear();
                rootDrives.addAll(latestRootDrives);
            });
            rootDrivePaths = latestRootPaths;
            Messages.sprintf("Updated root drives, count: " + rootDrives.size());
        }

        return changed;
    }

    private void remapSelectedFoldersToCurrentDriveLetters(List<Path> roots, List<HWDiskStore> hardwareDisks) {
        if (!isWindows()) {
            return;
        }

        List<SelectedFolderDriveRemap> remaps = new ArrayList<>();
        for (SelectedFolder selectedFolder : modelMain.getSelectedFolders().getSelectedFolderScanner_obs()) {
            if (!selectedFolder.isSelected()) {
                continue;
            }

            Path selectedPath;
            try {
                selectedPath = Paths.get(selectedFolder.getFolder()).toAbsolutePath().normalize();
            } catch (RuntimeException e) {
                Messages.sprintfError("Invalid selected folder path: " + selectedFolder.getFolder() + " error: " + e.getMessage());
                continue;
            }
            Path previousRoot = selectedPath.getRoot();
            if (previousRoot == null || Files.exists(selectedPath)) {
                continue;
            }

            Path relativePath = previousRoot.relativize(selectedPath);
            List<SelectedFolderDriveRemap> matchingRemaps = new ArrayList<>();
            for (Path root : roots) {
                Path currentRoot = root.toAbsolutePath().normalize();
                if (currentRoot.toString().equalsIgnoreCase(previousRoot.toString())) {
                    continue;
                }

                Path candidateFolder = currentRoot.resolve(relativePath).normalize();
                if (!Files.isDirectory(candidateFolder)) {
                    continue;
                }

                String driveIdentity = Objects.toString(
                        OSHI_Utils.getDriveSerialNumber(currentRoot.toString(), hardwareDisks), "").trim();
                if (driveIdentity.isBlank()) {
                    continue;
                }

                Path mdirDatabase = candidateFolder.resolve(Main.conf.getMdir_db_fileName());
                if (!Files.isRegularFile(mdirDatabase)) {
                    continue;
                }

                FolderInfo loadedFolderInfo = FolderInfoDao.loadFolderInfo(candidateFolder);
                if (loadedFolderInfo == null
                        || !samePath(loadedFolderInfo.getFolderPath(), selectedPath)
                        || !hasMatchingFileInfoDriveSerial(loadedFolderInfo, driveIdentity)) {
                    continue;
                }

                matchingRemaps.add(new SelectedFolderDriveRemap(
                        selectedFolder, selectedPath, candidateFolder, driveIdentity, loadedFolderInfo));
            }

            if (matchingRemaps.size() == 1) {
                remaps.add(matchingRemaps.getFirst());
            } else if (matchingRemaps.size() > 1) {
                Messages.sprintfError("Multiple drives contain matching mdir databases for selected folder: " + selectedPath);
            }
        }

        if (remaps.isEmpty()) {
            return;
        }

        Platform.runLater(() -> {
            boolean updated = false;
            for (SelectedFolderDriveRemap remap : remaps) {
                if (!samePath(remap.selectedFolder.getFolder(), remap.previousPath)
                        || !Files.isDirectory(remap.currentPath)) {
                    continue;
                }

                Path previousRoot = remap.previousPath.getRoot();
                Path currentRoot = remap.currentPath.getRoot();
                rebaseFolderInfoPaths(remap.loadedFolderInfo, previousRoot, currentRoot);
                remapLoadedFolderPaths(previousRoot, currentRoot);
                addPreloadedFolderInfoIfMissing(remap.loadedFolderInfo, remap.currentPath);
                remap.selectedFolder.setFolder(remap.currentPath.toString());
                remap.selectedFolder.setDriveSerialNumber(remap.driveIdentity);
                remap.selectedFolder.setConnected(true);
                updated = true;
                Messages.sprintf("Remapped selected folder from " + remap.previousPath + " to " + remap.currentPath
                        + " after matching saved file drive serials");
            }
            if (updated) {
                SelectedFolderInfoDao.saveSelectedFoldersToConfigDb(modelMain);
            }
        });
    }

    static boolean hasMatchingFileInfoDriveSerial(FolderInfo folderInfo, String driveIdentity) {
        if (folderInfo == null || folderInfo.getFileInfoList() == null || driveIdentity == null || driveIdentity.isBlank()) {
            return false;
        }

        List<String> serials = folderInfo.getFileInfoList().stream()
                .filter(Objects::nonNull)
                .map(FileInfo::getOrgPathDriveSerialNumber)
                .map(serial -> Objects.toString(serial, "").trim())
                .filter(serial -> !serial.isBlank())
                .toList();
        return !serials.isEmpty() && serials.stream().allMatch(serial -> serial.equalsIgnoreCase(driveIdentity));
    }

    private void remapLoadedFolderPaths(Path previousRoot, Path currentRoot) {
        if (previousRoot == null || currentRoot == null || modelMain.tables() == null) {
            return;
        }

        for (var table : TableUtils.getAllTables(modelMain.tables())) {
            for (FolderInfo folderInfo : table.getItems()) {
                rebaseFolderInfoPaths(folderInfo, previousRoot, currentRoot);
            }
        }
    }

    private void addPreloadedFolderInfoIfMissing(FolderInfo folderInfo, Path currentPath) {
        if (folderInfo.getTableType() == null || folderInfo.getTableType().isBlank()) {
            return;
        }
        TableView<FolderInfo> table = modelMain.tables().getTableByType(folderInfo.getTableType());
        if (table == null) {
            return;
        }
        boolean alreadyLoaded = TableUtils.getAllTables(modelMain.tables()).stream()
                .flatMap(existingTable -> existingTable.getItems().stream())
                .anyMatch(existing -> samePath(existing.getFolderPath(), currentPath));
        if (!alreadyLoaded) {
            table.getItems().add(folderInfo);
        }
    }

    private static void rebaseFolderInfoPaths(FolderInfo folderInfo, Path previousRoot, Path currentRoot) {
        folderInfo.setFolderPath(rebaseDrivePath(folderInfo.getFolderPath(), previousRoot, currentRoot));
        folderInfo.setSelectedFolderParentPath(
                rebaseDrivePath(folderInfo.getSelectedFolderParentPath(), previousRoot, currentRoot));
        if (folderInfo.getFileInfoList() == null) {
            return;
        }
        for (FileInfo fileInfo : folderInfo.getFileInfoList()) {
            if (fileInfo != null) {
                fileInfo.setOrgPath(rebaseDrivePath(fileInfo.getOrgPath(), previousRoot, currentRoot));
            }
        }
    }

    private static String rebaseDrivePath(String value, Path previousRoot, Path currentRoot) {
        if (value == null || value.isBlank()) {
            return value;
        }
        try {
            Path path = Paths.get(value).toAbsolutePath().normalize();
            if (path.getRoot() == null || !path.getRoot().toString().equalsIgnoreCase(previousRoot.toString())) {
                return value;
            }
            return currentRoot.resolve(previousRoot.relativize(path)).normalize().toString();
        } catch (RuntimeException e) {
            Messages.sprintfError("Unable to remap path: " + value + " error: " + e.getMessage());
            return value;
        }
    }

    private static boolean samePath(String first, Path second) {
        if (first == null || second == null) {
            return false;
        }
        try {
            return Paths.get(first).toAbsolutePath().normalize().toString()
                    .equalsIgnoreCase(second.toAbsolutePath().normalize().toString());
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static final class SelectedFolderDriveRemap {
        private final SelectedFolder selectedFolder;
        private final Path previousPath;
        private final Path currentPath;
        private final String driveIdentity;
        private final FolderInfo loadedFolderInfo;

        private SelectedFolderDriveRemap(SelectedFolder selectedFolder, Path previousPath, Path currentPath,
                                         String driveIdentity, FolderInfo loadedFolderInfo) {
            this.selectedFolder = selectedFolder;
            this.previousPath = previousPath;
            this.currentPath = currentPath;
            this.driveIdentity = driveIdentity;
            this.loadedFolderInfo = loadedFolderInfo;
        }
    }

    private List<String> toDiskKeys(List<HWDiskStore> drives) {
        List<String> keys = new ArrayList<>(drives.size());
        for (HWDiskStore diskStore : drives) {
            keys.add(getDiskKey(diskStore));
        }
        keys.sort(String::compareToIgnoreCase);
        return keys;
    }


    private List<String> getDiskMountPoints(HWDiskStore diskStore) {
        List<String> mountPoints = new ArrayList<>();
        for (oshi.hardware.HWPartition partition : diskStore.getPartitions()) {
            String mountPoint = Objects.toString(partition.getMountPoint(), "").trim();
            if (!mountPoint.isBlank()) {
                mountPoints.add(mountPoint);
            }
        }
        return mountPoints;
    }

    private String resolveDriveIdentifier(HWDiskStore diskStore, HWPartition partition) {
        if (partition != null) {
            String partitionUuid = Objects.toString(partition.getUuid(), "").trim();
            if (!partitionUuid.isBlank()) {
                return partitionUuid;
            }

            String partitionIdentification = Objects.toString(partition.getIdentification(), "").trim();
            if (!partitionIdentification.isBlank()) {
                return partitionIdentification;
            }
        }
        return resolveDiskIdentity(diskStore);
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

    private boolean isWindows() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.contains("win");
    }

    private String extractDriveLetter(String drivePath) {
        if (drivePath != null && drivePath.length() >= 2) {
            return drivePath.substring(0, 2);
        }
        return "";
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
