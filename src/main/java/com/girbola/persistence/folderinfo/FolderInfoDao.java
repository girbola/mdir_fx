package com.girbola.persistence.folderinfo;

import com.girbola.Main;
import com.girbola.controllers.main.SQLTableEnums;
import com.girbola.controllers.main.tables.model.FolderInfo;

import com.girbola.controllers.main.tables.tabletype.FolderInfoEnum;
import com.girbola.fileinfo.FileInfo;
import com.girbola.fileinfo.FileInfoEnum;
import com.girbola.messages.Messages;
import com.girbola.persistence.fileinfo.FileInfoDao;
import com.girbola.persistence.fileinfo.FileInfoMapper;
import com.girbola.persistence.fileinfo.FileInfoSqlConnectionFactory;
import com.girbola.sql.SQL_Utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Arrays;
import java.util.List;

@Getter
public class FolderInfoDao {

    private Connection connection = null;

    final static String createFolderInfoSQL = buildCreateFolderInfoSql();


    private static final String insertFolderInfo = buildInsertFolderInfoSql();

    private static String buildCreateFolderInfoSql() {
        StringBuilder createTableSql = new StringBuilder("CREATE TABLE IF NOT EXISTS " + SQLTableEnums.FOLDERINFO.getType() + " (");
        FolderInfoEnum[] columns = FolderInfoEnum.getValuesInBindingOrder();
        for (int i = 0; i < columns.length; i++) {
            if (i > 0) {
                createTableSql.append(", ");
            }
            createTableSql.append("'")
                    .append(columns[i].getColumnName())
                    .append("' ")
                    .append(columns[i].getSqlType());
        }
        createTableSql.append(")");
        return createTableSql.toString();
    }

    private static String buildInsertFolderInfoSql() {
        StringBuilder sql = new StringBuilder("INSERT OR REPLACE INTO ")
                .append(SQLTableEnums.FOLDERINFO.getType())
                .append(" (");
        FolderInfoEnum[] columns = FolderInfoEnum.getValuesInBindingOrder();
        for (int i = 0; i < columns.length; i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append("'").append(columns[i].getColumnName()).append("'");
        }
        sql.append(") VALUES(");
        for (int i = 0; i < columns.length; i++) {
            if (i > 0) {
                sql.append(",");
            }
            sql.append("?");
        }
        sql.append(")");
        return sql.toString();
    }

    //@formatter:on
    private static boolean createFolderInfoTable(Connection mdirDatabaseConnection) {
        Messages.sprintf("createFolderInfoTable createFolderInfoSQL is: " + createFolderInfoSQL);

        if (mdirDatabaseConnection == null) {
            throw new IllegalArgumentException("Connection must not be null");
        }

        String url = SQL_Utils.getUrl(mdirDatabaseConnection);
        Messages.sprintf("------------------Creating folder info table in database: " + url);

        try {
            boolean dbConnected = SQL_Utils.isDbConnected(mdirDatabaseConnection);
            if (!dbConnected) {
                Messages.sprintfError("Error: Database connection is not active while creating folder info table!");
                return false;
            }

            // Setting Auto-Commit to false for transactional safety
            mdirDatabaseConnection.setAutoCommit(false);

            // Creating the folder info table
            try (Statement stmt = mdirDatabaseConnection.createStatement()) {
                stmt.execute(createFolderInfoSQL); // Ensure createFolderInfoSQL contains valid SQL
                mdirDatabaseConnection.commit();  // Commit the transaction
                return true;
            }
        } catch (SQLException e) {
            // Roll back the transaction in case of an exception
            SQL_Utils.rollBackConnection(mdirDatabaseConnection);
            Messages.sprintfError("SQL Exception while creating folder info table: " + e.getMessage());
            return false;
        }
    }

    private static void ensureFolderInfoTable(Connection connection) {
        try {
            SQL_Utils.ensureColumnsExist(connection, SQLTableEnums.FOLDERINFO.getType(), FolderInfoEnum.getRequiredColumns());
        } catch (SQLException e) {
            Messages.sprintfError("Overall error altering table: " + e.getMessage());
            SQL_Utils.rollBackConnection(connection);
        }
    }

