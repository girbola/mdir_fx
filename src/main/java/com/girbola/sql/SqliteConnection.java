package com.girbola.sql;

import com.girbola.messages.Messages;
import java.io.File;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lombok.Getter;

public class SqliteConnection {

	@Getter
    private static List<Connection> connectionList = new ArrayList<>();

	public static void closeAllConnections() {
		Iterator<Connection> iterator = connectionList.iterator();

		while (iterator.hasNext()) {
			Connection conn = iterator.next();
			try {
				if (conn != null) {
					if(!conn.isClosed()) {
						Messages.sprintf("Closed connection: " + conn.getMetaData().getURL());
						conn.close();
						iterator.remove();
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

    public static void addConnection(Connection conn) {
		connectionList.add(conn);
	}

	public static void removeConnection(Connection conn) {
		connectionList.remove(conn);
	}

	public static Connection connector(Path path, String tableName) {
		Messages.sprintf("Connection to path: " + path.toFile().getAbsolutePath() + " tableName: " + tableName);

		if(path.startsWith("")) {
			Messages.sprintf("Path is empty and its absolutely path is: " + path.toFile().getAbsolutePath() + ". TableName is: " + tableName);
			return null;
		}

		Connection conn = null;
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:" + path.toString() + File.separator + tableName);
			Messages.sprintf("Opening SQLite connection: " + conn.getMetaData().getURL());

			addConnection(conn);
			return conn;
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Something went wrong while connecting SQLITE database.\n" + e.getMessage());
			return null;
		}
	}

	public static Connection connector(String path, String tableName) {
		Connection conn = null;
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:" + path + File.separator + tableName);
			return conn;
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Something went wrong while connecting SQLITE database.\n" + e.getMessage());
			return null;
		}
	}

	/**
	 * Check if table is not empty
	 * 
	 * @param connection
	 * @param tableName
	 * @return
	 */
	public static boolean tableExists(Connection connection, String tableName) {
		if (connection == null) {
			return false;
		}
		try {
			DatabaseMetaData md = connection.getMetaData();
			ResultSet rs = md.getTables(null, null, tableName, null);
			rs.last();
			return rs.getRow() > 0;
		} catch (SQLException ex) {
//			ex.printStackTrace();
			return false;
		}
	}

}
