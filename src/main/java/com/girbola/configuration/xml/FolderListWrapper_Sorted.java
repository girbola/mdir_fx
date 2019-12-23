/*
 * Marko Lokka project. MDir Tool
 * 2013 (c) mDesign
 */
package com.girbola.configuration.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.girbola.controllers.main.tables.FolderInfo;

/**
 *
 * @author Marko Lokka
 */
@XmlRootElement(name = "Sorted")
public class FolderListWrapper_Sorted {

    private List<FolderInfo> sortedFolders;

    @XmlElement(name = "sortedFolders")
    public List<FolderInfo> getSortedFolders() {
        return this.sortedFolders;
    }

    public void setSortedFolders(List<FolderInfo> value) {
        this.sortedFolders = value;
    }

}
