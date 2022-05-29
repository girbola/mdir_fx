/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.folderscanner;

import static com.girbola.messages.Messages.sprintf;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 *
 * @author Marko Lokka
 */
public class FolderScanner_Methods {

    public static boolean titledPaneExists(VBox listOfRoots_vbox, Path p) {
        for (Node n : listOfRoots_vbox.getChildren()) {
            if (n instanceof TitledPane) {
                if (((TitledPane) n).getText().equals(p.toString())) {
                    sprintf("Titledpane did exists");
                    return true;
                }
            }
        }
        return false;
    }



}
