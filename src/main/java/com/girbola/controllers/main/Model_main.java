/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.main;

import static com.girbola.Main.bundle;
import static com.girbola.Main.conf;
import static com.girbola.messages.Messages.sprintf;

import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.girbola.Load_FileInfosBackToTableViews;
import com.girbola.Main;
import com.girbola.concurrency.ConcurrencyUtils;
import com.girbola.configuration.Configuration_SQL_Utils;
import com.girbola.controllers.loading.LoadingProcess_Task;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.dialogs.Dialogs;
import com.girbola.fileinfo.ThumbInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.sql.FileInfo_SQL;
import com.girbola.sql.FolderInfo_SQL;
import com.girbola.sql.FolderState;
import com.girbola.sql.SQL_Utils;
import com.girbola.sql.SqliteConnection;
import com.girbola.workdir.WorkDirHandler;

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

/**
 *
 * @author Marko Lokka
 */
public class Model_main {

	private final String ERROR = Model_main.class.getSimpleName();

	private Tables tables;
	private WorkDirHandler workDirHandler = new WorkDirHandler();

	private StringProperty table_root_hbox_width = new SimpleStringProperty();

	public StringProperty getTable_root_hbox_width() {
		return table_root_hbox_width;
	}

	public void setTable_root_hbox_width(String table_root_hbox_width) {
		this.table_root_hbox_width.set(table_root_hbox_width);
	}

	private Stage main_stage;

	private AnchorPane main_container;
	private VBox main_vbox;
	private Buttons buttons;
	private Populate populate;
	private Scene scene;
	private SelectedFolderScanner selectedFolders;
	private TablePositionHolder tablePositionHolder;
	private List<ThumbInfo> thumbInfo = new ArrayList<>();
	private ScheduledService<Void> monitorExternalDriveConnectivity;

	private TableStatistic sortitTableStatistic;
	private TableStatistic sortedTableStatistic;
	private TableStatistic asitisTableStatistic;

	private SimpleDoubleProperty sortitTableWidth_prop;
	private SimpleDoubleProperty sortedTableWidth_prop;
	private SimpleDoubleProperty asitisTableWidth_prop;

	public TableStatistic getSortitTableStatistic() {
		return sortitTableStatistic;
	}

	public void setSortitTableStatistic(TableStatistic sortitTableStatistic) {
		this.sortitTableStatistic = sortitTableStatistic;
	}

	public TableStatistic getSortedTableStatistic() {
		return sortedTableStatistic;
	}

	public void setSortedTableStatistic(TableStatistic sortedTableStatistic) {
		this.sortedTableStatistic = sortedTableStatistic;
	}

	public TableStatistic getAsitisTableStatistic() {
		return asitisTableStatistic;
	}

	public void setAsitisTableStatistic(TableStatistic asitisTableStatistic) {
		this.asitisTableStatistic = asitisTableStatistic;
	}

	public Model_main() {
		sprintf("Model instantiated...");

		tables = new Tables(this);

		buttons = new Buttons(this);

		selectedFolders = new SelectedFolderScanner();

		populate = new Populate(this);

		tablePositionHolder = new TablePositionHolder(this);

		monitorExternalDriveConnectivity = new MonitorExternalDriveConnectivity(this);
		monitorExternalDriveConnectivity.setPeriod(Duration.seconds(15));
		tables.init();

		sortitTableWidth_prop = new SimpleDoubleProperty(0);
		sortedTableWidth_prop = new SimpleDoubleProperty(0);
		asitisTableWidth_prop = new SimpleDoubleProperty(0);

//		tables.getSorted_table().widthProperty();
	}

	public final List<ThumbInfo> getThumbInfo() {
		return thumbInfo;
	}

	public final void setThumbInfo(List<ThumbInfo> thumbInfo) {
		this.thumbInfo = thumbInfo;
	}

	public TablePositionHolder getTablePositionHolder() {
		return this.tablePositionHolder;
	}

	public SelectedFolderScanner getSelectedFolders() {
		return selectedFolders;
	}

	public Buttons buttons() {
		return this.buttons;
	}

	void setMainContainer(AnchorPane main_container) {
		this.main_container = main_container;
	}

	void setMainVBox(VBox main_vbox) {
		this.main_vbox = main_vbox;
	}

	public Tables tables() {
		return this.tables;
	}

	public Populate populate() {
		return this.populate;
	}

