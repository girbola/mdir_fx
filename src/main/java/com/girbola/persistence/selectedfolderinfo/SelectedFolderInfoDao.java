
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javafx.collections.ObservableList;

import static com.girbola.sql.SQL_Utils.closeConnection;
import static com.girbola.sql.SQL_Utils.isDbConnected;

public class SelectedFolderInfoDao {

    public static final String PATH = "path";
    public static final String CONNECTED = "connected";
    public static final String IGNORED = "ignored";
    public static final String SELECTED = "selected";
    public static final String MEDIA = "media";
    public static final String DRIVE_SERIAL_NUMBER = "driveSerialNumber";

    private static final String SELECT_COLUMNS = String.join(", ",
            PATH, CONNECTED, IGNORED, SELECTED, MEDIA, DRIVE_SERIAL_NUMBER);

    private static final String selectedFolderTable = String.format("""
            CREATE TABLE IF NOT EXISTS %s (
                %s STRING PRIMARY KEY UNIQUE NOT NULL,
                %s BOOLEAN,
                %s BOOLEAN DEFAULT 0,
                %s BOOLEAN,
                %s BOOLEAN,
                %s STRING
            )
            """, SQLTableEnums.SELECTEDFOLDERS.getType(),
            PATH, CONNECTED, IGNORED, SELECTED, MEDIA, DRIVE_SERIAL_NUMBER);


    public static boolean createSelectedFoldersDBTable(Connection connection) {
        if (!isDbConnected(connection)) {
            Messages.sprintf("createSelectedFoldersDBTable: not connected");
            return false;
        }
        try (Statement stmt = connection.createStatement()) {
            Messages.sprintf("createSelectedFoldersDBTable: executing DDL");
            stmt.execute(selectedFolderTable); // ensure DDL uses IF NOT EXISTS and correct types
            ensureSelectedFoldersColumns(connection);
            return true;
        } catch (SQLException ex) {
            Messages.sprintf("createSelectedFoldersDBTable: DDL failed. SQLState=" + ex.getSQLState()
                    + " ErrorCode=" + ex.getErrorCode() + " Message=" + ex.getMessage());
            return false;
        }
    }

    private static void ensureSelectedFoldersColumns(Connection connection) {
        try {
            Map<String, String> requiredColumns = new LinkedHashMap<>();
            requiredColumns.put(CONNECTED, "BOOLEAN");
            requiredColumns.put(IGNORED, "BOOLEAN");
            requiredColumns.put(SELECTED, "BOOLEAN");
            requiredColumns.put(MEDIA, "BOOLEAN");
            requiredColumns.put(DRIVE_SERIAL_NUMBER, "TEXT");
            SQL_Utils.ensureColumnsExist(connection, SQLTableEnums.SELECTEDFOLDERS.getType(), requiredColumns);
        } catch (SQLException e) {
            Messages.sprintfError("Error ensuring columns exist in selected folders table: " + e.getMessage());
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

        String sql = "DELETE FROM " + SQLTableEnums.SELECTEDFOLDERS.getType() + " WHERE " + PATH + " = ?";
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

        String sql = "DELETE FROM " + SQLTableEnums.SELECTEDFOLDERS.getType() + " WHERE " + PATH + " = ?";
        Messages.sprintf("removeFromIgnoredList SQL= " + sql);

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            connection.setAutoCommit(false);
            for (SelectedFolder path : list) {
                pstmt.setString(1, path.getFolder());
                pstmt.addBatch();
            }
            int[] counter = pstmt.executeBatch();
            connection.commit();
            Messages.sprintf("removeSelectedFolders counted rows: " + counter.length);
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
                    Messages.sprintf("----SelectedFolder: isSelected? "+ sf.isSelected() + " " + sf.getFolder() + " isConnected? " + sf.isConnected() + " is ignored? " + sf.isIgnored() + " is media? " + sf.isMedia());
                }
            } else {
                Messages.sprintf("Table: not exists " + SQLTableEnums.SELECTEDFOLDERS.getType() + " not exists");
                createSelectedFoldersDBTable(connection);
                insertSelectedFoldersToDB(connection, modelMain.getSelectedFolders().getSelectedFolderScanner_obs());
            }
        } catch (SQLException e) {
            Messages.sprintfError("Error saving selected folders to database: " + e.getMessage());
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

        String sql = "SELECT " + SELECT_COLUMNS + " FROM " + SQLTableEnums.SELECTEDFOLDERS.getType();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String path = rs.getString(PATH);
                boolean connected = rs.getBoolean(CONNECTED);
                boolean ignored = rs.getBoolean(IGNORED);
                boolean selected = rs.getBoolean(SELECTED);
                boolean media = rs.getBoolean(MEDIA);
                String driveSerialNumber = rs.getString(DRIVE_SERIAL_NUMBER);

                Messages.sprintf("---------------------------selected::: " + selected + " path::: " + path + " connected:::" + connected + " media:::" + media + " ignored:::" + ignored + " driveSerialNumber:::" + driveSerialNumber);

                SelectedFolder selectedFolder = SelectedFolder.create(path, selected, connected, media, ignored);
                if (selectedFolder != null) {
                    if (driveSerialNumber != null && !driveSerialNumber.isBlank()) {
                        selectedFolder.setDriveSerialNumber(driveSerialNumber);
                    }
                    Messages.sprintf("loadFolders_list: " + selectedFolder.getFolder());

                    boolean exists = modelMain.getSelectedFolders().getSelectedFolderScanner_obs().stream().anyMatch(sf -> Objects.equals(sf.getFolder(), selectedFolder.getFolder()));
                    if (!exists) {
                        modelMain.getSelectedFolders().getSelectedFolderScanner_obs().add(selectedFolder);
                    } else {
                        Messages.sprintf("Skipped duplicate selected folder: " + selectedFolder.getFolder());
                    }
                }

            }
            Messages.sprintf("size of sel obs= " + modelMain.getSelectedFolders().getSelectedFolderScanner_obs().size());
            for (SelectedFolder self : modelMain.getSelectedFolders().getSelectedFolderScanner_obs()) {
                Messages.sprintf("selectedFolder: " + self.getFolder() + " isConnected? " + self.isConnected());
            }
            return true;
        } catch (Exception e) {
            Messages.sprintfError("Can't find selectedfolders list: " + e.getMessage());
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
                    %s,
                    %s,
                    %s,
                    %s,
                    %s,
                    %s
                ) VALUES (?,?,?,?,?,?)
                ON CONFLICT(%s) DO UPDATE SET
                    %s = excluded.%s,
                    %s = excluded.%s,
                    %s = excluded.%s,
                    %s = excluded.%s,
                    %s = excluded.%s
                """,
                SQLTableEnums.SELECTEDFOLDERS.getType(),
                PATH, CONNECTED, IGNORED, SELECTED, MEDIA, DRIVE_SERIAL_NUMBER,
                PATH,
                CONNECTED, CONNECTED,
                IGNORED, IGNORED,
                SELECTED, SELECTED,
                MEDIA, MEDIA,
                DRIVE_SERIAL_NUMBER, DRIVE_SERIAL_NUMBER);

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

                    pstmt.setString(1, folder.getFolder());
                    pstmt.setBoolean(2, folder.isConnected());
                    pstmt.setBoolean(3, folder.isIgnored());
                    pstmt.setBoolean(4, folder.isSelected());
                    pstmt.setBoolean(5, folder.isMedia());
                    pstmt.setString(6, folder.getDriveSerialNumber());

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

}
