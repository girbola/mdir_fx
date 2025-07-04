
package com.girbola.configuration;

import com.girbola.Main;
import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.sql.ConfigurationSQLHandler;
import com.girbola.messages.Messages;
import com.girbola.sql.SQL_Utils;
import com.girbola.sql.SqliteConnection;

import java.sql.Connection;
import java.sql.SQLException;


public class Configuration extends Configuration_defaults {

    private static final String ERROR = Configuration.class.getSimpleName();

    private ModelMain model_Main;

    private final String programName = "MDir - Image and Video Organizer";


    public String getProgramName() { return programName; }

    public Configuration() { Messages.sprintf("Configuration instantiated..."); }

    public boolean loadConfig_SQL() throws SQLException {
        Messages.sprintf("Loading SQL config: " + Main.conf.getAppDataPath(), Main.conf.getConfiguration_db_fileName());
        Connection connection = SqliteConnection.connector(Main.conf.getAppDataPath(), Main.conf.getConfiguration_db_fileName());

        if (SQL_Utils.isDbConnected(connection)) {
            connection.setAutoCommit(false);
            ConfigurationSQLHandler.loadConfiguration(Main.conf);
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

    public void setModel(ModelMain model) {
        this.model_Main = model;
    }

    public ModelMain getModel() {
        return this.model_Main;
    }

}
