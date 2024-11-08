package com.girbola.controllers.datefixer;

import com.girbola.Main;
import com.girbola.controllers.main.SQL_Enums;
import com.girbola.controllers.main.tables.*;
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

@Getter
@Setter
public class WorkDirSQL implements WorkDirInterface {
    private Path folder;
    private List<FolderInfo> folderInfo_list = new ArrayList<>();
    private FolderInfo folderInfo = new FolderInfo();

    private List<FileInfo> fileInfo_list = new ArrayList<>();

    private Connection workDirConnection;

    public WorkDirSQL(Path folder) {
        if (folder == null) {
            Messages.warningText("folder were null!!!");
            return;
        }
        if (!folder.toString().isEmpty() && !Files.exists(folder)) {
            Messages.warningText(Main.bundle.getString("reconnectDrives") + " at path: " + folder);
            return;
        } else {
            this.folder = folder;
            boolean b = loadWorkDirDatabase(this.folder);
            if (!b) {
                Messages.warningText(Main.bundle.getString("reconnectDrives") + " at path: " + folder);
            }
        }
    }

    String fileInfoSQL =
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

    public boolean loadWorkDirDatabase(Path workDir) {
        if (workDir == null) {
            Messages.warningText(Main.bundle.getString("workDirHasNotBeenSet"));
            return false;
        }
        if (!Files.exists(workDir)) {
            Messages.warningText(Main.bundle.getString("workDirHasNotBeenSet"));
            return false;
        }

        workDirConnection = SqliteConnection.connector(workDir, SQL_Enums.WORKDIR.getType());
        boolean dbConnected = SQL_Utils.isDbConnected(workDirConnection);
        if (dbConnected) {
            Messages.sprintf("workDir loaded: " + workDir);
            return true;
        } else {
            Messages.sprintf("Can't find current workDir: " + workDir);
            return false;
        }

    }

    @Override
    public void saveWorkDirDatabase() {
        if(SQL_Utils.isDbConnected(workDirConnection)) {
            SQL_Utils.setAutoCommit(workDirConnection, true);
            SQL_Utils.closeConnection(workDirConnection);
        }
    }

    @Override
    public void insertFileInfo(FileInfo fileInfo) {

        if (fileInfo == null) {
            Messages.warningText("Cannot insert null FileInfo");
            return;
        }

        try {
            if (workDirConnection == null || workDirConnection.isClosed()) {
                boolean b = loadWorkDirDatabase(Paths.get(Main.conf.getWorkDir()));
                if (!b) {
                    // Handle the case where the connection is null or closed
                    // You can either throw an exception or open a new connection
                    throw new SQLException("workDirConnection is not available.");
                }

            }

            String sql = "INSERT INTO " + SQL_Enums.WORKDIR.getType() + FileInfoEnum.getAllFileInfoEnumValues() + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
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
        }
    }

