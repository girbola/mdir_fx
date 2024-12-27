package com.girbola.sql;

import com.girbola.controllers.folderscanner.ModelFolderScanner;
import com.girbola.controllers.main.SQL_Enums;
import com.girbola.drive.DriveInfo;
import com.girbola.messages.Messages;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import static com.girbola.sql.SQL_Utils.isDbConnected;

public class DriveInfo_SQL {

    public static final String TABLE_NAME = "DriveInfo";

    final private static String createDriveInfoTable =
            "CREATE TABLE IF NOT EXISTS " +
                    SQL_Enums.DRIVEINFO.getType() +
                    " (" +
                    "drivePath STRING NOT NULL, " +
                    "driveTotalSize INTEGER, " +
                    "identifier STRING, " +
                    "driveSelected STRING," +
                    "driveConnected BOOLEAN)";

    final static String insertDriveInfo =
            "INSERT OR REPLACE INTO " +
                    SQL_Enums.DRIVEINFO.getType() +
                    "('drivePath', " +
                    "'identifier', " +
                    "'totalSize', " +
                    "'connected,' " +
                    "'selected')" +
                    " VALUES(?,?,?,?)";


    /*
     * DriveInfo
     */
    private static boolean createDriveInfoTable(Connection connection) {
        Messages.sprintf("createDriveInfoTable creating..." + connection);
        if (!isDbConnected(connection)) {
            return false;
        }
        try {
            Statement stmt = connection.createStatement();
            stmt.execute(createDriveInfoTable);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }


    public static boolean addDriveInfo_list(Connection connection, List<DriveInfo> driveInfo_list) {
        if (isDbConnected(connection)) {
            return false;
        }
        createDriveInfoTable(connection);

        try {
            PreparedStatement pstmt = connection.prepareStatement(insertDriveInfo);
            for (DriveInfo driveInfo : driveInfo_list) {
                pstmt.setString(1, driveInfo.getDrivePath());
                pstmt.setBoolean(2, driveInfo.isConnected());
                pstmt.setBoolean(3, driveInfo.isSelected());
                pstmt.setLong(4, driveInfo.getDriveTotalSize());
                pstmt.setString(5, driveInfo.getIdentifier());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            if (pstmt != null) {
                pstmt.close();
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // @formatter:on
    public static boolean loadDriveInfo(Connection connection, ModelFolderScanner model_folderScanner) {
        String sql = "SELECT * FROM " + SQL_Enums.DRIVEINFO.getType();
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                String drivePath = rs.getString("drivePath");
                boolean isConnected = rs.getBoolean("driveConnected");
                boolean isSelected = rs.getBoolean("driveSelected");
                long driveTotalSize = rs.getLong("driveTotalSize");
                String identifier = rs.getString("identifier");

                DriveInfo driveInfo = new DriveInfo(drivePath, driveTotalSize, isConnected, isSelected, identifier);
                model_folderScanner.drive().getDrivesList_obs().add(driveInfo);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
