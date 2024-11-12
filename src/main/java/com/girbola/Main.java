/*
Copyright [2020] [Marko Lokka]

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

/*
@(#)Copyright:  Copyright (c) 2012-2024 All right reserved.
@(#)Author:     Marko Lokka
@(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
@(#)Purpose:    To help to organize images and video files in your harddrive with less pain
*/
package com.girbola;

import com.girbola.concurrency.ConcurrencyUtils;
import com.girbola.configuration.Configuration;
import com.girbola.configuration.ConfigurationUtils;
import com.girbola.configuration.VLCJDiscovery;
import com.girbola.controllers.loading.LoadingProcessTask;
import com.girbola.controllers.main.MainController;
import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.tables.*;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.sql.FolderScannerSQL;
import com.girbola.sql.SQL_Utils;
import com.girbola.sql.SqliteConnection;
import common.utils.date.SimpleDates;
import java.awt.Font;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.awt.*;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.girbola.messages.Messages.sprintf;

public class Main extends Application {

    private static final String ERROR = Main.class.getSimpleName();
    final public static boolean DEBUG = true;
    final public static boolean debugMemory = false;

    public final static boolean DEBUG_CONF = true;
    public final static boolean DEBUG_XML = true;

    public static Configuration conf = new Configuration();
    public static SimpleDates simpleDates = new SimpleDates();

    public static ResourceBundle bundle;
    public static Locale locale;

    public final static String country = "EN";
    public final static String lang = "en";

    private static AtomicBoolean processCancelled = new AtomicBoolean(false);
    private static AtomicBoolean changed = new AtomicBoolean(false);

    private ModelMain model_main = new ModelMain();

    private static Stage main_stage;

    private Task<Boolean> load_FileInfosBackToTableViews = null;

    public static SceneSwitcher scene_Switcher = new SceneSwitcher();

    private Scene primaryScene;
    public static LoadingProcessTask lpt;
    private Task<Void> mainTask;
    private StageControl stageControl;

