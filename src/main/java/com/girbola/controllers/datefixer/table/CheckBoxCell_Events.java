
package com.girbola.controllers.datefixer.table;

import com.girbola.controllers.datefixer.ModelDatefix;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

import static com.girbola.messages.Messages.sprintf;


public class CheckBoxCell_Events extends TableCell<EXIF_Data_Selector, Boolean> {

	private final String ERROR = CheckBoxCell_Events.class.getSimpleName();
	private CheckBox checkBox;
	private ModelDatefix model_DateFix;

	public CheckBoxCell_Events(ModelDatefix model_DateFix) {
		this.model_DateFix = model_DateFix;
	}

	@Override
	protected void updateItem(Boolean item, boolean empty) {
		super.updateItem(item, empty);
		if (empty) {
			sprintf("CheckBoxCell_Events checkboxtree was empty");
			this.setGraphic(null);
			this.setText(null);
		} else {
			sprintf("Painting cell CheckBoxCell_Events! getTypeSelector : " + getTypeSelector());
			paintCell();
		}
	}

	private void paintCell() {
		if (checkBox == null) {
			checkBox = new CheckBox();
			checkBox.setSelected(getValue());
			checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
					// setItem(newValue);
					sprintf("CheckBoxCell_Events is selected? " + newValue);
					Task<ObservableList<Node>> updateEvents_Task = new Task<ObservableList<Node>>() {
						@Override
						protected ObservableList<Node> call() throws Exception {
							model_DateFix.getCameras_TableView().setDisable(true);
							model_DateFix.getDates_TableView().setDisable(true);
							model_DateFix.getEvents_TableView().setDisable(true);
							model_DateFix.getLocations_TableView().setDisable(true);

							EXIF_Data_Selector events_data = (EXIF_Data_Selector) getTableView().getItems().get(getIndex());
							events_data.setIsShowing(newValue);
							sprintf("events checkboz: " + checkBox + " data isShowing? " + events_data.isShowing() + " string= "
									+ events_data.getInfo());
							ObservableList<Node> theList = FXCollections.observableArrayList();
							List<String> listOfEvents = new ArrayList<>();
							if (newValue == true) {
								for (EXIF_Data_Selector events : model_DateFix.getEvents_TableView().getItems()) {
									if (events.isShowing()) {
										Messages.sprintf("Were showing events: " + events);
										listOfEvents.add(events.getInfo());
									}
								}
							} else {
								for (EXIF_Data_Selector events : model_DateFix.getEvents_TableView().getItems()) {
									if (!events.isShowing()) {
										listOfEvents.add(events.getInfo());
									}
								}
							}
							if (listOfEvents.isEmpty()) {
								sprintf("List of cameras were empty");
								return theList;
							}
							int counter = 0;
							if (newValue == true) {
								for (Node node : model_DateFix.getTilePane().getChildren()) {
									if (node instanceof VBox && node.getId().equals("imageFrame")) {

										Messages.sprintf("NODEEEE: " + node.getId());
										FileInfo fi = (FileInfo) node.getUserData();
										if (has_events(fi.getEvent(), listOfEvents)) {
											theList.add(node);
											Messages.sprintf("About to add");
											model_DateFix.getSelectionModel().addOnly(node);

											Messages.sprintf("-------added");
										}
										counter++;
									}
								}
							} else if (newValue == false) {
								for (Node node : model_DateFix.getTilePane().getChildren()) {
									if (node instanceof VBox && node.getId().equals("imageFrame")) {
										FileInfo fi = (FileInfo) node.getUserData();
										if (has_events(fi.getEvent(), listOfEvents)) {
											theList.add(node);
											model_DateFix.getSelectionModel().remove(node);
										}
										counter++;
									}
								}
							}
							return theList;
						}
					};
					updateEvents_Task.setOnSucceeded((WorkerStateEvent event) -> {
//						model_DateFix.getScrollPane().setVvalue(-1);
//						model_DateFix.getScrollPane().setVvalue(0);

						model_DateFix.getCameras_TableView().setDisable(false);
						model_DateFix.getDates_TableView().setDisable(false);
						model_DateFix.getEvents_TableView().setDisable(false);
						model_DateFix.getLocations_TableView().setDisable(false);
					});

					updateEvents_Task.setOnCancelled((WorkerStateEvent event) -> {
						sprintf("updateEvents_Task cancelled");
					});

					updateEvents_Task.setOnFailed((WorkerStateEvent event) -> {
						sprintf("updateEvents_Task failed");
						Messages.errorSmth(ERROR, "", null, Misc.getLineNumber(), true);
					});
					new Thread(updateEvents_Task).run();
				}

				private boolean has_events(String format, List<String> listOfEvents) {
					Messages.sprintf("Has events started");
					for (String str : listOfEvents) {
						if (str.equals(format)) {
							Messages.sprintf("has_events format: " + str);
							return true;
						}
					}
					return false;
				}
			});
		}
		checkBox.setSelected(getValue());
		setText(null);
		setGraphic(checkBox);
	}

	private Boolean getValue() {
		return getItem() == null ? false : getItem();
	}

}
