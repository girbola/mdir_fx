/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.misc;

import javafx.application.Platform;

import static com.girbola.messages.Messages.sprintf;
import static com.girbola.misc.Misc.getLineNumber;

/**
 *
 * @author Marko Lokka
 */
public class Misc_GUI {

    public static void fastExit() {
        sprintf("exitProgram " + getLineNumber());
        sprintf("DELETING RUNNING DAT FILE DEMOOOOOOOOOOOOOOOOOOO FIX THIS BEFORE RELEASE" + getLineNumber());
        Platform.exit();
        // main_stage.close();
        System.exit(0);

    }
}