    private static boolean insertFolderInfo(Connection connection, FolderInfo folderInfo) {
        Messages.sprintf("insertFolderInfo: " + folderInfo.getFolderPath() + " folderInfo INSERT:::::::::\n" + insertFolderInfo);
        try (PreparedStatement pstmt = connection.prepareStatement(insertFolderInfo)) {

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
            pstmt.setString(23, folderInfo.getSourceFolderSerialNumber());
            pstmt.addBatch();

            int[] counter = pstmt.executeBatch();
            connection.commit();
            pstmt.close();
            Messages.sprintf("COUNTER WERE: " + counter.length);
            return true;
        } catch (SQLException e) {
            Messages.sprintfError("PSTMT::: Error inserting folder info: " + e.getMessage());
            return false;
        }
    }

    public static FolderInfo loadFolderInfo(Path path) {
        return loadFolderInfo(path.toAbsolutePath().toString());
    }


    /**
     * Loads folder information from a database file located at the specified path.
     * If the database file does not exist, or if an error occurs during loading,
     * an appropriate error message is logged, and null is returned.
     *
     * @param path the directory path where the database file is located.
     *             This must be a non-null, non-empty string.
     * @return a FolderInfo object containing the folder's details and associated file information
     * if successfully loaded; otherwise, returns null.
     */
    public static FolderInfo loadFolderInfo(String path) {
        if (path == null || path.isEmpty()) {
            Messages.sprintfError("Invalid path provided");
            return null;
        }

        Path src = Paths.get(path, Main.conf.getMdir_db_fileName());
        Messages.sprintf("Loading FolderInfo from: " + src);

        if (!Files.exists(src)) {
            Messages.sprintfError("Database file not found at: " + src);
            return null;
        }

        Connection connectionFileInfos = null;
        boolean success = false;

        try {
            connectionFileInfos = FileInfoSqlConnectionFactory.connectToDatabase(path, Main.conf.getMdir_db_fileName());
            if (!SQL_Utils.isDbConnected(connectionFileInfos)) {
                Messages.sprintfError("Failed to establish database connection: " + path);
                return null;
            }
            Messages.sprintf("Connection connected to path: " + path);

            SQL_Utils.setAutoCommit(connectionFileInfos, false);
            createFolderInfoTable(connectionFileInfos);
            ensureFolderInfoTable(connectionFileInfos);
            FileInfoDao.createFileInfoTable(connectionFileInfos);

            String sql = buildSelectFolderInfoQuery();
            Messages.sprintf("sql query is: " + sql);

            try (Statement stmt = connectionFileInfos.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

                if (!rs.next()) {
                    Messages.sprintfError("No folder information found in database");
                    SQL_Utils.rollBackConnection(connectionFileInfos);
                    return null;
                }

                FolderInfo folderInfo = loadFolderInfoFromResultSet(rs);
                Messages.sprintf("Loaded folder info: " + folderInfo.toString());

                List<FileInfo> fileInfos = FileInfoDao.loadFileInfoDatabase(connectionFileInfos);
                if (fileInfos == null || fileInfos.isEmpty()) {
                    Messages.sprintfError("No file information found in database");
                    SQL_Utils.rollBackConnection(connectionFileInfos);
                    return null;
                }
                Messages.sprintf("Loaded file infos: " + fileInfos.size() + " folderInfo: " + folderInfo.getFolderPath());

                folderInfo.setFileInfoList(fileInfos);

                if (Main.DEBUG) {
                    for (FileInfo fileInfo : fileInfos) {
                        Messages.sprintf("Loaded file info: " + fileInfo.getOrgPath());
                    }
                }

                SQL_Utils.commitChanges(connectionFileInfos);
                Messages.sprintf("Successfully loaded FolderInfo and committed transaction");
                success = true;
                return folderInfo;

            } catch (SQLException e) {
                SQL_Utils.rollBackConnection(connectionFileInfos);
                Messages.sprintfError("Database error while loading folder info: " + e.getMessage());
                return null;
            }
        } catch (Exception e) {
            // Ensure we rollback the transaction here too
            if (connectionFileInfos != null) {
                SQL_Utils.rollBackConnection(connectionFileInfos);
            }
            Messages.sprintfError("Failed to process folder info: " + e.getMessage());
            return null;
        } finally {
            // Only close the connection if we've finished with it
            if (connectionFileInfos != null) {
                SQL_Utils.closeConnection(connectionFileInfos);
            }
        }
    }

