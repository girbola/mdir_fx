
package com.girbola.persistence.selectedfolderinfo;

import com.girbola.Main;
import com.girbola.configuration.ConfigurationSqlConnection;
import com.girbola.controllers.folderscanner.SelectedFolder;
import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.SQLTableEnums;
import com.girbola.messages.Messages;
import com.girbola.persistence.fileinfo.FileInfoSqlConnectionFactory;
import com.girbola.sql.SQL_Utils;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javafx.collections.ObservableList;

import static com.girbola.sql.SQL_Utils.closeConnection;
import static com.girbola.sql.SQL_Utils.isDbConnected;

public class SelectedFolderInfoDao {

    private static final String selectedFolderTable = String.format("""
            CREATE TABLE IF NOT EXISTS %s (
                path      STRING PRIMARY KEY,
                connected BOOLEAN,
                ignored   BOOLEAN DEFAULT 0,
                selected  BOOLEAN,
                media     BOOLEAN
            
            )
            """, SQLTableEnums.SELECTEDFOLDERS.getType());


    static final String insertSelectedFolders = String.format("""
            INSERT OR REPLACE INTO %s (
                'selected',
                'connected',
                'ignored',
                'path',
                'media'
            ) VALUES (?,?,?,?,?)
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

        Messages.sprintf("configFile.getParent().toString(), configFile.getFileName().toString() " + configFile.getParent().toString() + " DATABASE NAMEEEE:::::::::::: " + configFile.getFileName().toString());
        try (Connection connection = ConfigurationSqlConnection.connectToDatabase(configFile.getParent().toString(), configFile.getFileName().toString())) {

            if (!isDbConnected(connection)) {
                Messages.sprintf("load_SelectedFolders_UsingSQL loading....");
                return false;
            }
            if (!FileInfoSqlConnectionFactory.tableExists(connection, SQLTableEnums.SELECTEDFOLDERS.getType())) {
                Messages.sprintf("Table not found + " + SQLTableEnums.SELECTEDFOLDERS.getType());
                return false;
            }

            boolean loadSelectedFoldersFromConfigDb = SelectedFolderInfoDao.loadSelectedFoldersFromConfigDb(connection, modelMain);
            if (loadSelectedFoldersFromConfigDb) {
                Messages.sprintf("load_SelectedFolders_UsingSQL loaded....");
                return true;
            } else {
                Messages.warningText("Could not load selected folders from database");
                return false;
            }

        } catch (SQLException ex) {
            Messages.sprintfError("Error connecting to database: " + configFile + " Exception: " + ex);
            return false;
        }
    }

    /**
     * Removes selected folders from the database table.
     *
     * @param selectedItems The list of selected folders to remove.
     */
    public static void removeFromTable(ObservableList<SelectedFolder> selectedItems) {
        if (selectedItems == null || selectedItems.isEmpty()) {
            return;
        }

        Connection connection = ConfigurationSqlConnection.connectToDatabase(Main.conf.getAppDataPath(), Main.conf.getConfiguration_db_fileName());
        if (connection == null) {
            Messages.sprintfError("Could not connect to configuration DB for removing selected folders: " + Main.conf.getConfiguration_db_fileName());
            return;
        }

        String sql = "DELETE FROM " + SQLTableEnums.SELECTEDFOLDERS.getType() + " WHERE path = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            connection.setAutoCommit(false);

            for (SelectedFolder sf : selectedItems) {
                if (sf == null || sf.getFolder() == null) {
                    continue;
                }
                pstmt.setString(1, sf.getFolder());
                pstmt.addBatch();
            }

            int[] counter = pstmt.executeBatch();
            connection.commit();
            Messages.sprintf("removeFromTable deleted rows: " + counter.length);
        } catch (Exception e) {
            Messages.sprintfError("removeFromTable failed: " + e.getMessage());
            SQL_Utils.rollBackConnection(connection);
        } finally {
            closeConnection(connection);
        }
    }

    public static boolean clearSelectedFolders(ModelMain modelMain) {
        Connection connection = ConfigurationSqlConnection.connectToDatabase(Main.conf.getAppDataPath(), Main.conf.getConfiguration_db_fileName());
        if (connection == null) {
            Messages.sprintfError("Could not SelectedFolder connect: " + Main.conf.getConfiguration_db_fileName());
            return false;
        }

        List<SelectedFolder> list = new ArrayList<>(modelMain.getSelectedFolders().getSelectedFolderScanner_obs());

        String sql = "DELETE FROM " + SQLTableEnums.SELECTEDFOLDERS.getType() + " WHERE path = ?";
        Messages.sprintf("removeFromIgnoredList SQL= " + sql);

        try {
            connection.setAutoCommit(false);
            PreparedStatement pstmt = connection.prepareStatement(sql);
            for (SelectedFolder path : list) {
                pstmt.setString(1, path.getFolder());
                pstmt.addBatch();
            }
            int[] counter = pstmt.executeBatch();
            connection.commit();
            Messages.sprintf("removeSelectedFolders counted rows: " + counter.length);
            pstmt.close();
            return true;

        } catch (Exception e) {
            SQL_Utils.rollBackConnection(connection);
            e.printStackTrace();
            return false;
        } finally {
            closeConnection(connection);
        }
    }

    public static void saveSelectedFoldersToConfigDb(ModelMain modelMain) {
        Connection connection = ConfigurationSqlConnection.connectToDatabase(Main.conf.getAppDataPath(), Main.conf.getConfiguration_db_fileName());
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
                Messages.sprintf("Table: already exists " + SQLTableEnums.SELECTEDFOLDERS.getType());
                for (SelectedFolder sf : modelMain.getSelectedFolders().getSelectedFolderScanner_obs()) {
                    Messages.sprintf("----SelectedFolder: " + sf.getFolder() + " isConnected? " + sf.isConnected() + " is ignored? " + sf.isIgnored() + " is media? " + sf.isMedia());
                }

                insertSelectedFoldersToDB(connection, modelMain.getSelectedFolders().getSelectedFolderScanner_obs());
//                updateSelectedFoldersToDB(connection, modelMain.getSelectedFolders().getSelectedFolderScanner_obs());
                for (SelectedFolder sf : modelMain.getSelectedFolders().getSelectedFolderScanner_obs()) {
                    Messages.sprintf("----SelectedFolder: " + sf.getFolder() + " isConnected? " + sf.isConnected() + " is ignored? " + sf.isIgnored() + " is media? " + sf.isMedia());
                }
            } else {
                Messages.sprintf("Table: not exists " + SQLTableEnums.SELECTEDFOLDERS.getType() + " not exists");
                createSelectedFoldersDBTable(connection);
                insertSelectedFoldersToDB(connection, modelMain.getSelectedFolders().getSelectedFolderScanner_obs());
            }
        } catch (SQLException e) {
            Messages.sprintfError("Error ensuring columns exist in file info table: " + e.getMessage());
        } finally {
            closeConnection(connection);
        }
    }

//    private static void updateSelectedFoldersToDB(Connection connection, ObservableList<SelectedFolder> selectedFolderScannerObs) {
//    }

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

                SelectedFolder selectedFolder = SelectedFolder.create(selected, connected, path, media, false);
                Messages.sprintf("loadFolders_list: " + selectedFolder.getFolder());

                boolean exists = modelMain.getSelectedFolders().getSelectedFolderScanner_obs().stream().anyMatch(sf -> Objects.equals(sf.getFolder(), selectedFolder.getFolder()));
                if (!exists) {
                    modelMain.getSelectedFolders().getSelectedFolderScanner_obs().add(selectedFolder);
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

    public static boolean insertSelectedFoldersToDB(
            Connection connection,
            List<SelectedFolder> selectedFolderList
    ) {

        if (!SQL_Utils.isDbConnected(connection)) {
            Messages.sprintfError("insertSelectedFoldersToDB: connection is null or closed");
            return false;
        }

        if (selectedFolderList == null) {
            Messages.sprintfError("insertSelectedFoldersToDB: list is null");
            return false;
        }

        if (selectedFolderList.isEmpty()) {
            Messages.sprintf("insertSelectedFoldersToDB: nothing to insert");
            return true;
        }

        if (!createSelectedFoldersDBTable(connection)) {
            Messages.sprintfError("insertSelectedFoldersToDB: failed to ensure table exists");
            return false;
        }

        boolean originalAutoCommit;

        final String sql = String.format("""
                INSERT INTO %s (
                    selected,
                    connected,
                    ignored,
                    path,
                    media
                ) VALUES (?,?,?,?,?)
                ON CONFLICT(path) DO UPDATE SET
                    selected = excluded.selected,
                    connected = excluded.connected,
                    ignored = excluded.ignored,
                    media = excluded.media
                """, SQLTableEnums.SELECTEDFOLDERS.getType());

        try {
            originalAutoCommit = connection.getAutoCommit();
        } catch (SQLException e) {
            Messages.sprintfError("Cannot read autoCommit: " + e.getMessage());
            return false;
        }

        int batchSize = 0;
        int batchLimit = 500;

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {

                for (SelectedFolder folder : selectedFolderList) {

                    if (folder == null) {
                        Messages.sprintfError("Null SelectedFolder skipped");
                        continue;
                    }

                    if (!folder.isSelected()) {
                        continue;
                    }

                    pstmt.setInt(1, folder.isSelected() ? 1 : 0);
                    pstmt.setInt(2, folder.isConnected() ? 1 : 0);
                    pstmt.setInt(3, folder.isIgnored() ? 1 : 0);
                    pstmt.setString(4, folder.getFolder());
                    pstmt.setString(5, folder.getDriveSerialNumber());

                    pstmt.addBatch();
                    batchSize++;

                    if (batchSize >= batchLimit) {
                        pstmt.executeBatch();
                        batchSize = 0;
                    }
                }

                if (batchSize > 0) {
                    pstmt.executeBatch();
                }
            }

            connection.commit();
            return true;

        } catch (SQLException ex) {

            try {
                connection.rollback();
            } catch (SQLException rbEx) {
                Messages.sprintfError("Rollback failed: " + rbEx.getMessage());
            }

            Messages.sprintfError("insertSelectedFoldersToDB failed: " + ex.getMessage());
            return false;

        } finally {

            try {
                connection.setAutoCommit(originalAutoCommit);
            } catch (SQLException e) {
                Messages.sprintfError("Failed to restore autoCommit: " + e.getMessage());
            }
        }
    }
//    public static boolean insertSelectedFoldersToDB_old(Connection connection, List<SelectedFolder> selectedFolder_list) {
//       // Messages.sprintf("insertSelectedFoldersToDB SQL: " + insertSelectedFolders);
//
//        // Validate inputs
//        if (!SQL_Utils.isDbConnected(connection)) {
//            Messages.sprintfError("insertSelectedFoldersToDB: Connection is closed or null!");
//            return false;
//        }
//        if (selectedFolder_list == null) {
//            Messages.sprintfError("insertSelectedFoldersToDB: selectedFolder_list is null");
//            return false;
//        }
//        if (selectedFolder_list.isEmpty()) {
//            Messages.sprintf("insertSelectedFoldersToDB: nothing to insert (empty list)");
//            return true;
//        }
//
//        // Ensure table exists
//        if (!createSelectedFoldersDBTable(connection)) {
//            Messages.sprintfError("insertSelectedFoldersToDB: failed to ensure table exists");
//            return false;
//        }
//
//        boolean originalAutoCommit;
//        try {
//            originalAutoCommit = connection.getAutoCommit();
//        } catch (SQLException e) {
//            Messages.sprintfError("insertSelectedFoldersToDB: could not read auto-commit state: " + e.getMessage());
//            return false;
//        }
//
//        boolean anyFailures = false;
//        boolean anyBatched = false;
//
//        try {
//            connection.setAutoCommit(false);
//
//            try (PreparedStatement pstmt = connection.prepareStatement(insertSelectedFolders)) {
//                for (SelectedFolder selectedFolder : selectedFolder_list) {
//                    if (selectedFolder == null) {
//                        anyFailures = true;
//                        Messages.sprintfError("insertSelectedFoldersToDB: encountered null SelectedFolder");
//                        continue;
//                    }
//
//                 //   Messages.sprintf("insertSelectedFoldersToDB -> evaluating: " + selectedFolder.getFolder());
//
//                    if (!selectedFolder.isSelected()) {
//                        // Skip silently or log at low level; do not break the loop
//                   //     Messages.sprintf("insertSelectedFoldersToDB: skipping unselected folder: " + selectedFolder.getFolder());
//                        continue;
//                    }
//
//                    boolean folderAdded = addToSelectedFoldersDB(connection, pstmt, selectedFolder);
//                    if (!folderAdded) {
//                        anyFailures = true;
//                     //   Messages.sprintfError("insertSelectedFoldersToDB: cannot add folder to database: " + selectedFolder.getFolder());
//                    } else {
//                       // Messages.sprintf("insertSelectedFoldersToDB: added folder to database: " + selectedFolder.getFolder());
//                        anyBatched = true;
//                    }
//                }
//
//                if (anyBatched) {
//                    pstmt.executeBatch();
//                }
//            }
//
//            connection.commit();
//            return !anyFailures;
//        } catch (SQLException ex) {
//            try {
//                connection.rollback();
//            } catch (SQLException rbEx) {
//                Messages.sprintfError("insertSelectedFoldersToDB rollback failed: " + rbEx.getMessage());
//            }
//           // Messages.sprintfError(Misc.getLineNumber() + " insertSelectedFoldersToDB failed: " + ex.getMessage());
//            return false;
//        } finally {
//            try {
//                connection.setAutoCommit(originalAutoCommit);
//            } catch (SQLException e) {
//                Messages.sprintfError("insertSelectedFoldersToDB: failed to restore auto-commit: " + e.getMessage());
//            }
//            //Messages.sprintf("insertSelectedFoldersToDB finished");
//        }
//    }


    /**
     * Adds the selected folder to the selected folders database.
     *
     * @param connection     The database connection.
     * @param pstmt          The prepared statement for the query.
     * @param selectedFolder The SelectedFolder object containing the folder details.
     * @return true if the folder is successfully added, false otherwise.
     */
//    private static boolean addToSelectedFoldersDB(Connection connection, PreparedStatement pstmt, SelectedFolder selectedFolder) {
//        try {
//            // INSERT columns order: 'selected', 'connected', 'path', 'media', 'ignored'
//            pstmt.setBoolean(1, selectedFolder.isSelected());
//            pstmt.setBoolean(2, selectedFolder.isConnected());
//            pstmt.setBoolean(3, selectedFolder.isIgnored());
//            pstmt.setString(4, selectedFolder.getFolder());
//            pstmt.setBoolean(5, selectedFolder.isMedia());
//
//            Messages.sprintf("addToSelectedFoldersDB: "
//                    + " path= " + selectedFolder.getFolder()
//                    + " selected=" + selectedFolder.isSelected()
//                    + " connected=" + selectedFolder.isConnected()
//                    + " media=" + selectedFolder.isMedia()
//                    + " ignored=" + selectedFolder.isIgnored());
//            pstmt.addBatch();
//            return true;
//        } catch (SQLException e) {
//            Messages.sprintfError("addToSelectedFoldersDB error for " + selectedFolder.getFolder() + ": " + e.getMessage());
//            return false;
//        }
//    }

}
