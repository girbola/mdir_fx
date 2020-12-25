/*
 @(#)Copyright:  Copyright (c) 2012-2020 All right reserved.
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.datefixer;

import static com.girbola.Main.bundle;
import static com.girbola.concurrency.ConcurrencyUtils.exec;
import static com.girbola.messages.Messages.errorSmth;
import static com.girbola.messages.Messages.sprintf;
import static com.girbola.messages.Messages.warningText;
import static com.girbola.misc.Misc.getLineNumber;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.girbola.Main;
import com.girbola.concurrency.ConcurrencyUtils;
import com.girbola.controllers.loading.LoadingProcess_Task;
import com.girbola.controllers.main.Model_main;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.fileinfo.FileInfo;
import com.girbola.fileinfo.FileInfo_Utils;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;

import common.utils.date.DateUtils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.util.converter.NumberStringConverter;

public class DateTimeAdjusterController {

	private final String ERROR = DateTimeAdjusterController.class.getSimpleName();
	private GridPane df_gridPane;
	private TilePane quickPick_tilePane;
	private Model_main model_main;
	private Model_datefix model_datefix;

	@FXML
	private DatePicker end_datePicker;
	@FXML
	private TextField end_hour;
	@FXML
	private Button copy_startToEnd_btn;
	@FXML
	private Button selectRange_btn;
	@FXML
	private Button setDateTimeRange_btn;
	@FXML
	private Button markFilesAccordingTheDateScale_btn;
	@FXML
	private Button findExistsPath_btn;

	@FXML
	private void findExistsPath_btn_action(ActionEvent event) {
		Messages.sprintf("findExistsPath_btn_action");
		LocalDateTime ldt_start = null;
		LocalDateTime ldt_end = null;

		try {
//			model_datefix.getStart_time().getTime();
//			model_datefix.getEnd_time().getTime();
			ldt_start = model_datefix.getLocalDateTime(true);
			ldt_end = model_datefix.getLocalDateTime(false);
			
		} catch (Exception ex) {
			errorSmth(ERROR, "Cannot get dates", ex, Misc.getLineNumber(), true);
			Main.setProcessCancelled(true);
		}
		List<FileInfo> collectedList = new ArrayList<>();
		if (this.model_main == null) {
			Messages.errorSmth(ERROR, "This is null!", null, Misc.getLineNumber(), true);
		}

		for (FolderInfo folderInfo : model_main.tables().getSortIt_table().getItems()) {
			if (Main.getProcessCancelled()) {
				Messages.sprintf("findFilesAccordingTheDateStale_btn_action cancelled");
				break;
			} /*
				 * ldt_start.isAfter(ldt_min) && ldt_end.isBefore(ldt_max)) 10.isAfter(9) &&
				 * 12.isBefore(11) == true 10.isAfter(9) && 12.isBefore(11) == false
				 */
			for (FileInfo fileInfo : folderInfo.getFileInfoList()) {
				LocalDateTime file_ldt = DateUtils.longToLocalDateTime(fileInfo.getDate());
				if (file_ldt.isAfter(ldt_start.minusDays(1)) && file_ldt.isBefore(ldt_end.plusDays(1))) {
					if (!FileInfo_Utils.findDuplicates(fileInfo, model_datefix.getFolderInfo_full())) {
//						model_ FolderInfo_Utils. addToObservableFileInfoList.
						collectedList.add(fileInfo);
						Messages.sprintf("File name: " + fileInfo.getOrgPath() + " file_ldt: " + file_ldt
								+ "  ldt_start: " + ldt_start + " ldt_end: " + ldt_end);
					}
				}
			}
		}
		

		for (FolderInfo folderInfo : model_main.tables().getSorted_table().getItems()) {
			if (Main.getProcessCancelled()) {
				Messages.sprintf("findFilesAccordingTheDateStale_btn_action cancelled");
				break;
			} /*
				 * ldt_start.isAfter(ldt_min) && ldt_end.isBefore(ldt_max)) 10.isAfter(9) &&
				 * 12.isBefore(11) == true 10.isAfter(9) && 12.isBefore(11) == false
				 */
			for (FileInfo fileInfo : folderInfo.getFileInfoList()) {
				LocalDateTime file_ldt = DateUtils.longToLocalDateTime(fileInfo.getDate());
				if (file_ldt.isAfter(ldt_start) && file_ldt.isBefore(ldt_end)) {
					if (!FileInfo_Utils.findDuplicates(fileInfo, model_datefix.getFolderInfo_full())) {
						collectedList.add(fileInfo);
						Messages.sprintf("File name: " + fileInfo.getOrgPath() + " file_ldt: " + file_ldt
								+ "  ldt_start: " + ldt_start + " ldt_end: " + ldt_end);
					}
				}
			}
			
		}
