/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.main.tables.cell;

import com.girbola.controllers.main.Model_main;
import com.girbola.controllers.main.tables.FolderInfo;
import javafx.scene.control.TableCell;
import javafx.scene.paint.Color;

/**
 *
 * @author Marko Lokka
 */
public class TableCell_DateDifference_Status extends TableCell<FolderInfo, Double> {

	private Model_main model_main;

	public TableCell_DateDifference_Status(Model_main model_main) {
		this.model_main = model_main;

	}

	@Override
	protected void updateItem(Double item, boolean empty) {
		super.updateItem(item, empty);
		if (empty) {
			setGraphic(null);
			setText(null);
		} else {
			FolderInfo folderInfo = getTableView().getItems().get(getIndex());
			if (folderInfo != null) {
				if (folderInfo.getFolderFiles() != 0) {
					if (folderInfo.getStatus() != 100 || folderInfo.getDateDifferenceRatio() >= 2) {
						// sprintf("folderInfo.getStatus(): " + folderInfo.getStatus() + "
						// folderInfo.getDateDifferenceRatio(): " +
						// folderInfo.getDateDifferenceRatio());
						setTextFill(Color.RED);
						setText("" + item);
						// setStyle("-fx-background-color: red;");
						setStyle("-fx-text-fill: red;");
					} else {

						// sprintf("ELSE!! folderInfo.getStatus(): " + folderInfo.getStatus() + "
						// folderInfo.getDateDifferenceRatio(): " +
						// folderInfo.getDateDifferenceRatio());
						// setTextFill(null);
						// setTextFill(Color.RED);
						setText("" + item);
						// setStyle("-fx-background-color: green;");
						setStyle("-fx-text-fill: green;");
						// setStyle("-fx-background-color: transparent;");
					}
				} else {
					setStyle("-fx-text-fill: transparent;");
				}
				// sprintf("folderInfo-> " + folderInfo.toString());
				// sprintf("file: " + folderInfo.getFolderPath() + " folderInfo
				// tablecell_Status: " + folderInfo.getStatus() + " raw: " +
				// folderInfo.getFolderRawFiles());
			}
		}
	}
}
