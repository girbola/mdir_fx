/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.main;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.girbola.Main;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;

import javafx.concurrent.Task;

import static com.girbola.Main.simpleDates;
import static com.girbola.controllers.main.tables.TableUtils.calculateDateDifferenceRatio;

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
    	TableUtils.updateFolderInfos_FileInfo(folderInfo);
        return null;
    }

}