    private static String buildSelectFolderInfoQuery() {
        return "SELECT " + FolderInfoEnum.getAllFolderInfoColumnNames() + " FROM " + SQLTableEnums.FOLDERINFO.getType() + ";";
    }

    private static FolderInfo loadFolderInfoFromResultSet(ResultSet rs) throws SQLException {
        FolderInfo folderInfo = new FolderInfo();

        // Initialize fileInfoList to prevent NullPointerException
        folderInfo.setFileInfoList(new ArrayList<>());

        // Get ID and STATUS columns
        // Check if the ID column exists before retrieving
        try {
            folderInfo.setStatus(rs.getInt(FolderInfoEnum.STATUS.getColumnName()));
        } catch (SQLException e) {
            // Handle or log missing STATUS column
            Messages.sprintf("Warning: Could not read STATUS column: " + e.getMessage());
        }

        // Get all the other columns with null/error handling
        try {
            folderInfo.setBadFiles(rs.getInt(FolderInfoEnum.BAD_FILES.getColumnName()));
        } catch (SQLException e) {
            Messages.sprintf("Warning: Could not read BAD_FILES column: " + e.getMessage());
        }

        try {
            folderInfo.setChanged(rs.getBoolean(FolderInfoEnum.CHANGED.getColumnName()));
        } catch (SQLException e) {
            Messages.sprintf("Warning: Could not read CHANGED column: " + e.getMessage());
        }

        try {
            folderInfo.setConfirmed(rs.getInt(FolderInfoEnum.CONFIRMED.getColumnName()));
        } catch (SQLException e) {
            Messages.sprintf("Warning: Could not read CONFIRMED column: " + e.getMessage());
        }

        try {
            folderInfo.setConnected(rs.getBoolean(FolderInfoEnum.CONNECTED.getColumnName()));
        } catch (SQLException e) {
            Messages.sprintf("Warning: Could not read CONNECTED column: " + e.getMessage());
        }

        try {
            folderInfo.setCopied(rs.getInt(FolderInfoEnum.COPIED.getColumnName()));
        } catch (SQLException e) {
            Messages.sprintf("Warning: Could not read COPIED column: " + e.getMessage());
        }

        try {
            folderInfo.setDateDifferenceRatio(rs.getDouble(FolderInfoEnum.DATE_DIFFERENCE.getColumnName()));
        } catch (SQLException e) {
            Messages.sprintf("Warning: Could not read DATE_DIFFERENCE column: " + e.getMessage());
        }

        try {
            folderInfo.setFolderFiles(rs.getInt(FolderInfoEnum.FOLDER_FILES.getColumnName()));
        } catch (SQLException e) {
            Messages.sprintf("Warning: Could not read FOLDER_FILES column: " + e.getMessage());
        }

        try {
            folderInfo.setFolderImageFiles(rs.getInt(FolderInfoEnum.FOLDER_IMAGE_FILES.getColumnName()));
        } catch (SQLException e) {
            Messages.sprintf("Warning: Could not read FOLDER_IMAGE_FILES column: " + e.getMessage());
        }

        try {
            folderInfo.setFolderPath(rs.getString(FolderInfoEnum.FOLDER_PATH.getColumnName()));
        } catch (SQLException e) {
            Messages.sprintf("Warning: Could not read FOLDER_PATH column: " + e.getMessage());
        }

        try {
            folderInfo.setFolderRawFiles(rs.getInt(FolderInfoEnum.FOLDER_RAW_FILES.getColumnName()));
        } catch (SQLException e) {
            Messages.sprintf("Warning: Could not read FOLDER_RAW_FILES column: " + e.getMessage());
        }

        try {
            folderInfo.setFolderSize(rs.getLong(FolderInfoEnum.FOLDER_SIZE.getColumnName()));
        } catch (SQLException e) {
            Messages.sprintf("Warning: Could not read FOLDER_SIZE column: " + e.getMessage());
        }

        try {
            folderInfo.setFolderVideoFiles(rs.getInt(FolderInfoEnum.FOLDER_VIDEO_FILES.getColumnName()));
        } catch (SQLException e) {
            Messages.sprintf("Warning: Could not read FOLDER_VIDEO_FILES column: " + e.getMessage());
        }

        try {
            folderInfo.setGoodFiles(rs.getInt(FolderInfoEnum.GOOD_FILES.getColumnName()));
        } catch (SQLException e) {
            Messages.sprintf("Warning: Could not read GOOD_FILES column: " + e.getMessage());
        }

        try {
            folderInfo.setIgnored(rs.getBoolean(FolderInfoEnum.IGNORED.getColumnName()));
        } catch (SQLException e) {
            Messages.sprintf("Warning: Could not read IGNORED column: " + e.getMessage());
        }

        try {
            folderInfo.setJustFolderName(rs.getString(FolderInfoEnum.JUST_FOLDER_NAME.getColumnName()));
        } catch (SQLException e) {
            Messages.sprintf("Warning: Could not read JUST_FOLDER_NAME column: " + e.getMessage());
        }

        try {
            folderInfo.setMaxDate(rs.getString(FolderInfoEnum.MAX_DATE.getColumnName()));
        } catch (SQLException e) {
            Messages.sprintf("Warning: Could not read MAX_DATE column: " + e.getMessage());
        }

        try {
            folderInfo.setMinDate(rs.getString(FolderInfoEnum.MIN_DATE.getColumnName()));
        } catch (SQLException e) {
            Messages.sprintf("Warning: Could not read MIN_DATE column: " + e.getMessage());
        }

        try {
            folderInfo.setState(rs.getString(FolderInfoEnum.STATE.getColumnName()));
        } catch (SQLException e) {
            Messages.sprintf("Warning: Could not read STATE column: " + e.getMessage());
        }

        try {
            folderInfo.setSuggested(rs.getInt(FolderInfoEnum.SUGGESTED.getColumnName()));
        } catch (SQLException e) {
            Messages.sprintf("Warning: Could not read SUGGESTED column: " + e.getMessage());
        }

        try {
            folderInfo.setTableType(rs.getString(FolderInfoEnum.TABLE_TYPE.getColumnName()));
        } catch (SQLException e) {
            Messages.sprintf("Warning: Could not read TABLE_TYPE column: " + e.getMessage());
        }

        // Add missing WORKDIR_SERIAL_NUMBER column
        try {
            String sourceFolderSerialNumber = rs.getString(FolderInfoEnum.WORKDIR_SERIAL_NUMBER.getColumnName());
            folderInfo.setSourceFolderSerialNumber(sourceFolderSerialNumber == null ? "" : sourceFolderSerialNumber);
        } catch (SQLException e) {
            Messages.sprintf("Warning: Could not read WORKDIR_SERIAL_NUMBER column: " + e.getMessage());
        }

        return folderInfo;
    }


