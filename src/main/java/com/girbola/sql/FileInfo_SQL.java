package com.girbola.sql;

import com.girbola.Main;
import com.girbola.controllers.main.SQLTableEnums;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.fileinfo.FileInfo;
import com.girbola.fileinfo.FileInfoEnum;
import com.girbola.messages.Messages;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FileInfo_SQL {

    private static final String ERROR = FileInfo_SQL.class.getName();

    // @formatter:on
    public static boolean addToFileInfoDB(PreparedStatement pstmt, FileInfo fileInfo) {
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

            pstmt.addBatch();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String convertFileHistoriesToString(List<String> fileHistories) {
        return String.join(",", fileHistories);
    }

    // @formatter:on
// ... existing code ...
    public static boolean insertFileInfoListToFileInfoDatabase(FolderInfo folderInfo, boolean isWorkDir) {
        final int BATCH_LIMIT = 1000;
        final String logPrefix = "insertFileInfoListToFileInfoDatabase";

        Messages.sprintf("--------------" + logPrefix + " started: " + folderInfo.getFolderPath() + " isWorkDir: " + isWorkDir);

        final List<FileInfo> fileInfos = (folderInfo != null) ? folderInfo.getFileInfoList() : null;
        if (folderInfo == null || fileInfos == null || fileInfos.isEmpty()) {
            Messages.sprintfError("Invalid parameters provided to " + logPrefix);
            return false;
        }

        final Connection connection = SqliteConnection.connectToDatabase(folderInfo.getFolderPath(), Main.conf.getMdir_db_fileName());
        if (connection == null) {
            Messages.sprintfError("Failed to open database connection: " + folderInfo.getFolderPath());
            return false;
        }

        try {
            if (!SQL_Utils.isDbConnected(connection)) {
                Messages.sprintfError("Database connection lost: " + logPrefix + " " + folderInfo.getFolderPath());
                return false;
            }

            SQL_Utils.setAutoCommit(connection, false);

            final Path folder;
            try {
                folder = Paths.get(fileInfos.get(0).getOrgPath()).getParent();
                if (folder == null || !Files.exists(folder)) {
                    Messages.sprintfError("Parent folder does not exist: " + folder);
                    return false;
                }
            } catch (Exception e) {
                Messages.sprintfError("Cannot get path for the folder: " + e.getMessage());
                return false;
            }

            if (!createFileInfoTable(connection)) {
                Messages.sprintfError("Failed to create FileInfo table");
                return false;
            }
            Messages.sprintf("FileInfo table created/verified");

            // IMPORTANT: perform any other writes (which may commit/rollback/DDL) BEFORE preparing the statement
            if (!FolderInfo_SQL.saveFolderInfo(connection, folderInfo)) {
                Messages.sprintfError("Failed to save folder info for later loading: " + folderInfo.getFolderPath() + " still continuing");
            }
            SQL_Utils.commitChanges(connection);

            if (!SQL_Utils.isDbConnected(connection)) {
                Messages.sprintfError("Database connection lost after writing folder info");
                return false;
            }

            final String columnNames = FileInfoEnum.getAllFileInfoColumnNames();
            final String[] columns = columnNames.split("\\s*,\\s*");
            final String placeholders = String.join(", ", Collections.nCopies(columns.length, "?"));
            final String insertSql = "INSERT OR REPLACE INTO " + SQLTableEnums.FILEINFO.getType() + " (" +
                    columnNames +
                    ") VALUES (" + placeholders + ");";

            Messages.sprintf("FileInfo columns count: " + columns.length + " columns: " + Arrays.toString(columns));
            Messages.sprintf("Insert SQL: " + insertSql);

            try (PreparedStatement pstmt = connection.prepareStatement(insertSql)) {
                int batchSize = 0;

                for (FileInfo fileInfo : fileInfos) {
                    Messages.sprintf("FIQ - Processing file: " + fileInfo.getOrgPath());

                    if (!addToFileInfoDB(pstmt, fileInfo)) {
                        throw new SQLException("Failed to add file info to database: " + fileInfo.getOrgPath());
                    }

                    batchSize++;
                    if (batchSize >= BATCH_LIMIT) {
                        pstmt.executeBatch();
                        connection.commit();
                        batchSize = 0;
                    }
                }

                if (batchSize > 0) {
                    pstmt.executeBatch();
                    connection.commit();
                }

                Messages.sprintf("Successfully inserted all file info records");
                return true;
            }
        } catch (SQLException ex) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                Messages.sprintfError("Failed to rollback transaction: " + rollbackEx.getMessage());
            }
            Messages.sprintfError("Failed to insert file info records: " + ex);
            ex.printStackTrace();
            return false;
        } finally {
            if (SQL_Utils.isDbConnected(connection)) {
                SQL_Utils.closeConnection(connection);
            }
        }
    }

    // ... existing code ...
    private static void ensureFileInfoColumnsExists_(Connection connection, Map<String, String> map) throws SQLException {
        DatabaseMetaData meta = connection.getMetaData();
        String fileInfoTable = SQLTableEnums.FILEINFO.getType();

        // Get existing columns
        Set<String> existingColumns = new HashSet<>();
        try (ResultSet rs = meta.getColumns(null, null, fileInfoTable, null)) {
            while (rs.next()) {
                existingColumns.add(rs.getString("COLUMN_NAME").toLowerCase());
            }
        }

        try (Statement stmt = connection.createStatement()) {
            // Add any missing columns

            for (String columnDef : FileInfoEnum.getAllFileInfoColumnNames().split(",")) {
                String columnName = columnDef.split("\\s+")[0].toLowerCase();
                if (!existingColumns.contains(columnName)) {
                    try {
                        String alterTableSQL = "ALTER TABLE " + fileInfoTable + " ADD COLUMN " + columnDef;
                        Messages.sprintf("Adding column: " + alterTableSQL);
                        stmt.executeUpdate(alterTableSQL);
                    } catch (SQLException e) {
                        // Column may already exist, ignore error
                    }
                }
            }
        }
    }

    /**
     * @param connection
     * @param list
     * @return
     */
    // @formatter:on
    public static boolean deleteFileInfoListToDatabase(Connection connection, List<FileInfo> list) {
        Messages.sprintf("deleteFileInfoListToDatabase tableCreated Started");

        try {

            SQL_Utils.setAutoCommit(connection, false);
            boolean tableCreated = createFileInfoTable(connection);
            if (tableCreated) {
                Messages.sprintf("insertFileInfoListToDatabase tableCreated");
            } else {
                Messages.sprintfError("insertFileInfoListToDatabase NOT tableCreated");
            }
            if (!SQL_Utils.isDbConnected(connection)) {
                Messages.sprintfError("insertFileInfoListToDatabase were NOT connected");
                return false;
            }
            String sql = "DELETE FROM " + SQLTableEnums.FILEINFO.getType() + " WHERE orgPath = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            for (FileInfo fileInfo : list) {
                pstmt.setString(1, fileInfo.getOrgPath());
                pstmt.addBatch();
                Messages.sprintf("=====addToFileInfoDB started: " + fileInfo.getOrgPath());
            }
            pstmt.executeBatch();
//			pstmt.closeOnCompletion();
            pstmt.close();
            connection.commit();
            Messages.sprintf("**deleteFileInfoListToDatabase tableCreated DONE");
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            Messages.sprintfError("deleteFileInfoListToDatabase tableCreated FAILED");
            return false;
        }
    }

    public static String createFileInfoTable() {
        return FileInfoEnum.getCreateTableSQL(SQLTableEnums.FILEINFO.getType());
//
//        return "CREATE TABLE IF NOT EXISTS " + SQLTableEnums.FILEINFO.getType() + "(" +
//                FileInfoEnum.BAD.getColumnName() + " " + FileInfoEnum.BAD.getSqlType() + ", " +
//                FileInfoEnum.CAMERA_MODEL.getColumnName() + " " + FileInfoEnum.CAMERA_MODEL.getSqlType() + ", " +
//                FileInfoEnum.CONFIRMED.getColumnName() + " " + FileInfoEnum.CONFIRMED.getSqlType() + ", " +
//                FileInfoEnum.DESTINATION_PATH.getColumnName() + " " + FileInfoEnum.DESTINATION_PATH.getSqlType() + ", " +
//                FileInfoEnum.EVENT.getColumnName() + " " + FileInfoEnum.EVENT.getSqlType() + ", " +
//                FileInfoEnum.FILEINFO_ID.getColumnName() + " " + FileInfoEnum.FILEINFO_ID.getSqlType() + ", " +
//                FileInfoEnum.FILEHISTORIES.getColumnName() + " " + FileInfoEnum.FILEHISTORIES.getSqlType() + ", " +
//                FileInfoEnum.GOOD.getColumnName() + " " + FileInfoEnum.GOOD.getSqlType() + ", " +
//                FileInfoEnum.COPIED.getColumnName() + " " + FileInfoEnum.COPIED.getSqlType() + ", " +
//                FileInfoEnum.IGNORED.getColumnName() + " " + FileInfoEnum.IGNORED.getSqlType() + ", " +
//                FileInfoEnum.IMAGE.getColumnName() + " " + FileInfoEnum.IMAGE.getSqlType() + ", " +
//                FileInfoEnum.IMAGE_DIFFERENCE_HASH.getColumnName() + " " + FileInfoEnum.IMAGE_DIFFERENCE_HASH.getSqlType() + ", " +
//                FileInfoEnum.LOCATION.getColumnName() + " " + FileInfoEnum.LOCATION.getSqlType() + ", " +
//                FileInfoEnum.MODIFIED.getColumnName() + " " + FileInfoEnum.MODIFIED.getSqlType() + ", " +
//                FileInfoEnum.ORGPATH.getColumnName() + " " + FileInfoEnum.ORGPATH.getSqlType() + ", " +
//                FileInfoEnum.ORGPATH_DRIVE_SERIAL_NUMBER.getColumnName() + " " + FileInfoEnum.ORGPATH_DRIVE_SERIAL_NUMBER.getSqlType() + ", " +
//                FileInfoEnum.ORIENTATION.getColumnName() + " " + FileInfoEnum.ORIENTATION.getSqlType() + ", " +
//                FileInfoEnum.RAW.getColumnName() + " " + FileInfoEnum.RAW.getSqlType() + ", " +
//                FileInfoEnum.SIZE.getColumnName() + " " + FileInfoEnum.SIZE.getSqlType() + ", " +
//                FileInfoEnum.SUGGESTED.getColumnName() + " " + FileInfoEnum.SUGGESTED.getSqlType() + ", " +
//                FileInfoEnum.TABLE_DUPLICATED.getColumnName() + " " + FileInfoEnum.TABLE_DUPLICATED.getSqlType() + ", " +
//                FileInfoEnum.TAGS.getColumnName() + " " + FileInfoEnum.TAGS.getSqlType() + ", " +
//                FileInfoEnum.THUMB_LENGTH.getColumnName() + " " + FileInfoEnum.THUMB_LENGTH.getSqlType() + ", " +
//                FileInfoEnum.THUMB_OFFSET.getColumnName() + " " + FileInfoEnum.THUMB_OFFSET.getSqlType() + ", " +
//                FileInfoEnum.TIME_SHIFT.getColumnName() + " " + FileInfoEnum.TIME_SHIFT.getSqlType() + ", " +
//                FileInfoEnum.USER.getColumnName() + " " + FileInfoEnum.USER.getSqlType() + ", " +
//                FileInfoEnum.VIDEO.getColumnName() + " " + FileInfoEnum.VIDEO.getSqlType() + ", " +
//                FileInfoEnum.WORK_DIR.getColumnName() + " " + FileInfoEnum.WORK_DIR.getSqlType() + ", " +
//                FileInfoEnum.WORK_DIR_DRIVE_SERIAL_NUMBER.getColumnName() + " " + FileInfoEnum.WORK_DIR_DRIVE_SERIAL_NUMBER.getSqlType() + ");";
    }

    public static FileInfo loadFileInfo(ResultSet rs) throws SQLException {
        if (rs == null) {
            Messages.sprintfError("No file info found in database. ResultSet is null");
            return null;
        }

        Messages.sprintf("--------------orgPath = " + rs.getString(FileInfoEnum.ORGPATH.getColumnName()));
        String camelModel = rs.getString(FileInfoEnum.CAMERA_MODEL.getColumnName());
        String destinationPath = rs.getString(FileInfoEnum.DESTINATION_PATH.getColumnName());
        String event = rs.getString(FileInfoEnum.EVENT.getColumnName());
        String imageDifferenceHash = rs.getString(FileInfoEnum.IMAGE_DIFFERENCE_HASH.getColumnName());
        String location = rs.getString(FileInfoEnum.LOCATION.getColumnName());
        String orgPath = rs.getString(FileInfoEnum.ORGPATH.getColumnName());
        String orgPathDriveSerialNumber = rs.getString(FileInfoEnum.ORGPATH_DRIVE_SERIAL_NUMBER.getColumnName());
        String tags = rs.getString(FileInfoEnum.TAGS.getColumnName());
        String user = rs.getString(FileInfoEnum.USER.getColumnName());
        String workDir = rs.getString(FileInfoEnum.WORK_DIR.getColumnName());
        String workDirDriveSerialNumber = rs.getString(FileInfoEnum.WORK_DIR_DRIVE_SERIAL_NUMBER.getColumnName());
        boolean bad = rs.getBoolean(FileInfoEnum.BAD.getColumnName());
        boolean confirmed = rs.getBoolean(FileInfoEnum.CONFIRMED.getColumnName());
        boolean copied = rs.getBoolean(FileInfoEnum.COPIED.getColumnName());
        boolean good = rs.getBoolean(FileInfoEnum.GOOD.getColumnName());
        boolean ignored = rs.getBoolean(FileInfoEnum.IGNORED.getColumnName());
        boolean image = rs.getBoolean(FileInfoEnum.IMAGE.getColumnName());
        boolean modified = rs.getBoolean(FileInfoEnum.MODIFIED.getColumnName());
        boolean raw = rs.getBoolean(FileInfoEnum.RAW.getColumnName());
        boolean suggested = rs.getBoolean(FileInfoEnum.SUGGESTED.getColumnName());
        boolean tableDuplicated = rs.getBoolean(FileInfoEnum.TABLE_DUPLICATED.getColumnName());
        boolean video = rs.getBoolean(FileInfoEnum.VIDEO.getColumnName());
        int fileInfo_id = rs.getInt(FileInfoEnum.FILEINFO_ID.getColumnName());
        int orientation = rs.getInt(FileInfoEnum.ORIENTATION.getColumnName());
        int thumb_lenght = rs.getInt(FileInfoEnum.THUMB_LENGTH.getColumnName());
        int thumb_offset = rs.getInt(FileInfoEnum.THUMB_OFFSET.getColumnName());
        long date = rs.getLong(FileInfoEnum.DATE.getColumnName());
        long size = rs.getLong(FileInfoEnum.SIZE.getColumnName());
        long timeShift = rs.getLong(FileInfoEnum.TIME_SHIFT.getColumnName()); // <-- use getLong to match stored type

        List<String> fileHistories = getFileHistoriesData(rs);

        return new FileInfo(
                orgPath,
                orgPathDriveSerialNumber,
                workDir,
                workDirDriveSerialNumber,
                destinationPath,
                event,
                location,
                tags,
                camelModel,
                user,
                orientation,
                timeShift,
                fileInfo_id,
                bad,
                good,
                suggested,
                confirmed,
                modified,
                image,
                raw,
                video,
                ignored,
                copied,
                tableDuplicated,
                date,
                size,
                imageDifferenceHash,
                thumb_offset,
                thumb_lenght,
                fileHistories);
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

    /*
     * FileInfo
     */
    // @formatter:off
	public static boolean createFileInfoTable(Connection connection) {
        if (!SQL_Utils.isDbConnected(connection)) {
            Messages.sprintfError("Database connection is not active. Aborting the operation. Path is?" + SQL_Utils.getUrl(connection));
            return false;
        }

        String createTableSQL = FileInfoEnum.getCreateTableSQL(SQLTableEnums.FILEINFO.getType());
Messages.sprintf("Createa fileinfo table: " +  createTableSQL);
		try {
			Statement stmt = connection.createStatement();
			stmt.execute(createTableSQL);
			return true;
		} catch (Exception ex) {
            Messages.sprintfError("Cannot create table: " + createTableSQL);
			return false;
		}
	}

    /**
     * Loads the file info database for a given folder.
     *
     * @param folderInfo The folder information containing the path to the database.
     * @return true if the file info database was loaded successfully, false otherwise.
     */
	public static boolean loadFileInfoDatabase(FolderInfo folderInfo) {
		boolean loaded = false;
		Connection connection = SqliteConnection.connectToDatabase(Paths.get(folderInfo.getFolderPath()),
				Main.conf.getMdir_db_fileName());

		if (SQL_Utils.isDbConnected(connection)) {
			List<FileInfo> fileInfo_list = loadFileInfoDatabase(connection);
			if (!fileInfo_list.isEmpty()) {
				folderInfo.setFileInfoList(fileInfo_list);
				loaded = true;
			}
		}

        SQL_Utils.closeConnection(connection);

		return loaded;
	}

	/**
	 *
	 * @param connection
	 * @return
	 */
	// @formatter:off
    public static List<FileInfo> loadFileInfoDatabase(Connection connection) {
        if (connection == null) {
            return new ArrayList<>();
        }

        Messages.sprintf("loadFileInfoDatabase Started!: " + SQL_Utils.getUrl(connection));

        if (!SQL_Utils.isDbConnected(connection)) {
            Messages.sprintf("loadFileInfoDatabase Not Connected!");
            return new ArrayList<>();
        }

        // Add this block to ensure all required columns exist
        try {
            // Create a map of column names and their SQL types
            Map<String, String> fileInfoColumnsMap = new HashMap<>();
            for (FileInfoEnum column : FileInfoEnum.values()) {
                fileInfoColumnsMap.put(column.getColumnName(), column.getSqlType());
                //Messages.sprintf("############Adding column: " + column.getColumnName());
            }
            SQL_Utils.ensureColumnsExist(connection, SQLTableEnums.FILEINFO.getType(), fileInfoColumnsMap);
            Messages.sprintf("############Ensured columns exist!");
        } catch (SQLException e) {
            Messages.sprintf("Error ensuring columns exist in file info table: " + e.getMessage());
            throw new RuntimeException(e);
        }

//        try {
//            SQL_Utils.ensureColumnsExist(connection, SQLTableEnums.FILEINFO.getType(), fileInfoColumnsMap);}
//        catch (SQLException e) {
//            Messages.sprintf("Error ensuring columns exist in file info table: " + e.getMessage());
//            throw new RuntimeException(e);
//        }
        List<FileInfo> list = new ArrayList<>();

        //String sql = "SELECT * FROM " + SQLTableEnums.FILEINFO.getType();
        String sql = "SELECT " + FileInfoEnum.getAllFileInfoColumnNames() + " FROM " + SQLTableEnums.FILEINFO.getType();
        Messages.sprintf("############ FILEINFOOOOOO SQL: " + sql);
        try {
            SQL_Utils.setAutoCommit(connection, false);

            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    if (Main.getProcessCancelled()) {
                        Messages.sprintfError("############## Process cancelled, rolling back connection");
                        SQL_Utils.rollBackConnection(connection);
                        return new ArrayList<>();
                    }

                    FileInfo finfo = loadFileInfo(rs);
                    if(finfo == null) {
                        Messages.sprintfError("############## Cannot load fileinfo: " + finfo.toString() );
                        continue;
                    }
                    list.add(finfo);
                }

                return list;
            }
        } catch (SQLException e) {
            Messages.sprintfError("Error ensuring columns exist in file info table: " + e.getMessage());
            SQL_Utils.rollBackConnection(connection);
            return new ArrayList<>();
        } catch (Exception e) {
            Messages.sprintfError("Error loading file info database: " + e.getMessage());
            SQL_Utils.rollBackConnection(connection);
            return new ArrayList<>();
        }
    }

}
