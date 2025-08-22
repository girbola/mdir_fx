
package com.girbola.controllers.main;

import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.controllers.main.tables.FolderInfoUtils;
import com.girbola.messages.Messages;
import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.List;


public class UpdateFolderInfoContent extends Task<Integer> {

    private FolderInfo folderInfo;

    public UpdateFolderInfoContent(FolderInfo folderInfo) {
        this.folderInfo = folderInfo;
    }
    List<Long> dateCounter_list = new ArrayList<>();

    @Override
    protected Integer call() throws Exception {
    	Messages.sprintf("Running now updatefolerinfocontent");
    	FolderInfoUtils.calculateFolderInfoStatus(folderInfo);
        return null;
    }

}
