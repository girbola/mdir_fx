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
import com.girbola.configuration.VLCJDiscovery;
import com.girbola.controllers.loading.LoadingProcessTask;
import com.girbola.controllers.main.MainController;
import com.girbola.controllers.main.Model_main;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.sql.SQL_Utils;
import com.girbola.sql.SqliteConnection;
import common.utils.date.SimpleDates;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.robot.Robot;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.nio.file.Paths;
import java.sql.Connection;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
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

    private Model_main model_main = new Model_main();

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

        mainTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                setMain_stage(primaryStage);
                setChanged(false);
                conf.setModel(model_main);
                conf.createProgramPaths();
                conf.loadConfig();
                Messages.sprintf("CONFIG contains: " + conf.toString());
                System.out.println("Java version: " + System.getProperty("java.version"));
                System.out.println("JavaFX version: " + System.getProperty("javafx.version"));

                Messages.sprintf("Created program path and loaded config. The workDir should something else than NULL? "
                        + conf.getWorkDir());

                FXMLLoader main_loader = null;

                Parent parent = null;
                try {
                    ResourceBundle bundle2 = bundle;
                    main_loader = new FXMLLoader(getClass().getResource("fxml/main/Main.fxml"), bundle);
                    parent = main_loader.load();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Messages.sprintf("error loading parent= " + ex.getMessage());
                    cancel();
                }

                primaryScene = new Scene(parent);

                primaryScene.getStylesheets()
                        .add(Main.class.getResource(conf.getThemePath() + "mainStyle.css").toExternalForm());

                Messages.sprintf("theme path is: " + conf.getThemePath());
                MainController mainController = (MainController) main_loader.getController();
                mainController.initialize(model_main);

                scene_Switcher.setWindow(primaryStage);
                scene_Switcher.setScene_main(primaryScene);

                Platform.runLater(() -> {
                    primaryStage.setScene(primaryScene);

//						defineScreenBounds(primaryStage);
                    primaryStage.show();

                    model_main.getBottomController().initBottomWorkdirMonitors();
                });
                lpt = new LoadingProcessTask(scene_Switcher.getWindow());
                Platform.runLater(() -> {
                    lpt.setTask(mainTask);
//					lpt.showLoadStage();
                });
//				ScenicView.show(parent);
                return null;
            }
        };

        mainTask.setOnSucceeded(event -> {
            Messages.sprintf("main succeeded");
//				scene_Switcher.setWindow(primaryStage);
//				scene_Switcher.setScene_main(primaryScene);

            boolean isValidOS = Misc.checkOS();
            if (isValidOS) {
                Messages.sprintf("Valid OS found");
            } else {
                Messages.sprintfError("Valid OS NOT found");
            }

            conf.loadConfig_GUI();

            model_main.getSelectedFolders().load_SelectedFolders_UsingSQL(model_main);

            Connection connection_loadConfigurationFile = SqliteConnection.connector(conf.getAppDataPath(),
                    conf.getConfiguration_db_fileName());
            stageControl.setStageBoundarys();
Messages.sprintf("CAEONRGOAERGNAERg: " + conf.toString());
            if (SQL_Utils.isDbConnected(connection_loadConfigurationFile)) {
                Messages.sprintf("Loading workdir content: " + conf.getAppDataPath() + " filename: "
                        + conf.getConfiguration_db_fileName());
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

                    boolean loaded = model_main.getWorkDir_Handler().loadAllLists(Paths.get(conf.getWorkDir()));
                    if (loaded) {
                        for (FileInfo finfo : model_main.getWorkDir_Handler().getWorkDir_List()) {
                            Messages.sprintf("loadAllLists fileInfo loading: " + finfo.getOrgPath());
                        }
                        Messages.sprintf("==============Loading workdir size is: "
                                + model_main.getWorkDir_Handler().getWorkDir_List().size());

                        VLCJDiscovery.initVlc();
                    }

                    TableUtils.calculateTableViewsStatistic(model_main.tables());
                    getMain_stage().maximizedProperty().addListener((ov, t, t1) -> {
//									model_main.tables().getHideButtons().updateVisibleTableWidths();
                        System.out.println("model_main.tables().getHideButtons().updateTableVisible();: "
                                + t1.booleanValue());
                    });

//							conf.windowStartPosX_property().bind(primaryScene.xProperty());
//							conf.windowStartPosY_property().bind(primaryScene.yProperty());
                });
                load_FileInfosBackToTableViews.setOnCancelled(event12 -> {
                    try {
                        if (load_FileInfosBackToTableViews.get() == false) {
                            Messages.errorSmth(ERROR, bundle.getString("cannotLoadFolderInfoDatFile"), null,
                                    Misc.getLineNumber(), true);
                        }
                    } catch (InterruptedException | ExecutionException ex) {
                        Messages.errorSmth(ERROR, bundle.getString("cannotLoadFolderInfoDatFile"), ex,
                                Misc.getLineNumber(), true);
                    }
                    primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                        @Override
                        public void handle(WindowEvent event12) {
                            model_main.exitProgram_NOSAVE();
                        }
                    });
                    lpt.closeStage();
                });
                load_FileInfosBackToTableViews.setOnFailed(event13 -> {
                    primaryStage.setOnCloseRequest(event131 -> model_main.exitProgram_NOSAVE());
                    lpt.closeStage();
                });

                lpt.setTask(load_FileInfosBackToTableViews);
                Thread load = new Thread(load_FileInfosBackToTableViews, "Main thread");
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
            primaryStage.setOnCloseRequest(model_main.dontExit);
            Messages.errorSmth(ERROR, "Something went wrong while loading main window", null, Misc.getLineNumber(),
                    true);
            model_main.exitProgram_NOSAVE();
        });
        mainTask.setOnCancelled(event -> {
            Messages.sprintf("Main Task cancelled");
            lpt.closeStage();
        });

        Thread mainTask_th = new Thread(mainTask, "mainTask_th");
        mainTask_th.run();

    }

    private Point2D defineScreenWithMouseCursor() {
        Robot r = new Robot();
        Point2D d = r.getMousePosition();
        return d;

    }

    public static AtomicBoolean processCancelled_property() {
        return processCancelled;
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

    public Screen getScreen() {
        Robot r = new Robot();
        Point2D d = r.getMousePosition();
        for (Screen sc : Screen.getScreens()) {
            double minX = sc.getVisualBounds().getMinX();
            double maxX = sc.getVisualBounds().getMaxX();
            if (d.getX() >= minX && d.getX() <= maxX) {
                return sc;
            }

        }
        return Screen.getPrimary();
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
        model_main.getMonitorExternalDriveConnectivity().cancel();
    }

    public static void centerWindowDialog(Stage stage) {
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
