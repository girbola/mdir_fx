package com.girbola.persistence.fileinfo;

import com.girbola.Main;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.fileinfo.FileInfo;
import com.girbola.fileinfo.FileInfoEnum;
import com.girbola.messages.Messages;
import com.girbola.sql.SQL_Utils;
import com.girbola.utils.FileInfoUtils;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FileInfoMapper {

    public static boolean bindToFileInfoStatement(PreparedStatement pstmt, FileInfo fileInfo) {
        try {
            for (FileInfoEnum column : FileInfoEnum.getValuesInBindingOrder()) {
                int index = column.getBindIndex();

                switch (column) {
                    case FILEINFO_ID -> pstmt.setInt(index, fileInfo.getFileInfo_id());
                    case BAD -> pstmt.setBoolean(index, fileInfo.isBad());
                    case CAMERA_MODEL -> pstmt.setString(index, fileInfo.getCamera_model());
                    case CONFIRMED -> pstmt.setBoolean(index, fileInfo.isConfirmed());
                    case DESTINATION_PATH -> pstmt.setString(index, fileInfo.getDestination_Path());
                    case GPS_COORDINATES -> pstmt.setString(index, fileInfo.getGpsCoordinates());
                    case CUSTOM_GPS_COORDINATES -> pstmt.setBoolean(index, fileInfo.isCustomGpsCoordinates());
                    case DATE -> pstmt.setLong(index, fileInfo.getDate());
                    case EVENT -> pstmt.setString(index, fileInfo.getEvent());
                    case FILEHISTORIES -> pstmt.setString(index, convertFileHistoriesToString(fileInfo.getFileHistories()));
                    case GOOD -> pstmt.setBoolean(index, fileInfo.isGood());
                    case COPIED -> pstmt.setBoolean(index, fileInfo.isCopied());
                    case IGNORED -> pstmt.setBoolean(index, fileInfo.isIgnored());
                    case IMAGE -> pstmt.setBoolean(index, fileInfo.isImage());
                    case IMAGE_DIFFERENCE_HASH -> pstmt.setString(index, fileInfo.getImageDifferenceHash());
                    case LOCATION -> pstmt.setString(index, fileInfo.getLocation());
                    case MODIFIED -> pstmt.setBoolean(index, fileInfo.isModified());
                    case ORGPATH -> pstmt.setString(index, fileInfo.getOrgPath());
                    case ORGPATH_DRIVE_SERIAL_NUMBER -> pstmt.setString(index, fileInfo.getOrgPathDriveSerialNumber());
                    case ORIENTATION -> pstmt.setInt(index, fileInfo.getOrientation());
                    case RAW -> pstmt.setBoolean(index, fileInfo.isRaw());
                    case SHA256_CHECKSUM -> pstmt.setString(index, fileInfo.getSha256Checksum());
                    case SIZE -> pstmt.setLong(index, fileInfo.getSize());
                    case SUGGESTED -> pstmt.setBoolean(index, fileInfo.isSuggested());
                    case TABLE_DUPLICATED -> pstmt.setBoolean(index, fileInfo.isTableDuplicated());
                    case TAGS -> pstmt.setString(index, fileInfo.getTags());
                    case THUMB_LENGTH -> pstmt.setInt(index, fileInfo.getThumb_length());
                    case THUMB_OFFSET -> pstmt.setInt(index, fileInfo.getThumb_offset());
                    case TIME_SHIFT -> pstmt.setLong(index, fileInfo.getTimeShift());
                    case USER -> pstmt.setString(index, fileInfo.getUser());
                    case VIDEO -> pstmt.setBoolean(index, fileInfo.isVideo());
                    case WORK_DIR -> pstmt.setString(index, fileInfo.getWorkDir());
                    case WORK_DIR_DRIVE_SERIAL_NUMBER -> pstmt.setString(index, fileInfo.getWorkDirDriveSerialNumber());
                }
            }

            pstmt.addBatch();
            return true;
        } catch (Exception e) {
            Messages.sprintfError("Failed to bind FileInfo statement for: "
                    + (fileInfo == null ? "null" : fileInfo.getOrgPath())
                    + " error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static void bindToFileInfoBatch(PreparedStatement pstmt, FileInfo fileInfo) throws SQLException {
        if (!bindToFileInfoStatement(pstmt, fileInfo)) {
            throw new SQLException("Failed to bind FileInfo statement for: " + fileInfo.getOrgPath());
        }
    }

//    private static String convertFileHistoriesToString(List<String> fileHistories) {
//        return String.join(",", fileHistories);
//    }

    public static boolean bindToFileInfoStatementOld(PreparedStatement pstmt, FileInfo fileInfo) {
        try {
            int index = 1;
            pstmt.setBoolean(index++, fileInfo.isBad());
            pstmt.setString(index++, fileInfo.getCamera_model());
            pstmt.setBoolean(index++, fileInfo.isConfirmed());
            pstmt.setString(index++, fileInfo.getDestination_Path());
            pstmt.setLong(index++, fileInfo.getDate());        // <-- added DATE binding
            pstmt.setString(index++, fileInfo.getEvent());
            pstmt.setInt(index++, fileInfo.getFileInfo_id());
            pstmt.setString(index++, convertFileHistoriesToString(fileInfo.getFileHistories()));
            pstmt.setBoolean(index++, fileInfo.isGood());
            pstmt.setBoolean(index++, fileInfo.isCopied());
            pstmt.setBoolean(index++, fileInfo.isIgnored());
            pstmt.setBoolean(index++, fileInfo.isImage());
            pstmt.setString(index++, fileInfo.getImageDifferenceHash());
            pstmt.setString(index++, fileInfo.getLocation());
            pstmt.setBoolean(index++, fileInfo.isModified());
            pstmt.setString(index++, fileInfo.getOrgPath());
            pstmt.setString(index++, fileInfo.getWorkDirDriveSerialNumber());
            pstmt.setInt(index++, fileInfo.getOrientation());
            pstmt.setBoolean(index++, fileInfo.isRaw());
            pstmt.setLong(index++, fileInfo.getSize());
            pstmt.setBoolean(index++, fileInfo.isSuggested());
            pstmt.setBoolean(index++, fileInfo.isTableDuplicated());
            pstmt.setString(index++, fileInfo.getTags());
            pstmt.setInt(index++, fileInfo.getThumb_length());
            pstmt.setInt(index++, fileInfo.getThumb_offset());
            pstmt.setLong(index++, fileInfo.getTimeShift());
            pstmt.setBoolean(index++, fileInfo.isVideo());
            pstmt.setString(index++, fileInfo.getWorkDir());
            pstmt.setString(index++, fileInfo.getWorkDirDriveSerialNumber());
            pstmt.setString(index++, fileInfo.getSha256Checksum());

            pstmt.addBatch();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void bindToFileInfoBatch_(PreparedStatement pstmt, FileInfo fileInfo) throws SQLException {
        bindToFileInfoStatement(pstmt, fileInfo);
        try {
            pstmt.addBatch();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static String convertFileHistoriesToString(List<String> fileHistories) {
        return String.join(",", fileHistories);
    }

    /**
     * Loads the file info database for a given folder.
     *
     * @param folderInfo The folder information containing the path to the database.
     * @return true if the file info database was loaded successfully, false otherwise.
     */
    public static boolean loadFileInfoDatabase(FolderInfo folderInfo) {
        boolean loaded = false;
        Connection connection = FileInfoSqlConnectionFactory.connectToDatabase(Paths.get(folderInfo.getFolderPath()),
                Main.conf.getMdir_db_fileName());

        if (SQL_Utils.isDbConnected(connection)) {
            List<FileInfo> fileInfo_list = FileInfoDao.loadFileInfoDatabase(connection);
            if (!fileInfo_list.isEmpty()) {
                folderInfo.setFileInfoList(fileInfo_list);
                loaded = true;
            }
        }

        SQL_Utils.closeConnection(connection);

        return loaded;
    }


    public static FileInfo fromFileInfoResultSet(ResultSet rs) throws SQLException, IOException {
        if (rs == null) {
            Messages.sprintfError("No file info found in database. ResultSet is null");
            return null;
        }

        /*
        creationDate
        orientation
        cameraModel
        imagaThumbOffsetLenght
        imageDimensions
         */
if(rs.getString(FileInfoEnum.ORGPATH.getColumnName()).equals("C:\\Users\\marko\\OneDrive\\Kuvat\\Ruotsin reissu\\IMG-20220413-WA0001.jpg")){
    Messages.sprintf("--------------WEEEEEEEEE HAVE A WINNER orgPath = " + rs.getString(FileInfoEnum.ORGPATH.getColumnName()));
        }
        Messages.sprintf("--------------orgPath = " + rs.getString(FileInfoEnum.ORGPATH.getColumnName()));
        // Metadata
        // String fields - Paths and identifiers
        String orgPath = rs.getString(FileInfoEnum.ORGPATH.getColumnName());
        String orgPathDriveSerialNumber = rs.getString(FileInfoEnum.ORGPATH_DRIVE_SERIAL_NUMBER.getColumnName());
        String workDir = rs.getString(FileInfoEnum.WORK_DIR.getColumnName());
        String workDirDriveSerialNumber = rs.getString(FileInfoEnum.WORK_DIR_DRIVE_SERIAL_NUMBER.getColumnName());
        String destinationPath = rs.getString(FileInfoEnum.DESTINATION_PATH.getColumnName());

        // String fields - Metadata and categorization
        String cameraModel = rs.getString(FileInfoEnum.CAMERA_MODEL.getColumnName());
        String event = rs.getString(FileInfoEnum.EVENT.getColumnName());
        String location = rs.getString(FileInfoEnum.LOCATION.getColumnName());
        String tags = rs.getString(FileInfoEnum.TAGS.getColumnName());
        String user = rs.getString(FileInfoEnum.USER.getColumnName());

        // String fields - Hashes and checksums
        String imageDifferenceHash = rs.getString(FileInfoEnum.IMAGE_DIFFERENCE_HASH.getColumnName());
        String sha256CheckSum = rs.getString(FileInfoEnum.SHA256_CHECKSUM.getColumnName());

        // Boolean fields - File state flags
        boolean bad = rs.getBoolean(FileInfoEnum.BAD.getColumnName());
        boolean good = rs.getBoolean(FileInfoEnum.GOOD.getColumnName());
        boolean suggested = rs.getBoolean(FileInfoEnum.SUGGESTED.getColumnName());
        boolean confirmed = rs.getBoolean(FileInfoEnum.CONFIRMED.getColumnName());
        boolean modified = rs.getBoolean(FileInfoEnum.MODIFIED.getColumnName());
        boolean ignored = rs.getBoolean(FileInfoEnum.IGNORED.getColumnName());
        boolean copied = rs.getBoolean(FileInfoEnum.COPIED.getColumnName());
        boolean tableDuplicated = rs.getBoolean(FileInfoEnum.TABLE_DUPLICATED.getColumnName());

        // Boolean fields - File type flags
        boolean image = rs.getBoolean(FileInfoEnum.IMAGE.getColumnName());
        boolean raw = rs.getBoolean(FileInfoEnum.RAW.getColumnName());
        boolean video = rs.getBoolean(FileInfoEnum.VIDEO.getColumnName());

        // Integer fields
        int fileInfoId = rs.getInt(FileInfoEnum.FILEINFO_ID.getColumnName());
        int orientation = rs.getInt(FileInfoEnum.ORIENTATION.getColumnName());
        int thumbLenght = rs.getInt(FileInfoEnum.THUMB_LENGTH.getColumnName());
        int thumbOffset = rs.getInt(FileInfoEnum.THUMB_OFFSET.getColumnName());

        // Long fields
        long createdDate = rs.getLong(FileInfoEnum.DATE.getColumnName());
        long size = rs.getLong(FileInfoEnum.SIZE.getColumnName());
        long timeShift = rs.getLong(FileInfoEnum.TIME_SHIFT.getColumnName());

                /*
        creationDate
        orientation
        cameraModel
        imagaThumbOffsetLenght
        imageDimensions
         */

        // Complex fields
        List<String> fileHistories = getFileHistoriesData(rs);
        FileInfo fileInfo = new FileInfo(orgPath, orgPathDriveSerialNumber, workDir, workDirDriveSerialNumber, destinationPath, event, location, tags, cameraModel, user, orientation, timeShift, fileInfoId, bad, good, suggested, confirmed, modified, image, raw, video, ignored, copied, tableDuplicated, createdDate, size, imageDifferenceHash, thumbOffset, thumbLenght, sha256CheckSum, fileHistories);
Messages.sprintf("****************FileInfo all values*************: " + fileInfo.showAllValues());
Messages.sprintf("*************************************************");
        if (FileInfoUtils.handleMetadataInformation(fileInfo)) {
            return fileInfo;
        }
        return fileInfo;
    }

    private static List<String> getFileHistoriesData(ResultSet rs) throws SQLException {
        List<String> list = new ArrayList<>();
        // Remove the while loop - we're already in the correct row from the calling method
        String fileHistoriesStr = rs.getString(FileInfoEnum.FILEHISTORIES.getColumnName());
        if (fileHistoriesStr != null && !fileHistoriesStr.isEmpty()) {
            String[] fileHistories = fileHistoriesStr.trim().split("\\s*,\\s*");
            for (String history : fileHistories) {
                if (!history.isEmpty()) {
                    list.add(history);
                }
            }
        }
        return list;
    }


}
