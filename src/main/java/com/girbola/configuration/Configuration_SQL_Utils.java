package com.girbola.configuration;

import com.girbola.Main;
import com.girbola.controllers.main.SQL_Enums;
import com.girbola.controllers.main.Tables;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.sql.SQL_Utils;
import com.girbola.sql.SqliteConnection;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;

public class Configuration_SQL_Utils {

    private static final String ERROR = Configuration_SQL_Utils.class.getName();
    public static final String id = "id";

    private final static String tablesColumnsInfoInsert = "INSERT OR REPLACE INTO " + SQL_Enums.TABLES_COLS.getType()
            + " ('tableColumn', " + "'width')" + " VALUES(?, ?)";

    private static int configuration_id = 0;
    public static final String betterQualityThumbs = "betterQualityThumbs";
    public static final String confirmOnExit = "confirmOnExit";
    public static final String id_counter = "id_counter";
    public static final String imageViewXPos = "imageViewXPos";
    public static final String imageViewYPos = "imageViewYPos";
    public static final String saveDataToHD = "saveDataToHD";
    public static final String showFullPath = "showFullPath";
    public static final String showHints = "showHints";
    public static final String showTooltips = "showTooltips";
    public static final String tableShow_asItIs = "tableShow_asItIs";
    public static final String tableShow_sortIt = "tableShow_sortIt";
    public static final String tableShow_sorted = "tableShow_sorted";
    public static final String themePath = "themePath";
    public static final String vlcPath = "vlcPath";
    public static final String vlcSupport = "vlcSupport";
    public static final String windowStartHeigth = "windowStartHeigth";
    public static final String windowStartPosX = "windowStartPosX";
    public static final String windowStartPosY = "windowStartPosY";
    public static final String windowStartWidth = "windowStartWidth";
    public static final String workDir = "workDir";
    public static final String workDirSerialNumber = "workDirSerialNumber";
    final private static String ignoredListTable = "CREATE TABLE IF NOT EXISTS " + SQL_Enums.IGNOREDLIST.getType()
            + " (path STRING UNIQUE)";

