/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.datefixer.utils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Marko Lokka
 */
public class CameraColor {

    private AtomicInteger color = new AtomicInteger(-1);
    private String color1 = "-fx-background-color: yellow;";
    private String color2 = "-fx-background-color: blue;";
    private String color3 = "-fx-background-color: orange;";
    private String color4 = "-fx-background-color: cyan;";
    private String color5 = "-fx-background-color: green;";

    String getCameraColor_Increase() {
        int c = color.incrementAndGet();
        if (c == 0) {
            return color1;
        } else if (c == 1) {
            return color2;
        } else if (c == 2) {
            return color3;
        } else if (c == 3) {
            return color4;
        } else if (c == 4) {
            color.set(-1);
            return color5;
        }
        return color1;
    }

}
