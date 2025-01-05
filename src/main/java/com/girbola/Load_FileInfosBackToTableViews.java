package com.girbola;

import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.messages.Messages;
import com.girbola.sql.FolderInfo_SQL;
import com.girbola.sql.SelectedFolderInfo;
import com.girbola.sql.FolderInfosSQL;
import com.girbola.sql.SQL_Utils;
import javafx.concurrent.Task;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.List;

public class Load_FileInfosBackToTableViews extends Task<Boolean> {
    private ModelMain modelMain;
    private Connection connection;

    public Load_FileInfosBackToTableViews(ModelMain modelMain, Connection connection) {
        this.modelMain = modelMain;
        this.connection = connection;
    }

    @Override
    protected Boolean call() throws Exception {
        Messages.sprintf("Load_FileInfosBackToTableViews starts " + Paths.get(Main.conf.getAppDataPath() + File.separator + Main.conf.getConfiguration_db_fileName()));

        if (!Files.exists(Paths.get(Main.conf.getAppDataPath() + File.separator + Main.conf.getConfiguration_db_fileName()))) {
            Messages.sprintf("Can't find " + (Main.conf.getAppDataPath() + File.separator + Main.conf.getConfiguration_db_fileName()));
            return false;
        }

        if (!SQL_Utils.isDbConnected(connection)) {
            return false;
        }

        List<SelectedFolderInfo> selectedFolderInfos = FolderInfosSQL.getAll(connection, modelMain);

        if (selectedFolderInfos == null || selectedFolderInfos.isEmpty()) {
            Messages.sprintfError("folderInfo_list were empty!!!!" + Load_FileInfosBackToTableViews.class.getName());
            cancel();
            return false;
        } else {
            for (SelectedFolderInfo selectedFolderInfo : selectedFolderInfos) {
                if (Main.getProcessCancelled()) {
                    cancel();
                    return false;
                }
                FolderInfo folderInfo = FolderInfo_SQL.loadFolderInfo(selectedFolderInfo.getFolderPath());
                Messages.sprintf("FolderInfo= " + folderInfo.getFolderPath());
                if (folderInfo == null) {
                    Messages.sprintfError("FolderInfo were null!: " + selectedFolderInfo.getFolderPath());
                    continue;
                }

                if (folderInfo.getTableType().equals(TableType.SORTIT.getType())) {
                    modelMain.tables().getSortIt_table().getItems().add(folderInfo);
                }
                if (folderInfo.getTableType().equals(TableType.SORTED.getType())) {
                    modelMain.tables().getSorted_table().getItems().add(folderInfo);
                }
                if (folderInfo.getTableType().equals(TableType.ASITIS.getType())) {
                    modelMain.tables().getAsItIs_table().getItems().add(folderInfo);
                }

            }
        }

        return true;
    }

    @Override
    protected void cancelled() {
        super.cancelled();
        Messages.sprintfError("Load fileinfo back to table cancelled!");
        Main.setChanged(false);
    }

    @Override
    protected void failed() {
        super.failed();
        Messages.sprintfError("Load fileinfo back to table FAILED!");
        Main.setChanged(false);
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        Messages.sprintf("Load fileinfo back to table SUCCEEDED!");
        TableUtils.refreshTableContent(modelMain.tables().getSortIt_table());
        TableUtils.refreshTableContent(modelMain.tables().getSorted_table());
        TableUtils.refreshTableContent(modelMain.tables().getAsItIs_table());
        Main.setChanged(false);
    }

}
