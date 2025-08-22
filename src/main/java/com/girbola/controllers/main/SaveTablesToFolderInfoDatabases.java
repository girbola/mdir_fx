package com.girbola.controllers.main;

import com.girbola.Main;
import com.girbola.controllers.main.sql.ConfigurationSQLHandler;
import com.girbola.controllers.loading.LoadingProcessTask;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.messages.Messages;
import com.girbola.sql.SQL_Utils;
import javafx.concurrent.Task;
import javafx.stage.Stage;

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

        boolean dbConnected = SQL_Utils.isDbConnected(ConfigurationSQLHandler.getConnection());
        if (!dbConnected) {
            Messages.sprintf("Cannot get connected with database at: " + ConfigurationSQLHandler.getConnection().getMetaData().getURL());
        }

        SQL_Utils.clearTable(ConfigurationSQLHandler.getConnection(), SQLTableEnums.FOLDERINFOS.getType());

        long start = System.currentTimeMillis();
        updateMessage("Loading Sorted");
        boolean sorted = model_main.saveTableContent(ConfigurationSQLHandler.getConnection(), model_main.tables().getSorted_table().getItems(), TableType.SORTED.getType());
        if (sorted) {
            Messages.sprintf("sorted were saved successfully took: " + (System.currentTimeMillis() - start));
        }

        start = System.currentTimeMillis();
        updateMessage("Loading SortIt");
        boolean sortit = model_main.saveTableContent(ConfigurationSQLHandler.getConnection(), model_main.tables().getSortIt_table().getItems(), TableType.SORTIT.getType());
        if (sortit) {
            Messages.sprintf("sortit were saved successfully took: " + (System.currentTimeMillis() - start));
        }

        start = System.currentTimeMillis();
        updateMessage("Loading AsItIs");
        boolean asitis = model_main.saveTableContent(ConfigurationSQLHandler.getConnection(), model_main.tables().getAsItIs_table().getItems(), TableType.ASITIS.getType());
        if (asitis) {
            Messages.sprintf("asitis were saved successfully took: " + (System.currentTimeMillis() - start));
        }
        SQL_Utils.commitChanges(ConfigurationSQLHandler.getConnection());
        //SQL_Utils.closeConnection(ConfigurationSQLHandler.getConnection());

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
