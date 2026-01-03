package com.girbola.sql;

import com.girbola.Main;
import com.girbola.controllers.folderscanner.SelectedFolder;
import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.SQLTableEnums;
import com.girbola.messages.Messages;

import com.girbola.misc.Misc;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.girbola.sql.SQL_Utils.closeConnection;
import static com.girbola.sql.SQL_Utils.isDbConnected;

public class SelectedFolderInfoSQL {

    private static final String selectedFolderTable = String.format("""
            CREATE TABLE IF NOT EXISTS %s (
                selected  BOOLEAN,
                path      STRING PRIMARY KEY,
                connected BOOLEAN,
                media     BOOLEAN
            )
            """, SQLTableEnums.SELECTEDFOLDERS.getType());


    static final String insertSelectedFolders = String.format("""
            INSERT OR REPLACE INTO %s (
                'selected',
                'connected',
                'path',
                'media'
            ) VALUES (?,?,?,?)
            """, SQLTableEnums.SELECTEDFOLDERS.getType());



    public static boolean createSelectedFoldersDBTable(Connection connection) {
        if (!isDbConnected(connection)) {
            Messages.sprintf("createSelectedFoldersDBTable: not connected");
            return false;
        }
        try (Statement stmt = connection.createStatement()) {
            Messages.sprintf("createSelectedFoldersDBTable: executing DDL");
            stmt.execute(selectedFolderTable); // ensure DDL uses IF NOT EXISTS and correct types
            return true;
        } catch (SQLException ex) {
            Messages.sprintf("createSelectedFoldersDBTable: DDL failed. SQLState=" + ex.getSQLState()
                    + " ErrorCode=" + ex.getErrorCode() + " Message=" + ex.getMessage());
            return false;
        }
    }


    public static boolean loadSelectedFolders(ModelMain modelMain) {

        Path configFile = Paths.get(Main.conf.getAppDataPath().toString(), Main.conf.getConfiguration_db_fileName());

        Messages.sprintf("configFile.getParent().toString(), configFile.getFileName().toString() " + configFile.getParent().toString() + " DATABASE NAMEEEE:::::::::::: " +  configFile.getFileName().toString());
        try (Connection connection = SqliteConnection.connectToDatabase(configFile.getParent().toString(), configFile.getFileName().toString())) {

            if (!isDbConnected(connection)) {
                Messages.sprintf("load_SelectedFolders_UsingSQL loading....");
                return false;
            }
            if (!SqliteConnection.tableExists(connection, SQLTableEnums.SELECTEDFOLDERS.getType())) {
                Messages.sprintf("Table not found + " + SQLTableEnums.SELECTEDFOLDERS.getType());
                return false;
            }

            boolean loadSelectedFoldersFromConfigDb = SelectedFolderInfoSQL.loadSelectedFoldersFromConfigDb(connection, modelMain);
            if(loadSelectedFoldersFromConfigDb) {
                Messages.sprintf("load_SelectedFolders_UsingSQL loaded....");
                closeConnection(connection);
                return true;
            } else {
                Messages.warningText("Could not load selected folders from database");
                closeConnection(connection);
                return false;
            }

        } catch (SQLException ex) {
            Messages.sprintfError("Error connecting to database: " + configFile + " Exception: " + ex);
            return false;
        }
    }


    public static boolean clearSelectedFolders(ModelMain modelMain) {
        Connection connection = SqliteConnection.connectToDatabase(Main.conf.getAppDataPath(), Main.conf.getConfiguration_db_fileName());
        if (connection == null) {
            Messages.sprintfError("Could not SelectedFolder connect: " + Main.conf.getConfiguration_db_fileName());
        }

        List<SelectedFolder> list = new ArrayList<>(modelMain.getSelectedFolders().getSelectedFolderScanner_obs());

        String sql = "DELETE FROM " + SQLTableEnums.SELECTEDFOLDERS.getType() + " WHERE path = ?";
        Messages.sprintf("removeFromIgnoredList SQL= " + sql);

        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            for (SelectedFolder path : list) {
                pstmt.setString(1, path.getFolder());
                pstmt.addBatch();
            }
            int[] counter = pstmt.executeBatch();
            connection.commit();
            Messages.sprintf("removeSelectedFolders counted rows: " + counter.length);
            pstmt.close();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        closeConnection(connection);
        return true;
    }

