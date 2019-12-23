/*
 @(#)Copyright:  Copyright (c) 2012-2019 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.importimages;

import static com.girbola.messages.Messages.errorSmth;
import static com.girbola.messages.Messages.sprintf;

import java.nio.file.Path;
import java.sql.Connection;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.girbola.Main;
import com.girbola.controllers.datefixer.RenderVisibleNode;
import com.girbola.controllers.datefixer.TimeControl;
import com.girbola.fileinfo.FileInfo;
import com.girbola.misc.Misc;
import com.girbola.sql.SqliteConnection;

import common.utils.date.DateUtils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ScrollPane;
import javafx.util.Callback;
import javafx.util.StringConverter;

/**
 *
 * @author Marko Lokka
 */
class Model_importImages {

	public Model_importImages(Path currentFolderPath) {
		this.currentFolderPath = currentFolderPath;
		this.connection = SqliteConnection.connector(currentFolderPath, Main.conf.getThumbInfo_db_fileName());
		
	}

	private final String ERROR = Model_importImages.class.getSimpleName();

	private Path currentFolderPath;
	private final String pattern = "yyyy-MM-dd";

	private SelectionModel_Import selectionModel_Import = new SelectionModel_Import();
	private RenderVisibleNode renderVisibleNode;

	private GUIUtils GUIUtils = new GUIUtils(this);

	private ToggleButtonControl toggleButtonControl = new ToggleButtonControl();

	public ToggleButtonControl getToggleButtonControl() {
		return toggleButtonControl;
	}

	public void setToggleButtonControl(ToggleButtonControl toggleButtonControl) {
		this.toggleButtonControl = toggleButtonControl;
	}

	public GUIUtils getGUIUtils() {
		return GUIUtils;
	}

	private DatePicker start_datePicker;
	private DatePicker end_datePicker;

	private TimeControl s_time = new TimeControl();
	private TimeControl e_time = new TimeControl();

	private LocalDateTime min_ldf;
	private LocalDateTime max_ldf;

	private ComboBox event_cb;
	private ComboBox location_cb;

	private ObservableList<String> event_obs = FXCollections.observableArrayList();
	private ObservableList<String> location_obs = FXCollections.observableArrayList();
	private Connection connection;
	private ScrollPane scrollPane;
	private Map<String, List<FileInfo>> theList;

	public Map<String, List<FileInfo>> getTheList() {
		return theList;
	}

	public void setTheList(Map<String, List<FileInfo>> theList) {
		this.theList = theList;
	}

	public RenderVisibleNode getRenderVisibleNode() {
		return renderVisibleNode;
	}

	public ScrollPane getScrollPane() {
		return scrollPane;
	}

	public void setScrollPane(ScrollPane scrollPane) {
		this.scrollPane = scrollPane;
	}

	public ObservableList<String> getEvent_obs() {
		return event_obs;
	}

	public void setEvent_obs(ObservableList<String> event_obs) {
		this.event_obs = event_obs;
	}

	public ObservableList<String> getLocation_obs() {
		return location_obs;
	}

	public void setLocation_obs(ObservableList<String> location_obs) {
		this.location_obs = location_obs;
	}

	private Scene scene;

	public LocalDateTime getMin_ldf() {
		return min_ldf;
	}

	public LocalDateTime getMax_ldf() {
		return max_ldf;
	}

	public void setScene(Scene aScene) {
		this.scene = aScene;
	}

	void setMin_Max_TIMES(Map<String, List<FileInfo>> list) {
		if (list == null) {
			errorSmth(ERROR, "", null, Misc.getLineNumber(), true);
		}
		List<Long> dateList = new ArrayList<>();
		for (Map.Entry<String, List<FileInfo>> entry : list.entrySet()) {
			List<FileInfo> fi_list = entry.getValue();
			for (FileInfo fi : fi_list) {
				dateList.add(fi.getDate());
			}
		}

		Collections.sort(dateList);
		long min = Collections.min(dateList);
		long max = Collections.max(dateList);
		min_ldf = Instant.ofEpochMilli(min).atZone(ZoneId.systemDefault()).toLocalDateTime().minusDays(1);
		max_ldf = Instant.ofEpochMilli(max).atZone(ZoneId.systemDefault()).toLocalDateTime().minusDays(1);
		s_time.setHour(min_ldf.toLocalTime().getHour());
		s_time.setMin(min_ldf.toLocalTime().getMinute());
		s_time.setSec(min_ldf.toLocalTime().getSecond());
		e_time.setHour(max_ldf.toLocalTime().getHour());
		e_time.setMin(max_ldf.toLocalTime().getMinute());
		e_time.setSec(max_ldf.toLocalTime().getSecond());
		sprintf("min_ldf: " + min_ldf + "max_ldf: " + max_ldf);
		max = 0;
		min = 0;
		dateList.clear();

	}

