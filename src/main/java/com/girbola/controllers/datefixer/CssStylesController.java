/*
 @(#)Copyright:  Copyright (c) 2012-2020 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.datefixer;

public class CssStylesController {

	private static final String styleBad = "-fx-background-color: derive(red, 50%);";
	private static final String styleConfirmed = "-fx-background-color: derive(green, 50%);";
	private static final String styleGood = "-fx-background-color: derive(grey, 50%);";
	private static final String styleModified = "-fx-background-color: derive(blue, 50%);";
	private static final String styleSuggested = "-fx-background-color: derive(pink, 50%); -fx-text-fill: black;";
	private static final String styleVideo = "-fx-background-color: orange;  -fx-text-fill: black;";
	private static final String styleDeselected = "-fx-border-color: #807c7ca4;"
			+ "-fx-border-style: none;" 
			+ "-fx-border-width: 1px;";
	private static final String styleSelected = "-fx-border-color: red;" + "-fx-border-width: 2px;";

	public static String getStyleBad() {
		return styleBad;
	}

	public static String getStyleConfirmed() {
		return styleConfirmed;
	}

	public static String getStyleGood() {
		return styleGood;
	}

	public static String getStyleModified() {
		return styleModified;
	}

	public static String getStyleSuggested() {
		return styleSuggested;
	}

	public static String getStyleVideo() {
		return styleVideo;
	}

	public static String getStyleDeselected() {
		return styleDeselected;
	}

	public static String getStyleSelected() {
		return styleSelected;
	}

}
