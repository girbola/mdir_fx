/*
 * Marko Lokka project. MDir Tool
 * 2013 (c) mDesign
 */
package com.girbola.configuration.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.girbola.controllers.main.tables.FolderInfo;

/**
 *
 * @author Marko Lokka
 */
@XmlRootElement(name = "AsItIs")

public class FolderListWrapper_AsItIs {

    private List<FolderInfo> asitisFolders = new ArrayList<>();

    @XmlElement(name = "asitis")
    public List<FolderInfo> getAsitisFolders() {
        return this.asitisFolders;
    }

    public void setAsitisFolders(List<FolderInfo> asitisFolders) {
        this.asitisFolders = asitisFolders;
    }

}
