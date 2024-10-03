/*
 @(#)Copyright:  Copyright (c) 2012-2024 All right reserved.
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.datefixer;

import com.girbola.Main;
import com.girbola.concurrency.ConcurrencyUtils;
import com.girbola.controllers.datefixer.tasks.MakeChanges;
import com.girbola.controllers.datefixer.utils.DateFixGuiUtils;
import com.girbola.controllers.loading.LoadingProcessTask;
import com.girbola.controllers.main.Model_main;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.fileinfo.FileInfo;
import com.girbola.utils.FileInfoUtils;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import common.utils.date.DateUtils;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.util.converter.NumberStringConverter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static com.girbola.Main.bundle;
import static com.girbola.concurrency.ConcurrencyUtils.exec;
import static com.girbola.messages.Messages.*;
import static com.girbola.misc.Misc.getLineNumber;

public class DateTimeAdjusterController {

    private final String ERROR = DateTimeAdjusterController.class.getSimpleName();
    private TilePane df_tilePane;

    //private GridPane df_tilePane;
    private TilePane quickPick_tilePane;
    private Model_main model_main;
    private Model_datefix model_datefix;

    //@formatter:off
    @FXML private DatePicker end_datePicker;
    @FXML private TextField end_hour;
    @FXML private Button copy_startToEnd_btn;
    @FXML private Button selectRange_btn;
    @FXML private Button setDateTimeRange_btn;
    @FXML private Button findExistsPath_btn;
    //@formatter:on

    //@formatter:off
    @FXML private Button end_hour_btn_down;
    @FXML private Button end_hour_btn_up;
    @FXML private TextField end_min;
    @FXML private Button end_min_btn_down;
    @FXML private Button end_min_btn_up;
    @FXML private TextField end_sec;
    @FXML private Button end_sec_btn_down;
    @FXML private Button end_sec_btn_up;
    @FXML private Button set_btn;
    @FXML private DatePicker start_datePicker;
    @FXML private TextField start_hour;
    @FXML private Button start_hour_btn_down;
    @FXML private Button start_hour_btn_up;

    @FXML private TextField start_min;
    @FXML private Button start_min_btn_down;
    @FXML private Button start_min_btn_up;
    @FXML private TextField start_sec;
    @FXML private Button start_sec_btn_down;
    @FXML private Button start_sec_btn_up;

    @FXML private Button selectRangeOfNumbers_btn;

    @FXML private TextField startFromNumber_tf;
    @FXML private TextField endToNumber_tf;


    //@formatter:on


    @FXML
    private void selectRangeOfNumbers_btn_action(ActionEvent event) {
        Messages.sprintf("selectRangeOfNumbers_btn_action");
        int startFrom = Integer.parseInt(startFromNumber_tf.getText().trim());
        int endTo = Integer.parseInt(endToNumber_tf.getText().trim());

        model_datefix.getSelectionModel().clearAll(df_tilePane);
        for (Node node : model_datefix.getTilePane().getChildren()) {
            if (node instanceof VBox imageFrame && node.getId().equals("imageFrame")) {
                int imageFrameImageNumber = DateFixGuiUtils.getImageFrameImageNumber(imageFrame);

                if (imageFrameImageNumber >= 0 && startFrom <= imageFrameImageNumber && endTo >= imageFrameImageNumber) {
                    Messages.sprintf("imageFrameImageNumber: " + imageFrameImageNumber);
                    model_datefix.getSelectionModel().addOnly(imageFrame);
                }
            }
        }
    }

    @FXML
    private void copy_startToEnd_btn_action(ActionEvent event) {
        sprintf("Date to copy_startToEnd: ");
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
        start_datePicker.setValue(end_datePicker.getValue());

        model_datefix.getStart_time().setHour(model_datefix.getEnd_time().getHour());
        model_datefix.getStart_time().setMin(model_datefix.getEnd_time().getMin());
        model_datefix.getStart_time().setSec(model_datefix.getEnd_time().getSec());

    }

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
        for (Node node : model_datefix.getTilePane().getChildren()) {
            if (node instanceof VBox) {
                Messages.sprintf("123 node name " + node.getId());
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
                                            model_datefix.getSelectionModel().addWithToggle(vbox);
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

        Task<Integer> changeDates = new MakeChanges(model_datefix, ldt_start, ldt_end, files);

        LoadingProcessTask lpt = new LoadingProcessTask(Main.scene_Switcher.getWindow());
        changeDates.setOnSucceeded(event -> {
            model_datefix.getSelectionModel().clearAll(df_tilePane);
            lpt.closeStage();
            Messages.sprintf("changeDates were successfully done");
        });
        changeDates.setOnFailed(event -> {
            Messages.sprintf("changeDates failed");
            errorSmth(ERROR, "Task failed", null, getLineNumber(), true);
            lpt.closeStage();
        });
        changeDates.setOnCancelled((event) -> {
            Messages.sprintf("changeDates were cancelled");
            lpt.closeStage();
            errorSmth(ERROR, "Task cancelled", null, getLineNumber(), true);
        });

        lpt.setTask(changeDates);

        Thread changeDates_th = new Thread(changeDates, "changeDates_th");
        changeDates_th.start();

    }

    private boolean checkIfInDateRange(LocalDateTime ldtStart, LocalDateTime ldtEnd, int size) {
        long duration = Duration.between(ldtStart, ldtEnd).getSeconds();

        return duration >= 0 && size > 1 && (size - 1 - duration) >= 0;
    }

    private void setTextProperty(TextField tf) {
        tf.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() <= 1) {
                tf.setText("0" + newValue);
            }
        });
    }

    private int parseTextFieldToInteger(TextField tf) {
        int sec = 0;
        if (tf != null && tf.getText() != null) {
            sec = Integer.parseInt(tf.getText().trim());
        }
        return sec;
    }

    private void setupDatePickers(Model_datefix model_datefix) {
        model_datefix.setStart_datePicker(start_datePicker);
        model_datefix.setEnd_datePicker(end_datePicker);
    }

    private void setupTimeBindings(Model_datefix model_datefix) {
        bindTimeField(start_hour, model_datefix.getStart_time().hour_property());
        bindTimeField(start_min, model_datefix.getStart_time().min_property());
        bindTimeField(start_sec, model_datefix.getStart_time().sec_property());

        bindTimeField(end_hour, model_datefix.getEnd_time().hour_property());
        bindTimeField(end_min, model_datefix.getEnd_time().min_property());
        bindTimeField(end_sec, model_datefix.getEnd_time().sec_property());
    }

    private void bindTimeField(TextField field, Property<Number> property) {
        field.textProperty().bindBidirectional(property, new NumberStringConverter());
        setTextProperty(field);
    }

    private void resetTimeFields() {
        setFieldText(start_hour, "12");
        setFieldText(start_min, "00");
        setFieldText(start_sec, "00");
        setFieldText(end_hour, "12");
        setFieldText(end_min, "00");
        setFieldText(end_sec, "00");
    }

    private void setFieldText(TextField field, String text) {
        field.setText(text);
    }

    public void init(Model_main aModel_main, Model_datefix aModel_datefix, TilePane aTilePane,
                     TilePane aQuickPick_tilePane) {
        this.model_main = aModel_main;
        this.model_datefix = aModel_datefix;
        this.df_tilePane = aTilePane;
        this.quickPick_tilePane = aQuickPick_tilePane;
//        aModel_datefix.setStart_datePicker(start_datePicker);
//        aModel_datefix.setEnd_datePicker(end_datePicker);

        setupDatePickers(aModel_datefix);
        setupTimeBindings(aModel_datefix);
        resetTimeFields();



        endToNumber_tf.setText("" + model_datefix.getFolderInfo_full().getFileInfoList().size());
        startFromNumber_tf.getText().trim();
        endToNumber_tf.getText().trim();

        startFromNumber_tf.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {

                startFromNumber_tf.setText(newValue.replaceAll("[^\\d]", ""));
                //         model_datefix.setStartFromNumber(startFromNumber_tf.getText());
            }
        });
        endToNumber_tf.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                endToNumber_tf.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
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
