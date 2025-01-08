package com.girbola.controllers.main.sql;

import com.girbola.configuration.Configuration;
import com.girbola.drive.DriveInfo;
import com.girbola.messages.Messages;
import com.girbola.sql.DriveInfoSQL;
import com.girbola.sql.SqliteConnection;
import java.util.List;
import lombok.Getter;

import java.sql.Connection;

@Getter
public class SQLHandler {

    private Configuration configuration;
    private Connection configurationConnection;
    private DriveInfoHandler driveInfoHandler;

    public SQLHandler(Configuration configuration) {
        this.configuration = configuration;

        configurationConnection = SqliteConnection.connector(configuration.getAppDataPath(), configuration.getConfiguration_db_fileName());

        driveInfoHandler = new DriveInfoHandler(configurationConnection, configuration);

        try {
            boolean driveInfos = driveInfoHandler.loadSQL();
            if (driveInfos) {
                Messages.sprintf("driveInfos loaded");
            } else {
                Messages.sprintf("DriveInfoSQL not loaded");
            }
        } catch (Exception e) {
            Messages.sprintfError("Cannot initialize SQLHandler");
        }
    }
}
