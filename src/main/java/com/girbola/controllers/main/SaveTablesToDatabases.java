package com.girbola.controllers.main;

import java.sql.Connection;

import com.girbola.Main;
import com.girbola.controllers.loading.LoadingProcess_Task;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.sql.SQL_Utils;
import com.girbola.sql.SqliteConnection;

import javafx.concurrent.Task;
import javafx.stage.Stage;

public class SaveTablesToDatabases extends Task<Integer> {

	private final String ERROR = SaveTablesToDatabases.class.getName();

	private Stage stage;
	private LoadingProcess_Task loadingProcess_Task;
	private boolean closeLoadingStage;
	private Model_main model_main;

	public SaveTablesToDatabases(Model_main model_main, Stage stage, LoadingProcess_Task loadingProcess_Task,
			boolean closeLoadingStage) {
		super();
		this.model_main = model_main;
		this.stage = stage;
		if (loadingProcess_Task == null) {
			loadingProcess_Task = new LoadingProcess_Task(stage);
			loadingProcess_Task.setTask(this);
		}
		this.loadingProcess_Task = loadingProcess_Task;
		this.closeLoadingStage = closeLoadingStage;
	}

	@Override
	protected Integer call() throws Exception {

		Connection connection = SqliteConnection.connector(Main.conf.getAppDataPath(),
				Main.conf.getFolderInfo_db_fileName()); // folderInfo.db
		
		SQL_Utils.createFolderInfoDatabase(connection); // create new folderinfodatabase folderInfo.db
		SQL_Utils.clearTable(connection, SQL_Enums.FOLDERINFO.getType()); // clear table folderInfo.db
		
		if (connection == null) {
			Messages.errorSmth(ERROR, "createFolderInfoDatabase failed!", new Exception("Saving folderinfo's failed!"),
					Misc.getLineNumber(), true);
		}
		long start = System.currentTimeMillis();
		updateMessage("Loading Sorted");
		boolean sorted = model_main.saveTableContent(connection, model_main.tables().getSorted_table().getItems(),
				TableType.SORTED.getType());
		if (sorted) {
			Messages.sprintf("sorted were saved successfully took: " + (System.currentTimeMillis() - start));
		}
		start = System.currentTimeMillis();
		updateMessage("Loading SortIt");
		boolean sortit = model_main.saveTableContent(connection, model_main.tables().getSortIt_table().getItems(),
				TableType.SORTIT.getType());
		if (sortit) {
			Messages.sprintf("sortit were saved successfully took: " + (System.currentTimeMillis() - start));
		}
		start = System.currentTimeMillis();
		updateMessage("Loading AsItIs");
		boolean asitis = model_main.saveTableContent(connection, model_main.tables().getAsItIs_table().getItems(),
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
		}
		return null;
	}


	@Override
	protected void succeeded() {
		super.succeeded();
		Messages.sprintf("Saving succeeded");
		if (closeLoadingStage) {
			loadingProcess_Task.closeStage();
		}
		Main.setChanged(false);
	}

	@Override
	protected void cancelled() {
		super.cancelled();
		if (closeLoadingStage) {
			loadingProcess_Task.closeStage();
		}
	}

	@Override
	protected void failed() {
		super.failed();
		if (closeLoadingStage) {
			loadingProcess_Task.closeStage();
		}
	}

}
