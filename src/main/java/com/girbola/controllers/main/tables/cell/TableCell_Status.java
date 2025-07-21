package com.girbola.controllers.main.tables.cell;

import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.controllers.main.tables.tabletype.FolderInfoType;
import javafx.scene.control.TableCell;

public class TableCell_Status extends TableCell<FolderInfo, Integer> {

	private ModelMain model_main;
	private int currentFilesTotal;

	public TableCell_Status(ModelMain model_main) {
		this.model_main = model_main;
	}

	@Override
	protected void updateItem(Integer item, boolean empty) {
		super.updateItem(item, empty);
		if (empty) {
			setGraphic(null);
			setText(null);
		} else {
			FolderInfo folderInfo = getTableView().getItems().get(getIndex());
			if (folderInfo != null) {
				if (folderInfo.getFileInfoList().size() > 0
						&& folderInfo.getFileInfoList().size() == folderInfo.getCopied()
						&& folderInfo.getBadFiles() == 0) {
					setText(FolderInfoType.DONE.getType());
					getStyleClass().add("tableCellStatus_DONE");
				} else if (folderInfo.getFileInfoList().size() > 0 && folderInfo.getBadFiles() == 0) {
					setText("OK");
					getStyleClass().add("tableCellStatus_READY");
				} else {
					int procentage = (int) Math.floor(
							((double) folderInfo.getBadFiles() / (double) folderInfo.getFileInfoList().size()) * 100);
					setText("" + procentage);
					getStyleClass().add("tableCellStatus_BAD");
				}
				if (folderInfo.getFolderFiles() > 0) {
					if (currentFilesTotal > folderInfo.getFolderFiles()) {
						currentFilesTotal = folderInfo.getFolderFiles();
						setText(FolderInfoType.ADD.getType());
						getStyleClass().add("tableCellStatus_ADD");
					}
				}
			} else {
				setText(null);
				setGraphic(null);

			}
		}

	}

}
