package com.girbola.fxml.main.merge;

import com.girbola.controllers.main.tables.FolderInfo;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class AbsolutePathCellFactory extends ListCell<FolderInfo> {

    @Override
    protected void updateItem(FolderInfo item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null || empty) {
            setGraphic(null);
        } else {
            setText(item.getFolderPath());
        }

    }
}
