/*
 @(#)Copyright:  Copyright (c) 2012-2020 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.datefixer;

import static com.girbola.messages.Messages.sprintf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

/**
 *
 * @author Marko Lokka
 */
public class SelectionModel {

	private SimpleIntegerProperty selectedIndicator_property = new SimpleIntegerProperty();

	private ObservableList<Node> selectionList = FXCollections.observableArrayList();

	public SelectionModel() {
		this.selectionList.addListener(new ListChangeListener<Node>() {
			@Override
			public void onChanged(ListChangeListener.Change<? extends Node> c) {
				Platform.runLater(() -> {
					selectedIndicator_property.set(selectionList.size());
				});
			}
		});
	}

	public synchronized void addAll(Node node) {
		// sprintf("addAll: " + node);
		if (!contains(node)) {
			node.setStyle(CssStylesController.getStyleSelected());
			this.selectionList.add(node);
		}
	}

	/**
	 * Returns true if node is already selected. Otherwise returns false as node is
	 * deselected
	 * 
	 * @param node
	 * @return
	 */
	public synchronized boolean add(Node node) {
		if (!contains(node)) {
			Platform.runLater(() -> {
				node.setStyle(CssStylesController.getStyleSelected());
				this.selectionList.add(node);
			});
			return false;
			// }
		} else {
			Platform.runLater(() -> {
				sprintf("remove: " + node);
				remove(node);
			});
			return true;
		}

	}

	/**
	 * 
	 * @param node
	 */
	public synchronized void addOnly(Node node) {
		if (!contains(node)) {
			node.setStyle(CssStylesController.getStyleSelected());
			this.selectionList.add(node);
		}
	}

	/**
	 * 
	 */
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
			Messages.sprintf("NODE deselected: " + node.getId());
//			node.getStyleClass().remove(CssStylesController.getStyleSelected());
			node.setStyle(CssStylesController.getStyleDeselected());
//			node.getStyleClass().clear();
//			node.getStyleClass().add(null);
//			node.getStyleClass().add("imageFrameDeSelected");
			this.selectionList.remove(node);
		} else {
			Messages.sprintf("NODE CAN't be deselected: " + node.getId());
		}
	}

	public int getRightColorForBackground(Node node, FileInfo fileInfo) {
		if (fileInfo.isBad()) { // esim red
			node.setStyle(CssStylesController.getStyleBad());
		} else if(fileInfo.isConfirmed()) {
//			FileInfo
		} else if(fileInfo.isGood()) {
			
		} else if(fileInfo.isConfirmed()) {
			
		}
		return 0;
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
