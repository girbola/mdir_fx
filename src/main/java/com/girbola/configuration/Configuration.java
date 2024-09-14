/*
 @(#)Copyright:  Copyright (c) 2012-2024 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.configuration;

import com.girbola.Main;
import com.girbola.controllers.main.Model_main;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.sql.SQL_Utils;
import com.girbola.sql.SqliteConnection;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;

import static com.girbola.Main.conf;

/**
 * @author Marko Lokka
 */
public class Configuration extends Configuration_defaults {

    private static final String ERROR = Configuration.class.getSimpleName();

    private Model_main model_Main;

    private final String programName = "MDir - Image and Video Organizer";

    protected SimpleDoubleProperty imageViewXProperty = new SimpleDoubleProperty(0);
    protected SimpleDoubleProperty imageViewYProperty = new SimpleDoubleProperty(0);

    public double getImageViewXPosition() {
        return imageViewXProperty.get();
    }

    public void setImageViewXProperty(double value) {
        this.imageViewXProperty.set(value);
    }

    public double getImageViewYPosition() {
        return imageViewYProperty.get();
    }

    public void setImageViewYProperty(double value) {
        this.imageViewYProperty.set(value);
    }

    public String getProgramName() { return programName; }

    public Configuration() { Messages.sprintf("Configuration instantiated..."); }







    public boolean loadConfig_SQL() throws SQLException {
        Messages.sprintf("Loading SQL config: " + Main.conf.getWorkDir());
        Connection connection = SqliteConnection.connector(Main.conf.getAppDataPath(),
                Main.conf.getConfiguration_db_fileName());
        if (SQL_Utils.isDbConnected(connection)) {
            connection.setAutoCommit(false);
            Configuration_SQL_Utils.loadConfiguration(connection, Main.conf);
            Messages.sprintf("Loading stopped. SQL config: " + Main.conf.getWorkDir());
            try {
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        } else {
            System.err.println("Couldn't connect to database");
            return false;
        }

    }

    public void setModel(Model_main model) {
        this.model_Main = model;
    }

    public Model_main getModel() {
        return this.model_Main;
    }

}
