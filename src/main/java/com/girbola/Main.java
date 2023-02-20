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
@(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
@(#)Author:     Marko Lokka
@(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
@(#)Purpose:    To help to organize images and video files in your harddrive with less pain
*/
package com.girbola;

import static com.girbola.messages.Messages.sprintf;

import java.net.URL;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.girbola.concurrency.ConcurrencyUtils;
import com.girbola.configuration.Configuration;
import com.girbola.configuration.VLCJDiscovery;
import com.girbola.controllers.loading.LoadingProcess_Task;
import com.girbola.controllers.main.MainController;
import com.girbola.controllers.main.Model_main;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.sql.SQL_Utils;
import com.girbola.sql.SqliteConnection;

import com.girbola.vlcj.VLCPlayerController;
import common.utils.date.SimpleDates;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.robot.Robot;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

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
	private LoadingProcess_Task lpt;
	private Task<Void> mainTask;

	@Override
	public void start(Stage primaryStage) throws Exception {

		primaryStage.setUserData(model_main);
		mainTask = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				setMain_stage(primaryStage);
				setChanged(false);
				sprintf("Program starting");
				locale = new Locale(lang, country);
				bundle = ResourceBundle.getBundle("bundle/lang", locale);

				conf.setModel(model_main);
				conf.createProgramPaths();
				conf.loadConfig();
				System.out.println("Java version: " + System.getProperty("java.version"));
				System.out.println("JavaFX version: " + System.getProperty("javafx.version"));

				Messages.sprintf("Created program path and loaded config. The workDir should something else than NULL? "
						+ conf.getWorkDir());

				FXMLLoader main_loader = null;

				Parent parent = null;
				try {
					ResourceBundle bundle2 = bundle;
					sprintf("bundle2: " + bundle2);
					URL resource = getClass().getResource("fxml/main/Main.fxml");
					sprintf("bundle2: " + bundle2 + " resource " + resource);
					main_loader = new FXMLLoader(getClass().getResource("fxml/main/Main.fxml"), bundle);
					parent = main_loader.load();
					sprintf("main_loader location: " + main_loader.getLocation());
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

				primaryStage.setTitle(conf.getProgramName());

				primaryStage.setMinWidth(800);
				primaryStage.setMinHeight(600);
				primaryStage.setX(Main.conf.getWindowStartPosX());
				primaryStage.setY(Main.conf.getWindowStartPosY());
				primaryStage.setWidth(Main.conf.getWindowStartWidth());
				primaryStage.setHeight(Main.conf.getWindowStartHeight());

				scene_Switcher.setWindow(primaryStage);
				scene_Switcher.setScene_main(primaryScene);

				primaryStage.fullScreenProperty().addListener(new ChangeListener<Boolean>() {
					@Override
					public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
							Boolean newValue) {
						Messages.sprintf("stage fullScreen changed: " + newValue);
					}
				});
				// stage.setMaximized(true);
				primaryStage.setOnCloseRequest(model_main.dontExit);

				Platform.runLater(() -> {
					primaryStage.setScene(primaryScene);
//						defineScreenBounds(primaryStage);
					primaryStage.show();
					model_main.getBottomController().initBottomWorkdirMonitors();
				});
				lpt = new LoadingProcess_Task(scene_Switcher.getWindow());
				Platform.runLater(() -> {

					lpt.setTask(mainTask);
//					lpt.showLoadStage();
				});
//				ScenicView.show(parent);
				return null;
			}
		};

		mainTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
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
				if (SQL_Utils.isDbConnected(connection_loadConfigurationFile)) {
					Messages.sprintf("Loading workdir content: " + conf.getAppDataPath() + " filename: "
							+ conf.getConfiguration_db_fileName());
					load_FileInfosBackToTableViews = new Load_FileInfosBackToTableViews(model_main,
							connection_loadConfigurationFile);
					load_FileInfosBackToTableViews.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
						@Override
						public void handle(WorkerStateEvent event) {
							lpt.closeStage();
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
							
							primaryStage.xProperty().addListener(new ChangeListener<Number>() {

								@Override
								public void changed(ObservableValue<? extends Number> observable, Number oldValue,
										Number newValue) {
									if (conf != null) {
										conf.setWindowStartPosX((double) newValue);
										Messages.sprintf("windowstartposX: " + newValue);
									}
								}
							});
							
							primaryStage.yProperty().addListener(new ChangeListener<Number>() {

								@Override
								public void changed(ObservableValue<? extends Number> observable, Number oldValue,
										Number newValue) {
									if (conf != null) {
										conf.setWindowStartPosY((double) newValue);
										Messages.sprintf("windowstartposY: " + newValue);
									}
								}
							});
							
							primaryStage.widthProperty().addListener(new ChangeListener<Number>() {

								@Override
								public void changed(ObservableValue<? extends Number> observable, Number oldValue,
										Number newValue) {
									if (conf != null) {
										conf.setWindowStartWidth((double) newValue);
										Messages.sprintf("setWindowStartWidth: " + newValue);
									}
								}
							});
							
							primaryStage.heightProperty().addListener(new ChangeListener<Number>() {

								@Override
								public void changed(ObservableValue<? extends Number> observable, Number oldValue,
										Number newValue) {
									if (conf != null) {
										conf.setWindowStartHeight((double) newValue);
										Messages.sprintf("setWindowStartWidth: " + newValue);
									}
								}
							});
							TableUtils.calculateTableViewsStatistic(model_main.tables());
							getMain_stage().maximizedProperty().addListener(new ChangeListener<Boolean>() {

								@Override
								public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
//									model_main.tables().getHideButtons().updateVisibleTableWidths();
									System.out.println("model_main.tables().getHideButtons().updateTableVisible();: "
											+ t1.booleanValue());
								}
							});

//							conf.windowStartPosX_property().bind(primaryScene.xProperty());
//							conf.windowStartPosY_property().bind(primaryScene.yProperty());
						}
					});
					load_FileInfosBackToTableViews.setOnCancelled(new EventHandler<WorkerStateEvent>() {
						@Override
						public void handle(WorkerStateEvent event) {
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
								public void handle(WindowEvent event) {
									model_main.exitProgram_NOSAVE();
								}
							});
							lpt.closeStage();
						}
					});
					load_FileInfosBackToTableViews.setOnFailed(new EventHandler<WorkerStateEvent>() {
						@Override
						public void handle(WorkerStateEvent event) {
							primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
								@Override
								public void handle(WindowEvent event) {
									model_main.exitProgram_NOSAVE();
								}
							});
							lpt.closeStage();
						}
					});

					lpt.setTask(load_FileInfosBackToTableViews);
					Thread load = new Thread(load_FileInfosBackToTableViews, "Main thread");
					load.start();
				} else {
					lpt.closeStage();
				}
			}
		});
		mainTask.setOnFailed(new EventHandler<WorkerStateEvent>() {

			@Override
			public void handle(WorkerStateEvent event) {
				Messages.sprintfError("Main Task failed!!!");
				if (lpt != null) {
					lpt.closeStage();
				}
				primaryStage.setOnCloseRequest(model_main.dontExit);
				Messages.errorSmth(ERROR, "Something went wrong while loading main window", null, Misc.getLineNumber(),
						true);
				model_main.exitProgram_NOSAVE();
			}
		});
		mainTask.setOnCancelled(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				Messages.sprintf("Main Task cancelled");
				lpt.closeStage();
			}
		});

		Thread mainTask_th = new Thread(mainTask, "mainTask_th");
		mainTask_th.start();
		// lp.showLoadStage();

	}

	private void defineScreenBounds(Stage stage) {
		int screens = Screen.getScreens().size();
		if (stage.isFullScreen()) {
			return;
		}
		if (conf.getWindowStartPosX() == -1 && conf.getWindowStartPosY() == -1 && conf.getWindowStartWidth() == -1
				&& conf.getWindowStartHeight() == -1) {
			stage.setX(0);
			stage.setY(0);

		} else {
			if (screens > 1) {
				double x = conf.getWindowStartPosX();
				double y = conf.getWindowStartPosY();
				double width = conf.getWindowStartWidth();
				double heigth = conf.getWindowStartHeight();
				if (x < 0) {
					x = 0;
				}
				if (y < 0) {
					y = 0;
				}
				/*
				 * sc.getBounds().getHeight(): Rectangle2D [minX = 0.0, minY=0.0, maxX=1366.0,
				 * maxY=768.0, width=1366.0, height=768.0] window x pos: 250.0 y POS: 73.0
				 * window width: 1321.0 height: 623.0 sc.getVisualBounds().getWidth() 1366.0
				 * height 728.0 sc.getBounds().getHeight(): Rectangle2D [minX = 1366.0,
				 * minY=0.0, maxX=4806.0, maxY=1440.0, width=3440.0, height=1440.0] window x
				 * pos: 250.0 y POS: 73.0 window width: 1321.0 height: 623.0
				 * sc.getVisualBounds().getWidth() 3440.0 height 1400.0
				 * 
				 */
				for (Screen sc : Screen.getScreensForRectangle(x, y, width, heigth)) {
					Messages.sprintf("sc.getBounds().getHeight(): " + sc.getBounds());
					Messages.sprintf("window x pos: " + x + " y POS: " + y + " window width: " + width + " height: "
							+ heigth + " sc.getVisualBounds().getWidth() " + sc.getVisualBounds().getWidth()
							+ " height " + sc.getVisualBounds().getHeight());
					if (width >= sc.getBounds().getWidth()) {
						width = (sc.getBounds().getWidth() - 100);
					}

					if (heigth >= sc.getBounds().getHeight()) {
						heigth = (sc.getBounds().getHeight() - 100);
					}
				}
				stage.setX(x);
				stage.setY(y);
				stage.setWidth(width);
				stage.setHeight(heigth);
			} else {
				stage.setX(0);
				stage.setY(0);
				stage.setWidth(800);
				stage.setHeight(640);
			}
		}
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
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					getMain_stage().setTitle("");
					getMain_stage().setTitle("* " + conf.getProgramName());
				}
			});
		} else {
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					getMain_stage().setTitle("");
					getMain_stage().setTitle(conf.getProgramName());
				}

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
				System.out.println("startposX: " + Main.conf.getWindowStartPosX() + " stage width: " + stage.getWidth()
						+ " Main.conf.getWidth: " + Main.conf.getWindowStartWidth());
				System.out.println("startposY: " + Main.conf.getWindowStartPosY() + " stage height: "
						+ stage.getHeight() + " Main.conf.getHeight: " + Main.conf.getWindowStartHeight());
				stage.setX(Main.conf.getWindowStartPosX() + (Main.conf.getWindowStartWidth() / 2)
						- (stage.getWidth() / 2));
				stage.setY((Main.conf.getWindowStartPosY() + (Main.conf.getWindowStartHeight() / 2)
						- stage.getHeight() / 2));
			});
		});

	}

}