    public static void saveSelectedFoldersToConfigDb(ModelMain modelMain) {
        Connection connection = SqliteConnection.connectToDatabase(Main.conf.getAppDataPath(), Main.conf.getConfiguration_db_fileName());
        if (connection == null) {
            Messages.sprintfError("Could not SelectedFolder connect: " + Main.conf.getConfiguration_db_fileName());
            return;
        }

        boolean dbConnected = isDbConnected(connection);
        if (!dbConnected) {
            Messages.sprintfError("Could not SelectedFolder connect: " + Main.conf.getConfiguration_db_fileName());
        }

        try {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet resultSet = metaData.getTables(null, null, SQLTableEnums.SELECTEDFOLDERS.getType(), null);
            if (resultSet.next()) {
                Messages.sprintf("Table: " + SQLTableEnums.SELECTEDFOLDERS.getType() + " already exists");
                insertSelectedFoldersToDB(connection, modelMain.getSelectedFolders().getSelectedFolderScanner_obs());
            } else {
                Messages.sprintf("Table: " + SQLTableEnums.SELECTEDFOLDERS.getType() + " not exists");
                createSelectedFoldersDBTable(connection);
                insertSelectedFoldersToDB(connection, modelMain.getSelectedFolders().getSelectedFolderScanner_obs());
            }
        } catch (SQLException e) {
            Messages.sprintfError("Error ensuring columns exist in file info table: " + e.getMessage());
        } finally {
            closeConnection(connection);
        }
    }

