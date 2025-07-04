
package com.girbola.controllers.datefixer.utils;

import com.girbola.fileinfo.FileInfo;
import java.util.Collections;
import java.util.Comparator;
import javafx.collections.ObservableList;
import javafx.scene.Node;


public class Sorting_Utils {

    public static void sortByFileName(ObservableList<Node> list) {
        Collections.sort(list, new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                FileInfo f1 = (FileInfo) o1.getUserData();
                FileInfo f2 = (FileInfo) o2.getUserData();
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
