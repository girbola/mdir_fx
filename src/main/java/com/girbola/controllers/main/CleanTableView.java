package com.girbola.controllers.main;

import com.girbola.controllers.main.tables.model.FolderInfo;
import javafx.concurrent.Task;
import javafx.scene.control.TableView;
import lombok.extern.java.Log;

import java.util.Iterator;

@Log
public class CleanTableView extends Task<Void> {
    private TableView<FolderInfo> tableView;

    @Override
    protected void succeeded() {
        super.succeeded();
        log.info("Removing empty folders from tables is done");
    }

    @Override
    protected void cancelled() {
        super.cancelled();
        log.info("Removing empty folders from tables is cancelled");
    }

    @Override
    protected void failed() {
        super.failed();
        log.info("Removing empty folders from tables is done");
    }

    public CleanTableView(TableView<FolderInfo> tableView) {
        this.tableView = tableView;
    }

    @Override
    protected Void call() throws Exception {
        Iterator<FolderInfo> iterator = tableView.getItems().iterator();
        while (iterator.hasNext()) {
            FolderInfo folderInfo = tableView.getItems().iterator().next();
            if (folderInfo.getFileInfoList().size() == 0) {
                iterator.remove();
            }
        }

        return null;
    }
}
