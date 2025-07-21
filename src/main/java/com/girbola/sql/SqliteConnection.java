package com.girbola.sql;

import com.girbola.messages.Messages;
import java.io.File;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lombok.Getter;

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

    public static void addConnection(Connection conn) {

        try {
            for(Connection c : connectionList) {
                if(c.getMetaData().getURL().equals(conn.getMetaData().getURL())) {
                    return;
                }
                if(c == null || c.isClosed()) {
                    connectionList.remove(c);
                }
            }
            Messages.sprintf("Adding connection database: " + conn.getMetaData().getURL());
            connectionList.add(conn);
        } catch (Exception e) {
            Messages.sprintfError("Error adding connection: " + e.getMessage());
        }
    }

    public static void removeConnection(Connection conn) {
        connectionList.remove(conn);
    }

    public static Connection connector(Path path, String tableName) {
        Messages.sprintf("Connection to path: " + path.toFile().getAbsolutePath() + " tableName: " + tableName);

        if (path.startsWith("")) {
            Messages.sprintf("Path is empty and its absolutely path is: " + path.toFile().getAbsolutePath() + ". TableName is: " + tableName);
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
        if (connection == null) {
            return false;
        }
        try {
            DatabaseMetaData md = connection.getMetaData();
            ResultSet rs = md.getTables(null, null, tableName, null);
            rs.last();
            return rs.getRow() > 0;
        } catch (SQLException ex) {
//			ex.printStackTrace();
            return false;
        }
    }

}
