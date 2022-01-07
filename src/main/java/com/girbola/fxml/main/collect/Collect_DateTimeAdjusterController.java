package com.girbola.fxml.main.collect;

import static com.girbola.messages.Messages.errorSmth;
import static com.girbola.messages.Messages.sprintf;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.girbola.Main;
import com.girbola.controllers.main.Model_main;
import com.girbola.controllers.main.Tables;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;

import common.utils.date.DateUtils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.converter.NumberStringConverter;

public class Collect_DateTimeAdjusterController {
	private final String ERROR = Collect_DateTimeAdjusterController.class.getName();

	private Model_main model_main;

	private Model_CollectDialog model_CollectDialog;

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private DatePicker start_datePicker;

	@FXML
	private TextField start_hour;

	@FXML
	private Button start_hour_btn_up;

	@FXML
	private Button start_hour_btn_down;

	@FXML
	private TextField start_min;

	@FXML
	private Button start_min_btn_up;

	@FXML
	private Button start_min_btn_down;

	@FXML
	private TextField start_sec;

	@FXML
	private Button start_sec_btn_up;

	@FXML
	private Button start_sec_btn_down;

	@FXML
	private DatePicker end_datePicker;

	@FXML
	private TextField end_min;

	@FXML
	private Button end_min_btn_up;

	@FXML
	private Button end_min_btn_down;

	@FXML
	private TextField end_sec;

	@FXML
	private Button end_sec_btn_up;

	@FXML
	private Button end_sec_btn_down;

	@FXML
	private TextField end_hour;

	@FXML
	private Button end_hour_btn_up;

	@FXML
	private Button end_hour_btn_down;

	@FXML
	private Button copy_startToEnd_btn;

	@FXML
	private Button copy_endToStart;

	@FXML
	private Button findDateRelatives_btn;

	@FXML
	private void findDateRelatives_btn_action(ActionEvent event) {
		Messages.warningText("Not ready yet");
		Messages.sprintf("findExistsPath_btn_action");
		LocalDateTime ldt_start = null;
		LocalDateTime ldt_end = null;

		try {
//			model_CollectDialog.getStart_time().getTime();
//			model_CollectDialog.getEnd_time().getTime();
			ldt_start = model_CollectDialog.getLocalDateTime(true);
			ldt_end = model_CollectDialog.getLocalDateTime(false);
			if (ldt_start.isAfter(ldt_end)) {
				Messages.warningText("Start date is after startdate!" + ldt_start + " end : " + ldt_end);
				return;
			}
		} catch (Exception ex) {
			errorSmth(ERROR, "Cannot get dates", ex, Misc.getLineNumber(), true);
			Main.setProcessCancelled(true);
		}
		List<FileInfo> collectedList = new ArrayList<>();
		if (this.model_main == null) {
			Messages.errorSmth(ERROR, "This is null!", null, Misc.getLineNumber(), true);
		}

		List<FileInfo> collected_FilesByDateScale_list = getFilesByDateScale(model_main.tables(), ldt_start, ldt_end);
		
		
		model_CollectDialog.obs_Events.clear();
		model_CollectDialog.obs_Location.clear();

		collectFiles(TableType.SORTIT.getType(), model_main.tables().getSortIt_table(), collectedList, ldt_start,
				ldt_end);
		collectFiles(TableType.SORTED.getType(), model_main.tables().getSorted_table(), collectedList, ldt_start,
				ldt_end);

		Messages.warningText(
				"Similar files found = " + collectedList.size() + " startdate: " + ldt_start + " end: " + ldt_end);
		Messages.warningText(
				" collected_FilesByDateScale_list Similar files found = " + collected_FilesByDateScale_list.size() + " startdate: " + ldt_start + " end: " + ldt_end);
		
		
	}

	private List<FileInfo> getFilesByDateScale(Tables tables, LocalDateTime ldt_start, LocalDateTime ldt_end) {

		List<FileInfo> listOfFiles = new ArrayList<>();

		for (FolderInfo folderInfo_Sorted : tables.getSorted_table().getItems()) {
			LocalDateTime start = DateUtils.parseLocalDateTimeFromString(folderInfo_Sorted.getMinDate()).minusDays(1);
			LocalDateTime end = DateUtils.parseLocalDateTimeFromString(folderInfo_Sorted.getMaxDate()).plusDays(1);

			if (folderInfo_Sorted.getBadFiles() != folderInfo_Sorted.getFolderFiles()) {
				if (isDateValid(start)) {
					if (ldt_start.isAfter(start) && ldt_end.isBefore(end)) {
						for (FileInfo fileInfo : folderInfo_Sorted.getFileInfoList()) {
							LocalDateTime f_start = fileInfo.getLocalDateTime();
							if (f_start.minusDays(1).isAfter(start) && f_start.plusDays(1).isBefore(end)) {
								listOfFiles.add(fileInfo);
							}
						}
					}
				}
			}
		}
		return listOfFiles;
	}

	private boolean isDateValid(LocalDateTime start) {

		return false;
	}

