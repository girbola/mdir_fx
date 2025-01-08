package com.girbola.controllers.main.sql;

import com.girbola.configuration.Configuration;
import com.girbola.drive.DriveInfo;
import com.girbola.messages.Messages;
import com.girbola.sql.DriveInfoSQL;
import com.girbola.sql.SQL_Utils;
import java.sql.Connection;
import java.util.List;

public class DriveInfoHandler {

    private Configuration configuration;
    private Connection configurationConnection;

    private List<DriveInfo> driveInfoList;
    private DriveInfoSQL driveInfoSQL;

    public DriveInfoHandler(Connection configurationConnection, Configuration configuration) {
        this.configurationConnection = configurationConnection;
        this.configuration = configuration;
        init();
    }

//    private void handleDriveInfoSQL() {
//        if (driveInfoSQL.load()) {
//            Messages.sprintf("DriveInfoSQL loaded");
//        } else {
//            Messages.sprintf("DriveInfoSQL not loaded");
//        }
//    }

    private void init() {
        driveInfoSQL = new DriveInfoSQL(configurationConnection);
    }

    public boolean loadSQL() {
        if(configurationConnection == null) {
            this.configurationConnection = SQL_Utils.createConfigurationConfig();
        }
        try {
            driveInfoList = driveInfoSQL.load();
            return true;
        } catch (Exception e) {
            Messages.sprintfError("Cannot load driveInfos");
            return false;
        }
    }


    public boolean saveSQL() {
        try {
            driveInfoSQL.save(driveInfoList);
            return true;
        } catch (Exception e) {
            Messages.sprintfError("Cannot load driveInfos");
            return false;
        }
    }
}
