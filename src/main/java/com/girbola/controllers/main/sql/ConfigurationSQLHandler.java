package com.girbola.controllers.main.sql;

import com.girbola.Main;
import com.girbola.configuration.Configuration;
import com.girbola.configuration.Configuration_Type;
import com.girbola.controllers.main.SQLTableEnums;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.messages.Messages;
import com.girbola.sql.DriveInfoSQL;
import com.girbola.sql.SQL_Utils;
import java.util.List;
import javafx.collections.ObservableList;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;

public class ConfigurationSQLHandler extends DriveInfoSQL {

    private static final String ERROR = ConfigurationSQLHandler.class.getName();
    public static final String ID = "id";

    private static Connection connection;

    private static int configuration_id = 0;

    public static final String BETTER_THUMBNAIL_QUALITY = "betterQualityThumbs";
    public static final String CONFIRM_ON_EXIT = "confirmOnExit";
    public static final String ID_COUNTER = "id_counter";
    public static final String IMAGE_VIEW_X_POSITION = "imageViewXPos";
    public static final String IMAGE_VIEW_Y_POSITION = "imageViewYPos";
    public static final String SAVE_DATA_AS_HD = "saveDataToHD";
    public static final String SHOW_FULL_PATH = "showFullPath";
    public static final String SHOW_HINTS = "showHints";
    public static final String SHOW_TOOLTIPS = "showTooltips";

    public static final String CURRENTTHEME = "currentTheme";
    public static final String VLC_PATH = "vlcPath";
    public static final String VLC_SUPPORT = "vlcSupport";
    public static final String WINDOW_START_HEIGTH = "windowStartHeigth";
    public static final String WINDOW_START_POSITION_X = "windowStartPosX";
    public static final String WINDOW_START_POSITION_Y = "windowStartPosY";
    public static final String WINDOW_START_WIDTH = "windowStartWidth";
    public static final String WORK_DIR = "workDir";
    public static final String WORK_DIR_SERIAL_NUMBER = "workDirSerialNumber";

    public static boolean checkConnection() {
        try {
            // Check if the connection is valid
            if (!SQL_Utils.isDbConnected(connection)) {
                connection = SQL_Utils.createConfigurationConfig();
                if (connection == null) {
                    Messages.sprintfError("Failed to create new configuration connection");
                    return false;
                }
                SQL_Utils.setAutoCommit(connection, false);
                Messages.sprintf("Configuration database opened: " + SQL_Utils.getUrl(connection));
            }

            // Ensure AutoCommit is disabled
            if (connection.getAutoCommit()) {
                SQL_Utils.setAutoCommit(connection, false);
            }

            return true;
        } catch (SQLException e) {
            Messages.sprintfError("Error checking the database connection: " + e.getMessage());
            return false;
        }
    }

