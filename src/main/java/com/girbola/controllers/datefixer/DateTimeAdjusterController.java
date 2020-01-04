/*
 @(#)Copyright:  Copyright (c) 2012-2020 All right reserved.
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.datefixer;

import static com.girbola.messages.Messages.sprintf;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.TilePane;
import javafx.util.converter.NumberStringConverter;

public class DateTimeAdjusterController {

	private final String ERROR = DateTimeAdjusterController.class.getSimpleName();
	private GridPane df_gridPane;
	private TilePane quickPick_tilePane;
	private Model_datefix model_datefix;

	@FXML
	private DatePicker end_datePicker;
	@FXML
	private TextField end_hour;
	@FXML
	private Button copy_startToEnd;

	@FXML
	private void copy_startToEnd_action(ActionEvent event) {
		sprintf("Date to copy_startToEnd: ");
		start_datePicker.setValue(end_datePicker.getValue());

		model_datefix.end_time().setHour(model_datefix.start_time().getHour());
		model_datefix.end_time().setMin(model_datefix.start_time().getMin());
		model_datefix.end_time().setSec(model_datefix.start_time().getSec());

	}

	@FXML
	private Button copy_endToStart;

	@FXML
	private void copy_endToStart_action(ActionEvent event) {
		start_datePicker.setValue(end_datePicker.getValue());

		model_datefix.start_time().setHour(model_datefix.end_time().getHour());
		model_datefix.start_time().setMin(model_datefix.end_time().getMin());
		model_datefix.start_time().setSec(model_datefix.end_time().getSec());

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
		model_datefix.end_time().setHour(parseTextFieldToInteger(end_hour));
	}

	@FXML
	private void end_hour_btn_down_action(ActionEvent event) {
		model_datefix.end_time().decrease_hour();
	}

	@FXML
	private void end_hour_btn_up_action(ActionEvent event) {
		model_datefix.end_time().increase_hour();
	}

	@FXML
	private void end_min_action(ActionEvent event) {
		model_datefix.end_time().setMin(parseTextFieldToInteger(end_min));
	}

	@FXML
	private void end_min_btn_down(ActionEvent event) {
		model_datefix.end_time().decrease_min();
	}

	@FXML
	private void end_min_btn_up_action(ActionEvent event) {
		model_datefix.end_time().increase_min();
	}

	@FXML
	private void end_sec_action(ActionEvent event) {
		model_datefix.end_time().setSec(parseTextFieldToInteger(end_sec));
	}

	@FXML
	private void end_sec_btn_down_action(ActionEvent event) {
		model_datefix.end_time().decrease_sec();
	}

	@FXML
	private void end_sec_btn_up_action(ActionEvent event) {
		model_datefix.end_time().increase_sec();
	}

	@FXML
	private void start_hour_action(ActionEvent event) {
		model_datefix.start_time().setHour(parseTextFieldToInteger(start_hour));
	}

	@FXML
	private void start_hour_btn_up_action(ActionEvent event) {
		model_datefix.start_time().increase_hour();
	}

	@FXML
	private void start_hour_btn_down_action(ActionEvent event) {
		model_datefix.start_time().decrease_hour();
	}

	@FXML
	private void start_min_action(ActionEvent event) {
		model_datefix.start_time().setMin(parseTextFieldToInteger(start_min));
	}

	@FXML
	private void start_min_btn_down_action(ActionEvent event) {
		model_datefix.start_time().decrease_min();
	}

	@FXML
	private void start_min_btn_up_action(ActionEvent event) {
		model_datefix.start_time().increase_min();
	}

	@FXML
	private void start_sec_action(ActionEvent event) {
		model_datefix.start_time().setSec(parseTextFieldToInteger(start_sec));
	}

	@FXML
	private void start_sec_btn_down_action(ActionEvent event) {
		model_datefix.start_time().decrease_sec();
	}

	@FXML
	private void start_sec_btn_up_action(ActionEvent event) {
		model_datefix.start_time().increase_sec();
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
				model_datefix.start_time().setHour(Integer.parseInt(tf.getText()));
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

	public void init(Model_datefix aModel_datefix, GridPane aDf_gridPane, TilePane aQuickPick_tilePane) {
		this.model_datefix = aModel_datefix;
		this.df_gridPane = aDf_gridPane;
		this.quickPick_tilePane = aQuickPick_tilePane;
		aModel_datefix.setStart_datePicker(start_datePicker);
		aModel_datefix.setEnd_datePicker(end_datePicker);

		start_hour.textProperty().bindBidirectional(aModel_datefix.start_time().hour_property(), new NumberStringConverter());
		setTextProperty(start_hour);

		start_min.textProperty().bindBidirectional(aModel_datefix.start_time().min_property(), new NumberStringConverter());
		setTextProperty(start_min);

		start_sec.textProperty().bindBidirectional(aModel_datefix.start_time().sec_property(), new NumberStringConverter());
		setTextProperty(start_sec);

		end_hour.textProperty().bindBidirectional(aModel_datefix.end_time().hour_property(), new NumberStringConverter());
		setTextProperty(end_hour);

		end_min.textProperty().bindBidirectional(aModel_datefix.end_time().min_property(), new NumberStringConverter());
		setTextProperty(end_min);

		end_sec.textProperty().bindBidirectional(aModel_datefix.end_time().sec_property(), new NumberStringConverter());
		setTextProperty(end_sec);

		start_hour.setText("00");
		start_min.setText("00");
		start_sec.setText("00");
		end_hour.setText("00");
		end_min.setText("00");
		end_sec.setText("00");
	}

	public void centerNodeInScrollPane(ScrollPane scrollPane, Node node) {
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
