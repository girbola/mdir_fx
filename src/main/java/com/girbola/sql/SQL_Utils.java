package com.girbola.sql;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.girbola.Main;
import com.girbola.controllers.folderscanner.Model_folderScanner;
import com.girbola.controllers.folderscanner.SelectedFolder;
import com.girbola.controllers.main.Model_main;
import com.girbola.controllers.main.SQL_Enums;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.drive.DriveInfo;
import com.girbola.fileinfo.FileInfo;
import com.girbola.fileinfo.ThumbInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;

import javafx.collections.ObservableList;

public class SQL_Utils {
	final private static String ERROR = SQL_Utils.class.getSimpleName();

	// @formatter:off
	final static String thumbInfoInsert = "INSERT OR REPLACE INTO " + SQL_Enums.THUMBINFO.getType() + " ('id',"
			+ "'filepath', " + "'thumb_width', " + "'thumb_height', " + "'thumb_fast_width', " + "'thumb_fast_height', "
			+ "'orientation', " + "'image_0', " + "'image_1', " + "'image_2', " + "'image_3', "
			+ "'image_4') VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";

	final static String folderInfoDatabaseSQL = "CREATE TABLE IF NOT EXISTS " + SQL_Enums.FOLDERINFO.getType()
			+ " (path STRING NOT NULL UNIQUE PRIMARY KEY, " + "justFolderName STRING, " + "tableType STRING NOT NULL, "
			+ "connected BOOLEAN)";

	final static String selectedFolderTable = "CREATE TABLE IF NOT EXISTS " + SQL_Enums.SELECTEDFOLDERS.getType()
			+ " (path STRING PRIMARY KEY, connected BOOLEAN)";

	/*
	 * this.orgPath = aOrgPath; this.fileInfo_id = fileInfo_id; this.destinationPath
	 * = ""; this.event = ""; this.location = ""; this.tags = ""; this.camera_model
	 * = "Unknown"; this.orientation = 0; this.timeShift = 0; this.bad = false;
	 * this.good = false; this.suggested = false; this.confirmed = false; this.raw =
	 * false; this.image = false; this.video = false; this.ignored = false;
	 * this.tableDuplicated = false; this.date = 0; this.size = 0; this.thumb_offset
	 * = 0; this.thumb_length = 0; this.user = "";
	 */
	final static String fileInfoInsert = "INSERT OR REPLACE INTO " + SQL_Enums.FILEINFO.getType() + " ("
			+ "'fileInfo_id', " + "'orgPath', " + "'workDir', " + "'workDirDriveSerialNumber', "
			+ "'destination_Path', " + "'camera_model', " + "'user', " + "'orientation', " + "'bad'," + "'good', "
			+ "'confirmed', " + "'copied'," + "'ignored', " + "'suggested', " + "'image', " + "'raw', " + "'video', "
			+ "'timeshift', " + "'date', " + "'size', " + "'tableduplicated', " + "'tags', " + "'event', "
			+ "'location', " + "'thumb_offset', " + "'thumb_length')"
			+ " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

	final static String selectedFoldersInsert = "INSERT OR REPLACE INTO " + SQL_Enums.SELECTEDFOLDERS.getType()
			+ " ('path', 'connected') VALUES(?,?)";
	final static String folderInfoInsert = "INSERT OR REPLACE INTO " + SQL_Enums.FOLDERINFO.getType() + " ("
			+ "'path', " + "'tableType', " + "'justFolderName', " + "'connected')" + " VALUES(?,?,?,?)";

	final static String insertDriveInfo = "INSERT OR REPLACE INTO " + SQL_Enums.DRIVEINFO.getType() + "('drivePath', "
			+ "'identifier', " + "'totalSize', " + "'connected,' " + "'selected')" + " VALUES(?,?,?,?,?)";

	private static String getFileInfoTable(String tableType) {
		String sql = "CREATE TABLE IF NOT EXISTS " + tableType + fileInfoTableColumns();
		return sql;
	}

	private static String alterTableColumn_(String tableType) {
		String sql = "ALTER TABLE " + tableType + fileInfoTableColumns();
		return sql;
	}