    public static boolean loadSelectedFoldersFromConfigDb(Connection connection, ModelMain modelMain) {

        if (!isDbConnected(connection)) {
            Messages.sprintf("loadSelectedFoldersFromConfigDb did not connect");
            return false;
        }

        try {
            String sql = "SELECT * FROM " + SQLTableEnums.SELECTEDFOLDERS.getType(); // configurationDB / selectedfolders table
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                boolean selected = rs.getBoolean("selected");
                String path = rs.getString("path");

                boolean connected = rs.getBoolean("connected");
                boolean media = rs.getBoolean("media");

                Messages.sprintf("---------------------------selected::: " + selected + " path::: " + path + " connected:::" + connected + " media:::" + media);

                SelectedFolder selectedFolder = new SelectedFolder(selected, connected, path, media);
                Messages.sprintf("loadFolders_list: " + selectedFolder.getFolder());

//                ObservableList<SelectedFolder> obs = modelMain.getSelectedFolders().getSelectedFolderScanner_obs();

                boolean exists = modelMain.getSelectedFolders().getSelectedFolderScanner_obs().stream().anyMatch(sf -> Objects.equals(sf.getFolder(), selectedFolder.getFolder()));
                if (!exists) {
                    modelMain.getSelectedFolders().getSelectedFolderScanner_obs().add(selectedFolder);
//                    Messages.sprintf("Added selected folder: " + selectedFolder.getFolder());
//                    for(SelectedFolder selectedFolderExists : modelMain.getSelectedFolders().getSelectedFolderScanner_obs()) {
//                        if(selectedFolderExists.getFolder().equals(selectedFolder.getFolder())) {
//                            modelMain.getSelectedFolders().getSelectedFolderScanner_obs().add(selectedFolder);
//                        }
//                    }
                } else {
                    Messages.sprintf("Skipped duplicate selected folder: " + selectedFolder.getFolder());
                }

            }
            Messages.sprintf("size of sel obs= " + modelMain.getSelectedFolders().getSelectedFolderScanner_obs().size());
            for (SelectedFolder self : modelMain.getSelectedFolders().getSelectedFolderScanner_obs()) {
                Messages.sprintf("selectedFolder: " + self.getFolder() + " isConnected? " + self.isConnected());
            }
            return true;
        } catch (Exception e) {
            Messages.sprintfError("Can't find selectedfolders list.");
            return false;
        }
    }


    public static boolean insertSelectedFoldersToDB(Connection connection, List<SelectedFolder> selectedFolder_list) {
        Messages.sprintf("insertSelectedFoldersToDB SQL: " + insertSelectedFolders);

        // Validate inputs
        if (!SQL_Utils.isDbConnected(connection)) {
            Messages.sprintfError("insertSelectedFoldersToDB: Connection is closed or null!");
            return false;
        }
        if (selectedFolder_list == null) {
            Messages.sprintfError("insertSelectedFoldersToDB: selectedFolder_list is null");
            return false;
        }
        if (selectedFolder_list.isEmpty()) {
            Messages.sprintf("insertSelectedFoldersToDB: nothing to insert (empty list)");
            return true;
        }

        // Ensure table exists
        if (!createSelectedFoldersDBTable(connection)) {
            Messages.sprintfError("insertSelectedFoldersToDB: failed to ensure table exists");
            return false;
        }

        boolean originalAutoCommit;
        try {
            originalAutoCommit = connection.getAutoCommit();
        } catch (SQLException e) {
            Messages.sprintfError("insertSelectedFoldersToDB: could not read auto-commit state: " + e.getMessage());
            return false;
        }

        boolean anyFailures = false;
        boolean anyBatched = false;

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement pstmt = connection.prepareStatement(insertSelectedFolders)) {
                for (SelectedFolder selectedFolder : selectedFolder_list) {
                    if (selectedFolder == null) {
                        anyFailures = true;
                        Messages.sprintfError("insertSelectedFoldersToDB: encountered null SelectedFolder");
                        continue;
                    }

                    Messages.sprintf("insertSelectedFoldersToDB -> evaluating: " + selectedFolder.getFolder());

                    if (!selectedFolder.isSelected()) {
                        // Skip silently or log at low level; do not break the loop
                        Messages.sprintf("insertSelectedFoldersToDB: skipping unselected folder: " + selectedFolder.getFolder());
                        continue;
                    }

                    boolean folderAdded = addToSelectedFoldersDB(connection, pstmt, selectedFolder);
                    if (!folderAdded) {
                        anyFailures = true;
                        Messages.sprintfError("insertSelectedFoldersToDB: cannot add folder to database: " + selectedFolder.getFolder());
                    } else {
                        anyBatched = true;
                    }
                }

                if (anyBatched) {
                    pstmt.executeBatch();
                }
            }

            connection.commit();
            return !anyFailures;
        } catch (SQLException ex) {
            try {
                connection.rollback();
            } catch (SQLException rbEx) {
                Messages.sprintfError("insertSelectedFoldersToDB: rollback failed: " + rbEx.getMessage());
            }
            Messages.sprintfError(Misc.getLineNumber() + " insertSelectedFoldersToDB failed: " + ex.getMessage());
            return false;
        } finally {
            try {
                connection.setAutoCommit(originalAutoCommit);
            } catch (SQLException e) {
                Messages.sprintfError("insertSelectedFoldersToDB: failed to restore auto-commit: " + e.getMessage());
            }
            Messages.sprintf("insertSelectedFoldersToDB finished");
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
            // INSERT columns order: 'selected', 'connected', 'path', 'media'
            pstmt.setBoolean(1, selectedFolder.isSelected());
            pstmt.setBoolean(2, selectedFolder.isConnected());
            pstmt.setString(3, selectedFolder.getFolder());
            pstmt.setBoolean(4, selectedFolder.isMedia());

            Messages.sprintf("addToSelectedFoldersDB: " + selectedFolder.getFolder()
                    + " connected=" + selectedFolder.isConnected()
                    + " media=" + selectedFolder.isMedia());

            pstmt.addBatch();
            return true;
        } catch (SQLException e) {
            Messages.sprintfError("addToSelectedFoldersDB error for " + selectedFolder.getFolder() + ": " + e.getMessage());
            return false;
        }
    }


}
