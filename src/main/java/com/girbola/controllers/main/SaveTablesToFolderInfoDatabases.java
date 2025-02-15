package com.girbola.controllers.main;

import com.girbola.Main;
import com.girbola.configuration.Configuration_SQL_Utils;
import com.girbola.controllers.loading.LoadingProcessTask;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.sql.SQL_Utils;
import com.girbola.sql.SqliteConnection;
import javafx.concurrent.Task;
import javafx.stage.Stage;

import java.sql.Connection;

public class SaveTablesToFolderInfoDatabases extends Task<Integer> {

	private final String ERROR = SaveTablesToFolderInfoDatabases.class.getName();

	private Stage stage;
	private LoadingProcessTask loadingProcess_Task;
	private boolean closeLoadingStage;
	private ModelMain model_main;

	public SaveTablesToFolderInfoDatabases(ModelMain model_main, Stage stage, LoadingProcessTask loadingProcess_Task,
                                           boolean closeLoadingStage) {
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

        Connection connectionConfiguration = SqliteConnection.connector(Main.conf.getAppDataPath(), Main.conf.getConfiguration_db_fileName());
        if (connectionConfiguration == null) {
            Messages.sprintfError("Can't connect configuration file: " + Main.conf.getConfiguration_db_fileName());
            Messages.errorSmth(ERROR, "createFolderInfoDatabase failed!", new Exception("Saving FolderInfos failed!"),
                    Misc.getLineNumber(), true);
            cancel();
            return null;
        }
        try {
            connectionConfiguration.setAutoCommit(false);
        } catch (Exception e) {
            e.printStackTrace();
            cancel();
            return null;
        }
        SQL_Utils.clearTable(connectionConfiguration, SQL_Enums.FOLDERINFOS.getType());
        Configuration_SQL_Utils.createFolderInfosDatabase(connectionConfiguration); // create new FoldersInfos table
        if(!SQL_Utils.isDbConnected(connectionConfiguration)) {
            Messages.errorSmth(ERROR, "Connection were closed!", new Exception("Connection were closed"),
                    Misc.getLineNumber(), true);

        }

        long start = System.currentTimeMillis();
        updateMessage("Loading Sorted");
        boolean sorted = model_main.saveTableContent(connectionConfiguration,
                model_main.tables().getSorted_table().getItems(), TableType.SORTED.getType());
        if (sorted) {
            Messages.sprintf("sorted were saved successfully took: " + (System.currentTimeMillis() - start));
        }

        start = System.currentTimeMillis();
        updateMessage("Loading SortIt");
        boolean sortit = model_main.saveTableContent(connectionConfiguration,
                model_main.tables().getSortIt_table().getItems(), TableType.SORTIT.getType());
        if (sortit) {
            Messages.sprintf("sortit were saved successfully took: " + (System.currentTimeMillis() - start));
        }

        start = System.currentTimeMillis();
        updateMessage("Loading AsItIs");
        boolean asitis = model_main.saveTableContent(connectionConfiguration,
                model_main.tables().getAsItIs_table().getItems(), TableType.ASITIS.getType());
        if (asitis) {
            Messages.sprintf("asitis were saved successfully took: " + (System.currentTimeMillis() - start));
        }
        SQL_Utils.closeConnection(connectionConfiguration);

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
