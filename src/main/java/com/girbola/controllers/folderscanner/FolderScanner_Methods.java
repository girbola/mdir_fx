/*
 @(#)Copyright:  Copyright (c) 2012-2025 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.folderscanner;

import javafx.scene.Node;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;

import java.nio.file.Path;

import static com.girbola.messages.Messages.sprintf;

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
