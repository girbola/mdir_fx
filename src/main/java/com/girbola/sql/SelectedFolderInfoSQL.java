package com.girbola.sql;

import com.girbola.Main;
import com.girbola.controllers.folderscanner.SelectedFolder;
import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.SQL_Enums;
import com.girbola.messages.Messages;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static com.girbola.sql.SQL_Utils.closeConnection;

public class SelectedFolderInfoSQL {

    private static final String selectedFolderTable =
            "CREATE TABLE IF NOT EXISTS " +
                    SQL_Enums.SELECTEDFOLDERS.getType() +
                    " (selected BOOLEAN, path STRING PRIMARY KEY, connected BOOLEAN, media BOOLEAN)";

    static final String insertSelectedFolders = "INSERT OR REPLACE INTO " + SQL_Enums.SELECTEDFOLDERS.getType()
            + " ('selected', 'connected', 'path', 'media') VALUES(?,?,?,?)";

    public static boolean createSelectedFoldersDBTable(Connection connection) {
        if (connection == null) {
            Messages.sprintf("createSelectedFoldersTable Connection were null!");
            return false;
        }
        if (!SQL_Utils.isDbConnected(connection)) {
            Messages.sprintf("createSelectedFoldersTable Not connected");
            return false;
        }
        try {
            Statement stmt = connection.createStatement();
            stmt.execute(selectedFolderTable);
            return true;
        } catch (Exception ex) {
            Messages.sprintf("createSelectedFoldersTable were not able to connect to");
            return false;
        }
    }

    public static boolean loadSelectedFolders(ModelMain model_Main) {

        Connection connection = null;
        Path configFile = Paths.get(Main.conf.getAppDataPath() + File.separator + Main.conf.getConfiguration_db_fileName());

        try {
            connection = SqliteConnection.connector(Main.conf.getAppDataPath(), Main.conf.getConfiguration_db_fileName());
        } catch (Exception e) {
            Messages.sprintfError("Error connecting to database: " + configFile);
        }

        if (connection == null) {
            return false;
        }

        if (SQL_Utils.isDbConnected(connection)) {
            Messages.sprintf("load_SelectedFolders_UsingSQL loading....");
            SQL_Utils.loadFolders_list(connection, model_Main);
            closeConnection(connection);
            return true;
        } else {
            Messages.sprintf("Nothing to load from database");
            closeConnection(connection);
            return false;
        }
    }



    public static boolean clearSelectedFolders(ModelMain modelMain) {
        Connection connection = SqliteConnection.connector(Main.conf.getAppDataPath(), Main.conf.getConfiguration_db_fileName());
        if (connection == null) {
            Messages.sprintfError("Could not SelectedFolder connect: " + Main.conf.getConfiguration_db_fileName());
        }

        List<SelectedFolder> list = new ArrayList<>(modelMain.getSelectedFolders().getSelectedFolderScanner_obs());

        String sql = "DELETE FROM " + SQL_Enums.SELECTEDFOLDERS.getType() + " WHERE path = ?";
        Messages.sprintf("removeFromIgnoredList SQL= " + sql);

        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            for (SelectedFolder path : list) {
                pstmt.setString(1, path.getFolder());
                pstmt.addBatch();
            }
            int[] counter = pstmt.executeBatch();
            Messages.sprintf("removeSelectedFolders counted rows: " + counter.length);
            pstmt.close();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        closeConnection(connection);
        return true;
    }

    public static void saveSelectedFolder(ModelMain modelMain) {
        Connection connection = SqliteConnection.connector(Main.conf.getAppDataPath(), Main.conf.getConfiguration_db_fileName());
        if (connection == null) {
            Messages.sprintfError("Could not SelectedFolder connect: " + Main.conf.getConfiguration_db_fileName());
            return;
        }
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet resultSet = metaData.getTables(null, null, SQL_Enums.SELECTEDFOLDERS.getType(), null);
            if (resultSet.next()) {
                Messages.sprintf("Table: " + SQL_Enums.SELECTEDFOLDERS.getType() + " already exists");
            } else {
                Messages.sprintf("Table: " + SQL_Enums.SELECTEDFOLDERS.getType() + " not exists");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        //createFolderInfoDatabase

        createSelectedFoldersDBTable(connection);

        insertSelectedFoldersToDB(connection, modelMain.getSelectedFolders().getSelectedFolderScanner_obs());

        closeConnection(connection);

    }

    public static boolean insertSelectedFoldersToDB(Connection connection, List<SelectedFolder> selectedFolder_list) {
        Messages.sprintf("insertSelectedFolders_List_ToDB: " + insertSelectedFolders);
        createSelectedFoldersDBTable(connection);
        try {
            connection.setAutoCommit(false);
            PreparedStatement pstmt = connection.prepareStatement(insertSelectedFolders);

            for (SelectedFolder selectedFolder : selectedFolder_list) {
                Messages.sprintf("select: " + selectedFolder.getFolder());
                if (selectedFolder.isSelected()) {
                    Messages.sprintf("332 addToSelectedFoldersDB");
                    boolean folderAdded = addToSelectedFoldersDB(connection, pstmt, selectedFolder);
                    if (!folderAdded) {
                        Messages.sprintfError("Cannot add folder to database: " + selectedFolder.getFolder());
                    }
                } else {
                    Messages.sprintfError("insertSelectedFolders_List_ToDB SelectedFolder did not exist: " + selectedFolder.getFolder());
                    break;
                }
            }
            pstmt.executeBatch();
            connection.commit();
            pstmt.close();
            closeConnection(connection);
            return true;
        } catch (Exception ex) {
            Messages.sprintfError("Insert SElected Folders list to database has failed: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Adds the selected folder to the selected folders database.
     *
     * @param connection     The database connection.
     * @param pstmt          The prepared statement for the query.
     * @param selectedFolder The SelectedFolder object containing the folder details.
     * @return true if the folder is successfully added, false otherwise.
     */
    private static boolean addToSelectedFoldersDB(Connection connection, PreparedStatement pstmt, SelectedFolder selectedFolder) {
        try {
            pstmt.setBoolean(1, selectedFolder.selectedProperty().get());
            pstmt.setBoolean(2, selectedFolder.connected_property().get());
            pstmt.setString(3, selectedFolder.getFolder());

            Messages.sprintf("selectedfolder is. " + selectedFolder.getFolder() + " is connected? " + selectedFolder.connected_property().get());
            pstmt.addBatch();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}