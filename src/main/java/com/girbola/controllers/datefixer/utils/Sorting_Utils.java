/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.datefixer.utils;

import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import javafx.collections.ObservableList;
import javafx.scene.Node;

import java.util.Collections;
import java.util.Comparator;

/**
 * @author Marko Lokka
 */
public class Sorting_Utils {

    public static void sortByFileName(ObservableList<Node> list) {
        Collections.sort(list, new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                FileInfo f1 = (FileInfo) o1.getUserData();
                FileInfo f2 = (FileInfo) o2.getUserData();
                // return o1.getDate() < o2.getDate() ? -1 : 1;
                if (f1.getOrgPath() != null && f2.getOrgPath() != null) {
                    return f1.getOrgPath().compareTo(f2.getOrgPath());
                } else {
                    return 0;
                }

            }
        });
    }

    public static void sortByDate(ObservableList<Node> list) {
        Collections.sort(list, new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                FileInfo f1 = (FileInfo) o1.getUserData();
                FileInfo f2 = (FileInfo) o2.getUserData();
                if (f1.getDate() < f2.getDate()) {
                    return -1;
                } else if (f1.getDate() > f2.getDate()) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
    }

    public static void sortByCamera(ObservableList<Node> list) {
        Collections.sort(list, new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                FileInfo f1 = (FileInfo) o1.getUserData();
                FileInfo f2 = (FileInfo) o2.getUserData();
                if (f1.getCamera_model() != null && f2.getCamera_model() != null) {
                    return f1.getCamera_model().compareTo(f2.getCamera_model());
                } else {
                    return 0;
                }
            }
        });
    }
}
