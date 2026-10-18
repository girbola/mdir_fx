
package com.girbola.controllers.main.tables.cell;

import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.tables.model.FolderInfo;
import javafx.scene.control.TableCell;
import javafx.scene.paint.Color;


public class TableCell_DateDifference_Status extends TableCell<FolderInfo, Double> {

	private ModelMain model_main;

	public TableCell_DateDifference_Status(ModelMain model_main) {
		this.model_main = model_main;

	}

	@Override
	protected void updateItem(Double item, boolean empty) {
		super.updateItem(item, empty);
		if (empty) {
			setStyle("");
			setTextFill(null);
			setGraphic(null);
			setText(null);
		} else {
			FolderInfo folderInfo = getTableView().getItems().get(getIndex());
			if (folderInfo != null) {
				if (folderInfo.getFolderFiles() != 0) {
					if (folderInfo.getStatus() != 100 || folderInfo.getDateDifferenceRatio() >= 2) {
						// sprintf("folderInfo.getStatus(): " + folderInfo.getStatus() + "
						// folderInfo.getDateDifferenceRatio(): " +
						// folderInfo.getDateDifferenceRatio());
						setStyle("");
						setTextFill(Color.web("#d98282"));
						setText("" + item);
					} else {

						// sprintf("ELSE!! folderInfo.getStatus(): " + folderInfo.getStatus() + "
						// folderInfo.getDateDifferenceRatio(): " +
						// folderInfo.getDateDifferenceRatio());
						setStyle("");
						setTextFill(Color.web("#8fc49f"));
						setText("" + item);
					}
				} else {
					setStyle("");
					setTextFill(Color.TRANSPARENT);
					setText("");
				}
				// sprintf("folderInfo-> " + folderInfo.toString());
				// sprintf("file: " + folderInfo.getFolderPath() + " folderInfo
				// tablecell_Status: " + folderInfo.getStatus() + " raw: " +
				// folderInfo.getFolderRawFiles());
			}
		}
	}
}
