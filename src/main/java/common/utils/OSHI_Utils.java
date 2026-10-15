package common.utils;

import com.girbola.messages.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.nio.file.*;
import java.util.List;
import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HWPartition;
import oshi.hardware.HardwareAbstractionLayer;

public class OSHI_Utils {

    public static final class DriveIdentity {
        private final String identifier;
        private final String serial;
        private final String partitionUuid;
        private final String partitionIdentification;
        private final String partitionMountPoint;
        private final long diskSize;

        public DriveIdentity(String identifier, String serial, String partitionUuid,
                             String partitionIdentification, String partitionMountPoint) {
            this(identifier, serial, partitionUuid, partitionIdentification, partitionMountPoint, 0L);
        }

        public DriveIdentity(String identifier, String serial, String partitionUuid,
                             String partitionIdentification, String partitionMountPoint, long diskSize) {
            this.identifier = sanitize(identifier);
            this.serial = sanitize(serial);
            this.partitionUuid = sanitize(partitionUuid);
            this.partitionIdentification = sanitize(partitionIdentification);
            this.partitionMountPoint = sanitize(partitionMountPoint);
            this.diskSize = diskSize;
        }

        public String identifier() {
            return identifier;
        }

        public String serial() {
            return serial;
        }

        public String partitionUuid() {
            return partitionUuid;
        }

        public String partitionIdentification() {
            return partitionIdentification;
        }

        public String partitionMountPoint() {
            return partitionMountPoint;
        }

        public long diskSize() {
            return diskSize;
        }
    }

    private static final long DISK_STORE_CACHE_TTL_MILLIS = 5_000L;
    private static volatile List<HWDiskStore> cachedDiskStores = new ArrayList<>();
    private static volatile long cachedAtMillis = 0L;

    private OSHI_Utils() {
    }

    public static String getDriveSerialNumber(String path) {
        DriveIdentity identity = resolveDriveIdentity(path, getHWDiskStore());
        if (!identity.identifier().isBlank()) {
            return identity.identifier();
        }
        return buildFallbackDriveIdentifier(path);
    }

    public static String getDriveSerialNumber(String path, List<HWDiskStore> hardwareDisks) {
        DriveIdentity identity = resolveDriveIdentity(path, hardwareDisks);
        if (!identity.identifier().isBlank()) {
            return identity.identifier();
        }
        return buildFallbackDriveIdentifier(path);
    }

    public static List<HWDiskStore> getHWDiskStore() {
        long now = System.currentTimeMillis();
        if (now - cachedAtMillis <= DISK_STORE_CACHE_TTL_MILLIS && !cachedDiskStores.isEmpty()) {
            return new ArrayList<>(cachedDiskStores);
        }

        synchronized (OSHI_Utils.class) {
            long refreshedNow = System.currentTimeMillis();
            if (refreshedNow - cachedAtMillis <= DISK_STORE_CACHE_TTL_MILLIS && !cachedDiskStores.isEmpty()) {
                return new ArrayList<>(cachedDiskStores);
            }

            List<HWDiskStore> latestDiskStores = loadDiskStores();
            if (!latestDiskStores.isEmpty()) {
                cachedDiskStores = latestDiskStores;
                cachedAtMillis = refreshedNow;
            }
            return new ArrayList<>(cachedDiskStores);
        }
    }

    private static List<HWDiskStore> loadDiskStores() {
        try {
            SystemInfo si = new SystemInfo();
            HardwareAbstractionLayer hal = si.getHardware();
            return hal.getDiskStores();
        } catch (Exception e) {
            Messages.sprintfError("Unable to resolve drive HWDiskStore: " + e.getMessage());
        }
        return new ArrayList<>();
    }


    private static String getRawDriveSerialNumber(String path, List<HWDiskStore> hardwareDisks) {
        return resolveDriveIdentity(path, hardwareDisks).identifier();
    }

    public static DriveIdentity resolveDriveIdentity(String path) {
        return resolveDriveIdentity(path, getHWDiskStore());
    }

    public static DriveIdentity resolveDriveIdentity(String path, List<HWDiskStore> hardwareDisks) {
        try {
            Path root = getPathRoot(path);
            if (root == null) {
                return emptyIdentity();
            }

            HWDiskStore diskStore = findDiskStoreForRoot(root, hardwareDisks);
            if (diskStore == null) {
                return emptyIdentity();
            }

            HWPartition partition = findPartitionForRoot(root, diskStore);
            String serial = sanitize(diskStore.getSerial());
            String partitionUuid = partition == null ? "" : sanitize(partition.getUuid());
            String partitionIdentification = partition == null ? "" : sanitize(partition.getIdentification());
            String partitionMountPoint = partition == null ? "" : sanitize(partition.getMountPoint());

            String identifier = chooseIdentifier(partitionUuid, partitionIdentification, serial,
                    sanitize(diskStore.getName()), sanitize(diskStore.getModel()));

            return new DriveIdentity(identifier, serial, partitionUuid, partitionIdentification,
                    partitionMountPoint, diskStore.getSize());
        } catch (Exception e) {
            Messages.sprintfError("Unable to resolve drive serial for path: " + path + " error: " + e.getMessage());
        }
        return emptyIdentity();
    }

