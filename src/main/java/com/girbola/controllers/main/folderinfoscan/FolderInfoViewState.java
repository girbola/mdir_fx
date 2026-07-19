package com.girbola.controllers.main.folderinfoscan;

import com.girbola.fileinfo.FileInfo;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FolderInfoViewState {

    private FolderInfoScannerActionStateEnum state;
    private List<FileInfo> fileInfos;

    public FolderInfoViewState(List<FileInfo> fileInfos, FolderInfoScannerActionStateEnum state) {
        this.fileInfos = fileInfos;
        this.state = state;
    }
}
