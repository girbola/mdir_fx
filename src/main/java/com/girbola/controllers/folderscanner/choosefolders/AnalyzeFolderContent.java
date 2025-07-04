
package com.girbola.controllers.folderscanner.choosefolders;

import com.girbola.controllers.folderscanner.FolderInfoTable;
import com.girbola.controllers.folderscanner.FolderScanner_Methods;
import com.girbola.controllers.folderscanner.ModelFolderScanner;
import com.girbola.filelisting.ValidatePathUtils;
import common.utils.Comparators;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static com.girbola.messages.Messages.sprintf;


public class AnalyzeFolderContent extends Task<Void> {

	private List<Path> selectedPaths;
	private ModelFolderScanner modelFolderScanner;
	//    private Path rootPath = null;

	public AnalyzeFolderContent(List<Path> selectedPaths, ModelFolderScanner modelFolderScanner) {
		this.selectedPaths = selectedPaths;
		this.modelFolderScanner = modelFolderScanner;
	}

	@Override
	protected Void call() throws Exception {
		sprintf("analyzeFolderContent started: " + selectedPaths.size());
		for (Path selectedFolder : modelFolderScanner.getSelectedDrivesFoldersListObs()) {
			sprintf("getDrivesList is: " + selectedFolder);
		}
		Map<Path, List<Path>> map = new HashMap<>();
		for (Path selectedFolder : modelFolderScanner.getSelectedDrivesFoldersListObs()) {
			List<Path> list = createList(selectedFolder, selectedPaths);
			Comparators.compareInt(list);

			sprintf("rootPath: " + selectedFolder);
			sprintf("First node path would be: " + list.getFirst());
			map.put(list.getFirst(), list);
		}

		CheckBoxTreeItem<FolderInfoTable> root = null;
		for (Map.Entry<Path, List<Path>> entry : map.entrySet()) {
			sprintf("Selected Folders ROOT is: " + entry.getKey());

			if (!FolderScanner_Methods.titledPaneExists(modelFolderScanner.getAnalyzeList_vbox(), entry.getKey())) {
				TreeTableView ttv = createTreeTableView(entry.getKey());
				TitledPane titledPane = new TitledPane(entry.getKey().toString(), ttv);
				root = new CheckBoxTreeItem<>(new FolderInfoTable(entry.getKey().toString()));
				root.setExpanded(true);
				ttv.setRoot(root);

				titledPane.setContent(ttv);

				modelFolderScanner.getAnalyzeList_vbox().getChildren().add(titledPane);
			}

			sprintf("Test root path is false");
			for (Path p : entry.getValue()) {
				if (Files.exists(p)) {
					if (ValidatePathUtils.hasMediaFilesInFolder(p)) {
						TreeItem<FolderInfoTable> cbi = createTreeItemFolderInfo(p.toFile(), modelFolderScanner.getAnalyzeList_selected());
						root.getChildren().add(cbi);
					}
				}
			}

		}
		return null;
	}

	private TreeItem<FolderInfoTable> createTreeItemFolderInfo(File file, List<TreeItem<FolderInfoTable>> selectedList) {
	    TreeItem<FolderInfoTable> treeItem = new TreeItem<>(new FolderInfoTable(file.getAbsolutePath()));
	    CheckBox checkBoxi = new CheckBox();
	    checkBoxi.getStyleClass().add("checkBoxi");
	    checkBoxi.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
	        if (newValue) {
	            sprintf("Selected TreeItem<FolderInfo> cbi: " + treeItem.getValue().getPath());
	            selectedList.add(treeItem);
	        } else {
	            sprintf("Deselected TreeItem<FolderInfo> cbi: " + treeItem.getValue().getPath());
	            selectedList.remove(treeItem);
	        }
	    });
	    treeItem.setGraphic(new HBox(checkBoxi));

	    return treeItem;
	}

	private List<Path> createList(Path rootPath, List<Path> selectedList) {
		sprintf("rootPath: " + rootPath);
		List<Path> found = new ArrayList<>();
		for (Path p : selectedPaths) {
			if (p.getRoot().toString().equals(rootPath.getRoot().toString())) {
				sprintf("found same root! " + p);
				found.add(p);
			}
		}
		return found;
	}

	@SuppressWarnings("unchecked")
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

		path_col.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().getPath()));
		folders_col.setCellValueFactory(param -> new ReadOnlyObjectWrapper<Integer>(param.getValue().getValue().getFolders()));
		files_col.setCellValueFactory(param -> new ReadOnlyObjectWrapper<Integer>(param.getValue().getValue().getFiles()));
		media_col.setCellValueFactory(param -> new ReadOnlyObjectWrapper<Integer>(param.getValue().getValue().getMedia()));

		return treeTableView;
	}

	private void setColumnMaxWidth(TreeTableColumn<FolderInfoTable,
			?> treeTableColumn, int procentage) {
		treeTableColumn.setMaxWidth(1F * Integer.MAX_VALUE * procentage);
	}

}
