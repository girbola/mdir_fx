package com.girbola.fxml.datestreetableview;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.girbola.controllers.main.Model_main;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.fileinfo.FileInfo;

import common.utils.date.CreateDateList;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;

public class DatesTreeTableViewController {

	private Model_main model_main;
	@FXML
	private TreeTableView<FolderInfo> dates_treeTableView;

	@FXML
	private TreeTableColumn<FolderInfo, Integer> rootYear_ttc;
	@FXML
	private TreeTableColumn<List<FileInfo>, Integer> rootMonth_ttc;
	@FXML
	private TreeTableColumn rootDay_ttc;

	public void init(Model_main model_main) {
		this.model_main = model_main;
	}

	public void makeTable() {
		/*
		 * 2017 01 02 03 04 - Kalassa 2008 03 04 06 - Tukholmassa
		 */
//		TreeItem<>

		Map<String, List<FileInfo>> filesMap = new HashMap<>();

		for (FolderInfo folderInfo : model_main.tables().getSorted_table().getItems()) {
			CreateDateList.addToDateMap(filesMap, folderInfo.getFileInfoList(), TableType.SORTED);
		}
		for (FolderInfo folderInfo : model_main.tables().getSortIt_table().getItems()) {
			CreateDateList.addToDateMap(filesMap, folderInfo.getFileInfoList(), TableType.SORTIT);
		}

	}

	private void getExistsTreeItem(int year) {
		TreeItem<FolderInfo> root = dates_treeTableView.getRoot();
		for (TreeItem<FolderInfo> fooo : root.getChildren()) {

		}
	}

}