    private static Path getPathRoot(String path) {
        return Paths.get(new File(path).getAbsolutePath()).getRoot();
    }

    private static String getSerialForRoot(HWDiskStore disk, String rootPath) {
        for (HWPartition part : disk.getPartitions()) {
            if (mountPointMatchesRoot(part, rootPath)) {
                return resolveSerial(disk, part);
            }
        }
        return "";
    }

    private static boolean mountPointMatchesRoot(HWPartition part, String rootPath) {
        String mountPoint = part.getMountPoint();
        return mountPoint != null && mountPoint.contains(rootPath);
    }

    private static String resolveSerial(HWDiskStore disk, HWPartition part) {
        String serial = part.getUuid();
        if (serial == null || serial.isBlank()) {
            serial = part.getIdentification();
        }
        if (serial == null || serial.isBlank()) {
            serial = disk.getSerial();
        }
        return serial == null ? "" : serial.trim();
    }

    private static String buildFallbackDriveIdentifier(String path) {
        try {
            File file = new File(path);
            Path root = Paths.get(file.getAbsolutePath()).getRoot();
            File rootFile = root != null ? root.toFile() : file;

            String[] entries = rootFile.list();
            String rootStructureHash = "no-entries";
            int entryCount = 0;

            if (entries != null && entries.length > 0) {
                Arrays.sort(entries, String.CASE_INSENSITIVE_ORDER);
                entryCount = entries.length;
                rootStructureHash = Integer.toHexString(String.join("|", entries).hashCode());
            }

            return "fallback:" + rootFile.getTotalSpace() + ":" + rootFile.getUsableSpace() + ":"
                    + entryCount + ":" + rootStructureHash;
        } catch (Exception e) {
            Messages.sprintfError("Unable to build fallback drive identifier for path: " + path + " error: " + e.getMessage());
            return "";
        }
    }

    public static HWDiskStore getDrive(String path) {
        for (HWDiskStore disk : getHWDiskStore()) {
            for (HWPartition part : disk.getPartitions()) {
                Path root = Paths.get(path).getRoot();
                if (part.getMountPoint().contains(root.toString())) {
                    Messages.sprintf("Is root drive: " + disk);
                    return disk;
                }
            }
        }
        return null;
    }

    public static HWDiskStore findDiskStoreForRoot(Path rootPath, List<HWDiskStore> hardwareDisks) {
        if (rootPath == null || hardwareDisks == null || hardwareDisks.isEmpty()) {
            return null;
        }

        for (HWDiskStore diskStore : hardwareDisks) {
            Messages.sprintf("Searching for disk store: " + diskStore);
            for (HWPartition partition : diskStore.getPartitions()) {

                if (mountPointMatchesRoot(partition.getMountPoint(), rootPath)) {
                    Messages.sprintf("mountPointMatchesRoot FOUND: " + partition.getMountPoint() + " against root path: " + rootPath);
                    return diskStore;
                }
            }
        }
        return null;
    }

//    public static HWDiskStore findDiskStoreForRoot(Path rootPath) {
//        return findDiskStoreForRoot(rootPath, getHWDiskStore());
//    }

    public static HWPartition findPartitionForRoot(Path rootPath, HWDiskStore diskStore) {
        if (rootPath == null || diskStore == null) {
            return null;
        }
        for (HWPartition partition : diskStore.getPartitions()) {
            if (mountPointMatchesRoot(partition.getMountPoint(), rootPath)) {
                return partition;
            }
        }
        return null;
    }

    private static DriveIdentity emptyIdentity() {
        return new DriveIdentity("", "", "", "", "", 0L);
    }

    private static String chooseIdentifier(String partitionUuid, String partitionIdentification,
                                           String serial, String diskName, String diskModel) {
        if (!partitionUuid.isBlank()) {
            return partitionUuid;
        }
        if (!partitionIdentification.isBlank()) {
            return partitionIdentification;
        }
        if (!serial.isBlank()) {
            return serial;
        }
        if (!diskName.isBlank()) {
            return diskName;
        }
        return diskModel;
    }

    private static String sanitize(String value) {
        return value == null ? "" : value.trim();
    }

    private static boolean mountPointMatchesRoot(String mountPoint, Path rootPath) {
        if (mountPoint == null || mountPoint.isBlank() || rootPath == null) {
            return false;
        }

        try {
            Path mountPath = Paths.get(mountPoint).toAbsolutePath().normalize();
            Path mountRoot = mountPath.getRoot() == null ? mountPath : mountPath.getRoot().normalize();
            Path normalizedRoot = rootPath.toAbsolutePath().normalize();
            return mountPath.equals(normalizedRoot) || mountRoot.equals(normalizedRoot);
        } catch (Exception e) {
            Messages.sprintfError("Unable to parse mount point: " + mountPoint + " error: " + e.getMessage());
            return false;
        }
    }
}
