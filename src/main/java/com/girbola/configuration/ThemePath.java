/*
 @(#)Copyright:  Copyright (c) 2012-2024 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.configuration;

enum ThemePath {
    DARK("dark"),
    LIGHT("light");

    private String type;

    public String getType() {
        return type;
    }

    ThemePath(String type) {
        this.type = type;
    }
}