	private void collectFiles(String tableType, TableView<FolderInfo> table, List<FileInfo> collectedList,
			LocalDateTime ldt_start, LocalDateTime ldt_end) {
		for (FolderInfo folderInfo : table.getItems()) {
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
					model_CollectDialog.addToEvent(fileInfo, tableType);
					model_CollectDialog.addToLocation(fileInfo);

//					if (FileInfo_Utils.findDuplicates(fileInfo, folderInfo)) {
					collectedList.add(fileInfo);
					Messages.sprintf("File name: " + fileInfo.getOrgPath() + " file_ldt: " + file_ldt + "  ldt_start: "
							+ ldt_start + " ldt_end: " + ldt_end);
//					}

				}
			}
		}

	}

	@FXML
	void copy_endToStart_action(ActionEvent event) {
		start_datePicker.setValue(end_datePicker.getValue());

		model_CollectDialog.getStart_time().setHour(model_CollectDialog.getEnd_time().getHour());
		model_CollectDialog.getStart_time().setMin(model_CollectDialog.getEnd_time().getMin());
		model_CollectDialog.getStart_time().setSec(model_CollectDialog.getEnd_time().getSec());
	}

	@FXML
	void copy_startToEnd_btn_action(ActionEvent event) {
		sprintf("Date to copy_startToEnd: ");
		start_datePicker.setValue(end_datePicker.getValue());

		model_CollectDialog.getEnd_time().setHour(model_CollectDialog.getStart_time().getHour());
		model_CollectDialog.getEnd_time().setMin(model_CollectDialog.getStart_time().getMin());
		model_CollectDialog.getEnd_time().setSec(model_CollectDialog.getStart_time().getSec());
	}

	@FXML
	void end_hour_action(ActionEvent event) {
		model_CollectDialog.getEnd_time().setHour(parseTextFieldToInteger(end_hour));
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

	@FXML
	void end_hour_btn_down_action(ActionEvent event) {
		model_CollectDialog.getEnd_time().decrease_hour();
	}

	@FXML
	void end_hour_btn_up_action(ActionEvent event) {
		model_CollectDialog.getEnd_time().increase_hour();
	}

	@FXML
	void end_min_action(ActionEvent event) {
		model_CollectDialog.getEnd_time().setMin(parseTextFieldToInteger(end_min));
	}

	@FXML
	void end_min_btn_down(ActionEvent event) {
		model_CollectDialog.getEnd_time().decrease_min();
	}

	@FXML
	void end_min_btn_up_action(ActionEvent event) {

	}

	@FXML
	void end_sec_action(ActionEvent event) {

	}

	@FXML
	void end_sec_btn_down_action(ActionEvent event) {
		model_CollectDialog.getEnd_time().decrease_sec();
	}

	@FXML
	void end_sec_btn_up_action(ActionEvent event) {
		model_CollectDialog.getEnd_time().increase_sec();
	}

	@FXML
	void start_hour_action(ActionEvent event) {
		model_CollectDialog.getStart_time().setHour(parseTextFieldToInteger(start_hour));
	}

	@FXML
	void start_hour_btn_down_action(ActionEvent event) {
		model_CollectDialog.getStart_time().decrease_hour();
	}

	@FXML
	void start_hour_btn_up_action(ActionEvent event) {
		model_CollectDialog.getStart_time().increase_hour();
	}

	@FXML
	void start_min_action(ActionEvent event) {
		model_CollectDialog.getStart_time().setMin(parseTextFieldToInteger(start_min));
	}

	@FXML
	void start_min_btn_down_action(ActionEvent event) {
		model_CollectDialog.getStart_time().decrease_min();
	}

	@FXML
	void start_min_btn_up_action(ActionEvent event) {
		model_CollectDialog.getStart_time().increase_min();
	}

	@FXML
	void start_sec_action(ActionEvent event) {
		model_CollectDialog.getStart_time().setSec(parseTextFieldToInteger(start_sec));
	}

	@FXML
	void start_sec_btn_down_action(ActionEvent event) {
		model_CollectDialog.getStart_time().decrease_sec();
	}

	@FXML
	void start_sec_btn_up_action(ActionEvent event) {
		model_CollectDialog.getStart_time().increase_sec();
	}

	@FXML
	void initialize() {
		assert start_datePicker != null
				: "fx:id=\"start_datePicker\" was not injected: check your FXML file 'Collect_DateTimeAdjuster.fxml'.";
		assert start_hour != null
				: "fx:id=\"start_hour\" was not injected: check your FXML file 'Collect_DateTimeAdjuster.fxml'.";
		assert start_hour_btn_up != null
				: "fx:id=\"start_hour_btn_up\" was not injected: check your FXML file 'Collect_DateTimeAdjuster.fxml'.";
		assert start_hour_btn_down != null
				: "fx:id=\"start_hour_btn_down\" was not injected: check your FXML file 'Collect_DateTimeAdjuster.fxml'.";
		assert start_min != null
				: "fx:id=\"start_min\" was not injected: check your FXML file 'Collect_DateTimeAdjuster.fxml'.";
		assert start_min_btn_up != null
				: "fx:id=\"start_min_btn_up\" was not injected: check your FXML file 'Collect_DateTimeAdjuster.fxml'.";
		assert start_min_btn_down != null
				: "fx:id=\"start_min_btn_down\" was not injected: check your FXML file 'Collect_DateTimeAdjuster.fxml'.";
		assert start_sec != null
				: "fx:id=\"start_sec\" was not injected: check your FXML file 'Collect_DateTimeAdjuster.fxml'.";
		assert start_sec_btn_up != null
				: "fx:id=\"start_sec_btn_up\" was not injected: check your FXML file 'Collect_DateTimeAdjuster.fxml'.";
		assert start_sec_btn_down != null
				: "fx:id=\"start_sec_btn_down\" was not injected: check your FXML file 'Collect_DateTimeAdjuster.fxml'.";
		assert end_datePicker != null
				: "fx:id=\"end_datePicker\" was not injected: check your FXML file 'Collect_DateTimeAdjuster.fxml'.";
		assert end_min != null
				: "fx:id=\"end_min\" was not injected: check your FXML file 'Collect_DateTimeAdjuster.fxml'.";
		assert end_min_btn_up != null
				: "fx:id=\"end_min_btn_up\" was not injected: check your FXML file 'Collect_DateTimeAdjuster.fxml'.";
		assert end_min_btn_down != null
				: "fx:id=\"end_min_btn_down\" was not injected: check your FXML file 'Collect_DateTimeAdjuster.fxml'.";
		assert end_sec != null
				: "fx:id=\"end_sec\" was not injected: check your FXML file 'Collect_DateTimeAdjuster.fxml'.";
		assert end_sec_btn_up != null
				: "fx:id=\"end_sec_btn_up\" was not injected: check your FXML file 'Collect_DateTimeAdjuster.fxml'.";
		assert end_sec_btn_down != null
				: "fx:id=\"end_sec_btn_down\" was not injected: check your FXML file 'Collect_DateTimeAdjuster.fxml'.";
		assert end_hour != null
				: "fx:id=\"end_hour\" was not injected: check your FXML file 'Collect_DateTimeAdjuster.fxml'.";
		assert end_hour_btn_up != null
				: "fx:id=\"end_hour_btn_up\" was not injected: check your FXML file 'Collect_DateTimeAdjuster.fxml'.";
		assert end_hour_btn_down != null
				: "fx:id=\"end_hour_btn_down\" was not injected: check your FXML file 'Collect_DateTimeAdjuster.fxml'.";
		assert copy_startToEnd_btn != null
				: "fx:id=\"copy_startToEnd_btn\" was not injected: check your FXML file 'Collect_DateTimeAdjuster.fxml'.";
		assert copy_endToStart != null
				: "fx:id=\"copy_endToStart\" was not injected: check your FXML file 'Collect_DateTimeAdjuster.fxml'.";
		assert findDateRelatives_btn != null
				: "fx:id=\"findDateRelatives_btn\" was not injected: check your FXML file 'Collect_DateTimeAdjuster.fxml'.";
//		start_datePicker.
	}

	private void setTextProperty(TextField tf) {
		tf.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (newValue.length() <= 1) {
					tf.setText("0" + newValue);
				}
			}
		});
	}

	public void init(Model_main aModel_main, Model_CollectDialog aModel_CollectDialog) {
		this.model_main = aModel_main;
		this.model_CollectDialog = aModel_CollectDialog;

		model_CollectDialog.setStart_datePicker(start_datePicker);
		model_CollectDialog.setEnd_datePicker(end_datePicker);

		model_CollectDialog.getStart_datePicker().setValue(LocalDate.of(2020, 7, 28));
		model_CollectDialog.getEnd_datePicker().setValue(LocalDate.now());

		start_hour.textProperty().bindBidirectional(model_CollectDialog.getStart_time().hour_property(),
				new NumberStringConverter());
		setTextProperty(start_hour);

		start_min.textProperty().bindBidirectional(model_CollectDialog.getStart_time().min_property(),
				new NumberStringConverter());
		setTextProperty(start_min);

		start_sec.textProperty().bindBidirectional(model_CollectDialog.getStart_time().sec_property(),
				new NumberStringConverter());
		setTextProperty(start_sec);

		end_hour.textProperty().bindBidirectional(model_CollectDialog.getEnd_time().hour_property(),
				new NumberStringConverter());
		setTextProperty(end_hour);

		end_min.textProperty().bindBidirectional(model_CollectDialog.getEnd_time().min_property(),
				new NumberStringConverter());
		setTextProperty(end_min);

		end_sec.textProperty().bindBidirectional(model_CollectDialog.getEnd_time().sec_property(),
				new NumberStringConverter());
		setTextProperty(end_sec);

		start_hour.setText("12");
		start_min.setText("00");
		start_sec.setText("00");
		end_hour.setText("12");
		end_min.setText("00");
		end_sec.setText("00");
	}
}
