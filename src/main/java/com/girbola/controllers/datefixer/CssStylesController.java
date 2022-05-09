/*
 @(#)Copyright:  Copyright (c) 2012-2020 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.datefixer;

public class CssStylesController {

	private static final String bad_style = "-fx-background-color: derive(red, 50%)";
	private static final String confirmed_style = "-fx-background-color: derive(green, 50%)";
	private static final String good_style = "-fx-background-color: derive(grey, 50%)";
	private static final String modified_style = "-fx-background-color: derive(blue, 50%)";
	private static final String suggested_style = "-fx-background-color: derive(pink, 50%); -fx-text-fill: black;";
	private static final String video_style = "-fx-background-color: orange;  -fx-text-fill: black;";

	public static String getConfirmed_style() {
		return confirmed_style;
	}

	public static String getBad_style() {
		return bad_style;
	}

	public static String getSuggested_style() {
		return suggested_style;
	}

	public static String getModified_style() {
		return modified_style;
	}

	public static String getGood_style() {
		return good_style;
	}

	public static String getVideo_style() {
		return video_style;
	}

}
