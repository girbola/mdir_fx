package com.girbola.controllers.importimages;

import static com.girbola.messages.Messages.sprintf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

class SelectionModel_Import {

	final private String style_deselected = "-fx-border-color: white;" + "-fx-border-radius: 1 1 1 1;" + "-fx-border-style: none;"
			+ "-fx-border-width: 2px;";
	final private String style_removed = "-fx-border-color: red;" + "-fx-border-width: 2px;";
	final private String style_selected = "-fx-border-color: red;" + "-fx-border-width: 2px;";

	/*
	 * -fx-background-color: #36443a; -fx-background-radius: 1 1 1 1;
	 * -fx-border-color: white; -fx-border-radius: 1 1 1 1; -fx-border-width:
	 * 2px;
	 */
	private SimpleIntegerProperty selectedIndicator_property = new SimpleIntegerProperty();

	private ObservableList<Node> selectionList = FXCollections.observableArrayList();

//	SelectionModel_Import() {
//		this.selectionList.addListener(new ListChangeListener<Node>() {
//			@Override
//			public void onChanged(ListChangeListener.Change<? extends Node> c) {
//				Messages.sprintf("selectionList changed");
//				selectedIndicator_property.set(selectionList.size());
//			}
//		});
//	}

	public String getStyle_removed() {
		return style_removed;
	}

	public synchronized void addAll(Node node) {
		// sprintf("addAll: " + node);
		if (!contains(node)) {
			node.setStyle(style_selected);
			this.selectionList.add(node);
		}
	}

	public synchronized void add(Node node) {
		sprintf("56add: " + node);

		if (!contains(node)) {
			sprintf("59add: " + node);
			node.setStyle(style_selected);
			this.selectionList.add(node);
			// }
		} else {
			sprintf("remove: " + node);
			remove(node);
		}

	}

	public synchronized void clearAll() {
		sprintf("clearing all");

		while (!this.selectionList.isEmpty()) {
			remove(this.selectionList.iterator().next());
		}

	}

	public synchronized boolean contains(Node node) {
		return this.selectionList.contains(node);
	}

	public void log() {
		sprintf("Items in model: " + Arrays.asList(selectionList.toArray()));
	}

	public synchronized void remove(Node node) {
		if (contains(node)) {
			node.getStyleClass().remove(style_selected);
			node.setStyle(style_deselected);
			this.selectionList.remove(node);

		}
	}

	public synchronized void invertSelection(Pane pane) {
		if (pane == null || !pane.isVisible()) {
			return;
		}
		List<Node> list = new ArrayList<>();

		if (pane instanceof GridPane) {
			for (Node grid : pane.getChildren()) {
				// if (grid instanceof VBox) {
				if (!contains(grid)) {
					list.add(grid);
					// }
				}
			}
		}

		if (list.isEmpty()) {
			Messages.sprintf("list was empty at selectionModel lineb " + selectionList.size());
			return;
		}
		clearAll();
		list.forEach((n) -> {
			add(n);
		});
		list.clear();

	}

	public synchronized void isAllSelected(CheckBox checkBox, List<FileInfo> list) {
		List<FileInfo> sel = new ArrayList<>();
		if (list == null) {
			Messages.sprintf("isAllSelected getUserData were null");
			return;
		}
		for (FileInfo fileInfo : list) {
			for (Node node : selectionList) {
				FileInfo node_fl = (FileInfo) node.getUserData();
				if (node_fl.equals(fileInfo)) {
					sel.add(node_fl);
				}
			}
		}
		if (sel.size() == 0) {
			checkBox.setSelected(false);
		} else if (sel.size() == list.size()) {
			checkBox.setSelected(true);
		} else if (sel.size() != list.size()) {
			checkBox.setIndeterminate(true);
		}
	}

	public synchronized void setSelectionList(ObservableList<Node> selectionList) {
		this.selectionList = selectionList;
	}

	public ObservableList<Node> getSelectionList() {
		return this.selectionList;
	}

	public SimpleIntegerProperty getSelectedIndicator_property() {
		return this.selectedIndicator_property;
	}

	public synchronized void setSelected(SimpleIntegerProperty selected) {
		this.selectedIndicator_property = selected;
	}

}