	public Callback<DatePicker, DateCell> dateCellFactory(LocalDateTime localDateTime_limitter, boolean max) {
		Callback<DatePicker, DateCell> callBack = new Callback<DatePicker, DateCell>() {
			@Override
			public DateCell call(DatePicker param) {
				return new DateCell() {
					@Override
					public void updateItem(LocalDate item, boolean empty) {
						super.updateItem(item, empty);

						if (max) {
							if (item.isBefore(localDateTime_limitter.toLocalDate())) { // Disable
								// all
								// dates
								// after
								// required
								// date
								setDisable(true);
								setStyle("-fx-background-color: #ffc0cb;"); // To
								// set
								// background
								// on
								// different
								// color
							}
						} else {

							if (item.isAfter(localDateTime_limitter.toLocalDate())) { // Disable
								// all
								// dates
								// after
								// required
								// date
								setDisable(true);
								setStyle("-fx-background-color: #ffc0cb;"); // To
								// set
								// background
								// on
								// different
								// color
							}
						}
					}
				};
			}
		};
		return callBack;
	}

	public SelectionModel_Import getSelectionModel_Import() {
		return this.selectionModel_Import;
	}

	public TimeControl start_time() {
		return this.s_time;
	}

	public TimeControl end_time() {
		return this.e_time;
	}

	public void setDateTime(String date, boolean start) {
		if (start) {
			getStart_datePicker().setValue(DateUtils.parseLocalDateFromString(date));
			s_time.setTime(date);
		} else {
			getEnd_datePicker().setValue(DateUtils.parseLocalDateFromString(date));
			e_time.setTime(date);
		}
	}

	public void setStart_datePicker(DatePicker start_datePicker) {
		this.start_datePicker = start_datePicker;
		this.start_datePicker.setConverter(converter);
		this.start_datePicker.getEditor().textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				sprintf("2s_datePicker: " + newValue);
			}
		});
	}

	public void setEnd_datePicker(DatePicker end_datePicker) {
		this.end_datePicker = end_datePicker;
		this.end_datePicker.setConverter(converter);
		this.end_datePicker.getEditor().textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				sprintf("e_datePicker: " + newValue);
			}
		});
	}

	public DatePicker getEnd_datePicker() {
		return end_datePicker;
	}

	public DatePicker getStart_datePicker() {
		return start_datePicker;
	}

	StringConverter<LocalDate> converter = new StringConverter<LocalDate>() {
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(pattern);

		@Override
		public String toString(LocalDate date) {
			if (date != null) {
				return dateFormatter.format(date);
			} else {
				return "";
			}
		}

		@Override
		public LocalDate fromString(String string) {
			if (string != null && !string.isEmpty()) {
				return LocalDate.parse(string, dateFormatter);
			} else {
				return null;
			}
		}
	};

	public LocalDateTime getLocalDateTime(boolean start) {
		if (start) {
			return LocalDateTime.of(start_datePicker.getValue(),
					LocalTime.of(start_time().getHour(), start_time().getMin(), start_time().getSec()));
		}
		return LocalDateTime.of(end_datePicker.getValue(),
				LocalTime.of(end_time().getHour(), end_time().getMin(), end_time().getSec()));
	}

	public void instantiateRenderVisibleNodes() {
		if (renderVisibleNode == null) {
			renderVisibleNode = new RenderVisibleNode(scrollPane, currentFolderPath, connection);
		}
	}

	/**
	 * @return the currentFolderPath
	 */
	public Path getCurrentFolderPath() {
		return currentFolderPath;
	}

	/**
	 * @param currentFolderPath the currentFolderPath to set
	 */
	public void setCurrentFolderPath(Path currentFolderPath) {
		this.currentFolderPath = currentFolderPath;
	}

}
