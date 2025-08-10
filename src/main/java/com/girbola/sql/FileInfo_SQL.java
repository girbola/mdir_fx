package com.girbola.sql;

import com.girbola.Main;
import com.girbola.controllers.main.SQLTableEnums;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

public class FileInfo_SQL {

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

    final static Map<String, String> fileInfoColumnsMap = new LinkedHashMap<String, String>() {{
        put(FileInfoConstants.BAD, "BOOLEAN");
        put(FileInfoConstants.CAMERA_MODEL, "STRING");
        put(FileInfoConstants.CONFIRMED, "BOOLEAN");
        put(FileInfoConstants.COPIED, "BOOLEAN");
        put(FileInfoConstants.DATE, "NUMERIC");
        put(FileInfoConstants.DESTINATIONPATH, "STRING");
        put(FileInfoConstants.EVENT, "STRING");
        put(FileInfoConstants.FILEHISTORIES, "STRING");
        put(FileInfoConstants.FILEINFOID, "INTEGER PRIMARY KEY");
        put(FileInfoConstants.GOOD, "BOOLEAN");
        put(FileInfoConstants.IGNORED, "BOOLEAN");
        put(FileInfoConstants.IMAGE, "BOOLEAN");
        put(FileInfoConstants.IMAGE_DIFFERENCE_HASH, "INTEGER");
        put(FileInfoConstants.LOCATION, "STRING");
        put(FileInfoConstants.ORG_PATH, "STRING UNIQUE");
        put(FileInfoConstants.ORIENTATION, "INTEGER");
        put(FileInfoConstants.RAW, "BOOLEAN");
        put(FileInfoConstants.SIZE, "NUMERIC");
        put(FileInfoConstants.SUGGESTED, "BOOLEAN");
        put(FileInfoConstants.TABLE_DUPLICATED, "BOOLEAN");
        put(FileInfoConstants.TAGS, "STRING");
        put(FileInfoConstants.THUMB_LENGTH, "INTEGER");
        put(FileInfoConstants.THUMB_OFFSET, "INTEGER");
        put(FileInfoConstants.TIMESHIFT, "INTEGER");
        put(FileInfoConstants.USER, "STRING");
        put(FileInfoConstants.VIDEO, "BOOLEAN");
        put(FileInfoConstants.WORK_DIR, "STRING");
        put(FileInfoConstants.WORK_DIR_DRIVE_SERIAL_NUMBER, "STRING");
    }};

    final static String fileInfoInsert = "INSERT OR REPLACE INTO " + SQLTableEnums.FILEINFO.getType() + " (" + FileInfoConstants.FILEINFOID + ", "
            + FileInfoConstants.ORG_PATH + ", "
            + FileInfoConstants.WORK_DIR + ", "
            + FileInfoConstants.WORK_DIR_DRIVE_SERIAL_NUMBER + ", "
            + FileInfoConstants.DESTINATIONPATH + ", "
            + FileInfoConstants.CAMERA_MODEL + ", "
            + FileInfoConstants.USER + ", "
            + FileInfoConstants.ORIENTATION + ", "
            + FileInfoConstants.BAD + ", "
            + FileInfoConstants.GOOD + ", "
            + FileInfoConstants.CONFIRMED + ", "
            + FileInfoConstants.COPIED + ", "
            + FileInfoConstants.IGNORED + ", "
            + FileInfoConstants.SUGGESTED + ", "
            + FileInfoConstants.IMAGE + ", "
            + FileInfoConstants.RAW + ", "
            + FileInfoConstants.VIDEO + ", "
            + FileInfoConstants.TIMESHIFT + ", "
            + FileInfoConstants.DATE + ", "
            + FileInfoConstants.SIZE + ", "
            + FileInfoConstants.TABLE_DUPLICATED + ", "
            + FileInfoConstants.TAGS + ", "
            + FileInfoConstants.EVENT + ", "
            + FileInfoConstants.LOCATION + ", "
            + FileInfoConstants.IMAGE_DIFFERENCE_HASH + ", "
            + FileInfoConstants.THUMB_OFFSET + ", "
            + FileInfoConstants.THUMB_LENGTH + ", "
            + FileInfoConstants.FILEHISTORIES + ")"
            + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    private static final String ERROR = FileInfo_SQL.class.getName();

