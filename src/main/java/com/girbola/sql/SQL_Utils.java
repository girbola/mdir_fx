package com.girbola.sql;

import com.girbola.Main;
import com.girbola.controllers.folderscanner.SelectedFolder;
import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.SQL_Enums;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.messages.Messages;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class SQL_Utils extends FolderInfo_SQL {
    final private static String ERROR = SQL_Utils.class.getSimpleName();

    // @formatter:off
/*    final static String folderInfoColumns =
            "configuration," +
            "folderinfo," +
            "fileinfo," +
            "selectedfolders, " +
            "registereddrives," +
            "driveinfo, " +
            "thumbinfo," +
            "workdir," +
            "ignoredlist, " +
            "config," +
            "tables_cols," +
            "folderinfos " +
            "FROM your_table_name;";*/

	/*
	 * this.orgPath = aOrgPath; this.fileInfo_id = fileInfo_id; this.destinationPath
	 * = ""; this.event = ""; this.location = ""; this.tags = ""; this.camera_model
	 * = "Unknown"; this.orientation = 0; this.timeShift = 0; this.bad = false;
	 * this.good = false; this.suggested = false; this.confirmed = false; this.raw =
	 * false; this.image = false; this.video = false; this.ignored = false;
	 * this.tableDuplicated = false; this.date = 0; this.size = 0; this.thumb_offset
	 * = 0; this.thumb_length = 0; this.user = "";
	 */
/*
  pstmt.setBoolean(1, selectedFolder.selectedProperty().get());
            pstmt.setBoolean(2, selectedFolder.connected_property().get());
            pstmt.setString(3, selectedFolder.getFolder());
 */
/*

    final static String insertToFolderInfos =
            "INSERT OR REPLACE INTO " +
                    SQL_Enums.FOLDERINFOS.getType() +
                    " (" +
                    "'path', " +
                    "'tableType', " +
                    "'justFolderName', " +
                    "'connected')" +
                    " VALUES(?,?,?,?)";

*/

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

    public static boolean closeConnection(Connection connection) {
        try {
            if (connection != null) {
                connection.close();
                return true;
            }
        } catch (Exception e) {
            Messages.warningText(Main.bundle.getString("cannotCloseConnection") + e.getMessage());
            return false;
        }
        return false;
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

    /**
     * @param connection
     * @return
     */
    public static boolean isDbConnected(Connection connection) {
        if (connection == null) {
            return false;
        }
        try {
            return !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }

    }

    public static FolderInfo loadFolderInfoCurrentDir(Path path) {
        Messages.sprintf("loadFolderInfos started: " + path);
        FolderInfo folderInfo = FolderInfo_SQL.loadFolderInfo(path.toString());
        return folderInfo;
    }

    public static boolean removeAllData(Connection connection, String tableName, String path) {
        if (connection == null) {
            return false;
        }
        if (!isDbConnected(connection)) {
            return false;
        }
        String sql = "DELETE FROM " + tableName + " WHERE path LIKE " + path;
        try {
            Statement stmt = connection.createStatement();
            stmt.execute(sql);
        } catch (Exception e) {
            return false;
        }

        return false;
    }

    public static boolean removeAllData_list(Connection connection, ArrayList<SelectedFolder> listToRemove, String table) {
        if (connection == null) {
            return false;
        }
        if (!isDbConnected(connection)) {
            return false;
        }

        String sql = "DELETE FROM " + table + " WHERE path = ?";
        try {
            connection.setAutoCommit(false);
            PreparedStatement pstmt = connection.prepareStatement(sql);

            for (SelectedFolder self : listToRemove) {
                Messages.sprintf("Self path is: " + self.getFolder());
                pstmt.setString(1, self.getFolder());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            connection.commit();
            pstmt.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
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

    public static void removeSelectedFolder_FromDB(Connection connection, String path) {
        try {
            String sql = "DELETE FROM " + SQL_Enums.SELECTEDFOLDERS.getType() + " WHERE path = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, path);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void rollBack(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            Messages.sprintfError("Could not rollback: " + e.getMessage());
            Main.setProcessCancelled(true);
        }
    }

    public static Connection createConfigurationConfig() {
        try {
            Connection connection = SqliteConnection.connector(Main.conf.getAppDataPath(), Main.conf.getConfiguration_db_fileName());
            if (connection == null) {
                return null;
            }
            if (!SQL_Utils.isDbConnected(connection)) {
                return null;
            }
            return connection;
        } catch (Exception e) {
            Messages.sprintfError("Error connecting to database: " + Main.conf.getConfiguration_db_fileName());
            return null;
        }

    }
}
