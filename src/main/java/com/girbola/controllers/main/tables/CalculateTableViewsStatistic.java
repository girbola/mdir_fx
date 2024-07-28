package com.girbola.controllers.main.tables;

import com.girbola.controllers.main.TableStatistic;
import com.girbola.controllers.main.Tables;
import com.girbola.messages.Messages;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.TableView;

import java.util.Iterator;

public class CalculateTableViewsStatistic extends Task<Void> {

    private Tables tables;
    private TableView<FolderInfo> table;

    public CalculateTableViewsStatistic(Tables tables, TableView<FolderInfo> table) {
        this.tables = tables;
        this.table = table;
    }

    @Override
    protected Void call() throws Exception {
        Iterator<FolderInfo> tableView = table.getItems().iterator();
        while (tableView.hasNext()) {
            FolderInfo folderInfo = tableView.next();

            if (folderInfo.getFileInfoList().isEmpty()) {
                Messages.sprintf("sortit were empty");
                tableView.remove();
            } else {
                TableStatistic tableStatistic = tables.getTableStatisticByType(table.getId());
                resetStatistics(tableStatistic);

                tableStatistic.setTotalFiles(tableStatistic.totalFiles_property().get() + folderInfo.getFolderFiles());
                tableStatistic.setTotalFilesCopied(tableStatistic.totalFilesCopied_property().get() + folderInfo.getFolderFiles());
                tableStatistic.setTotalFilesSize(tableStatistic.totalFilesSize_property().get() + folderInfo.getFolderFiles());
            }
        }
        return null;
    }

    private static void resetStatistics(TableStatistic tableStatistic) {
        tableStatistic.setTotalFiles(0);
        tableStatistic.setTotalFilesCopied(0);
        tableStatistic.setTotalFilesSize(0);
    }
}