    @Override
    public void start(Stage primaryStage) throws Exception {
        stageControl = new StageControl(model_main, primaryStage);

        try {
            locale = new Locale(lang, country);
            bundle = ResourceBundle.getBundle("bundle/lang", locale);
        } catch (Exception e) {
            e.printStackTrace();
            Messages.sprintfError("Something went wrong: " + e.getMessage());
        }

        mainTask = new Task<>() {
            @Override
            protected Void call() {
                setMain_stage(primaryStage);
                setChanged(false);
                conf.setModel(model_main);
                conf.createProgramPaths();
                ConfigurationUtils.loadConfig();
                Messages.sprintf("CONFIG contains: " + conf.toString());
                Messages.sprintf("Java version: " + System.getProperty("java.version"));
                Messages.sprintf("JavaFX version: " + System.getProperty("javafx.version"));
                GraphicsEnvironment ge = GraphicsEnvironment
                        .getLocalGraphicsEnvironment();

                Font[] allFonts = ge.getAllFonts();

                Messages.sprintf("Created program path and loaded config. The workDir should something else than NULL? "
                        + conf.getWorkDir());

                FXMLLoader main_loader = null;

                Parent parent = null;
                try {
                    main_loader = new FXMLLoader(getClass().getResource("fxml/main/Main.fxml"), bundle);
                    parent = main_loader.load();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Messages.sprintf("error loading parent= " + ex.getMessage());
                    cancel();
                }
                primaryStage.getIcons().add(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("/img/mdir_m2_icon.png"))));
                primaryScene = new Scene(parent);

                if (conf.getThemePath() == null) {
                    Messages.sprintf("conf.getThemePath() == null");
                }

                primaryScene.getStylesheets().add(Main.class.getResource(conf.getThemePath() + MDir_Stylesheets_Constants.MAINSTYLE.getType()).toExternalForm());

                primaryScene.widthProperty().addListener((observableValue, number, t1) -> Messages.sprintf("wdth: " + t1));

                MainController mainController = (MainController) main_loader.getController();
                mainController.initialize(model_main);
                Platform.runLater(() -> {
                    scene_Switcher.setWindow(primaryStage);
                    scene_Switcher.setScene_main(primaryScene);
                    primaryStage.setScene(primaryScene);
                    primaryStage.show();
                    model_main.getBottomController().initBottomWorkdirMonitors();
                });

                lpt = new LoadingProcessTask(scene_Switcher.getWindow());

                Platform.runLater(() -> {
                    lpt.setTask(mainTask);
                });

                // ScenicView.show(parent);
                return null;
            }
        };

        mainTask.setOnSucceeded(event -> {
            Messages.sprintf("main succeeded");

            boolean isValidOS = Misc.checkOS();
            if (isValidOS) {
                Messages.sprintf("Valid OS found");
            } else {
                Messages.sprintfError("Valid OS NOT found");
            }

            ConfigurationUtils.loadConfig_GUI(model_main);

            FolderScannerSQL.loadSelectedFolders(model_main);
            Messages.workdirConnection(Paths.get(Main.conf.getWorkDir()));
            if (Platform.isFxApplicationThread()) {
                Messages.workdirConnection(Paths.get(Main.conf.getWorkDir()));
            } else {
                Platform.runLater(() -> Paths.get(Main.conf.getWorkDir()));
            }

            Connection connection_loadConfigurationFile = SqliteConnection.connector(conf.getAppDataPath(), conf.getConfiguration_db_fileName());
            stageControl.setStageBoundarys();

            if (SQL_Utils.isDbConnected(connection_loadConfigurationFile)) {
                Messages.sprintf("Loading workdir content: " + conf.getAppDataPath() + " filename: " + conf.getConfiguration_db_fileName());
                load_FileInfosBackToTableViews = new Load_FileInfosBackToTableViews(model_main,
                        connection_loadConfigurationFile);
                load_FileInfosBackToTableViews.setOnSucceeded(event1 -> {
                    Platform.runLater(() -> {
                        lpt.closeStage();
                    });
                    model_main.tables().registerTableView_obs_listener();
                    primaryStage.setOnCloseRequest(model_main.exitProgram);
                    try {
                        if (!load_FileInfosBackToTableViews.get()) {
                            Messages.warningText("Can't load previous session!");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    TableUtils.refreshAllTableContent(model_main.tables());

                    Messages.sprintf("load_FileInfosBackToTableViews succeeded: " + Paths.get(conf.getWorkDir()));

                    model_main.getMonitorExternalDriveConnectivity().restart();

                    VLCJDiscovery.initVlc();

                    autoResizeColumns(model_main.tables().getSorted_table());
                    autoResizeColumns(model_main.tables().getSorted_table());
                    autoResizeColumns(model_main.tables().getSortIt_table());
                    autoResizeColumns(model_main.tables().getSortIt_table());
                });

                load_FileInfosBackToTableViews.setOnCancelled(event12 -> {

                    primaryStage.setOnCloseRequest(event14 -> model_main.exitProgram_NOSAVE());
                    Platform.runLater(() -> {
                        lpt.closeStage();
                    });

                });
                load_FileInfosBackToTableViews.setOnFailed(event13 -> {
                    primaryStage.setOnCloseRequest(event131 -> model_main.exitProgram_NOSAVE());
                    lpt.closeStage();
                });

                lpt.setTask(load_FileInfosBackToTableViews);
                Thread load = new Thread(load_FileInfosBackToTableViews, "Main thread");
                load.setDaemon(true);
                load.start();
            } else {
                lpt.closeStage();
            }
        });
        mainTask.setOnFailed(event -> {
            Messages.sprintfError("Main Task failed!!!");
            if (lpt != null) {
                lpt.closeStage();
            }

            Messages.sprintf("Something went wrong while loading main window");

            System.exit(1);
        });
        mainTask.setOnCancelled(event -> {
            Messages.sprintf("Main Task cancelled");
            lpt.closeStage();
        });

        Thread mainTask_th = new Thread(mainTask, "mainTask_th");
        mainTask_th.setDaemon(true);
        mainTask_th.start();

    }

    public static void autoResizeColumns(TableView<?> table) {
        //Set the right policy
        //table.setColumnResizePolicy( TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.getColumns().stream().forEach((column) ->
        {
            //Minimal width = columnheader
            Text t = new Text(column.getText());
            double max = t.getLayoutBounds().getWidth();
            for (int i = 0; i < table.getItems().size(); i++) {
                //cell must not be empty
                if (column.getCellData(i) != null) {
                    t = new Text(column.getCellData(i).toString());
                    double calcwidth = t.getLayoutBounds().getWidth();
                    //remember new max-width
                    if (calcwidth > max) {
                        max = calcwidth;
                    }
                }
            }
            //set the new max-widht with some extra space
            column.setPrefWidth(max + 10.0d);
        });
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
        System.out.println("Stopping app");
        model_main.getMonitorExternalDriveConnectivity().cancel();
        ConcurrencyUtils.stopAllExecThreadNow();
        System.out.println("Threads are ended. Exiting...");
    }

    public static void centerWindowDialog(Stage stage) {
        Messages.sprintf("centerWindowDialog...");
        if (Main.conf.getWindowStartPosX() > 0) {
            stage.setOnShowing(ev -> {
                Platform.runLater(() -> {
                    stage.setX(Main.conf.getWindowStartPosX() + (Main.conf.getWindowStartWidth() / 2)
                            - (stage.getWidth() / 2));
                    stage.setY((Main.conf.getWindowStartPosY() + (Main.conf.getWindowStartHeight() / 2)
                            - stage.getHeight() / 2));
                });
            });
        }
    }

}
