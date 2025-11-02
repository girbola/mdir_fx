package com.girbola.sql;

import com.girbola.Main;
import com.girbola.controllers.main.SQLTableEnums;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.controllers.main.tables.tabletype.FolderInfoEnum;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import lombok.Getter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Arrays;
import java.util.List;

@Getter
public class FolderInfo_SQL {

    private static final String ALTER_TABLE = "ALTER TABLE ";

    private Connection connection = null;

    //@formatter:off
//	private static final String createFolderInfoSQL = "CREATE TABLE IF NOT EXISTS " + SQLTableEnums.FOLDERINFO.getType()
//			+ " (id INTEGER PRIMARY KEY AUTOINCREMENT, "
//			+ "status INTEGER, "
//			+ "changed BOOLEAN, "
//			+ "connected BOOLEAN, "
//			+ "ignored BOOLEAN, "
//			+ "dateDifference DOUBLE, "
//			+ "badFiles INTEGER, "
//			+ "confirmed INTEGER, "
//			+ "copied INTEGER, "
//			+ "folderFiles INTEGER, "
//			+ "folderImageFiles INTEGER, "
//			+ "folderRawFiles INTEGER, "
//			+ "folderVideoFiles INTEGER, "
//			+ "goodFiles INTEGER, "
//			+ "suggested INTEGER, "
//			+ "folderSize INTEGER, "
//			+ "justFolderName TEXT, "
//			+ "folderPath TEXT, "
//			+ "maxDate TEXT, "
//			+ "minDate TEXT, "
//			+ "state TEXT, "
//			+ "tableType TEXT)";

    final static String createFolderInfoSQL = "CREATE TABLE IF NOT EXISTS " + SQLTableEnums.FOLDERINFO.getType() + " ("
            + "'id' INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "'status' INTEGER, "
            + "'" + FolderInfoEnum.CHANGED.getColumnName() + "' BOOLEAN, "
            + "'" + FolderInfoEnum.CONNECTED.getColumnName() + "' BOOLEAN, "
            + "'" + FolderInfoEnum.IGNORED.getColumnName() + "' BOOLEAN, "
            + "'" + FolderInfoEnum.DATE_DIFFERENCE.getColumnName() + "' DOUBLE, "
            + "'" + FolderInfoEnum.BAD_FILES.getColumnName() + "' INTEGER, "
            + "'" + FolderInfoEnum.CONFIRMED.getColumnName() + "' INTEGER, "
            + "'" + FolderInfoEnum.COPIED.getColumnName() + "' INTEGER, "
            + "'" + FolderInfoEnum.FOLDER_FILES.getColumnName() + "' INTEGER, "
            + "'" + FolderInfoEnum.FOLDER_IMAGE_FILES.getColumnName() + "' INTEGER, "
            + "'" + FolderInfoEnum.FOLDER_RAW_FILES.getColumnName() + "' INTEGER, "
            + "'" + FolderInfoEnum.FOLDER_VIDEO_FILES.getColumnName() + "' INTEGER, "
            + "'" + FolderInfoEnum.GOOD_FILES.getColumnName() + "' INTEGER, "
            + "'" + FolderInfoEnum.SUGGESTED.getColumnName() + "' INTEGER, "
            + "'" + FolderInfoEnum.FOLDER_SIZE.getColumnName() + "' INTEGER, "
            + "'" + FolderInfoEnum.JUST_FOLDER_NAME.getColumnName() + "' TEXT, "
            + "'" + FolderInfoEnum.FOLDER_PATH.getColumnName() + "' TEXT, "
            + "'" + FolderInfoEnum.MAX_DATE.getColumnName() + "' TEXT, "
            + "'" + FolderInfoEnum.MIN_DATE.getColumnName() + "' TEXT, "
            + "'" + FolderInfoEnum.STATE.getColumnName() + "' TEXT, "
            + "'" + FolderInfoEnum.TABLE_TYPE.getColumnName() + "' TEXT)";


