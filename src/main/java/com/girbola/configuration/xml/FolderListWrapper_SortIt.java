/*
 * Marko Lokka project. MDir Tool
 * 2013 (c) mDesign
 */
package com.girbola.configuration.xml;

import com.girbola.controllers.main.tables.FolderInfo;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 *
 * @author Marko Lokka
 */
@XmlRootElement(name = "SortIt")
//@XmlAccessorType(XmlAccessType.NONE)
public class FolderListWrapper_SortIt {

    
    private List<FolderInfo> sortitFolders;

    @XmlElement(name = "sortit")
    public List<FolderInfo> getSortitFolders() {
        return this.sortitFolders;
    }

    public void setSortitFolders(List<FolderInfo> sortitFolders) {
        this.sortitFolders = sortitFolders;
    }
}
