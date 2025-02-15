package com.girbola.sql;

import com.girbola.Main;
import com.girbola.controllers.main.SQL_Enums;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileInfo_SQL {

    private static final String ERROR = FileInfo_SQL.class.getName();

    final static String[] fileInfoColumns = {(
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

    final static String fileInfoInsert = "INSERT OR REPLACE INTO " + SQL_Enums.FILEINFO.getType() + " (" + FileInfoConstants.FILEINFOID + ", "
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


    /**
     * Inserts a list of FileInfo objects into the database. If the isWorkDir parameter is false,
     * the method clears the existing data in the file information table before insertion.
     *
     * @param connection The database connection to use for the operation.
     * @param list       A list of FileInfo objects to be inserted into the database.
     * @param isWorkDir  A boolean indicating whether the operation relates to a working directory.
     *                   If false, the table is cleared prior to insertion.
     * @return A boolean indicating whether the operation was successful. Returns true if the
     * records were inserted successfully and the database operations completed without errors;
     * otherwise, returns false.
     */
    // @formatter:on
    public static boolean insertFileInfoListToDatabase(Connection connection, List<FileInfo> list, boolean isWorkDir) {
        Messages.sprintf("insertFileInfoListToDatabase tableCreated Started");
//        if (!isWorkDir) {
//            boolean clearTable = SQL_Utils.clearTable(connection, SQL_Enums.FILEINFO.getType());
//            if (clearTable) {
//                Messages.sprintf("FileInfo table cleared");
//            }
//        }
        Path folder = null;
        try {
            folder = Paths.get(list.get(0).getOrgPath()).getParent();
            if (Files.exists(folder)) {

            } else {
                return false;
            }
        } catch (Exception e) {
            Messages.sprintf("Cannot get path for the folder");
            return false;
        }
        SqliteConnection.connector(folder, Main.conf.getMdir_db_fileName());
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

            PreparedStatement pstmt = connection.prepareStatement(fileInfoInsert);

            ensureFileInfoColumnsExists(connection);

            for (FileInfo fileInfo : list) {
                Messages.sprintf("=====addToFileInfoDB started: " + fileInfo.getOrgPath());
                addToFileInfoDB(pstmt, fileInfo);
            }
            pstmt.executeBatch();
            pstmt.close();
            connection.commit();
            Messages.sprintf("**insertFileInfoListToDatabase tableCreated DONE");
            return true;
        } catch (Exception ex) {
            Messages.sprintfError("insertFileInfoListToDatabase tableCreated FAILED: " + ex.getMessage());
            return false;
        }
    }

    private static void ensureFileInfoColumnsExists(Connection connection) throws SQLException {

        String fileInfoTable = SQL_Enums.FILEINFO.getType();
        try (Statement stmt = connection.createStatement()) {
            for (String column : fileInfoColumns) {
                String alterTableSQL = "ALTER TABLE " + fileInfoTable + " ADD COLUMN " + column + ";";
                stmt.executeUpdate(alterTableSQL);
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
            String sql = "DELETE FROM " + SQL_Enums.FILEINFO.getType() + " WHERE orgPath = ?";
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

        return new FileInfo(orgPath, workDir, workDirDriveSerialNumber, destPath, event, location, tags, camera_model, user, orientation, timeShift, fileInfo_id, bad, good, suggested, confirmed, image, raw, video, ignored, copied, tableDuplicated, date, size, imageDifferenceHash, thumb_offset, thumb_lenght, fileHistories);
    }

    private static List<String> getFileHistoriesData(ResultSet rs) throws SQLException {
        List<String> list = new ArrayList<>();
        while (rs.next()) {
            String[] fileHistories = rs.getString(FileInfoConstants.FILEHISTORIES).split(",");
            list.addAll(Arrays.asList(fileHistories));
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

        final String sql = "CREATE TABLE IF NOT EXISTS " + SQL_Enums.FILEINFO.getType() + " (" + String.join(",", fileInfoColumns) +");";

        Messages.sprintf("CREATE TABLE IF NOT EXISTS: " + sql);

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
	 * Loads List<FileInfo> using SQL and adds it into FolderInfo
	 * 
	 * @param folderInfo
	 * @return
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
		if (!SQL_Utils.isDbConnected(connection)) {
			Messages.sprintf("loadFileInfoDatabase Not Connected!");
			return new ArrayList<>();
		}
		List<FileInfo> list = new ArrayList<>();
		String sql = "SELECT * FROM " + SQL_Enums.FILEINFO.getType();

		try {
			boolean tableCreated = createFileInfoTable(connection);
        	Messages.sprintf("tableCreated? " + tableCreated);
			connection.setAutoCommit(false);
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
                if(Main.getProcessCancelled()) {
                    return null;
                }
				FileInfo finfo = loadFileInfo(rs);
                if(finfo == null) {
                    return null;
                }
				list.add(finfo);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
		return list;
	}

}
