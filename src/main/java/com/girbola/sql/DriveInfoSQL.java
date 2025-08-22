package com.girbola.sql;

import com.girbola.controllers.main.SQLTableEnums;
import com.girbola.controllers.main.sql.ConfigurationSQLHandler;
import com.girbola.controllers.main.sql.TablesSQL;
import com.girbola.drive.DriveInfo;
import com.girbola.messages.Messages;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DriveInfoSQL extends TablesSQL {

    private final static String ERROR = DriveInfoSQL.class.getSimpleName();

    private static Connection connection = null;

    private static final String DRIVE_PATH = "drivePath";
    private static final String DRIVE_CONNECTED = "driveConnected";
    private static final String DRIVE_SELECTED = "driveSelected";
    private static final String DRIVE_TOTAL_SIZE = "driveTotalSize";
    private static final String IDENTIFIER = "identifier";

    private static final String insertDriveInfo = "INSERT OR REPLACE INTO " + SQLTableEnums.DRIVEINFO.getType() + "('" + DRIVE_PATH + "', " + "'" + IDENTIFIER + "', " + "'" + DRIVE_TOTAL_SIZE + "', " + "'" + DRIVE_CONNECTED + "', " + "'" + DRIVE_SELECTED + "')" + " VALUES(?,?,?,?,?)";

    public static boolean addDriveInfos(List<DriveInfo> driveInfos) {
        connection = ConfigurationSQLHandler.getConnection();
        try {
            createDriveInfoTable(); // Ensure the table exists

            try (PreparedStatement pstmt = connection.prepareStatement(insertDriveInfo)) {
                for (DriveInfo driveInfo : driveInfos) {
                    pstmt.setString(1, driveInfo.getDrivePath());
                    pstmt.setString(2, driveInfo.getIdentifier());
                    pstmt.setLong(3, driveInfo.getDriveTotalSize());
                    pstmt.setBoolean(4, driveInfo.isConnected());
                    pstmt.setBoolean(5, driveInfo.isSelected());
                    pstmt.addBatch();
                }

                pstmt.executeBatch(); // Execute batch insert/update
            }

            SQL_Utils.commitChanges(connection); // Commit changes to the database
            return true; // Return success
        } catch (Exception e) {
            Messages.sprintfError("Error adding list of drive infos: " + e.getMessage());
            return false;
        } finally {
            SQL_Utils.closeConnection(connection); // Ensure connection is closed
        }
    }

    public static boolean closeConnection() {
        try {
            SQL_Utils.closeConnection(connection); // Ensure connection is closed
            return true;
        } catch (Exception e) {
            Messages.sprintfError("Error closing connection: " + e.getMessage());
            return false;
        }
    }

    public static boolean createDriveInfoTable() {
        final String driveInfoTableSchema = "CREATE TABLE IF NOT EXISTS " +
                SQLTableEnums.DRIVEINFO.getType()
                + " (" + DRIVE_PATH + " STRING NOT NULL UNIQUE, "
                + DRIVE_TOTAL_SIZE + " INTEGER, "
                + IDENTIFIER + " STRING, "
                + DRIVE_SELECTED + " STRING, "
                + DRIVE_CONNECTED + " BOOLEAN)";

        connection = ConfigurationSQLHandler.getConnection();

        try {
            Statement stmt = connection.createStatement();
            stmt.execute(driveInfoTableSchema);
            stmt.close();
            SQL_Utils.commitChanges(ConfigurationSQLHandler.getConnection());
            //  SQL_Utils.closeConnection(ConfigurationSQLHandler.getConnection());
            return true;
        } catch (Exception e) {
            Messages.sprintfError("Could not create DriveInfo table: " + e.getMessage());
            return false;
        }

    }

    // @formatter:on
    public static List<DriveInfo> loadDriveInfos() {
        connection = ConfigurationSQLHandler.getConnection();
        List<DriveInfo> driveInfos = new ArrayList<>();
        final String selectAll = "SELECT " + DRIVE_PATH + ", " + DRIVE_TOTAL_SIZE + ", " + IDENTIFIER + ", " + DRIVE_SELECTED + ", " + DRIVE_CONNECTED + " FROM " + SQLTableEnums.DRIVEINFO.getType();

        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(selectAll);

            while (rs.next()) {
                // Using constants for column names in ResultSet
                String drivePath = rs.getString(DRIVE_PATH);
                boolean isConnected = rs.getBoolean(DRIVE_CONNECTED);
                boolean isSelected = rs.getBoolean(DRIVE_SELECTED);
                long driveTotalSize = rs.getLong(DRIVE_TOTAL_SIZE);
                String identifier = rs.getString(IDENTIFIER);

                // Creating DriveInfo object
                DriveInfo driveInfo = new DriveInfo(drivePath, driveTotalSize, isConnected, isSelected, identifier);
                driveInfos.add(driveInfo);
            }

            return driveInfos;
        } catch (Exception e) {
            Messages.sprintfError("Failed to load DriveInfo from database");
            return null;
        }
    }

}
