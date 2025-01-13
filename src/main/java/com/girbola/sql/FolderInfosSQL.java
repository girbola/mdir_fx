package com.girbola.sql;

import com.girbola.Main;
import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.SQL_Enums;
import com.girbola.controllers.main.tables.model.SavedFolderInfo;
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

public class FolderInfosSQL {

    private static final String ERROR = FolderInfosSQL.class.getSimpleName();

    //@formatter:off
    final static String insertToFolderInfos =
            "INSERT OR REPLACE INTO " +
                    SQL_Enums.FOLDERINFOS.getType() +
                    " (" +
                    "'path', " +
                    "'tableType', " +
                    "'justFolderName', " +
                    "'connected')" +
                    " VALUES(?,?,?,?)";

    //@formatter:on
    public static boolean addToFolderInfosDB(Connection connection, SavedFolderInfo savedFolderInfo) {
        if (connection == null) {
            return false;
        }
        createFolderInfosDatabase(connection);
        try {
            PreparedStatement pstmt = connection.prepareStatement(insertToFolderInfos);
            pstmt.setString(1, savedFolderInfo.getFolderPath());
            pstmt.setString(2, savedFolderInfo.getTableType());
            pstmt.setString(3, savedFolderInfo.getJustFolderName());
            pstmt.setBoolean(4, savedFolderInfo.isConnected());
            pstmt.executeUpdate();
            pstmt.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public static List<SavedFolderInfo> getAll(Connection connection, ModelMain model_Main) {
        if (Main.getProcessCancelled()) {
            return null;
        }

        if (!SQL_Utils.isDbConnected(connection)) {
            Messages.sprintf("NOT Connected!");
        }

        String sql = "SELECT * FROM " + SQL_Enums.FOLDERINFOS.getType();
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            List<SavedFolderInfo> arrayList = new ArrayList<>();

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
                SavedFolderInfo savedFolderInfo = new SavedFolderInfo(path, tableType, justFolderName, isConnected);
                savedFolderInfo.setConnected(Files.exists(Paths.get(path)));
                Messages.sprintf("path: " + path + " FolderInfos.db were connected? " + savedFolderInfo.isConnected());
                arrayList.add(savedFolderInfo);
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
    public static boolean createFolderInfosDatabase(Connection connection) {

        if (!SQL_Utils.isDbConnected(connection)) {
            Messages.errorSmth(FolderInfosSQL.class.getSimpleName(), Main.bundle.getString("cannotCreateDatabase"), null, Misc.getLineNumber(), true);
            return false;
        }

        String sql = "CREATE TABLE IF NOT EXISTS " + SQL_Enums.FOLDERINFOS.getType() + " (path STRING NOT NULL PRIMARY KEY UNIQUE, " + "justFolderName STRING, " + "tableType STRING NOT NULL, " + "connected BOOLEAN)";

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
