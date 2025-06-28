package com.girbola.sql;

import com.girbola.Main;
import com.girbola.controllers.main.SQLTableEnums;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.controllers.main.tables.tabletype.FolderInfoEnum;
import com.girbola.fileinfo.FileInfo;
import com.girbola.utils.FileInfoUtils;
import com.girbola.messages.Messages;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import lombok.Getter;

@Getter
public class FolderInfo_SQL {

    private static final String ALTER_TABLE = "ALTER TABLE ";

    private Connection connection = null;

    //@formatter:off
	private static final String folderInfoTable = "CREATE TABLE IF NOT EXISTS " + SQLTableEnums.FOLDERINFO.getType()
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



    /**
     * @param connectionMdirFileStatement
     * @return
     */
    //@formatter:on
    private static boolean createFolderInfoTable(Connection connectionMdirFileStatement) {
        try {
            Statement stmt = connectionMdirFileStatement.createStatement();
            stmt.execute(folderInfoTable);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
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
                        Messages.sprintfError("Error altering table: {}" + e.getMessage());
                        connection.rollback(); // rollback on any non-recoverable error
                        throw e;
                    }
                }
            }
            connection.commit(); // commit only if all commands succeed
        } catch (SQLException e) {
            Messages.sprintfError("Overall error altering table: " + e.getMessage());
            SQL_Utils.rollBackConnection(connection);

        } finally {
            SQL_Utils.setAutoCommit(connection, true);
        }

//        try (Statement stmt = connection.createStatement()) {
//            for (String sql : alterTableCommands) {
//                try {
//                    stmt.executeUpdate(sql);
//                } catch (SQLException e) {
//                    if (!e.getMessage().contains("duplicate column name")) {
//                        // Log and rethrow the exception for any issue other than duplicate column
//                        Messages.sprintfError("Error altering table: " + e.getMessage());
//                        throw e;
//                    } else {
//                        Messages.sprintfError("Error altering table for duplicated column name: " + e.getMessage());
//                    }
//                }
//            }
//        } catch (SQLException e) {
//            // Use a proper logger instead of e.printStackTrace() in production.
//            Messages.sprintfError("Overall error altering table: " + e.getMessage());
//        }
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
     * @param path
     * @return
     */
    public static FolderInfo loadFolderInfo(String path) {
        Path src = Paths.get(path + File.separator + Main.conf.getMdir_db_fileName());
        Messages.sprintf("loadFolderInfo src is: " + src);
        if (!Files.exists(src)) {
            Messages.sprintfError("1Cannot load FolderInfo from: " + path);
            return null;
        }
        FolderInfo folderInfo = null;

        Connection connectionFileInfos = SqliteConnection.connector(path, Main.conf.getMdir_db_fileName());
        boolean dbConnected = SQL_Utils.isDbConnected(connectionFileInfos);
        SQL_Utils.setAutoCommit(connectionFileInfos, false);
        if (dbConnected) {
            try {
                String sql = "SELECT korjaa tämä hirvitys * FROM " + SQLTableEnums.FILEINFO.getType();
                Statement smtm = connectionFileInfos.createStatement();
                ResultSet rs = smtm.executeQuery(sql);

                boolean changed = rs.getBoolean(FolderInfoEnum.CHANGED.getColumnName());
                boolean connected = rs.getBoolean(FolderInfoEnum.CONNECTED.getColumnName());
                boolean ignored = rs.getBoolean(FolderInfoEnum.IGNORED.getColumnName());
                double dateDifference = rs.getDouble(FolderInfoEnum.DATE_DIFFERENCE.getColumnName());
                int badFiles = rs.getInt(FolderInfoEnum.BAD_FILES.getColumnName());
                int confirmed = rs.getInt(FolderInfoEnum.CONFIRMED.getColumnName());
                int copied = rs.getInt(FolderInfoEnum.COPIED.getColumnName());
                int folderFiles = rs.getInt(FolderInfoEnum.FOLDER_FILES.getColumnName());
                int folderImageFiles = rs.getInt(FolderInfoEnum.FOLDER_IMAGE_FILES.getColumnName());
                int folderRawFiles = rs.getInt(FolderInfoEnum.FOLDER_RAW_FILES.getColumnName());
                int folderVideoFiles = rs.getInt(FolderInfoEnum.FOLDER_VIDEO_FILES.getColumnName());
                int goodFiles = rs.getInt(FolderInfoEnum.GOOD_FILES.getColumnName());
                int suggested = rs.getInt(FolderInfoEnum.SUGGESTED.getColumnName());
                long folderSize = rs.getLong(FolderInfoEnum.FOLDER_SIZE.getColumnName());
                String justFolderName = rs.getString(FolderInfoEnum.JUST_FOLDER_NAME.getColumnName());
                String folderPath = rs.getString(FolderInfoEnum.FOLDER_PATH.getColumnName());
                String maxDate = rs.getString(FolderInfoEnum.MAX_DATE.getColumnName());
                String minDate = rs.getString(FolderInfoEnum.MIN_DATE.getColumnName());
                String state = rs.getString(FolderInfoEnum.STATE.getColumnName());
                String tableType = rs.getString(FolderInfoEnum.TABLE_TYPE.getColumnName());

                List<FileInfo> fileInfos = FileInfo_SQL.loadFileInfoDatabase(connectionFileInfos);

                if (fileInfos == null || fileInfos.isEmpty()) {
                    Messages.sprintfError("Could not load fileInfos!");
                    return null;
                }

                folderInfo = new FolderInfo();
                if (fileInfos.isEmpty()) {
                    Messages.sprintf("FileInfo were empty!");
                    fileInfos = FileInfoUtils.createFileInfo_list(folderInfo);
                    if (fileInfos == null || fileInfos.isEmpty()) {
                        Messages.sprintf("Folder were empty.");
                        return null;
                    }
                }

                folderInfo.setChanged(changed);
                folderInfo.setConnected(connected);
                folderInfo.setFileInfoList(fileInfos);
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
                connectionFileInfos.close();

                return folderInfo;

            } catch (Exception e) {
                Messages.sprintfError("2Cannot load FolderInfo from: " + path);
                Messages.sprintfError(Main.bundle.getString("cannotLoadFolderInfoFromDatabase") + path);
                return null;
            } finally {
                SQL_Utils.closeConnection(connectionFileInfos);
            }
        } else {
            Messages.sprintfError(Main.bundle.getString("cannotLoadFolderInfoFromDatabase"));
        }
        return null;
    }

    public static void saveFolderInfoToDatabase(Connection connectionMdirFile, FolderInfo folderInfo) {

        try {

                boolean create = createFolderInfoTable(connectionMdirFile);
                if (create) {
                    insertFolderInfo(connectionMdirFile, folderInfo);
                    return;
                }
                ensureFolderInfoTable(connectionMdirFile);

                Statement stmt = connectionMdirFile.createStatement();
                stmt.execute(folderInfoTable);
                insertFolderInfo(connectionMdirFile, folderInfo);
                connectionMdirFile.commit();


        } catch (Exception ex) {
            ex.printStackTrace();

        } finally {
            SQL_Utils.closeConnection(connectionMdirFile);
        }
    }

}