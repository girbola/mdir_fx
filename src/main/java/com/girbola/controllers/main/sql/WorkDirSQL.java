package com.girbola.controllers.main.sql;

import com.girbola.Main;
import com.girbola.controllers.main.SQLTableEnums;
import com.girbola.controllers.operate.CopyState;
import com.girbola.fileinfo.FileInfo;
import com.girbola.fileinfo.FileInfoEnum;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.sql.FileInfo_SQL;
import com.girbola.sql.SQL_Utils;
import com.girbola.sql.SqliteConnection;
import com.girbola.utils.FileInfoUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.girbola.Main.simpleDates;
import static com.girbola.workdir.WorkDirSQL.createWorkDirTable;
import static com.girbola.workdir.WorkDirSQL.fileInfoColumnsSQL;

public class WorkDirSQL {

    private Connection connection;

    public Connection getConnection() {
        return this.connection;
    }

    public WorkDirSQL(Path workDirPath) {
        if (Files.exists(workDirPath)) {
            System.out.println("WorkDirSQL: " + workDirPath.toString() + " exists");
            this.connection = createWorkDirConnection(workDirPath);
        } else {
            System.err.println("WorkDirSQL: " + workDirPath.toString() + " does not exist");
        }
    }

    private Connection createWorkDirConnection(Path workDirPath) {
        try {
            connection = SqliteConnection.connectToDatabase(workDirPath, Main.conf.getWorkDir_db_fileName());
            if (SQL_Utils.isDbConnected(connection)) {
                createFileInfoTable(connection);
                return connection;
            }
        } catch (Exception e) {
            Messages.sprintfError("Error connecting to database: " + Main.conf.getWorkDir_db_fileName());
            return null;
        }
        return null;
    }


    public boolean createFileInfoTable(Connection connection) {
        if (!SQL_Utils.isDbConnected(connection)) {
            Messages.sprintfError("Database connection is not active. Aborting the operation. Path is?" + SQL_Utils.getUrl(connection));
            return false;
        }

        final String sql = FileInfoEnum.getCreateTableSQL(SQLTableEnums.FILEINFO.getType());
        System.out.println("SQLLLRLERLGELR::::::: " + sql);
        try {
            Statement stmt = connection.createStatement();
            stmt.execute(sql);

            // Ensure all columns exist (migration for existing databases)
            ensureAllFileInfoColumnsExist(connection);

            return true;
        } catch (Exception ex) {
            Messages.sprintfError("Cannot create table: " + sql);
            return false;
        }
    }

