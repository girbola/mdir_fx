package com.girbola.controllers.main.tables.cell;

import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.controllers.main.tables.tabletype.FolderInfoType;

import javafx.scene.control.TableCell;

public class TableCell_Copied extends TableCell<FolderInfo, Integer> {

	@Override
	protected void updateItem(Integer item, boolean empty) {
		super.updateItem(item, empty);
		if (empty) {
			setGraphic(null);
			setText(null);
		} else {

			FolderInfo folderInfo = (FolderInfo) getTableView().getItems().get(getIndex());
			if (folderInfo == null || item == null) {
				return;
			}
			if (folderInfo.getFileInfoList() == null) {
				return;
			}
//			TableUtils.updateCopiedStatus(folderInfo);
			if (folderInfo.getFolderFiles() != 0) {
				if (item == folderInfo.getFileInfoList().size() && folderInfo.getFileInfoList().size() != 0) {
					folderInfo.setState(FolderInfoType.DONE.getType());
					setStyle("-fx-background-color: blue; ");
					setText("" + item);
				} else {
					setStyle("-fx-background-color: orange;");
					setText("" + item);
					// setStyle("null");
				}
			}

		}
	}

}
