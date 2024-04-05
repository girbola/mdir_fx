package com.girbola.sql;

import com.girbola.Main;
import com.girbola.controllers.main.SQL_Enums;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;

import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FileInfo_SQL {

    private static final String ERROR = FileInfo_SQL.class.getName();

    private static String fileInfoTableColumns() {
        return " (fileInfo_id  INTEGER PRIMARY KEY," + "orgPath STRING UNIQUE," + "workDir STRING,"
                + "workDirDriveSerialNumber STRING," + "destination_Path STRING," + "event           STRING,"
                + "location        STRING," + "tags            STRING," + "camera_model    STRING,"
                + "user			   STRING," + "orientation 	   INTEGER," + "timeshift       INTEGER,"
                + "bad             BOOLEAN," + "good            BOOLEAN," + "suggested       BOOLEAN,"
                + "confirmed       BOOLEAN," + "copied          BOOLEAN," + "ignored         BOOLEAN,"
                + "tableduplicated BOOLEAN," + "image           BOOLEAN," + "video           BOOLEAN,"
                + "raw             BOOLEAN," + "date            NUMERIC," + "size            NUMERIC,"
                + "imageDifferenceHash   INTEGER," + "thumb_offset    INTEGER," + "thumb_length    INTEGER)";

    }

    final static String fileInfoInsert = "INSERT OR REPLACE INTO " + SQL_Enums.FILEINFO.getType() + " ("
            + "'fileInfo_id', " + "'orgPath', " + "'workDir', " + "'workDirDriveSerialNumber', "
            + "'destination_Path', " + "'camera_model', " + "'user', " + "'orientation', " + "'bad'," + "'good', "
            + "'confirmed', " + "'copied'," + "'ignored', " + "'suggested', " + "'image', " + "'raw', " + "'video', "
            + "'timeshift', " + "'date', " + "'size', " + "'tableduplicated', " + "'tags', " + "'event', "
            + "'location', " + "'imageDifferenceHash'," + "'thumb_offset', " + "'thumb_length')"
            + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

//
//	private static String alterTableColumn_(String tableType) {
//		String sql = "ALTER TABLE " + tableType + fileInfoTableColumns();
//		return sql;
//	}

    // @formatter:on
    public static boolean addToFileInfoDB(Connection connection, PreparedStatement pstmt, FileInfo fileInfo) {
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
            pstmt.setLong(25, fileInfo.getImageDifferenceHash());
            pstmt.setInt(26, fileInfo.getThumb_offset());
            pstmt.setInt(27, fileInfo.getThumb_length());
            pstmt.addBatch();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @param connection
     * @param list
     * @return
     */
    // @formatter:on
    public static boolean insertFileInfoListToDatabase(Connection connection, List<FileInfo> list, boolean isWorkDir) {
        Messages.sprintf("insertFileInfoListToDatabase tableCreated Started");
        if (!isWorkDir) {
            boolean clearTable = SQL_Utils.clearTable(connection, SQL_Enums.FILEINFO.getType());
            if (clearTable) {
                Messages.sprintf("FileInfo table cleared");
            }
        }
        try {
            if (!connection.getAutoCommit()) {
                connection.setAutoCommit(false);
            }
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
			for (FileInfo fileInfo : list) {
				Messages.sprintf("=====addToFileInfoDB started: " + fileInfo.getOrgPath());
				addToFileInfoDB(connection, pstmt, fileInfo);
			}
			pstmt.executeBatch();
//			pstmt.closeOnCompletion();
			pstmt.close();
			connection.commit();
			Messages.sprintf("**insertFileInfoListToDatabase tableCreated DONE");
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			Messages.sprintfError("insertFileInfoListToDatabase tableCreated FAILED");
			return false;
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
            if (!connection.getAutoCommit()) {
                connection.setAutoCommit(false);
            }
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
                pstmt.addBatch(fileInfo.getOrgPath());
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
        String orgPath = rs.getString("orgPath");
        Messages.sprintf("loadFileInfo orgPath: " + orgPath);
        String workDir = rs.getString("workDir");
        String workDirDriveSerialNumber = rs.getString("workDirDriveSerialNumber");
        String destPath = rs.getString("destination_Path");
        String event = rs.getString("event");
        String location = rs.getString("location");
        String tags = rs.getString("tags");
        String camera_model = rs.getString("camera_model");
        String user = rs.getString("user");
        int orientation = rs.getInt("orientation");
        long timeShift = rs.getInt("timeshift");
        int fileInfo_id = rs.getInt("fileInfo_id");
        boolean bad = rs.getBoolean("bad");
        boolean good = rs.getBoolean("good");
        boolean suggested = rs.getBoolean("suggested");
        boolean confirmed = rs.getBoolean("confirmed");
        boolean image = rs.getBoolean("image");
        boolean raw = rs.getBoolean("raw");
        boolean video = rs.getBoolean("video");
        boolean ignored = rs.getBoolean("ignored");
        boolean copied = rs.getBoolean("copied");
        boolean tableDuplicated = rs.getBoolean("tableDuplicated");
        long date = rs.getLong("date");
        long size = rs.getLong("size");
        long imageDifferenceHash = rs.getLong("imageDifferenceHash");
        int thumb_offset = rs.getInt("thumb_offset");
        int thumb_lenght = rs.getInt("thumb_length");
        return new FileInfo(orgPath, workDir, workDirDriveSerialNumber, destPath, event, location, tags,
                camera_model, user, orientation, timeShift, fileInfo_id, bad, good, suggested, confirmed, image, raw,
                video, ignored, copied, tableDuplicated, date, size, imageDifferenceHash, thumb_offset, thumb_lenght);
    }

    /*
     * FileInfo
     */
    // @formatter:off
	public static boolean createFileInfoTable(Connection connection) {
		if (connection == null) {
			Messages.sprintf("Connection were null!");
			return false;
		}
		final String sql = "CREATE TABLE IF NOT EXISTS " + SQL_Enums.FILEINFO.getType() + fileInfoTableColumns();

		try {
			Statement stmt = connection.createStatement();
			stmt.execute(sql);
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
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
			if (tableCreated) {
				Messages.sprintf("tableCreated");
			} else {
				Messages.sprintf("NOT tableCreated");
			}
			connection.setAutoCommit(false);
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				FileInfo finfo = loadFileInfo(rs);
                Messages.sprintf("fileinfo Loadedddd: " + finfo.getOrgPath());
				list.add(finfo);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
		return list;
	}

}