//		for(FileInfo fileInfo : collectedList) {
//			Node n = createNode(fileInfo);
//		}
//		model_datefix.getFolderInfo_full().getFileInfoList();
//		model_datefix.updateAllInfos(fileInfo_List);  AllInfos(model_datefix.getGridPane());
		
		LoadingProcess_Task loadingProcess_task = new LoadingProcess_Task(Main.scene_Switcher.getWindow());
		Task<ObservableList<Node>> updateGridPane_Task = new UpdateGridPane_Task(model_datefix,
				model_datefix.filterAllNodesList(model_datefix.getAllNodes()), loadingProcess_task);
		loadingProcess_task.setTask(updateGridPane_Task);
		
		Thread thread = new Thread(updateGridPane_Task, "updateGridPane_Task_th");
		thread.start();

		Messages.warningText(
				"Similar files found = " + collectedList.size() + " startdate: " + ldt_start + " end: " + ldt_end);
		for(FileInfo fileInfo : collectedList) {
			Messages.sprintf("fileInfo collected: " + fileInfo.toString() + " fileInfo date: " + DateUtils.longToLocalDateTime(fileInfo.getDate()));
		}

	}

	@FXML
	private void markFilesAccordingTheDateScale_btn_action(ActionEvent event) {
		Messages.sprintf("markFilesAccordingTheDateScale_btn_action");
		LocalDateTime ldt_start = null;
		LocalDateTime ldt_end = null;

		try {
			model_datefix.getStart_time().getTime();
			model_datefix.getEnd_time().getTime();
			ldt_start = model_datefix.getLocalDateTime(true);
			ldt_end = model_datefix.getLocalDateTime(false);
		} catch (Exception ex) {
			errorSmth(ERROR, "Cannot get dates", ex, Misc.getLineNumber(), true);
			Main.setProcessCancelled(true);
		}
		List<FileInfo> collectedList = new ArrayList<>();
		if (this.model_main == null) {
			Messages.errorSmth(ERROR, "This is null!", null, Misc.getLineNumber(), true);
		}

		for (FolderInfo folderInfo : model_main.tables().getSortIt_table().getItems()) {
			if (Main.getProcessCancelled()) {
				Messages.sprintf("findFilesAccordingTheDateStale_btn_action cancelled");
				break;
			} /*
				 * ldt_start.isAfter(ldt_min) && ldt_end.isBefore(ldt_max)) 10.isAfter(9) &&
				 * 12.isBefore(11) == true 10.isAfter(9) && 12.isBefore(11) == false
				 */
			for (FileInfo fileInfo : folderInfo.getFileInfoList()) {
				LocalDateTime file_ldt = DateUtils.longToLocalDateTime(fileInfo.getDate());
				if (file_ldt.isAfter(ldt_start) && file_ldt.isBefore(ldt_end)) {
					if (FileInfo_Utils.findDuplicates(fileInfo, model_datefix.getFolderInfo_full())) {
						collectedList.add(fileInfo);
						Messages.sprintf("File name: " + fileInfo.getOrgPath() + " file_ldt: " + file_ldt
								+ "  ldt_start: " + ldt_start + " ldt_end: " + ldt_end);
					}
				}
			}
		}
		Messages.warningText(
				"Similar files found = " + collectedList.size() + " startdate: " + ldt_start + " end: " + ldt_end);

	}

	@FXML
	private void copy_startToEnd_btn_action(ActionEvent event) {
		sprintf("Date to copy_startToEnd: ");
		Messages.sprintf("Copying start to end: " + start_datePicker.getValue());
		
		start_datePicker.setValue(end_datePicker.getValue());

		model_datefix.getEnd_time().setHour(model_datefix.getStart_time().getHour());
		model_datefix.getEnd_time().setMin(model_datefix.getStart_time().getMin());
		model_datefix.getEnd_time().setSec(model_datefix.getStart_time().getSec());

	}

	@FXML
	private void setDateTimeRange_btn_action(ActionEvent event) {

		if (model_datefix.getSelectionModel().getSelectionList().isEmpty()) {
			warningText(bundle.getString("noSelectedFiles"));
			return;
		}
		exec[ConcurrencyUtils.getExecCounter()].shutdownNow();
		LocalDateTime ldt_start = null;
		LocalDateTime ldt_end = null;

		try {
			model_datefix.getStart_time().getTime();
			model_datefix.getEnd_time().getTime();
			ldt_start = model_datefix.getLocalDateTime(true);
			ldt_end = model_datefix.getLocalDateTime(false);
		} catch (Exception ex) {
			errorSmth(ERROR, "Cannot get dates", ex, Misc.getLineNumber(), true);
		}

		sprintf("ldt_start: " + ldt_start);
		sprintf("ldt_end: " + ldt_end);

		if (model_datefix.getSelectionModel().getSelectionList().size() == 1) {
			makeChanges(ldt_start, ldt_start, model_datefix.getSelectionModel().getSelectionList().size());
		} else if (model_datefix.getSelectionModel().getSelectionList().size() >= 2) {
			if (ldt_start.isEqual(ldt_end)) {
				Dialog<ButtonType> dialog = Messages.ask("", "", bundle.getString("startAndEndDateSame"),
						Misc.getLineNumber());
				ButtonType yes = new ButtonType(bundle.getString("yes"), ButtonBar.ButtonData.YES);
				ButtonType cancel = new ButtonType(bundle.getString("cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
				dialog.getDialogPane().getButtonTypes().addAll(yes, cancel);
				Optional<ButtonType> result = dialog.showAndWait();

				if (result.get().getButtonData().equals(ButtonBar.ButtonData.YES)) {
					makeChanges(ldt_start, ldt_end, model_datefix.getSelectionModel().getSelectionList().size());
				} else if (result.get().getButtonData().equals(ButtonBar.ButtonData.NO)) {
					return;
				}
				// warningText(bundle.getString("startAndEndDateSame"));
				// endDate + end_Time) > (startDate + start_Time
			} else if (ldt_start.isBefore(ldt_end)) {
				sprintf("isBefore");
				if (checkIfInDateRange(ldt_start, ldt_end,
						model_datefix.getSelectionModel().getSelectionList().size())) {
					Duration d = Duration.between(ldt_start, ldt_end);
					warningText(bundle.getString("timeRangeNotValid") + "\nStart date is: "
							+ ldt_start.toString().replace("T", " ") + "\nEnd date is : "
							+ ldt_end.toString().replace("T", " ") + "\nDuration in seconds is: " + d.getSeconds()
							+ "\nDuration should be atleast: "
							+ model_datefix.getSelectionModel().getSelectionList().size());
				} else {
					makeChanges(ldt_start, ldt_end, model_datefix.getSelectionModel().getSelectionList().size());
				}
			} else if (ldt_start.isAfter(ldt_end)) {
				warningText(bundle.getString("startDateLower"));
			}
		}
	}

	@FXML
	private Button copy_endToStart;

	@FXML
	private void copy_endToStart_action(ActionEvent event) {
		Messages.sprintf("Copying end to start: " + end_datePicker.getValue());
		start_datePicker.setValue(end_datePicker.getValue());

		model_datefix.getStart_time().setHour(model_datefix.getEnd_time().getHour());
		model_datefix.getStart_time().setMin(model_datefix.getEnd_time().getMin());
		model_datefix.getStart_time().setSec(model_datefix.getEnd_time().getSec());

	}

	@FXML
	private Button end_hour_btn_down;
	@FXML
	private Button end_hour_btn_up;
	@FXML
	private TextField end_min;
	@FXML
	private Button end_min_btn_down;
	@FXML
	private Button end_min_btn_up;
	@FXML
	private TextField end_sec;
	@FXML
	private Button end_sec_btn_down;
	@FXML
	private Button end_sec_btn_up;

	@FXML
	private Button set_btn;
	@FXML
	private DatePicker start_datePicker;
	@FXML
	private TextField start_hour;
	@FXML
	private Button start_hour_btn_down;
	@FXML
	private Button start_hour_btn_up;

	@FXML
	private TextField start_min;
	@FXML
	private Button start_min_btn_down;
	@FXML
	private Button start_min_btn_up;
	@FXML
	private TextField start_sec;
	@FXML
	private Button start_sec_btn_down;
	@FXML
	private Button start_sec_btn_up;

	@FXML
	private void end_hour_action(ActionEvent event) {
		model_datefix.getEnd_time().setHour(parseTextFieldToInteger(end_hour));
	}

	@FXML
	private void end_hour_btn_down_action(ActionEvent event) {
		model_datefix.getEnd_time().decrease_hour();
	}

	@FXML
	private void end_hour_btn_up_action(ActionEvent event) {
		model_datefix.getEnd_time().increase_hour();
	}

	@FXML
	private void end_min_action(ActionEvent event) {
		model_datefix.getEnd_time().setMin(parseTextFieldToInteger(end_min));
	}

	@FXML
	private void end_min_btn_down(ActionEvent event) {
		model_datefix.getEnd_time().decrease_min();
	}

	@FXML
	private void end_min_btn_up_action(ActionEvent event) {
		model_datefix.getEnd_time().increase_min();
	}

	@FXML
	private void end_sec_action(ActionEvent event) {
		model_datefix.getEnd_time().setSec(parseTextFieldToInteger(end_sec));
	}

	@FXML
	private void end_sec_btn_down_action(ActionEvent event) {
		model_datefix.getEnd_time().decrease_sec();
	}

	@FXML
	private void end_sec_btn_up_action(ActionEvent event) {
		model_datefix.getEnd_time().increase_sec();
	}

	@FXML
	private void start_hour_action(ActionEvent event) {
		model_datefix.getStart_time().setHour(parseTextFieldToInteger(start_hour));
	}

	@FXML
	private void start_hour_btn_up_action(ActionEvent event) {
		model_datefix.getStart_time().increase_hour();
	}

	@FXML
	private void start_hour_btn_down_action(ActionEvent event) {
		model_datefix.getStart_time().decrease_hour();
	}

	@FXML
	private void start_min_action(ActionEvent event) {
		model_datefix.getStart_time().setMin(parseTextFieldToInteger(start_min));
	}

	@FXML
	private void start_min_btn_down_action(ActionEvent event) {
		model_datefix.getStart_time().decrease_min();
	}

	@FXML
	private void start_min_btn_up_action(ActionEvent event) {
		model_datefix.getStart_time().increase_min();
	}

	@FXML
	private void start_sec_action(ActionEvent event) {
		model_datefix.getStart_time().setSec(parseTextFieldToInteger(start_sec));
	}

	@FXML
	private void start_sec_btn_down_action(ActionEvent event) {
		model_datefix.getStart_time().decrease_sec();
	}

	@FXML
	private void start_sec_btn_up_action(ActionEvent event) {
		model_datefix.getStart_time().increase_sec();
	}

	@FXML
	private void selectRange_btn_action(ActionEvent event) {
		Messages.sprintf("selected pressed");
		LocalDateTime ldt_start = null;
		LocalDateTime ldt_end = null;
		try {

			ldt_start = model_datefix.getLocalDateTime(true).minusSeconds(1);
			ldt_end = model_datefix.getLocalDateTime(false).plusSeconds(1);
		} catch (Exception ex) {
			errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
		}
		Messages.sprintf("s: " + ldt_start + " e; " + ldt_end);
		for (Node node : model_datefix.getGridPane().getChildren()) {
			if (node instanceof VBox) {
				Messages.sprintf("node name " + node.getId());
				VBox vbox = (VBox) node;
				if (vbox.getId().equals("imageFrame")) {
					for (Node hbox : vbox.getChildren()) {
						if (hbox instanceof HBox) {
							for (Node tff : ((HBox) hbox).getChildren()) {
								if (tff instanceof TextField) {
									TextField tf = (TextField) tff;
									if (tf != null) {
										LocalDateTime fileDate = DateUtils.stringDateToLocalDateTime(tf.getText());
										Messages.sprintf("fileDate= " + fileDate);
										if (fileDate.isAfter(ldt_start) && fileDate.isBefore(ldt_end)) {
											model_datefix.getSelectionModel().add(vbox);
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	private void makeChanges(LocalDateTime ldt_start, LocalDateTime ldt_end, int files) {

		LocalTimeDifference localTimeDifference = new LocalTimeDifference(ldt_start, ldt_end);
		ArrayList<LocalDateTime> localDateTime_list = localTimeDifference.createDateList_logic(files, ldt_start,
				ldt_end);
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH.mm.ss");

		if (localDateTime_list.isEmpty()) {
			errorSmth(ERROR, "List were empty", null, getLineNumber(), true);
		}
		Main.setChanged(true);
		Task<Integer> changeDates = new Task<Integer>() {
			@Override
			protected Integer call() throws Exception {

				List<String> dateList = new ArrayList<>();
				for (LocalDateTime localDateTime : localDateTime_list) {
					String d_temp = dtf.format(localDateTime);
					dateList.add(d_temp);
					sprintf("=========ldtime: " + d_temp);
				}
				List<Node> list = create_listOfSelectedNodes(df_gridPane);

				if (list.isEmpty()) {
					errorSmth(ERROR, "List were empty", null, getLineNumber(), true);
				}

				Collections.sort(dateList);
				Collections.sort(list, (Node o1, Node o2) -> {
					String value1 = o1.getId().replace("fileDate: ", "");
					if (value1.length() <= 1) {
						value1 = "0" + value1;
						sprintf("Zero added: " + value1);
					}
					String value2 = o2.getId().replace("fileDate: ", "");
					if (value2.length() <= 1) {
						value2 = "0" + value2;
						sprintf("Zero added: " + value2);
					}
					return value1.compareTo(value2);
				});
				for (String dl : dateList) {
					sprintf("DLLIST: " + dl);
				}
				Iterator<Node> it = list.iterator();
				Iterator<String> it2 = dateList.iterator();
				if (list.size() != dateList.size()) {
					sprintf("list size is: " + list.size() + " dateList size is: " + dateList.size());
					errorSmth(ERROR, "List were different", null, getLineNumber(), true);
				}
				while (it.hasNext() && it2.hasNext()) {
					try {
						TextField tf = (TextField) it.next();
						tf.setText(it2.next());
						tf.setStyle(CssStylesController.getStyleModified());
					} catch (Exception ex) {
						errorSmth(ERROR, "Cannot make textfield changes", ex, Misc.getLineNumber(), true);
					}
				}
				return null;
			}
		};
		LoadingProcess_Task lpt = new LoadingProcess_Task(Main.scene_Switcher.getWindow());
		changeDates.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				model_datefix.getSelectionModel().clearAll();
				lpt.closeStage();
			}
		});
		changeDates.setOnFailed(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				errorSmth(ERROR, "Task failed", null, getLineNumber(), true);
				lpt.closeStage();
			}
		});
		changeDates.setOnCancelled(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				lpt.closeStage();
				errorSmth(ERROR, "Task cancelled", null, getLineNumber(), true);
			}
		});
		lpt.setTask(changeDates);
		Thread changeDates_th = new Thread(changeDates, "changeDates_th");
		sprintf("changeDates_th.getName(): " + changeDates_th.getName());
		changeDates_th.run();
	}

	private List<Node> create_listOfSelectedNodes(GridPane df_gridpane) {
		List<Node> list = new ArrayList<>();
		for (Node node_main : model_datefix.getSelectionModel().getSelectionList()) {
			if (node_main instanceof VBox) {
				for (Node n : ((VBox) node_main).getChildren()) {
					if (n instanceof HBox) {
						for (Node hbc : ((HBox) n).getChildren()) {
							if (hbc instanceof TextField) {
								list.add(hbc);
								sprintf("TextField found and it is date: " + ((TextField) hbc).getText()
										+ " getId() is " + hbc.getId());
							}
						}
					}
				}
			}
		}
		return list;
	}

	private boolean checkIfInDateRange(LocalDateTime ldt_start, LocalDateTime ldt_end, int size) {
		Duration duration = Duration.between(ldt_start, ldt_end);
		sprintf("checkIfInDateRange in sec= " + duration.getSeconds());
		if (size > 1) {
			if ((size - 1 - duration.getSeconds()) < 0) {
				return false;
			} else {
				return true;
			}
		} else {
			sprintf("size were below 1= " + size);
			return false;
		}
	}

	private void setNumberTextField(TextField tf) {

		tf.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (newValue.matches("[0-9]*") && newValue.length() < 3 && newValue.length() >= 0) {
					sprintf("is Text: " + newValue);

				} else {
					tf.setText(oldValue);
				}
			}
		});
		tf.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (!newValue) {
					tf.setText(defineFormat(tf));
				}
			}
		});
		tf.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				sprintf("starthour on action");
				model_datefix.getStart_time().setHour(Integer.parseInt(tf.getText()));
				tf.setText("" + defineFormat(tf));
			}
		});
	}

	private void setTextProperty(TextField tf) {
		tf.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				// sprintf("changing: " + newValue);
				if (newValue.length() <= 1) {
					tf.setText("0" + newValue);
				}
			}
		});
	}

	private int parseTextFieldToInteger(TextField tf) {
		int sec = 0;
		try {
			sec = Integer.parseInt(tf.getText());
		} catch (NumberFormatException e) {
			sec = 0;
		}
		return sec;
	}

	public void init(Model_main aModel_main, Model_datefix aModel_datefix, GridPane aDf_gridPane,
			TilePane aQuickPick_tilePane) {
		this.model_main = aModel_main;
		this.model_datefix = aModel_datefix;
		this.df_gridPane = aDf_gridPane;
		this.quickPick_tilePane = aQuickPick_tilePane;
		aModel_datefix.setStart_datePicker(start_datePicker);
		aModel_datefix.setEnd_datePicker(end_datePicker);

		start_hour.textProperty().bindBidirectional(aModel_datefix.getStart_time().hour_property(),
				new NumberStringConverter());
		setTextProperty(start_hour);

		start_min.textProperty().bindBidirectional(aModel_datefix.getStart_time().min_property(),
				new NumberStringConverter());
		setTextProperty(start_min);

		start_sec.textProperty().bindBidirectional(aModel_datefix.getStart_time().sec_property(),
				new NumberStringConverter());
		setTextProperty(start_sec);

		end_hour.textProperty().bindBidirectional(aModel_datefix.getEnd_time().hour_property(),
				new NumberStringConverter());
		setTextProperty(end_hour);

		end_min.textProperty().bindBidirectional(aModel_datefix.getEnd_time().min_property(), new NumberStringConverter());
		setTextProperty(end_min);

		end_sec.textProperty().bindBidirectional(aModel_datefix.getEnd_time().sec_property(), new NumberStringConverter());
		setTextProperty(end_sec);

		start_hour.setText("12");
		start_min.setText("00");
		start_sec.setText("00");
		end_hour.setText("12");
		end_min.setText("00");
		end_sec.setText("00");
	}

	public void centerNodeInScrollPane_(ScrollPane scrollPane, Node node) {
		double h = scrollPane.getContent().getBoundsInLocal().getHeight();
		double y = (node.getBoundsInParent().getMaxY() + node.getBoundsInParent().getMinY()) / 2.0;
		double v = scrollPane.getViewportBounds().getHeight();
		scrollPane.setVvalue(scrollPane.getVmax() * ((y - 0.5 * v) / (h - v)));
	}

	private String defineFormat(TextField tf) {
		int ba = Integer.parseInt(tf.getText());
		if (ba >= 0 && ba <= 9) {
			sprintf("were under 9");
			return ("0" + ba);
		}
		return ("" + ba);
	}

}