    private static boolean addTableColumn(Connection connection, PreparedStatement pstmt, TableColumn tc,
                                          String tableId) {
        try {
            if (tc.getId() != null) {
                pstmt.setString(1, tableId + "_" + tc.getId());
                pstmt.setDouble(2, tc.getWidth());
                pstmt.addBatch();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    /**
     * Creates the configuration table with properties in the database.
     *
     * @param connection the connection to the database
     * @return true if the configuration table is successfully created, false otherwise
     */
    public static boolean createConfigurationTable_properties(Connection connection) {
        try {
            if (!SQL_Utils.isDbConnected(connection)) {
                Messages.sprintf("createConfigurationTable connection failed");
                return false;
            }

            String sql = "CREATE TABLE IF NOT EXISTS " + SQL_Enums.TABLES_COLS.getType()
                    + " (tableColumn STRING UNIQUE, " + "	width DOUBLE)";
            Statement stmt = connection.createStatement();
            stmt.execute(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Creates the configuration table in the database with the specified properties.
     *
     * @param connection the connection to the database
     * @return true if the configuration table is successfully created, false otherwise
     */
    public static boolean createConfiguration_Table(Connection connection) {
        Messages.sprintfError("createConfiguration_Table: " + connection);

        try {
            if (!SQL_Utils.isDbConnected(connection)) {
                Messages.sprintf("createConfiguration connection failed");
                return false;
            }
            //@formatter:off
			String sql = "CREATE TABLE IF NOT EXISTS " + SQL_Enums.CONFIGURATION.getType()+ " ("
					+ id + " INTEGER PRIMARY KEY CHECK (id = 0),"
					+ betterQualityThumbs + " BOOLEAN, "
		        	+ confirmOnExit + " BOOLEAN,"
		    	    + id_counter + " INTEGER UNIQUE,"
		    	    + showFullPath + " BOOLEAN,"
		    	    + showHints + " BOOLEAN,"
		    	    + showTooltips + " BOOLEAN,"
		    	    + themePath + " STRING,"
		    	    + vlcPath + " STRING,"
		    	    + vlcSupport + " BOOLEAN,"
		    	    + saveDataToHD + " STRING,"
		    	    + windowStartPosX  + " DOUBLE DEFAULT ( -1),"
					+ windowStartPosY  + " DOUBLE DEFAULT ( -1),"
					+ windowStartWidth  + " DOUBLE DEFAULT ( -1),"
					+ windowStartHeigth + " DOUBLE DEFAULT ( -1),"
					+ imageViewXPos + " DOUBLE,"
					+ imageViewYPos + " DOUBLE,"
					+ workDirSerialNumber + " STRING, "
		    	    + workDir + " STRING, "
		    	    + tableShow_sortIt + " BOOLEAN,"
		    	    + tableShow_sorted + " BOOLEAN,"
		    	    + tableShow_asItIs + " BOOLEAN)";
			
			//@formatter:on
            Statement stmt = connection.createStatement();
            stmt.execute(sql);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Creates the ignored list table in the database.
     *
     * @param connection the connection to the database
     * @return true if the ignored list table is successfully created, false otherwise
     */
    public static boolean createIgnoredListTable(Connection connection) {
        SQL_Utils.isDbConnected(connection);
        try {
            Statement stmt = connection.createStatement();
            stmt.execute(ignoredListTable);
            return true;
        } catch (Exception ex) {
            Messages.warningText("Cannot create ignored list table");
            return false;
        }
    }

    /**
     * Loads the configuration from the database.
     *
     * @param connection    the connection to the database
     * @param configuration the Configuration object to load the values into
     * @return true if the configuration is successfully loaded, false otherwise
     */
    public static boolean loadConfiguration(Connection connection, Configuration configuration) {
        //String sql = "SELECT id,betterQualityThumbs,confirmOnExit,id_counter,showFullPath,showHints,showTooltips,themePath,vlcPath,vlcSupport,saveDataToHD,windowStartPosX,windowStartPosY,windowStartWidth,windowStartHeigth,imageViewXPos,imageViewYPos,workDirSerialNumber,workDir,tableShow_sortIt,tableShow_sorted,tableShow_asItIs FROM " + SQL_Enums.CONFIGURATION.getType();
        String sql = "SELECT id, " +
                "betterQualityThumbs, " +
                "confirmOnExit, " +
                "id_counter, " +
                "showFullPath, " +
                "showHints, " +
                "showTooltips, " +
                "themePath, " +
                "vlcPath, " +
                "vlcSupport, " +
                "saveDataToHD, " +
                "windowStartPosX, " +
                "windowStartPosY, " +
                "windowStartWidth, " +
                "windowStartHeigth, " +
                "imageViewXPos, " +
                "imageViewYPos, " +
                "workDirSerialNumber, " +
                "workDir, " +
                "tableShow_sortIt, " +
                "tableShow_sorted, " +
                "tableShow_asItIs " +
                "FROM " + SQL_Enums.CONFIGURATION.getType();
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.executeQuery();
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                configuration_id = (Integer.parseInt(rs.getString(id)));
                configuration.setBetterQualityThumbs(Boolean.parseBoolean(rs.getString(betterQualityThumbs)));
                configuration.setConfirmOnExit(Boolean.parseBoolean(rs.getString(confirmOnExit)));
                configuration.setId_counter(Integer.parseInt(rs.getString(id_counter)));
                configuration.setShowFullPath(Boolean.parseBoolean(rs.getString(showFullPath)));
                configuration.setShowHints(Boolean.parseBoolean(rs.getString(showHints)));
                configuration.setShowTooltips(Boolean.parseBoolean(rs.getString(showTooltips)));
                configuration.setThemePath(rs.getString(themePath));
                configuration.setVlcPath(rs.getString(vlcPath));
                configuration.setVlcSupport(Boolean.parseBoolean(rs.getString(vlcSupport)));
                configuration.setSaveDataToHD(Boolean.parseBoolean(rs.getString(saveDataToHD)));
                configuration.setWindowStartPosX(Double.parseDouble(rs.getString(windowStartPosX)));
                configuration.setWindowStartPosY(Double.parseDouble(rs.getString(windowStartPosY)));
                configuration.setWindowStartWidth(Double.parseDouble(rs.getString(windowStartWidth)));
                configuration.setWindowStartHeight(Double.parseDouble(rs.getString(windowStartHeigth)));

                String imageViewXPosTemp = (rs.getString(imageViewXPos));
                Messages.sprintf("imageViewXPosTemp: " + imageViewXPosTemp);

                String imageViewYPosTemp = (rs.getString(imageViewYPos));
                Messages.sprintf("imageViewYPosTemp: " + imageViewYPosTemp);
                configuration.setImageViewXProperty(Double.parseDouble(rs.getString(imageViewXPos)));
                configuration.setImageViewYProperty(Double.parseDouble(rs.getString(imageViewYPos)));

                configuration.setWorkDirSerialNumber(rs.getString(workDirSerialNumber));
                String workDirTemp = rs.getString(workDir);
                Messages.sprintfError("workDirTemp= " + workDirTemp);
                if (workDirTemp != null) {
                    configuration.setWorkDir(rs.getString(workDir));
                }
                System.err.println("1conf.workDir_property(): " + configuration.getWorkDir().hashCode());
                Messages.sprintf("Workdir loaded: " + rs.getString(workDir) + " serial number = "
                        + rs.getString(workDirSerialNumber) + " show tooltips " + configuration.isShowTooltips()
                        + " configuration.: " + configuration.getWorkDir());

                configuration.setTableShow_sortIt(Boolean.parseBoolean(rs.getString(tableShow_sortIt)));
                configuration.setTableShow_sorted(Boolean.parseBoolean(rs.getString(tableShow_sorted)));
                configuration.setTableShow_asItIs(Boolean.parseBoolean(rs.getString(tableShow_asItIs)));

                return true;
            }
            connection.commit();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        System.err.println("RETURNING FALSE 1conf.workDir_property(): " + configuration.getWorkDir());
        return false;
    }

    /**
     * Loads the ignored list from the database and adds the paths to the provided ObservableList.
     *
     * @param connection the connection to the database
     * @param obs        the ObservableList to which the paths will be added
     * @return true if the ignored list is successfully loaded, false otherwise
     */
    public static boolean loadIgnored_list(Connection connection, ObservableList<Path> obs) {
        if (connection == null) {
            Messages.sprintfError("loadIgnored_list Connection were null!");
            return false;
        }
        if (!SQL_Utils.isDbConnected(connection)) {
            Messages.sprintf("Configuration database were not connected while loading loadIgnored_list");
            return false;
        }
        try {
            String sql = "SELECT * FROM " + SQL_Enums.IGNOREDLIST.getType();
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

    /**
     * Loads table columns widths from the database for the provided TableView objects.
     *
     * @param connection The connection to the database.
     * @param tables     The Tables object containing the TableView objects.
     * @return true if the table columns widths are successfully loaded from the database, false otherwise.
     */
    public static boolean loadTables(Connection connection, Tables tables) {
        if (connection == null) {
            return false;
        }
        if (!SQL_Utils.isDbConnected(connection)) {
            Messages.sprintf("loadFileInfoDatabase Not Connected!");
            return false;
        }
//		String sql = "SELECT * FROM " + SQL_Enums.TABLES_COLS.getType();
        Messages.sprintf("Loading table column loading");
        loadTableColumnsWidths(connection, tables.getSorted_table());
        loadTableColumnsWidths(connection, tables.getSortIt_table());
        loadTableColumnsWidths(connection, tables.getAsItIs_table());

        return true;
    }

    /**
     * Loads table columns widths from the database for the provided TableView object.
     *
     * @param connection The connection to the database.
     * @param table      The TableView object to load the columns widths.
     * @return true if the table columns widths are successfully loaded from the database, false otherwise.
     */
    public static boolean loadTableColumnsWidths(Connection connection, TableView<FolderInfo> table) {
        Messages.sprintf("loadTableColumnsWidths started: " + table.getId());
//		final String tableId = table.getId();

        String sql = "SELECT tableColumn, width FROM " + SQL_Enums.TABLES_COLS.getType() + " WHERE tableColumn = ?";
        PreparedStatement pstmt;
        try {
            pstmt = connection.prepareStatement(sql);
            for (TableColumn tc : table.getColumns()) {
                Messages.sprintf("Iterating columns: " + tc.getId());
                pstmt.setString(1, table.getId() + "_" + tc.getId());
                pstmt.executeQuery();
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    String column = rs.getString("tableColumn");
                    Messages.sprintf("Loading tablecolumn: " + column + " ID: " + tc.getId());
                    double width = rs.getDouble("width");
                    tc.setPrefWidth(width);
                }
            }
            pstmt.execute();
            pstmt.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Inserts or replaces the given Configuration object into the specified database connection.
     *
     * @param connection    the connection to the database
     * @param configuration the Configuration object to insert or replace
     * @return true if the Configuration object is successfully inserted or replaced, false otherwise
     */
    public static boolean insert_Configuration(Connection connection, Configuration configuration) {
        if (configuration != null) {
            if (SQL_Utils.isDbConnected(connection)) {
                Messages.sprintf("insertAllProgram_config connection were connected");
                try {
                    //@formatter:off
                    String sql = "INSERT OR REPLACE INTO " + SQL_Enums.CONFIGURATION.getType()
                            + "('" + id + "', "
                            + "'" + betterQualityThumbs + "',"
                            + "'" + confirmOnExit + "', "
                            + "'" + id_counter + "', "
                            + "'" + showFullPath + "', "
                            + "'" + showHints + "', "
                            + "'" + showTooltips + "', "
                            + "'" + themePath + "', "
                            + "'" + vlcPath + "', "
                            + "'" + vlcSupport + "', "
                            + "'" + saveDataToHD + "', "
                            + "'" + windowStartPosX + "', "
                            + "'" + windowStartPosY + "', "
                            + "'" + windowStartWidth + "', "
                            + "'" + windowStartHeigth + "', "
                            + "'" + imageViewXPos+ "', "
                            + "'" + imageViewYPos+ "', "
                            + "'" + workDirSerialNumber + "', "
                            + "'" + workDir + "',"
                            + "'" + tableShow_sortIt + "',"
                            + "'" + tableShow_sorted + "',"
                            + "'" + tableShow_asItIs + "')"
                            + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                    //@formatter:on
                    Messages.sprintf("insert_Configuration: " + sql);
                    PreparedStatement pstmt = connection.prepareStatement(sql);
                    pstmt.setInt(1, configuration_id);
                    pstmt.setBoolean(2, configuration.isBetterQualityThumbs());
                    pstmt.setBoolean(3, configuration.isConfirmOnExit());
                    pstmt.setInt(4, configuration.getId_counter().get());
                    pstmt.setBoolean(5, configuration.isShowFullPath());
                    pstmt.setBoolean(6, configuration.isShowHints());
                    pstmt.setBoolean(7, configuration.isShowTooltips());
                    pstmt.setString(8, configuration.getThemePath());
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
                    pstmt.setBoolean(20, configuration.getTableShow_sortIt());
                    pstmt.setBoolean(21, configuration.getTableShow_sorted());
                    pstmt.setBoolean(22, configuration.getTableShow_asItIs());

                    Messages.sprintf(" configuration.getWorkDiREPLACE INTOr()" + configuration.getWorkDir());
                    pstmt.executeUpdate();

                    pstmt.close();
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
            return false;
        } else {
            Messages.errorSmth(ERROR, "Configration were not instantiated!", null, Misc.getLineNumber(), true);
            return false;
        }
    }

    /**
     * Inserts or replaces the paths of the folders in the ignored list into the database.
     *
     * @param listToRemove the ArrayList of FolderInfo objects representing the folders to insert or replace in the ignored list
     */
    public static void insert_IgnoredList(ArrayList<FolderInfo> listToRemove) {
        Connection connection = SqliteConnection.connector(Main.conf.getAppDataPath(),
                Main.conf.getConfiguration_db_fileName());
        try {
            connection.setAutoCommit(false);
            String sql = "INSERT OR REPLACE INTO " + SQL_Enums.IGNOREDLIST.getType() + " ('path') VALUES(?)";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            for (FolderInfo folderInfo : listToRemove) {
                pstmt.setString(1, folderInfo.getFolderPath());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            pstmt.close();
            connection.commit();
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
        Connection connection = null;

        if (connection_open == null) {
            connection = SqliteConnection.connector(Main.conf.getAppDataPath(),
                    Main.conf.getConfiguration_db_fileName());
        } else {
            connection = connection_open;
        }

        String sql = "DELETE FROM " + SQL_Enums.IGNOREDLIST.getType() + " WHERE path = ?";
        Messages.sprintf("removeFromIgnoredList SQL= " + sql);
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, removePath.toString());
            pstmt.executeUpdate();
            pstmt.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Saves the widths of the table columns for the specified Tables object into the database.
     *
     * @param table The Tables object containing the TableView objects.
     */
    // @formatter:on
    public static void saveTableWidths(Tables table) {
        try {
            Connection connection = SqliteConnection.connector(Main.conf.getAppDataPath(),
                    Main.conf.getConfiguration_db_fileName());
            connection.setAutoCommit(false);
            PreparedStatement pstmt = null;

            String sql = "DELETE FROM " + SQL_Enums.TABLES_COLS.getType();
            pstmt = connection.prepareStatement(sql);
            pstmt.executeUpdate();
            pstmt.close();
            pstmt = connection.prepareStatement(tablesColumnsInfoInsert);

            createConfigurationTable_properties(connection);
            saveTableWidths(connection, pstmt, table.getSortIt_table().getColumns(), table.getSortIt_table().getId());
            saveTableWidths(connection, pstmt, table.getSorted_table().getColumns(), table.getSorted_table().getId());
            saveTableWidths(connection, pstmt, table.getAsItIs_table().getColumns(), table.getAsItIs_table().getId());


            pstmt.executeBatch();
            pstmt.close();
            connection.commit();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Saves the widths of the table columns for the specified table into the database.
     *
     * @param connection The connection to the database.
     * @param pstmt      The prepared statement for executing SQL queries.
     * @param columns    The list of table columns.
     * @param tableId    The ID of the table.
     */
    // @formatter:off
	private static void saveTableWidths(Connection connection, PreparedStatement pstmt,
			ObservableList<TableColumn<FolderInfo, ?>> columns, String tableId) {
		try {
			for (TableColumn tc : columns) {
				addTableColumn(connection, pstmt, tc, tableId);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
     * Updates the configuration in the database.
     */
    public static void update_Configuration() {
		Connection connection = SqliteConnection.connector(Main.conf.getAppDataPath(),
				Main.conf.getConfiguration_db_fileName());
		try {
			connection.setAutoCommit(false);
			insert_Configuration(connection, Main.conf);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		try {
			connection.commit();
			connection.close();
		} catch (Exception e) {
			System.err.println("Can't close database file at: " + Main.conf.getAppDataPath());
			e.printStackTrace();
		}
	}

}
