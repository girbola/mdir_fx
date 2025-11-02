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
import java.sql.*;
import java.util.*;

public class FileInfo_SQL {


//    final public static String[] fileInfoColumnsSQL_ = {
//            FileInfoConstants.BAD + " BOOLEAN",
//            FileInfoConstants.CAMERA_MODEL + " STRING",
//            FileInfoConstants.CONFIRMED + " BOOLEAN",
//            FileInfoConstants.DATE + " NUMERIC",
//            FileInfoConstants.DESTINATIONPATH + " STRING",
//            FileInfoConstants.EVENT + " STRING",
//            FileInfoConstants.FILEINFOID + " INTEGER PRIMARY KEY",
//            FileInfoConstants.FILEHISTORIES + " STRING",
//            FileInfoConstants.GOOD + " BOOLEAN",
//            FileInfoConstants.COPIED + " BOOLEAN",
//            FileInfoConstants.IGNORED + " BOOLEAN",
//            FileInfoConstants.IMAGE + " BOOLEAN",
//            FileInfoConstants.IMAGE_DIFFERENCE_HASH + " STRING",
//            FileInfoConstants.LOCATION + " STRING",
//            FileInfoConstants.MODIFIED + " BOOLEAN",
//            FileInfoConstants.ORG_PATH + " STRING UNIQUE",
//            FileInfoConstants.ORIENTATION + " INTEGER",
//            FileInfoConstants.RAW + " BOOLEAN",
//            FileInfoConstants.SIZE + " NUMERIC",
//            FileInfoConstants.SUGGESTED + " BOOLEAN",
//            FileInfoConstants.TABLE_DUPLICATED + " BOOLEAN",
//            FileInfoConstants.TAGS + " STRING",
//            FileInfoConstants.THUMB_LENGTH + " INTEGER",
//            FileInfoConstants.THUMB_OFFSET + " INTEGER",
//            FileInfoConstants.TIMESHIFT + " INTEGER",
//            FileInfoConstants.USER + " STRING",
//            FileInfoConstants.VIDEO + " BOOLEAN",
//            FileInfoConstants.WORK_DIR + " STRING",
//            FileInfoConstants.WORK_DIR_DRIVE_SERIAL_NUMBER + " STRING"
//    };


    private static final String ERROR = FileInfo_SQL.class.getName();

