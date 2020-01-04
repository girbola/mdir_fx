/*
 @(#)Copyright:  Copyright (c) 2012-2020 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.main.tables.tabletype;

/**
 *
 * @author Marko Lokka
 */
public enum TableType {
    SORTIT("SortIt"),
    SORTED("Sorted"),
    ASITIS("AsItIs");

    private String type;

    TableType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
