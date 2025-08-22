package com.girbola.sql;

import com.girbola.Main;
import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.SQLTableEnums;
import com.girbola.controllers.main.tables.model.StoredFolderInfoStatus;
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

public class SavedFolderInfosSQL {

    private static final String ERROR = SavedFolderInfosSQL.class.getSimpleName();

    //@formatter:off
    private static final String insertToFolderInfos =
            "INSERT OR REPLACE INTO " +
                    SQLTableEnums.FOLDERINFOS.getType() +
                    " (" +
                    "'path', " +
                    "'tableType', " +
                    "'justFolderName', " +
                    "'connected')" +
                    " VALUES(?,?,?,?)";

    //@formatter:on
    public static boolean insertSavedFolderInfoToDatabase(Connection connection, StoredFolderInfoStatus storedFolderInfoStatus) {
        if (connection == null) {
            return false;
        }
        createSavedFolderInfosDatabase(connection);
        try {
            PreparedStatement pstmt = connection.prepareStatement(insertToFolderInfos);
            pstmt.setString(1, storedFolderInfoStatus.getFolderPath());
            pstmt.setString(2, storedFolderInfoStatus.getTableType());
            pstmt.setString(3, storedFolderInfoStatus.getJustFolderName());
            pstmt.setBoolean(4, storedFolderInfoStatus.isConnected());
            pstmt.executeUpdate();
            pstmt.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public static List<StoredFolderInfoStatus> fetchAllSavedFolderInfosFromDatabase(Connection connection, ModelMain model_Main) {
        if (Main.getProcessCancelled()) {
            return null;
        }

        if (!SQL_Utils.isDbConnected(connection)) {
            Messages.sprintf("NOT Connected!");
        }

        String sql = "SELECT * FROM " + SQLTableEnums.FOLDERINFOS.getType();
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            List<StoredFolderInfoStatus> arrayList = new ArrayList<>();

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
                StoredFolderInfoStatus storedFolderInfoStatus = new StoredFolderInfoStatus(path, tableType, justFolderName, isConnected);
                storedFolderInfoStatus.setConnected(Files.exists(Paths.get(path)));
                Messages.sprintf("path: " + path + " FolderInfos.db were connected? " + storedFolderInfoStatus.isConnected());
                arrayList.add(storedFolderInfoStatus);
            }
            Messages.sprintf("getALLLLLL size was: " + arrayList.size());
            return arrayList;
        } catch (Exception e) {
            //SQL_Utils.closeConnection(connection);
            return null;
        }

    }


    /*
     * FolderInfos
     */
    public static boolean createSavedFolderInfosDatabase(Connection connection) {
        Messages.sprintf("createFolderInfosDatabase: " + SQL_Utils.getUrl(connection));

        if (!SQL_Utils.isDbConnected(connection)) {
            Messages.errorSmth(SavedFolderInfosSQL.class.getSimpleName(), Main.bundle.getString("cannotCreateDatabase"), null, Misc.getLineNumber(), true);
            return false;
        }

        String sql = "CREATE TABLE IF NOT EXISTS " + SQLTableEnums.FOLDERINFOS.getType() + " (path STRING NOT NULL PRIMARY KEY UNIQUE, " + "justFolderName STRING, " + "tableType STRING NOT NULL, " + "connected BOOLEAN)";

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
