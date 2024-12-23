package com.girbola.sql;

import com.girbola.Main;
import com.girbola.controllers.main.SQL_Enums;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.fileinfo.FileInfo;
import com.girbola.utils.FileInfoUtils;
import com.girbola.messages.Messages;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

public class FolderInfo_SQL {

	//@formatter:off
	private static final String folderInfoTable = "CREATE TABLE IF NOT EXISTS " + SQL_Enums.FOLDERINFO.getType()
			+ " (id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ "status INTEGER, "
			+ "changed BOOLEAN, "
			+ "connected BOOLEAN, "
			+ "ignored BOOLEAN, "
			+ "dateDifference DOUBLE, "
			+ "badFiles INTEGER, "
			+ "confirmed INTEGER, "
			+ "copied INTEGER, "
			+ "folderFiles INTEGER, "
			+ "folderImageFiles INTEGER, "
			+ "folderRawFiles INTEGER, "
			+ "folderVideoFiles INTEGER, "
			+ "goodFiles INTEGER, "
			+ "suggested INTEGER, "
			+ "folderSize INTEGER, "
			+ "justFolderName TEXT, "
			+ "folderPath TEXT, "
			+ "maxDate TEXT, "
			+ "minDate TEXT, "
			+ "state TEXT, "
			+ "tableType TEXT)";

	//@formatter:on
	private static final String folderInfoInsert = "INSERT OR REPLACE INTO "
			+ SQL_Enums.FOLDERINFO.getType()
			+ " ("
			+ "'id', "
			+ "'status', "
			+ "'changed', "
			+ "'connected', "
			+ "'ignored', "
			+ "'dateDifference', "
			+ "'badFiles', "
			+ "'confirmed', "
			+ "'copied', "
			+ "'folderFiles', "
			+ "'folderImageFiles', "
			+ "'folderRawFiles', "
			+ "'folderVideoFiles', "
			+ "'goodFiles', "
			+ "'suggested', "
			+ "'folderSize', "
			+ "'justFolderName', "
			+ "'folderPath', "
			+ "'maxDate', "
			+ "'minDate', "
			+ "'state', "
			+ "'tableType')"
			+ " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

