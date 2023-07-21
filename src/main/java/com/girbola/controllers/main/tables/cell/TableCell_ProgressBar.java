/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.main.tables.cell;

import com.girbola.controllers.main.tables.FolderInfo;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.layout.StackPane;
import javafx.scene.text.TextAlignment;

import static com.girbola.messages.Messages.sprintf;

/**
 *
 * @author Marko
 */
public class TableCell_ProgressBar extends TableCell<FolderInfo, Integer> {

    final StackPane stackPane_pbar_background = new StackPane();
    final ProgressBar pbar = new ProgressBar(10);
    final Label text = new Label("0");
    private FolderInfo sortit;
    final String[] barColorStyleClasses = {"pbar20", "pbar40", "pbar60", "pbar80", "pbar100"};
//    double result = 0;

    public TableCell_ProgressBar() {

//        stackPane_pbar_background.getStyleClass().add("stackPane_pbar_background");
        stackPane_pbar_background.getChildren().addAll(pbar, text);

//        text.setText("Analyzing...");
    }

    @Override
    public void updateItem(Integer item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
            setText(null);
        } else {
            sprintf("Item is: " + item);
            sortit = (FolderInfo) getTableView().getItems().get(getIndex());
//            sortit.setStatus(item);
//            text.textProperty().bindBidirectional((Property<String>) sortit.status_property().asString());
//            sortit.status_property().addListener(new ChangeListener<Number>() {
//                @Override
//                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//                    sprintf("sortitititit: " + newValue);
//                    text.setText("" + newValue);
//                }
//            });
            text.setTextAlignment(TextAlignment.CENTER);

            setGraphic(stackPane_pbar_background);
            setText(null);
        }
    }

}
