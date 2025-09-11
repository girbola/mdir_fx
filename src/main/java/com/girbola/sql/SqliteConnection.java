package com.girbola.sql;

import com.girbola.messages.Messages;
import java.util.Objects;
import lombok.Getter;

import java.io.File;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SqliteConnection {

    @Getter
    private static List<Connection> connectionList = new ArrayList<>();

    public static synchronized void closeAllConnections() {
        Iterator<Connection> iterator = connectionList.iterator();
        while (iterator.hasNext()) {
            Connection conn = iterator.next();
            if (conn == null) {
                iterator.remove();
                continue;
            }

            try {
                String url = null;
                try {
                    // Try to get URL before potentially closing the connection
                    if (!conn.isClosed()) {
                        url = conn.getMetaData().getURL();
                    }
                } catch (SQLException e) {
                    // Ignore metadata access errors
                }

                // Close the connection if it's not already closed
                if (conn != null && !conn.isClosed()) {
                    try {
                        SQL_Utils.setAutoCommit(conn, false);
                        SQL_Utils.commitChanges(conn);
                        conn.close();
                        if (url != null) {
                            Messages.sprintf("Successfully closed connection: " + url);
                        } else {
                            Messages.sprintf("Successfully closed connection");
                        }
                    } catch (SQLException e) {
                        Messages.sprintfError("Error during connection closure: " + e.getMessage());
                    }
                }
            } catch (SQLException e) {
                Messages.sprintfError("Error checking connection state: " + e.getMessage());
            } finally {
                iterator.remove();
            }
        }
    }

    public static synchronized void addConnection(Connection conn) {
        // Validate input connection early
        if (conn == null) {
            Messages.sprintfError("addConnection called with null connection");
            return;
        }
        try {
            if (conn.isClosed()) {
                Messages.sprintfError("addConnection called with a closed connection");
                return;
            }
        } catch (SQLException e) {
            Messages.sprintfError("Cannot verify connection state: " + e.getMessage());
            return;
        }

        // Cache URL of the incoming connection (may be null for some drivers)
        String newUrl = safeGetUrl(conn);

        // Clean list and detect duplicates using an iterator
        Iterator<Connection> it = connectionList.iterator();
        while (it.hasNext()) {
            Connection c = it.next();

            if (c == null) {
                it.remove();
                continue;
            }

            try {
                if (c.isClosed()) {
                    it.remove();
                    continue;
                }
            } catch (SQLException e) {
                // If we cannot determine state, err on the side of removing it
                it.remove();
                continue;
            }

            // Compare target database identity safely
            String existingUrl = safeGetUrl(c);
            if (Objects.equals(existingUrl, newUrl)) {
                // Already tracked
                return;
            }
        }

        // Add the new connection
        if (newUrl != null) {
            Messages.sprintf("Adding connection database: " + newUrl);
        } else {
            Messages.sprintf("Adding connection database");
        }
        connectionList.add(conn);
    }

    private static String safeGetUrl(Connection c) {
        if (c == null) return null;
        try {
            DatabaseMetaData md = c.getMetaData();
            return (md != null) ? md.getURL() : null;
        } catch (SQLException ignored) {
            return null;
        }
    }

    public static void removeConnection(Connection conn) {
        connectionList.remove(conn);
    }

    public static Connection connector(Path path, String tableName) {
        Messages.sprintf("Connection to path: " + path.toFile().getAbsolutePath() + " tableName: " + tableName);

        // Fix: do not use path.startsWith("") which is always true for Path and incorrectly returns null
        if (path == null || tableName == null || tableName.trim().isEmpty()) {
            Messages.sprintf("Invalid database path or tableName. path=" + path + " tableName=" + tableName);
            return null;
        }

        Connection conn = SqliteConnection.hasDatabase(path.toString() + File.separator + tableName);
        if (conn == null) {
            try {
                Class.forName("org.sqlite.JDBC");
                conn = DriverManager.getConnection("jdbc:sqlite:" + path.toString() + File.separator + tableName);
                Messages.sprintf("Opening SQLite connection: " + conn.getMetaData().getURL());

                addConnection(conn);
                showConnections(connectionList);
                return conn;
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Something went wrong while connecting SQLITE database.\n" + e.getMessage());
                return null;
            }
        }
        return conn;
    }


    private static Connection hasDatabase(String databasePath) {
        for (Connection conn : connectionList) {
            try {
                Messages.sprintf("Listing connected databases: " + conn.getMetaData().getURL());
                if (conn.getMetaData().getURL().contains(databasePath)) {
                    Messages.sprintf("FOUND Connection to database: " + databasePath + " already exists!");
                    return conn;
                }
            } catch (SQLException e) {
                return null;
            }
        }
        return null;
    }

    private static void showConnections(List<Connection> connectionList) {
        Messages.sprintf("Database ConnectionList size is: " + connectionList.size());
        Iterator<Connection> iterator = connectionList.iterator();
        while (iterator.hasNext()) {
            Connection conn = iterator.next();
            try {
                if (conn != null && !conn.isClosed()) {
                    Messages.sprintf("SQLite database connection is: " + conn.getMetaData().getURL());
                } else {
                    iterator.remove(); // Remove closed connections from the list
                }
            } catch (SQLException e) {
                iterator.remove(); // Remove problematic connections
                Messages.sprintf("Error accessing connection: " + e.getMessage());
            }
        }
    }


    public static Connection connector(String path, String tableName) {
        Connection conn = null;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + path + File.separator + tableName);
            return conn;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Something went wrong while connecting SQLITE database.\n" + e.getMessage());
            return null;
        }
    }

    /**
     * Check if table is not empty
     *
     * @param connection
     * @param tableName
     * @return
     */
    public static boolean tableExists(Connection connection, String tableName) {
        Messages.sprintf("tableExists() tableName: " + tableName);
        try {
            if (connection == null) {
                return false;
            }
            if (!SQL_Utils.isDbConnected(connection)) {
                Messages.sprintfError("Database connection is not valid!");
                return false;
            }
            DatabaseMetaData md = connection.getMetaData();
            ResultSet rs = md.getTables(null, null, tableName, null);
            boolean exists = rs.next(); // Checks if there's at least one row
            if (!exists) {
                Messages.sprintf("Table does not exist: " + tableName);
            }
            rs.close(); // Always close the ResultSet to avoid resource leaks
            return exists;
        } catch (SQLException ex) {
            Messages.sprintfError("tableExists() Error: " + ex.getMessage());
            return false;
        }
    }

}
