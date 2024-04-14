package com.girbola.controllers.main;

import com.girbola.Main;
import com.girbola.controllers.loading.LoadingProcessTask;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.sql.SQL_Utils;
import com.girbola.sql.SqliteConnection;
import javafx.concurrent.Task;
import javafx.stage.Stage;

import java.sql.Connection;

public class SaveTablesToDatabases extends Task<Integer> {

	private final String ERROR = SaveTablesToDatabases.class.getName();

	private Stage stage;
	private LoadingProcessTask loadingProcess_Task;
	private boolean closeLoadingStage;
	private Model_main model_main;

	public SaveTablesToDatabases(Model_main model_main, Stage stage, LoadingProcessTask loadingProcess_Task,
			boolean closeLoadingStage) {
		super();
		this.model_main = model_main;
		this.stage = stage;
		if (loadingProcess_Task == null) {
			loadingProcess_Task = new LoadingProcessTask(stage);
			loadingProcess_Task.setTask(this);
		}
		this.loadingProcess_Task = loadingProcess_Task;
		this.closeLoadingStage = closeLoadingStage;
	}

	@Override
	protected Integer call() throws Exception {

        Connection connection_Configuration = SqliteConnection.connector(Main.conf.getAppDataPath(),
                Main.conf.getConfiguration_db_fileName()); // folderState.db
        if (connection_Configuration == null) {
            Messages.errorSmth(ERROR, "createFolderInfoDatabase failed!", new Exception("Saving folderinfo's failed!"),
                    Misc.getLineNumber(), true);
            cancel();
            Messages.sprintfError("Can't connect configutation file: " + Main.conf.getConfiguration_db_fileName());
            return null;
        }
        try {
            connection_Configuration.setAutoCommit(false);
        } catch (Exception e) {
            e.printStackTrace();
            cancel();
            return null;
        }
        SQL_Utils.clearTable(connection_Configuration, SQL_Enums.FOLDERSSTATE.getType());
        SQL_Utils.createFoldersStatesDatabase(connection_Configuration); // create new foldersStateDatabase
        if(!SQL_Utils.isDbConnected(connection_Configuration)) {
            Messages.errorSmth(ERROR, "Connection were closed!", new Exception("Connection were closed"),
                    Misc.getLineNumber(), true);

        }
//		SQL_Utils.clearTable(connection_Configuration, SQL_Enums.FOLDERSSTATE.getType()); // clear table folderState.db

        long start = System.currentTimeMillis();
        updateMessage("Loading Sorted");
        boolean sorted = model_main.saveTableContent(connection_Configuration,
                model_main.tables().getSorted_table().getItems(), TableType.SORTED.getType());
        if (sorted) {
            Messages.sprintf("sorted were saved successfully took: " + (System.currentTimeMillis() - start));
        }
        start = System.currentTimeMillis();
        updateMessage("Loading SortIt");
        boolean sortit = model_main.saveTableContent(connection_Configuration,
                model_main.tables().getSortIt_table().getItems(), TableType.SORTIT.getType());
        if (sortit) {
            Messages.sprintf("sortit were saved successfully took: " + (System.currentTimeMillis() - start));
        }
        start = System.currentTimeMillis();
        updateMessage("Loading AsItIs");
        boolean asitis = model_main.saveTableContent(connection_Configuration,
                model_main.tables().getAsItIs_table().getItems(), TableType.ASITIS.getType());
        if (asitis) {
            Messages.sprintf("asitis were saved successfully took: " + (System.currentTimeMillis() - start));
        }
        SQL_Utils.closeConnection(connection_Configuration);

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
