package com.girbola.workdir;

import com.girbola.Main;
import com.girbola.controllers.main.SQLTableEnums;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.fileinfo.*;
import com.girbola.messages.Messages;
import com.girbola.sql.*;

import com.girbola.utils.*;
import java.nio.file.*;
import java.sql.*;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

import static com.girbola.sql.FileInfo_SQL.createFileInfoTable;

@Getter
@Setter
public class WorkDirSQL {
    private static Path folder;

    private static List<FolderInfo> folderInfo_list = new ArrayList<>();
    private FolderInfo folderInfo = new FolderInfo();

    private List<FileInfo> fileInfo_list = new ArrayList<>();

    private static Connection workDirConnection;

    final static String fileInfoSQL =
            FileInfoEnum.BAD.getColumnName() + ", " +
                    FileInfoEnum.CAMERA_MODEL.getColumnName() + ", " +
                    FileInfoEnum.CONFIRMED.getColumnName() + ", " +
                    FileInfoEnum.DATE.getColumnName() + ", " +
                    FileInfoEnum.DESTINATION_PATH.getColumnName() + ", " +
                    FileInfoEnum.EVENT.getColumnName() + ", " +
                    FileInfoEnum.FILE_INFO_ID.getColumnName() + ", " +
                    FileInfoEnum.GOOD.getColumnName() + ", " +
                    FileInfoEnum.IGNORED.getColumnName() + ", " +
                    FileInfoEnum.IMAGE.getColumnName() + ", " +
                    FileInfoEnum.IMAGE_DIFFERENCE_HASH.getColumnName() + ", " +
                    FileInfoEnum.LOCAL_DATE_TIME.getColumnName() + ", " +
                    FileInfoEnum.LOCATION.getColumnName() + ", " +
                    FileInfoEnum.ORG_PATH.getColumnName() + ", " +
                    FileInfoEnum.ORIENTATION.getColumnName() + ", " +
                    FileInfoEnum.RAW.getColumnName() + ", " +
                    FileInfoEnum.SIZE.getColumnName() + ", " +
                    FileInfoEnum.SUGGESTED.getColumnName() + ", " +
                    FileInfoEnum.TABLE_DUPLICATED.getColumnName() + ", " +
                    FileInfoEnum.TAGS.getColumnName() + ", " +
                    FileInfoEnum.THUMB_LENGTH.getColumnName() + ", " +
                    FileInfoEnum.THUMB_OFFSET.getColumnName() + ", " +
                    FileInfoEnum.TIME_SHIFT.getColumnName() +
                    FileInfoEnum.USER.getColumnName() + ", " +
                    FileInfoEnum.VIDEO.getColumnName() + ", " +
                    FileInfoEnum.WORK_DIR.getColumnName() + ", " +
                    FileInfoEnum.WORK_DIR_DRIVE_SERIAL_NUMBER.getColumnName();


    public WorkDirSQL() {
    }

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

        workDirConnection = SqliteConnection.connector(workDir, SQLTableEnums.WORKDIR.getType());
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