	/**
	 *
	 * @param connection_mdirFile
	 * @return
	 */
	//@formatter:on
	private static boolean createFolderInfoTable(Connection connection_mdirFile) {
		try {
			Statement stmt = connection_mdirFile.createStatement();
			stmt.execute(folderInfoTable);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}


	private static final String ALTER_TABLE = "ALTER TABLE ";

	private static void ensureFolderInfoTable(Connection connection) {
		List<String> alterTableCommands = Arrays.asList(
				ALTER_TABLE + SQL_Enums.FOLDERINFO.getType() + " ADD COLUMN id INTEGER PRIMARY KEY AUTOINCREMENT;",
				ALTER_TABLE + SQL_Enums.FOLDERINFO.getType() + " ADD COLUMN status INTEGER;",
				ALTER_TABLE + SQL_Enums.FOLDERINFO.getType() + " ADD COLUMN changed BOOLEAN;",
				ALTER_TABLE + SQL_Enums.FOLDERINFO.getType() + " ADD COLUMN connected BOOLEAN;",
				ALTER_TABLE + SQL_Enums.FOLDERINFO.getType() + " ADD COLUMN ignored BOOLEAN;",
				ALTER_TABLE + SQL_Enums.FOLDERINFO.getType() + " ADD COLUMN dateDifference DOUBLE;",
				ALTER_TABLE + SQL_Enums.FOLDERINFO.getType() + " ADD COLUMN badFiles INTEGER;",
				ALTER_TABLE + SQL_Enums.FOLDERINFO.getType() + " ADD COLUMN confirmed INTEGER;",
				ALTER_TABLE + SQL_Enums.FOLDERINFO.getType() + " ADD COLUMN copied INTEGER;",
				ALTER_TABLE + SQL_Enums.FOLDERINFO.getType() + " ADD COLUMN folderFiles INTEGER;",
				ALTER_TABLE + SQL_Enums.FOLDERINFO.getType() + " ADD COLUMN folderImageFiles INTEGER;",
				ALTER_TABLE + SQL_Enums.FOLDERINFO.getType() + " ADD COLUMN folderRawFiles INTEGER;",
				ALTER_TABLE + SQL_Enums.FOLDERINFO.getType() + " ADD COLUMN folderVideoFiles INTEGER;",
				ALTER_TABLE + SQL_Enums.FOLDERINFO.getType() + " ADD COLUMN goodFiles INTEGER;",
				ALTER_TABLE + SQL_Enums.FOLDERINFO.getType() + " ADD COLUMN suggested INTEGER;",
				ALTER_TABLE + SQL_Enums.FOLDERINFO.getType() + " ADD COLUMN folderSize INTEGER;",
				ALTER_TABLE + SQL_Enums.FOLDERINFO.getType() + " ADD COLUMN justFolderName TEXT;",
				ALTER_TABLE + SQL_Enums.FOLDERINFO.getType() + " ADD COLUMN folderPath TEXT;",
				ALTER_TABLE + SQL_Enums.FOLDERINFO.getType() + " ADD COLUMN maxDate TEXT;",
				ALTER_TABLE + SQL_Enums.FOLDERINFO.getType() + " ADD COLUMN minDate TEXT;",
				ALTER_TABLE + SQL_Enums.FOLDERINFO.getType() + " ADD COLUMN state TEXT;",
				ALTER_TABLE + SQL_Enums.FOLDERINFO.getType() + " ADD COLUMN tableType TEXT;"
		);

		try (Statement stmt = connection.createStatement()) {
			for (String sql : alterTableCommands) {
				try {
					stmt.executeUpdate(sql);
				} catch (SQLException e) {
					if (!e.getMessage().contains("duplicate column name")) {
						// Log and rethrow the exception for any issue other than duplicate column
						System.err.println("Error altering table: " + e.getMessage());
						e.printStackTrace();
						throw e;
					}
				}
			}
		} catch (SQLException e) {
			// Use a proper logger instead of e.printStackTrace() in production.
			System.err.println("Overall error altering table: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private static boolean insertFolderInfo(Connection connection, FolderInfo folderInfo) {
		try {
			PreparedStatement pstmt = connection.prepareStatement(folderInfoInsert);
			pstmt.setInt(1, 0);
			pstmt.setInt(2, folderInfo.getStatus());
			pstmt.setBoolean(3, folderInfo.getChanged());
			pstmt.setBoolean(4, folderInfo.isConnected());
			pstmt.setBoolean(5, folderInfo.getIgnored());
			pstmt.setDouble(6, folderInfo.getDateDifferenceRatio());
			pstmt.setInt(7, folderInfo.getBadFiles());
			pstmt.setInt(8, folderInfo.getConfirmed());
			pstmt.setInt(9, folderInfo.getCopied());
			pstmt.setInt(10, folderInfo.getFolderFiles());
			pstmt.setInt(11, folderInfo.getFolderImageFiles());
			pstmt.setInt(12, folderInfo.getFolderRawFiles());
			pstmt.setInt(13, folderInfo.getFolderVideoFiles());
			pstmt.setInt(14, folderInfo.getGoodFiles());
			pstmt.setInt(15, folderInfo.getSuggested());
			pstmt.setLong(16, folderInfo.getFolderSize());
			pstmt.setString(17, folderInfo.getJustFolderName());
			pstmt.setString(18, folderInfo.getFolderPath());
			pstmt.setString(19, folderInfo.getMaxDate());
			pstmt.setString(20, folderInfo.getMinDate());
			pstmt.setString(21, folderInfo.getState());
			pstmt.setString(22, folderInfo.getTableType());
			pstmt.addBatch();

			int[] counter = pstmt.executeBatch();
			Messages.sprintf("COUNTER WERE: " + counter.length);
			pstmt.close();
//			connection.commit();
			return true;
//			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static FolderInfo loadFolderInfo(Path path) {
		return loadFolderInfo(path.toAbsolutePath().toString());
	}

	/**
	 *
	 * @param path
	 * @return
	 */
	public static FolderInfo loadFolderInfo(String path) {
		Path src = Paths.get(path + File.separator + Main.conf.getMdir_db_fileName());
		Messages.sprintf("loadFolderInfo src is: " + src);
		if (!Files.exists(src)) {
			Messages.sprintfError("Cannot load FolderInfo from: " + path);
			return null;
		}
		FolderInfo folderInfo = null;

		Connection connection = SqliteConnection.connector(path, Main.conf.getMdir_db_fileName());
		boolean dbConnected = SQL_Utils.isDbConnected(connection);
		SQL_Utils.setAutoCommit(connection, false);
		if (dbConnected) {
			try {
				String sql = "SELECT * FROM " + SQL_Enums.FOLDERINFO.getType();
				Statement smtm = connection.createStatement();
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

				List<FileInfo> fileInfo_list = FileInfo_SQL.loadFileInfoDatabase(connection);

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
				connection.close();

				return folderInfo;

			} catch (Exception e) {
				Messages.sprintfError(Main.bundle.getString("cannotLoadFolderInfoFromDatabase") + path);
				return null;
			} finally {
				SQL_Utils.closeConnection(connection);
			}
		} else {
			Messages.sprintfError(Main.bundle.getString("cannotLoadFolderInfoFromDatabase"));
		}
		return null;
	}

	public static boolean saveFolderInfoToTable(Connection connection_mdirFile, FolderInfo folderInfo) {

		ensureFolderInfoTable(connection_mdirFile);

		boolean create = createFolderInfoTable(connection_mdirFile);
		if (create) {
			return insertFolderInfo(connection_mdirFile, folderInfo);
		}
		return false;
//		try {
//			Statement stmt = connection_mdirFile.createStatement();
//			stmt.execute(folderInfoTable);
//			insertFolderInfo(connection_mdirFile, folderInfo);
//			connection_mdirFile.commit();
//			return true;
//		} catch (Exception ex) {
//			ex.printStackTrace();
//
//			return false;
//		}
	}

}