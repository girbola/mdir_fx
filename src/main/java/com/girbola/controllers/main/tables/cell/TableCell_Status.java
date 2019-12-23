package com.girbola.controllers.main.tables.cell;

import com.girbola.controllers.main.Model_main;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.controllers.main.tables.tabletype.FolderInfoType;

import javafx.scene.control.TableCell;
import javafx.scene.paint.Color;

public class TableCell_Status extends TableCell<FolderInfo,
		Integer> {

//	private Scene scene;
	private Model_main model_main;

	public TableCell_Status(Model_main model_main) {
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
				setText(FolderInfoType.DONE.getType());
				setTextFill(Color.BLUE);
				setStyle("-fx-text-fill: white;");
			} else {
				setText(null);
				setGraphic(null);

			}
		}

	}

}
