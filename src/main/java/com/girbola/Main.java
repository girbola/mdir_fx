/*
Copyright [2019] [Marko Lokka]

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
@(#)Copyright:  Copyright (c) 2012-2019 All right reserved.
@(#)Author:     Marko Lokka
@(#)Product:    Image and Video Files Organizer Tool
@(#)Purpose:    To help to organize images and video files in your harddrive with less pain
*/
package com.girbola;

import static com.girbola.messages.Messages.sprintf;

import java.nio.file.Paths;
import java.sql.Connection;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.girbola.concurrency.ConcurrencyUtils;
import com.girbola.configuration.Configuration;
import com.girbola.controllers.loading.LoadingProcess_Task;
import com.girbola.controllers.main.MainController;
import com.girbola.controllers.main.Model_main;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.sql.SQL_Utils;
import com.girbola.sql.SqliteConnection;

import common.utils.date.SimpleDates;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
	private Scene scene;

	@Override
	public void start(Stage stage) throws Exception {
		stage.setUserData(model_main);
		Main.setMain_stage(stage);
		setChanged(false);
		sprintf("Program starting");
		locale = new Locale(lang, country);
		bundle = ResourceBundle.getBundle("bundle/lang", locale);

		conf.setModel(model_main);
		conf.createProgramPaths();
		Main.conf.loadConfig();
		System.out.println("Java version: " + System.getProperty("java.version"));

		Task<Void> task = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				FXMLLoader main_loader = new FXMLLoader(getClass().getResource("fxml/main/Main.fxml"), bundle);
				sprintf("main_loader location: " + main_loader.getLocation());
				Parent parent = null;
				try {
					parent = main_loader.load();
				} catch (Exception ex) {
					ex.printStackTrace();
					Messages.sprintf("error loading parent= " + ex.getMessage());
					cancel();
				}

				scene = new Scene(parent);
				scene.widthProperty().addListener(new ChangeListener<Number>() {

					@Override
					public void changed(ObservableValue<? extends Number> observable, Number oldValue,
							Number newValue) {
						Messages.sprintf("Scene width changing: " + newValue);
					}
				});
				scene.heightProperty().addListener(new ChangeListener<Number>() {

					@Override
					public void changed(ObservableValue<? extends Number> observable, Number oldValue,
							Number newValue) {
						Messages.sprintf("Scene height changing: " + newValue);
					}
				});

				scene.getStylesheets()
						.add(Main.class.getResource(conf.getThemePath() + "mainStyle.css").toExternalForm());

				Messages.sprintf("theme path is: " + conf.getThemePath());
				MainController mainController = (MainController) main_loader.getController();
				mainController.initialize(model_main);

				stage.setTitle(conf.getProgramName());
				stage.setMaxWidth(conf.getScreenBounds().getWidth());
				stage.setMaxHeight(conf.getScreenBounds().getHeight() - 20);
				// stage.setMinWidth(600);
				// stage.setMinHeight(600);
				stage.fullScreenProperty().addListener(new ChangeListener<Boolean>() {

					@Override
					public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
							Boolean newValue) {
						Messages.sprintf("stage fullScreen changed: " + newValue);
					}
				});
				// stage.setMaximized(true);
				stage.setOnCloseRequest(model_main.dontExit);
				// ScenicView.show(parent);
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						stage.setScene(scene);
						stage.show();
						model_main.getBottomController().initBottomWorkdirMonitors();
					}
				});

				// stage.show();
				return null;
			}

		};
		LoadingProcess_Task lpt = new LoadingProcess_Task();

		task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

			@Override
			public void handle(WorkerStateEvent event) {
				/*
				 * TODO 1. turhat duplicated jo tableviewissä pois ensin sorted ylhäältä alas
				 * etsien sortit ja jos on sortit duplicoitu niin skippaa haussa. sen jälkeen
				 * sortit ylhäältä alas 2. sen jälkeen sortit järjestyksessä ylhäältä alas
				 * etsitään sopivaa kansiota workdirrin päivyksistä TÄMÄ TOIMII!!UIIIJJJUIIII!!!
				 */
				Messages.sprintf("main succeeded");
				scene_Switcher.setWindow(stage);
				scene_Switcher.setScene_main(scene);

				Misc.checkOS();
				conf.loadConfig_GUI();
				model_main.getSelectedFolders().load_SelectedFolders_UsingSQL(model_main);
				Connection connection = SqliteConnection.connector(Main.conf.getAppDataPath(),
						Main.conf.getFolderInfo_db_fileName());
				if (SQL_Utils.isDbConnected(connection)) {
					load_FileInfosBackToTableViews = new Load_FileInfosBackToTableViews(model_main, connection);
					load_FileInfosBackToTableViews.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
						@Override
						public void handle(WorkerStateEvent event) {
							lpt.closeStage();
							model_main.tables().registerTableView_obs_listener();
							stage.setOnCloseRequest(model_main.exitProgram);
							try {
								if (!load_FileInfosBackToTableViews.get()) {
									Messages.warningText("Can't load previous session!");
								}

							} catch (Exception e) {
								e.printStackTrace();
							}
							Messages.sprintf("load_FileInfosBackToTableViews succeeded");
							model_main.getRegisterTableActivity().restart();
							boolean loaded = model_main.getWorkDir_Handler()
									.loadAllLists(Paths.get(Main.conf.getWorkDir()));
							if (loaded) {

								for (FileInfo finfo : model_main.getWorkDir_Handler().getWorkDir_List()) {
									Messages.sprintf("fileInfo loading: " + finfo.getOrgPath());
								}
								Messages.sprintf("==============Loading workdir size is: "
										+ model_main.getWorkDir_Handler().getWorkDir_List().size());
							}
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
							stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
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
							stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
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
		task.setOnFailed(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				Messages.sprintf("Main Task failed");
				lpt.closeStage();
			}
		});
		task.setOnCancelled(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				Messages.sprintf("Main Task cancelled");
				lpt.closeStage();
			}
		});
		// lp.showLoadStage();
		Thread thread = new Thread(task, "Main Thread");
		thread.start();
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

	public static Stage getMain_stage() {
		return main_stage;
	}

	public static void setMain_stage(Stage main_stage) {
		Main.main_stage = main_stage;
	}

	@Override
	public void stop() throws Exception {
		super.stop();
		model_main.getRegisterTableActivity().cancel();
	}

}
