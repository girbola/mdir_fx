package com.girbola.sql;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SqliteConnection {

	public static Connection connector(Path path, String tableName) {
		Connection conn = null;
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:" + path.toString() + File.separator + tableName);
			return conn;
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Something went wrong while connecting SQLITE database.\n" + e.getMessage());
			return null;
		}
	}

	/**
	 * Check if table is not empty
	 * @param connection
	 * @param tableName
	 * @return
	 */
	public static boolean tableExists(Connection connection, String tableName) {
		if(connection == null) {
			return false;
		}
		try {
			DatabaseMetaData md = connection.getMetaData();
			ResultSet rs = md.getTables(null, null, tableName, null);
			rs.last();
			return rs.getRow() > 0;
		} catch (SQLException ex) {
			ex.printStackTrace();
			return false;
		}
	}

}
