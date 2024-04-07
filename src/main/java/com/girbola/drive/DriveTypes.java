/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.drive;

/**
 *
 * @author Marko Lokka
 */
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