    private static final String insertFolderInfo = "INSERT OR REPLACE INTO "
            + SQLTableEnums.FOLDERINFO.getType()
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
        List<String> alterTableCommands = Arrays.asList(
                ALTER_TABLE + SQLTableEnums.FOLDERINFO.getType() + " ADD COLUMN id INTEGER PRIMARY KEY AUTOINCREMENT;",
                ALTER_TABLE + SQLTableEnums.FOLDERINFO.getType() + " ADD COLUMN status INTEGER;",
                ALTER_TABLE + SQLTableEnums.FOLDERINFO.getType() + " ADD COLUMN changed BOOLEAN;",
                ALTER_TABLE + SQLTableEnums.FOLDERINFO.getType() + " ADD COLUMN connected BOOLEAN;",
                ALTER_TABLE + SQLTableEnums.FOLDERINFO.getType() + " ADD COLUMN ignored BOOLEAN;",
                ALTER_TABLE + SQLTableEnums.FOLDERINFO.getType() + " ADD COLUMN dateDifference DOUBLE;",
                ALTER_TABLE + SQLTableEnums.FOLDERINFO.getType() + " ADD COLUMN badFiles INTEGER;",
                ALTER_TABLE + SQLTableEnums.FOLDERINFO.getType() + " ADD COLUMN confirmed INTEGER;",
                ALTER_TABLE + SQLTableEnums.FOLDERINFO.getType() + " ADD COLUMN copied INTEGER;",
                ALTER_TABLE + SQLTableEnums.FOLDERINFO.getType() + " ADD COLUMN folderFiles INTEGER;",
                ALTER_TABLE + SQLTableEnums.FOLDERINFO.getType() + " ADD COLUMN folderImageFiles INTEGER;",
                ALTER_TABLE + SQLTableEnums.FOLDERINFO.getType() + " ADD COLUMN folderRawFiles INTEGER;",
                ALTER_TABLE + SQLTableEnums.FOLDERINFO.getType() + " ADD COLUMN folderVideoFiles INTEGER;",
                ALTER_TABLE + SQLTableEnums.FOLDERINFO.getType() + " ADD COLUMN goodFiles INTEGER;",
                ALTER_TABLE + SQLTableEnums.FOLDERINFO.getType() + " ADD COLUMN suggested INTEGER;",
                ALTER_TABLE + SQLTableEnums.FOLDERINFO.getType() + " ADD COLUMN folderSize INTEGER;",
                ALTER_TABLE + SQLTableEnums.FOLDERINFO.getType() + " ADD COLUMN justFolderName TEXT;",
                ALTER_TABLE + SQLTableEnums.FOLDERINFO.getType() + " ADD COLUMN folderPath TEXT;",
                ALTER_TABLE + SQLTableEnums.FOLDERINFO.getType() + " ADD COLUMN maxDate TEXT;",
                ALTER_TABLE + SQLTableEnums.FOLDERINFO.getType() + " ADD COLUMN minDate TEXT;",
                ALTER_TABLE + SQLTableEnums.FOLDERINFO.getType() + " ADD COLUMN state TEXT;",
                ALTER_TABLE + SQLTableEnums.FOLDERINFO.getType() + " ADD COLUMN tableType TEXT;"
        );

