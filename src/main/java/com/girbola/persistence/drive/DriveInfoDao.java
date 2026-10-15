package com.girbola.persistence.drive;

import com.girbola.controllers.main.SQLTableEnums;
import com.girbola.controllers.main.sql.ConfigurationSQLHandler;
import com.girbola.controllers.main.sql.TablesSQL;
import com.girbola.drive.DriveInfo;
import com.girbola.messages.Messages;
import com.girbola.sql.SQL_Utils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DriveInfoDao extends TablesSQL {

    private final static String ERROR = DriveInfoDao.class.getSimpleName();

    private static Connection connection = null;

    private static final String DRIVE_PATH = "drivePath";
    private static final String DRIVE_CONNECTED = "driveConnected";
    private static final String DRIVE_SELECTED = "driveSelected";
    private static final String DRIVE_TOTAL_SIZE = "driveTotalSize";
    private static final String IDENTIFIER = "identifier";
    private static final String SERIAL = "serial";
    private static final String PARTITION_UUID = "partitionUuid";
    private static final String PARTITION_IDENTIFICATION = "partitionIdentification";
    private static final String PARTITION_MOUNT_POINT = "partitionMountPoint";

    private static final String insertDriveInfo = """
            INSERT OR REPLACE INTO %s (
                %s,
                %s,
                %s,
                %s,
                %s,
                %s,
                %s,
                %s,
                %s
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.formatted(
            SQLTableEnums.DRIVEINFO.getType(),
            DRIVE_PATH,
            IDENTIFIER,
            DRIVE_TOTAL_SIZE,
            DRIVE_CONNECTED,
            DRIVE_SELECTED,
            SERIAL,
            PARTITION_UUID,
            PARTITION_IDENTIFICATION,
            PARTITION_MOUNT_POINT
    );


    final static String driveInfoTableSchema = """
            CREATE TABLE IF NOT EXISTS %s (
                %s TEXT NOT NULL UNIQUE,
                %s INTEGER,
                %s TEXT,
                %s TEXT,
                %s BOOLEAN,
                %s TEXT,
                %s TEXT,
                %s TEXT,
                %s TEXT
            )
            """.formatted(
            SQLTableEnums.DRIVEINFO.getType(),
            DRIVE_PATH,
            DRIVE_TOTAL_SIZE,
            IDENTIFIER,
            DRIVE_SELECTED,
            DRIVE_CONNECTED,
            SERIAL,
            PARTITION_UUID,
            PARTITION_IDENTIFICATION,
            PARTITION_MOUNT_POINT
    );

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
                    pstmt.setString(6, driveInfo.getSerial());
                    pstmt.setString(7, driveInfo.getPartitionUuid());
                    pstmt.setString(8, driveInfo.getPartitionIdentification());
                    pstmt.setString(9, driveInfo.getPartitionMountPoint());
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
        connection = ConfigurationSQLHandler.getConnection();

        try {
            Statement stmt = connection.createStatement();
            stmt.execute(driveInfoTableSchema);
            stmt.close();

            Map<String, String> requiredColumns = new LinkedHashMap<>();
            requiredColumns.put(SERIAL, "TEXT");
            requiredColumns.put(PARTITION_UUID, "TEXT");
            requiredColumns.put(PARTITION_IDENTIFICATION, "TEXT");
            requiredColumns.put(PARTITION_MOUNT_POINT, "TEXT");
            SQL_Utils.ensureColumnsExist(connection, SQLTableEnums.DRIVEINFO.getType(), requiredColumns);

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
        createDriveInfoTable();
        List<DriveInfo> driveInfos = new ArrayList<>();
        final String selectAll = "SELECT * FROM " + SQLTableEnums.DRIVEINFO.getType();

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

                driveInfo.setSerial(rs.getString(SERIAL));
                driveInfo.setPartitionUuid(rs.getString(PARTITION_UUID));
                driveInfo.setPartitionIdentification(rs.getString(PARTITION_IDENTIFICATION));
                driveInfo.setPartitionMountPoint(rs.getString(PARTITION_MOUNT_POINT));
                driveInfos.add(driveInfo);
            }

            return driveInfos;
        } catch (Exception e) {
            Messages.sprintfError("Failed to load DriveInfo from database");
            return null;
        }
    }

}
