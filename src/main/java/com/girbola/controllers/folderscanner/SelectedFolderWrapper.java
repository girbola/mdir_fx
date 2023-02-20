package com.girbola.controllers.folderscanner;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "SelectedFolder")
public class SelectedFolderWrapper {
	private List<SelectedFolder> selectedFolder_list = new ArrayList<>();

    @XmlElement(name = "selectedfolder")
    public List<SelectedFolder> getSelectedFolder_list() {
        return this.selectedFolder_list;
    }

    public void setSelectedFolder_list(List<SelectedFolder> selectedFolder_list) {
        this.selectedFolder_list = selectedFolder_list;
    }
    
}
