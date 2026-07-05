/*
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package com.girbola;

import com.girbola.concurrency.ConcurrencyUtils;
import com.girbola.configuration.Configuration;
import com.girbola.configuration.ConfigurationSqlConnection;
import com.girbola.configuration.VLCJDiscovery;
import com.girbola.controllers.loading.LoadingProcessTask;
import com.girbola.controllers.main.MainController;
import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.sql.ConfigurationSQLHandler;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.messages.Messages;
import com.girbola.messages.html.HTMLClass;
import com.girbola.misc.Misc;
import com.girbola.persistence.fileinfo.FileInfoSqlConnectionFactory;
import com.girbola.sql.SQL_Utils;
import com.girbola.persistence.selectedfolderinfo.SelectedFolderInfoDao;
import common.utils.date.SimpleDates;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.girbola.controllers.misc.Misc_GUI.autoResizeColumns;
import static com.girbola.messages.Messages.sprintf;

public class Main extends Application {

    private static final String ERROR = Main.class.getSimpleName();

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static RandomAccessFile lockFile;
    private static FileLock fileLock;

    private ModelMain model_main = new ModelMain();
    private Scene primaryScene;
    private StageControl stageControl;
    private Service<Boolean> load_FileInfosBackToTableViews = null;
    private Task<Void> mainTask;
    private static AtomicBoolean changed = new AtomicBoolean(false);
    private static AtomicBoolean processCancelled = new AtomicBoolean(false);
    private static Stage main_stage;
    public static Configuration conf = new Configuration();
    public static LoadingProcessTask lpt;
    public static Locale locale;
    public static ResourceBundle bundle;
    public static SceneSwitcher sceneManager = new SceneSwitcher();
    public static SimpleDates simpleDates = new SimpleDates();

    public static final boolean DEBUG = true;
    public static final boolean DEBUG_CONF = true;
    public static final String country = "EN";
    public static final String lang = "en";

    public static final String APP_NAME = "MDir - Image and Video Organizer";
    public static final String APP_VERSION = "1.0.0";
    public static final String APP_AUTHOR = "Marko Lokka";
    public static final String APP_EMAIL = "girbola9@gmail.com";
    public static final String APP_WEBSITE = "https://github.com//mdir_fx";
    public static final String APP_COPYRIGHT = "Copyright (c) 2026 All right reserved.";
    public static final String APP_LICENSE = "Apache License, Version 2.0";

    private static final String BUNDLE_PATH = "bundle/lang";

    @Override
    public void init() throws Exception {
        super.init();

        if (lang == null || country == null) {
            throw new IllegalStateException("Language or country code not initialized");
        }

        try {
            locale = Locale.of(lang, country);
            if (locale == null) {
                Messages.sprintfError("Locale was null");
                Platform.exit();
                return;
            }
            if (country.equals("EN") && lang.equals("en") || country.equals("FI") && lang.equals("fi") || country.equals("SV") && lang.equals("sv")) {
                Messages.sprintf("Locale is: " + locale.getLanguage() + ", " + locale.getCountry());
            } else {
                Messages.sprintfError("Locale is: " + locale.getLanguage() + ", " + locale.getCountry());
                Platform.exit();
            }
            boolean loadSpecificBundle = loadSpecificBundle();
            if (!loadSpecificBundle) {
                boolean loadDefaultBundle = loadDefaultBundle();
                if (!loadDefaultBundle) {
                    Messages.sprintfError("Unable to load default bundle");
                    Platform.exit();
                } else {
                    Messages.sprintf("=====Successfully loaded default resource bundle: " + bundle.getString("startBatchCopy"));
                }
            }
        } catch (MissingResourceException e) {
            boolean loadedDefault = loadDefaultBundle();
            if (bundle == null || !loadedDefault) {
                Messages.sprintfError("Unable to load default bundle");
            }

        }
    }
    /*private boolean loadSpecificBundle() throws MissingResourceException {
        // Add these imports at the top of your file
        // import org.apache.log4j.Logger;


        logger.debug("Attempting to load bundle:");
        logger.debug("- BUNDLE_PATH: " + BUNDLE_PATH);
        logger.debug("- Locale country: " + locale.getCountry());
        logger.debug("- Locale language: " + locale.getLanguage());
        logger.debug("- ClassLoader: " + Main.class.getClassLoader());

        try {
            URL resourceURL = Main.class.getClassLoader().getResource(BUNDLE_PATH.replace(".", "/") + ".properties");
            logger.debug("- Looking for resource at: " + (resourceURL != null ? resourceURL : "not found"));

            bundle = ResourceBundle.getBundle(BUNDLE_PATH, locale, Main.class.getClassLoader());

            logger.debug("- Bundle loaded: " + bundle.getClass().getName());
            logger.debug("- Bundle locale: " + bundle.getLocale());
            logger.debug("- Available keys: " + String.join(", ", bundle.keySet()));

            if (bundle != null) {
                Messages.sprintf("=====Successfully loaded specific resource bundle: " + bundle.getString("startBatchCopy"));
                return true;
            }
        } catch (MissingResourceException e) {
            logger.error("Failed to load bundle: " + e.getMessage());
            logger.error("- Cause: " + e.getCause());
            logger.error("- Key: " + e.getKey());
            logger.error("- Expected path: " + e.getClassName());
            throw e;
        }

        logger.warn("Locale NOT initialized: country=" + locale.getCountry() + ", language=" + locale.getLanguage());
        return false;
    }*/

    private boolean loadSpecificBundle() throws MissingResourceException {
        bundle = ResourceBundle.getBundle(BUNDLE_PATH, locale, Main.class.getClassLoader());
        if (bundle != null) {
            Messages.sprintf("=====Successfully loaded specific resource bundle: " + bundle.getString("startBatchCopy"));
            return true;
        }
        Messages.sprintf("Locale NOT initialized: country=" + locale.getCountry() + ", language=" + locale.getLanguage());
        return false;
    }

    private boolean loadDefaultBundle() {
        try {
            bundle = ResourceBundle.getBundle(BUNDLE_PATH);

            Messages.sprintf("=====Successfully loaded default resource bundle. startBatchCopy = " + bundle.getString("startBatchCopy"));
            return true;
        } catch (MissingResourceException defaultEx) {
            boolean fallingBack = loadFallbackBundle();
            if (bundle.getString("startBatchCopy").equals("Start Batch Copy") && bundle != null && fallingBack) {
                Messages.sprintf("Fallback bundle were loaded successfully: " + bundle.toString());
                return true;
            } else {
                Messages.sprintfError("Cannot load bundle!");
                return false;
            }
        }
    }

    private boolean loadFallbackBundle() {
        bundle = new MinimalFallbackBundle();
        if (bundle != null) {
            return true;
        }
        return false;
    }

    private static class MinimalFallbackBundle extends ListResourceBundle {
        @Override
        protected Object[][] getContents() {
            return new Object[][]{{"startBatchCopy", "Start Batch Copy"}, {"error.general", "An error occurred"}, {"button.ok", "OK"},
                    // Add other essential messages
            };
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        if (!acquireSingleInstanceLock()) {
            Messages.sprintfError("Another instance of " + APP_NAME + " is already running!");
            Platform.exit();
            return;
        }

        stageControl = new StageControl(model_main, primaryStage);

        mainTask = new Task<>() {
            @Override
            protected Void call() {
                setMain_stage(primaryStage);
                setChanged(false);
                conf.setModel(model_main);
                conf.createProgramPaths();
//                ConfigurationSQLHandler.loadConfiguration(Main.conf);

                Messages.sprintf("\n╔════════════════════════════════════════╗");
                Messages.sprintf("║  SYSTEM INFORMATION                    ║");
                Messages.sprintf("╚════════════════════════════════════════╝");

                Messages.sprintf("  File Encoding:     " + System.getProperty("file.encoding"));
                Messages.sprintf("  Default Charset:   " + String.valueOf(java.nio.charset.Charset.defaultCharset()));
                Messages.sprintf("  Configuration:     " + conf.toString());
                Messages.sprintf("  Java Version:      " + System.getProperty("java.version"));
                Messages.sprintf("  JavaFX Version:    " + System.getProperty("javafx.version"));
                System.setProperty("javafx.verbose", "false");

//                GraphicsEnvironment ge = GraphicsEnvironment
//                        .getLocalGraphicsEnvironment();

                Messages.sprintf("Created program path and loaded config. The workDir should something else than NULL? " + conf.getWorkDir());

                FXMLLoader main_loader = null;

                Parent parent = null;
                try {
                    main_loader = new FXMLLoader(getClass().getResource("fxml/main/Main.fxml"), bundle);

                    parent = main_loader.load();
                } catch (Exception ex) {
                    Messages.sprintf("error loading parent= " + ex.getMessage());
                    ex.printStackTrace();
                    cancel();
                }
                primaryStage.getIcons().add(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("/img/mdir_m2_icon.png"))));

                try {
                    if(Taskbar.isTaskbarSupported()) {
                        Taskbar.getTaskbar().setIconImage(
                                Toolkit.getDefaultToolkit().getImage(
                                        Main.class.getResource("/img/mdir_m2_icon.png")
                                )
                        );
                    }
                } catch (UnsupportedOperationException e) {
                    Messages.sprintfError("AWT Taskbar is not supported on this platform: " + e.getMessage());
                } catch (Exception e) {
                    Messages.sprintfError("Error setting AWT Taskbar icon: " + e.getMessage());
                }

                /*
                             primaryStage.getIcons().add(new Image(Objects.requireNonNull(
                        Main.class.getResourceAsStream("/img/mdir_m2_icon.png")
                )));

                // 2. macOS Dock-kuvake (Suojattu tarkistuksilla)
                try {
                    if (Taskbar.isTaskbarSupported()) {
                        Taskbar taskbar = Taskbar.getTaskbar();
                        if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
                            java.awt.Image macIcon = Toolkit.getDefaultToolkit().getImage(
                                    Main.class.getResource("/img/mdir_m2_icon.png")
                            );
                            taskbar.setIconImage(macIcon);
                        }
                    }
                } catch (Exception e) {
                    // Hiljennetään virhe, jos alusta ei tue AWT Taskbaria
                    System.out.println("AWT Taskbar ei ole tuettu tällä alustalla: " + e.getMessage());
                }

                 */

                primaryScene = new Scene(parent);

                if (conf.getThemePath() == null) {
                    Messages.sprintf("conf.getThemePath() == null");
                }
                try {

                    if (conf.getThemePath() != null) {
                        Messages.sprintf("conf.getThemePath() != null: " + conf.getThemePath());
                    }
                    if (MDir_Stylesheets_Constants.MAINSTYLE.getType() != null) {
                        Messages.sprintf("MDir_Stylesheets_Constants.MAINSTYLE.getType() != null: " + conf.getThemePath() + MDir_Stylesheets_Constants.MAINSTYLE.getType());
                    }
                    primaryScene.getStylesheets().add(Main.class.getResource(conf.getThemePath() + MDir_Stylesheets_Constants.MAINSTYLE.getType()).toExternalForm());
                } catch (Exception e) {
                    Messages.sprintfError("Something went wrong: " + e.getMessage());
                    System.exit(1);
                }
                primaryScene.widthProperty().addListener((observableValue, number, t1) -> Messages.sprintf("wdth: " + t1));

                MainController mainController = (MainController) main_loader.getController();
                mainController.initialize(model_main);
                Platform.runLater(() -> {
                    sceneManager.setWindow(primaryStage);
                    sceneManager.setScene_main(primaryScene);
                    primaryStage.setScene(primaryScene);
                    primaryStage.show();
                    model_main.getBottomController().initBottomWorkdirMonitors();
                    model_main.getFolderScannerController().initFolderScanner();

                });

//                lpt = new LoadingProcessTask(sceneManager.getWindow());
//
//                Platform.runLater(() -> {
//                    lpt.setTask(mainTask);
//                });

                // ScenicView.show(parent);
                return null;
            }
        };

        mainTask.setOnSucceeded(event -> {
            //ConfigurationSQLHandler.createConfigurationDatabase();
//            boolean configurationDatabase = ConfigurationSQLHandler.createConfigurationDatabase();
//            if (!configurationDatabase) {
//                Messages.sprintfError("Couldn't create configuration database");
//                lpt.closeStage();
//                return;
//            }

            ConfigurationSQLHandler.loadConfiguration(Main.conf);

            SelectedFolderInfoDao.loadSelectedFolders(model_main);

            // Loads save folders from configuration db
            Connection configurationLoadedFile = ConfigurationSqlConnection.connectToDatabase(conf.getAppDataPath(), conf.getConfiguration_db_fileName());


            VLCJDiscovery.initVlc();

            if (SQL_Utils.isDbConnected(configurationLoadedFile)) {
                Messages.sprintf("Loading workdir content: " + conf.getAppDataPath() + " filename: " + conf.getConfiguration_db_fileName());
                load_FileInfosBackToTableViews = new LoadFileInfosBackToTableViews(model_main, configurationLoadedFile);
                load_FileInfosBackToTableViews.valueProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        boolean result = newVal;
                        if (!result) {
                            Messages.warningText("Can't load previous session!");
                            Platform.runLater(() -> {
                                lpt.closeStage();
                            });
                        }

                        // Use the result value here
                    }
                });
                load_FileInfosBackToTableViews.setOnSucceeded(event1 -> {
                    Messages.sprintf("load_FileInfosBackToTableViews succeeded");
                    Platform.runLater(() -> {
                        lpt.closeStage();
                    });
                    model_main.tables().registerTableViewObsListener();
                    primaryStage.setOnCloseRequest(model_main.exitProgram);

                    TableUtils.refreshAllTableContent(model_main.tables());

                    Messages.sprintf("load_FileInfosBackToTableViews succeeded: " + Paths.get(conf.getWorkDir()));

                    model_main.getMonitorExternalDriveConnectivity().restart();
                    //stageControl.setStageBoundarys();
                    autoResizeColumns(model_main.tables().getSorted_table(), 10d);
                    autoResizeColumns(model_main.tables().getSorted_table(), 10d);
                    autoResizeColumns(model_main.tables().getSortIt_table(), 10d);
                    autoResizeColumns(model_main.tables().getSortIt_table(), 10d);
                });

                load_FileInfosBackToTableViews.setOnCancelled(event12 -> {
                    Messages.sprintfError(("load_FileInfosBackToTableViews cancelled"));
                    primaryStage.setOnCloseRequest(event14 -> model_main.exitProgram_NOSAVE());
                    Platform.runLater(() -> lpt.closeStage());
                });
                load_FileInfosBackToTableViews.setOnFailed(event13 -> {
                    Messages.sprintfError("load_FileInfosBackToTableViews failed");
                    primaryStage.setOnCloseRequest(event131 -> model_main.exitProgram_NOSAVE());
                    lpt.closeStage();
                });

                load_FileInfosBackToTableViews.start();

                /*lpt.setTask(load_FileInfosBackToTableViews);
                Thread load = new Thread(load_FileInfosBackToTableViews, "Load_FileInfosBackToTableViewsThread");
//                load.setDaemon(true);
                load.start();*/
            } else {
                Messages.sprintfError("11111 Couldn't connect to database");
                lpt.closeStage();
            }
        });
