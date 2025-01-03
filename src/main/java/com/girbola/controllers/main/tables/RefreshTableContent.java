package com.girbola.controllers.main.tables;

import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.TableView;

import java.util.Iterator;

public class RefreshTableContent extends Task<Void> {
    private TableView<FolderInfo> table;

    public RefreshTableContent(TableView<FolderInfo> table) {
        this.table = table;
    }

    @Override
    protected Void call() throws Exception {
        if (table == null) {
            Messages.sprintfError("refreshTableContent - Table were null at TableUtils. Line: " + Misc.getLineNumber());
            return null;
        }
        if (table.getColumns().get(0) != null) {

            Iterator<FolderInfo> tableIT = table.getItems().iterator();
            while(tableIT.hasNext()) {
                FolderInfo folderInfo = tableIT.next();

                if(folderInfo.getFileInfoList().isEmpty()) {
                    Messages.sprintf("tableIT had empty folder. Removing...");
                    tableIT.remove();
                }
            }
        }
        return null;
    }


    @Override
    protected void succeeded() {
        super.succeeded();
        Messages.sprintf("RefreshTableContent succeeded: " + table.getId());
        Platform.runLater(()-> {
            table.getColumns().get(0).setVisible(false);
            table.getColumns().get(0).setVisible(true);
            table.refresh();
        });
    }

    @Override
    protected void cancelled() {
        super.cancelled();
        Messages.sprintf("RefreshTableContent cancelled: " + table.getId());
//        Main.setProcessCancelled(true);

        Platform.runLater(()-> {
            table.getColumns().get(0).setVisible(false);
            table.getColumns().get(0).setVisible(true);
            table.refresh();
        });
    }

    @Override
    protected void failed() {
        super.failed();
        Messages.sprintf("RefreshTableContent failed: " + table.getId());
//        Main.setProcessCancelled(true);

        Platform.runLater(()-> {
            table.getColumns().get(0).setVisible(false);
            table.getColumns().get(0).setVisible(true);
            table.refresh();
        });
    }

}