    // @formatter:on
    public static boolean addToFileInfoDB(PreparedStatement pstmt, FileInfo fileInfo) {
        try {
            pstmt.setInt(1, fileInfo.getFileInfo_id());
            pstmt.setString(2, fileInfo.getOrgPath());
            pstmt.setString(3, fileInfo.getWorkDir());
            pstmt.setString(4, fileInfo.getWorkDirDriveSerialNumber());
            pstmt.setString(5, fileInfo.getDestination_Path());
            pstmt.setString(6, fileInfo.getCamera_model());
            pstmt.setString(7, fileInfo.getUser());
            pstmt.setInt(8, fileInfo.getOrientation());
            pstmt.setBoolean(9, fileInfo.isBad());
            pstmt.setBoolean(10, fileInfo.isGood());
            pstmt.setBoolean(11, fileInfo.isConfirmed());
            pstmt.setBoolean(12, fileInfo.isCopied());
            pstmt.setBoolean(13, fileInfo.isIgnored());
            pstmt.setBoolean(14, fileInfo.isSuggested());
            pstmt.setBoolean(15, fileInfo.isImage());
            pstmt.setBoolean(16, fileInfo.isRaw());
            pstmt.setBoolean(17, fileInfo.isVideo());
            pstmt.setLong(18, fileInfo.getTimeShift());
            pstmt.setLong(19, fileInfo.getDate());
            pstmt.setLong(20, fileInfo.getSize());
            pstmt.setBoolean(21, fileInfo.isTableDuplicated());
            pstmt.setString(22, fileInfo.getTags());
            pstmt.setString(23, fileInfo.getEvent());
            pstmt.setString(24, fileInfo.getLocation());
            pstmt.setString(25, fileInfo.getImageDifferenceHash());
            pstmt.setInt(26, fileInfo.getThumb_offset());
            pstmt.setInt(27, fileInfo.getThumb_length());
            pstmt.setString(28, convertFileHistoriesToString(fileInfo.getFileHistories()));

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
    public static boolean insertFileInfoListToDatabase(FolderInfo folderInfo, boolean isWorkDir) {

        Connection connection = SqliteConnection.connector(folderInfo.getFolderPath(), Main.conf.getMdir_db_fileName());
        SQL_Utils.isDbConnected(connection);
        SQL_Utils.setAutoCommit(connection, false);

        List<FileInfo> list = folderInfo.getFileInfoList();

        if (connection == null || list == null || list.isEmpty()) {
            Messages.sprintfError("Invalid parameters provided to insertFileInfoListToDatabase");
            return false;
        }

        Messages.sprintf("insertFileInfoListToDatabase started");
        Path folder = null;
        try {
            folder = Paths.get(list.get(0).getOrgPath()).getParent();
            if (!Files.exists(folder)) {
                Messages.sprintfError("Parent folder does not exist: " + folder);
                return false;
            }
        } catch (Exception e) {
            Messages.sprintfError("Cannot get path for the folder: " + e.getMessage());
            return false;
        }

        try {
            boolean tableCreated = createFileInfoTable(connection);
            if (!tableCreated) {
                Messages.sprintfError("Failed to create FileInfo table");
                SQL_Utils.closeConnection(connection);
                return false;
            }
            Messages.sprintf("FileInfo table created/verified");

            if (!SQL_Utils.isDbConnected(connection)) {
                Messages.sprintfError("Database connection lost");
                SQL_Utils.closeConnection(connection);
                return false;
            }
            SQL_Utils.ensureColumnsExist(connection, SQLTableEnums.FILEINFO.getType(), fileInfoColumnsMap);

            try (PreparedStatement pstmt = connection.prepareStatement(fileInfoInsert)) {

                int batchSize = 0;
                final int BATCH_LIMIT = 1000;

                for (FileInfo fileInfo : list) {
                    Messages.sprintf("Processing file: {}", fileInfo.getOrgPath());
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
        } catch (Exception ex) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                Messages.sprintfError("Failed to rollback transaction: " + rollbackEx.getMessage());

            }
            Messages.sprintfError("Failed to insert file info records: " + ex.getMessage());
        } finally {
            SQL_Utils.closeConnection(connection);
        }
        return true;
    }

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
            for (String columnDef : fileInfoColumnsSQL) {
                String columnName = columnDef.split("\\s+")[0].toLowerCase();
                if (!existingColumns.contains(columnName)) {
                    try {
                        String alterTableSQL = "ALTER TABLE " + fileInfoTable + " ADD COLUMN " + columnDef;
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

    public static FileInfo loadFileInfo(ResultSet rs) throws SQLException {
        String camera_model = rs.getString(FileInfoConstants.CAMERA_MODEL);
        String destPath = rs.getString(FileInfoConstants.DESTINATIONPATH);
        String event = rs.getString(FileInfoConstants.EVENT);
        String imageDifferenceHash = rs.getString(FileInfoConstants.IMAGE_DIFFERENCE_HASH);
        String location = rs.getString(FileInfoConstants.LOCATION);
        String orgPath = rs.getString(FileInfoConstants.ORG_PATH);
        String tags = rs.getString(FileInfoConstants.TAGS);
        String user = rs.getString(FileInfoConstants.USER);
        String workDir = rs.getString(FileInfoConstants.WORK_DIR);
        String workDirDriveSerialNumber = rs.getString(FileInfoConstants.WORK_DIR_DRIVE_SERIAL_NUMBER);
        boolean bad = rs.getBoolean(FileInfoConstants.BAD);
        boolean confirmed = rs.getBoolean(FileInfoConstants.CONFIRMED);
        boolean copied = rs.getBoolean(FileInfoConstants.COPIED);
        boolean good = rs.getBoolean(FileInfoConstants.GOOD);
        boolean ignored = rs.getBoolean(FileInfoConstants.IGNORED);
        boolean image = rs.getBoolean(FileInfoConstants.IMAGE);
        boolean raw = rs.getBoolean(FileInfoConstants.RAW);
        boolean suggested = rs.getBoolean(FileInfoConstants.SUGGESTED);
        boolean tableDuplicated = rs.getBoolean(FileInfoConstants.TABLE_DUPLICATED);
        boolean video = rs.getBoolean(FileInfoConstants.VIDEO);
        int fileInfo_id = rs.getInt(FileInfoConstants.FILEINFOID);
        int orientation = rs.getInt(FileInfoConstants.ORIENTATION);
        int thumb_lenght = rs.getInt(FileInfoConstants.THUMB_LENGTH);
        int thumb_offset = rs.getInt(FileInfoConstants.THUMB_OFFSET);
        long date = rs.getLong(FileInfoConstants.DATE);
        long size = rs.getLong(FileInfoConstants.SIZE);
        long timeShift = rs.getInt(FileInfoConstants.TIMESHIFT);

        List<String> fileHistories = getFileHistoriesData(rs);

        return new FileInfo(
                orgPath,
                workDir,
                workDirDriveSerialNumber,
                destPath,
                event,
                location,
                tags,
                camera_model,
                user,
                orientation,
                timeShift,
                fileInfo_id,
                bad,
                good,
                suggested,
                confirmed,
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
                fileHistories
        );
    }

    private static List<String> getFileHistoriesData(ResultSet rs) throws SQLException {
        List<String> list = new ArrayList<>();
        // Remove the while loop - we're already in the correct row from the calling method
        String fileHistoriesStr = rs.getString(FileInfoConstants.FILEHISTORIES);
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

    /**
     * Loads the file info database for a given folder.
     *
     * @param folderInfo The folder information containing the path to the database.
     * @return true if the file info database was loaded successfully, false otherwise.
     */
	public static boolean loadFileInfoDatabase(FolderInfo folderInfo) {
		boolean loaded = false;
		Connection connection = SqliteConnection.connector(Paths.get(folderInfo.getFolderPath()),
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

        try {
            SQL_Utils.ensureColumnsExist(connection, SQLTableEnums.FILEINFO.getType(), fileInfoColumnsMap);}
        catch (SQLException e) {
            Messages.sprintf("Error ensuring columns exist in file info table: " + e.getMessage());
            throw new RuntimeException(e);
        }
        List<FileInfo> list = new ArrayList<>();
        String sql = "SELECT * FROM " + SQLTableEnums.FILEINFO.getType();

        boolean originalAutoCommit = true;
        try {
            originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            boolean tableCreated = createFileInfoTable(connection);
            Messages.sprintf("tableCreated? " + tableCreated);

            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    if (Main.getProcessCancelled()) {
                        SQL_Utils.rollBackConnection(connection);
                        return new ArrayList<>();
                    }

                    FileInfo finfo = loadFileInfo(rs);
//                    if (finfo == null) {
//                        SQL_Utils.rollBackConnection(connection);
//                        return new ArrayList<>();
//                    }
                    list.add(finfo);
                }

                connection.commit();
                return list;
            }
        } catch (Exception e) {
            Messages.sprintfError("Error loading file info database: " + e.getMessage());
            SQL_Utils.rollBackConnection(connection);
            return new ArrayList<>();
        } finally {
            try {
                connection.setAutoCommit(originalAutoCommit);
            } catch (SQLException e) {
                Messages.sprintfError("Error restoring auto-commit state: " + e.getMessage());
            }
        }
    }

}