    private void ensureAllFileInfoColumnsExist(Connection connection) {
        String fileInfoTable = SQLTableEnums.FILEINFO.getType();

        try (Statement stmt = connection.createStatement()) {
            String[] allColumns = FileInfoEnum.getAllColumnNames().split(",");
            for (String columnDef : allColumns) {
                try {
                    String alterTableSQL = "ALTER TABLE " + fileInfoTable + " ADD COLUMN " + columnDef;
                    stmt.executeUpdate(alterTableSQL);
                } catch (SQLException e) {
                    // Column already exists, which is fine
                    if (!e.getMessage().toLowerCase().contains("duplicate column") &&
                            !e.getMessage().toLowerCase().contains("already exists")) {
                        Messages.sprintf("Column migration note: " + e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            Messages.sprintfError("Error ensuring FileInfo columns exist: " + e.getMessage());
        }
    }

    public void checkConnection() {
        try {
            // Check if the connection is valid
            if (!SQL_Utils.isDbConnected(connection)) {
                Messages.sprintf("Configuration database closed");
                connection = createWorkDirConnection(Main.conf.getAppDataPath());
                if (connection == null) {
                    Messages.sprintfError("Failed to create new configuration connection");
                    return;
                }
                SQL_Utils.setAutoCommit(connection, false);
                Messages.sprintf("Configuration database opened: " + SQL_Utils.getUrl(connection));
            }

            // Ensure AutoCommit is disabled
            if (connection.getAutoCommit()) {
                SQL_Utils.setAutoCommit(connection, false);
            }

        } catch (SQLException e) {
            Messages.sprintfError("Error checking the database connection: " + e.getMessage());
        }
    }

    public void closeConnection() {
        SQL_Utils.closeConnection(connection);
    }

    public List<FileInfo> findDuplicatesByDateRange(String startDate, String endDate) {
        List<FileInfo> duplicates = new ArrayList<>();
        String sql = "SELECT * FROM " + SQLTableEnums.WORKDIR.getType() + " WHERE date_range BETWEEN ? AND ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, startDate);
            pstmt.setString(2, endDate);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Map ResultSet to FileInfo objects and add to duplicates list
                    // Implementation depends on your FileInfo class structure
                }
            }
        } catch (SQLException e) {
            Messages.sprintfError("Error finding duplicates: " + e.getMessage());
            // Consider throwing a custom exception or handling the error appropriately
        }

        return duplicates;
    }


    public void loadWorkDir() {
        getConnection();
    }

    public void ensureFileInfoTable() {
        FileInfo_SQL.createFileInfoTable(connection);
    }


    public void insertFileInfoToWorkDir(FileInfo fileInfo) {
        Messages.sprintf("insertFileInfo starting: " + fileInfo);
        if (fileInfo == null) {
            Messages.warningText("Cannot insert null FileInfo");
            return;
        }

        try {
            // Try to get or create connection if needed
            if (SQL_Utils.isDbConnected(connection) && !connection.isClosed()) {

                boolean fileInfoTable = FileInfo_SQL.createFileInfoTable(connection);
                if (!fileInfoTable) {
                    Messages.sprintfError("Could not create fileinfo workdir table");
                    return;
                }
                // Verify connection is valid
                if (!SQL_Utils.isDbConnected(connection)) {
                    throw new SQLException("Database connection validation failed");
                }
            }

            int columnCount = 0;
            // Get the length of the column names
            for (FileInfoEnum e : FileInfoEnum.values()) {
                if (e != FileInfoEnum.ORGPATH) { // Adjust based on your needs
                    columnCount++;
                }
            }

            List<String> allValuesSplitted = Arrays.asList(FileInfoEnum.getAllColumnNames().split(","));
            StringBuilder valuePlaceholders = new StringBuilder();
            for (String placeHolder : allValuesSplitted) {
                if (valuePlaceholders.length() > 0) {
                    valuePlaceholders.append(',');
                }
                valuePlaceholders.append("?");
            }

            final String sql = "INSERT INTO " + SQLTableEnums.FILEINFO.getType()
                    + " (" + FileInfoEnum.getAllColumnNames() + ") "
                    + "VALUES (" + valuePlaceholders + ")";

            SQL_Utils.setAutoCommit(connection, false);

            System.out.println("sql:::::::: " + sql);
            System.out.println(" fileInfo: " + fileInfo.showAllValues());
            PreparedStatement pstmt = connection.prepareStatement(sql);
            int index = 1;

            // Update values
            setFileInfoParameters(pstmt, fileInfo, index);

            pstmt.executeUpdate();
            Messages.sprintf("FileInfo inserted/updated successfully");
        } catch (SQLException e) {
            String orgPath = fileInfo.getOrgPath();

            Messages.sprintfError("1111Error inserting/updating FileInfo: " + e.getMessage() + " orgPath::: " + orgPath);
        }
    }

    public void insertToWorkDir_2(FileInfo fileInfo) {
        Messages.sprintf("insertFileInfo starting: " + fileInfo);
        if (fileInfo == null) {
            Messages.warningText("Cannot insert null FileInfo");
            return;
        }

        try {
            // Try to get or create connection if needed
            if (connection == null || connection.isClosed()) {

                if (connection == null) {
                    throw new SQLException("Could not create database connection");
                }
                boolean fileInfoTable = FileInfo_SQL.createFileInfoTable(connection);
                // Create table if it doesn't exist


                // Verify connection is valid
                if (!SQL_Utils.isDbConnected(connection)) {
                    throw new SQLException("Database connection validation failed");
                }
            }
            final String sql =
                    "INSERT INTO "
                            + SQLTableEnums.WORKDIR.getType()
                            + " ("
                            + FileInfoEnum.values()
                            + ") "
                            + "VALUES ("
                            + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?"
                            + ") "
                            + "ON CONFLICT("
                            + FileInfoEnum.ORGPATH.getColumnName() + ") DO UPDATE SET "
                            + FileInfoEnum.BAD.getColumnName() + " = ?, "
                            + FileInfoEnum.CAMERA_MODEL.getColumnName() + " = ?, "
                            + FileInfoEnum.CONFIRMED.getColumnName() + " = ?, "
                            + FileInfoEnum.DESTINATION_PATH.getColumnName() + " = ?, "
                            + FileInfoEnum.EVENT.getColumnName() + " = ?, "
                            + FileInfoEnum.FILEHISTORIES.getColumnName() + " = ?, "
                            + FileInfoEnum.FILEINFO_ID.getColumnName() + " = ?, "
                            + FileInfoEnum.GOOD.getColumnName() + " = ?, "
                            + FileInfoEnum.IGNORED.getColumnName() + " = ?, "
                            + FileInfoEnum.IMAGE.getColumnName() + " = ?, "
                            + FileInfoEnum.IMAGE_DIFFERENCE_HASH.getColumnName() + " = ?, "
                            + FileInfoEnum.LOCATION.getColumnName() + " = ?, "
                            + FileInfoEnum.MODIFIED.getColumnName() + " = ?, "
                            + FileInfoEnum.ORIENTATION.getColumnName() + " = ?, "
                            + FileInfoEnum.RAW.getColumnName() + " = ?, "
                            + FileInfoEnum.SIZE.getColumnName() + " = ?, "
                            + FileInfoEnum.SUGGESTED.getColumnName() + " = ?, "
                            + FileInfoEnum.TABLE_DUPLICATED.getColumnName() + " = ?, "
                            + FileInfoEnum.TAGS.getColumnName() + " = ?, "
                            + FileInfoEnum.THUMB_LENGTH.getColumnName() + " = ?, "
                            + FileInfoEnum.THUMB_OFFSET.getColumnName() + " = ?, "
                            + FileInfoEnum.TIME_SHIFT.getColumnName() + " = ?, "
                            + FileInfoEnum.USER.getColumnName() + " = ?, "
                            + FileInfoEnum.VIDEO.getColumnName() + " = ?, "
                            + FileInfoEnum.WORK_DIR.getColumnName() + " = ?, "
                            + FileInfoEnum.WORK_DIR_DRIVE_SERIAL_NUMBER.getColumnName() + " = ?";

            PreparedStatement pstmt = connection.prepareStatement(sql);
            int index = 1;

            // Update values
            setFileInfoParameters(pstmt, fileInfo, index);

            pstmt.executeUpdate();
            Messages.sprintf("FileInfo inserted/updated successfully");
        } catch (SQLException e) {
            Messages.sprintfError("333Error inserting/updating FileInfo: " + e.getMessage());
        }
    }


    public void insertToWorkDir_(FileInfo fileInfo) {
        String sql = "INSERT INTO " + SQLTableEnums.WORKDIR.getType() + " ('path') VALUES(?)";
        boolean fileInfoTable = FileInfo_SQL.createFileInfoTable(connection);
        if (!fileInfoTable) {
            Messages.sprintfError("Could not create fileinfo workdir table");
            return;
        }
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, fileInfo.getOrgPath());
            pstmt.addBatch();
            pstmt.executeBatch();
            SQL_Utils.commitChanges(connection);
        } catch (SQLException e) {
            Messages.sprintfError("Error inserting to workdir: " + e.getMessage());
        }
//        finally {
//            SQL_Utils.closeConnection(connection);
//        }
    }

    public void updateWorkDir() {

    }

    public void deleteWorkDir() {
        String sql = "DELETE FROM " + SQLTableEnums.WORKDIR.getType();
        try (java.sql.Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
            SQL_Utils.commitChanges(connection);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SQL_Utils.closeConnection(connection);
        }
    }

    public CopyState findDuplicates(FileInfo fileInfo) {

        findDuplicatesByDateRange(simpleDates.getSdf_ymd_minus().format(fileInfo.getDate()), simpleDates.getSdf_ymd_minus().format(fileInfo.getDate()));

        return CopyState.COPY; // Default state, should be replaced with actual logic to determine the state
    }

    public List<FileInfo> findDuplicateByExactDate(FileInfo fileInfo) throws SQLException {
        if (fileInfo == null) {
            return new ArrayList<>();
        }

        // If connection failed or database doesn't exist, return empty list
        if (connection == null || !SQL_Utils.isDbConnected(connection)) {
            Messages.sprintf("Database connection failed for: " + Main.conf.getWorkDir());
            return new ArrayList<>();
        }

        // If table creation fails, return empty list
        if (!createFileInfoTable(connection)) {
            Messages.sprintf("Failed to create FileInfo table in: " + Main.conf.getWorkDir_db_fileName());
            return new ArrayList<>();
        }

        List<FileInfo> list = new ArrayList<>();

        boolean empty = isTableEmpty(connection, SQLTableEnums.WORKDIR.getType());
        if (!empty) {
            Messages.sprintf("findDuplicateByExactDate: " + fileInfo.getOrgPath() + " empty? ");

            String sql = "SELECT " + FileInfoEnum.getAllColumnNames() +
                    " FROM " + SQLTableEnums.WORKDIR.getType() +
                    " WHERE orgPath = ? AND size = ? AND localDateTime = ? AND imageDifferenceHash = ?";

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, fileInfo.getOrgPath());
                pstmt.setLong(2, fileInfo.getSize());
//            pstmt.setObject(3, fileInfo.getLocalDateTime());
                pstmt.setString(3, fileInfo.getImageDifferenceHash());

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        FileInfo duplicateFileInfo = populateFileInfoFromResultSet(rs);
                        if (duplicateFileInfo != null && FileInfoUtils.compareImagesMetadata(fileInfo, duplicateFileInfo)) {
                            list.add(duplicateFileInfo);
                        }
                    }
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                Messages.sprintfError("Error finding duplicate FileInfo: " + e.getMessage() + " line nubmer: " + Misc.getLineNumber());
                return new ArrayList<>();
            }

            return list;
        }
        Messages.sprintf("findDuplicateByExactDate: " + fileInfo.getOrgPath() + " empty? ");
        return new ArrayList<>();
    }


    public static boolean isTableEmpty(Connection conn, String tableName) {
        if (conn == null || tableName == null || tableName.trim().isEmpty()) {
            return true;
        }
        if (SQL_Utils.isDbConnected(conn)) {
            String sql = "SELECT 1 FROM " + tableName + " LIMIT 1";

            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                return !rs.next();
            } catch (SQLException e) {
                // If the table doesn't exist or connection is dead,
                // we treat it as having no data.
                return true;
            }
        }
        return true;
    }

    private static FileInfo populateFileInfoFromResultSet(ResultSet rs) throws SQLException {
        if (!rs.next()) {
            return null;
        }

        FileInfo fileInfo = new FileInfo();
        fileInfo.setBad(rs.getBoolean(FileInfoEnum.BAD.getColumnName()));
        fileInfo.setCamera_model(rs.getString(FileInfoEnum.CAMERA_MODEL.getColumnName()));
        fileInfo.setConfirmed(rs.getBoolean(FileInfoEnum.CONFIRMED.getColumnName()));
        fileInfo.setDestination_Path(rs.getString(FileInfoEnum.DESTINATION_PATH.getColumnName()));
        fileInfo.setEvent(rs.getString(FileInfoEnum.EVENT.getColumnName()));
        fileInfo.setFileInfo_id(rs.getInt(FileInfoEnum.FILEINFO_ID.getColumnName()));
        fileInfo.setGood(rs.getBoolean(FileInfoEnum.GOOD.getColumnName()));
        fileInfo.setCopied(rs.getBoolean(FileInfoEnum.COPIED.getColumnName()));
        fileInfo.setIgnored(rs.getBoolean(FileInfoEnum.IGNORED.getColumnName()));
        fileInfo.setImage(rs.getBoolean(FileInfoEnum.IMAGE.getColumnName()));
        fileInfo.setImageDifferenceHash(rs.getString(FileInfoEnum.IMAGE_DIFFERENCE_HASH.getColumnName()));
        fileInfo.setLocation(rs.getString(FileInfoEnum.LOCATION.getColumnName()));
        fileInfo.setModified(rs.getBoolean(FileInfoEnum.MODIFIED.getColumnName()));
        fileInfo.setOrgPath(rs.getString(FileInfoEnum.ORGPATH.getColumnName()));
        fileInfo.setOrientation(rs.getInt(FileInfoEnum.ORIENTATION.getColumnName()));
        fileInfo.setRaw(rs.getBoolean(FileInfoEnum.RAW.getColumnName()));
        fileInfo.setSize(rs.getLong(FileInfoEnum.SIZE.getColumnName()));
        fileInfo.setSuggested(rs.getBoolean(FileInfoEnum.SUGGESTED.getColumnName()));
        fileInfo.setTableDuplicated(rs.getBoolean(FileInfoEnum.TABLE_DUPLICATED.getColumnName()));
        fileInfo.setTags(rs.getString(FileInfoEnum.TAGS.getColumnName()));
        fileInfo.setThumb_length(rs.getInt(FileInfoEnum.THUMB_LENGTH.getColumnName()));
        fileInfo.setThumb_offset(rs.getInt(FileInfoEnum.THUMB_OFFSET.getColumnName()));
        fileInfo.setTimeShift(rs.getLong(FileInfoEnum.TIME_SHIFT.getColumnName()));
        fileInfo.setUser(rs.getString(FileInfoEnum.USER.getColumnName()));
        fileInfo.setVideo(rs.getBoolean(FileInfoEnum.VIDEO.getColumnName()));
        fileInfo.setWorkDir(rs.getString(FileInfoEnum.WORK_DIR.getColumnName()));
        fileInfo.setWorkDirDriveSerialNumber(rs.getString(FileInfoEnum.WORK_DIR_DRIVE_SERIAL_NUMBER.getColumnName()));
        String fileHistoriesStr = rs.getString(FileInfoEnum.FILEHISTORIES.getColumnName());
        if (fileHistoriesStr != null && !fileHistoriesStr.isEmpty()) {
            List<String> fileHistories = Arrays.asList(fileHistoriesStr.split("\\|"));
            fileInfo.setFileHistories(fileHistories);
        }
        return fileInfo;
    }
    //@formatter:on

    public boolean insertFileInfo(FileInfo fileInfo) {
        Messages.sprintf("insertFileInfo starting: " + fileInfo);
        if (fileInfo == null) {
            Messages.warningText("Cannot insert null FileInfo");
            return false;
        }

        try {
            if (!SQL_Utils.isDbConnected(connection)) {
                Messages.sprintf("Database connection failed for: " + Main.conf.getWorkDir());
                return false;
            }

            String sql = "CREATE TABLE " + SQLTableEnums.FILEINFO.getType() + "(" + FileInfoEnum.BAD.getColumnName() + " " + FileInfoEnum.BAD.getSqlType() + ", " + FileInfoEnum.CAMERA_MODEL.getColumnName() + " " + FileInfoEnum.CAMERA_MODEL.getSqlType() + ", " + FileInfoEnum.CONFIRMED.getColumnName() + " " + FileInfoEnum.CONFIRMED.getSqlType() + ", " + FileInfoEnum.DESTINATION_PATH.getColumnName() + " " + FileInfoEnum.DESTINATION_PATH.getSqlType() + ", " + FileInfoEnum.EVENT.getColumnName() + " " + FileInfoEnum.EVENT.getSqlType() + ", " + FileInfoEnum.FILEINFO_ID.getColumnName() + " " + FileInfoEnum.FILEINFO_ID.getSqlType() + ", " + FileInfoEnum.FILEHISTORIES.getColumnName() + " " + FileInfoEnum.FILEHISTORIES.getSqlType() + ", " + FileInfoEnum.GOOD.getColumnName() + " " + FileInfoEnum.GOOD.getSqlType() + ", " + FileInfoEnum.COPIED.getColumnName() + " " + FileInfoEnum.COPIED.getSqlType() + ", " + FileInfoEnum.IGNORED.getColumnName() + " " + FileInfoEnum.IGNORED.getSqlType() + ", " + FileInfoEnum.IMAGE.getColumnName() + " " + FileInfoEnum.IMAGE.getSqlType() + ", " + FileInfoEnum.IMAGE_DIFFERENCE_HASH.getColumnName() + " " + FileInfoEnum.IMAGE_DIFFERENCE_HASH.getSqlType() + ", " + FileInfoEnum.LOCATION.getColumnName() + " " + FileInfoEnum.LOCATION.getSqlType() + ", " + FileInfoEnum.MODIFIED.getColumnName() + " " + FileInfoEnum.MODIFIED.getSqlType() + ", " + FileInfoEnum.ORGPATH.getColumnName() + " " + FileInfoEnum.ORGPATH.getSqlType() + ", " + FileInfoEnum.ORIENTATION.getColumnName() + " " + FileInfoEnum.ORIENTATION.getSqlType() + ", " + FileInfoEnum.RAW.getColumnName() + " " + FileInfoEnum.RAW.getSqlType() + ", " + FileInfoEnum.SIZE.getColumnName() + " " + FileInfoEnum.SIZE.getSqlType() + ", " + FileInfoEnum.SUGGESTED.getColumnName() + " " + FileInfoEnum.SUGGESTED.getSqlType() + ", " + FileInfoEnum.TABLE_DUPLICATED.getColumnName() + " " + FileInfoEnum.TABLE_DUPLICATED.getSqlType() + ", " + FileInfoEnum.TAGS.getColumnName() + " " + FileInfoEnum.TAGS.getSqlType() + ", " + FileInfoEnum.THUMB_LENGTH.getColumnName() + " " + FileInfoEnum.THUMB_LENGTH.getSqlType() + ", " + FileInfoEnum.THUMB_OFFSET.getColumnName() + " " + FileInfoEnum.THUMB_OFFSET.getSqlType() + ", " + FileInfoEnum.TIME_SHIFT.getColumnName() + " " + FileInfoEnum.TIME_SHIFT.getSqlType() + ", " + FileInfoEnum.USER.getColumnName() + " " + FileInfoEnum.USER.getSqlType() + ", " + FileInfoEnum.VIDEO.getColumnName() + " " + FileInfoEnum.VIDEO.getSqlType() + ", " + FileInfoEnum.WORK_DIR.getColumnName() + " " + FileInfoEnum.WORK_DIR.getSqlType() + ", " + FileInfoEnum.WORK_DIR_DRIVE_SERIAL_NUMBER.getColumnName() + " " + FileInfoEnum.WORK_DIR_DRIVE_SERIAL_NUMBER.getSqlType() + ");";

            PreparedStatement pstmt = connection.prepareStatement(sql);

            int index = 1;

            /*
fileInfo.getDestination_Path() (for DESTINATIONPATH)
fileInfo.getFileHistories() (for FILEHISTORIES - needs to be converted to String)
             */
            // Update values
            // Update values
            setFileInfoParameters(pstmt, fileInfo, index);

            pstmt.executeUpdate();
            Messages.sprintf("FileInfo inserted/updated successfully");
            return true;
        } catch (SQLException e) {
            Messages.sprintfError("4444Error inserting/updating FileInfo: " + e.getMessage());
            return false;
        }
    }


    private int setFileInfoParameters(PreparedStatement pstmt, FileInfo fileInfo, int index) throws SQLException {
        // Must match the order in FileInfoEnum.values()
        // BAD, CAMERA_MODEL, CONFIRMED, DATE, DESTINATION_PATH, EVENT, FILEINFO_ID, FILEHISTORIES,
        // GOOD, COPIED, IGNORED, IMAGE, IMAGE_DIFFERENCE_HASH, LOCATION, MODIFIED, ORGPATH,
        // ORIENTATION, RAW, SIZE, SUGGESTED, TABLE_DUPLICATED, TAGS, THUMB_LENGTH, THUMB_OFFSET,
        // TIME_SHIFT, USER, VIDEO, WORK_DIR, WORK_DIR_DRIVE_SERIAL_NUMBER

        pstmt.setBoolean(index++, fileInfo.isBad());                        // 1 - BAD
        pstmt.setString(index++, fileInfo.getCamera_model());               // 2 - CAMERA_MODEL
        pstmt.setBoolean(index++, fileInfo.isConfirmed());                  // 3 - CONFIRMED
        pstmt.setString(index++, fileInfo.getDestination_Path());           // 4 - DESTINATION_PATH
        pstmt.setString(index++, fileInfo.getEvent());                      // 5 - EVENT
        pstmt.setInt(index++, fileInfo.getFileInfo_id());                   // 6 - FILEINFO_ID
        String historiesStr = String.join("|", fileInfo.getFileHistories());
        pstmt.setString(index++, historiesStr);                             // 7 - FILEHISTORIES
        pstmt.setBoolean(index++, fileInfo.isGood());                       // 8 - GOOD
        pstmt.setBoolean(index++, fileInfo.isCopied());                     // 9 - COPIED
        pstmt.setBoolean(index++, fileInfo.isIgnored());                    // 10 - IGNORED
        pstmt.setBoolean(index++, fileInfo.isImage());                      // 11 - IMAGE
        pstmt.setString(index++, fileInfo.getImageDifferenceHash());        // 11 - IMAGE_DIFFERENCE_HASH
        pstmt.setString(index++, fileInfo.getLocation());                   // 12 - LOCATION
        pstmt.setBoolean(index++, fileInfo.isModified());                   // 13 - MODIFIED
        pstmt.setString(index++, fileInfo.getOrgPath());                    // 14 - ORGPATH
        pstmt.setInt(index++, fileInfo.getOrientation());                   // 15 - ORIENTATION
        pstmt.setBoolean(index++, fileInfo.isRaw());                        // 16 - RAW
        pstmt.setLong(index++, fileInfo.getSize());                         // 17 - SIZE
        pstmt.setBoolean(index++, fileInfo.isSuggested());                  // 18 - SUGGESTED
        pstmt.setBoolean(index++, fileInfo.isTableDuplicated());            // 19 - TABLE_DUPLICATED
        pstmt.setString(index++, fileInfo.getTags());                       // 20 - TAGS
        pstmt.setInt(index++, fileInfo.getThumb_length());                  // 21 - THUMB_LENGTH
        pstmt.setInt(index++, fileInfo.getThumb_offset());                  // 22 - THUMB_OFFSET
        pstmt.setLong(index++, fileInfo.getTimeShift());                    // 23 - TIME_SHIFT
        pstmt.setString(index++, fileInfo.getUser());                       // 24 - USER
        pstmt.setBoolean(index++, fileInfo.isVideo());                      // 25 - VIDEO
        pstmt.setString(index++, fileInfo.getWorkDir());                    // 26 - WORK_DIR
        pstmt.setString(index++, fileInfo.getWorkDirDriveSerialNumber());   // 27 - WORK_DIR_DRIVE_SERIAL_NUMBER

        return index;
    }
}