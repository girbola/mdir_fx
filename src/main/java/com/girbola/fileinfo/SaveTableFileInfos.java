package com.girbola.fileinfo;

import com.girbola.controllers.loading.LoadingProcessTask;
import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.SaveTablesToFolderInfoDatabases;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.sql.FileInfo_SQL;
import com.girbola.sql.SQL_Utils;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import javafx.concurrent.Task;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class SaveTableFileInfos {

    private final String ERROR = SaveTablesToFolderInfoDatabases.class.getName();

    private Stage stage;
    private LoadingProcessTask loadingProcess_Task;
    private boolean closeLoadingStage;
    private ModelMain model_main;

    public SaveTableFileInfos(ModelMain model_main, Stage stage, LoadingProcessTask loadingProcess_Task, boolean closeLoadingStage) {
        this.model_main = model_main;
        this.stage = stage;
        if (loadingProcess_Task == null) {
            loadingProcess_Task = new LoadingProcessTask(stage);
            loadingProcess_Task.setTask(this);
        }
        this.loadingProcess_Task = loadingProcess_Task;
        this.closeLoadingStage = closeLoadingStage;
    }



    public void readTables() {

        boolean sortedTable = iterateTable(model_main.tables().getTableByType(TableType.SORTED.getType()));
        boolean sortItTable = iterateTable(model_main.tables().getTableByType(TableType.SORTIT.getType()));
        boolean asItIsTable = iterateTable(model_main.tables().getTableByType(TableType.ASITIS.getType()));




    }

    private boolean iterateTable(TableView<FolderInfo> table) {

        for(FolderInfo folderInfo : table.getItems()) {

            if(!Files.exists(Paths.get(folderInfo.getFolderPath()))) {
                return false;
            }
            Connection folderInfosConnection = SQL_Utils.createFolderInfoDatabase(Paths.get(folderInfo.getFolderPath()));
            if (folderInfosConnection == null) {
                return false;
            }


            FileInfo_SQL.insertFileInfoListToDatabase(folderInfo.getFileInfoList());


        }
    }
}
