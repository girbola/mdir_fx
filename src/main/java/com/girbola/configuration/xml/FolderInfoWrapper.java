/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.configuration.xml;

import com.girbola.controllers.main.tables.FolderInfo;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Marko Lokka
 */
/**
 *
 * @author Marko Lokka
 */
@XmlRootElement(name = "FolderInfo")
//@XmlAccessorType(XmlAccessType.NONE)
public class FolderInfoWrapper {

    private FolderInfo folderInfo;

    /**
     * XML getFolderInfo
     * @return
     */
    @XmlElement(name = "folderInfo")
    public FolderInfo getFolderInfo() {
        return this.folderInfo;
    }

    /**
     * XML setTableValues
     * @param aFolderInfo
     */
    public void setFolderInfo(FolderInfo aFolderInfo) {
        this.folderInfo = aFolderInfo;
    }
}