	public boolean saveAllTableContents() {
		Messages.sprintf("save started");
		if (tables() == null) {
			Messages.warningText("model.getTables() were null!");
		}
		Connection connection = SqliteConnection.connector(Main.conf.getAppDataPath(),
				Main.conf.getConfiguration_db_fileName()); // folderInfo.db
		try {
			connection.setAutoCommit(false);
		} catch (Exception e) {
			Messages.sprintfError("Can't change to autocommit: " + e.getMessage());
			e.printStackTrace();
		}

		SQL_Utils.clearTable(connection, SQL_Enums.FOLDERSSTATE.getType()); // clear table folderInfo.db
		SQL_Utils.createFoldersStatesDatabase(connection); // create new folderinfodatabase folderInfo.db
		if (connection == null) {
			Messages.errorSmth(ERROR, "createFolderInfoDatabase failed!", new Exception("Saving folderinfo's failed!"),
					Misc.getLineNumber(), true);
		}
		long start = System.currentTimeMillis();
		boolean sorted = saveTableContent(connection, tables().getSorted_table().getItems(),
				TableType.SORTED.getType());
		if (sorted) {
			Messages.sprintf("sorted were saved successfully took: " + (System.currentTimeMillis() - start));
		}
		start = System.currentTimeMillis();
		boolean sortit = saveTableContent(connection, tables().getSortIt_table().getItems(),
				TableType.SORTIT.getType());
		if (sortit) {
			Messages.sprintf("sortit were saved successfully took: " + (System.currentTimeMillis() - start));
		}
		start = System.currentTimeMillis();
		boolean asitis = saveTableContent(connection, tables().getAsItIs_table().getItems(),
				TableType.ASITIS.getType());
		if (asitis) {
			Messages.sprintf("asitis were saved successfully took: " + (System.currentTimeMillis() - start));
		}

		try {
			if (connection != null) {
				connection.commit();
				connection.close();
				Main.setChanged(false);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Main.setChanged(true);
			return false;
		}
		Main.setChanged(false);
		return true;
	}

	public boolean saveTableContent(Connection connection_Configuration, ObservableList<FolderInfo> items,
			String tableType) {
		if (items.isEmpty()) {
			Messages.sprintfError("saveTableContent items list were empty. tabletype: " + tableType);
			return false;
		}

		Connection fileList_connection = null;
		for (FolderInfo folderInfo : items) {
			if (folderInfo.getFolderFiles() >= 1) {
				Messages.sprintf("saveTableContent folderInfo: " + folderInfo.getFolderPath());
				folderInfo.setTableType(tableType);
				try {
					FolderState folderState = new FolderState(folderInfo.getFolderPath(), tableType,
							folderInfo.getJustFolderName(), folderInfo.isConnected());

					boolean addingToFolderState = SQL_Utils.addToFolderStateDB(connection_Configuration, folderState);
					if (!addingToFolderState) {
						Messages.sprintfError("Something went wrong with adding folderinfo configuration file");
					}
					/*
					 * Adds FolderInfo into table folderInfo.db. Stores: FolderPath, TableType and
					 * Connection status when this was saved Connects to current folder for existing
					 * or creates new one called fileinfo.db
					 */
					fileList_connection = SqliteConnection.connector(Paths.get(folderInfo.getFolderPath()),
							Main.conf.getMdir_db_fileName());
					fileList_connection.setAutoCommit(false);
					// Inserts all data info fileinfo.db
					FileInfo_SQL.insertFileInfoListToDatabase(fileList_connection, folderInfo.getFileInfoList(), false);
					FolderInfo_SQL.saveFolderInfoToTable(fileList_connection, folderInfo);
					if (fileList_connection != null) {
						fileList_connection.commit();
						fileList_connection.close();
						Messages.sprintf("saveTableContent folderInfo: " + folderInfo.getFolderPath()
								+ " DONE! Closing connection");
					} else {
						Messages.sprintfError("ERROR with saveTableContent folderInfo: " + folderInfo.getFolderPath()
								+ " FAILED! Closing connection");
					}
				} catch (Exception e) {
					e.printStackTrace();
					Messages.sprintfError("Something went wrong writing folderinfo to database at line: "
							+ Misc.getLineNumber() + " folderInfo path was: " + folderInfo.getFolderPath());
					return false;
				}

			}
		}
		try {
			Messages.sprintf("foldersStateConnection.commit() " + " foldersStateConnection.close();");
			connection_Configuration.commit();
			return true;
		} catch (Exception e) {
			Messages.errorSmth(ERROR, "Cannot commit to SQL database", e, Misc.getLineNumber(), true);
			e.printStackTrace();
			return false;
		}
	}

	public void exitProgram_NOSAVE() {
		ConcurrencyUtils.stopExecThreadNow();
		Platform.exit();
	}

	public EventHandler<WindowEvent> exitProgram = new EventHandler<WindowEvent>() {
		@Override
		public void handle(WindowEvent event) {
			Messages.sprintf("exitProgram");
			exitProgram();
			Messages.sprintf("exitProgram ended");
		}
	};
	// model_main.getSelection_FolderScanner().save_Selection_FolderScannerDataTo_File(model_main.getSelection_FolderScanner().getSelectedFolderScanner_list());

	public EventHandler<WindowEvent> dontExit = new EventHandler<WindowEvent>() {
		@Override
		public void handle(WindowEvent event) {
			event.consume();
		}

	};

	public void exitProgram() {
		sprintf("exitProgram()");
		// TODO laita savetus daemon päälle jottei tallennukset keskeytyisi
		// Platform.setImplicitExit(false)
		// Misc_GUI.saveObject(TablePositionHolder, folder);
		// saveTablePositions();
		// erg;
		Messages.sprintf("Exiting program");
		Configuration_SQL_Utils.update_Configuration();

		if (Main.getChanged()) {
			Dialog<ButtonType> dialog = Dialogs.createDialog_YesNoCancel(Main.scene_Switcher.getWindow(),
					bundle.getString("saveBeforeExit"));
			Messages.sprintf("dialog changesDialog width: " + dialog.getWidth());
			Iterator<ButtonType> iterator = dialog.getDialogPane().getButtonTypes().iterator();
			while(iterator.hasNext()) {
				ButtonType btn = iterator.next();
				Messages.sprintf("BTNNTTNNT: " + btn.getText());
			}
			Optional<ButtonType> result = dialog.showAndWait();
			if (result.get().getButtonData().equals(ButtonBar.ButtonData.YES)) {
				saveAllTableContents();
				getMonitorExternalDriveConnectivity().cancel();
			} else if (result.get().getButtonData().equals(ButtonBar.ButtonData.NO)) {
				Messages.sprintf("No answered. This is not finished!");
			} else if (result.get().getButtonData().equals(ButtonBar.ButtonData.CANCEL_CLOSE)) {
				Messages.sprintf("Cancel pressed. This is not finished!");
				return;
			}
		}
		if (conf.isConfirmOnExit()) {
			sprintf("isShowOnExit was true");
			ConcurrencyUtils.stopExecThreadNow();
			// save();
			getMonitorExternalDriveConnectivity().cancel();
			Platform.exit();
		} else {
			sprintf("isShowOnExit was false");
			ConcurrencyUtils.stopExecThreadNow();
			// save();
			getMonitorExternalDriveConnectivity().cancel();
			Platform.exit();
		}

	}

	/**
	 * @return the workDir_Handler
	 */
	public WorkDirHandler getWorkDir_Handler() {
		return this.workDirHandler;
	}

	public ScheduledService<Void> getMonitorExternalDriveConnectivity() {
		return monitorExternalDriveConnectivity;
	}

	private BottomController bottomController;

	public void setBottomController(BottomController bottomController) {
		this.bottomController = bottomController;

	}

	public BottomController getBottomController() {
		return this.bottomController;
	}

	public void load() {
		Connection connection = SqliteConnection.connector(Main.conf.getAppDataPath(),
				Main.conf.getConfiguration_db_fileName());
		if (SQL_Utils.isDbConnected(connection)) {
			TableUtils.clearTablesContents(tables());
			Load_FileInfosBackToTableViews load_FileInfosBackToTableViews = new com.girbola.Load_FileInfosBackToTableViews(
					this, connection);

			Thread load_thread = new Thread(load_FileInfosBackToTableViews, "Loading folderinfos Thread");
			load_thread.start();
		} else {
			Messages.sprintf("Can't load folderinfos back to tables because the database were not connected");
		}
	}

	public void saveTablesToDatabases_(Stage stage, LoadingProcess_Task loadingProcess_Task,
			boolean closeLoadingStage) {
		Task<Void> task = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				saveAllTableContents();
				return null;
			}
		};
		if (loadingProcess_Task == null) {
			loadingProcess_Task = new LoadingProcess_Task(stage);
		}

		task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				Messages.sprintf("Database were successfully saved");
				if (closeLoadingStage) {
					// loadingProcess_Task.closeStage();
				}
			}
		});

		task.setOnCancelled(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				Messages.sprintfError("Saving database has been cancelled");
//				lpt.closeStage();
			}
		});
		task.setOnFailed(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				Messages.sprintfError("Saving database has been failed");
//				lpt.closeStage();
			}
		});

//		lpt.setTask(task);
		Thread thread = new Thread(task, "Saving Thread");
		thread.start();

	}

}