    private static boolean createWorkDirTable(Connection connection) {
        try {
            Statement stmt = connection.createStatement();
            String createTableSql = "CREATE TABLE IF NOT EXISTS " + SQLTableEnums.WORKDIR.getType() + " ("
                    + "fileInfo_id INTEGER PRIMARY KEY, "
                    + "orgPath STRING UNIQUE, "
                    + "destination_Path STRING, "
                    + "event STRING, "
                    + "location STRING, "
                    + "orientation INTEGER, "
                    + "tags STRING, "
                    + "camera_model STRING, "
                    + "bad BOOLEAN, "
                    + "good BOOLEAN, "
                    + "suggested BOOLEAN, "
                    + "confirmed BOOLEAN, "
                    + "ignored BOOLEAN, "
                    + "tableDuplicated BOOLEAN, "
                    + "raw BOOLEAN, "
                    + "image BOOLEAN, "
                    + "video BOOLEAN, "
                    + "date INTEGER, "
                    + "size INTEGER, "
                    + "thumb_offset INTEGER, "
                    + "thumb_length INTEGER, "
                    + "imageDifferenceHash STRING, "
                    + "user STRING, "
                    + "workDir STRING, "
                    + "workDirDriveSerialNumber STRING, "
                    + "localDateTime TEXT, "
                    + "timeShift INTEGER"
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

        try {
            // Try to get or create connection if needed
            if (workDirConnection == null || workDirConnection.isClosed()) {
                workDirConnection = SqliteConnection.connector(Paths.get(Main.conf.getWorkDir()), SQLTableEnums.WORKDIR.getType());
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

            final String sql = "INSERT INTO " + SQLTableEnums.WORKDIR.getType() + FileInfoEnum.getAllFileInfoEnumValues() + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
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
            pstmt.setObject(index++, fileInfo.getLocalDateTime());
            pstmt.setLong(index++, fileInfo.getTimeShift());

            pstmt.executeUpdate();
            Messages.sprintf("FileInfo inserted/updated successfully");
        } catch (SQLException e) {
            Messages.sprintfError("Error inserting/updating FileInfo: " + e.getMessage());
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


    public static void insertFileInfo_(FileInfo fileInfo) {
        Messages.sprintf("insertFileInfo starting: " + fileInfo);
        if (fileInfo == null) {
            Messages.warningText("Cannot insert null FileInfo");
            return;
        }

        try {
            // Try to get or create connection if needed
            if (workDirConnection == null || workDirConnection.isClosed()) {
                workDirConnection = SqliteConnection.connector(Paths.get(Main.conf.getWorkDir()), SQLTableEnums.WORKDIR.getType());
                if (workDirConnection == null) {
                    throw new SQLException("Could not create database connection");
                }

                // Verify connection is valid
                if (!SQL_Utils.isDbConnected(workDirConnection)) {
                    throw new SQLException("Database connection validation failed");
                }
            }

            final String sql = "INSERT INTO " + SQLTableEnums.WORKDIR.getType() + FileInfoEnum.getAllFileInfoEnumValues() + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
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
            pstmt.setObject(index++, fileInfo.getLocalDateTime());
            pstmt.setLong(index++, fileInfo.getTimeShift());

            pstmt.executeUpdate();
            Messages.sprintf("FileInfo inserted/updated successfully");
        } catch (SQLException e) {
            Messages.sprintfError("Error inserting/updating FileInfo: " + e.getMessage());
        } finally {
            // Don't close the connection here since it may be needed for other operations
            // Instead, let the connection pool or application lifecycle manage the connection
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

    final static String[] fileInfoColumnsSQL = {(
            FileInfoConstants.FILEINFOID + " INTEGER PRIMARY KEY, " +
                    FileInfoConstants.ORG_PATH + " STRING UNIQUE, " +
                    FileInfoConstants.WORK_DIR + " STRING, " +
                    FileInfoConstants.WORK_DIR_DRIVE_SERIAL_NUMBER + " STRING, " +
                    FileInfoConstants.DESTINATIONPATH + " STRING, " +
                    FileInfoConstants.EVENT + " STRING, " +
                    FileInfoConstants.LOCATION + " STRING, " +
                    FileInfoConstants.TAGS + " STRING, " +
                    FileInfoConstants.CAMERA_MODEL + " STRING, " +
                    FileInfoConstants.USER + " STRING, " +
                    FileInfoConstants.ORIENTATION + " INTEGER, " +
                    FileInfoConstants.TIMESHIFT + " INTEGER, " +
                    FileInfoConstants.BAD + " BOOLEAN, " +
                    FileInfoConstants.GOOD + " BOOLEAN, " +
                    FileInfoConstants.SUGGESTED + " BOOLEAN, " +
                    FileInfoConstants.CONFIRMED + " BOOLEAN, " +
                    FileInfoConstants.COPIED + " BOOLEAN, " +
                    FileInfoConstants.IGNORED + " BOOLEAN, " +
                    FileInfoConstants.TABLE_DUPLICATED + " BOOLEAN, " +
                    FileInfoConstants.IMAGE + " BOOLEAN, " +
                    FileInfoConstants.VIDEO + " BOOLEAN, " +
                    FileInfoConstants.RAW + " BOOLEAN, " +
                    FileInfoConstants.DATE + " NUMERIC, " +
                    FileInfoConstants.SIZE + " NUMERIC, " +
                    FileInfoConstants.IMAGE_DIFFERENCE_HASH + " INTEGER, " +
                    FileInfoConstants.THUMB_OFFSET + " INTEGER, " +
                    FileInfoConstants.THUMB_LENGTH + " INTEGER, " +
                    FileInfoConstants.FILEHISTORIES + " STRING"
    )};

    /*
     * FileInfo
     */
    // @formatter:off
    public static boolean createFileInfoTable(Connection connection) {
        if (!SQL_Utils.isDbConnected(connection)) {
            Messages.sprintfError("Database connection is not active. Aborting the operation. Path is?" + SQL_Utils.getUrl(connection));
            return false;
        }

        final String sql = "CREATE TABLE IF NOT EXISTS " + SQLTableEnums.FILEINFO.getType() + " ("
                + String.join(",", fileInfoColumnsSQL) +");";

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

        workDirConnection = SqliteConnection.connector(Paths.get(Main.conf.getWorkDir()), Main.conf.getWorkDir_db_fileName());

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

        String sql = "SELECT " + FileInfoEnum.getAllFileInfoEnumValues() +
                " FROM " + SQLTableEnums.WORKDIR.getType() +
                " WHERE orgPath = ? AND size = ? AND localDateTime = ? AND imageDifferenceHash = ?";

        try (PreparedStatement pstmt = workDirConnection.prepareStatement(sql)) {
            pstmt.setString(1, fileInfo.getOrgPath());
            pstmt.setLong(2, fileInfo.getSize());
            pstmt.setObject(3, fileInfo.getLocalDateTime());
            pstmt.setString(4, fileInfo.getImageDifferenceHash());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    FileInfo duplicateFileInfo = populateFileInfoFromResultSet(rs);
                    if (duplicateFileInfo != null && FileInfoUtils.compareImagesMetadata(fileInfo, duplicateFileInfo)) {
                        list.add(duplicateFileInfo);
                    }
                }
            }
        } catch (SQLException e) {
            Messages.sprintfError("Error finding duplicate FileInfo: " + e.getMessage());
            return new ArrayList<>();
        }

        return list;
    }
    public  static List<FileInfo> findDuplicateByExactDate_(FileInfo fileInfo) {
        LocalDateTime date = fileInfo.getLocalDateTime();
        workDirConnection = SqliteConnection.connector(Paths.get(Main.conf.getWorkDir()), Main.conf.getWorkDir_db_fileName());
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

        String sql = "SELECT " + FileInfoEnum.getAllFileInfoEnumValues() + " FROM " + SQLTableEnums.WORKDIR.getType() + " WHERE orgPath = ? AND size = ? AND localDateTime = ? AND imageDifferenceHash = ?";
        try (PreparedStatement pstmt = workDirConnection.prepareStatement(sql)) {
            pstmt.setString(1, fileInfo.getOrgPath());
            pstmt.setLong(2, fileInfo.getSize());
            pstmt.setObject(3, fileInfo.getLocalDateTime());
            pstmt.setString(4, fileInfo.getImageDifferenceHash());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    duplicateFileInfo = populateFileInfoFromResultSet(rs);

                    if (duplicateFileInfo != null) {
                        if (FileInfoUtils.compareImagesMetadata(fileInfo, duplicateFileInfo)) {
                            list.add(duplicateFileInfo);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            Messages.sprintfError("Error finding duplicate FileInfo: " + e.getMessage());
            return null;
        }
        if (duplicateFileInfo != null) {
            Messages.warningText("duplicateFileInfo were not null. This under constructor");
        }
        return list;

    }

    private  static FileInfo populateFileInfoFromResultSet(ResultSet rs) throws SQLException {
        if (!rs.next()) {
            return null;
        }

        FileInfo fileInfo = new FileInfo();
        fileInfo.setBad(rs.getBoolean(FileInfoEnum.BAD.getColumnName()));
        fileInfo.setCamera_model(rs.getString(FileInfoEnum.CAMERA_MODEL.getColumnName()));
        fileInfo.setConfirmed(rs.getBoolean(FileInfoEnum.CONFIRMED.getColumnName()));
        fileInfo.setDate(rs.getLong(FileInfoEnum.DATE.getColumnName()));
        fileInfo.setDestination_Path(rs.getString(FileInfoEnum.DESTINATION_PATH.getColumnName()));
        fileInfo.setEvent(rs.getString(FileInfoEnum.EVENT.getColumnName()));
        fileInfo.setFileInfo_id(rs.getInt(FileInfoEnum.FILE_INFO_ID.getColumnName()));
        fileInfo.setGood(rs.getBoolean(FileInfoEnum.GOOD.getColumnName()));
        fileInfo.setIgnored(rs.getBoolean(FileInfoEnum.IGNORED.getColumnName()));
        fileInfo.setImage(rs.getBoolean(FileInfoEnum.IMAGE.getColumnName()));
        fileInfo.setImageDifferenceHash(rs.getString(FileInfoEnum.IMAGE_DIFFERENCE_HASH.getColumnName()));
        fileInfo.setLocalDateTime(rs.getObject(FileInfoEnum.LOCAL_DATE_TIME.getColumnName(), LocalDateTime.class));
        fileInfo.setLocation(rs.getString(FileInfoEnum.LOCATION.getColumnName()));
        fileInfo.setOrgPath(rs.getString(FileInfoEnum.ORG_PATH.getColumnName()));
        fileInfo.setOrientation(rs.getInt(FileInfoEnum.ORIENTATION.getColumnName()));
        fileInfo.setRaw(rs.getBoolean(FileInfoEnum.RAW.getColumnName()));
        fileInfo.setSize(rs.getLong(FileInfoEnum.SIZE.getColumnName()));
        fileInfo.setSuggested(rs.getBoolean(FileInfoEnum.SUGGESTED.getColumnName()));
        fileInfo.setTableDuplicated(rs.getBoolean(FileInfoEnum.TABLE_DUPLICATED.getColumnName()));
        fileInfo.setTags(rs.getString(FileInfoEnum.TAGS.getColumnName()));
        fileInfo.setThumb_length(rs.getInt(FileInfoEnum.THUMB_LENGTH.getColumnName()));
        fileInfo.setThumb_offset(rs.getInt(FileInfoEnum.THUMB_OFFSET.getColumnName()));
        fileInfo.setTimeShift(rs.getLong(FileInfoEnum.TIME_SHIFT.getColumnName()));
        fileInfo.setUser(rs.getString(FileInfoEnum.USER.getColumnName()));
        fileInfo.setVideo(rs.getBoolean(FileInfoEnum.VIDEO.getColumnName()));
        fileInfo.setWorkDir(rs.getString(FileInfoEnum.WORK_DIR.getColumnName()));
        fileInfo.setWorkDirDriveSerialNumber(rs.getString(FileInfoEnum.WORK_DIR_DRIVE_SERIAL_NUMBER.getColumnName()));
        return fileInfo;
    }

    public static  List<FileInfo> findDuplicateByDateRange(FileInfo fileInfo, String startDate, String endDate) {

        boolean dbConnected = SQL_Utils.isDbConnected(workDirConnection);
        if(!dbConnected) {
            return new ArrayList<>();
        }

        List<FileInfo> fileInfoDuplicates = new ArrayList<>();
        String sql =
                "SELECT " + fileInfoSQL + " FROM " + SQLTableEnums.WORKDIR.getType() +
                        " WHERE orgPath = ? AND size = ? AND localDateTime BETWEEN ? AND ?";

        try (PreparedStatement pstmt = workDirConnection.prepareStatement(sql)) {
            pstmt.setString(1, fileInfo.getOrgPath());
            pstmt.setLong(2, fileInfo.getSize());
            pstmt.setString(3, startDate);
            pstmt.setString(4, endDate);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    populateFileInfoFromResultSet(fileInfo, rs);
                    fileInfoDuplicates.add(fileInfo);
                }
            }
        } catch (SQLException e) {
            Messages.sprintfError("Error finding duplicate FileInfo: " + e.getMessage());
            return new ArrayList<>();
        }

        return fileInfoDuplicates;
    }

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

    private  static void populateFileInfoFromResultSet(FileInfo fileInfo, ResultSet rs) throws SQLException {
        fileInfo.setBad(rs.getBoolean(FileInfoEnum.BAD.getColumnName()));
        fileInfo.setCamera_model(rs.getString(FileInfoEnum.CAMERA_MODEL.getColumnName()));
        fileInfo.setConfirmed(rs.getBoolean(FileInfoEnum.CONFIRMED.getColumnName()));
        fileInfo.setDate(rs.getLong(FileInfoEnum.DATE.getColumnName()));
        fileInfo.setDestination_Path(rs.getString(FileInfoEnum.DESTINATION_PATH.getColumnName()));
        fileInfo.setEvent(rs.getString(FileInfoEnum.EVENT.getColumnName()));
        fileInfo.setFileInfo_id(rs.getInt(FileInfoEnum.FILE_INFO_ID.getColumnName()));
        fileInfo.setGood(rs.getBoolean(FileInfoEnum.GOOD.getColumnName()));
        fileInfo.setIgnored(rs.getBoolean(FileInfoEnum.IGNORED.getColumnName()));
        fileInfo.setImage(rs.getBoolean(FileInfoEnum.IMAGE.getColumnName()));
        fileInfo.setImageDifferenceHash(rs.getString(FileInfoEnum.IMAGE_DIFFERENCE_HASH.getColumnName()));
        fileInfo.setLocalDateTime(rs.getObject(FileInfoEnum.LOCAL_DATE_TIME.getColumnName(), LocalDateTime.class));
        fileInfo.setLocation(rs.getString(FileInfoEnum.LOCATION.getColumnName()));
        fileInfo.setOrgPath(rs.getString(FileInfoEnum.ORG_PATH.getColumnName()));
        fileInfo.setOrientation(rs.getInt(FileInfoEnum.ORIENTATION.getColumnName()));
        fileInfo.setRaw(rs.getBoolean(FileInfoEnum.RAW.getColumnName()));
        fileInfo.setSize(rs.getLong(FileInfoEnum.SIZE.getColumnName()));
        fileInfo.setSuggested(rs.getBoolean(FileInfoEnum.SUGGESTED.getColumnName()));
        fileInfo.setTableDuplicated(rs.getBoolean(FileInfoEnum.TABLE_DUPLICATED.getColumnName()));
        fileInfo.setTags(rs.getString(FileInfoEnum.TAGS.getColumnName()));
        fileInfo.setThumb_length(rs.getInt(FileInfoEnum.THUMB_LENGTH.getColumnName()));
        fileInfo.setThumb_offset(rs.getInt(FileInfoEnum.THUMB_OFFSET.getColumnName()));
        fileInfo.setTimeShift(rs.getLong(FileInfoEnum.TIME_SHIFT.getColumnName()));
        fileInfo.setUser(rs.getString(FileInfoEnum.USER.getColumnName()));
        fileInfo.setVideo(rs.getBoolean(FileInfoEnum.VIDEO.getColumnName()));
        fileInfo.setWorkDir(rs.getString(FileInfoEnum.WORK_DIR.getColumnName()));
        fileInfo.setWorkDirDriveSerialNumber(rs.getString(FileInfoEnum.WORK_DIR_DRIVE_SERIAL_NUMBER.getColumnName()));
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
