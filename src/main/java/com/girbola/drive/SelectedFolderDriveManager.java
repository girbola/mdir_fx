package com.girbola.drive;

import com.girbola.Main;
import com.girbola.controllers.folderscanner.SelectedFolder;
import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.persistence.folderinfo.FolderInfoDao;
import com.girbola.persistence.selectedfolderinfo.SelectedFolderInfoDao;
import common.utils.OSHI_Utils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javafx.application.Platform;
import javafx.scene.control.TableView;
import oshi.hardware.HWDiskStore;

final class SelectedFolderDriveManager {

    private final ModelMain modelMain;

    SelectedFolderDriveManager(ModelMain modelMain) {
        this.modelMain = modelMain;
    }

    void updateConnectivity(List<HWDiskStore> currentDrives) {
        for (SelectedFolder selectedFolder : modelMain.getSelectedFolders().getSelectedFolderScanner_obs()) {
            if (!selectedFolder.isSelected()) {
                continue;
            }

            HWDiskStore matchingDrive = findMatchingDriveForSelectedFolder(selectedFolder, currentDrives);
            boolean connected = matchingDrive != null;
            String resolvedIdentity = connected
                    ? DriveScannerUtils.resolveDiskIdentity(matchingDrive)
                    : selectedFolder.getDriveSerialNumber();

            Platform.runLater(() -> {
                selectedFolder.setConnected(connected);
                if (connected && !Objects.equals(selectedFolder.getDriveSerialNumber(), resolvedIdentity)
                        && !resolvedIdentity.isBlank()) {
                    selectedFolder.setDriveSerialNumber(resolvedIdentity);
                }
            });
        }
    }

    void refreshConnections(List<HWDiskStore> currentDrives) {
        List<SelectedFolderConnectionState> updates = new ArrayList<>();

        for (SelectedFolder selectedFolder : modelMain.getSelectedFolders().getSelectedFolderScanner_obs()) {
            if (!selectedFolder.isSelected()) {
                continue;
            }

            HWDiskStore matchingDrive = findMatchingDriveForSelectedFolder(selectedFolder, currentDrives);
            boolean connected = matchingDrive != null;
            String resolvedIdentity = connected
                    ? DriveScannerUtils.resolveDiskIdentity(matchingDrive)
                    : selectedFolder.getDriveSerialNumber();
            updates.add(new SelectedFolderConnectionState(selectedFolder, connected, resolvedIdentity));
        }

        Platform.runLater(() -> applySelectedFolderConnections(updates));
    }

    void remapSelectedFoldersToCurrentDriveLetters(List<Path> roots, List<HWDiskStore> hardwareDisks) {
        if (!DriveInfoUtils.isWindows()) {
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
                Messages.sprintfError("Invalid selected folder path: " + selectedFolder.getFolder()
                        + " error: " + e.getMessage());
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
                Messages.sprintfError("Multiple drives contain matching mdir databases for selected folder: "
                        + selectedPath);
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
        if (folderInfo == null || folderInfo.getFileInfoList() == null
                || driveIdentity == null || driveIdentity.isBlank()) {
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

    private void applySelectedFolderConnections(List<SelectedFolderConnectionState> updates) {
        for (SelectedFolderConnectionState update : updates) {
            update.selectedFolder.setConnected(update.connected);
            if (update.connected && !Objects.equals(update.selectedFolder.getDriveSerialNumber(), update.resolvedIdentity)
                    && !update.resolvedIdentity.isBlank()) {
                update.selectedFolder.setDriveSerialNumber(update.resolvedIdentity);
            }
        }
    }

    private HWDiskStore findMatchingDriveForSelectedFolder(
            SelectedFolder selectedFolder, List<HWDiskStore> currentDrives) {
        String selectedIdentity = Objects.toString(selectedFolder.getDriveSerialNumber(), "").trim();

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
            if (Objects.equals(selectedFolder.getDriveSerialNumber(), DriveScannerUtils.resolveDiskIdentity(diskStore))) {
                return diskStore;
            }

            if (matchesDriveByFolderStructure(selectedFolder.getFolder(), diskStore)) {
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
            for (String mountPoint : DriveScannerUtils.getDiskMountPoints(diskStore)) {
                Path candidatePath = Paths.get(mountPoint).resolve(relativeToRoot).normalize();
                if (Files.exists(candidatePath)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            Messages.sprintfError("Unable to compare folder structure for path: " + selectedFolderPath
                    + " disk: " + diskStore + " error: " + e.getMessage());
            return false;
        }
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
}
