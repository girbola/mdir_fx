/*
 @(#)Copyright:  Copyright (c) 2012-2024 All right reserved.
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.main.selectedfolder;

import com.girbola.controllers.folderscanner.SelectedFolder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class SelectedFolderScanner {

    private ObservableList<SelectedFolder> selectedFolderScanner_obs = FXCollections.observableArrayList();

    public ObservableList<SelectedFolder> getSelectedFolderScanner_obs() {
        return this.selectedFolderScanner_obs;
    }

}
