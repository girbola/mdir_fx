/*
 @(#)Copyright:  Copyright (c) 2012-2019 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.configuration.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.girbola.controllers.main.tables.FolderInfo;

/**
 *
 * @author Marko Lokka
 */
/**
 *
 * @author Marko Lokka
 */
@XmlRootElement(name = "Sorted")
//@XmlAccessorType(XmlAccessType.NONE)
public class FolderInfoWrapper_Sorted {

    private FolderInfo folderInfoSorted;

    @XmlElement(name = "sorted")
    public FolderInfo getFolderInfoSorted() {
        return this.folderInfoSorted;
    }

    public void setFolderInfoSorted(FolderInfo aFolderInfoSorted) {
        this.folderInfoSorted = aFolderInfoSorted;
    }
}
