/*
 @(#)Copyright:  Copyright (c) 2012-2019 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.main;

import static com.girbola.messages.Messages.sprintf;

import javafx.application.Platform;
import javafx.scene.control.MenuItem;

/**
 *
 * @author Marko Lokka
 */
public class Menu {

    private Model_main model;

    protected Menu(Model_main model) {
        this.model = model;
    }

//    public void closeStage(MenuItem button) {
//        System.gc();
//        Platform.exit();
//        sprintf("Exiting...");
//    }

}
