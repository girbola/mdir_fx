package com.girbola.fileinfo;

import com.girbola.Main;
import com.girbola.controllers.main.sql.ConfigurationSQLHandler;
import com.girbola.controllers.loading.LoadingProcessTask;
import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.SaveTablesToFolderInfoDatabases;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.controllers.main.tables.model.FolderInfoStatus;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.messages.Messages;
import com.girbola.sql.SavedFolderInfosSQL;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class SaveTableFileInfos {

    private final String ERROR = SaveTablesToFolderInfoDatabases.class.getName();

    private Stage stage;
    private LoadingProcessTask loadingProcessTask;
    private boolean closeLoadingStage;
    private ModelMain model_main;

    public SaveTableFileInfos(ModelMain modelMain, Stage stage, LoadingProcessTask loadingProcessTask, boolean closeLoadingStage) {
        this.model_main = modelMain;
        this.stage = stage;

        this.loadingProcessTask = loadingProcessTask;
        this.closeLoadingStage = closeLoadingStage;
    }

    public boolean readTables() {
        boolean sortedTable = iterateTable(model_main.tables().getTableByType(TableType.SORTED.getType()));
        if (!sortedTable) {
            Messages.sprintfError(TableType.SORTED.getType() + " " + Main.bundle.getString("cannotSaveStatus"));
            return false;
        }
        boolean sortItTable = iterateTable(model_main.tables().getTableByType(TableType.SORTIT.getType()));
        if (!sortItTable) {
            Messages.sprintfError(TableType.SORTIT.getType() + " " + Main.bundle.getString("cannotSaveStatus"));
            return false;
        }
        boolean asItIsTable = iterateTable(model_main.tables().getTableByType(TableType.ASITIS.getType()));
        if (!asItIsTable) {
            Messages.sprintfError(TableType.ASITIS.getType() + " " + Main.bundle.getString("cannotSaveStatus"));
            return false;
        }
        return true;
    }

    private boolean iterateTable(TableView<FolderInfo> table) {

        for (FolderInfo folderInfo : table.getItems()) {
            try {
                FolderInfoStatus folderInfoStatus = new FolderInfoStatus(folderInfo.getFolderPath(), folderInfo.getTableType(), folderInfo.getJustFolderName(), folderInfo.isConnected());
                SavedFolderInfosSQL.insertSavedFolderInfoToDatabase(ConfigurationSQLHandler.getConnection(), folderInfoStatus);

            } catch (Exception e) {
                Messages.sprintfError(Main.bundle.getString("cannotSaveStatus"));
                return false;
            }
        }
        return true;
    }
}
