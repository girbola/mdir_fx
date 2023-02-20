/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.workdir;

import java.io.File;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.girbola.Main;
import com.girbola.controllers.main.Model_main;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.Image;
import javafx.util.Callback;

public class WorkDirController {

	SimpleDateFormat dateFormat = new SimpleDateFormat();
	NumberFormat numberFormat = NumberFormat.getIntegerInstance();

	@FXML
	private ComboBox<String> combBox_Day;
	@FXML
	private ComboBox<String> combBox_Month;
	@FXML
	private ComboBox<String> combBox_Year;
	@FXML
	private ComboBox<String> combBox_Event;
	@FXML
	private ComboBox<String> combBox_Location;

	@FXML
	private TreeTableView<File> treeTableView;

	@FXML
	private Label label;

	private Model_main model_Main;

	public void init(Model_main aModel_main) {
		this.model_Main = aModel_main;
	}

	@FXML
	private TreeTableColumn<File, FileTreeItem> nameColumn;
	@FXML
	private TreeTableColumn<File, String> sizeColumn;
	@FXML
	private TreeTableColumn<File, String> lastModifiedColumn;

	public void createFileBrowserTreeTableView() {

		FileTreeItem root = new FileTreeItem(new File(Main.conf.getWorkDir()));

		treeTableView.setShowRoot(true);
		treeTableView.setRoot(root);
		root.setExpanded(true);
//		treeTableView.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);

		nameColumn.setCellValueFactory(
				cellData -> new ReadOnlyObjectWrapper<FileTreeItem>((FileTreeItem) cellData.getValue()));

//		Image image1 = getImageResource("img/unknown-file-16x16.png");
//		Image image2 = getImageResource("img/folder-open-16x16.png");
//		Image image3 = getImageResource("img/folder-close-16x16.png");

		nameColumn.setCellFactory(column -> {
			TreeTableCell<File, FileTreeItem> cell = new TreeTableCell<File, FileTreeItem>() {

//				ImageView imageView1 = new ImageView(image1);
//				ImageView imageView2 = new ImageView(image2);
//				ImageView imageView3 = new ImageView(image3);

				@Override
				protected void updateItem(FileTreeItem item, boolean empty) {
					super.updateItem(item, empty);

					if (item == null || empty || item.getValue() == null) {
						setText(null);
						setGraphic(null);
						setStyle("");
					} else {
						File f = item.getValue();
						String text = f.getParentFile() == null ? File.separator : f.getName();
						setText(text);
						String style = item.isHidden() && f.getParentFile() != null ? "-fx-accent"
								: "-fx-text-base-color";
						setStyle("-fx-text-fill: " + style);
//						if (item.isLeaf()) {
//							setGraphic(imageView1);
//						} else {
//							setGraphic(item.isExpanded() ? imageView2 : imageView3);
//						}
					}
				}
			};
			return cell;
		});

		nameColumn.setPrefWidth(300);
//		nameColumn.setSortable(false);
//		treeTableView.getColumns().add(nameColumn);

		sizeColumn.setCellValueFactory(cellData -> {
			FileTreeItem item = ((FileTreeItem) cellData.getValue());
			String s = item.isLeaf() ? numberFormat.format(item.length()) : "";
			return new ReadOnlyObjectWrapper<String>(s);
		});

		Callback<TreeTableColumn<File, String>, TreeTableCell<File, String>> sizeCellFactory = sizeColumn
				.getCellFactory();
		sizeColumn.setCellFactory(column -> {
			TreeTableCell<File, String> cell = sizeCellFactory.call(column);
			cell.setAlignment(Pos.CENTER_RIGHT);
			cell.setPadding(new Insets(0, 8, 0, 0));
			return cell;
		});

		sizeColumn.setPrefWidth(100);
		sizeColumn.setSortable(false);

		lastModifiedColumn.setCellValueFactory(cellData -> {
			FileTreeItem item = (FileTreeItem) cellData.getValue();
			String s = dateFormat.format(new Date(item.lastModified()));
			return new ReadOnlyObjectWrapper<String>(s);
		});

		lastModifiedColumn.setPrefWidth(130);
		lastModifiedColumn.setSortable(false);

		treeTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			label.setText(newValue != null ? newValue.getValue().getAbsolutePath() : "");
		});

		treeTableView.getSelectionModel().selectFirst();

//		return treeTableView;
	}

	private Image getImageResource(String name) {
		Image img = null;
		try {
			img = new Image(getClass().getResourceAsStream(name));
		} catch (Exception e) {
		}
		return img;
	}

	private class FileTreeItem extends TreeItem<File> {
		private boolean expanded = false;
		private boolean directory;
		private boolean hidden;
		private long length;
		private long lastModified;

		FileTreeItem(File file) {
			super(file);
			EventHandler<TreeModificationEvent<File>> eventHandler = event -> changeExpand();
			addEventHandler(TreeItem.branchExpandedEvent(), eventHandler);
			addEventHandler(TreeItem.branchCollapsedEvent(), eventHandler);

			directory = getValue().isDirectory();
			hidden = getValue().isHidden();
			length = getValue().length();
			lastModified = getValue().lastModified();
		}

		private void changeExpand() {
			if (expanded != isExpanded()) {
				expanded = isExpanded();
				if (expanded) {
					createChildren();
				} else {
					getChildren().clear();
				}
				if (getChildren().size() == 0)
					Event.fireEvent(this,
							new TreeItem.TreeModificationEvent<>(TreeItem.valueChangedEvent(), this, getValue()));
			}
		}

		@Override
		public boolean isLeaf() {
			return !isDirectory();
		}

		public boolean isDirectory() {
			return directory;
		}

		public long lastModified() {
			return lastModified;
		}

		public long length() {
			return length;
		}

		public boolean isHidden() {
			return hidden;
		}

		private void createChildren() {
			if (isDirectory() && getValue() != null) {
				File[] files = getValue().listFiles();
				if (files != null && files.length > 0) {
					getChildren().clear();
					for (File childFile : files) {
						getChildren().add(new FileTreeItem(childFile));
					}
					getChildren().sort((ti1, ti2) -> {
						return ((FileTreeItem) ti1).isDirectory() == ((FileTreeItem) ti2).isDirectory()
								? ti1.getValue().getName().compareToIgnoreCase(ti2.getValue().getName())
								: ((FileTreeItem) ti1).isDirectory() ? -1 : 1;
					});
				}
			}
		}
	} // end class FileTreeItem

}
