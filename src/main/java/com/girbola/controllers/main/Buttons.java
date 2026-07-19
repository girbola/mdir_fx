
package com.girbola.controllers.main;

import com.girbola.controllers.main.tables.model.FolderInfo;
import javafx.scene.control.TableView;

import java.util.stream.IntStream;

import static com.girbola.messages.Messages.sprintf;


public class Buttons {

	private ModelMain model;

	protected Buttons(ModelMain model) {
		this.model = model;
		sprintf("Buttons instantiated...");
	}

	private boolean isSelectedTable(TableView<FolderInfo> table, FolderInfo aFolderInfo) {

		for (FolderInfo tv : table.getSelectionModel().getSelectedItems()) {
			if (tv.equals(aFolderInfo)) {
				sprintf("Value found isSelectedTable");
				return true;
			}
		}
//		setFocusOnTable(table);
		return false;
	}

	void select_invert_Table(TableView<FolderInfo> table) {
        IntStream.range(0, table.getItems().size()).forEach(i -> {
            if (isSelectedTable(table, table.getItems().get(i))) {
                table.getSelectionModel().clearSelection(i);
            } else {
                table.getSelectionModel().select(i);
            }
        });
//		setFocusOnTable(table);
	}

	/*
	 * Select all in table according data model
	 */
	void select_all_Table(TableView<FolderInfo> table) {
		sprintf("Select all: " + table.getItems().size());
		for (FolderInfo tv : table.getItems()) {
			table.getSelectionModel().select(tv);
		}
//		setFocusOnTable(table);
	}

	void select_bad_Table(TableView<FolderInfo> table) {
        IntStream.range(0, table.getItems().size()).forEach(i -> {
            if (table.getItems().get(i).getBadFiles() >= 1) {
                table.getSelectionModel().select(i);
            } else {
                table.getSelectionModel().clearSelection(i);
            }
        });

		/*
		 * for (int j = 0; j < table.getColumns().size(); j++) { if
		 * (table.getColumns().get(j).getId().equals("badFiles_col")) {
		 * table.getSortOrder().add(table.getColumns().get(j));
		 * table.getSortOrder().setAll(Collections.singletonList(table.getColumns().get(
		 * j))); table.getColumns().get(j).setSortType(SortType.ASCENDING); } }
		 */

//		setFocusOnTable(table);
	}

	void select_dateDifference_Table(TableView<FolderInfo> table) {
        IntStream.range(0, table.getItems().size()).forEach(i -> {
            if (table.getItems().get(i).getDateDifferenceRatio() >= 2) {
                table.getSelectionModel().select(i);
            } else {
                table.getSelectionModel().clearSelection(i);
            }
        });
//		setFocusOnTable(table);
	}

	void select_good_Table(TableView<FolderInfo> table) {
        IntStream.range(0, table.getItems().size()).forEach(i -> {
            if (table.getItems().get(i).getBadFiles() == 0 && table.getItems().get(i).getGoodFiles() >= 1
                    && table.getItems().get(i).getDateDifferenceRatio() <= 1) {
                table.getSelectionModel().select(i);
            } else {
                table.getSelectionModel().clearSelection(i);
            }
        });
//		setFocusOnTable(table);
	}

	void select_none_Table(TableView<FolderInfo> table) {
		table.getSelectionModel().clearSelection();
	}
}
