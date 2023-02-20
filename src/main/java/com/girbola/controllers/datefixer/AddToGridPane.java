/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.datefixer;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/**
 *
 * @author Marko Lokka
 */
public class AddToGridPane implements Runnable {

    private GridPane gridPane;
    private VBox frame;
    private int x;
    private int y;

    public AddToGridPane(GridPane gridPane, VBox frame, int x, int y) {
        this.gridPane = gridPane;
        this.frame = frame;
        this.x = x;
        this.y = y;
    }

    @Override
    public void run() {
        gridPane.add(frame, x, y);
    }
}
