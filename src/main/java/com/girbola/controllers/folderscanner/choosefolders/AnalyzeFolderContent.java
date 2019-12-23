/*
 @(#)Copyright:  Copyright (c) 2012-2019 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.folderscanner.choosefolders;

import static com.girbola.messages.Messages.sprintf;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.girbola.controllers.folderscanner.FolderInfoTable;
import com.girbola.controllers.folderscanner.FolderScanner_Methods;
import com.girbola.controllers.folderscanner.Model_folderScanner;
import com.girbola.controllers.folderscanner.SelectedFolder;
import com.girbola.filelisting.ValidatePathUtils;
import com.girbola.sql.SQL_Utils;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

/**
 *
 * @author Marko Lokka
 */
public class AnalyzeFolderContent extends Task<Void> {

	private List<Path> selected;
	private Model_folderScanner model_folderScanner;
	//    private Path rootPath = null;

	public AnalyzeFolderContent(List<Path> selected, Model_folderScanner model_folderScanner) {
		this.selected = selected;
		this.model_folderScanner = model_folderScanner;

	}

	@Override
	protected Void call() throws Exception {
		sprintf("analyzeFolderContent started: " + selected.size());
		for (Path selectedFolder : model_folderScanner.getDrivesList_selected_obs()) {
			sprintf("getDrivesList is: " + selectedFolder);
		}
		Map<Path,
				List<Path>> map = new HashMap<>();
		for (Path selectedFolder : model_folderScanner.getDrivesList_selected_obs()) {
			List<Path> list = new ArrayList<>();
			list = createList(selectedFolder, selected);
			Collections.sort(list, (Path o1, Path o2) -> {
				if (o1.toString().length() == o2.toString().length()) {
					return 0;
				}
				return o1.toString().length() < o2.toString().length() ? -1 : 1;
			});
			sprintf("rootPath: " + selectedFolder);
			sprintf("First node path would be: " + list.get(0));
			map.put(list.get(0), list);
		}

		CheckBoxTreeItem<FolderInfoTable> root = null;
		for (Map.Entry<Path,
				List<Path>> entry : map.entrySet()) {
			sprintf("Selected Folders ROOT is: " + entry.getKey());

			if (!FolderScanner_Methods.titledPaneExists(model_folderScanner.getAnalyzeList_vbox(), entry.getKey())) {
				TreeTableView ttv = createTreeTableView(entry.getKey());
				TitledPane titledPane = new TitledPane(entry.getKey().toString(), ttv);
				root = new CheckBoxTreeItem<>(new FolderInfoTable(entry.getKey().toString()));
				root.setExpanded(true);
				ttv.setRoot(root);

				titledPane.setContent(ttv);

				model_folderScanner.getAnalyzeList_vbox().getChildren().add(titledPane);
			}

			sprintf("Test root path is false");
			for (Path p : entry.getValue()) {
				if (Files.exists(p)) {
					if (ValidatePathUtils.hasMediaFilesInFolder(p)) {
						TreeItem<FolderInfoTable> cbi = createTreeItem_FolderInfo(p.toFile(), model_folderScanner.getAnalyzeList_selected());
						root.getChildren().add(cbi);
					}
				}
			}

		}
		return null;
	}

	private TreeItem<FolderInfoTable> createTreeItem_FolderInfo(File file, List<TreeItem<FolderInfoTable>> selected_list) {
		TreeItem<FolderInfoTable> treeItem = new TreeItem<>(new FolderInfoTable(file.getAbsolutePath()));
		CheckBox checkBoxi = new CheckBox();
		checkBoxi.getStyleClass().add("checkBoxi");
		checkBoxi.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
			if (newValue) {
				sprintf("Selected TreeItem<FolderInfo> cbi: " + treeItem.getValue().getPath());
				selected_list.add(treeItem);
			} else {
				sprintf("Deselected TreeItem<FolderInfo> cbi: " + treeItem.getValue().getPath());
				selected_list.remove(treeItem);
			}
		});
		treeItem.setGraphic(new HBox(checkBoxi));

		return treeItem;
	}

	private List<Path> createList(Path rootPath, List<Path> selectedList) {
		sprintf("rootPath: " + rootPath);
		List<Path> found = new ArrayList<>();
		for (Path p : selectedList) {
			if (p.getRoot().toString().equals(rootPath.getRoot().toString())) {
				sprintf("found same root! " + p);
				found.add(p);
			}
		}
		return found;
	}

	private TreeTableView<FolderInfoTable> createTreeTableView(Path path) {

		TreeTableView<FolderInfoTable> treeTableView = new TreeTableView<>();
		treeTableView.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);

		TreeTableColumn<FolderInfoTable,
				String> path_col = new TreeTableColumn<>("Folder");
		TreeTableColumn<FolderInfoTable,
				Integer> folders_col = new TreeTableColumn<>("Folders");
		TreeTableColumn<FolderInfoTable,
				Integer> files_col = new TreeTableColumn<>("Files");
		TreeTableColumn<FolderInfoTable,
				Integer> media_col = new TreeTableColumn<>("Media");
		treeTableView.getColumns().addAll(path_col, folders_col, files_col, media_col);

		setColumnMaxWidth(path_col, 70);
		setColumnMaxWidth(files_col, 8);
		setColumnMaxWidth(folders_col, 8);
		setColumnMaxWidth(media_col, 8);

		path_col.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<FolderInfoTable,
				String>,
				ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<FolderInfoTable,
					String> param) {
				return new ReadOnlyStringWrapper(param.getValue().getValue().getPath());
			}
		});

		folders_col.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<FolderInfoTable,
				Integer>,
				ObservableValue<Integer>>() {
			@Override
			public ObservableValue<Integer> call(TreeTableColumn.CellDataFeatures<FolderInfoTable,
					Integer> param) {
				return new ReadOnlyObjectWrapper<Integer>(param.getValue().getValue().getFolders());
			}
		});

		files_col.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<FolderInfoTable,
				Integer>,
				ObservableValue<Integer>>() {
			@Override
			public ObservableValue<Integer> call(TreeTableColumn.CellDataFeatures<FolderInfoTable,
					Integer> param) {
				return new ReadOnlyObjectWrapper<Integer>(param.getValue().getValue().getFiles());
			}
		});
		media_col.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<FolderInfoTable,
				Integer>,
				ObservableValue<Integer>>() {
			@Override
			public ObservableValue<Integer> call(TreeTableColumn.CellDataFeatures<FolderInfoTable,
					Integer> param) {
				return new ReadOnlyObjectWrapper<Integer>(param.getValue().getValue().getMedia());
			}
		});
		return treeTableView;
	}

	private void setColumnMaxWidth(TreeTableColumn<FolderInfoTable,
			?> treeTableColumn, int procentage) {
		treeTableColumn.setMaxWidth(1F * Integer.MAX_VALUE * procentage);
	}

}
