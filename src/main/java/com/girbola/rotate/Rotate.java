/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.rotate;

/**
 *
 * @author Marko Lokka
 */
public class Rotate {

    public static double rotate(int orientation) {
        switch (orientation) {
            case 1:
                break;
            case 2:
                break;
            case 3:
                break;
            case 4:
                break;
            case 5:
                break;
            case 6:
                return 90;
            case 7:
                break;
            case 8:
                return -90;
            default:
                return 0;
        }
        return 0;
    }

}
