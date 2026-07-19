
package com.girbola.controllers.main;

import com.girbola.Main;
import com.girbola.controllers.loading.LoadingProcessTask;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.controllers.main.tables.FolderInfoUtils;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.sql.SQL_Utils;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Task;
import javafx.scene.control.TableView;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.girbola.Main.conf;
import static com.girbola.utils.FileInfoUtils.createFileInfo_list;
import static com.girbola.messages.Messages.sprintf;


public class CalculateFolderContent extends Task<Void> {

    private final String ERROR = CalculateFolderContent.class.getSimpleName();

    private ModelMain modelMain;
    private IntegerProperty total;
    private IntegerProperty counter = new SimpleIntegerProperty();

    public CalculateFolderContent(ModelMain modelMain, LoadingProcessTask aLoadingProcess_Task, IntegerProperty total) {
        this.modelMain = modelMain;
        this.total = total;
        this.counter = this.total;
    }

    @Override
    protected Void call() throws Exception {
        init();
        return null;
    }

    private void loopFolderInfos(final String tableType) {
        TableView<FolderInfo> tableView;

        if (Main.getProcessCancelled()) {
            cancel();
            return;
        }
        if (tableType.equals(TableType.SORTED.getType())) {
            tableView = modelMain.tables().getSorted_table();
        } else if (tableType.equals(TableType.SORTIT.getType())) {
            tableView = modelMain.tables().getSortIt_table();
        } else if (tableType.equals(TableType.ASITIS.getType())) {
            tableView = modelMain.tables().getAsItIs_table();
        } else {
            Messages.errorSmth(ERROR, "Can't find Tabletype: " + tableType, null, Misc.getLineNumber(), true);
            return;
        }
        for (FolderInfo folderInfo : tableView.getItems()) {
            sprintf("tableType is: " + tableType + " createFileInfo_list: " + folderInfo.getFolderPath());
            if (Main.getProcessCancelled()) {
                cancel();
                break;
            }
            handleFolderInfo(folderInfo, tableView);
            folderInfo.setTableType(tableType);
        }
        TableUtils.refreshTableContent(tableView);
    }

    private void init() {
        checkIFCancelled();
        updateMessage(TableType.SORTIT.getType());
        loopFolderInfos(TableType.SORTIT.getType());

        checkIFCancelled();
        updateMessage(TableType.SORTED.getType());
        loopFolderInfos(TableType.SORTED.getType());

        checkIFCancelled();
        updateMessage(TableType.ASITIS.getType());
        loopFolderInfos(TableType.ASITIS.getType());
        checkIFCancelled();
    }

    private void checkIFCancelled() {
        if (Main.getProcessCancelled()) {
            cancel();
            return;
        }
    }

    private void handleFolderInfo(FolderInfo folderInfo, TableView<FolderInfo> tableView) {
        Path mdirDatabaseFilePath = Paths.get(folderInfo.getFolderPath(), conf.getMdir_db_fileName());
        Messages.sprintf("mdirDatabaseFilePath trying to find folderinfo path: " + mdirDatabaseFilePath);

        try {
            if (hasFileInfos(folderInfo)) {
                Messages.sprintf("Using existing FileInfo list: " + folderInfo.getFolderPath());
                finishFolderInfo(folderInfo, tableView);
                return;
            }

            FolderInfo loadedFolderInfo = loadFolderInfoIfDatabaseExists(mdirDatabaseFilePath);

            if (loadedFolderInfo != null && hasFileInfos(loadedFolderInfo)) {
                folderInfo.setBadFiles(loadedFolderInfo.getBadFiles());
                folderInfo.setFileInfoList(loadedFolderInfo.getFileInfoList());
                Messages.sprintf("Loaded FolderInfo from database: " + folderInfo.getFolderPath());
                finishFolderInfo(folderInfo, tableView);
                return;
            }

            Messages.sprintf("Creating FileInfo list for: " + folderInfo.getFolderPath());
            List<FileInfo> fileInfos = createFileInfo_list(folderInfo);
            folderInfo.setFileInfoList(fileInfos);

            if (hasFileInfos(folderInfo)) {
                finishFolderInfo(folderInfo, tableView);
            } else {
                Messages.sprintf("FolderInfo FileInfo list was empty: " + folderInfo.getFolderPath());
                updateTaskProgress(folderInfo, tableView);
            }
        } catch (Exception ex) {
            Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
            updateTaskProgress(folderInfo, tableView);
        }
    }

    private FolderInfo loadFolderInfoIfDatabaseExists(Path mdirDatabaseFilePath) {
        if (!Files.exists(mdirDatabaseFilePath)) {
            Messages.sprintf("File DOES NOT exist at: " + mdirDatabaseFilePath);
            return null;
        }

        Messages.sprintf("File DOES exists at: " + mdirDatabaseFilePath);

        try {
            FolderInfo loadedFolderInfo = SQL_Utils.loadFolderInfoCurrentDir(mdirDatabaseFilePath.getParent());

            if (loadedFolderInfo == null) {
                Messages.sprintf("loadedFolderInfo was null at: " + mdirDatabaseFilePath);
            }

            return loadedFolderInfo;
        } catch (Exception ex) {
            Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
            return null;
        }
    }

    private boolean hasFileInfos(FolderInfo folderInfo) {
        return folderInfo != null
                && folderInfo.getFileInfoList() != null
                && !folderInfo.getFileInfoList().isEmpty();
    }

    private void finishFolderInfo(FolderInfo folderInfo, TableView<FolderInfo> tableView) {
        FolderInfoUtils.calculateFolderInfoStatus(folderInfo);
        Messages.sprintf("FolderInfo calculated: " + folderInfo.getFolderPath());
        updateTaskProgress(folderInfo, tableView);
    }

    private void updateTaskProgress(FolderInfo folderInfo, TableView<FolderInfo> tableView) {
        counter.set(counter.get() - 1);
        updateProgress(counter.get(), total.get());
        updateMessage(folderInfo.getFolderPath());
        TableUtils.refreshTableContent(tableView);
    }
}
