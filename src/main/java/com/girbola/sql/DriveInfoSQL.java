package com.girbola.sql;

import com.girbola.controllers.folderscanner.ModelFolderScanner;
import com.girbola.controllers.main.SQL_Enums;
import com.girbola.drive.DriveInfo;
import com.girbola.messages.Messages;

import com.girbola.misc.Misc;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import static com.girbola.sql.SQL_Utils.isDbConnected;

public class DriveInfoSQL implements SQLInterface {

    private final static String ERROR = DriveInfoSQL.class.getSimpleName();

    private static Connection configurationConnection;

    public static final String TABLE_NAME = "DriveInfo";

    private static final String createDriveInfoTable =
            "CREATE TABLE IF NOT EXISTS " +
                    SQL_Enums.DRIVEINFO.getType() +
                    " (" +
                    "drivePath STRING NOT NULL, " +
                    "driveTotalSize INTEGER, " +
                    "identifier STRING, " +
                    "driveSelected STRING," +
                    "driveConnected BOOLEAN)";

    private static final String insertDriveInfo =
            "INSERT OR REPLACE INTO " +
                    SQL_Enums.DRIVEINFO.getType() +
                    "('drivePath', " +
                    "'identifier', " +
                    "'totalSize', " +
                    "'connected,' " +
                    "'selected')" +
                    " VALUES(?,?,?,?)";

    public DriveInfoSQL(Connection configurationConnection) {
        this.configurationConnection = configurationConnection;
    }

    public static boolean addDriveInfos(List<DriveInfo> driveInfos) {
        if (isDbConnected(configurationConnection)) {
            return false;
        }

        try {
            boolean driveInfoTable = createDriveInfoTable(configurationConnection);
            if(!driveInfoTable) {
                Messages.sprintfError("DriveInfo table was not created!");
                Messages.errorSmth(ERROR,"", null, Misc.getLineNumber(), false);
                return false;
            }
            PreparedStatement pstmt = configurationConnection.prepareStatement(insertDriveInfo);
            for (DriveInfo driveInfo : driveInfos) {
                pstmt.setString(1, driveInfo.getDrivePath());
                pstmt.setBoolean(2, driveInfo.isConnected());
                pstmt.setBoolean(3, driveInfo.isSelected());
                pstmt.setLong(4, driveInfo.getDriveTotalSize());
                pstmt.setString(5, driveInfo.getIdentifier());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            if (pstmt != null) {
                pstmt.close();
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // @formatter:on
    public static List<DriveInfo> loadDriveInfo(Connection connection) {
        List<DriveInfo> driveInfos = null;

        String sql = "SELECT * FROM " + SQL_Enums.DRIVEINFO.getType();
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                String drivePath = rs.getString("drivePath");
                boolean isConnected = rs.getBoolean("driveConnected");
                boolean isSelected = rs.getBoolean("driveSelected");
                long driveTotalSize = rs.getLong("driveTotalSize");
                String identifier = rs.getString("identifier");

                DriveInfo driveInfo = new DriveInfo(drivePath, driveTotalSize, isConnected, isSelected, identifier);
                driveInfos.add(driveInfo);
            }
            return driveInfos;
        } catch (Exception e) {
            Messages.sprintfError("Failed to load DriveInfo from database");
            return null;
        }
    }


    /*
     * DriveInfo
     */
    private static boolean createDriveInfoTable(Connection connection) {
        return false;
    }

    @Override
    public boolean save(List<DriveInfo> driveInfos) {
        return false;
    }

    @Override
    public List<DriveInfo> load() {
        return loadDriveInfo(this.configurationConnection);
    }

    @Override
    public boolean delete() {
        return false;
    }

    @Override
    public boolean update(List<DriveInfo> driveInfos) {
        return addDriveInfos(driveInfos);
    }

    @Override
    public boolean create() {
        return false;
    }

    @Override
    public boolean insert() {
        return false;
    }

    @Override
    public Connection getConfigurationConnection() {
        return this.configurationConnection;
    }

    @Override
    public boolean isConnected() {
        return SQL_Utils.isDbConnected(this.configurationConnection);
    }
}
