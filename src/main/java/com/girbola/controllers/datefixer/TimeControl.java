/*
 @(#)Copyright:  Copyright (c) 2012-2024 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */

package com.girbola.controllers.datefixer;

import common.utils.date.DateUtils;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.time.LocalTime;

/**
 *
 * @author Marko Lokka
 */

public class TimeControl {

    private IntegerProperty hour = new SimpleIntegerProperty(0);
    private IntegerProperty min = new SimpleIntegerProperty(0);
    private IntegerProperty sec = new SimpleIntegerProperty(0);

    public void setTime(int hour, int min, int sec) {
        this.hour.set(hour);
        this.min.set(min);
        this.sec.set(sec);
    }

    public void setTime(String date) {
        LocalTime lt = DateUtils.parseLocalTimeFromString(date);
        setTime(lt.getHour(), lt.getMinute(), lt.getSecond());
    }

    /**
     * Return time 00:00:00
     * @return
     */
    public String getTime() {
        return "" + hour.get() + ":" + min.get() + ":" + sec.get();
    }

    public IntegerProperty hour_property() {
        return this.hour;
    }

    public IntegerProperty min_property() {
        return this.min;
    }

    public IntegerProperty sec_property() {
        return this.sec;
    }

    public void setHour(int value) {
        this.hour.set(value);
    }

    public void setMin(int value) {
        this.min.set(value);
    }

    public void setSec(int value) {
        this.sec.set(value);
    }

    public void increase_hour() {
        updateTime(hour, 0, 23, true);
//        if ((this.hour.get() + 1) == 24) {
//            this.hour.set(0);
//        } else {
//            this.hour.set(this.hour.get() + 1);
//        }
    }

    public void increase_min() {
        updateTime(min, 0, 59, true);
//        if ((this.min.get() + 1) == 61) {
//            this.min.set(0);
//        } else {
//            this.min.set(this.min.get() + 1);
//        }
    }

    public void increase_sec() {
        updateTime(sec, 0, 59, true);
    }

    private void updateTime(IntegerProperty current, int min, int max, boolean plus) {
        /*
        hour    timeUpdate(3,  0, 23,  true)
        minute  timeUpdate(21, 0, 59, true)
        seconds timeUpdate(21, 0, 59, true)
       
        hour    timeUpdate(3,  0, 23, false)
        minute  timeUpdate(21, 0, 59, false)
        seconds timeUpdate(21, 0, 59, false)
         */
        if (plus) {
            if ((current.get() + 1) > max) {
                current.set(min);
            } else {
                current.set(current.get() + 1);
            }
        } else {
            if ((current.get() - 1) < min) {
                current.set(max);
            } else {
                current.set(current.get() - 1);
            }
        }
    }

    public void decrease_hour() {
        updateTime(hour, 0, 23, false);
    }

    public void decrease_min() {
        updateTime(min, 0, 59, false);
    }

    public void decrease_sec() {
        updateTime(sec, 0, 59, false);
    }

    public int getHour() {
        return hour.get();
    }

    public int getMin() {
        return min.get();
    }

    public int getSec() {
        return sec.get();
    }

}