    @Override
    public boolean deleteFileInfo(FileInfo fileInfo) {
        if (fileInfo == null) {
            Messages.warningText("Cannot insert null FileInfo");
            return false;
        }
        try {
            String sql = "DELETE FROM " + SQL_Enums.WORKDIR.getType() + " WHERE id = ?";
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

    @Override
    public List<FileInfo> findDuplicateByExactDate(FileInfo fileInfo) {
        LocalDateTime date = fileInfo.getLocalDateTime();

        List<FileInfo> list = new ArrayList<>();
        FileInfo duplicateFileInfo = null;

        String sql = "SELECT " + FileInfoEnum.getAllFileInfoEnumValues() + " FROM " + SQL_Enums.WORKDIR.getType() + " WHERE orgPath = ? AND size = ? AND localDateTime = ? AND imageDifferenceHash = ?";
        try (PreparedStatement pstmt = workDirConnection.prepareStatement(sql)) {
            pstmt.setString(1, fileInfo.getOrgPath());
            pstmt.setLong(2, fileInfo.getSize());
            pstmt.setObject(3, fileInfo.getLocalDateTime());
            pstmt.setString(4, fileInfo.getImageDifferenceHash());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    duplicateFileInfo = populateFileInfoFromResultSet(rs);

                    if(duplicateFileInfo != null) {
                        if(FileInfoUtils.compareImagesMetadata(fileInfo, duplicateFileInfo)) {
                            list.add(duplicateFileInfo);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            Messages.sprintfError("Error finding duplicate FileInfo: " + e.getMessage());
            return null;
        }
        if(duplicateFileInfo != null) {

        }
        return list;

    }

    private FileInfo populateFileInfoFromResultSet(ResultSet rs) throws SQLException {
        if(!rs.next()) {
            return null;
        }
        FileInfo fileInfo = new FileInfo();
        fileInfo.setOrgPath(rs.getString("orgPath"));
        fileInfo.setFileInfo_id(rs.getInt("fileInfo_id"));
        fileInfo.setDestination_Path(rs.getString("destination_Path"));
        fileInfo.setEvent(rs.getString("event"));
        fileInfo.setLocation(rs.getString("location"));
        fileInfo.setOrientation(rs.getInt("orientation"));
        fileInfo.setTags(rs.getString("tags"));
        fileInfo.setCamera_model(rs.getString("camera_model"));
        fileInfo.setBad(rs.getBoolean("bad"));
        fileInfo.setGood(rs.getBoolean("good"));
        fileInfo.setSuggested(rs.getBoolean("suggested"));
        fileInfo.setConfirmed(rs.getBoolean("confirmed"));
        fileInfo.setIgnored(rs.getBoolean("ignored"));
        fileInfo.setTableDuplicated(rs.getBoolean("tableDuplicated"));
        fileInfo.setRaw(rs.getBoolean("raw"));
        fileInfo.setImage(rs.getBoolean("image"));
        fileInfo.setVideo(rs.getBoolean("video"));
        fileInfo.setDate(rs.getLong("date"));
        fileInfo.setSize(rs.getLong("size"));
        fileInfo.setThumb_offset(rs.getInt("thumb_offset"));
        fileInfo.setThumb_length(rs.getInt("thumb_length"));
        fileInfo.setImageDifferenceHash(rs.getString("imageDifferenceHash"));
        fileInfo.setUser(rs.getString("user"));
        fileInfo.setWorkDir(rs.getString("workDir"));
        fileInfo.setWorkDirDriveSerialNumber(rs.getString("workDirDriveSerialNumber"));
        fileInfo.setLocalDateTime(rs.getObject("localDateTime", LocalDateTime.class));
        fileInfo.setTimeShift(rs.getLong("timeShift"));
        return fileInfo;
    }
    @Override
    public List<FileInfo> findDuplicateByDateRange(FileInfo fileInfo, String startDate, String endDate) {

        List<FileInfo> fileInfoDuplicates = new ArrayList<>();
        String sql =
                "SELECT " + fileInfoSQL + " FROM " + SQL_Enums.WORKDIR.getType() +
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
        }

        return fileInfoDuplicates;
    }

    @Override
    public List<FileInfo> findByExactDate(FileInfo fileInfo, String date) {
        String sql = "SELECT changed,connected,ignored,dateDifference,badFiles,confirmed,copied,folderFiles,folderImageFiles," +
                "folderRawFiles,folderVideoFiles,goodFiles,suggested,folderSize,justFolderName,folderPath,maxDate,minDate,state,tableType FROM "
                + SQL_Enums.WORKDIR.getType() + "WHERE minDate" + date + " between ";

        return null;
    }

    @Override
    public List<FileInfo> findByDateRange(FileInfo fileInfo, String date1, String date2, String date3) {

        return null;
    }

    public List<FolderInfo> getFolderInfo_list() {
        return folderInfo_list;
    }

    private void populateFileInfoFromResultSet(FileInfo fileInfo, ResultSet rs) throws SQLException {
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

    private FolderInfo loadFolderInfo() {

        SQL_Utils.setAutoCommit(workDirConnection, false);
        try {
            String sql = "SELECT * FROM " + SQL_Enums.FOLDERINFO.getType();
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

            folderInfo = new FolderInfo();
            if (fileInfo_list.isEmpty()) {
                Messages.sprintf("FileInfo were empty!");
                fileInfo_list = FileInfoUtils.createFileInfo_list(folderInfo);
                if (fileInfo_list.isEmpty()) {
                    Messages.sprintf("FileInfo creationg did not work this time or folder were empty.");
                }
            }
            folderInfo.setChanged(changed);
            folderInfo.setConnected(connected);
            folderInfo.setFileInfoList(fileInfo_list);
            folderInfo.setIgnored(ignored);
            folderInfo.setDateDifferenceRatio(dateDifference);
            folderInfo.setBadFiles(badFiles);
            folderInfo.setConfirmed(confirmed);
            folderInfo.setCopied(copied);
            folderInfo.setFolderFiles(folderFiles);
            folderInfo.setFolderImageFiles(folderImageFiles);
            folderInfo.setFolderRawFiles(folderRawFiles);
            folderInfo.setFolderVideoFiles(folderVideoFiles);
            folderInfo.setGoodFiles(goodFiles);
            folderInfo.setSuggested(suggested);
            folderInfo.setFolderSize(folderSize);
            folderInfo.setJustFolderName(justFolderName);
            folderInfo.setFolderPath(folderPath);
            folderInfo.setMaxDate(maxDate);
            folderInfo.setMinDate(minDate);
            folderInfo.setState(state);
            folderInfo.setTableType(tableType);
            smtm.close();
            workDirConnection.close();

            return folderInfo;

        } catch (Exception e) {
            Messages.sprintfError(Main.bundle.getString("cannotLoadFolderInfoFromDatabase"));
            return null;
        }
    }

}
