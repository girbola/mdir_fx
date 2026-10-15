package com.girbola.drive;

import java.nio.file.FileStore;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DriveInfo {

    private FileStore fileSystem;
    private List<Path> selectedFolders = new ArrayList<>();
    private Path drive;
    private String drivePath;
    private String identifier;
    private String serial;
    private String partitionUuid;
    private String partitionIdentification;
    private String partitionMountPoint;
    private boolean connected;
    private boolean isWritable;
    private boolean selected;
    private long driveTotalSize;

    public DriveInfo(String drivePath, long driveTotalSize, boolean connected, boolean selected,
                     String identifier) {
        this(drivePath, driveTotalSize, connected, selected, identifier, null);
    }

    public DriveInfo(String drivePath, long driveTotalSize, boolean connected, boolean selected,
                     String identifier, String serial) {
        this.drivePath = drivePath;
        this.driveTotalSize = driveTotalSize;
        this.connected = connected;
        this.selected = selected;
        this.identifier = identifier;
        this.serial = serial;
    }

    @Override
    public String toString() {
        return "DriveInfo{" +
                "fileSystem=" + fileSystem +
                ", selectedFolders=" + selectedFolders +
                ", drive=" + drive +
                ", drivePath='" + drivePath + '\'' +
                ", identifier='" + identifier + '\'' +
                ", serial='" + serial + '\'' +
                ", partitionUuid='" + partitionUuid + '\'' +
                ", partitionIdentification='" + partitionIdentification + '\'' +
                ", partitionMountPoint='" + partitionMountPoint + '\'' +
                ", connected=" + connected +
                ", isWritable=" + isWritable +
                ", selected=" + selected +
                ", driveTotalSize=" + driveTotalSize +
                '}';
    }
}
