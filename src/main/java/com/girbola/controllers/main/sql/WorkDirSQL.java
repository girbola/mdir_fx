package com.girbola.controllers.main.sql;

import com.girbola.Main;
import com.girbola.controllers.main.SQLTableEnums;
import com.girbola.controllers.operate.CopyState;
import com.girbola.fileinfo.FileInfo;
import com.girbola.fileinfo.FileInfoEnum;
import com.girbola.messages.Messages;
import com.girbola.sql.FileInfoConstants;
import com.girbola.sql.FileInfo_SQL;
import com.girbola.sql.SQL_Utils;
import com.girbola.sql.SqliteConnection;
import com.girbola.utils.FileInfoUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.girbola.Main.simpleDates;

public class WorkDirSQL {

    private Connection connection;

    public Connection getConnection() {
        return connection;
    }

    public final String[] fileInfoColumnsSQL = {(FileInfoConstants.FILEINFOID + " INTEGER PRIMARY KEY, " + FileInfoConstants.ORG_PATH + " STRING UNIQUE, " + FileInfoConstants.WORK_DIR + " STRING, " + FileInfoConstants.WORK_DIR_DRIVE_SERIAL_NUMBER + " STRING, " + FileInfoConstants.DESTINATIONPATH + " STRING, " + FileInfoConstants.EVENT + " STRING, " + FileInfoConstants.LOCATION + " STRING, " + FileInfoConstants.TAGS + " STRING, " + FileInfoConstants.CAMERA_MODEL + " STRING, " + FileInfoConstants.USER + " STRING, " + FileInfoConstants.ORIENTATION + " INTEGER, " + FileInfoConstants.TIMESHIFT + " INTEGER, " + FileInfoConstants.BAD + " BOOLEAN, " + FileInfoConstants.GOOD + " BOOLEAN, " + FileInfoConstants.SUGGESTED + " BOOLEAN, " + FileInfoConstants.CONFIRMED + " BOOLEAN, " + FileInfoConstants.COPIED + " BOOLEAN, " + FileInfoConstants.IGNORED + " BOOLEAN, " + FileInfoConstants.TABLE_DUPLICATED + " BOOLEAN, " + FileInfoConstants.IMAGE + " BOOLEAN, " + FileInfoConstants.VIDEO + " BOOLEAN, " + FileInfoConstants.RAW + " BOOLEAN, " + FileInfoConstants.DATE + " NUMERIC, " + FileInfoConstants.SIZE + " NUMERIC, " + FileInfoConstants.IMAGE_DIFFERENCE_HASH + " INTEGER, " + FileInfoConstants.THUMB_OFFSET + " INTEGER, " + FileInfoConstants.THUMB_LENGTH + " INTEGER, " + FileInfoConstants.FILEHISTORIES + " STRING")};

    public WorkDirSQL(Path workDirPath) {
        if (Files.exists(workDirPath)) {
            this.connection = createWorkDirConnection(workDirPath);
        }
    }

    private Connection createWorkDirConnection(Path workDirPath) {
        try {
            connection = SqliteConnection.connectToDatabase(workDirPath, Main.conf.getWorkDir_db_fileName());
            return connection;
        } catch (Exception e) {
            Messages.sprintfError("Error connecting to database: " + Main.conf.getWorkDir_db_fileName());
            return null;
        }
    }


