package com.girbola.configuration.xml;

import com.girbola.controllers.main.tables.FolderInfo;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Marko Lokka
 */
@XmlRootElement(name = "WorkDir")

public class FolderListWrapper_WorkDir {

    private List<FolderInfo> workDir_list = new ArrayList<>();

    @XmlElement(name = "workdir_list")
    public List<FolderInfo> getWorkDir_list() {
        return this.workDir_list;
    }

    public void setWorkDir_list(List<FolderInfo> workDir_list) {
        this.workDir_list = workDir_list;
    }

}
