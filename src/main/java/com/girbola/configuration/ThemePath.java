/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.configuration;

enum ThemePath {
    DARK("/resources/themes/dark/"),
    LIGHT("/resources/themes/light/");

    private String type;

    public String getType() {
        return type;
    }

    ThemePath(String type) {
        this.type = type;
    }
}
