package com.girbola.sql;

import com.girbola.Main;
import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.SQLTableEnums;
import com.girbola.controllers.main.tables.model.FolderInfoStatus;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ConfigurationSavedFoldersDao {

    private static final String ERROR = ConfigurationSavedFoldersDao.class.getSimpleName();

    //@formatter:off
    private static final String insertToFolderInfos =
            "INSERT OR REPLACE INTO " +
                    SQLTableEnums.SAVED_FOLDERS.getType() +
                    " (" +
                    "'path', " +
                    "'tableType', " +
                    "'justFolderName', " +
                    "'connected')" +
                    " VALUES(?,?,?,?)";

    /**
     * Inserts the information of a saved folder into the database.
     *
     * @param configurationDatabaseConnection the database connection object used for the operation.
     *                   This must not be null to perform the insertion.
     * @param folderInfoStatus an object containing folder details such as the folder path,
     *                                table type, folder name, and connection status.
     * @return true if the folder information was successfully inserted into the database,
     *         false otherwise (e.g., in case of null connection, database creation failure, or errors during insertion).
     */
 //@formatter:on
    public static boolean insertSavedFoldersIntoConfigurationDatabase(Connection configurationDatabaseConnection, FolderInfoStatus folderInfoStatus) {
        if (configurationDatabaseConnection == null) {
            Messages.sprintfError("insertSavedFolderInfoToDatabase Connection was null!");
            return false;
        }
        boolean savedFolderInfosDatabase = createSavedFoldersIntoConfigurationDatabase(configurationDatabaseConnection);
        if (!savedFolderInfosDatabase) {
            Messages.sprintfError("insertSavedFolderInfoToDatabase Could not create FolderInfos database!");
            return false;
        }
        try (PreparedStatement pstmt = configurationDatabaseConnection.prepareStatement(insertToFolderInfos)) {
            pstmt.setString(1, folderInfoStatus.getFolderPath());
            pstmt.setString(2, folderInfoStatus.getTableType());
            pstmt.setString(3, folderInfoStatus.getJustFolderName());
            pstmt.setBoolean(4, folderInfoStatus.isConnected());
            pstmt.executeUpdate();
            SQL_Utils.commitChanges(configurationDatabaseConnection);
            return true;
        } catch (Exception e) {
            Messages.sprintfError("insertSavedFolderInfoToDatabase error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    public static List<FolderInfoStatus> loadSavedFolderDetails(Connection connection, ModelMain model_Main) {
        if (Main.getProcessCancelled()) {
            return null;
        }

        if (!SQL_Utils.isDbConnected(connection)) {
            Messages.sprintf("NOT Connected!");
        }

        String sql = "SELECT * FROM " + SQLTableEnums.SAVED_FOLDERS.getType();
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            List<FolderInfoStatus> arrayList = new ArrayList<>();

            while (rs.next()) {
                if (Main.getProcessCancelled()) {
                    Messages.sprintf("getAll cancelled!");
                    return null;
                }
                String path = rs.getString("path");
                String tableType = rs.getString("tableType");
                String justFolderName = rs.getString("justFolderName");
                boolean isConnected = rs.getBoolean("connected");
                if (path == null) {
                    Messages.sprintf("Something went badly wrong!");
                    Messages.errorSmth(ERROR, "Something went terrible wrong at: " + path, null, Misc.getLineNumber(), true);
                    return null;
                }
                FolderInfoStatus folderInfoStatus = new FolderInfoStatus(path, tableType, justFolderName, isConnected);
                folderInfoStatus.setConnected(Files.exists(Paths.get(path)));
                arrayList.add(folderInfoStatus);
            }
            return arrayList;
        } catch (Exception e) {
            //SQL_Utils.closeConnection(connection);
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

        String sql = "CREATE TABLE IF NOT EXISTS " + SQLTableEnums.SAVED_FOLDERS.getType() + " (path STRING NOT NULL PRIMARY KEY UNIQUE, " + "justFolderName STRING, " + "tableType STRING NOT NULL, " + "connected BOOLEAN)";
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
