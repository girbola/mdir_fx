package com.girbola.persistence.configuration;

import com.girbola.Main;
import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.SQLTableEnums;
import com.girbola.controllers.main.sql.ConfigurationSQLHandler;
import com.girbola.controllers.main.tables.model.SavedFolderInfos;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;

import com.girbola.sql.SQL_Utils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConfigurationSavedFoldersDao {

    private static final String ERROR = ConfigurationSavedFoldersDao.class.getSimpleName();

    //@formatter:off
    private static final String insertToSavedFolderInfos = """
        INSERT OR REPLACE INTO %s (
            path,
            tableType,
            justFolderName,
            connected,
            driveIdentifier,
            driveSerial,
            driveSize,
            partitionUuid,
            partitionIdentification
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.formatted(SQLTableEnums.SAVED_FOLDERS.getType());

    private static final String createSavedFolderTable = """
        CREATE TABLE IF NOT EXISTS %s (
            path TEXT NOT NULL PRIMARY KEY UNIQUE,
            justFolderName TEXT,
            tableType TEXT NOT NULL,
            connected BOOLEAN,
            driveIdentifier TEXT,
            driveSerial TEXT,
            driveSize INTEGER NOT NULL DEFAULT 0,
            partitionUuid TEXT,
            partitionIdentification TEXT
        )
        """.formatted(SQLTableEnums.SAVED_FOLDERS.getType());
    //@formatter:on
    public static boolean replaceSavedFoldersDriveLetter(Path oldDriveLetterPath, Path newDriveLetterPath) {
        Connection configurationDatabaseConnection = ConfigurationSQLHandler.getConnection();
        if (configurationDatabaseConnection == null) {
            Messages.sprintfError("replaceSavedFoldersDriveLetter Connection was null!");
            return false;
        }
        List<SavedFolderInfos> savedFolderInfos = loadSavedFolderDetails(configurationDatabaseConnection, null);

        assert savedFolderInfos != null : "savedFolderInfos is null!";

        boolean changed = false;
        for (SavedFolderInfos savedFolderInfo : savedFolderInfos) {
            if (savedFolderInfo.getFolderPath().equals(oldDriveLetterPath.toString())) {
                Messages.sprintf("replaceSavedFoldersDriveLetter: The new drive letter is already present in the database: " + newDriveLetterPath);
                if (Files.exists(oldDriveLetterPath)) {
                    savedFolderInfo.setFolderPath(newDriveLetterPath.toString());
                    Messages.sprintf("replaceSavedFoldersDriveLetter: Replaced old drive letter with new drive letter in the database: " + newDriveLetterPath);
                    changed = true;
                }
            }
        }

        if (changed) {
            Messages.sprintf("replaceSavedFoldersDriveLetter: Saving changes to the database...");
            boolean savedFolderInfosDatabase = createSavedFoldersIntoConfigurationDatabase(configurationDatabaseConnection);
            if (!savedFolderInfosDatabase) {
                Messages.sprintfError("replaceSavedFoldersDriveLetter Could not create FolderInfos database!");
                return false;
            }
            return true;
        }
        return true;
    }

    /**
     * Inserts the information of a saved folder into the database.
     *
     * @param configurationDatabaseConnection the database connection object used for the operation.
     *                                        This must not be null to perform the insertion.
     * @param savedFolderInfos                an object containing folder details such as the folder path,
     *                                        table type, folder name, and connection status.
     * @return true if the folder information was successfully inserted into the database,
     * false otherwise (e.g., in case of null connection, database creation failure, or errors during insertion).
     */
    //@formatter:on
    public static boolean insertSavedFoldersIntoConfigurationDatabase(Connection configurationDatabaseConnection, SavedFolderInfos savedFolderInfos) {
        if (configurationDatabaseConnection == null) {
            Messages.sprintfError("insertSavedFolderInfoToDatabase Connection was null!");
            return false;
        }
        boolean savedFolderInfosDatabase = createSavedFoldersIntoConfigurationDatabase(configurationDatabaseConnection);
        if (!savedFolderInfosDatabase) {
            Messages.sprintfError("insertSavedFolderInfoToDatabase Could not create FolderInfos database!");
            return false;
        }
        try (PreparedStatement pstmt = configurationDatabaseConnection.prepareStatement(insertToSavedFolderInfos)) {
            pstmt.setString(1, savedFolderInfos.getFolderPath());
            pstmt.setString(2, savedFolderInfos.getTableType());
            pstmt.setString(3, savedFolderInfos.getJustFolderName());
            pstmt.setBoolean(4, savedFolderInfos.isConnected());
            pstmt.setString(5, savedFolderInfos.getDriveIdentifier());
            pstmt.setString(6, savedFolderInfos.getDriveSerial());
            pstmt.setLong(7, savedFolderInfos.getDriveSize());
            pstmt.setString(8, savedFolderInfos.getPartitionUuid());
            pstmt.setString(9, savedFolderInfos.getPartitionIdentification());
            pstmt.executeUpdate();
            SQL_Utils.commitChanges(configurationDatabaseConnection);
            return true;
        } catch (Exception e) {
            Messages.sprintfError("insertSavedFolderInfoToDatabase error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static List<SavedFolderInfos> loadSavedFolderDetails(Connection connection, ModelMain model_Main) {
        if (Main.getProcessCancelled()) {
            return null;
        }

        if (!SQL_Utils.isDbConnected(connection)) {
            Messages.sprintf("NOT Connected!");
        }

        if (!createSavedFoldersIntoConfigurationDatabase(connection)) {
            return null;
        }

        String sql = "SELECT * FROM " + SQLTableEnums.SAVED_FOLDERS.getType();
        try {
            List<SavedFolderInfos> savedFolderInfos = new ArrayList<>();
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    if (Main.getProcessCancelled()) {
                        Messages.sprintf("getAll cancelled!");
                        return null;
                    }
                    String path = rs.getString("path");
                    if (path == null) {
                        Messages.sprintf("Something went badly wrong!");
                        Messages.errorSmth(ERROR, "Saved folder row has no path", null, Misc.getLineNumber(), true);
                        return null;
                    }
                    SavedFolderInfos savedFolder = new SavedFolderInfos(
                            path,
                            rs.getString("tableType"),
                            rs.getString("justFolderName"),
                            Files.exists(Paths.get(path)),
                            rs.getString("driveIdentifier"),
                            rs.getString("driveSerial"),
                            rs.getLong("driveSize"),
                            rs.getString("partitionUuid"),
                            rs.getString("partitionIdentification"));
                    savedFolderInfos.add(savedFolder);
                }
            }
            return savedFolderInfos;
        } catch (Exception e) {
            Messages.sprintfError("Unable to load saved folder information: " + e.getMessage());
            return null;
        }
    }

    /*
     * FolderInfos
     */
    public static boolean createSavedFoldersIntoConfigurationDatabase(Connection connection) {
        Messages.sprintf("createFolderInfosDatabase: " + SQL_Utils.getUrl(connection));

        if (!SQL_Utils.isDbConnected(connection)) {
            Messages.errorSmth(ConfigurationSavedFoldersDao.class.getSimpleName(), Main.bundle.getString("cannotCreateDatabase"), null, Misc.getLineNumber(), true);
            return false;
        }


        try {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(createSavedFolderTable);
            }
            ensureDriveIdentityColumns(connection);
            return true;
        } catch (Exception ex) {
            Messages.sprintfError("Unable to create or upgrade saved folder table: " + ex.getMessage());
            return false;
        }
    }

    private static void ensureDriveIdentityColumns(Connection connection) throws Exception {
        String tableName = SQLTableEnums.SAVED_FOLDERS.getType();
        Set<String> columns = new HashSet<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("PRAGMA table_info(" + tableName + ")")) {
            while (resultSet.next()) {
                columns.add(resultSet.getString("name"));
            }
        }

        String[][] identityColumns = {
                {"driveIdentifier", "TEXT"},
                {"driveSerial", "TEXT"},
                {"driveSize", "INTEGER NOT NULL DEFAULT 0"},
                {"partitionUuid", "TEXT"},
                {"partitionIdentification", "TEXT"}
        };
        try (Statement alter = connection.createStatement()) {
            for (String[] column : identityColumns) {
                if (!columns.contains(column[0])) {
                    alter.execute("ALTER TABLE " + tableName + " ADD COLUMN " + column[0] + " " + column[1]);
                }
            }
        }
    }

    public static boolean dropTable() {
        Connection connection = ConfigurationSQLHandler.getConnection();

        if (!SQL_Utils.isDbConnected(connection)) {
            Messages.errorSmth(ConfigurationSavedFoldersDao.class.getSimpleName(), Main.bundle.getString("cannotCreateDatabase"), null, Misc.getLineNumber(), true);
            return false;
        }

        String sql = "DROP TABLE IF EXISTS " + SQLTableEnums.SAVED_FOLDERS.getType();
        try {
            Statement stmt = connection.createStatement();
            stmt.execute(sql);
            stmt.close();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