    public static synchronized void updateConfiguration() {
        Connection localConnection = null;
        try {
            localConnection = getConnection();
            if (localConnection == null) {
                Messages.sprintfError("Could not establish database connection");
                return;
            }

            // Set busy timeout
            try (Statement stmt = localConnection.createStatement()) {
                stmt.execute("PRAGMA busy_timeout = 30000");
            }

            localConnection.setAutoCommit(false);

            // Ensure all columns exist
            ensureAllColumnExists(localConnection);

            // Insert configuration
            if (insertConfiguration(localConnection, Main.conf)) {
                localConnection.commit();
            } else {
                localConnection.rollback();
            }
        } catch (Exception e) {
            if (localConnection != null) {
                try {
                    localConnection.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
        } finally {
            if (localConnection != null) {
                try {
                    localConnection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean insertConfiguration(Connection conn, Configuration configuration) {
        Messages.sprintf("Inserting configuration");

        if (conn == null || configuration == null) {
            return false;
        }

        String sql = "INSERT OR REPLACE INTO " + SQLTableEnums.CONFIGURATION.getType() +
                " (" + ID + ", " +
                BETTER_THUMBNAIL_QUALITY + ", " +
                CONFIRM_ON_EXIT + ", " +
                ID_COUNTER + ", " +
                SHOW_FULL_PATH + ", " +
                SHOW_HINTS + ", " +
                SHOW_TOOLTIPS + ", " +
                CURRENTTHEME + ", " +
                VLC_PATH + ", " +
                VLC_SUPPORT + ", " +
                SAVE_DATA_AS_HD + ", " +
                WINDOW_START_POSITION_X + ", " +
                WINDOW_START_POSITION_Y + ", " +
                WINDOW_START_WIDTH + ", " +
                WINDOW_START_HEIGTH + ", " +
                IMAGE_VIEW_X_POSITION + ", " +
                IMAGE_VIEW_Y_POSITION + ", " +
                WORK_DIR_SERIAL_NUMBER + ", " +
                WORK_DIR + ") " +
                "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, configuration_id);
            pstmt.setBoolean(2, configuration.isBetterQualityThumbs());
            pstmt.setBoolean(3, configuration.isConfirmOnExit());
            pstmt.setInt(4, configuration.getId_counter().get());
            pstmt.setBoolean(5, configuration.isShowFullPath());
            pstmt.setBoolean(6, configuration.isShowHints());
            pstmt.setBoolean(7, configuration.isShowTooltips());
            pstmt.setString(8, configuration.getCurrentTheme());
            pstmt.setString(9, configuration.getVlcPath());
            pstmt.setBoolean(10, configuration.isVlcSupport());
            pstmt.setBoolean(11, configuration.isSaveDataToHD());
            pstmt.setDouble(12, configuration.getWindowStartPosX());
            pstmt.setDouble(13, configuration.getWindowStartPosY());
            pstmt.setDouble(14, configuration.getWindowStartWidth());
            pstmt.setDouble(15, configuration.getWindowStartHeight());
            pstmt.setDouble(16, configuration.getImageViewXPosition());
            pstmt.setDouble(17, configuration.getImageViewYPosition());
            pstmt.setString(18, configuration.getWorkDirSerialNumber());
            pstmt.setString(19, configuration.getWorkDir());

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean createConfigurationDatabase() {
        Connection localConnection = null;
        try {
            localConnection = getConnection();
            if (localConnection == null) {
                Messages.sprintfError("Could not establish database connection");
                return false;
            }

            // Set timeout for busy connections
            try (Statement stmt = localConnection.createStatement()) {
                stmt.execute("PRAGMA busy_timeout = 30000");
            }

            localConnection.setAutoCommit(false);

            String sql = "CREATE TABLE IF NOT EXISTS " + SQLTableEnums.CONFIGURATION.getType() + " ("
                    + ID + " INTEGER PRIMARY KEY,"
                    + BETTER_THUMBNAIL_QUALITY + " BOOLEAN, "
                    + CONFIRM_ON_EXIT + " BOOLEAN, "
                    + ID_COUNTER + " INTEGER UNIQUE, "
                    + SHOW_FULL_PATH + " BOOLEAN, "
                    + SHOW_HINTS + " BOOLEAN, "
                    + SHOW_TOOLTIPS + " BOOLEAN, "
                    + CURRENTTHEME + " TEXT, "
                    + VLC_PATH + " TEXT, "
                    + VLC_SUPPORT + " BOOLEAN, "
                    + SAVE_DATA_AS_HD + " BOOLEAN, "
                    + WINDOW_START_POSITION_X + " DOUBLE, "
                    + WINDOW_START_POSITION_Y + " DOUBLE, "
                    + WINDOW_START_WIDTH + " DOUBLE, "
                    + WINDOW_START_HEIGTH + " DOUBLE, "
                    + IMAGE_VIEW_X_POSITION + " DOUBLE, "
                    + IMAGE_VIEW_Y_POSITION + " DOUBLE, "
                    + WORK_DIR_SERIAL_NUMBER + " TEXT, "
                    + WORK_DIR + " TEXT)";

            try (Statement stmt = localConnection.createStatement()) {
                stmt.execute(sql);

                if (insertConfiguration(localConnection, Main.conf)) {
                    createIgnoredListTable(Main.conf);
                    localConnection.commit();
                    return true;
                }
                localConnection.rollback();
                return false;
            }
        } catch (Exception e) {
            if (localConnection != null) {
                try {
                    localConnection.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            Messages.sprintfError("Database error: " + e.getMessage());
            return false;
        } finally {
            if (localConnection != null) {
                try {
                    localConnection.close();
                } catch (SQLException e) {
                    Messages.sprintfError("Error closing connection: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Creates the ignored list table in the database.
     *
     * @return true if the ignored list table is successfully created, false otherwise
     */
    public static boolean createIgnoredListTable(Configuration configuration) {
        Messages.sprintf("createIgnoredListTable:::: ");
        if (!validateConnectionWithRetry()) {
            Messages.warningText("Database connection could not be established.");
            return false;
        }
        String sql = "CREATE TABLE IF NOT EXISTS " + SQLTableEnums.IGNOREDLIST.getType() + " (" +
                "path STRING UNIQUE)";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            return true;
        } catch (SQLException ex) {
            Messages.warningText("Cannot create ignored list table: " + ex.getMessage());
            return false;
        }

    }

    public static boolean validateConnectionWithRetry() {
        int maxRetries = 3;
        int delaySeconds = 5;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                // Check if the connection is null, disconnected, or closed
                if (connection == null || !SQL_Utils.isDbConnected(connection) || connection.isClosed()) {
                    connection = SQL_Utils.createConfigurationConfig();
                    SQL_Utils.setAutoCommit(connection, false);
                    Messages.sprintf("Configuration database opened: " + SQL_Utils.getUrl(connection));
                    return true; // Connection successfully re-established
                }

                // Ensure AutoCommit is disabled
                if (connection.getAutoCommit()) {
                    SQL_Utils.setAutoCommit(connection, false);
                }

                return true; // Existing connection is valid

            } catch (SQLException e) {
                Messages.sprintfError("Error checking the database connection (attempt " + attempt + "/" + maxRetries + "): " + e.getMessage());

                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(delaySeconds * 1000); // Convert seconds to milliseconds
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                }
            }
        }

        // If we get here, all attempts failed
        Messages.sprintfError("Failed to establish database connection after " + maxRetries + " attempts");
        return false;
    }

    /**
     * Updates the configuration in the database.
     */
//    public static synchronized void updateConfiguration() {
//        checkConnection();
//
//        if (!SQL_Utils.isDbAccessible(connection, Main.conf.getConfiguration_db_fileName())) {
//            createConfigurationDatabase();
//        }
//
//        try {
//            // Ensure the 'currentTheme' column exists.
//            ensureAllColumnExists(connection);
//
//            insertConfiguration(Main.conf);
//            SQL_Utils.commitChanges(connection);
//            SQL_Utils.closeConnection(connection);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        //SQL_Utils.closeConnection(connection);
//    }


    /**
     * Ensures that the 'currentTheme' column exists in the configuration table.
     *
     * @param connection the connection to the database.
     * @throws Exception if any SQL error occurs.
     */
    private static void ensureAllColumnExists(Connection connection) throws Exception {
        String configTable = SQLTableEnums.CONFIGURATION.getType();
        final String[] columnsSettings = {
                BETTER_THUMBNAIL_QUALITY + " BOOLEAN",
                CONFIRM_ON_EXIT + " BOOLEAN",
                CURRENTTHEME + " STRING",
                ID + " INTEGER PRIMARY KEY CHECK (id = 0)",
                ID_COUNTER + " INTEGER UNIQUE",
                IMAGE_VIEW_X_POSITION + " DOUBLE",
                IMAGE_VIEW_Y_POSITION + " DOUBLE",
                SAVE_DATA_AS_HD + " STRING",
                SHOW_FULL_PATH + " BOOLEAN",
                SHOW_HINTS + " BOOLEAN",
                SHOW_TOOLTIPS + " BOOLEAN",
                VLC_PATH + " STRING",
                VLC_SUPPORT + " BOOLEAN",
                WINDOW_START_HEIGTH + " DOUBLE DEFAULT (-1)",
                WINDOW_START_POSITION_X + " DOUBLE DEFAULT (-1)",
                WINDOW_START_POSITION_Y + " DOUBLE DEFAULT (-1)",
                WINDOW_START_WIDTH + " DOUBLE DEFAULT (-1)",
                WORK_DIR + " STRING",
                WORK_DIR_SERIAL_NUMBER + " STRING"
        };

        try (Statement stmt = connection.createStatement()) {
            for (String column : columnsSettings) {
                try {
                    String alterTableSQL = "ALTER TABLE " + configTable + " ADD COLUMN " + column;
                    stmt.executeUpdate(alterTableSQL);
                } catch (SQLException e) {
                    // Column already exists, ignore this error
                    if (!e.getMessage().contains("duplicate column name")) {
                        throw e;
                    }
                }
            }
        }
    }

    /**
     * Loads the configuration from the database.
     *
     * @param configuration the Configuration object to load the values into
     * @return true if the configuration is successfully loaded, false otherwise
     */
    public static boolean loadConfiguration(Configuration configuration) {
        Messages.sprintf("loadConfiguration Loading SQL config: " + Main.conf.getAppDataPath() + " - " + Main.conf.getConfiguration_db_fileName());

        checkConnection();
        if (!SQL_Utils.isDbAccessible(connection, SQLTableEnums.CONFIGURATION.getType()) || !SQL_Utils.isDbAccessible(connection, SQLTableEnums.CONFIGURATION.getType())) {
            Messages.sprintf("loadConfiguration database not accessible: " + SQLTableEnums.CONFIGURATION.getType());
            createConfigurationDatabase();
        }

        Messages.sprintf("loadConfiguration connection were connected: " + SQL_Utils.getUrl(connection));

        //String sql_ = "SELECT id, " + "betterQualityThumbs, " + "confirmOnExit, " + "id_counter, " + "showFullPath, " + "showHints, " + "showTooltips, " + "currentTheme, " + "vlcPath, " + "vlcSupport, " + "saveDataToHD, " + "windowStartPosX, " + "windowStartPosY, " + "windowStartWidth, " + "windowStartHeigth, " + "imageViewXPos, " + "imageViewYPos, " + "workDirSerialNumber, " + "workDir, " + "tableShow_sortIt, " + "tableShow_sorted, " + "tableShow_asItIs " + "FROM " + SQLTableEnums.CONFIGURATION.getType();

        String sql = "SELECT id, " +
                Configuration_Type.BETTERQUALITYTHUMBS.getType() + ", " +
                Configuration_Type.CONFIRMONEXIT.getType() + ", " +
                Configuration_Type.ID_COUNTER.getType() + ", " +
                Configuration_Type.SHOWFULLPATH.getType() + ", " +
                Configuration_Type.SHOWHINTS.getType() + ", " +
                Configuration_Type.SHOWTOOLTIPS.getType() + ", " +
                Configuration_Type.THEMEPATH.getType() + ", " +
                Configuration_Type.VLCPATH.getType() + ", " +
                Configuration_Type.VLCSUPPORT.getType() + ", " +
                Configuration_Type.SAVEDATATOHD.getType() + ", " +
                Configuration_Type.WINDOW_START_POS_X.getType() + ", " +
                Configuration_Type.WINDOW_START_POS_Y.getType() + ", " +
                Configuration_Type.WINDOW_START_WIDTH.getType() + ", " +
                Configuration_Type.WINDOW_START_HEIGTH.getType() + ", " +
                Configuration_Type.IMAGEVIEW_X_POS.getType() + ", " +
                Configuration_Type.IMAGEVIEW_Y_POS.getType() + ", " +
                Configuration_Type.WORKDIR_SERIAL_NUMBER.getType() + ", " +
                Configuration_Type.WORKDIR.getType() + ", " +
                Configuration_Type.TABLE_SHOW_SORT_IT.getType() + ", " +
                Configuration_Type.TABLE_SHOW_SORTED.getType() + ", " +
                Configuration_Type.TABLE_SHOW_ASITIS.getType() + " " +
                " FROM " + SQLTableEnums.CONFIGURATION.getType();
        Messages.sprintf("loadConfiguration: " + sql);
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.executeQuery();
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                configuration_id = (Integer.parseInt(rs.getString(ID)));
                configuration.setBetterQualityThumbs(Boolean.parseBoolean(rs.getString(BETTER_THUMBNAIL_QUALITY)));
                configuration.setConfirmOnExit(Boolean.parseBoolean(rs.getString(CONFIRM_ON_EXIT)));
                configuration.setId_counter(Integer.parseInt(rs.getString(ID_COUNTER)));
                configuration.setShowFullPath(Boolean.parseBoolean(rs.getString(SHOW_FULL_PATH)));
                configuration.setShowHints(Boolean.parseBoolean(rs.getString(SHOW_HINTS)));
                configuration.setShowTooltips(Boolean.parseBoolean(rs.getString(SHOW_TOOLTIPS)));
                configuration.setCurrentTheme(rs.getString(CURRENTTHEME)); // e.x. dark
                configuration.setThemePath("/" + configuration.getCurrentTheme() + "/");
                configuration.setVlcPath(rs.getString(VLC_PATH));
                configuration.setVlcSupport(Boolean.parseBoolean(rs.getString(VLC_SUPPORT)));
                configuration.setSaveDataToHD(Boolean.parseBoolean(rs.getString(SAVE_DATA_AS_HD)));
                configuration.setWindowStartPosX(Double.parseDouble(rs.getString(WINDOW_START_POSITION_X)));
                configuration.setWindowStartPosY(Double.parseDouble(rs.getString(WINDOW_START_POSITION_Y)));
                configuration.setWindowStartWidth(Double.parseDouble(rs.getString(WINDOW_START_WIDTH)));
                configuration.setWindowStartHeight(Double.parseDouble(rs.getString(WINDOW_START_HEIGTH)));

                String imageViewXPosTemp = (rs.getString(IMAGE_VIEW_X_POSITION));
                Messages.sprintf("imageViewXPosTemp: " + imageViewXPosTemp);

                String imageViewYPosTemp = (rs.getString(IMAGE_VIEW_Y_POSITION));
                Messages.sprintf("imageViewYPosTemp: " + imageViewYPosTemp);
                configuration.setImageViewXProperty(Double.parseDouble(rs.getString(IMAGE_VIEW_X_POSITION)));
                configuration.setImageViewYProperty(Double.parseDouble(rs.getString(IMAGE_VIEW_Y_POSITION)));

                configuration.setWorkDirSerialNumber(rs.getString(WORK_DIR_SERIAL_NUMBER));
                String workDirTemp = rs.getString(WORK_DIR);
                Messages.sprintfError("workDirTemp= " + workDirTemp);
                if (workDirTemp != null && !workDirTemp.isEmpty()) {
                    configuration.setWorkDir(rs.getString(WORK_DIR));
                }
                //System.err.println("1conf.workDir_property(): " + configuration.getWorkDir().hashCode());
                Messages.sprintf("Workdir loaded: " + rs.getString(WORK_DIR) + " serial number = " + rs.getString(WORK_DIR_SERIAL_NUMBER) + " show tooltips " + configuration.isShowTooltips() + " configuration.: " + configuration.getWorkDir());


                //return true;
            }
            SQL_Utils.commitChanges(connection);
            //SQL_Utils.closeConnection(connection);
            return true;
        } catch (Exception e) {
            System.err.println("RETURNING FALSE 1conf.workDir_property(): " + configuration.getWorkDir() + " ERROR: " + e.getMessage());
            return false;
        }
//        finally {
//            SQL_Utils.closeConnection(connection);
//            return true;
//        }
    }

    /**
     * Loads the ignored list from the database and adds the paths to the provided ObservableList.
     *
     * @param connection the connection to the database
     * @param obs        the ObservableList to which the paths will be added
     * @return true if the ignored list is successfully loaded, false otherwise
     */
    public static boolean loadIgnoredList(Connection connection, ObservableList<Path> obs) {
        checkConnection();

        try {
            String sql = "SELECT * FROM " + SQLTableEnums.IGNOREDLIST.getType();
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Messages.sprintf("loadIgnored_list starting: " + sql);
                String path = rs.getString("path");
                obs.add(Paths.get(path));
            }
            stmt.close();

            Messages.sprintf("loadIgnored_listsize of sel obs= " + obs.size());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean insertConfiguration(Configuration configuration) {
        Messages.sprintf("Inserting insert_Configuration");
        Connection localConnection = null;

        try {
            localConnection = getConnection();
            if (!SQL_Utils.isDbConnected(localConnection)) {
                return false;
            }

            //@formatter:off
            String sql = "INSERT OR REPLACE INTO " + SQLTableEnums.CONFIGURATION.getType() +
                    " (" + ID + ", " +
                    BETTER_THUMBNAIL_QUALITY + ", " +
                    CONFIRM_ON_EXIT + ", " +
                    ID_COUNTER + ", " +
                    SHOW_FULL_PATH + ", " +
                    SHOW_HINTS + ", " +
                    SHOW_TOOLTIPS + ", " +
                    CURRENTTHEME + ", " +
                    VLC_PATH + ", " +
                    VLC_SUPPORT + ", " +
                    SAVE_DATA_AS_HD + ", " +
                    WINDOW_START_POSITION_X + ", " +
                    WINDOW_START_POSITION_Y + ", " +
                    WINDOW_START_WIDTH + ", " +
                    WINDOW_START_HEIGTH + ", " +
                    IMAGE_VIEW_X_POSITION + ", " +
                    IMAGE_VIEW_Y_POSITION + ", " +
                    WORK_DIR_SERIAL_NUMBER + ", " +
                    WORK_DIR + ") " +
                    "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            //@formatter:on

            try (PreparedStatement pstmt = localConnection.prepareStatement(sql)) {
                pstmt.setInt(1, configuration_id);
                pstmt.setBoolean(2, configuration.isBetterQualityThumbs());
                pstmt.setBoolean(3, configuration.isConfirmOnExit());
                pstmt.setInt(4, configuration.getId_counter().get());
                pstmt.setBoolean(5, configuration.isShowFullPath());
                pstmt.setBoolean(6, configuration.isShowHints());
                pstmt.setBoolean(7, configuration.isShowTooltips());
                pstmt.setString(8, configuration.getCurrentTheme());
                pstmt.setString(9, configuration.getVlcPath());
                pstmt.setBoolean(10, configuration.isVlcSupport());
                pstmt.setBoolean(11, configuration.isSaveDataToHD());
                pstmt.setDouble(12, configuration.getWindowStartPosX());
                pstmt.setDouble(13, configuration.getWindowStartPosY());
                pstmt.setDouble(14, configuration.getWindowStartWidth());
                pstmt.setDouble(15, configuration.getWindowStartHeight());
                pstmt.setDouble(16, configuration.getImageViewXPosition());
                pstmt.setDouble(17, configuration.getImageViewYPosition());
                pstmt.setString(18, configuration.getWorkDirSerialNumber());
                pstmt.setString(19, configuration.getWorkDir());

                int result = pstmt.executeUpdate();

                if (result > 0) {
                    localConnection.commit();
                    return true;
                }
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (localConnection != null) {
                try {
                    localConnection.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        }
    }

    /**
     * Inserts or replaces the paths of the folders in the ignored list into the database.
     *
     * @param listToRemove the ArrayList of FolderInfo objects representing the folders to insert or replace in the ignored list
     */
    public static void insertIgnoredList(List<FolderInfo> listToRemove) {

        checkConnection();

        try {
            String sql = "INSERT OR REPLACE INTO " + SQLTableEnums.IGNOREDLIST.getType() + " ('path') VALUES(?)";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            for (FolderInfo folderInfo : listToRemove) {
                pstmt.setString(1, folderInfo.getFolderPath());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            pstmt.close();
            SQL_Utils.commitChanges(connection);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @param connection_open The open Connection object to the database. If null, a new connection will be created.
     * @param removePath      The Path object representing the path to be removed from the ignored list.
     * @return true if the path is successfully removed from the ignored list, false otherwise.
     */
    public static boolean removeFromIgnoredList(Connection connection_open, Path removePath) {
        checkConnection();

        String sql = "DELETE FROM " + SQLTableEnums.IGNOREDLIST.getType() + " WHERE path = ?";
        Messages.sprintf("removeFromIgnoredList SQL= " + sql);
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, removePath.toString());
            pstmt.executeUpdate();
            pstmt.close();

            SQL_Utils.commitChanges(connection);
            //SQL_Utils.closeConnection(connection);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //@formatter:on
    public static Connection getConnection() {
        if (checkConnection()) {
            return connection;
        }
        createConfigurationDatabase();
        return connection;
    }

    public static void close() {
        SQL_Utils.closeConnection(connection);
    }
}