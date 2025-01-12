/*
 @(#)Copyright:  Copyright (c) 2012-2025 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.main;

import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.controllers.main.tables.FolderInfoUtils;
import com.girbola.messages.Messages;
import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Marko Lokka
 */
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
