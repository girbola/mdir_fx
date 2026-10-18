package com.girbola.drive;

import common.utils.OSHI_Utils;
import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HWPartition;

final class DriveScannerUtils {

    private DriveScannerUtils() {
    }

    static File[] getListOfRoots() {
        Set<Path> roots = new HashSet<>();

        for (Path root : FileSystems.getDefault().getRootDirectories()) {
            roots.add(root.toAbsolutePath().normalize());
        }

        addMountedChildren(roots, Paths.get("/Volumes"));
        addMountedChildren(roots, Paths.get("/media"));
        addMountedChildren(roots, Paths.get("/mnt"));

        return roots.stream()
                .filter(Files::exists)
                .filter(Files::isDirectory)
                .map(Path::toFile)
                .sorted((first, second) ->
                        first.getAbsolutePath().compareToIgnoreCase(second.getAbsolutePath()))
                .toArray(File[]::new);
    }

    static void addMountedChildren(Set<Path> roots, Path mountParent) {
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

    static List<String> toDiskKeys(List<HWDiskStore> drives) {
        List<String> keys = new ArrayList<>(drives.size());
        for (HWDiskStore diskStore : drives) {
            keys.add(getDiskKey(diskStore));
        }
        keys.sort(String::compareToIgnoreCase);
        return keys;
    }

    static String getDriveKey(DriveInfo driveInfo) {
        return resolveDriveIdentity(driveInfo) + "|" + driveInfo.getDriveTotalSize();
    }

    static String getDiskKey(HWDiskStore diskStore) {
        return resolveDiskIdentity(diskStore) + "|" + diskStore.getSize();
    }

    static String resolveDriveIdentity(DriveInfo driveInfo) {
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

    static String resolveDiskIdentity(HWDiskStore diskStore) {
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

    static List<String> getDiskMountPoints(HWDiskStore diskStore) {
        List<String> mountPoints = new ArrayList<>();
        for (HWPartition partition : diskStore.getPartitions()) {
            String mountPoint = Objects.toString(partition.getMountPoint(), "").trim();
            if (!mountPoint.isBlank()) {
                mountPoints.add(mountPoint);
            }
        }
        return mountPoints;
    }

    static String resolveDriveIdentifier(HWDiskStore diskStore, HWPartition partition) {
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

}
