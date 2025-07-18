package com.girbola.controllers.main.tables;

import com.girbola.controllers.main.Tables;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import javafx.scene.control.TableView;

import java.util.Iterator;

public class RefreshAllTableContent {
    private Tables tables;

    public RefreshAllTableContent(Tables tables) {
        this.tables = tables;

        Messages.sprintf("RefreshTableContent size: " + tables.getSortIt_table().getItems().size());
        refresh(tables.getSortIt_table());
        refresh(tables.getSorted_table());
        refresh(tables.getAsItIs_table());
    }

    //    @Override
    void refresh(TableView<FolderInfo> table) {
        Iterator<FolderInfo> tableIT = table.getItems().iterator();

        if (table == null) {
            Messages.sprintfError("refreshTableContent - Table were null at TableUtils. Line: " + Misc.getLineNumber());
            return;
        }
        if (table.getColumns().getFirst() != null) {
            while (tableIT.hasNext()) {
                FolderInfo folderInfo = tableIT.next();
                if (folderInfo.getFileInfoList().isEmpty()) {
                    Messages.sprintf("tableIT had empty folder. Removing... SIZE: " + folderInfo.getFileInfoList().size());
                    tableIT.remove();
                }
            }
        }
    }

//
//    @Override
//    protected void succeeded() {
//        super.succeeded();
//        Messages.sprintf("RefreshTableContent succeeded: " + table.getId());
//        Platform.runLater(()-> {
//            table.getColumns().get(0).setVisible(false);
//            table.getColumns().get(0).setVisible(true);
//            table.refresh();
//        });
//    }
//
//    @Override
//    protected void cancelled() {
//        super.cancelled();
//        Messages.sprintf("RefreshTableContent cancelled: " + table.getId());
////        Main.setProcessCancelled(true);
//
//        Platform.runLater(()-> {
//            table.getColumns().get(0).setVisible(false);
//            table.getColumns().get(0).setVisible(true);
//            table.refresh();
//        });
//    }
//
//    @Override
//    protected void failed() {
//        super.failed();
//        Messages.sprintf("RefreshTableContent failed: " + table.getId());
////        Main.setProcessCancelled(true);
//
//        Platform.runLater(()-> {
//            table.getColumns().get(0).setVisible(false);
//            table.getColumns().get(0).setVisible(true);
//            table.refresh();
//        });
//    }

}
