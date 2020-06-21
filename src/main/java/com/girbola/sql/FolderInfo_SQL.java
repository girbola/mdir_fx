package com.girbola.sql;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.girbola.controllers.main.SQL_Enums;
import com.girbola.controllers.main.tables.FolderInfo;

public class FolderInfo_SQL {

	final String folderInfoColumns = "CREATE TABLE IF NOT EXISTS" + SQL_Enums.FOLDERINFO.getType()
			+ " (status INTEGER, " + "changed BOOLEAN, " + "connected BOOLEAN, " + "ignored BOOLEAN, "
			+ "dateDifference DOUBLE, " + "badFiles INTEGER, " + "confirmed INTEGER, " + "copied INTEGER, "
			+ "folderFiles INTEGER, " + "folderImageFiles INTEGER, " + "folderRawFiles INTEGER, "
			+ "folderVideoFiles INTEGER, " + "goodFiles INTEGER, " + "suggested INTEGER, " + "folderSize INTEGER, "
			+ "justFolderName STRING, " + "folderPath NOT NULL STRING UNIQUE, " + "maxDate STRING, "
			+ "minDate STRING, " + "state STRING, " + "tableType STRING)";

	public boolean createFolderInfoTable(Path path) {
		try {
			Connection connection = SqliteConnection.connector(path, SQL_Enums.FOLDERINFO.getType());
			Statement stmt = connection.createStatement();
			stmt.execute(folderInfoColumns);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void insertFolderInfo(FolderInfo folderInfo) {
		int status = folderInfo.getStatus();
		boolean changed = folderInfo.getChanged();
		boolean connected = folderInfo.isConnected();
		boolean ignored = folderInfo.getIgnored();
		double dateDifference = folderInfo.getDateDifferenceRatio();
		int badFiles = folderInfo.getBadFiles();
		int confirmed = folderInfo.getConfirmed();
		int copied = folderInfo.getCopied();
		int folderFiles = folderInfo.getFolderFiles();
		int folderImageFiles = folderInfo.getFolderImageFiles();
		int folderRawFiles = folderInfo.getFolderRawFiles();
		int folderVideoFiles = folderInfo.getFolderVideoFiles();
		int goodFiles = folderInfo.getGoodFiles();
		int suggested = folderInfo.getSuggested();
		long folderSize = folderInfo.getFolderSize();
		String justFolderName = folderInfo.getJustFolderName();
		String folderPath = folderInfo.getFolderPath();
		String maxDate = folderInfo.getMaxDate();
		String minDate = folderInfo.getMinDate();
		String state = folderInfo.getState();
		String tableType = folderInfo.getTableType();
//			PreparedStatement pstmt = connection.prepareStatement(foldersStateInsert);
//			pstmt.setString(1, folderInfo.getFolderPath();
//			pstmt.setString(2, folderInfo.getTableType();
//			pstmt.setString(3, folderInfo.getJustFolderName();
//			pstmt.setBoolean(4, folderInfo.isConnected();
//
//			String badFiles = rs.getString("drivePath");
//			boolean isConnected = rs.getBoolean("driveConnected");
//			boolean isSelected = rs.getBoolean("driveSelected");
//			long driveTotalSize = rs.getLong("driveTotalSize");
//			String identifier = rs.getString("identifier");		
	}

}
