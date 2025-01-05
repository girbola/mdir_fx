package com.girbola.workdir;

import com.girbola.Main;
import com.girbola.controllers.main.SQL_Enums;
import com.girbola.messages.Messages;
import com.girbola.sql.SQL_Utils;
import com.girbola.sql.SqliteConnection;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RegisteredDevicesSQL {

    public void registerDrive(String string, String serial) {
        /*
        C:\\,serial1234
        D:\\,serial1234
        E:\\,serial9999
         */

        if(!SQL_Utils.isDbConnected(workDirConnection)) {
            workDirConnection = SqliteConnection.connector(Paths.get(Main.conf.getWorkDir()), SQL_Enums.WORKDIR.getType());
        }

        try {
            String sql = "INSERT INTO " + SQL_Enums.REGISTEREDDRIVES.getType() + " VALUES (?, ?)";
            PreparedStatement pstmt = workDirConnection.prepareStatement(sql);
            pstmt.setString(1, string);
            pstmt.setString(2, serial);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            Messages.sprintfError("Error registering drive: " + e.getMessage());
        }
    }
}
