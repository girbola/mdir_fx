/*
 @(#)Copyright:  Copyright (c) 2012-2024 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.configuration;

/**
 * The GUIPrefs class holds static configuration values for the graphical user interface.
 * These values are used to define sizes and dimensions for various GUI components such as buttons, frames, and thumbnails.
 */
public class GuiImageFrame {

    public final static double ARROW_BUTTON_SIZE = 13;
    public final static int BUTTON_WIDTH = 20;

    public static double FOLDER_SIZE = 40;


    /**
     * The horizontal position of the image frame in the graphical user interface.
     * This value is used to set or adjust the X-coordinate of the image frame.
     * Ratio is 0,75
     */
    public static int imageFrame_x = 250;
    public static int imageFrame_y = 250;

    /**
     * The maximum width allowed for a thumbnail image in the graphical user interface.
     * Ratio is 0,565
     */
    public static double thumb_x_MAX = 200;
    public static double thumb_y_MAX = 113;

}