    /**
     * Helper method to populate FolderInfo object from ResultSet
     */
    private static void populateFolderInfo(FolderInfo folderInfo, ResultSet rs, List<FileInfo> fileInfos) throws SQLException {
        folderInfo.setChanged(rs.getBoolean(FolderInfoEnum.CHANGED.getColumnName()));
        folderInfo.setConnected(rs.getBoolean(FolderInfoEnum.CONNECTED.getColumnName()));
        folderInfo.setFileInfoList(fileInfos);
        folderInfo.setIgnored(rs.getBoolean(FolderInfoEnum.IGNORED.getColumnName()));
        folderInfo.setDateDifferenceRatio(rs.getDouble(FolderInfoEnum.DATE_DIFFERENCE.getColumnName()));
        folderInfo.setBadFiles(rs.getInt(FolderInfoEnum.BAD_FILES.getColumnName()));
        folderInfo.setConfirmed(rs.getInt(FolderInfoEnum.CONFIRMED.getColumnName()));
        folderInfo.setCopied(rs.getInt(FolderInfoEnum.COPIED.getColumnName()));
        folderInfo.setFolderFiles(rs.getInt(FolderInfoEnum.FOLDER_FILES.getColumnName()));
        folderInfo.setFolderImageFiles(rs.getInt(FolderInfoEnum.FOLDER_IMAGE_FILES.getColumnName()));
        folderInfo.setFolderRawFiles(rs.getInt(FolderInfoEnum.FOLDER_RAW_FILES.getColumnName()));
        folderInfo.setFolderVideoFiles(rs.getInt(FolderInfoEnum.FOLDER_VIDEO_FILES.getColumnName()));
        folderInfo.setGoodFiles(rs.getInt(FolderInfoEnum.GOOD_FILES.getColumnName()));
        folderInfo.setSuggested(rs.getInt(FolderInfoEnum.SUGGESTED.getColumnName()));
        folderInfo.setFolderSize(rs.getLong(FolderInfoEnum.FOLDER_SIZE.getColumnName()));
        folderInfo.setJustFolderName(rs.getString(FolderInfoEnum.JUST_FOLDER_NAME.getColumnName()));
        folderInfo.setFolderPath(rs.getString(FolderInfoEnum.FOLDER_PATH.getColumnName()));
        folderInfo.setMaxDate(rs.getString(FolderInfoEnum.MAX_DATE.getColumnName()));
        folderInfo.setMinDate(rs.getString(FolderInfoEnum.MIN_DATE.getColumnName()));
        folderInfo.setState(rs.getString(FolderInfoEnum.STATE.getColumnName()));
        folderInfo.setTableType(rs.getString(FolderInfoEnum.TABLE_TYPE.getColumnName()));
    }

