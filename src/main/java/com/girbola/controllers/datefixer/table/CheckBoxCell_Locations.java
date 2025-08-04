
package com.girbola.controllers.datefixer.table;

import com.girbola.controllers.datefixer.ModelDatefix;
import com.girbola.controllers.datefixer.utils.MetadataField;
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


public class CheckBoxCell_Locations extends TableCell<EXIF_Data_Selector, Boolean> {

	private final String ERROR = CheckBoxCell_Locations.class.getSimpleName();
	private CheckBox checkBox;
	private ModelDatefix model_DateFix;

	public CheckBoxCell_Locations(ModelDatefix model_DateFix) {
		this.model_DateFix = model_DateFix;
	}

	@Override
	protected void updateItem(Boolean item, boolean empty) {
		super.updateItem(item, empty);
		if (empty) {
			sprintf("CheckBoxCell_Cameras checkboxtree was empty");
			this.setGraphic(null);
			this.setText(null);
		} else {
			sprintf("Painting cell! getTypeSelector : " + getTypeSelector());
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
					sprintf("checkBox Cameras is selected? " + newValue);
					Task<ObservableList<Node>> updateLocations_Task = new Task<ObservableList<Node>>() {
						@Override
						protected ObservableList<Node> call() throws Exception {
							model_DateFix.getCameras_TableView().setDisable(true);
							model_DateFix.getDates_TableView().setDisable(true);
							model_DateFix.getEvents_TableView().setDisable(true);
							model_DateFix.getLocations_TableView().setDisable(true);

							EXIF_Data_Selector location_data = (EXIF_Data_Selector) getTableView().getItems().get(getIndex());
							location_data.setIsShowing(newValue);
							sprintf("locations checkboz: " + checkBox + " data isShowing? " + location_data.isShowing() + " string= "
									+ location_data.getInfo());
							ObservableList<Node> theList = FXCollections.observableArrayList();
							List<String> listOfLocations = new ArrayList<>();
							if (newValue == true) {
								for (EXIF_Data_Selector locations : model_DateFix.getLocations_TableView().getItems()) {
									if (locations.isShowing()) {
										listOfLocations.add(locations.getInfo());
									}
								}
							} else {
								for (EXIF_Data_Selector locations : model_DateFix.getLocations_TableView().getItems()) {
									if (!locations.isShowing()) {
										listOfLocations.add(locations.getInfo());
									}
								}
							}
							if (listOfLocations.isEmpty()) {
								sprintf("List of locations were empty");
								return theList;
							}

							int counter = 0;
							if (newValue) {
								for (Node node : model_DateFix.getTilePane().getChildren()) {
									if (node instanceof VBox && node.getId().equals("imageFrame")) {
										FileInfo fi = (FileInfo) node.getUserData();
										if (!fi.getLocation().isEmpty()) {
											if (has_locations(fi.getLocation(), listOfLocations)) {
												theList.add(node);
												model_DateFix.getSelectionModel().addOnly(node);
											}
										}
										counter++;
									}
								}

							} else {
								for (Node node : model_DateFix.getTilePane().getChildren()) {
									if (node instanceof VBox && node.getId().equals("imageFrame")) {
										FileInfo fi = (FileInfo) node.getUserData();
										if (has_locations(fi.getLocation(), listOfLocations)) {
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
					updateLocations_Task.setOnSucceeded((WorkerStateEvent event) -> {
//						model_DateFix.getScrollPane().setVvalue(-1);
//						model_DateFix.getScrollPane().setVvalue(0);

						model_DateFix.getCameras_TableView().setDisable(false);
						model_DateFix.getDates_TableView().setDisable(false);
						model_DateFix.getEvents_TableView().setDisable(false);
						model_DateFix.getLocations_TableView().setDisable(false);
					});

					updateLocations_Task.setOnCancelled((WorkerStateEvent event) -> {
						sprintf("updateCamera_Task cancelled");
					});

					updateLocations_Task.setOnFailed((WorkerStateEvent event) -> {
						sprintf("updateCamera_Task failed");
						Messages.errorSmth(ERROR, "", null, Misc.getLineNumber(), true);
					});
					new Thread(updateLocations_Task).run();
				}

				private boolean has_locations(String format, List<String> listOfLocations) {
					for (String str : listOfLocations) {
						if (str.equals(format)) {
							return true;
						}
						if (str.isEmpty()) {
							if (str.equals(MetadataField.UNKNOWN.getType())) {
								return true;
							}
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
