package com.girbola.workdir;

import com.girbola.Main;
import com.girbola.controllers.main.SQLTableEnums;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.fileinfo.FileInfo;
import com.girbola.fileinfo.FileInfoEnum;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.sql.FileInfo_SQL;
import com.girbola.sql.SQL_Utils;
import com.girbola.sql.SqliteConnection;
import com.girbola.utils.FileInfoUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkDirSQL {
    private static Path folder;

    private static List<FolderInfo> folderInfo_list = new ArrayList<>();
    private FolderInfo folderInfo = new FolderInfo();

    private List<FileInfo> fileInfo_list = new ArrayList<>();

    private static Connection workDirConnection;

    public WorkDirSQL(Path folder) {
        initWorkDirDatabase(folder);
    }

    public static void setFolder(Path folder) {
        initWorkDirDatabase(folder);
    }

    private static void initWorkDirDatabase(Path folder) {
        if (folder == null) {
            Messages.warningText("folder were null!!!");
            return;
        }
        Messages.sprintf("WorkDirSQL before loadWorkDirDatabase: " + folder);

        if (!Files.exists(folder)) {
            Messages.warningText("Workdir folder did not exists" + Main.bundle.getString("reconnectDrives") + " at path: " + folder);
        } else {

            Messages.sprintf("WorkDirSQL before loadWorkDirDatabase: " + folder);
            boolean b = loadWorkDirDatabase(folder);
            if (!b) {

                Messages.warningText("loading workdir database: " + Main.bundle.getString("reconnectDrives") + " at path: " + folder);
            }
        }
    }


    public static boolean loadWorkDirDatabase(Path workDir) {
        Messages.sprintf("loadWorkDirDatabase starting: " + workDir);
        if (workDir == null) {
            Messages.warningText(Main.bundle.getString("workDirHasNotBeenSet"));
            return false;
        }
        if (!Files.exists(workDir)) {
            Messages.warningText(Main.bundle.getString("workDirHasNotBeenSet"));
            return false;
        }

        workDirConnection = SqliteConnection.connectToDatabase(workDir, SQLTableEnums.WORKDIR.getType());
        boolean dbConnected = SQL_Utils.isDbConnected(workDirConnection);
        if (dbConnected) {
            Messages.sprintf("workDir loaded: " + workDir);
            return true;
        } else {
            Messages.sprintf("Can't find current workDir: " + workDir);
            return false;
        }

    }

    public static boolean saveWorkDirDatabase() {
        try {
            if (SQL_Utils.isDbConnected(workDirConnection)) {
                SQL_Utils.setAutoCommit(workDirConnection, true);
                SQL_Utils.closeConnection(workDirConnection);
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public static boolean createWorkDirTable(Connection connection) {
        try {
            Statement stmt = connection.createStatement();
            String createTableSql = "CREATE TABLE IF NOT EXISTS " + SQLTableEnums.WORKDIR.getType() + " ("
                    + "bad BOOLEAN, "
                    + "camera_model STRING, "
                    + "confirmed BOOLEAN, "
                    + "date INTEGER, "
                    + "destination_Path STRING, "
                    + "event STRING, "
                    + "fileInfo_id INTEGER PRIMARY KEY, "
                    + "good BOOLEAN, "
                    + "ignored BOOLEAN, "
                    + "image BOOLEAN, "
                    + "imageDifferenceHash STRING, "
                    + "location STRING, "
                    + "orgPath STRING UNIQUE, "
                    + "orientation INTEGER, "
                    + "raw BOOLEAN, "
                    + "size INTEGER, "
                    + "suggested BOOLEAN, "
                    + "tableDuplicated BOOLEAN, "
                    + "tags STRING, "
                    + "thumb_length INTEGER, "
                    + "thumb_offset INTEGER, "
                    + "timeShift INTEGER"
                    + "user STRING, "
                    + "video BOOLEAN, "
                    + "workDir STRING, "
                    + "workDirDriveSerialNumber STRING, "
                    + ")";
            stmt.execute(createTableSql);
            return true;
        } catch (SQLException e) {
            Messages.sprintfError("Error creating workdir table: " + e.getMessage());
            return false;
        }
    }

    public static void insertFileInfo(FileInfo fileInfo) {
        Messages.sprintf("insertFileInfo starting: " + fileInfo);
        if (fileInfo == null) {
            Messages.warningText("Cannot insert null FileInfo");
            return;
        }

        if(fileInfo.getOrgPath().contains("IMG_1551.JPG")) {
            Messages.sprintf("insertFileInfo starting: " + fileInfo);
        }
        try {
            // Try to get or create connection if needed
            if (workDirConnection == null || workDirConnection.isClosed()) {
                workDirConnection = SqliteConnection.connectToDatabase(Paths.get(Main.conf.getWorkDir()), SQLTableEnums.WORKDIR.getType());
                if (workDirConnection == null) {
                    throw new SQLException("Could not create database connection");
                }

                // Create table if it doesn't exist
                if (!createWorkDirTable(workDirConnection)) {
                    throw new SQLException("Failed to create workdir table");
                }

                // Verify connection is valid
                if (!SQL_Utils.isDbConnected(workDirConnection)) {
                    throw new SQLException("Database connection validation failed");
                }
            }

            final String sql = "INSERT INTO " + SQLTableEnums.WORKDIR.getType() + FileInfoEnum.getAllFileInfoColumnNames() + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                    + "ON CONFLICT(orgPath) DO UPDATE SET fileInfo_id = ?, destination_Path = ?, event = ?, location = ?, orientation = ?, tags = ?, "
                    + "camera_model = ?, bad = ?, good = ?, suggested = ?, confirmed = ?, ignored = ?, tableDuplicated = ?, raw = ?, image = ?, video = ?, "
                    + "date = ?, size = ?, thumb_offset = ?, thumb_length = ?, imageDifferenceHash = ?, user = ?, workDir = ?, workDirDriveSerialNumber = ?, "
                    + "localDateTime = ?, timeShift = ?";

            PreparedStatement pstmt = workDirConnection.prepareStatement(sql);
            int index = 1;

            // Update values
            pstmt.setInt(index++, fileInfo.getFileInfo_id());
            pstmt.setString(index++, fileInfo.getDestination_Path());
            pstmt.setString(index++, fileInfo.getEvent());
            pstmt.setString(index++, fileInfo.getLocation());
            pstmt.setInt(index++, fileInfo.getOrientation());
            pstmt.setString(index++, fileInfo.getTags());
            pstmt.setString(index++, fileInfo.getCamera_model());
            pstmt.setBoolean(index++, fileInfo.isBad());
            pstmt.setBoolean(index++, fileInfo.isGood());
            pstmt.setBoolean(index++, fileInfo.isSuggested());
            pstmt.setBoolean(index++, fileInfo.isConfirmed());
            pstmt.setBoolean(index++, fileInfo.isIgnored());
            pstmt.setBoolean(index++, fileInfo.isTableDuplicated());
            pstmt.setBoolean(index++, fileInfo.isRaw());
            pstmt.setBoolean(index++, fileInfo.isImage());
            pstmt.setBoolean(index++, fileInfo.isVideo());
            pstmt.setLong(index++, fileInfo.getDate());
            pstmt.setLong(index++, fileInfo.getSize());
            pstmt.setInt(index++, fileInfo.getThumb_offset());
            pstmt.setInt(index++, fileInfo.getThumb_length());
            pstmt.setString(index++, fileInfo.getImageDifferenceHash());
            pstmt.setString(index++, fileInfo.getUser());
            pstmt.setString(index++, fileInfo.getWorkDir());
            pstmt.setString(index++, fileInfo.getWorkDirDriveSerialNumber());
//            pstmt.setObject(index++, fileInfo.getLocalDateTime());
            pstmt.setLong(index++, fileInfo.getTimeShift());

            pstmt.executeUpdate();
            Messages.sprintf("2FileInfo inserted/updated successfully");
        } catch (SQLException e) {
            Messages.sprintfError("22Error inserting/updating FileInfo: " + e.getMessage());
        } finally {
            // Don't close the connection here since it may be needed for other operations
            try {
                if (workDirConnection != null && workDirConnection.isClosed()) {
                    SQL_Utils.closeConnection(workDirConnection);
                    workDirConnection = null;
                }
            } catch (SQLException e) {
                Messages.sprintfError("Error closing connection: " + e.getMessage());
            }
        }
    }

    public static boolean deleteFileInfo(FileInfo fileInfo) {
        if (fileInfo == null) {
            Messages.warningText("Cannot insert null FileInfo");
            return false;
        }
        try {
            String sql = "DELETE FROM " + SQLTableEnums.WORKDIR.getType() + " WHERE id = ?";
            PreparedStatement pstmt = workDirConnection.prepareStatement(sql);
            pstmt.setInt(1, fileInfo.getFileInfo_id());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Deleting fileInfo failed, no rows affected.");
            }
            Messages.sprintf("FileInfo were removed from table successfully. Filename was: " + fileInfo.getOrgPath());
            return true;
        } catch (SQLException sqlException) {
            System.err.println("SQL Exception occurred while deleting FileInfo: " + sqlException.getMessage());
            return false;
        }
    }
    // @formatter:off

    /*
     * FileInfo
     */
    public static boolean createFileInfoTable(Connection connection) {
        if (!SQL_Utils.isDbConnected(connection)) {
            Messages.sprintfError("Database connection is not active. Aborting the operation. Path is?" + SQL_Utils.getUrl(connection));
            return false;
        }

        final String sql = FileInfo_SQL.createFileInfoTable();

        try {
            Statement stmt = connection.createStatement();
            stmt.execute(sql);
            return true;
        } catch (Exception ex) {
            Messages.sprintfError("Cannot create table: " + sql);
            return false;
        }
    }


    public static List<FileInfo> findDuplicateByExactDate(FileInfo fileInfo) {
        if (fileInfo == null) {
            return new ArrayList<>();
        }

        workDirConnection = SqliteConnection.connectToDatabase(Paths.get(Main.conf.getWorkDir()), Main.conf.getWorkDir_db_fileName());

        // If connection failed or database doesn't exist, return empty list
        if (workDirConnection == null || !SQL_Utils.isDbConnected(workDirConnection)) {
            Messages.sprintf("Database connection failed for: " + Main.conf.getWorkDir());
            return new ArrayList<>();
        }

        // If table creation fails, return empty list
        if (!createFileInfoTable(workDirConnection)) {
            Messages.sprintf("Failed to create FileInfo table in: " + Main.conf.getWorkDir_db_fileName());
            return new ArrayList<>();
        }

        List<FileInfo> list = new ArrayList<>();

        String sql = "SELECT " + FileInfoEnum.getAllFileInfoColumnNames() +
                " FROM " + SQLTableEnums.WORKDIR.getType() +
                " WHERE orgPath = ? AND size = ? AND localDateTime = ? AND imageDifferenceHash = ?";

        try (PreparedStatement pstmt = workDirConnection.prepareStatement(sql)) {
            pstmt.setString(1, fileInfo.getOrgPath());
            pstmt.setLong(2, fileInfo.getSize());
//            pstmt.setObject(3, fileInfo.getLocalDateTime());
            pstmt.setString(3, fileInfo.getImageDifferenceHash());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    FileInfo duplicateFileInfo = FileInfo_SQL.loadFileInfo(rs);
                    if (duplicateFileInfo != null && FileInfoUtils.compareImagesMetadata(fileInfo, duplicateFileInfo)) {
                        list.add(duplicateFileInfo);
                    }
                }
            }
        } catch (SQLException e) {
            Messages.sprintfError("Error finding duplicate FileInfo: " + e.getMessage() + " line nubmer: " + Misc.getLineNumber());
            return new ArrayList<>();
        }

        return list;
    }
    public  static List<FileInfo> findDuplicateByExactDate_(FileInfo fileInfo) {
//        LocalDateTime date = fileInfo.getLocalDateTime();
        workDirConnection = SqliteConnection.connectToDatabase(Paths.get(Main.conf.getWorkDir()), Main.conf.getWorkDir_db_fileName());
        boolean fileInfoTable = createFileInfoTable(workDirConnection);
        if(!fileInfoTable) {
            Messages.sprintf("WorkDirSQL work dir connection findDuplicateByExactDate: " +Main.conf.getWorkDir_db_fileName());
        }

        if(!SQL_Utils.isDbConnected(workDirConnection)) {
            Messages.sprintf("WorkDirSQL work dir connection findDuplicateByExactDate: " + Main.conf.getWorkDir());
        }
        boolean b = loadWorkDirDatabase(Paths.get(Main.conf.getWorkDir()));
        if(!b) {
            Messages.sprintf("NOT CONNECTED TO DATABASE: " + Main.conf.getWorkDir());
            createFileInfoTable(workDirConnection);
        }

        List<FileInfo> list = new ArrayList<>();
        FileInfo duplicateFileInfo = null;

        String sql = "SELECT " + FileInfoEnum.getAllFileInfoColumnNames() + " FROM " + SQLTableEnums.WORKDIR.getType() + " WHERE orgPath = ? AND size = ? AND localDateTime = ? AND imageDifferenceHash = ?";
        try (PreparedStatement pstmt = workDirConnection.prepareStatement(sql)) {
            pstmt.setString(1, fileInfo.getOrgPath());
            pstmt.setLong(2, fileInfo.getSize());
//            pstmt.setObject(3, fileInfo.getLocalDateTime());
            pstmt.setString(4, fileInfo.getImageDifferenceHash());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    duplicateFileInfo = FileInfo_SQL.loadFileInfo(rs);

                    if (duplicateFileInfo != null) {
                        if (FileInfoUtils.compareImagesMetadata(fileInfo, duplicateFileInfo)) {
                            list.add(duplicateFileInfo);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            Messages.sprintfError("Error finding duplicate FileInfo: " + e.getMessage() + " line nubmer: " + Misc.getLineNumber());
            return null;
        }
        if (duplicateFileInfo != null) {
            Messages.warningText("duplicateFileInfo were not null. This under constructor");
        }
        return list;

    }

//    public static  List<FileInfo> findDuplicateByDateRange(FileInfo fileInfo, String startDate, String endDate) {
//
//        boolean dbConnected = SQL_Utils.isDbConnected(workDirConnection);
//        if(!dbConnected) {
//            return new ArrayList<>();
//        }
//
//        List<FileInfo> fileInfoDuplicates = new ArrayList<>();
//        String sql =
//                "SELECT " + fileInfoSQL + " FROM " + SQLTableEnums.WORKDIR.getType() +
//                        " WHERE orgPath = ? AND size = ? AND localDateTime BETWEEN ? AND ?";
//
//        try (PreparedStatement pstmt = workDirConnection.prepareStatement(sql)) {
//            pstmt.setString(1, fileInfo.getOrgPath());
//            pstmt.setLong(2, fileInfo.getSize());
//            pstmt.setString(3, startDate);
//            pstmt.setString(4, endDate);
//
//            try (ResultSet rs = pstmt.executeQuery()) {
//                while (rs.next()) {
//                    FileInfo fileInfo = populateFileInfoFromResultSet(rs);
//                    fileInfoDuplicates.add(fileInfo);
//                }
//            }
//        } catch (SQLException e) {
//            Messages.sprintfError("Error finding duplicate FileInfo: " + e.getMessage() + " line nubmer: " + Misc.getLineNumber());
//            return new ArrayList<>();
//        }
//
//        return fileInfoDuplicates;
//    }

    public static  List<FileInfo> findByExactDate(FileInfo fileInfo, String date) {
        String sql = "SELECT changed,connected,ignored,dateDifference,badFiles,confirmed,copied,folderFiles,folderImageFiles," +
                "folderRawFiles,folderVideoFiles,goodFiles,suggested,folderSize,justFolderName,folderPath,maxDate,minDate,state,tableType FROM "
                + SQLTableEnums.WORKDIR.getType() + "WHERE minDate" + date + " between ";

        return null;
    }

    public static  List<FileInfo> findByDateRange(FileInfo fileInfo, String date1, String date2, String date3) {

        return null;
    }

    public  static List<FolderInfo> getFolderInfo_list() {
        return folderInfo_list;
    }

    private  static FolderInfo loadFolderInfo() {

        SQL_Utils.setAutoCommit(workDirConnection, false);
        try {
            String sql = "SELECT * FROM " + SQLTableEnums.FOLDERINFO.getType();
            Statement smtm = workDirConnection.createStatement();
            ResultSet rs = smtm.executeQuery(sql);

            boolean changed = rs.getBoolean("changed");
            boolean connected = rs.getBoolean("connected");
            boolean ignored = rs.getBoolean("ignored");
            double dateDifference = rs.getDouble("dateDifference");
            int badFiles = rs.getInt("badFiles");
            int confirmed = rs.getInt("confirmed");
            int copied = rs.getInt("copied");
            int folderFiles = rs.getInt("folderFiles");
            int folderImageFiles = rs.getInt("folderImageFiles");
            int folderRawFiles = rs.getInt("folderRawFiles");
            int folderVideoFiles = rs.getInt("folderVideoFiles");
            int goodFiles = rs.getInt("goodFiles");
            int suggested = rs.getInt("suggested");
            long folderSize = rs.getLong("folderSize");
            String justFolderName = rs.getString("justFolderName");
            String folderPath = rs.getString("folderPath");
            String maxDate = rs.getString("maxDate");
            String minDate = rs.getString("minDate");
            String state = rs.getString("state");
            String tableType = rs.getString("tableType");

            List<FileInfo> fileInfo_list = FileInfo_SQL.loadFileInfoDatabase(workDirConnection);

            FolderInfo folderInfo = new FolderInfo();
            if (fileInfo_list.isEmpty()) {
                Messages.sprintf("FileInfo were empty!");
                fileInfo_list = FileInfoUtils.createFileInfo_list(folderInfo);
                if (fileInfo_list.isEmpty()) {
                    Messages.sprintf("FileInfo creationg did not work this time or folder were empty.");
                }
            }
            folderInfo.setBadFiles(badFiles);
            folderInfo.setChanged(changed);
            folderInfo.setConfirmed(confirmed);
            folderInfo.setConnected(connected);
            folderInfo.setCopied(copied);
            folderInfo.setDateDifferenceRatio(dateDifference);
            folderInfo.setFileInfoList(fileInfo_list);
            folderInfo.setFolderFiles(folderFiles);
            folderInfo.setFolderImageFiles(folderImageFiles);
            folderInfo.setFolderPath(folderPath);
            folderInfo.setFolderRawFiles(folderRawFiles);
            folderInfo.setFolderSize(folderSize);
            folderInfo.setFolderVideoFiles(folderVideoFiles);
            folderInfo.setGoodFiles(goodFiles);
            folderInfo.setIgnored(ignored);
            folderInfo.setJustFolderName(justFolderName);
            folderInfo.setMaxDate(maxDate);
            folderInfo.setMinDate(minDate);
            folderInfo.setState(state);
            folderInfo.setSuggested(suggested);
            folderInfo.setTableType(tableType);

            smtm.close();

            return folderInfo;

        } catch (Exception e) {
            Messages.sprintfError(Main.bundle.getString("cannotLoadFolderInfoFromDatabase"));
            return null;
        } finally {
            SQL_Utils.closeConnection(workDirConnection);
        }
    }

}
