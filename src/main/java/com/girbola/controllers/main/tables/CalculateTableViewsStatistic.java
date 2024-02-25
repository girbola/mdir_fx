package com.girbola.controllers.main.tables;

import com.girbola.controllers.main.Tables;
import com.girbola.messages.Messages;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.util.Iterator;

public class CalculateTableViewsStatistic extends Task<Void> {

    private Tables tables;

    public CalculateTableViewsStatistic(Tables tables) {
        this.tables = tables;
    }

    @Override
    protected Void call() throws Exception {
        Platform.runLater(() -> {
            resetStatistics(tables);
        });
        Iterator<FolderInfo> sortitIT = tables.getSortIt_table().getItems().iterator();
        while (sortitIT.hasNext()) {
            FolderInfo folderInfo = sortitIT.next();

            if (folderInfo.getFileInfoList().isEmpty()) {
                Messages.sprintf("sortit were empty");
                sortitIT.remove();
            } else {
                Platform.runLater(() -> {
                    tables.getSortit_TableStatistic().setTotalFiles(tables.getSortit_TableStatistic().totalFiles_property().get() + folderInfo.getFolderFiles());
                    tables.getSortit_TableStatistic().setTotalFilesCopied(tables.getSortit_TableStatistic().totalFilesCopied_property().get() + folderInfo.getCopied());
                    tables.getSortit_TableStatistic().setTotalFilesSize(tables.getSortit_TableStatistic().totalFilesSize_property().get() + folderInfo.getFolderSize());
                });
            }
        }
        Iterator<FolderInfo> sortedIT = tables.getSorted_table().getItems().iterator();
        while (sortedIT.hasNext()) {
            FolderInfo folderInfo = sortedIT.next();
            if (folderInfo.getFileInfoList().isEmpty()) {
                Messages.sprintf("sorted were empty");
                sortedIT.remove();
            } else {
                Platform.runLater(() -> {
                    tables.getSorted_TableStatistic().setTotalFiles(tables.getSorted_TableStatistic().totalFiles_property().get() + folderInfo.getFolderFiles());
                    tables.getSorted_TableStatistic().setTotalFilesCopied(tables.getSorted_TableStatistic().totalFilesCopied_property().get() + folderInfo.getCopied());
                    tables.getSorted_TableStatistic().setTotalFilesSize(tables.getSorted_TableStatistic().totalFilesSize_property().get() + folderInfo.getFolderSize());
                });
            }
        }

        Iterator<FolderInfo> asitisIT = tables.getSorted_table().getItems().iterator();
        while (asitisIT.hasNext()) {
            FolderInfo folderInfo = asitisIT.next();
            if (folderInfo.getFileInfoList().isEmpty()) {
                Messages.sprintf("asitis were empty");
                asitisIT.remove();
            } else {
                Platform.runLater(() -> {
                    tables.getAsItIs_TableStatistic().setTotalFiles(tables.getAsItIs_TableStatistic().totalFiles_property().get() + folderInfo.getFolderFiles());
                    tables.getAsItIs_TableStatistic().setTotalFilesCopied(tables.getAsItIs_TableStatistic().totalFilesCopied_property().get() + folderInfo.getCopied());
                    tables.getAsItIs_TableStatistic().setTotalFilesSize(tables.getAsItIs_TableStatistic().totalFilesSize_property().get() + folderInfo.getFolderSize());
                });
            }
        }
        return null;
    }

    static void resetStatistics(Tables tables) {
        tables.getSortit_TableStatistic().setTotalFiles(0);
        tables.getSorted_TableStatistic().setTotalFiles(0);
        tables.getAsItIs_TableStatistic().setTotalFiles(0);

        tables.getSortit_TableStatistic().setTotalFilesCopied(0);
        tables.getSorted_TableStatistic().setTotalFilesCopied(0);
        tables.getAsItIs_TableStatistic().setTotalFilesCopied(0);

        tables.getSortit_TableStatistic().setTotalFilesSize(0);
        tables.getSorted_TableStatistic().setTotalFilesSize(0);
        tables.getAsItIs_TableStatistic().setTotalFilesSize(0);
    }
}
