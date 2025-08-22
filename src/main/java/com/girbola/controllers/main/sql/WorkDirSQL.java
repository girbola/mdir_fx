package com.girbola.controllers.main.sql;

import com.girbola.Main;
import com.girbola.controllers.main.SQLTableEnums;
import com.girbola.controllers.operate.CopyState;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.sql.SQL_Utils;
import com.girbola.sql.SqliteConnection;
import common.utils.date.DateUtils;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.girbola.Main.simpleDates;

public class WorkDirSQL {

    private static Connection connection;

    public static Connection getConnection() {
        return connection;
    }

    public static Connection createWorkDirConnection() {
        try {
            return SqliteConnection.connector(Main.conf.getAppDataPath(), Main.conf.getWorkDir_db_fileName());
        } catch (Exception e) {
            Messages.sprintfError("Error connecting to database: " + Main.conf.getWorkDir_db_fileName());
            return null;
        }
    }

    public static boolean checkConnection() {
        try {
            // Check if the connection is valid
            if (!SQL_Utils.isDbConnected(connection)) {
                connection = createWorkDirConnection();
                if (connection == null) {
                    Messages.sprintfError("Failed to create new configuration connection");
                    return false;
                }
                SQL_Utils.setAutoCommit(connection, false);
                Messages.sprintf("Configuration database opened: " + SQL_Utils.getUrl(connection));
            }

            // Ensure AutoCommit is disabled
            if (connection.getAutoCommit()) {
                SQL_Utils.setAutoCommit(connection, false);
            }

            return true;
        } catch (SQLException e) {
            Messages.sprintfError("Error checking the database connection: " + e.getMessage());
            return false;
        }
    }


    public static void closeConnection() {
        SQL_Utils.closeConnection(connection);
    }


    public static List<FileInfo> findDuplicatesByDateRange(String startDate, String endDate) {
        List<FileInfo> duplicates = new ArrayList<>();
        String sql = "SELECT * FROM " + SQLTableEnums.WORKDIR.getType() + " WHERE date_range BETWEEN ? AND ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, startDate);
            pstmt.setString(2, endDate);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Map ResultSet to FileInfo objects and add to duplicates list
                    // Implementation depends on your FileInfo class structure
                }
            }
        } catch (SQLException e) {
            Messages.sprintfError("Error finding duplicates: " + e.getMessage());
            // Consider throwing a custom exception or handling the error appropriately
        }

        return duplicates;
    }


    public static void loadWorkDir() {
        checkConnection();
    }

    public static void insertToWorkDir() {

    }

    public static void updateWorkDir() {

    }

    public static void deleteWorkDir() {
        String sql = "DELETE FROM " + SQLTableEnums.WORKDIR.getType();
        try (java.sql.Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
            SQL_Utils.commitChanges(connection);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SQL_Utils.closeConnection(connection);
        }
    }

    public static CopyState findDuplicates(FileInfo fileInfo) {

       findDuplicatesByDateRange(simpleDates.getSdf_ymd_minus().format(fileInfo.getDate()), simpleDates.getSdf_ymd_minus().format(fileInfo.getDate()));

       return CopyState.COPY; // Default state, should be replaced with actual logic to determine the state
    }
}

class FileInfoState extends FileInfo {
    private static String copyState = "state"; // Sets states: Copy, Rename, Duplicated


}
