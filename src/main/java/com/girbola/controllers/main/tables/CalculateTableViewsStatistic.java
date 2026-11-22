package com.girbola.controllers.main.tables;

import com.girbola.controllers.main.TableStatistic;
import com.girbola.controllers.main.Tables;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.messages.Messages;
import javafx.scene.control.TableView;

import java.util.Iterator;

public class CalculateTableViewsStatistic {

    private final Tables tables;
    private final TableView<FolderInfo> table;

    public CalculateTableViewsStatistic(Tables tables, TableView<FolderInfo> table) {
        this.tables = tables;
        this.table = table;
    }

    void calculate() {
        TableStatistic tableStatistic = tables.getTableStatisticByType(table.getId());
        int totalFiles = 0;
        int totalFilesCopied = 0;
        long totalFilesSize = 0;
        Iterator<FolderInfo> tableView = table.getItems().iterator();
        while (tableView.hasNext()) {
            FolderInfo folderInfo = tableView.next();

            if (folderInfo.getFileInfoList().isEmpty()) {
                Messages.sprintf("tableView were empty");
                tableView.remove();
            } else {
                totalFiles += folderInfo.getFolderFiles();
                totalFilesCopied += folderInfo.getCopied();
                totalFilesSize += folderInfo.getFolderSize();
            }
        }
        tableStatistic.setTotalFiles(totalFiles);
        tableStatistic.setTotalFilesCopied(totalFilesCopied);
        tableStatistic.setTotalFilesSize(totalFilesSize);
    }
}
