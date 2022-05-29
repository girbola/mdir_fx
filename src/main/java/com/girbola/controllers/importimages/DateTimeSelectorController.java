/*
 @(#)Copyright:  Copyright (c) 2012-2020 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.importimages;

import static com.girbola.messages.Messages.errorSmth;
import static com.girbola.messages.Messages.sprintf;

import java.time.LocalDateTime;
import java.util.ArrayList;

import com.girbola.Main;
import com.girbola.fileinfo.FileInfo;
import com.girbola.misc.Misc;
import com.girbola.workdir.WorkDirHandler;

import common.utils.date.DateUtils;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.util.converter.NumberStringConverter;

/**
 * FXML Controller class
 *
 * @author Marko Lokka
 */
public class DateTimeSelectorController {

	private final String ERROR = DateTimeSelectorController.class.getSimpleName();
	@FXML
	private GridPane selector_gridPane;
	@FXML
	private ToggleButton pick_endDateTime;
	@FXML
	private ToggleButton pick_startDateTime;
	@FXML
	private Label selectedFiles_lbl;

	boolean datePicker_start;
	private Model_importImages model_ImportImages;

	@FXML
	private DatePicker end_datePicker;
	@FXML
	private TextField end_hour;
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
	private Button select_btn;
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
		model_ImportImages.end_time().setHour(parseTextFieldToInteger(end_hour));
	}

	@FXML
	private void end_hour_btn_down_action(ActionEvent event) {
		model_ImportImages.end_time().decrease_hour();
	}

	@FXML
	private void end_hour_btn_up_action(ActionEvent event) {
		model_ImportImages.end_time().increase_hour();
	}

	@FXML
	private void end_min_action(ActionEvent event) {
		model_ImportImages.end_time().setMin(parseTextFieldToInteger(end_min));
	}

	@FXML
	private void end_min_btn_down(ActionEvent event) {
		model_ImportImages.end_time().decrease_min();
	}

	@FXML
	private void end_min_btn_up_action(ActionEvent event) {
		model_ImportImages.end_time().increase_min();
	}

	@FXML
	private void end_sec_action(ActionEvent event) {
		model_ImportImages.end_time().setSec(parseTextFieldToInteger(end_sec));
	}

	@FXML
	private void end_sec_btn_down_action(ActionEvent event) {
		model_ImportImages.end_time().decrease_sec();
	}

	@FXML
	private void end_sec_btn_up_action(ActionEvent event) {
		model_ImportImages.end_time().increase_sec();
	}

	@FXML
	private void select_btn_action(ActionEvent event) {
		LocalDateTime ldt_start = null;
		LocalDateTime ldt_end = null;

		try {
			model_ImportImages.start_time().getTime();
			model_ImportImages.end_time().getTime();

			ldt_start = model_ImportImages.getLocalDateTime(true).minusSeconds(1);
			ldt_end = model_ImportImages.getLocalDateTime(false).plusSeconds(1);
		} catch (Exception ex) {
			errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
		}

		sprintf("Start date and time:" + ldt_start);
		sprintf("End date and time: " + ldt_end);
		model_ImportImages.getSelectionModel_Import().clearAll();
		Parent vbox = select_btn.getScene().getRoot();
		ScrollPane sp = (ScrollPane) vbox.lookup("#scrollPane");
		Node sp_node = sp.getContent();
		if (sp_node instanceof VBox) {
			for (Node scrollPane_node : ((VBox) sp_node).getChildren()) {
				sprintf("scrollPane_node= " + scrollPane_node);
				if (scrollPane_node instanceof TitledPane) {
					TitledPane titledPane = (TitledPane) scrollPane_node;
					Node tp_node = titledPane.getContent();
					if (tp_node instanceof TilePane) {
						for (Node titledPane_node : ((TilePane) tp_node).getChildren()) {
							if (titledPane_node instanceof StackPane) {
								StackPane stackPane = (StackPane) titledPane_node;
								FileInfo fig = (FileInfo) stackPane.getUserData();
								LocalDateTime date = DateUtils.longToLocalDateTime(fig.getDate());

								if (date.isAfter(ldt_start) && date.isBefore(ldt_end)) {
									model_ImportImages.getSelectionModel_Import().add(titledPane_node);
								}
							}
						}
					}
				}
			}
		}
	}

	public static ArrayList<Node> getAllNodes(Parent root) {
		ArrayList<Node> nodes = new ArrayList<Node>();
		addAllDescendents(root, nodes);
		return nodes;
	}

	private static void addAllDescendents(Parent parent, ArrayList<Node> nodes) {
		for (Node node : parent.getChildrenUnmodifiable()) {
			nodes.add(node);
			if (node instanceof Parent) {
				addAllDescendents((Parent) node, nodes);
			}
		}
	}

	@FXML
	private void start_hour_action(ActionEvent event) {
		model_ImportImages.start_time().setHour(parseTextFieldToInteger(start_hour));
	}

	@FXML
	private void start_hour_btn_up_action(ActionEvent event) {
		model_ImportImages.start_time().increase_hour();
	}

	@FXML
	private void start_hour_btn_down_action(ActionEvent event) {
		model_ImportImages.start_time().decrease_hour();
	}

	@FXML
	private void start_min_action(ActionEvent event) {
		model_ImportImages.start_time().setMin(parseTextFieldToInteger(start_min));
	}

	@FXML
	private void start_min_btn_down_action(ActionEvent event) {
		model_ImportImages.start_time().decrease_min();
	}

	@FXML
	private void start_min_btn_up_action(ActionEvent event) {
		model_ImportImages.start_time().increase_min();
	}

	@FXML
	private void start_sec_action(ActionEvent event) {
		model_ImportImages.start_time().setSec(parseTextFieldToInteger(start_sec));
	}

	@FXML
	private void start_sec_btn_down_action(ActionEvent event) {
		model_ImportImages.start_time().decrease_sec();
	}

	@FXML
	private void start_sec_btn_up_action(ActionEvent event) {
		model_ImportImages.start_time().increase_sec();
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

	public void init(Model_importImages aModel_importImages) {
		sprintf("initing " + ERROR);
		this.model_ImportImages = aModel_importImages;

		aModel_importImages.setStart_datePicker(start_datePicker);
		aModel_importImages.setEnd_datePicker(end_datePicker);

		start_hour.textProperty().bindBidirectional(aModel_importImages.start_time().hour_property(), new NumberStringConverter());
		setTextProperty(start_hour);

		start_min.textProperty().bindBidirectional(aModel_importImages.start_time().min_property(), new NumberStringConverter());
		setTextProperty(start_min);

		start_sec.textProperty().bindBidirectional(aModel_importImages.start_time().sec_property(), new NumberStringConverter());
		setTextProperty(start_sec);

		end_hour.textProperty().bindBidirectional(aModel_importImages.end_time().hour_property(), new NumberStringConverter());
		setTextProperty(end_hour);

		end_min.textProperty().bindBidirectional(aModel_importImages.end_time().min_property(), new NumberStringConverter());
		setTextProperty(end_min);

		end_sec.textProperty().bindBidirectional(aModel_importImages.end_time().sec_property(), new NumberStringConverter());
		setTextProperty(end_sec);

		aModel_importImages.start_time().setHour(model_ImportImages.getMin_ldf().getHour());
		aModel_importImages.start_time().setMin(model_ImportImages.getMin_ldf().getMinute());
		aModel_importImages.start_time().setSec(model_ImportImages.getMin_ldf().getSecond());

		aModel_importImages.end_time().setHour(model_ImportImages.getMax_ldf().getHour());
		aModel_importImages.end_time().setMin(model_ImportImages.getMax_ldf().getMinute());
		aModel_importImages.end_time().setSec(model_ImportImages.getMax_ldf().getSecond());

		end_datePicker.setDayCellFactory(model_ImportImages.dateCellFactory(model_ImportImages.getMax_ldf(), false));
		start_datePicker.setDayCellFactory(model_ImportImages.dateCellFactory(model_ImportImages.getMin_ldf(), true));

		end_datePicker.setValue(model_ImportImages.getMax_ldf().toLocalDate());
		start_datePicker.setValue(model_ImportImages.getMin_ldf().toLocalDate());

		pick_startDateTime.selectedProperty().bindBidirectional(model_ImportImages.getToggleButtonControl().start_toggled_property());
		pick_startDateTime.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue == true) {
					disableDateTime();
				} else {
					enableDateTime();
				}
			}
		});
		
		pick_endDateTime.selectedProperty().bindBidirectional(model_ImportImages.getToggleButtonControl().end_toggled_property());
		pick_endDateTime.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue == true) {
					disableDateTime();
				} else {
					enableDateTime();
				}
			}
		});
