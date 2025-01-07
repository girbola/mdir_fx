package com.girbola.controllers.main.sql;

import com.girbola.configuration.Configuration;
import com.girbola.messages.Messages;
import com.girbola.sql.DriveInfoSQL;
import com.girbola.sql.SqliteConnection;
import lombok.Getter;

import java.sql.Connection;

@Getter
public class SQLHandler {
    private Configuration configuration;
    private Connection configrationConnection;

    private DriveInfoSQL driveInfoSQL;

    public SQLHandler(Configuration configuration) {
        this.configuration = configuration;
        init();
    }

    private void handleDriveInfoSQL() {
        if (driveInfoSQL.load()) {
            Messages.sprintf("DriveInfoSQL loaded");
        } else {
            Messages.sprintf("DriveInfoSQL not loaded");
        }
    }

    private void init() {
        configrationConnection = SqliteConnection.connector(configuration.getAppDataPath(), configuration.getConfiguration_db_fileName());

        driveInfoSQL = new DriveInfoSQL(configrationConnection);

    }

    public boolean loadSQL() {
        driveInfoSQL.load();
        // plus other SQLs
        return true;
    }


    public void saveSQL() {
        driveInfoSQL.save();

    }

}