// Modify the mainTask.setOnFailed handler
        mainTask.setOnFailed(event -> {
            Messages.sprintfError("Main Task failed!!!");
            try {
                if (lpt != null) {
                    lpt.closeStage();
                }
            } catch (Exception e) {
                Messages.sprintfError("Error while closing loading stage: " + e.getMessage());
            }
            Messages.sprintf("Something went wrong while loading main window");
            System.exit(1);
        });

// Modify the mainTask.setOnCancelled handler
        mainTask.setOnCancelled(event -> {
            Messages.sprintf("Main Task cancelled");
            try {
                if (lpt != null) {
                    lpt.closeStage();
                }
            } catch (Exception e) {
                Messages.sprintfError("Error while closing loading stage: " + e.getMessage());
                System.exit(1);
            }
        });

// Modify where lpt is initialized in the mainTask
// Move this initialization earlier in the task, before any UI operations
        lpt = new LoadingProcessTask(sceneManager.getWindow());
        Platform.runLater(() -> {
            if (lpt != null) {
                lpt.setTask(mainTask);
            }
        });
        Thread mainTaskTh = new Thread(mainTask, "mainTaskTh");
        mainTaskTh.setDaemon(false);
        Messages.sprintf("main succeeded");

        boolean isValidOS = Misc.checkOS();
        if (isValidOS) {
            Messages.sprintf("Valid OS found");
            mainTaskTh.start();
        } else {
            Messages.sprintfError("Your OS: " + Misc.getCurrentOs() + " is not supported yet" + "\n\n" + "Please visit our homepage: " + HTMLClass.programHomePage);
        }

    }


    public static boolean getProcessCancelled() {
        return processCancelled.get();
    }

    public static void setProcessCancelled(boolean aProcessCancelled) {
        processCancelled.set(aProcessCancelled);
        sprintf("Process has been set to cancel= " + aProcessCancelled);
        if (aProcessCancelled) {
            ConcurrencyUtils.stopExecThreadNow();
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    public static boolean getChanged() {
        return changed.get();
    }

    public static void setChanged(boolean value) {
        if (value == true && changed.get() == true) {
            return;
        }
        changed.set(value);
        if (changed.get() == true) {
            Platform.runLater(() -> {
                getMain_stage().setTitle("");
                getMain_stage().setTitle("* " + conf.getProgramName());
            });
        } else {
            Platform.runLater(() -> {
                getMain_stage().setTitle("");
                getMain_stage().setTitle(conf.getProgramName());
            });
        }
    }

    public static Stage getMain_stage() {
        return main_stage;
    }

    public static void setMain_stage(Stage main_stage) {
        Main.main_stage = main_stage;
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        Main.setProcessCancelled(true);
        Messages.sprintf("Stopping app");
        model_main.getMonitorExternalDriveConnectivity().cancel();

        ConfigurationSQLHandler.closeConnection();
//        model_main.getSqlConfigurationHandler().closeConfigurationConnection();

        Messages.sprintf("Configuration connection closed");

        ConcurrencyUtils.stopAllExecThreadNow();
        FileInfoSqlConnectionFactory.closeAllConnections();
        releaseSingleInstanceLock();
        Messages.sprintf("Program has ended. Exiting...");
        Platform.exit();
    }

    private boolean acquireSingleInstanceLock() {
        try {
            String tempDir = System.getProperty("java.io.tmpdir");
            File lockFileObj = new File(tempDir, "mdir_application.lock");
            lockFile = new RandomAccessFile(lockFileObj, "rw");
            fileLock = lockFile.getChannel().tryLock();

            if (fileLock == null) {
                lockFile.close();
                return false;
            }

            Messages.sprintf("Single instance lock acquired successfully");
            return true;
        } catch (IOException e) {
            Messages.sprintfError("Failed to acquire single instance lock: " + e.getMessage());
            return false;
        }
    }

    private void releaseSingleInstanceLock() {
        try {
            if (fileLock != null && fileLock.isValid()) {
                fileLock.release();
                Messages.sprintf("Single instance lock released");
            }
            if (lockFile != null) {
                lockFile.close();
            }
        } catch (IOException e) {
            Messages.sprintfError("Error releasing single instance lock: " + e.getMessage());
        }
    }

    public static void centerWindowDialog(Stage stage) {
        Messages.sprintf("centerWindowDialog...");
        if (Main.conf.getWindowStartPosX() > 0) {
            stage.setOnShowing(ev -> {
                Platform.runLater(() -> {
                    stage.setX(Main.conf.getWindowStartPosX() + (Main.conf.getWindowStartWidth() / 2) - (stage.getWidth() / 2));
                    stage.setY((Main.conf.getWindowStartPosY() + (Main.conf.getWindowStartHeight() / 2) - stage.getHeight() / 2));
                });
            });
        }
    }
}