        SQL_Utils.setAutoCommit(connection, false);
        try (Statement stmt = connection.createStatement()) {
            for (String sql : alterTableCommands) {
                try {
                    stmt.executeUpdate(sql);
                } catch (SQLException e) {
                    if (!e.getMessage().contains("duplicate column name")) {
                        Messages.sprintfError("Duplicated column name found! Error altering table: {}" + e.getMessage());
                        connection.rollback(); // rollback on any non-recoverable error
                        throw e;
                    }
                }
            }
            connection.commit(); // commit only if all commands succeed
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
        FolderInfo folderInfo = new FolderInfo();

        try {
            connectionFileInfos = SqliteConnection.connectToDatabase(path, Main.conf.getMdir_db_fileName());
            if (!SQL_Utils.isDbConnected(connectionFileInfos)) {
                Messages.sprintfError("Failed to establish database connection: " + path);
                return null;
            }

            SQL_Utils.setAutoCommit(connectionFileInfos, false);

            try {
                String sql = buildSelectFolderInfoQuery();
                Messages.sprintf("sql query is: " + sql);
                try (Statement stmt = connectionFileInfos.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {
                    if (!rs.next()) {
                        Messages.sprintfError("No folder information found in database");
                        return null;
                    }


                    List<FileInfo> fileInfos = FileInfo_SQL.loadFileInfoDatabase(connectionFileInfos);
                    if (fileInfos == null || fileInfos.isEmpty()) {
                        Messages.sprintfError("No file information found in database");
                        return null;
                    }

                    loadFolderInfoFromResultSet(folderInfo, rs);

                    folderInfo.setFileInfoList(fileInfos);

                    if (Main.DEBUG) {
                        for (FileInfo fileInfo : fileInfos) {
                            Messages.sprintf("Loaded file info: " + fileInfo.getOrgPath());
                        }
                    }

                    SQL_Utils.commitChanges(connectionFileInfos);
                    return folderInfo;
                }
            } catch (SQLException e) {
                SQL_Utils.rollBackConnection(connectionFileInfos);
                Messages.sprintfError("Database error while loading folder info: " + e.getMessage());
                return null;
            }
        } catch (Exception e) {
            Messages.sprintfError("Failed to process folder info: " + e.getMessage());
            return null;
        } finally {
            if (connectionFileInfos != null) {
                SQL_Utils.closeConnection(connectionFileInfos);
            }
        }
    }

    private static String buildSelectFolderInfoQuery() {
        return "SELECT id, status, changed, connected, ignored, dateDifference, " +
                "badFiles, confirmed, copied, folderFiles, folderImageFiles, " +
                "folderRawFiles, folderVideoFiles, goodFiles, suggested, " +
                "folderSize, justFolderName, folderPath, maxDate, minDate, " +
                "state, tableType FROM " + SQLTableEnums.FOLDERINFO.getType();

    }

    private static void loadFolderInfoFromResultSet(FolderInfo folderInfo, ResultSet rs) throws SQLException {
        folderInfo.setBadFiles(rs.getInt(FolderInfoEnum.BAD_FILES.getColumnName()));
        folderInfo.setChanged(rs.getBoolean(FolderInfoEnum.CHANGED.getColumnName()));
        folderInfo.setConfirmed(rs.getInt(FolderInfoEnum.CONFIRMED.getColumnName()));
        folderInfo.setConnected(rs.getBoolean(FolderInfoEnum.CONNECTED.getColumnName()));
        folderInfo.setCopied(rs.getInt(FolderInfoEnum.COPIED.getColumnName()));
        folderInfo.setDateDifferenceRatio(rs.getDouble(FolderInfoEnum.DATE_DIFFERENCE.getColumnName()));
        folderInfo.setFolderFiles(rs.getInt(FolderInfoEnum.FOLDER_FILES.getColumnName()));
        folderInfo.setFolderImageFiles(rs.getInt(FolderInfoEnum.FOLDER_IMAGE_FILES.getColumnName()));
        folderInfo.setFolderPath(rs.getString(FolderInfoEnum.FOLDER_PATH.getColumnName()));
        folderInfo.setFolderRawFiles(rs.getInt(FolderInfoEnum.FOLDER_RAW_FILES.getColumnName()));
        folderInfo.setFolderSize(rs.getLong(FolderInfoEnum.FOLDER_SIZE.getColumnName()));
        folderInfo.setFolderVideoFiles(rs.getInt(FolderInfoEnum.FOLDER_VIDEO_FILES.getColumnName()));
        folderInfo.setGoodFiles(rs.getInt(FolderInfoEnum.GOOD_FILES.getColumnName()));
        folderInfo.setIgnored(rs.getBoolean(FolderInfoEnum.IGNORED.getColumnName()));
        folderInfo.setJustFolderName(rs.getString(FolderInfoEnum.JUST_FOLDER_NAME.getColumnName()));
        folderInfo.setMaxDate(rs.getString(FolderInfoEnum.MAX_DATE.getColumnName()));
        folderInfo.setMinDate(rs.getString(FolderInfoEnum.MIN_DATE.getColumnName()));
        folderInfo.setState(rs.getString(FolderInfoEnum.STATE.getColumnName()));
        folderInfo.setSuggested(rs.getInt(FolderInfoEnum.SUGGESTED.getColumnName()));
        folderInfo.setTableType(rs.getString(FolderInfoEnum.TABLE_TYPE.getColumnName()));
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

    public static void saveConfigurationFolderInfoStateToDatabase(Connection connectionMdirFile, FolderInfo folderInfo) {
        Messages.sprintf("saveConfigurationFolderInfoStateToDatabase saving folder info to database: " + folderInfo.getFolderPath() + " connection is: " + SQL_Utils.getUrl(connectionMdirFile));

        try {
            // Ensure the table exists and has the correct structure
            ensureFolderInfoTable(connectionMdirFile);
//            aergaerg;
//            Statement stmt = connectionMdirFile.createStatement();
//            stmt.execute(createFolderInfoSQL);

            insertFolderInfo(connectionMdirFile, folderInfo);
            connectionMdirFile.commit();

        } catch (SQLException ex) {
            Messages.sprintfError("Error saving folder info to database: " + ex.getMessage());
        } finally {
            SQL_Utils.closeConnection(connectionMdirFile);
        }
    }

    public static boolean saveFolderInfo(Connection mdirDatabaseConnection, FolderInfo folderInfo) {
        try {
            if (!SQL_Utils.isDbConnected(mdirDatabaseConnection)) {
                Messages.sprintfError("Error: Database connection is not active while writing folder info! " + folderInfo.getFolderPath());
                return false;
            }

            FolderInfo_SQL.createFolderInfoTable(mdirDatabaseConnection);

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
}