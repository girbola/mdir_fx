package com.girbola.drive;

import com.girbola.messages.Messages;
import common.utils.OSHI_Utils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BooleanSupplier;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HWPartition;

final class DriveInventoryScanner {

    Result scanRoots(List<Path> roots, List<HWDiskStore> hardwareDisks,
                     List<DriveInfo> registeredDriveInfos, BooleanSupplier isCancelled) {
        List<HWDiskStore> discoveredDrives = new ArrayList<>();
        Set<String> discoveredDriveKeys = new HashSet<>();
        boolean driveInfosChanged = false;

        for (Path rootPath : roots) {
            Messages.sprintf("##updateRootDrives Updating root drives from path: " + rootPath);
            if (isCancelled.getAsBoolean()) {
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

            HWPartition partition = OSHI_Utils.findPartitionForRoot(rootPath, diskStore);
            DriveInfo driveInfo = createDriveInfo(rootPath, diskStore, partition);
            driveInfosChanged |= registerDriveInfo(driveInfo, registeredDriveInfos);

            String driveKey = DriveScannerUtils.getDriveKey(driveInfo);
            if (!discoveredDriveKeys.add(driveKey)) {
                continue;
            }
            discoveredDrives.add(diskStore);
        }

        return new Result(discoveredDrives, discoveredDriveKeys, driveInfosChanged, normalizeRootPaths(roots));
    }

    static DriveInfo createDriveInfo(Path rootPath, HWDiskStore diskStore, HWPartition partition) {
        String driveIdentifier = DriveScannerUtils.resolveDriveIdentifier(diskStore, partition);
        String diskSerial = Objects.toString(diskStore.getSerial(), "").trim();
        Messages.sprintf("ROOT DRIVE: " + rootPath + " identifier: " + driveIdentifier + " drive: " + rootPath);

        DriveInfo driveInfo = new DriveInfo(
                rootPath.toString(), diskStore.getSize(), Files.exists(rootPath), false, driveIdentifier, diskSerial);
        if (partition != null) {
            driveInfo.setPartitionUuid(Objects.toString(partition.getUuid(), "").trim());
            driveInfo.setPartitionIdentification(Objects.toString(partition.getIdentification(), "").trim());
            driveInfo.setPartitionMountPoint(Objects.toString(partition.getMountPoint(), "").trim());
        }
        return driveInfo;
    }

    static boolean registerDriveInfo(DriveInfo discoveredDrive, List<DriveInfo> registeredDriveInfos) {
        String discoveredKey = DriveScannerUtils.getDriveKey(discoveredDrive);
        for (DriveInfo registeredDrive : registeredDriveInfos) {
            if (DriveScannerUtils.getDriveKey(registeredDrive).equals(discoveredKey)) {
                registeredDrive.setConnected(true);
                Messages.sprintf("Existing Drive: " + registeredDrive.getDrivePath()
                        + " serial: " + registeredDrive.getIdentifier());
                return false;
            }
        }

        discoveredDrive.setSelected(false);
        discoveredDrive.setConnected(true);
        registeredDriveInfos.add(discoveredDrive);
        Messages.sprintf("New drive detected: " + discoveredDrive.getDrivePath()
                + " serial: " + discoveredDrive.getIdentifier());
        return true;
    }

    static void updateConnectivity(List<DriveInfo> driveInfos, Set<String> connectedDriveKeys) {
        for (DriveInfo driveInfo : driveInfos) {
            driveInfo.setConnected(connectedDriveKeys.contains(DriveScannerUtils.getDriveKey(driveInfo)));
        }
    }

    static List<String> normalizeRootPaths(List<Path> roots) {
        return roots.stream()
                .map(path -> path.toAbsolutePath().normalize().toString())
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    static boolean hasChanged(Result result, List<String> currentDriveKeys, List<String> previousRootPaths) {
        return result.driveInfosChanged()
                || !currentDriveKeys.equals(DriveScannerUtils.toDiskKeys(result.rootDrives()))
                || !previousRootPaths.equals(result.rootPaths());
    }

    record Result(List<HWDiskStore> rootDrives, Set<String> driveKeys, boolean driveInfosChanged,
                  List<String> rootPaths) {
        Result {
            rootDrives = List.copyOf(rootDrives);
            driveKeys = Set.copyOf(driveKeys);
            rootPaths = List.copyOf(rootPaths);
        }
    }
}
