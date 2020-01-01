/*
 @(#)Copyright:  Copyright (c) 2012-2019 All right reserved.
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.main;

import static com.girbola.Main.bundle;
import static com.girbola.Main.conf;
import static com.girbola.messages.Messages.sprintf;

import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.girbola.Main;
import com.girbola.concurrency.ConcurrencyUtils;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.dialogs.Dialogs;
import com.girbola.fileinfo.ThumbInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.sql.SQL_Utils;
import com.girbola.sql.SqliteConnection;
import com.girbola.workdir.WorkDir_Handler;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
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
	private WorkDir_Handler workDir_Handler = new WorkDir_Handler();
	private Stage main_stage;

	private AnchorPane main_container;
	private VBox main_vbox;
	private Buttons buttons;
	private Populate populate;
	private Menu menu;
	private Scene scene;
	private SelectedFolderScanner selectedFolders;
	private TablePositionHolder tablePositionHolder;
	private List<ThumbInfo> thumbInfo = new ArrayList<>();
	private ScheduledService<Void> registerTableActivity;

	public Model_main() {
		sprintf("Model instantiated...");

		tables = new Tables(this);

		buttons = new Buttons(this);

		menu = new Menu(this);

		selectedFolders = new SelectedFolderScanner();

		populate = new Populate(this);

		tablePositionHolder = new TablePositionHolder(this);

		registerTableActivity = new MonitorConnectivity(this);
		registerTableActivity.setPeriod(Duration.seconds(15));
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

	public Menu menu() {
		return this.menu;
	}
	//
	// public Tables tables() {
	// return this.tables;
	// }

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

	public boolean save() {
		Messages.sprintf("save started");
		if (tables() == null) {
			Messages.warningText("model.getTables() were null!");
		}
		Connection connection = SqliteConnection.connector(Main.conf.getAppDataPath(),
				Main.conf.getFolderInfo_db_fileName()); // folderInfo.db
		SQL_Utils.clearTable(connection, SQL_Enums.FOLDERINFO.getType()); // clear table folderInfo.db
		SQL_Utils.createFolderInfoDatabase(connection); // create new folderinfodatabase folderInfo.db
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
				connection.close();
			}
		} catch (Exception e) {
			Main.setChanged(true);
			return false;
		}
		Main.setChanged(false);
		return true;
	}

	private boolean saveTableContent(Connection connection, ObservableList<FolderInfo> items, String tableType) {
		if (items.isEmpty()) {
			return false;
		}
		if (connection == null) {
			Messages.sprintfError("saveTablecontent connection error!!!");
			return false;
		}
		Connection fileList_connection = null;
		for (FolderInfo folderInfo : items) {
			folderInfo.setTableType(tableType);
			try {
				SQL_Utils.addToFolderInfoDB(connection, folderInfo); // Adds FolderInfo into database folderInfo.db.
																		// FolderPath, TableType and Connection status
																		// when this was saved
				// Connects to current folder for existing or creates new one called fileinfo.db
				fileList_connection = SqliteConnection.connector(Paths.get(folderInfo.getFolderPath()),
						Main.conf.getFileInfo_db_fileName());
				// Inserts all data info fileinfo.db
				SQL_Utils.insertFileInfoListToDatabase(fileList_connection, folderInfo.getFileInfoList());
				if (fileList_connection != null) {
					fileList_connection.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
				Messages.sprintfError(
						"Something went wrong writing folderinfo to database at line: " + Misc.getLineNumber());
				return false;
			}
		}
		return true;
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
		// TODO laita savetus daemon p��lle jottei tallennukset keskeytyisi
		// Platform.setImplicitExit(false)
		// Misc_GUI.saveObject(TablePositionHolder, folder);
		// saveTablePositions();
		// erg;
		Messages.sprintf("Exiting program");
//		Configuration_SQL_Utils.update_Configuration();

		if (Main.getChanged()) {
			Dialog<ButtonType> dialog = Dialogs.createDialog_YesNoCancel(bundle.getString("saveBeforeExit"));

			Optional<ButtonType> result = dialog.showAndWait();
			if (result.get().getButtonData().equals(ButtonBar.ButtonData.YES)) {
				save();
				getRegisterTableActivity().cancel();
			} else if (result.get().getButtonData().equals(ButtonBar.ButtonData.CANCEL_CLOSE)) {

			}
		}
		if (conf.isConfirmOnExit()) {
			sprintf("isShowOnExit was true");
			ConcurrencyUtils.stopExecThreadNow();
			// save();
			getRegisterTableActivity().cancel();
			Platform.exit();
		} else {
			sprintf("isShowOnExit was false");
			ConcurrencyUtils.stopExecThreadNow();
			// save();
			getRegisterTableActivity().cancel();
			Platform.exit();
		}

	}

	/**
	 * @return the workDir_Handler
	 */
	public WorkDir_Handler getWorkDir_Handler() {
		return this.workDir_Handler;
	}

	public ScheduledService<Void> getRegisterTableActivity() {
		return registerTableActivity;
	}

	private BottomController bottomController;

	public void setBottomController(BottomController bottomController) {
		this.bottomController = bottomController;

	}

	public BottomController getBottomController() {
		return this.bottomController;
	}

}