//		srtb;
//		WorkDir_Handler workDir_handler = new WorkDir_Handler();
//		workDir_handler.load_All_WorkDirSub(Main.conf.getWorkDir(), start_year, end_year);
		

	}

	private void disableDateTime() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				end_datePicker.setDisable(true);
				end_hour.setDisable(true);
				end_hour_btn_down.setDisable(true);
				end_hour_btn_up.setDisable(true);
				end_min.setDisable(true);
				end_min_btn_down.setDisable(true);
				end_min_btn_up.setDisable(true);
				end_sec.setDisable(true);
				end_sec_btn_down.setDisable(true);
				end_sec_btn_up.setDisable(true);
				select_btn.setDisable(true);
				start_datePicker.setDisable(true);
				start_hour.setDisable(true);
				start_hour_btn_down.setDisable(true);
				start_hour_btn_up.setDisable(true);
				start_min.setDisable(true);
				start_min_btn_down.setDisable(true);
				start_min_btn_up.setDisable(true);
				start_sec.setDisable(true);
				start_sec_btn_down.setDisable(true);
				start_sec_btn_up.setDisable(true);
			}
		});

	}

	private void enableDateTime() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				end_datePicker.setDisable(false);
				end_hour.setDisable(false);
				end_hour_btn_down.setDisable(false);
				end_hour_btn_up.setDisable(false);
				end_min.setDisable(false);
				end_min_btn_down.setDisable(false);
				end_min_btn_up.setDisable(false);
				end_sec.setDisable(false);
				end_sec_btn_down.setDisable(false);
				end_sec_btn_up.setDisable(false);
				select_btn.setDisable(false);
				start_datePicker.setDisable(false);
				start_hour.setDisable(false);
				start_hour_btn_down.setDisable(false);
				start_hour_btn_up.setDisable(false);
				start_min.setDisable(false);
				start_min_btn_down.setDisable(false);
				start_min_btn_up.setDisable(false);
				start_sec.setDisable(false);
				start_sec_btn_down.setDisable(false);
				start_sec_btn_up.setDisable(false);
			}
		});

	}

}
