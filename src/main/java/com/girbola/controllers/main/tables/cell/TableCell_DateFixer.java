/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.main.tables.cell;

import com.girbola.controllers.datefixer.DateFixer;
import com.girbola.controllers.datefixer.GUI_Methods;
import com.girbola.controllers.main.Model_main;
import com.girbola.controllers.main.tables.FolderInfo;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.image.ImageView;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.girbola.messages.Messages.sprintf;

/**
 *
 * @author Marko
 */
public class TableCell_DateFixer extends TableCell<FolderInfo, String> {

	Button dateFixerButton = new Button();

	private Model_main model_main;

	public TableCell_DateFixer(Model_main model_main) {
		this.model_main = model_main;
		ImageView view_iv = new ImageView(GUI_Methods.loadImage("view.png", 15));
		dateFixerButton.setGraphic(view_iv);
		dateFixerButton.setStyle(null);
		dateFixerButton.getStyleClass().add("view_btn");
		dateFixerButton.setDisable(false);
	}

	@Override
	public void updateItem(String item, boolean empty) {
		super.updateItem(item, empty);
		if (empty) {
			setGraphic(null);
			setText(null);
		} else {
			FolderInfo folderInfo = getTableView().getItems().get(getIndex());
			if (folderInfo.getFolderFiles() == 0) {
				dateFixerButton.setDisable(true);
				dateFixerButton.setOnAction(null);
			} else {
				dateFixerButton.setDisable(false);
				Path path = Paths.get(folderInfo.getFolderPath());
				dateFixerButton.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {

//						model_main.getRegisterTableActivity().cancel();
						Task<Void> dateFixer = new DateFixer(path, folderInfo, model_main, false);
						Thread dateFixer_th = new Thread(dateFixer, "dateFixer_th");
						sprintf("dateFixer_th.getName(): " + dateFixer_th.getName());
						dateFixer_th.start();
					}
				});
			}
			setGraphic(dateFixerButton);
			setText(null);
		}
	}
}
