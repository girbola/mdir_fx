/*
 @(#)Copyright:  Copyright (c) 2012-2024 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.datefixer;

import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.messages.Messages;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SelectorController {

	private WorkDir_Loader workDir_Loader;
	private Model_datefix model_datefix;
	private TilePane df_tilePane;
	//private GridPane df_gridPane;
	private FolderInfo folderInfo;
	//@formatter:off
	@FXML private ScrollPane infoTables_container;
	@FXML private VBox selector_root;
	@FXML private TitledPane dates_titledPane;
	@FXML private TitledPane cameras_titledPane;
	@FXML private TitledPane events_titledPane;
	@FXML private TitledPane locations_titledPane;
	@FXML private TableView<EXIF_Data_Selector> cameras_tableView;
	@FXML private TableColumn<EXIF_Data_Selector, Boolean> cameras_checkBox_hide_col;
	@FXML private TableColumn<EXIF_Data_Selector, String> cameras_col;
	@FXML private TableColumn<EXIF_Data_Selector, Integer> cameras_counter_col;
	@FXML private TableView<EXIF_Data_Selector> dates_tableView;
	@FXML private TableColumn<EXIF_Data_Selector, Boolean> dates_checkBox_hide_col;
	@FXML private TableColumn<EXIF_Data_Selector, String> dates_col;
	@FXML private TableColumn<EXIF_Data_Selector, Integer> dates_counter_col;
	@FXML private TableView<EXIF_Data_Selector> locations_tableView;
	@FXML private TableColumn<EXIF_Data_Selector, Boolean> locations_checkBox_hide_col;
	@FXML private TableColumn<EXIF_Data_Selector, String> locations_col;
	@FXML private TableColumn<EXIF_Data_Selector, Integer> locations_counter_col;
	@FXML private TableView<EXIF_Data_Selector> events_tableView;
	@FXML private TableColumn<EXIF_Data_Selector, Boolean> events_checkBox_hide_col;
	@FXML private TableColumn<EXIF_Data_Selector, String> events_col;
	@FXML private TableColumn<EXIF_Data_Selector, Integer> events_counter_col;

	Callback<TableColumn<EXIF_Data_Selector, Boolean>, TableCell<EXIF_Data_Selector, Boolean>> checkbox_DATES_CellFactory = p -> new CheckBoxCell_Dates(model_datefix);
	Callback<TableColumn<EXIF_Data_Selector, Boolean>, TableCell<EXIF_Data_Selector, Boolean>> checkbox_CAMERAS_CellFactory = p -> new CheckBoxCell_Cameras(model_datefix);
	Callback<TableColumn<EXIF_Data_Selector, Boolean>, TableCell<EXIF_Data_Selector, Boolean>> checkbox_EVENTS_CellFactory = p -> new CheckBoxCell_Events(model_datefix);
	Callback<TableColumn<EXIF_Data_Selector, Boolean>, TableCell<EXIF_Data_Selector, Boolean>> checkbox_LOCATIONS_CellFactory = p -> new CheckBoxCell_Locations(model_datefix);
	//@formatter:on

	@Deprecated
	Callback<TableColumn<EXIF_Data_Selector, Boolean>, TableCell<EXIF_Data_Selector, Boolean>> dates_INFO_Select_CellFactory = p -> new Dates_INFO_Select_CellFactory(
			model_datefix);

	public void init(Model_datefix aModel_datefix, TilePane aDf_tilePane) {
		this.model_datefix = aModel_datefix;
		this.df_tilePane = aDf_tilePane;
		this.folderInfo = model_datefix.getFolderInfo_full();
		if (folderInfo == null) {
			Messages.sprintfError("folderinfo were null!!!");
		}

		Messages.sprintf("Folderinfo path name is: " + this.folderInfo.getFolderPath());

		workDir_Loader = new WorkDir_Loader(Paths.get(folderInfo.getFolderPath()));
		//df_gridPane.setGridLinesVisible(true);
		model_datefix.setDates_TableView(dates_tableView);
		model_datefix.setCameras_TableView(cameras_tableView);
		model_datefix.setEvents_TableView(events_tableView);
		model_datefix.setLocations_TableView(locations_tableView);

		// model_datefix.getDateFix_Utils().createDates_list(model_datefix.getFolderInfo_full().getFileInfoList());
		dates_tableView.setItems(model_datefix.getDateFix_Utils().getDate_obs());
		dates_checkBox_hide_col.setCellFactory(checkbox_DATES_CellFactory);
		dates_checkBox_hide_col.setCellValueFactory(
				(TableColumn.CellDataFeatures<EXIF_Data_Selector, Boolean> cellData) -> new SimpleObjectProperty<>(
						cellData.getValue().isShowing()));
		dates_col.setCellValueFactory(
				(TableColumn.CellDataFeatures<EXIF_Data_Selector, String> cellData) -> new SimpleObjectProperty<>(
						cellData.getValue().getInfo()));
		dates_counter_col.setCellValueFactory(
				(TableColumn.CellDataFeatures<EXIF_Data_Selector, Integer> cellData) -> new SimpleObjectProperty<>(
						cellData.getValue().getCount()));
		dates_tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		dates_tableView.setRowFactory(new TableRowSelector(dates_tableView, model_datefix.getScrollPane()));
		dates_tableView.getSelectionModel().selectedItemProperty()
				.addListener(new ChangeListener<EXIF_Data_Selector>() {
					@Override
					public void changed(ObservableValue<? extends EXIF_Data_Selector> observable,
							EXIF_Data_Selector oldValue, EXIF_Data_Selector newValue) {
						Task<Boolean> selectByType = new SelectByTableModel(model_datefix,
								SelectorModelType.DATE.getType(), dates_tableView);
						model_datefix.getSelector_exec().scheduleAtFixedRate(selectByType, 0, 100,
								TimeUnit.MILLISECONDS);
					}
				});
		// model_datefix.getDateFix_Utils().createCamera_list(model_datefix.getFolderInfo_full().getFileInfoList());
		cameras_tableView.setItems(model_datefix.getDateFix_Utils().getCameras_obs());
		cameras_tableView.getSelectionModel().selectedItemProperty()
				.addListener(new ChangeListener<EXIF_Data_Selector>() {
					@Override
					public void changed(ObservableValue<? extends EXIF_Data_Selector> observable,
							EXIF_Data_Selector oldValue, EXIF_Data_Selector newValue) {
						Task<Boolean> selectByType = new SelectByTableModel(model_datefix,
								SelectorModelType.CAMERA.getType(), cameras_tableView);
						model_datefix.getSelector_exec().scheduleAtFixedRate(selectByType, 0, 100,
								TimeUnit.MILLISECONDS);
					}
				});
		cameras_checkBox_hide_col.setCellFactory(checkbox_CAMERAS_CellFactory);
		cameras_checkBox_hide_col.setCellValueFactory(
				(TableColumn.CellDataFeatures<EXIF_Data_Selector, Boolean> cellData) -> new SimpleObjectProperty<>(
						cellData.getValue().isShowing()));
		cameras_col.setCellValueFactory(
				(TableColumn.CellDataFeatures<EXIF_Data_Selector, String> cellData) -> new SimpleObjectProperty<>(
						cellData.getValue().getInfo()));
		cameras_counter_col.setCellValueFactory(
				(TableColumn.CellDataFeatures<EXIF_Data_Selector, Integer> cellData) -> new SimpleObjectProperty<>(
						cellData.getValue().getCount()));
		cameras_tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		cameras_tableView.setRowFactory(new TableRowSelector(cameras_tableView, model_datefix.getScrollPane()));

		// model_datefix.getDateFix_Utils().createEvent_list(model_datefix.getFolderInfo_full().getFileInfoList());
		events_tableView.setItems(model_datefix.getDateFix_Utils().getEvents_obs());
		events_checkBox_hide_col.setCellFactory(checkbox_EVENTS_CellFactory);
		events_checkBox_hide_col.setCellValueFactory(
				(TableColumn.CellDataFeatures<EXIF_Data_Selector, Boolean> cellData) -> new SimpleObjectProperty<>(
						cellData.getValue().isShowing()));
		events_col.setCellValueFactory(
				(TableColumn.CellDataFeatures<EXIF_Data_Selector, String> cellData) -> new SimpleObjectProperty<>(
						cellData.getValue().getInfo()));
		events_counter_col.setCellValueFactory(
				(TableColumn.CellDataFeatures<EXIF_Data_Selector, Integer> cellData) -> new SimpleObjectProperty<>(
						cellData.getValue().getCount()));
		events_tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		events_tableView.setRowFactory(new TableRowSelector(events_tableView, model_datefix.getScrollPane()));

		// model_datefix.getDateFix_Utils().createLocation_list(model_datefix.getFolderInfo_full().getFileInfoList());
		locations_tableView.setItems(model_datefix.getDateFix_Utils().getLocations_obs());
		locations_checkBox_hide_col.setCellFactory(checkbox_LOCATIONS_CellFactory);
		locations_checkBox_hide_col.setCellValueFactory(
				(TableColumn.CellDataFeatures<EXIF_Data_Selector, Boolean> cellData) -> new SimpleObjectProperty<>(
						cellData.getValue().isShowing()));
		locations_col.setCellValueFactory(
				(TableColumn.CellDataFeatures<EXIF_Data_Selector, String> cellData) -> new SimpleObjectProperty<>(
						cellData.getValue().getInfo()));
		locations_counter_col.setCellValueFactory(
				(TableColumn.CellDataFeatures<EXIF_Data_Selector, Integer> cellData) -> new SimpleObjectProperty<>(
						cellData.getValue().getCount()));
		locations_tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		locations_tableView.setRowFactory(new TableRowSelector(locations_tableView, model_datefix.getScrollPane()));
		selector_root.heightProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				Messages.sprintf("selector_root height has changed to: " + newValue);
			}
		});
		cameras_titledPane.heightProperty().addListener((ChangeListener<Number>) (observable, oldValue, newValue) -> Messages.sprintf("cameras_titledPane height has changed to: " + newValue + " root height is : "
				+ selector_root.getHeight()));
//		cameras_tableView.heightProperty().addListener(listener);
//

	}

	private boolean hasDate(String format, List<String> listOfDates) {
		for (String str : listOfDates) {
			if (str.equals(format)) {
				return true;
			}
		}
		return false;
	}

	private void hideTableViewHeader(TableView<EXIF_Data_Selector> table) {
		table.widthProperty().addListener((ChangeListener<Number>) (ov, t, t1) -> {
			// Get the table header
			Pane header = (Pane) table.lookup("TableHeaderRow");
			if (header != null && header.isVisible()) {
				header.setMaxHeight(0);
				header.setMinHeight(0);
				header.setPrefHeight(0);
				header.setVisible(false);
				header.setManaged(false);
			}
		});
	}

	@Deprecated
	private void refreshTable_(TableView<?> table) {
		if (table == null) {
			return;
		}
		if (table.getColumns().get(0) != null) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					table.getColumns().get(0).setVisible(false);
					table.getColumns().get(0).setVisible(true);
					table.refresh();
				}
			});
		}
	}

	public ScrollPane getInfoTables_container() {
		return this.infoTables_container;
	}
}
