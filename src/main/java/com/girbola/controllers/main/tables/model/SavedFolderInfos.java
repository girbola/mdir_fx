package com.girbola.controllers.main.tables.model;

import com.girbola.drive.DriveInfo;
import common.utils.OSHI_Utils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SavedFolderInfos {
    private String folderPath;
    private String tableType;
    private String justFolderName;
    private boolean connected;
    private String driveIdentifier;
    private String driveSerial;
    private long driveSize;
    private String partitionUuid;
    private String partitionIdentification;

    public SavedFolderInfos(String folderPath, String tableType, String justFolderName, boolean connected) {
        this(folderPath, tableType, justFolderName, connected, null, null, 0L, null, null);
    }

    public SavedFolderInfos(String folderPath, String tableType, String justFolderName, boolean connected,
                            OSHI_Utils.DriveIdentity driveIdentity) {
        this(folderPath, tableType, justFolderName, connected,
                driveIdentity == null ? null : driveIdentity.identifier(),
                driveIdentity == null ? null : driveIdentity.serial(),
                driveIdentity == null ? 0L : driveIdentity.diskSize(),
                driveIdentity == null ? null : driveIdentity.partitionUuid(),
                driveIdentity == null ? null : driveIdentity.partitionIdentification());
    }

    public static SavedFolderInfos capture(String folderPath, String tableType, String justFolderName,
                                           boolean connected) {
        return new SavedFolderInfos(folderPath, tableType, justFolderName, connected,
                OSHI_Utils.resolveDriveIdentity(folderPath));
    }

    public SavedFolderInfos(String folderPath, String tableType, String justFolderName, boolean connected,
                            String driveIdentifier, String driveSerial, long driveSize,
                            String partitionUuid, String partitionIdentification) {
        this.folderPath = folderPath;
        this.tableType = tableType;
        this.justFolderName = justFolderName;
        this.connected = connected;
        this.driveIdentifier = driveIdentifier;
        this.driveSerial = driveSerial;
        this.driveSize = driveSize;
        this.partitionUuid = partitionUuid;
        this.partitionIdentification = partitionIdentification;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }

    public String getTableType() {
        return tableType;
    }

    public void setTableType(String tableType) {
        this.tableType = tableType;
    }

    public String getJustFolderName() {
        return justFolderName;
    }

    public void setJustFolderName(String justFolderName) {
        this.justFolderName = justFolderName;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public String getDriveIdentifier() {
        return driveIdentifier;
    }

    public String getDriveSerial() {
        return driveSerial;
    }

    public long getDriveSize() {
        return driveSize;
    }

    public String getPartitionUuid() {
        return partitionUuid;
    }

    public String getPartitionIdentification() {
        return partitionIdentification;
    }

    public boolean matchesDrive(DriveInfo driveInfo) {
        if (driveInfo == null
                || (driveSize > 0 && driveInfo.getDriveTotalSize() > 0
                && driveInfo.getDriveTotalSize() != driveSize)) {
            return false;
        }

        String[][] identifiers = {
                {partitionUuid, driveInfo.getPartitionUuid()},
                {partitionIdentification, driveInfo.getPartitionIdentification()},
                {driveSerial, driveInfo.getSerial()}
        };
        for (String[] identifier : identifiers) {
            String savedValue = normalize(identifier[0]);
            String currentValue = normalize(identifier[1]);
            if (!savedValue.isBlank() && !currentValue.isBlank()) {
                return savedValue.equalsIgnoreCase(currentValue);
            }
        }
        return false;
    }

    public boolean hasStableDriveIdentity() {
        return !normalize(partitionUuid).isBlank()
                || !normalize(partitionIdentification).isBlank()
                || !normalize(driveSerial).isBlank();
    }

    public Optional<DriveInfo> findMatchingDrive(Iterable<DriveInfo> drives) {
        List<DriveInfo> matches = new ArrayList<>();
        for (DriveInfo drive : drives) {
            if (matchesDrive(drive)) {
                matches.add(drive);
            }
        }
        return matches.size() == 1 ? Optional.of(matches.getFirst()) : Optional.empty();
    }

    public Optional<Path> resolveCurrentFolderPath(Iterable<DriveInfo> drives) {
        Path savedPath = toPath(folderPath);
        if (savedPath == null) {
            return Optional.empty();
        }
        Path previousRoot = savedPath.getRoot();
        if (previousRoot == null) {
            return Optional.empty();
        }

        List<DriveInfo> driveList = new ArrayList<>();
        drives.forEach(driveList::add);
        Optional<DriveInfo> originalDrive = driveList.stream()
                .filter(drive -> sameRoot(drive.getDrivePath(), previousRoot))
                .findFirst();
        boolean savedFolderExists = Files.isDirectory(savedPath);
        if (savedFolderExists
                && (!hasStableDriveIdentity() || originalDrive.map(this::matchesDrive).orElse(true))) {
            return Optional.of(savedPath);
        }

        Optional<Path> remappedPath = findMatchingDrive(driveList)
                .map(DriveInfo::getDrivePath)
                .map(SavedFolderInfos::toPath)
                .filter(Objects::nonNull)
                .map(Path::getRoot)
                .filter(Objects::nonNull)
                .filter(currentRoot -> !sameRoot(currentRoot.toString(), previousRoot))
                .map(currentRoot -> currentRoot.resolve(previousRoot.relativize(savedPath)).normalize())
                .filter(Files::isDirectory);
        if (remappedPath.isPresent()) {
            return remappedPath;
        }
        if (savedFolderExists && originalDrive.isEmpty()) {
            return Optional.of(savedPath);
        }
        return Optional.empty();
    }

    private static Path toPath(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Paths.get(value).toAbsolutePath().normalize();
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static String normalize(String value) {
        return Objects.toString(value, "").trim();
    }

    private static boolean sameRoot(String drivePath, Path root) {
        Path drive = toPath(drivePath);
        return drive != null && drive.getRoot() != null
                && drive.getRoot().toString().equalsIgnoreCase(root.toString());
    }
}
