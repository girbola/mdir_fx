package com.girbola.controllers.main.tables.cell;

import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.controllers.main.tables.tabletype.FolderInfoType;
import com.girbola.messages.Messages;
import javafx.scene.control.TableCell;

public class TableCell_Copied extends TableCell<FolderInfo, Integer> {

	@Override
	protected void updateItem(Integer item, boolean empty) {
		super.updateItem(item, empty);
		setStyle("");
		if (empty) {
//			setStyle("null");
			setGraphic(null);
			setText(null);
		} else {
			FolderInfo folderInfo = (FolderInfo) getTableView().getItems().get(getIndex());
			if (folderInfo == null || item == null) {
//				setStyle("null");
				setGraphic(null);
				setText(null);
				return;
			}
			if (folderInfo.getFileInfoList() == null) {
//				setStyle("null");
				setGraphic(null);
				setText(null);
				return;
			}
			if (folderInfo.getFolderFiles() != 0) {
				if (folderInfo.getFileInfoList().size() != 0) {
					if (item == folderInfo.getFileInfoList().size()) {
						folderInfo.setState(FolderInfoType.DONE.getType());
						setStyle("-fx-background-color: #29483c; -fx-text-fill: #b9e6cd;");
						setText("" + item);
					} else if (item > 0) {
						setStyle("-fx-text-fill: #dce2e9;");
						setText("" + item);
					} else if (item == 0) {
						setStyle("-fx-text-fill: #b7a36c;");
						setText("" + item);
					} else {
//						setStyle("null");
						setGraphic(null);
						setText(null);
						Messages.sprintf("TableCellfolderInfo ELSE");
					}
				} else {
//					setStyle("null");
					setGraphic(null);
					setText(null);
					Messages.sprintf("folderInfo.getFileInfoList().size() == 0");
				}
			} else {
//				setStyle("null");
				setGraphic(null);
				setText(null);
			}

		}
	}

}
