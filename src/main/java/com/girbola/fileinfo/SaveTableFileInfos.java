package com.girbola.fileinfo;

import com.girbola.Main;
import com.girbola.controllers.main.sql.ConfigurationSQLHandler;
import com.girbola.controllers.loading.LoadingProcessTask;
import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.SaveTablesToFolderInfoDatabases;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.controllers.main.tables.model.StoredFolderInfoStatus;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.messages.Messages;
import com.girbola.sql.SavedFolderInfosSQL;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class SaveTableFileInfos {

    private final String ERROR = SaveTablesToFolderInfoDatabases.class.getName();

    private Stage stage;
    private LoadingProcessTask loadingProcess_Task;
    private boolean closeLoadingStage;
    private ModelMain model_main;

    public SaveTableFileInfos(ModelMain modelMain, Stage stage, LoadingProcessTask loadingProcess_Task, boolean closeLoadingStage) {
        this.model_main = modelMain;
        this.stage = stage;

        this.loadingProcess_Task = loadingProcess_Task;
        this.closeLoadingStage = closeLoadingStage;
    }

    public void readTables() {
        boolean sortedTable = iterateTable(model_main.tables().getTableByType(TableType.SORTED.getType()));
        if(!sortedTable) {
            Messages.sprintfError(TableType.SORTED.getType() + " " + Main.bundle.getString("cannotSaveStatus"));
        }
        boolean sortItTable = iterateTable(model_main.tables().getTableByType(TableType.SORTIT.getType()));
        if(!sortItTable) {
            Messages.sprintfError(TableType.SORTIT.getType() + " " + Main.bundle.getString("cannotSaveStatus"));
        }
        boolean asItIsTable = iterateTable(model_main.tables().getTableByType(TableType.ASITIS.getType()));
        if(!asItIsTable) {
            Messages.sprintfError(TableType.ASITIS.getType() + " " + Main.bundle.getString("cannotSaveStatus"));
        }
    }

    private boolean iterateTable(TableView<FolderInfo> table) {

        for(FolderInfo folderInfo : table.getItems()) {
            try {
                StoredFolderInfoStatus storedFolderInfoStatus = new StoredFolderInfoStatus(folderInfo.getFolderPath(), folderInfo.getTableType(), folderInfo.getJustFolderName(), folderInfo.isConnected());
                SavedFolderInfosSQL.insertSavedFolderInfoToDatabase(ConfigurationSQLHandler.getConnection(), storedFolderInfoStatus);

            }catch (Exception e) {
                Messages.sprintfError(Main.bundle.getString("cannotSaveStatus"));
                return false;
            }
        }
        return true;
    }
}