    // @formatter:off
    public boolean createFileInfoTable(Connection connection) {
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

    public void checkConnection() {
        try {
            // Check if the connection is valid
            if (!SQL_Utils.isDbConnected(connection)) {
                Messages.sprintf("Configuration database closed");
                connection = createWorkDirConnection(Main.conf.getAppDataPath());
                if (connection == null) {
                    Messages.sprintfError("Failed to create new configuration connection");
                    return;
                }
                SQL_Utils.setAutoCommit(connection, false);
                Messages.sprintf("Configuration database opened: " + SQL_Utils.getUrl(connection));
            }

            // Ensure AutoCommit is disabled
            if (connection.getAutoCommit()) {
                SQL_Utils.setAutoCommit(connection, false);
            }

        } catch (SQLException e) {
            Messages.sprintfError("Error checking the database connection: " + e.getMessage());
        }
    }

    public void closeConnection() {
        SQL_Utils.closeConnection(connection);
    }

    public List<FileInfo> findDuplicatesByDateRange(String startDate, String endDate) {
        List<FileInfo> duplicates = new ArrayList<>();
        String sql = "SELECT * FROM " + SQLTableEnums.WORKDIR.getType() + " WHERE date_range BETWEEN ? AND ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, startDate);
            pstmt.setString(2, endDate);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Map ResultSet to FileInfo objects and add to duplicates list
                    // Implementation depends on your FileInfo class structure
                }
            }
        } catch (SQLException e) {
            Messages.sprintfError("Error finding duplicates: " + e.getMessage());
            // Consider throwing a custom exception or handling the error appropriately
        }

        return duplicates;
    }


    public void loadWorkDir() {
        checkConnection();
    }

    public void ensureFileInfoTable() {
        FileInfo_SQL.createFileInfoTable(connection);
    }

    public void insertToWorkDir(FileInfo fileInfo) {
        String sql = "INSERT INTO " + SQLTableEnums.WORKDIR.getType() + " ('path') VALUES(?)";
        boolean fileInfoTable = FileInfo_SQL.createFileInfoTable(connection);
        if (!fileInfoTable) {
            Messages.sprintfError("Could not create fileinfo workdir table");
            return;
        }
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, fileInfo.getOrgPath());
            pstmt.addBatch();
            pstmt.executeBatch();
            SQL_Utils.commitChanges(connection);
        } catch (SQLException e) {
            Messages.sprintfError("Error inserting to workdir: " + e.getMessage());
        } finally {
            SQL_Utils.closeConnection(connection);
        }
    }

    public void updateWorkDir() {

    }

    public void deleteWorkDir() {
        String sql = "DELETE FROM " + SQLTableEnums.WORKDIR.getType();
        try (java.sql.Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
            SQL_Utils.commitChanges(connection);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SQL_Utils.closeConnection(connection);
        }
    }

    public CopyState findDuplicates(FileInfo fileInfo) {

        findDuplicatesByDateRange(simpleDates.getSdf_ymd_minus().format(fileInfo.getDate()), simpleDates.getSdf_ymd_minus().format(fileInfo.getDate()));

        return CopyState.COPY; // Default state, should be replaced with actual logic to determine the state
    }

    public List<FileInfo> findDuplicateByExactDate(FileInfo fileInfo) {
        if (fileInfo == null) {
            return new ArrayList<>();
        }

        // If connection failed or database doesn't exist, return empty list
        if (connection == null || !SQL_Utils.isDbConnected(connection)) {
            Messages.sprintf("Database connection failed for: " + Main.conf.getWorkDir());
            return new ArrayList<>();
        }

        // If table creation fails, return empty list
        if (!createFileInfoTable(connection)) {
            Messages.sprintf("Failed to create FileInfo table in: " + Main.conf.getWorkDir_db_fileName());
            return new ArrayList<>();
        }

        List<FileInfo> list = new ArrayList<>();

        String sql = "SELECT " + FileInfoEnum.getAllFileInfoEnumValues() +
                " FROM " + SQLTableEnums.WORKDIR.getType() +
                " WHERE orgPath = ? AND size = ? AND localDateTime = ? AND imageDifferenceHash = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
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
    //@formatter:on

    public boolean insertFileInfo(FileInfo fileInfo) {
        Messages.sprintf("insertFileInfo starting: " + fileInfo);
        if (fileInfo == null) {
            Messages.warningText("Cannot insert null FileInfo");
            return false;
        }

        try {
            if (!SQL_Utils.isDbConnected(connection)) {
                Messages.sprintf("Database connection failed for: " + Main.conf.getWorkDir());
                return false;
            }

            PreparedStatement pstmt = connection.prepareStatement(fileInfoColumnsSQL[0]);

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
            return true;
        } catch (SQLException e) {
            Messages.sprintfError("Error inserting/updating FileInfo: " + e.getMessage());
            return false;
        }

    }
}