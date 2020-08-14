package com.girbola.fxml.datestreetableview;

import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeTableCell;

public class ListTreeTableViewCell extends TreeTableCell<TreeFolderInfo, String> {

	private ObservableList<String> eventList;
	ListView<String> listView = new ListView<>();

	ListTreeTableViewCell(ObservableList<String> eventList) {
		this.eventList = eventList;
	}

	@Override
	protected void updateItem(String t, boolean empty) {
		super.updateItem(t, empty);
		if (!empty) {
			setGraphic(listView);
		}
	}
}
