/*
 @(#)Copyright:  Copyright (c) 2012-2024 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.configuration;


enum Configuration_Type {

    SHOWHINTS("showHints"),
    SAVETHUMBS("savingThumbs"),
    WORKDIR("workDir"),
    THEMEPATH("themePath"),
    VLCPATH("vlcPath"),
    SAVEFOLDER("saveFolder"),
    CONFIRMONEXIT("confirmOnExit"),
    SHOWFULLPATH("showFullPath"),
    VLCSUPPORT("vlcSupport"),
    ID_COUNTER("id_counter"),
    SHOWTOOLTIPS("showTooltip"),
	BETTERQUALITYTHUMBS("betterQualityThumbs");
	
    private String type;

    Configuration_Type(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

}
