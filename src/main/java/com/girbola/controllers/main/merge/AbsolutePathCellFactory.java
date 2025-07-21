package com.girbola.controllers.main.merge;

import com.girbola.controllers.main.tables.model.FolderInfo;
import javafx.scene.control.ListCell;

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