    public static void saveConfigurationFolderInfoStateToDatabase(Connection connectionMdirFile, FolderInfo folderInfo, boolean leaveOpen) {
        Messages.sprintf("saveConfigurationFolderInfoStateToDatabase saving folder info to database: " + folderInfo.getFolderPath() + " connection is: " + SQL_Utils.getUrl(connectionMdirFile));

        try {
            // Ensure the table exists and has the correct structure

//            aergaerg;
            Statement stmt = connectionMdirFile.createStatement();
            stmt.execute(createFolderInfoSQL);

            ensureFolderInfoTable(connectionMdirFile);
            insertFolderInfo(connectionMdirFile, folderInfo);
            connectionMdirFile.commit();

        } catch (SQLException ex) {
            Messages.sprintfError("Error saving folder info to database: " + ex.getMessage());
        } finally {
            if (!leaveOpen) {
                SQL_Utils.closeConnection(connectionMdirFile);
            }
        }
    }

    public static boolean saveFolderInfo(Connection mdirDatabaseConnection, FolderInfo folderInfo) {
        try {
            if (!SQL_Utils.isDbConnected(mdirDatabaseConnection)) {
                Messages.sprintfError("Error: Database connection is not active while writing folder info! " + folderInfo.getFolderPath());
                return false;
            }

            FolderInfoDao.createFolderInfoTable(mdirDatabaseConnection);

            ensureFolderInfoTable(mdirDatabaseConnection);

            boolean insertingFolderInfo = insertFolderInfo(mdirDatabaseConnection, folderInfo);
            if (!insertingFolderInfo) {
                Messages.sprintfError("Error inserting folder info!: " + folderInfo.getFolderPath());
                return false;
            }
            SQL_Utils.commitChanges(mdirDatabaseConnection);
            Messages.sprintf("Folder info inserted: " + folderInfo.getFolderPath());
            return true;
        } catch (Exception ex) {
            Messages.sprintfError("Error writing folder info to database: " + ex.getMessage());
            return false;
        }
//        finally {
//            SQL_Utils.closeConnection(mdirDatabaseConnection);
//        }
    }