    // @formatter:on
    // java
    public static boolean addToFileInfoDB(PreparedStatement pstmt, FileInfo fileInfo) {
        try {
            int index = 1;
            pstmt.setInt(index++, fileInfo.getFileInfo_id());
            pstmt.setString(index++, fileInfo.getOrgPath());
            pstmt.setString(index++, fileInfo.getWorkDir());
            pstmt.setString(index++, fileInfo.getWorkDirDriveSerialNumber());
            pstmt.setString(index++, fileInfo.getDestination_Path());
            pstmt.setString(index++, fileInfo.getCamera_model());
            pstmt.setString(index++, fileInfo.getUser());
            pstmt.setInt(index++, fileInfo.getOrientation());
            pstmt.setBoolean(index++, fileInfo.isBad());
            pstmt.setBoolean(index++, fileInfo.isGood());
            pstmt.setBoolean(index++, fileInfo.isConfirmed());
            pstmt.setBoolean(index++, fileInfo.isCopied());
            pstmt.setBoolean(index++, fileInfo.isIgnored());
            pstmt.setBoolean(index++, fileInfo.isModified());
            pstmt.setBoolean(index++, fileInfo.isSuggested());
            pstmt.setBoolean(index++, fileInfo.isImage());
            pstmt.setBoolean(index++, fileInfo.isRaw());
            pstmt.setBoolean(index++, fileInfo.isVideo());
            // ensure DATE is bound after TIMESHIFT to match FileInfoEnum.getAllColumnNames() ordering
            pstmt.setLong(index++, fileInfo.getTimeShift());
            pstmt.setLong(index++, fileInfo.getDate());        // <-- added DATE binding
            pstmt.setLong(index++, fileInfo.getSize());
            pstmt.setBoolean(index++, fileInfo.isTableDuplicated());
            pstmt.setString(index++, fileInfo.getTags());
            pstmt.setString(index++, fileInfo.getEvent());
            pstmt.setString(index++, fileInfo.getLocation());
            pstmt.setString(index++, fileInfo.getImageDifferenceHash());
            pstmt.setInt(index++, fileInfo.getThumb_offset());
            pstmt.setInt(index++, fileInfo.getThumb_length());
            pstmt.setString(index++, convertFileHistoriesToString(fileInfo.getFileHistories()));

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
    public static boolean insertFileInfoListToFileInfoDatabase(FolderInfo folderInfo, boolean isWorkDir) {
        Messages.sprintf("--------------insertFileInfoListToDatabase started: " + folderInfo.getFolderPath() + " isWorkDir: " + isWorkDir);
        Connection mdirDatabaseConnection = SqliteConnection.connectToDatabase(folderInfo.getFolderPath(), Main.conf.getMdir_db_fileName());

        FolderInfo_SQL.saveFolderInfo(mdirDatabaseConnection, folderInfo);

        SQL_Utils.isDbConnected(mdirDatabaseConnection);
        SQL_Utils.setAutoCommit(mdirDatabaseConnection, false);

        List<FileInfo> list = folderInfo.getFileInfoList();

        if (mdirDatabaseConnection == null || list == null || list.isEmpty()) {
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
            boolean tableCreated = createFileInfoTable(mdirDatabaseConnection);
            if (!tableCreated) {
                Messages.sprintfError("Failed to create FileInfo table");
                SQL_Utils.closeConnection(mdirDatabaseConnection);
                return false;
            }
            Messages.sprintf("FileInfo table created/verified");

            if (!SQL_Utils.isDbConnected(mdirDatabaseConnection)) {
                Messages.sprintfError("Database connection lost");
                SQL_Utils.closeConnection(mdirDatabaseConnection);
                return false;
            }
//            SQL_Utils.ensureColumnsExist(mdirDatabaseConnection, SQLTableEnums.FILEINFO.getType(), fileInfoColumnsMap);
            // IMPORTANT: perform any other writes (which may commit/rollback/DDL) BEFORE preparing the statement
            FolderInfo_SQL.saveFolderInfo(mdirDatabaseConnection, folderInfo);
            SQL_Utils.commitChanges(mdirDatabaseConnection);

            // Re-check connection state after external call
            if (!SQL_Utils.isDbConnected(mdirDatabaseConnection)) {
                Messages.sprintfError("Database connection lost after writing folder info");
                SQL_Utils.closeConnection(mdirDatabaseConnection);
                return false;
            }
//            String sqlInsert =
//                    "INSERT OR REPLACE INTO " + SQLTableEnums.FILEINFO.getType() + " (" +
//                            FileInfoEnum.getAllColumnNames() +
//                            ") VALUES (" +
//                            "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?" +
//                            ");";


// java
            String[] cols = FileInfoEnum.getAllColumnNames().split("\\s*,\\s*");
            int colCount = cols.length;
            String placeholders = String.join(", ", Collections.nCopies(colCount, "?"));
            String sqlInsert = "INSERT OR REPLACE INTO " + SQLTableEnums.FILEINFO.getType() + " (" +
                    FileInfoEnum.getAllColumnNames() +
                    ") VALUES (" + placeholders + ");";

            Messages.sprintf("FileInfo columns count: " + colCount + " columns: " + Arrays.toString(cols));

            Messages.sprintf("FileInfoEnum.getAllColumnNames():::::::::: " + sqlInsert);
            try (PreparedStatement pstmt = mdirDatabaseConnection.prepareStatement(sqlInsert)) {

                int batchSize = 0;
                final int BATCH_LIMIT = 1000;

                for (FileInfo fileInfo : list) {
                    Messages.sprintf("FIQ - Processing file: " + fileInfo.getOrgPath());
                    if (!addToFileInfoDB(pstmt, fileInfo)) {
                        throw new SQLException("Failed to add file info to database: " + fileInfo.getOrgPath());
                    }

                    batchSize++;
                    if (batchSize >= BATCH_LIMIT) {
                        pstmt.executeBatch();
                        mdirDatabaseConnection.commit();
                        batchSize = 0;
                    }
                }

                if (batchSize > 0) {
                    pstmt.executeBatch();
                    mdirDatabaseConnection.commit();
                }

                Messages.sprintf("Successfully inserted all file info records");
                return true;
            }
        } catch (SQLException ex) {
            try {
                mdirDatabaseConnection.rollback();
            } catch (SQLException rollbackEx) {
                Messages.sprintfError("Failed to rollback transaction: " + rollbackEx.getMessage());
            }
            Messages.sprintfError("Failed to insert file info records: " + ex);
            ex.printStackTrace();
            return false;
        } finally {
            if (SQL_Utils.isDbConnected(mdirDatabaseConnection)) {
                SQL_Utils.closeConnection(mdirDatabaseConnection);
            }
        }
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

            for (String columnDef : FileInfoEnum.getAllColumnNames().split(",")) {
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
        boolean modified = rs.getBoolean(FileInfoConstants.MODIFIED);
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
        long timeShift = rs.getLong(FileInfoConstants.TIMESHIFT); // <-- use getLong to match stored type

        List<String> fileHistories = getFileHistoriesData(rs);

        return new FileInfo(orgPath, workDir, workDirDriveSerialNumber, destPath, event, location, tags, camera_model, user, orientation, timeShift, fileInfo_id, bad, good, suggested, confirmed, modified, image, raw, video, ignored, copied, tableDuplicated, date, size, imageDifferenceHash, thumb_offset, thumb_lenght, fileHistories);
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

//        try {
//            SQL_Utils.ensureColumnsExist(connection, SQLTableEnums.FILEINFO.getType(), fileInfoColumnsMap);}
//        catch (SQLException e) {
//            Messages.sprintf("Error ensuring columns exist in file info table: " + e.getMessage());
//            throw new RuntimeException(e);
//        }
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
