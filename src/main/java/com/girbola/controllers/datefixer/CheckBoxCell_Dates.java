/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.datefixer;

import static com.girbola.Main.simpleDates;
import static com.girbola.messages.Messages.sprintf;

import java.util.ArrayList;
import java.util.List;

import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.layout.VBox;

/**
 *
 * @author Marko Lokka
 */
public class CheckBoxCell_Dates extends TableCell<EXIF_Data_Selector, Boolean> {

	private final String ERROR = CheckBoxCell_Dates.class.getSimpleName();
	private CheckBox checkBox;
	private Model_datefix model_DateFix;

	public CheckBoxCell_Dates(Model_datefix model_DateFix) {
		this.model_DateFix = model_DateFix;
	}

	@Override
	protected void updateItem(Boolean item, boolean empty) {
		super.updateItem(item, empty);

		if (empty) {
			this.setGraphic(null);
			this.setText(null);
		} else {
			checkBox = new CheckBox();
			checkBox.setSelected(getValue());
			setAlignment(Pos.CENTER);
			checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
					setItem(newValue);
					sprintf("checkBox DATES is selected? " + newValue);
					model_DateFix.getCameras_TableView().setDisable(true);
					model_DateFix.getDates_TableView().setDisable(true);
					model_DateFix.getEvents_TableView().setDisable(true);
					model_DateFix.getLocations_TableView().setDisable(true);

					EXIF_Data_Selector dates_data = getTableView().getItems().get(getIndex());
					dates_data.setIsShowing(newValue);
					sprintf("dates checkboz: " + checkBox + " data isShowing? " + dates_data.isShowing() + " string= "
							+ dates_data.getInfo());
					ObservableList<Node> theList = FXCollections.observableArrayList();
					List<String> listOfDates = new ArrayList<>();
					Task<ObservableList<Node>> updateDates_Task = new Task<ObservableList<Node>>() {
						@Override
						protected ObservableList<Node> call() throws Exception {
							if (newValue == true) {
								for (EXIF_Data_Selector date : model_DateFix.getDates_TableView().getItems()) {
									if (date.isShowing()) {
										listOfDates.add(date.getInfo());
									}
								}
							} else {
								for (EXIF_Data_Selector date : model_DateFix.getDates_TableView().getItems()) {
									if (!date.isShowing()) {
										listOfDates.add(date.getInfo());
									}
								}
							}
							if (listOfDates.isEmpty()) {
								return theList;
							}
							if (newValue == true) {
								for (Node node : model_DateFix.getGridPane().getChildren()) {
									if (node instanceof VBox && node.getId().equals("imageFrame")) {
										FileInfo fi = (FileInfo) node.getUserData();
										if (hasDate(simpleDates.getSdf_ymd_minus().format(fi.getDate()), listOfDates)) {
											theList.add(node);
											model_DateFix.getSelectionModel().addOnly(node);
										}
									}
								}
							} else if (newValue == false) {
								for (Node node : model_DateFix.getGridPane().getChildren()) {
									if (node instanceof VBox && node.getId().equals("imageFrame")) {
										FileInfo fi = (FileInfo) node.getUserData();
										if (hasDate(simpleDates.getSdf_ymd_minus().format(fi.getDate()), listOfDates)) {
											theList.remove(node);
											model_DateFix.getSelectionModel().remove(node);
										}
									}
								}
							}
							return theList;
						}
					};
					updateDates_Task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
						@Override
						public void handle(WorkerStateEvent event) {
//							model_DateFix.getScrollPane().setVvalue(-1);
//							model_DateFix.getScrollPane().setVvalue(0);

							model_DateFix.getCameras_TableView().setDisable(false);
							model_DateFix.getDates_TableView().setDisable(false);
							model_DateFix.getEvents_TableView().setDisable(false);
							model_DateFix.getLocations_TableView().setDisable(false);
						}
					});
					updateDates_Task.setOnCancelled(new EventHandler<WorkerStateEvent>() {
						@Override
						public void handle(WorkerStateEvent event) {
							sprintf("updateDates_Task cancelled");
						}
					});
					updateDates_Task.setOnFailed(new EventHandler<WorkerStateEvent>() {
						@Override
						public void handle(WorkerStateEvent event) {
							sprintf("updateDates_Task failed");
							Messages.errorSmth(ERROR, "", null, Misc.getLineNumber(), true);
						}
					});
					new Thread(updateDates_Task).run();
				}

				private boolean hasDate(String format, List<String> listOfDates) {
					for (String str : listOfDates) {
						if (str.equals(format)) {
							return true;
						}
					}
					return false;
				}
			});

			setText(null);
			setGraphic(checkBox);

		}
	}

	private Boolean getValue() {
		return getItem() == null ? false : getItem();
	}

}
