
package com.girbola.drive;


public enum DriveTypes {
    CD_DRIVE("CD Drive"),
    LOCAL_DISK("Local Disk"),
    NETWORK_DRIVE("Network Drive"),
    REMOVABLE_DISK("Removable Disk");

    private String type;

    public String getType() {
        return type;
    }

    DriveTypes(String type) {
        this.type = type;
    }

}