	private static String fileInfoTableColumns() {
		String sql = " (fileInfo_id  INTEGER PRIMARY KEY," + "orgPath STRING UNIQUE," + "workDir STRING,"
				+ "workDirDriveSerialNumber STRING," + "destination_Path STRING," + "event           STRING,"
				+ "location        STRING," + "tags            STRING," + "camera_model    STRING,"
				+ "user			   STRING," + "orientation 	   INTEGER," + "timeshift       INTEGER,"
				+ "bad             BOOLEAN," + "good            BOOLEAN," + "suggested       BOOLEAN,"
				+ "confirmed       BOOLEAN," + "copied          BOOLEAN," + "ignored         BOOLEAN,"
				+ "tableduplicated BOOLEAN," + "image           BOOLEAN," + "video           BOOLEAN,"
				+ "raw             BOOLEAN," + "date            NUMERIC," + "size            NUMERIC,"
				+ "thumb_offset    INTEGER," + "thumb_length    INTEGER)";

		return sql;
	}

	/*
	 * DriveInfo
	 */
	private static boolean createDriveInfoTable(Connection connection) {
		Messages.sprintf("createDriveInfoTable creating..." + connection);
		if (connection == null) {
			return false;
		}
		if (!isDbConnected(connection)) {
			Messages.sprintf("NOT connected");
			return false;
		}
		try {
			Statement stmt = connection.createStatement();
			String sql = "CREATE TABLE IF NOT EXISTS " + SQL_Enums.DRIVEINFO.getType() + " ("
					+ "drivePath STRING NOT NULL, " + "driveTotalSize INTEGER, " + "identifier STRING, "
					+ "driveSelected STRING," + "driveConnected BOOLEAN)";
			stmt.execute(sql);
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public boolean addDriveInfo(Connection connection, DriveInfo driveInfo) {
		if (connection == null) {
			return false;
		}
		createDriveInfoTable(connection);
		try {
			PreparedStatement pstmt = connection.prepareStatement(insertDriveInfo);
			pstmt.setString(1, driveInfo.getDrivePath());
			pstmt.setBoolean(2, driveInfo.isConnected());
			pstmt.setBoolean(3, driveInfo.getSelected());
			pstmt.setLong(4, driveInfo.getDriveTotalSize());
			pstmt.setString(5, driveInfo.getIdentifier());

			pstmt.executeUpdate();
			pstmt.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean addDriveInfo_list(Connection connection, List<DriveInfo> driveInfo_list) {
		if (connection == null) {
			return false;
		}
		createDriveInfoTable(connection);

		try {
			PreparedStatement pstmt = connection.prepareStatement(insertDriveInfo);
			for (DriveInfo driveInfo : driveInfo_list) {
				pstmt.setString(1, driveInfo.getDrivePath());
				pstmt.setBoolean(2, driveInfo.isConnected());
				pstmt.setBoolean(3, driveInfo.getSelected());
				pstmt.setLong(4, driveInfo.getDriveTotalSize());
				pstmt.setString(5, driveInfo.getIdentifier());
				pstmt.addBatch();
			}
			pstmt.executeBatch();
			if (pstmt != null) {
				pstmt.close();
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	// @formatter:on
	public static boolean loadDriveInfo(Connection connection, Model_folderScanner model_folderScanner) {
		String sql = "SELECT * FROM " + SQL_Enums.DRIVEINFO.getType();
		try {
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				String drivePath = rs.getString("drivePath");
				boolean isConnected = rs.getBoolean("driveConnected");
				boolean isSelected = rs.getBoolean("driveSelected");
				long driveTotalSize = rs.getLong("driveTotalSize");
				String identifier = rs.getString("identifier");
				// SelectedFolder sel = new SelectedFolder(isConnected, drivePath);
				DriveInfo driveInfo = new DriveInfo(drivePath, driveTotalSize, isConnected, isSelected, identifier);
				model_folderScanner.drive().getDrivesList_obs().add(driveInfo);
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/*
	 * FolderInfo
	 */
	public static boolean createFolderInfoDatabase(Connection connection) {
		if (connection == null) {
			Messages.sprintfError("Can't connect folderInfo.db!!");
			return false;
		}
		if (!isDbConnected(connection)) {
			Messages.sprintf("createFolderInfoDatabase NOT connected");
			return false;
		}
		try {
			Statement stmt = connection.createStatement();
			stmt.execute(folderInfoDatabaseSQL);
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public static boolean createFolderInfoDatabase() {
		Connection connection = SqliteConnection.connector(Main.conf.getAppDataPath(),
				Main.conf.getFolderInfo_db_fileName());
		if (connection == null) {
			Messages.sprintfError("Can't connect folderInfo.db!!");
		}
		if (!isDbConnected(connection)) {
			Messages.sprintf("createFolderInfoDatabase NOT connected");
			return false;
		}
		try {
			Statement stmt = connection.createStatement();
			stmt.execute(folderInfoDatabaseSQL);
			stmt.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		} finally {
			try {
				connection.close();
			} catch (Exception e) {
				return false;
			}

		}
		return true;
	}

	// @formatter:on
	public static boolean addToFolderInfoDB(Connection connection, FolderInfo folderInfo) {
		if (connection == null) {
			return false;
		}
		createFolderInfoDatabase(connection);
		try {
			PreparedStatement pstmt = connection.prepareStatement(folderInfoInsert);
			pstmt.setString(1, folderInfo.getFolderPath());
			pstmt.setString(2, folderInfo.getTableType());
			pstmt.setString(3, folderInfo.getJustFolderName());
			pstmt.setBoolean(4, folderInfo.isConnected());

			pstmt.executeUpdate();
			pstmt.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static List<FolderInfo> loadFolderInfoTo_Tables(Connection connection, Model_main model_Main) {
		if (connection == null) {
			Messages.sprintf("Not connected NULL!");
		}
		if (isDbConnected(connection)) {
			Messages.sprintf("Connected!");
		} else {
			Messages.sprintf("Not Connected!");
			return null;
		}

		String sql = "SELECT * FROM " + SQL_Enums.FOLDERINFO.getType();
		try {
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			List<FolderInfo> arrayList = new ArrayList<>();

			while (rs.next()) {
				Path path = Paths.get(rs.getString("path"));
				String tableType = rs.getString("tableType");
				String justFolderName = rs.getString("justFolderName");
				boolean isConnected = rs.getBoolean("connected");
				if (path == null) {
					Messages.sprintf("Something went badly wrong!");
					Messages.errorSmth(ERROR, "Something went terrible wrong at: " + path, null, Misc.getLineNumber(),
							true);
					return null;
				}
				FolderInfo folderInfo = new FolderInfo(path, tableType, justFolderName, isConnected);
				folderInfo.setConnected(Files.exists(path));
				Messages.sprintf("path: " + path + " Folderinfo were connected? " + folderInfo.isConnected()
						+ " folderInfo: " + folderInfo.getFolderFiles());
				arrayList.add(folderInfo);
			}
			return arrayList;
		} catch (Exception e) {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
			return null;
		}

	}

	/*
	 * SelectedFolders
	 */

	private static boolean addToSelectedFoldersDB(Connection connection, PreparedStatement pstmt,
			SelectedFolder selectedFolder) {
		try {
			pstmt.setString(1, selectedFolder.getFolder());
			pstmt.setBoolean(2, selectedFolder.connected_property().get());
			Messages.sprintf("selectedfolder is. " + selectedFolder.getFolder() + " is connected? "
					+ selectedFolder.connected_property().get());
			pstmt.addBatch();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean createSelectedFoldersTable(Connection connection) {
		if (connection == null) {
			Messages.sprintf("createSelectedFoldersTable Connection were null!");
			return false;
		}
		if (!SQL_Utils.isDbConnected(connection)) {
			Messages.sprintf("createSelectedFoldersTable Not connected");
			return false;
		}
		try {
			Statement stmt = connection.createStatement();
			stmt.execute(selectedFolderTable);
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public static boolean insertSelectedFolders_List_ToDB(Connection connection,
			List<SelectedFolder> selectedFolder_list) {
		Messages.sprintf("insertSelectedFolders_List_ToDB: " + selectedFoldersInsert);
		createSelectedFoldersTable(connection);
		try {
			connection.setAutoCommit(false);
			PreparedStatement pstmt = connection.prepareStatement(selectedFoldersInsert);
			connection.setAutoCommit(false);
			for (SelectedFolder selectedFolder : selectedFolder_list) {
				Messages.sprintf("select: " + selectedFolder.getFolder());
				if (Files.exists(Paths.get(selectedFolder.getFolder()))) {
					addToSelectedFoldersDB(connection, pstmt, selectedFolder);
				} else {
					Messages.sprintfError("insertSelectedFolders_List_ToDB SelectedFolder did not exist: "
							+ selectedFolder.getFolder());
					break;
				}
			}
			pstmt.executeBatch();
			connection.commit();
			if (pstmt != null) {
				pstmt.close();
			}
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public static boolean insertSelectedFolder_ToDB(Connection connection, SelectedFolder selectedFolder) {
		Messages.sprintf("insertSelectedFolder_ToDB: " + selectedFolder.getFolder());
		createSelectedFoldersTable(connection);
		try {
			connection.setAutoCommit(false);
			PreparedStatement pstmt = connection.prepareStatement(selectedFoldersInsert);
			Messages.sprintf("selectedFoldersInsert: " + selectedFoldersInsert + " create one: " + selectedFolderTable);
			addToSelectedFoldersDB(connection, pstmt, selectedFolder);
			connection.commit();
			int[] value = pstmt.executeBatch();
			for (int v : value) {
				Messages.sprintf("v: " + v);
			}

			// if (connection != null) {
			// connection.close();
			// }
			if (pstmt != null) {
				pstmt.close();
			}
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public static boolean loadSelectedFolders_list(Connection connection, Model_main model_Main) {
		if (connection == null) {
			Messages.sprintfError("Connection were null!");
			return false;
		}
		if (!isDbConnected(connection)) {
			Messages.sprintf("NOT connected");
			return false;
		}
		try {
			String sql = "SELECT * FROM " + SQL_Enums.SELECTEDFOLDERS.getType();
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				Messages.sprintf("loadFolders_list starting: " + sql);
				String path = rs.getString("path");
				boolean connected = rs.getBoolean("connected");
				SelectedFolder selectedFolder = new SelectedFolder(connected, path);
				model_Main.getSelectedFolders().getSelectedFolderScanner_obs().add(selectedFolder);
			}
			Messages.sprintf(
					"size of sel obs= " + model_Main.getSelectedFolders().getSelectedFolderScanner_obs().size());
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean loadFolders_list(Connection connection, Model_main model_Main) {
		if (connection == null) {
			Messages.sprintfError("Connection were null!");
			return false;
		}
		if (!isDbConnected(connection)) {
			Messages.sprintf("NOT connected");
			return false;
		}
		try {
			String sql = "SELECT * FROM " + SQL_Enums.SELECTEDFOLDERS.getType();
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {

				Messages.sprintf("loadFolders_list starting: " + sql);
				String path = rs.getString("path");
				boolean connected = rs.getBoolean("connected");
				SelectedFolder selectedFolder = new SelectedFolder(connected, path);
				model_Main.getSelectedFolders().getSelectedFolderScanner_obs().add(selectedFolder);
			}
			Messages.sprintf(
					"size of sel obs= " + model_Main.getSelectedFolders().getSelectedFolderScanner_obs().size());
			for (SelectedFolder self : model_Main.getSelectedFolders().getSelectedFolderScanner_obs()) {
				Messages.sprintf("selectedFolder: " + self.getFolder() + " isConnected? " + self.isConnected());
			}
			return true;
		} catch (Exception e) {
			return false;
		}
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
		try {
			Statement stmt = connection.createStatement();
			stmt.execute(getFileInfoTable(SQL_Enums.FILEINFO.getType()));
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public static boolean insertFileInfoToDatabase(Connection connection, FileInfo fileInfo) {
		Messages.sprintf("insertFileInfo_list");
		createFileInfoTable(connection);
		if (!isDbConnected(connection)) {
			Messages.sprintf("NOT connected");
			return false;
		}
		try {
			connection.setAutoCommit(false);
			Messages.sprintf("insertFileInfoToDatabase file id: " + fileInfo.getFileInfo_id());
			PreparedStatement pstmt = connection.prepareStatement(fileInfoInsert);
			addToFileInfoDB(connection, pstmt, fileInfo);
			int[] count = pstmt.executeBatch();
			connection.commit();
			if (count.length >= 1) {
				Messages.sprintf("counted= " + count);
			}
			connection.close();
			pstmt.close();
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public static int countTableRows(Connection connection, String table) {
		try {
			Statement stmt = connection.createStatement();
			ResultSet res = stmt.executeQuery("select * from " + table);
			res.last(); // record pointer is placed on the last row.
			int counter = res.getRow();
			System.out.println("Number of records in ResultSet: " + counter);
			return counter;
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * 
	 * @param connection
	 * @param list
	 * @return
	 */
	// @formatter:on
	public static boolean insertFileInfoListToDatabase(Connection connection, List<FileInfo> list, boolean isWorkDir) {
		Messages.sprintf("insertFileInfoListToDatabase tableCreated Started");
		if (!isWorkDir) {
			boolean clearTable = clearTable(connection, SQL_Enums.FILEINFO.getType());
			if (clearTable) {
				boolean tableCreated = createFileInfoTable(connection);
				if (tableCreated) {
					Messages.sprintf("insertFileInfoListToDatabase tableCreated");
				} else {
					Messages.sprintf("insertFileInfoListToDatabase NOT tableCreated");
				}
				if (!isDbConnected(connection)) {
					Messages.sprintf("insertFileInfoListToDatabase Not connected");
					return false;
				}
			}
		}
		try {
			connection.setAutoCommit(false);
			PreparedStatement pstmt = null;
			pstmt = connection.prepareStatement(fileInfoInsert);
			for (FileInfo fileInfo : list) {
				long start = System.currentTimeMillis();
				Messages.sprintf("=====addToFileInfoDB started: " + fileInfo.getOrgPath());
				addToFileInfoDB(connection, pstmt, fileInfo);
				Messages.sprintf(
						"============addToFileInfoDB ENDED and it took: " + (System.currentTimeMillis() - start));
			}
			pstmt.executeBatch();
			Messages.sprintf("**********addToFileInfoDB pstmt.executeBatch();");
			connection.commit();
			Messages.sprintf("****connection.commit();");

			if (connection != null) {
				connection.close();
			}
			if (pstmt != null) {
				pstmt.close();
			}
			Messages.sprintf("**insertFileInfoListToDatabase tableCreated DONE");
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			Messages.sprintf("insertFileInfoListToDatabase tableCreated FAILED");
			return false;
		}
	}

	// @formatter:off
	public static List<FileInfo> loadFileInfoDatabase(Connection connection) {

		if (connection == null) {
			return null;
		}
		if (!isDbConnected(connection)) {
			Messages.sprintf("loadFileInfoDatabase Not Connected!");
			return null;
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
				list.add(finfo);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	public static FileInfo loadFileInfo(ResultSet rs) throws SQLException {
		String orgPath = rs.getString("orgPath");
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
		int thumb_offset = rs.getInt("thumb_offset");
		int thumb_lenght = rs.getInt("thumb_length");
		FileInfo finfo = new FileInfo(orgPath, workDir, workDirDriveSerialNumber, destPath, event, location, tags,
				camera_model, user, orientation, timeShift, fileInfo_id, bad, good, suggested, confirmed, image, raw,
				video, ignored, copied, tableDuplicated, date, size, thumb_offset, thumb_lenght);
		return finfo;
	}
	/*
	 * ThumbInfo
	 */

	private static boolean addToThumbInfoDB(Connection connection, PreparedStatement pstmt, ThumbInfo thumbInfo) {
		try {
			pstmt.setInt(1, thumbInfo.getId());
			pstmt.setString(2, thumbInfo.getFileName());
			pstmt.setDouble(3, thumbInfo.getThumb_width());
			pstmt.setDouble(4, thumbInfo.getThumb_height());
			pstmt.setDouble(5, thumbInfo.getThumb_fast_width());
			pstmt.setDouble(6, thumbInfo.getThumb_fast_height());
			pstmt.setDouble(7, thumbInfo.getOrientation());

			final int tsize = thumbInfo.getThumbs().size();
			int c = 8;
			for (int i = 0; i < (tsize); i++) {
				pstmt.setBytes((i + 8), thumbInfo.getThumbs().get(i));
				c++;
			}
			for (int i = c; i < (13); i++) {
				pstmt.setBytes((i), null);
			}

			pstmt.addBatch();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				pstmt.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			return false;
		}
	}

	// @formatter:off
	public static boolean createThumbInfoTable(Connection connection) {
		if (connection == null) {
			return false;
		}
		try {
			Statement stmt = connection.createStatement();
			connection.setAutoCommit(false);
			String sql = "CREATE TABLE IF NOT EXISTS " + SQL_Enums.THUMBINFO.getType() + " (id INTEGER PRIMARY KEY,"
					+ " filepath STRING UNIQUE NOT NULL," + " thumb_width  DOUBLE," + " thumb_height DOUBLE,"
					+ " thumb_fast_width  DOUBLE," + " thumb_fast_height DOUBLE," + " orientation INTEGER,"
					+ " image_0 BLOB NULL," + " image_1  BLOB NULL," + " image_2 BLOB NULL," + " image_3  BLOB NULL,"
					+ " image_4  BLOB NULL)";

			stmt.execute(sql);
			connection.commit();

			stmt.close();
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	// @formatter:on
	public static boolean insertThumbInfo(Connection connection, int id, ThumbInfo thumbInfo) {
		createThumbInfoTable(connection);
		if (!isDbConnected(connection)) {
			return false;
		}

		try {
			connection.setAutoCommit(false);
			PreparedStatement pstmt = connection.prepareStatement(thumbInfoInsert);
			addToThumbInfoDB(connection, pstmt, thumbInfo);
			pstmt.executeBatch();
			connection.commit();
			pstmt.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean insertThumbInfoListToDatabase(Connection connection, List<ThumbInfo> thumbInfoList) {
		createThumbInfoTable(connection);

		if (!isDbConnected(connection)) {
			Messages.sprintf("insertThumbInfoListToDatabase NOT connected");
			return false;
		}

		try {
			connection.setAutoCommit(false);
			PreparedStatement pstmt = null;
			pstmt = connection.prepareStatement(thumbInfoInsert);
			for (ThumbInfo thumbInfo : thumbInfoList) {
				addToThumbInfoDB(connection, pstmt, thumbInfo);
			}
			pstmt.executeBatch();
			connection.commit();
			pstmt.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	public static List<ThumbInfo> loadThumbInfo_list(Connection connection) {
		if (connection == null) {
			return null;
		}
		List<ThumbInfo> list = new ArrayList<>();
		if (!isDbConnected(connection)) {
			return null;
		}
		try {
			String sql = "SELECT * FROM " + SQL_Enums.THUMBINFO.getType();
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				Messages.sprintf("stmt starting: " + sql);
				ThumbInfo thumbInfo = thumbInfoCreation(rs);
				list.add(thumbInfo);
			}
		} catch (Exception e) {
			return null;
		}
		return list;
	}

	private static ThumbInfo thumbInfoCreation(ResultSet rs) throws SQLException {
		String fileName = rs.getString("filename");
		int id = rs.getInt("id");
		double thumb_width = rs.getDouble("thumb_width");
		double thumb_height = rs.getDouble("thumb_height");
		double thumb_fast_width = rs.getDouble("thumb_fast_width");
		double thumb_fast_height = rs.getDouble("thumb_fast_height");
		double orientation = rs.getDouble("orientation");
		byte[] byte0 = rs.getBytes("image_0");
		byte[] byte1 = rs.getBytes("image_1");
		byte[] byte2 = rs.getBytes("image_2");
		byte[] byte3 = rs.getBytes("image_3");
		byte[] byte4 = rs.getBytes("image_4");
		ArrayList<byte[]> byteList = new ArrayList<>(Arrays.asList(byte0, byte1, byte2, byte3, byte4));

		ThumbInfo thumbInfo = new ThumbInfo(id, fileName, thumb_width, thumb_height, thumb_fast_width,
				thumb_fast_height, orientation, byteList);
		return thumbInfo;
	}

	// @formatter:off
	public static ThumbInfo loadThumbInfo(Connection connection, int thumbInfo_ID) {
		Messages.sprintf("Loading thumbinfo SQL id= " + thumbInfo_ID);
		if (connection == null) {
			Messages.errorSmth(ERROR, "Connection was unable to establish", null, Misc.getLineNumber(), true);
			return null;
		}
		if (!isDbConnected(connection)) {
			return null;
		}
		ThumbInfo thumbInfo = null;
		try {
			String sql = "SELECT id, " + "filename, " + "thumb_width, " + "thumb_height, " + "thumb_fast_width, "
					+ "thumb_fast_height, " + "orientation, " + "image_0, " + "image_1, " + "image_2, " + "image_3, "
					+ "image_4 FROM " + SQL_Enums.THUMBINFO.getType() + " WHERE id = ?";
			// @formatter:on
			PreparedStatement pstmt = connection.prepareStatement(sql);
			pstmt.setInt(1, thumbInfo_ID);
			pstmt.executeQuery();
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				int id = rs.getInt("id");
				String filePath = rs.getString("filename");
				double thumb_width = rs.getDouble("thumb_width");
				double thumb_height = rs.getDouble("thumb_height");
				double thumb_fast_width = rs.getDouble("thumb_fast_width");
				double thumb_fast_height = rs.getDouble("thumb_fast_height");
				int orientation = rs.getInt("orientation");
				byte[] image_0 = rs.getBytes("image_0");
				byte[] image_1 = rs.getBytes("image_1");
				byte[] image_2 = rs.getBytes("image_2");
				byte[] image_3 = rs.getBytes("image_3");
				byte[] image_4 = rs.getBytes("image_4");

				thumbInfo = new ThumbInfo(id, filePath, thumb_width, thumb_height, thumb_fast_width, thumb_fast_height,
						orientation, new ArrayList<>(Arrays.asList(image_0, image_1, image_2, image_3, image_4)));
				thumbInfo = thumbInfoCreation(rs);
				return thumbInfo;
			}
		} catch (Exception e) {
			return null;
		}
		return thumbInfo;
	}

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
			pstmt.setInt(25, fileInfo.getThumb_offset());
			pstmt.setInt(26, fileInfo.getThumb_length());
			pstmt.addBatch();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean isDbConnected(Connection connection) {
		if (connection == null) {
			return false;
		}
		try {
			return !connection.isClosed();
		} catch (SQLException e) {
			return false;
		}

	}

	public String getRightTableFilename(String table) {
		if (table.equals(SQL_Enums.FILEINFO.getType())) {
			return Main.conf.getFileInfo_db_fileName();
		} else if (table.equals(SQL_Enums.FOLDERINFO.getType())) {
			return Main.conf.getFolderInfo_db_fileName();
		} else if (table.equals(SQL_Enums.SELECTEDFOLDERS.getType())) {
			return Main.conf.getSelectedFolders_db_fileName();
		} else if (table.equals(SQL_Enums.THUMBINFO.getType())) {
			return Main.conf.getThumbInfo_db_fileName();
		}
		return null;

	}

	public static FolderInfo loadFolderInfo(Path path, String table, String value) {
		Messages.sprintf("loadFolderInfo; " + path);
		FolderInfo folderInfo = new FolderInfo();
		try {
			Connection connection = SqliteConnection.connector(path, Main.conf.getFolderInfo_db_fileName());
			if (!SqliteConnection.tableExists(connection, Main.conf.getFolderInfo_db_fileName())) {
				Messages.sprintf("loadFolderInfo TAble has NO data!");
				return null;
			}
			String sql = "SELECT * FROM " + table + " WHERE path = " + value;
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(sql);

			Path filePath = Paths.get(rs.getString("path"));
			String tableType = rs.getString("tableType");
			boolean isConnected = rs.getBoolean("connected");
			Messages.sprintf("loadFolderInfo: " + path + "SQL: path: " + filePath + " tableType: " + tableType
					+ " iscon? " + isConnected);
			if (Files.exists(filePath)) {
				Connection fileInfo_connection = SqliteConnection.connector(filePath,
						Main.conf.getFileInfo_db_fileName());
				List<FileInfo> list = loadFileInfoDatabase(fileInfo_connection);
				if (list != null) {
					if (!list.isEmpty()) {
						folderInfo.setFileInfoList(list);
						TableUtils.updateFolderInfos_FileInfo(folderInfo);
						return folderInfo;
					} else {
						Messages.sprintf("loadFolderInfo List were emptyyyyy");
						folderInfo.setConnected(false);
					}
				} else {
					folderInfo.setConnected(false);
					return null;
				}
			} else {
				folderInfo.setConnected(false);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}

	public static boolean removeAllData(Connection connection, String tableName, String path) {
		if (connection == null) {
			return false;
		}
		if (!isDbConnected(connection)) {
			return false;
		}
		String sql = "DELETE FROM " + tableName + " WHERE path LIKE " + path;
		try {
			Statement stmt = connection.createStatement();
			stmt.execute(sql);
		} catch (Exception e) {
			return false;
		}

		return false;
	}

	public static boolean removeAllData_list(Connection connection, ArrayList<SelectedFolder> listToRemove,
			String table) {
		if (connection == null) {
			return false;
		}
		if (!isDbConnected(connection)) {
			return false;
		}

		String sql = "DELETE FROM " + table + " WHERE path = ?";
		try {
			connection.setAutoCommit(false);
			PreparedStatement pstmt = connection.prepareStatement(sql);

			for (SelectedFolder self : listToRemove) {
				Messages.sprintf("Self path is: " + self.getFolder());
				pstmt.setString(1, self.getFolder());
				pstmt.addBatch();
			}
			pstmt.executeBatch();
			connection.commit();
			pstmt.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean clearTable(Connection connection, String table) {
		final String sql = "DROP TABLE " + table;
		try {
			Statement stmt = connection.createStatement();
			stmt.executeUpdate(sql);
			stmt.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/*
	 * WorkDir
	 */

	public static boolean createWorkDirTable(Connection connection) {
		if (connection == null) {
			return false;
		}
		if (!isDbConnected(connection)) {
			Messages.sprintf("NOT connected");
			return false;
		}
		try {
			Statement stmt = connection.createStatement();
			stmt.execute(getFileInfoTable(SQL_Enums.WORKDIR.getType()));
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public static boolean insertToWorkDirFileInfo_list(Connection connection, List<FileInfo> list) {
		createWorkDirTable(connection);
		if (!isDbConnected(connection)) {
			Messages.sprintf("insertWorkDir Not connected");
			return false;
		}
		try {
			connection.setAutoCommit(false);
			PreparedStatement pstmt = null;
			pstmt = connection.prepareStatement(fileInfoInsert);
			for (FileInfo fileInfo : list) {
				addToFileInfoDB(connection, pstmt, fileInfo);
			}
			pstmt.executeBatch();
			connection.commit();
			if (connection != null) {
				connection.close();
			}
			if (pstmt != null) {
				pstmt.close();
			}
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}

	}

	public static boolean insertToWorkDirFileInfo(Connection connection, FileInfo fileInfo) {
		createWorkDirTable(connection);
		if (!isDbConnected(connection)) {
			Messages.sprintf("insertWorkDir Not connected");
			return false;
		}
		try {
			connection.setAutoCommit(false);
			PreparedStatement pstmt = null;
			pstmt = connection.prepareStatement(fileInfoInsert);
			addToFileInfoDB(connection, pstmt, fileInfo);
			pstmt.executeBatch();
			connection.commit();
			if (connection != null) {
				connection.close();
			}
			if (pstmt != null) {
				pstmt.close();
			}
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}

	}

	public static boolean loadFileInfoDatabase(FolderInfo folderInfo) {
		boolean loaded = false;
		Connection connection = SqliteConnection.connector(Paths.get(folderInfo.getFolderPath()),
				Main.conf.getFileInfo_db_fileName());
		if (SQL_Utils.isDbConnected(connection)) {

			List<FileInfo> fileInfo_list = loadFileInfoDatabase(connection);
			if (!fileInfo_list.isEmpty()) {
				folderInfo.setFileInfoList(fileInfo_list);
				TableUtils.updateFolderInfos_FileInfo(folderInfo);
				loaded = true;
			}
		}
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return loaded;
	}

	public static void removeSelectedFolder_FromDB(Connection connection, String path) {
		try {
			String sql = "DELETE FROM " + SQL_Enums.SELECTEDFOLDERS.getType() + " WHERE path = ?";
			PreparedStatement pstmt = connection.prepareStatement(sql);
			pstmt.setString(1, path);
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void insertFileInfoListToWorkdirDatabase(Connection connection, List<FileInfo> listCopiedFiles,
			boolean isWorkdir) {

		
	}

}
