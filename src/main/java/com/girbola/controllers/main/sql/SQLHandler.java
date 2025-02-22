package com.girbola.controllers.main.sql;

import com.girbola.configuration.Configuration;
import com.girbola.drive.DriveInfo;
import com.girbola.messages.Messages;
import com.girbola.sql.DriveInfoSQL;
import com.girbola.sql.SQL_Utils;
import java.util.List;
import lombok.Getter;

import java.sql.Connection;

@Getter
public class SQLHandler {

    private Configuration configuration;
    private Connection configurationConnection;

    private ConfigurationSQLHandler configurationSQLHandler;

//    private DriveInfoHandler driveInfoHandler;

    public SQLHandler(Configuration configuration) {
        Messages.sprintf("SQLHandler init");
        this.configuration = configuration;
        configurationSQLHandler = new ConfigurationSQLHandler();
        ConfigurationSQLHandler.getConnection();

        //TODO Initialize all Configuration tables in here in future
        DriveInfoSQL.createDriveInfoTable();
    }

    public void closeAll() {
        Messages.sprintf("Close all connection at once");
        if(!SQL_Utils.isDbConnected(ConfigurationSQLHandler.getConnection())) {
            SQL_Utils.commitChanges(ConfigurationSQLHandler.getConnection());
            SQL_Utils.closeConnection(ConfigurationSQLHandler.getConnection());
        }
    }

    public List<DriveInfo> getDriveInfoList() {
        return DriveInfoSQL.loadDriveInfos();
    }

    public Connection getConfigurationConnection() {
        return configurationConnection;
    }
}
