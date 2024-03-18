/*
 @(#)Copyright:  Copyright (c) 2012-2024 All right reserved.
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.main.tables;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

/**
 *
 * @author Marko
 */
public class TableCallBack_Sortit implements Callback<TableColumn<FolderInfo, String>, TableCell<FolderInfo, String>> {

    @Override
    public TableCell<FolderInfo, String> call(TableColumn<FolderInfo, String> p) {
        TableCell<FolderInfo, String> cell = new TableCell<FolderInfo, String>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                String text = getString();
                setText(empty ? null : text);
                setGraphic(null);
                getStyleClass().remove("notOk");
                if (text != null && text.contains("Not OK")) {
                    getStyleClass().add("notOk");
                }
            }

            private String getString() {
                return getItem() == null ? "" : getItem();
            }
        };
        return cell;
    }
}