    public static boolean insertFileInfoListToFileInfoDatabase(FolderInfo folderInfo, boolean isWorkDir) {
        final int BATCH_LIMIT = 1000;
        final String logPrefix = "insertFileInfoListToFileInfoDatabase";

        Messages.sprintf("--------------" + logPrefix + " started: " + folderInfo.getFolderPath() + " isWorkDir: " + isWorkDir);

        List<FileInfo> fileInfos = (folderInfo != null) ? folderInfo.getFileInfoList() : null;
        if (folderInfo == null || fileInfos == null || fileInfos.isEmpty()) {
            Messages.sprintfError("Invalid parameters provided to " + logPrefix);
            return false;
        }

        Connection connection = FileInfoSqlConnectionFactory.connectToDatabase(folderInfo.getFolderPath(), Main.conf.getMdir_db_fileName());
        if (connection == null) {
            Messages.sprintfError("Failed to open database connection: " + folderInfo.getFolderPath());
            return false;
        }

        try {
            if (!SQL_Utils.isDbConnected(connection)) {
                Messages.sprintfError("Database connection lost: " + logPrefix + " " + folderInfo.getFolderPath());
                return false;
            }

            SQL_Utils.setAutoCommit(connection, false);

//         Path folder;
//            try {
//                folder = Paths.get(fileInfos.get(0).getOrgPath()).getParent();
//                if (folder == null || !Files.exists(folder)) {
//                    Messages.sprintfError("Parent folder does not exist: " + folder);
//                    return false;
//                }
//            } catch (Exception e) {
//                Messages.sprintfError("Cannot get path for the folder: " + e.getMessage());
//                return false;
//            }

            if (!FileInfoDao.createFileInfoTable(connection)) {
                Messages.sprintfError("Failed to create FileInfo table");
                return false;
            }

//            FolderInfoDao.ensureFileInfoColumnsExist(connection);

            Messages.sprintf("FileInfo table created/verified");

            // IMPORTANT: perform any other writes (which may commit/rollback/DDL) BEFORE preparing the statement
            if (!FolderInfoDao.saveFolderInfo(connection, folderInfo)) {
                Messages.sprintfError("Failed to save folder info for later loading: " + folderInfo.getFolderPath() + " still continuing");
            }
            SQL_Utils.commitChanges(connection);

            if (!SQL_Utils.isDbConnected(connection)) {
                Messages.sprintfError("Database connection lost after writing folder info");
                return false;
            }

            final String columnNames = FileInfoEnum.getAllFileInfoColumnNames();
            final String placeholders = FileInfoEnum.getInsertPlaceholders();
            final String insertSql = "INSERT OR REPLACE INTO " + SQLTableEnums.FILEINFO.getType()
                    + " (" + columnNames + ") VALUES (" + placeholders + ");";

            Messages.sprintf("FileInfo columns count: " + FileInfoEnum.getBindingColumnCount() + " columns: " + columnNames);
            Messages.sprintf("FileInfo placeholders count: " + FileInfoEnum.getBindingColumnCount());
            Messages.sprintf("Insert SQL: " + insertSql);

            try (PreparedStatement pstmt = connection.prepareStatement(insertSql)) {
                int batchSize = 0;

                for (FileInfo fileInfo : fileInfos) {
                    Messages.sprintf("FIQ - Processing file: " + fileInfo.getOrgPath());

                    if (!FileInfoMapper.bindToFileInfoStatement(pstmt, fileInfo)) {
                        throw new SQLException("Failed to bind FileInfo: " + fileInfo.getOrgPath());
                    }

                    batchSize++;
                    if (batchSize >= BATCH_LIMIT) {
                        int[] results = pstmt.executeBatch();
                        Messages.sprintf("Executed FileInfo batch rows: " + results.length);
                        connection.commit();
                        batchSize = 0;
                    }
                }

                if (batchSize > 0) {
                    int[] results = pstmt.executeBatch();
                    Messages.sprintf("Executed final FileInfo batch rows: " + results.length);
                    connection.commit();
                }

                Messages.sprintf("Successfully inserted all file info records");
                return true;
            }

        } catch (SQLException ex) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                Messages.sprintfError("Failed to rollback transaction: " + rollbackEx.getMessage());
            }
            Messages.sprintfError("Failed to insert file info records: " + ex);
            ex.printStackTrace();
            return false;
        } finally {
            if (SQL_Utils.isDbConnected(connection)) {
                Messages.sprintf("insertFileInfoListToFileInfoDatabase. Closing database connection");
                SQL_Utils.closeConnection(connection);
            }
        }
    }

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

    public static void ensureFileInfoColumnsExist(Connection connection) throws SQLException {
        Map<String, String> requiredColumns = new HashMap<>();

        for (FileInfoEnum column : FileInfoEnum.getValuesInBindingOrder()) {
            requiredColumns.put(column.getColumnName(), column.getSqlType());
        }

        SQL_Utils.ensureColumnsExist(connection, SQLTableEnums.FILEINFO.getType(), requiredColumns);
    }

    /**
     *
     * @param connection
     * @return List<FileInfo>
     */
    // @formatter:off

}
