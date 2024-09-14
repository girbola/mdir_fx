package com.girbola.configuration;

import com.girbola.Main;
import com.girbola.controllers.main.Model_main;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.sql.SQL_Utils;
import com.girbola.sql.SqliteConnection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;

import static com.girbola.Main.conf;

public class ConfigurationUtils {

    final private static String ERROR = ConfigurationUtils.class.getSimpleName();

    public static void loadConfig() {
        Messages.sprintf("Loading configuration...");
        if (!Files.exists(Paths.get(Main.conf.getAppDataPath() + File.separator + Main.conf.getConfiguration_db_fileName()))) {
            boolean sqlDatabaseCreated = createConfiguration_db();
            if (!sqlDatabaseCreated) {
                Messages.errorSmth(ERROR, "Could not be able to create configuration file", null, Misc.getLineNumber(),
                        true);
                Messages.sprintf("Could not be able to create configuration file failed");
            } else {
                Messages.sprintf("Configuration databases were created successfully");
            }
        } else {
            Messages.sprintf("LOADING CONFIGURATION DATABASE");
            try {
                conf.loadConfig_SQL();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean createConfiguration_db() {
        Messages.sprintf("creatingConfiguration_DB at: " + Main.conf.getAppDataPath() + File.separator + Main.conf.getConfiguration_db_fileName());
        Connection connection = SqliteConnection.connector(Main.conf.getAppDataPath(),
                Main.conf.getConfiguration_db_fileName());
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        // Create configuration table_cols which keeps tableview's widths
        Configuration_SQL_Utils.createConfigurationTable_properties(connection);
        // Create configuration for programs config like currentTheme, workDir, vlcPath
        // etc.
        Configuration_SQL_Utils.createConfiguration_Table(connection);
        try {
            connection.commit();
        } catch (SQLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        // Inserts default params to configuration
        Configuration_SQL_Utils.insert_Configuration(connection, Main.conf);
        Configuration_SQL_Utils.createIgnoredListTable(connection);
        SQL_Utils.createFolderInfosDatabase(connection);

        try {
            connection.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {

            connection.close();
            return true;
        } catch (Exception e) {
            System.err.println("Can't close database file at: " + Main.conf.getAppDataPath());
            e.printStackTrace();
            return false;
        }

    }

    public static void loadConfig_GUI(Model_main modelMain) {
        Messages.sprintf("loadConfig_GUI Started: " + Main.conf.getAppDataPath());
        Connection connection = null;
        boolean createDatabase = false;
        Path configFile = Paths.get(Main.conf.getAppDataPath() + File.separator);
        configFile.toFile().setWritable(true);
        configFile.toFile().setReadable(true);

        if (!Files.exists(configFile) && Files.isWritable(configFile)) {
            try {
                Path createDirectories = Files.createDirectories(configFile);
                Path createFile = Files.createFile(Paths.get(configFile.toString() + Main.conf.getConfiguration_db_fileName()));
                createFile.toFile().setWritable(true);
                createFile.toFile().setReadable(true);
                if (!Files.exists(createDirectories)) {
                    Messages.errorSmth(ERROR, Main.bundle.getString("cannotCreateConfigFile"), null,
                            Misc.getLineNumber(), true);
                } else {
                    createDatabase = true;
                }
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            connection = SqliteConnection.connector(Main.conf.getAppDataPath(),
                    Main.conf.getConfiguration_db_fileName());

            if (createDatabase) {
                Configuration_SQL_Utils.createConfiguration_Table(connection);
            } else {
                Configuration_SQL_Utils.loadTables(connection, modelMain.tables());
            }
//			try {
//				connection.close();
//			} catch (Exception e) {
//				Messages.sprintfError("loadConfig_GUI error with closing SQL database");
//			}
        }
    }


}
