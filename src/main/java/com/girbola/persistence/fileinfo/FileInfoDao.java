package com.girbola.persistence.fileinfo;

import com.girbola.Main;
import com.girbola.controllers.main.SQLTableEnums;
import com.girbola.fileinfo.FileInfo;
import com.girbola.fileinfo.FileInfoEnum;
import com.girbola.messages.Messages;
import com.girbola.sql.SQL_Utils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileInfoDao {

    private static final String ERROR = FileInfoDao.class.getName();

//    private static void ensureFileInfoColumnsExists_(Connection connection, Map<String, String> map) throws SQLException {
//        DatabaseMetaData meta = connection.getMetaData();
//        String fileInfoTable = SQLTableEnums.FILEINFO.getType();
//
//        // Get existing columns
//        Set<String> existingColumns = new HashSet<>();
//        try (ResultSet rs = meta.getColumns(null, null, fileInfoTable, null)) {
//            while (rs.next()) {
//                existingColumns.add(rs.getString("COLUMN_NAME").toLowerCase());
//            }
//        }
//
//        try (Statement stmt = connection.createStatement()) {
//            // Add any missing columns
//
//            for (String columnDef : FileInfoEnum.getAllFileInfoColumnNames().split(",")) {
//                String columnName = columnDef.split("\\s+")[0].toLowerCase();
//                if (!existingColumns.contains(columnName)) {
//                    try {
//                        String alterTableSQL = "ALTER TABLE " + fileInfoTable + " ADD COLUMN " + columnDef;
//                        Messages.sprintf("Adding column: " + alterTableSQL);
//                        stmt.executeUpdate(alterTableSQL);
//                    } catch (SQLException e) {
//                        // Column may already exist, ignore error
//                    }
//                }
//            }
//        }
//    }


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

    public static String createFileInfoTable() {
        return FileInfoEnum.getCreateTableSQL(SQLTableEnums.FILEINFO.getType());
    }

    /*
     * FileInfo
     */
    // @formatter:on
    public static boolean createFileInfoTable(Connection connection) {
        if (!SQL_Utils.isDbConnected(connection)) {
            Messages.sprintfError("Database connection is not active. Aborting the operation. Path is?" + SQL_Utils.getUrl(connection));
            return false;
        }

        String createTableSQL = FileInfoEnum.getCreateTableSQL(SQLTableEnums.FILEINFO.getType());
        Messages.sprintf("Createa fileinfo table: " + createTableSQL);
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
     *
     * @param connection
     * @return List<FileInfo>
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

        if (!createFileInfoTable(connection)) {
            Messages.sprintfError("Failed to create or verify file info table before loading");
            return new ArrayList<>();
        }

        try {
            Map<String, String> fileInfoColumnsMap = new HashMap<>();
            for (FileInfoEnum column : FileInfoEnum.getValuesInBindingOrder()) {
                fileInfoColumnsMap.put(column.getColumnName(), column.getSqlType());
            }
            SQL_Utils.ensureColumnsExist(connection, SQLTableEnums.FILEINFO.getType(), fileInfoColumnsMap);
            Messages.sprintf("############Ensured columns exist!");
        } catch (Exception e) {
            Messages.sprintf("Error ensuring columns exist in file info table: " + e.getMessage());
            throw new RuntimeException(e);
        }
        List<FileInfo> list = new ArrayList<>();

        //String sql = "SELECT * FROM " + SQLTableEnums.FILEINFO.getType();
        String sql = "SELECT " + FileInfoEnum.getAllFileInfoColumnNames() + " FROM " + SQLTableEnums.FILEINFO.getType();
        Messages.sprintf("############ FILEINFOOOOOO SQL: " + sql);
        try {
            SQL_Utils.setAutoCommit(connection, false);

            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    if (Main.getProcessCancelled()) {
                        Messages.sprintfError("############## Process cancelled, rolling back connection");
                        SQL_Utils.rollBackConnection(connection);
                        return new ArrayList<>();
                    }

                    FileInfo finfo = FileInfoMapper.fromFileInfoResultSet(rs);
                    Messages.sprintf("############## Loaded fileinfo: " + finfo.getOrgPath());
                    if (finfo == null) {
                        Messages.sprintfError("############## Cannot load fileinfo");
                        continue;
                    }
                    list.add(finfo);
                }
                return list;
            }
        } catch (SQLException e) {
            Messages.sprintfError("Error ensuring columns exist in file info table: " + e.getMessage());
            SQL_Utils.rollBackConnection(connection);
            return new ArrayList<>();
        } catch (Exception e) {
            Messages.sprintfError("Error loading file info database: " + e.getMessage());
            SQL_Utils.rollBackConnection(connection);
            return new ArrayList<>();
        }
    }

}
