package com.girbola.controllers.main;

import com.girbola.Main;
import com.girbola.controllers.main.sql.ConfigurationSQLHandler;
import com.girbola.controllers.loading.LoadingProcessTask;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.messages.Messages;
import com.girbola.sql.SQL_Utils;
import javafx.concurrent.Task;
import javafx.stage.Stage;

public class WriteTablesFolderInfoToConfigurationDatabase extends Task<Integer> {

    private final String ERROR = WriteTablesFolderInfoToConfigurationDatabase.class.getName();

    private Stage stage;
    private LoadingProcessTask loadingProcess_Task;
    private boolean closeLoadingStage;
    private ModelMain model_main;


    public WriteTablesFolderInfoToConfigurationDatabase(ModelMain model_main, Stage stage, LoadingProcessTask loadingProcess_Task,
                                                        boolean closeLoadingStage) {
        this.model_main = model_main;
        this.stage = stage;
        this.loadingProcess_Task = loadingProcess_Task;
        this.closeLoadingStage = closeLoadingStage;
    }

    /**
     * Initializes LoadingProcessTask if it was null at construction time.
     * This should be called before attempting to use loadingProcess_Task.
     */
    private void initializeLoadingProcessTaskIfNeeded() {
        if (this.loadingProcess_Task == null && this.stage != null) {
            Messages.sprintf("Initializing LoadingProcessTask - it was NULL at constructor");
            this.loadingProcess_Task = new LoadingProcessTask(stage);
            this.loadingProcess_Task.setTask(this);
        }
    }

    @Override
    protected Integer call() throws Exception {
        try {

            if (!SQL_Utils.isDbConnected(ConfigurationSQLHandler.getConnection())) {
                Messages.sprintfError("Cannot get connected with database at: " + ConfigurationSQLHandler.getConnection().getMetaData().getURL());
                cancel();
                return null;
            }

//        SQL_Utils.clearTable(ConfigurationSQLHandler.getConnection(), SQLTableEnums.SAVED_FOLDERS.getType());

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
            //SQL_Utils.commitChanges(ConfigurationSQLHandler.getConnection());
        } catch (Exception e) {
            Messages.sprintfError("Error saving tables to databases: " + e.getMessage());
        } finally {
            SQL_Utils.commitChanges(ConfigurationSQLHandler.getConnection());
            SQL_Utils.closeConnection(ConfigurationSQLHandler.getConnection());
        }


        return null;
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        Messages.sprintf("WriteTablesFolderInfoToConfigurationDatabase Saving succeeded");
        if (closeLoadingStage) {
            initializeLoadingProcessTaskIfNeeded();
            if (loadingProcess_Task != null) {
                Messages.sprintf("WriteTablesFolderInfoToConfigurationDatabase CloseLoadingStage");
                loadingProcess_Task.closeStage();
            } else {
                Messages.sprintfError("Could not initialize loadingProcess_Task in succeeded()");
            }
        }
        Main.setChanged(false);
    }

    @Override
    protected void cancelled() {
        super.cancelled();
        if (closeLoadingStage) {
            initializeLoadingProcessTaskIfNeeded();
            if (loadingProcess_Task != null) {
                Messages.sprintf("WriteTablesFolderInfoToConfigurationDatabase CloseLoadingStage");
                loadingProcess_Task.closeStage();
            } else {
                Messages.sprintfError("Could not initialize loadingProcess_Task in cancelled()");
            }
        }
    }

    @Override
    protected void failed() {
        super.failed();
        if (closeLoadingStage) {
            initializeLoadingProcessTaskIfNeeded();
            if (loadingProcess_Task != null) {
                Messages.sprintf("WriteTablesFolderInfoToConfigurationDatabase CloseLoadingStage");
                loadingProcess_Task.closeStage();
            } else {
                Messages.sprintfError("Could not initialize loadingProcess_Task in failed()");
            }
        }
    }

}
