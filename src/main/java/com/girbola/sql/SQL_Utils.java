package com.girbola.sql;

import com.girbola.Main;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.messages.Messages;

import java.nio.file.Path;
import java.sql.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SQL_Utils extends FolderInfo_SQL {
    final private static String ERROR = SQL_Utils.class.getSimpleName();

    public static Connection createConfigurationConfig() {
        try {
            return SqliteConnection.connector(Main.conf.getAppDataPath(), Main.conf.getConfiguration_db_fileName());
        } catch (Exception e) {
            Messages.sprintfError("Error connecting to database: " + Main.conf.getConfiguration_db_fileName());
            return null;
        }
    }

    public static boolean clearTable(Connection connection, String table) {
        final String sql = "DROP TABLE " + table;
        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(sql);
            //stmt.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isDbConnected(Connection connection) {
        if (connection == null) {
            return false;
        }
        try {
            return !connection.isClosed();  // Return true if connection is NOT closed
        } catch (SQLException e) {
            Messages.sprintf("There is something wrong with the connection. Error message: " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks if the specified database and table are accessible by executing a basic query.
     *
     * @param conn      The database connection to be tested.
     * @param tableName The name of the table in the database to check accessibility.
     * @return true if the connection and table are accessible, false otherwise.
     */
    public static boolean isDbAccessible(Connection conn, String tableName) {
        try (Statement stmt = conn.createStatement()) {
            // A simple query that is generally safe to run and will work if the DB is accessible
            stmt.execute("SELECT * " + "FROM " + tableName + " " + "LIMIT 1" + ";");
            return true;  // If the query runs without exceptions, the database is accessible
        } catch (SQLException e) {
            return false;  // If an exception occurs, the database is not accessible
        }
    }

    public static boolean closeConnection(Connection connection) {
        Messages.sprintf("About to close connection at: " + getUrl(connection));
        try {
            if (isDbConnected(connection)) {
                connection.close();
                return true;
            }
        } catch (Exception e) {
            Messages.warningText(Main.bundle.getString("cannotCloseConnection") + " " + SQL_Utils.getUrl(connection) + "\n\nError message:" + e.getMessage());
            return false;
        }
        return false;
    }

    /**
     * Commits any pending changes to the database.
     *
     * @param connection The database connection.
     * @return true if the changes are successfully committed, false otherwise.
     */
    public static boolean commitChanges(Connection connection) {
        try {
            connection.commit();
            return true;
        } catch (SQLException e) {
            Messages.warningText(Main.bundle.getString("cannotCommitChanges") + e.getMessage());
            return false;
        }
    }

    public static boolean setAutoCommit(Connection connection, boolean b) {
        try {
            connection.setAutoCommit(false);
            return true;
        } catch (Exception e) {
            Messages.sprintfError("Cannot set connection AutoCommit to false: " + e.getMessage());
            return false;
        }
    }
    // @formatter:on


    public static FolderInfo loadFolderInfoCurrentDir(Path path) {
        Messages.sprintf("loadFolderInfos started: " + path);
        FolderInfo folderInfo = FolderInfo_SQL.loadFolderInfo(path.toString());
        return folderInfo;
    }

    public static void rollBackConnection(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            Messages.sprintfError("Could not rollback: " + e.getMessage());
            Main.setProcessCancelled(true);
        }
    }


    public static String getUrl(Connection connection) {
        if (connection == null) {
            return null;
        }
        try {
            return connection.getMetaData().getURL();
        } catch (SQLException e) {
            try {
                String state = connection.isClosed() ? "closed" : "open";
                Messages.sprintfError("Error getting URL - Connection state: " + state
                        + ", Auto-commit: " + connection.getAutoCommit()
                        + ", Read-only: " + connection.isReadOnly()
                        + ", Error: " + e.getMessage()
                        + ", SQL State: " + e.getSQLState()
                        + ", Error Code: " + e.getErrorCode());
            } catch (SQLException inner) {
                Messages.sprintfError("Failed to get connection details: " + inner.getMessage());
            }
            return null;
        }
    }


    public static void ensureColumnsExist(Connection conn, String tableName, Map<String, String> requiredColumns) throws SQLException {
        boolean committed = false;
        // Get existing columns
        Set<String> existingColumns = new HashSet<>();
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getColumns(null, null, tableName, null)) {
            while (rs.next()) {
                Messages.sprintf("------------Column found: " + rs.getString("COLUMN_NAME") + " of type: " + rs.getString("TYPE_NAME"));
                existingColumns.add(rs.getString("COLUMN_NAME"));
            }
        }

        // Add missing columns
        for (Map.Entry<String, String> entry : requiredColumns.entrySet()) {
            String columnName = entry.getKey();
            String columnType = entry.getValue();

            if (!existingColumns.contains(columnName)) {
                String alterSQL = String.format("ALTER TABLE %s ADD COLUMN %s %s;", tableName, columnName, columnType);
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(alterSQL);
                    conn.commit();
                    Messages.sprintf("Added missing column: " + columnName + " (" + columnType + ")");
                }
            }
        }
    }
}