package com.girbola.sql;

import com.girbola.Main;
import com.girbola.controllers.folderscanner.Model_folderScanner;
import com.girbola.controllers.folderscanner.SelectedFolder;
import com.girbola.controllers.main.Model_main;
import com.girbola.controllers.main.SQL_Enums;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.drive.DriveInfo;
import com.girbola.fileinfo.ThumbInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SQL_Utils extends FolderInfo_SQL {
	final private static String ERROR = SQL_Utils.class.getSimpleName();

	// @formatter:off
	final static String thumbInfoInsert = "INSERT OR REPLACE INTO " + SQL_Enums.THUMBINFO.getType() + " ('id',"
			+ "'filepath', " + "'thumb_width', " + "'thumb_height', " + "'thumb_fast_width', " + "'thumb_fast_height', "
			+ "'orientation', " + "'image_0', " + "'image_1', " + "'image_2', " + "'image_3', "
			+ "'image_4') VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";

	

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

	final static String selectedFoldersInsert = "INSERT OR REPLACE INTO " + SQL_Enums.SELECTEDFOLDERS.getType()
			+ " ('path', 'connected') VALUES(?,?)";
	final static String foldersStateInsert = "INSERT OR REPLACE INTO " + SQL_Enums.FOLDERSSTATE.getType() + " ("
			+ "'path', " + "'tableType', " + "'justFolderName', " + "'connected')" + " VALUES(?,?,?,?)";

	final static String insertDriveInfo = "INSERT OR REPLACE INTO " + SQL_Enums.DRIVEINFO.getType() + "('drivePath', "
			+ "'identifier', " + "'totalSize', " + "'connected,' " + "'selected')" + " VALUES(?,?,?,?,?)";

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
	 * FoldersStates
	 */
	public static boolean createFoldersStatesDatabase(Connection connection) {
		if (connection == null) {
			Messages.sprintfError("Can't connect with configuration file: " + Main.conf.getConfiguration_db_fileName());
			return false;
		}
		if (!isDbConnected(connection)) {
			Messages.sprintf("createFolderInfoDatabase NOT connected");
			return false;
		}
		String sql = "CREATE TABLE IF NOT EXISTS " + SQL_Enums.FOLDERSSTATE.getType()
				+ " (path STRING NOT NULL PRIMARY KEY UNIQUE, " + "justFolderName STRING, "
				+ "tableType STRING NOT NULL, " + "connected BOOLEAN)";
		try {
			Statement stmt = connection.createStatement();
			stmt.execute(sql);
			stmt.close();
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}
//
//	public static boolean createFoldersStateDatabase() {
//		Connection connection = SqliteConnection.connector(Main.conf.getAppDataPath(),
//				Main.conf.getConfiguration_db_fileName());
//		if (connection == null) {
//			Messages.sprintfError("Can't connect folderInfo.db!!");
//		}
//		if (!isDbConnected(connection)) {
//			Messages.sprintf("createFolderInfoDatabase NOT connected");
//			return false;
//		}
//		try {
//			Statement stmt = connection.createStatement();
//			stmt.execute(foldersStateDatabaseSQL);
//			stmt.close();
//		} catch (Exception ex) {
//			ex.printStackTrace();
//			return false;
//		} finally {
//			try {
//				connection.close();
//			} catch (Exception e) {
//				return false;
//			}
//
//		}
//		return true;
//	}

	// @formatter:on
	public static boolean addToFolderStateDB(Connection connection, FolderState folderState) {
		if (connection == null) {
			return false;
		}
		createFoldersStatesDatabase(connection);
		try {
			PreparedStatement pstmt = connection.prepareStatement(foldersStateInsert);
			pstmt.setString(1, folderState.getPath());
			pstmt.setString(2, folderState.getTableType());
			pstmt.setString(3, folderState.getJustFolderName());
			pstmt.setBoolean(4, folderState.isConnected());
			pstmt.executeUpdate();
			pstmt.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean updateFolderInfoDB(FolderInfo folderInfo, String previousName) {
		String sql = "SELECT path, tabletype, justfoldername, connected FROM " + SQL_Enums.FILEINFO.getType()
				+ " WHERE folderpath = ?";
		try {
			Connection connection = SqliteConnection.connector(folderInfo.getFolderPath(),
					Main.conf.getMdir_db_fileName());
			connection.setAutoCommit(false);
			PreparedStatement pstmt = connection.prepareStatement(sql);
			pstmt.setString(1, folderInfo.getFolderPath());
			pstmt.setString(2, folderInfo.getTableType());
			pstmt.setString(3, folderInfo.getJustFolderName());
			pstmt.setBoolean(4, folderInfo.isConnected());
			pstmt.executeUpdate();
			pstmt.close();
			connection.close();

			return true;
		} catch (Exception e) {
			Messages.sprintfError("sql is: " + sql);
			e.printStackTrace();
			return false;
		}
	}

	public static boolean renameToFolderInfoDB(FolderInfo folderInfo, String previousName) {
//		String sql = "SELECT path,tabletype,justfoldername, connected FROM " + SQL_Enums.FOLDERSSTATE.getType()
//				+ "WHERE folderpath = " + previousName + ";";
		try {
			Connection connection = SqliteConnection.connector(Main.conf.getAppDataPath(),
					Main.conf.getConfiguration_db_fileName());
			connection.setAutoCommit(false);
			PreparedStatement pstmt = connection.prepareStatement(foldersStateInsert);
			pstmt.setString(1, folderInfo.getFolderPath());
			pstmt.setString(2, folderInfo.getTableType());
			pstmt.setString(3, folderInfo.getJustFolderName());
			pstmt.setBoolean(4, folderInfo.isConnected());
			pstmt.executeUpdate();
			pstmt.close();
			connection.close();

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static List<FolderState> loadFoldersStateTo_Tables(Connection connection, Model_main model_Main) {
		if (connection == null) {
			Messages.sprintf("Not connected NULL!");
		}
		if (isDbConnected(connection)) {
			Messages.sprintf("Connected!");
		} else {
			Messages.sprintf("Not Connected!");
			return null;
		}

		String sql = "SELECT * FROM " + SQL_Enums.FOLDERSSTATE.getType();
		try {
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			List<FolderState> arrayList = new ArrayList<>();

			while (rs.next()) {
				String path = rs.getString("path");
				String tableType = rs.getString("tableType");
				String justFolderName = rs.getString("justFolderName");
				boolean isConnected = rs.getBoolean("connected");
				if (path == null) {
					Messages.sprintf("Something went badly wrong!");
					Messages.errorSmth(ERROR, "Something went terrible wrong at: " + path, null, Misc.getLineNumber(),
							true);
					return null;
				}
				FolderState folderState = new FolderState(path, tableType, justFolderName, isConnected);
				folderState.setConnected(Files.exists(Paths.get(path)));
				Messages.sprintf("path: " + path + " folderState were connected? " + folderState.isConnected());
				arrayList.add(folderState);
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
			Messages.sprintf("createSelectedFoldersTable were not able to connect to");
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
			Messages.sprintfError("Can't find selectedfolders list.");
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
			String sql = "SELECT * FROM " + SQL_Enums.THUMBINFO.getType() + " WHERE id = ?";
//			String sql = "SELECT id, " + "filename, " + "thumb_width, " + "thumb_height, " + "thumb_fast_width, "
//					+ "thumb_fast_height, " + "orientation, " + "image_0, " + "image_1, " + "image_2, " + "image_3, "
//					+ "image_4 FROM " + SQL_Enums.THUMBINFO.getType() + " WHERE id = ?";
			// @formatter:on
			PreparedStatement pstmt = connection.prepareStatement(sql);
			pstmt.setInt(1, thumbInfo_ID);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				int id = rs.getInt("id");
				String filePath = rs.getString("filepath");
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
				Messages.sprintf("ID WERE: " + id);
				thumbInfo = new ThumbInfo(id, filePath, thumb_width, thumb_height, thumb_fast_width, thumb_fast_height,
						orientation, new ArrayList<>(Arrays.asList(image_0, image_1, image_2, image_3, image_4)));
//				thumbInfo = thumbInfoCreation(rs);
				return thumbInfo;
			}
		} catch (Exception e) {
//			if (Main.DEBUG) {
//				e.printStackTrace();
//			}

			return null;
		}
		return thumbInfo;
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

	public static FolderInfo loadFolderInfoCurrentDir(Path path) {
		Messages.sprintf("loadFoldersState started");
		FolderInfo folderInfo = FolderInfo_SQL.loadFolderInfo(path.toString());
		return folderInfo;
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
			Messages.sprintfError("Couldn't be able to clear table because table did not exists");
			return false;
		}
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

}
