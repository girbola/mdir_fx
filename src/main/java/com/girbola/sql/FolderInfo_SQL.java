package com.girbola.sql;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.girbola.Main;
import com.girbola.controllers.main.SQL_Enums;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.messages.Messages;

public class FolderInfo_SQL {

//@formatter:off
	private static final String folderInfoTable = "CREATE TABLE IF NOT EXISTS " + SQL_Enums.FOLDERINFO.getType()
			+ " (id INTEGER PRIMARY KEY DEFAULT(0), "
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
			+ "justFolderName STRING, "
			+ "folderPath STRING, "
			+ "maxDate STRING, "
			+ "minDate STRING, "
			+ "state STRING, "
			+ "tableType STRING)";

	//@formatter:off
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

	//@formatter:on
	private static Connection createFolderInfoTable(Path path) {
		Connection connection = null;
		try {
			connection = SqliteConnection.connector(path, SQL_Enums.FOLDERINFO.getType());
			Statement stmt = connection.createStatement();
			stmt.execute(folderInfoTable);
			return connection;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static boolean close(Connection connection) {
		if (connection != null) {
			try {
				connection.close();
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		return false;
	}

	private static void insertFolderInfo(Connection connection, FolderInfo folderInfo) {
		try {
			connection.setAutoCommit(false);
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
			pstmt.executeBatch();
			connection.commit();
			pstmt.close();
//			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static FolderInfo loadFolderInfo(Path path) {
		FolderInfo folderInfo = null;
		Connection connection = SqliteConnection.connector(path, Main.conf.getFileInfo_db_fileName());

		if (connection != null) {
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

				folderInfo = new FolderInfo();
				folderInfo.setChanged(changed);
				folderInfo.setConnected(connected);
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
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}

	public static boolean saveFolderInfoToTable(FolderInfo folderInfo) {
		Connection connection = createFolderInfoTable(Paths.get(folderInfo.getFolderPath()));
		if (connection != null) {
			insertFolderInfo(connection, folderInfo);
			close(connection);
			return true;
		} else {
			Messages.sprintfError("Something went wrong with creating FolderInfo table");
			close(connection);
		}
		close(connection);
		return false;
	}

	public static boolean saveFolderInfoToTable(Connection aConnection, FolderInfo folderInfo) {
		Connection connection = createFolderInfoTable(Paths.get(folderInfo.getFolderPath()));
		try {
			Statement stmt = connection.createStatement();
			stmt.execute(folderInfoTable);
			insertFolderInfo(connection, folderInfo);
			connection.commit();
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			
			return false;
		}
	}
}
