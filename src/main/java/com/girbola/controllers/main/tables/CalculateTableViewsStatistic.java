package com.girbola.controllers.main.tables;

import com.girbola.controllers.main.Tables;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.TableView;
import uk.co.caprica.vlcj.player.list.PlaybackMode;

public class CalculateTableViewsStatistic extends Task<Void> {

    private Tables tables;

    public CalculateTableViewsStatistic(Tables tables) {
        this.tables = tables;
    }

    @Override
    protected Void call() throws Exception {
        Platform.runLater(()-> {
            tables.getSortit_TableStatistic().setTotalFiles(0);
            tables.getSorted_TableStatistic().setTotalFiles(0);
            tables.getAsItIs_TableStatistic().setTotalFiles(0);

            tables.getSortit_TableStatistic().setTotalFilesCopied(0);
            tables.getSorted_TableStatistic().setTotalFilesCopied(0);
            tables.getAsItIs_TableStatistic().setTotalFilesCopied(0);

            tables.getSortit_TableStatistic().setTotalFilesSize(0);
            tables.getSorted_TableStatistic().setTotalFilesSize(0);
            tables.getAsItIs_TableStatistic().setTotalFilesSize(0);
        });
        for (FolderInfo folderInfo : tables.getSortIt_table().getItems()) {
            Platform.runLater(() -> {

                tables.getSortit_TableStatistic().setTotalFiles(tables.getSortit_TableStatistic().totalFiles_property().get() + folderInfo.getFolderFiles());
                tables.getSortit_TableStatistic().setTotalFilesCopied(tables.getSortit_TableStatistic().totalFilesCopied_property().get() + folderInfo.getCopied());
                tables.getSortit_TableStatistic().setTotalFilesSize(tables.getSortit_TableStatistic().totalFilesSize_property().get() + folderInfo.getFolderSize());

            });
        }
        for (FolderInfo folderInfo : tables.getSorted_table().getItems()) {
            Platform.runLater(() -> {
                tables.getSorted_TableStatistic().setTotalFiles(tables.getSorted_TableStatistic().totalFiles_property().get() + folderInfo.getFolderFiles());
                tables.getSorted_TableStatistic().setTotalFilesCopied(tables.getSorted_TableStatistic().totalFilesCopied_property().get() + folderInfo.getCopied());
                tables.getSorted_TableStatistic().setTotalFilesSize(tables.getSorted_TableStatistic().totalFilesSize_property().get() + folderInfo.getFolderSize());
            });
        }

        for (FolderInfo folderInfo : tables.getAsItIs_table().getItems()) {
            Platform.runLater(() -> {
                tables.getAsItIs_TableStatistic().setTotalFiles(tables.getAsItIs_TableStatistic().totalFiles_property().get() + folderInfo.getFolderFiles());
                tables.getAsItIs_TableStatistic().setTotalFilesCopied(tables.getAsItIs_TableStatistic().totalFilesCopied_property().get() + folderInfo.getCopied());
                tables.getAsItIs_TableStatistic().setTotalFilesSize(tables.getAsItIs_TableStatistic().totalFilesSize_property().get() + folderInfo.getFolderSize());
            });
        }
        return null;
    }
}
