package com.girbola.controllers.datefixer;

import com.girbola.messages.Messages;
import common.utils.date.DateUtils;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.DatePicker;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import static com.girbola.Main.simpleDates;
import static com.girbola.messages.Messages.sprintf;

public class DateFixerModel {
    private TimeControl start_time = new TimeControl();
    private TimeControl end_time = new TimeControl();
    private DatePicker start_datePicker;
    private DatePicker end_datePicker;
    StringConverter<LocalDate> converter = new StringConverter<LocalDate>() {

        @Override
        public String toString(LocalDate date) {
            if (date != null) {
                return simpleDates.getDtf_ymd_minus().format(date);
            } else {
                return "";
            }
        }

        @Override
        public LocalDate fromString(String string) {
            if (string != null && !string.isEmpty()) {
                return LocalDate.parse(string, simpleDates.getDtf_ymd_minus());
            } else {
                return null;
            }
        }
    };


    /**
     * getLocalDateTime read datePicker time and gets time from DateFixer Time
     * chooser
     * <p>
     * If parameter start is true it will read start_datePicker and start time and
     * if it is false it will read end_datePicker and end time. Example: Start
     * date/time 2018/11/08 12:00:00 End date/time 2018/11/09 12:30:00 It will
     * combine there values as a one LocalDateTime
     *
     * @param start
     * @return
     */
    public LocalDateTime getLocalDateTime(boolean start) {
        if (start) {
            return LocalDateTime.of(getStart_datePicker().getValue(),
                    LocalTime.of(getStart_time().getHour(), getStart_time().getMin(), getStart_time().getSec()));
        }

        return LocalDateTime.of(getEnd_datePicker().getValue(),
                LocalTime.of(getEnd_time().getHour(), getEnd_time().getMin(), getEnd_time().getSec()));
    }

    public void setDateTime(String date, boolean start) {
        sprintf("SetDateTime: " + date);
        if (start) {
            Platform.runLater(() -> {
                getStart_datePicker().setValue(DateUtils.parseLocalDateFromString(date));
                getStart_time().setTime(date);
            });
        } else {
            Platform.runLater(() -> {
                getEnd_datePicker().setValue(DateUtils.parseLocalDateFromString(date));
                getEnd_time().setTime(date);
            });
        }
    }

    public DatePicker getStart_datePicker() {
        return start_datePicker;
    }

    public void setStart_datePicker(DatePicker start_datePicker) {
        this.start_datePicker = start_datePicker;
        this.start_datePicker.setConverter(converter);
        this.start_datePicker.getEditor().textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                sprintf("start_datePicker: " + newValue);
            }
        });
    }

    public DatePicker getEnd_datePicker() {
        return end_datePicker;
    }

    public void setEnd_datePicker(DatePicker end_datePicker) {
        this.end_datePicker = end_datePicker;
        this.end_datePicker.setConverter(converter);
        this.end_datePicker.getEditor().textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                sprintf("end_datePicker: " + newValue);
            }
        });
    }

    public TimeControl getStart_time() {
        return start_time;
    }

    public TimeControl getEnd_time() {
        return end_time;
    }

}
