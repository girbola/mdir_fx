package com.girbola.sql;

import com.girbola.controllers.main.sql.ConfigurationSQLHandler;
import com.girbola.controllers.main.SQL_Enums;
import com.girbola.drive.DriveInfo;
import com.girbola.messages.Messages;

import com.girbola.misc.Misc;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DriveInfoSQL {

    private final static String ERROR = DriveInfoSQL.class.getSimpleName();

    private static Connection connection;

    private static final String DRIVE_PATH = "drivePath";
    private static final String DRIVE_CONNECTED = "driveConnected";
    private static final String DRIVE_SELECTED = "driveSelected";
    private static final String DRIVE_TOTAL_SIZE = "driveTotalSize";
    private static final String IDENTIFIER = "identifier";

    private static final String insertDriveInfo = "INSERT OR REPLACE INTO " + SQL_Enums.DRIVEINFO.getType() + "('" + DRIVE_PATH + "', " + "'" + IDENTIFIER + "', " + "'" + DRIVE_TOTAL_SIZE + "', " + "'" + DRIVE_CONNECTED + "', " + "'" + DRIVE_SELECTED + "')" + " VALUES(?,?,?,?,?)";

    public static boolean addDriveInfo(DriveInfo driveInfo) {
        connection = ConfigurationSQLHandler.getConnection();
        try {
            createDriveInfoTable(); // Ensures the table exists

            try (PreparedStatement pstmt = connection.prepareStatement(insertDriveInfo)) {
                pstmt.setString(1, driveInfo.getDrivePath()); // Set drive path
                pstmt.setString(2, driveInfo.getIdentifier()); // Set identifier
                pstmt.setLong(3, driveInfo.getDriveTotalSize()); // Set total size
                pstmt.setBoolean(4, driveInfo.isConnected()); // Set connected status
                pstmt.setBoolean(5, driveInfo.isSelected()); // Set selected status

                pstmt.executeUpdate(); // Execute the Insert/Replace query
            }

            SQL_Utils.commitChanges(connection); // Commit changes
            return true;
        } catch (Exception e) {
            Messages.sprintfError("Error adding drive info: " + e.getMessage());
            return false;
        } finally {
            SQL_Utils.closeConnection(connection); // Ensure connection is closed
        }
    }

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

    public static boolean addDriveInfos_(List<DriveInfo> driveInfos) {

        connection = ConfigurationSQLHandler.getConnection();
        try {
            boolean driveInfoTable = createDriveInfoTable();
            if (!driveInfoTable) {
                Messages.sprintfError("DriveInfo table was not created!");
                Messages.errorSmth(ERROR, "", null, Misc.getLineNumber(), false);
                return false;
            }
            PreparedStatement pstmt = ConfigurationSQLHandler.getConnection().prepareStatement(insertDriveInfo);
            for (DriveInfo driveInfo : driveInfos) {
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
            SQL_Utils.commitChanges(ConfigurationSQLHandler.getConnection());
            SQL_Utils.closeConnection(ConfigurationSQLHandler.getConnection());

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean createDriveInfoTable() {
        final String driveInfoTableSchema = "CREATE TABLE IF NOT EXISTS " +
                SQL_Enums.DRIVEINFO.getType()
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

    public static void removeDriveInfo(String drivePath) {
        connection = ConfigurationSQLHandler.getConnection(); // Get database connection
        String configTable = SQL_Enums.DRIVEINFO.getType(); // Get table name

        String deleteSQL = "DELETE FROM " + configTable + " WHERE " + DRIVE_PATH + " = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(deleteSQL)) {
            pstmt.setString(1, drivePath); // Set the parameter for drivePath
            pstmt.executeUpdate(); // Execute the DELETE query
            SQL_Utils.commitChanges(connection); // Commit changes to the database
        } catch (SQLException e) {
            Messages.sprintfError("Error removing drive info for path " + drivePath + ": " + e.getMessage());
        } finally {
            SQL_Utils.closeConnection(connection); // Ensure the connection is closed
        }
    }

    private static void ensureAllColumnsExists() throws SQLException {
        connection = ConfigurationSQLHandler.getConnection();
        String configTable = SQL_Enums.DRIVEINFO.getType();
        final String[] columnsSettings = {DRIVE_PATH, DRIVE_CONNECTED, DRIVE_SELECTED, DRIVE_TOTAL_SIZE, IDENTIFIER};

        try (Statement stmt = connection.createStatement()) {
            for (String column : columnsSettings) {
                try {
                    String alterTableSQL = "ALTER TABLE " + configTable + " ADD COLUMN " + column + ";";
                    stmt.executeUpdate(alterTableSQL);
                } catch (SQLException e) {
                    // Column already exists, ignore this error
                    if (!e.getMessage().contains("duplicate column name")) {
                        throw e;
                    }
                }
            }
        }
    }

    // @formatter:on
    public static List<DriveInfo> loadDriveInfos() {
        connection = ConfigurationSQLHandler.getConnection();
        List<DriveInfo> driveInfos = new ArrayList<>();
        final String selectAll = "SELECT " + DRIVE_PATH + ", " + DRIVE_TOTAL_SIZE + ", " + IDENTIFIER + ", " + DRIVE_SELECTED + ", " + DRIVE_CONNECTED + " FROM " + SQL_Enums.DRIVEINFO.getType();

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

    public static synchronized void updateDriveInfo() {
        connection = ConfigurationSQLHandler.getConnection();

        try {
            ensureAllColumnsExists();
            // Additional logic to update the DriveInfo table can go here
            final String updateSQL = "UPDATE " + SQL_Enums.DRIVEINFO.getType() + " SET "
                    + DRIVE_CONNECTED + " = ?, "
                    + DRIVE_SELECTED + " = ? "
                    + "WHERE " + DRIVE_PATH + " = ?";

            try (PreparedStatement pstmt = connection.prepareStatement(updateSQL)) {
                // Example: Updating based on a specific condition
                pstmt.setBoolean(1, false); // Setting DRIVE_CONNECTED to false
                pstmt.setBoolean(2, false); // Setting DRIVE_SELECTED to false
                pstmt.setString(3, "exampleDrivePath"); // Example drive path to identify the row
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            Messages.sprintfError("Updating driveInfo table failed with: " + SQL_Utils.getUrl(connection) + "\n" + e.getMessage());
        } finally {
            SQL_Utils.closeConnection(connection);
        }
    }

}
