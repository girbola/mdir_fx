
package com.girbola.drive;


public enum DriveTypes {
    UNKNOWN("Unknown"),
    NO_ROOT_DIR("No Root Directory"),
    REMOVABLE_DISK("Removable Disk"),
    LOCAL_DISK("Local Disk"),
    NETWORK_DRIVE("Network Drive"),
    CD_DRIVE("CD/DVD Drive"),
    RAM_DISK("RAM Disk");

    private final String type;

    DriveTypes(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
