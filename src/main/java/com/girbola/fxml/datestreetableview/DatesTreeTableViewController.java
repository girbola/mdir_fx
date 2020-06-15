package com.girbola.fxml.datestreetableview;

import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.girbola.Main;
import com.girbola.controllers.main.Model_main;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;

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
		makeTable();
	}

	public void makeTable() {
		/*
		 * 2017 01 02 03 04 - Kalassa 2008 03 04 06 - Tukholmassa
		 */
//		TreeItem<>

		TreeMap<String, List<FileInfo>> filesMap = new TreeMap<>();
		int counter = 0;
		for (FolderInfo folderInfo : model_main.tables().getSorted_table().getItems()) {
			if (!folderInfo.getJustFolderName().isEmpty()) {
				for (FileInfo fileInfo : folderInfo.getFileInfoList()) {
					fileInfo.setEvent(folderInfo.getJustFolderName());
				}
			} else {
				Main.setProcessCancelled(true);
			}
			CreateDateList.addToDateMap(filesMap, folderInfo.getFileInfoList(), TableType.SORTED);

		}
		for (FolderInfo folderInfo : model_main.tables().getSortIt_table().getItems()) {
			CreateDateList.addToDateMap(filesMap, folderInfo.getFileInfoList(), TableType.SORTIT);
		}
		for (Entry<String, List<FileInfo>> entry : filesMap.entrySet()) {
			Messages.sprintf(entry.getKey() + " fileinfo lsit size is: " + entry.getValue().size());
			counter += entry.getValue().size();
		}

		Messages.sprintf("Map Size: " + filesMap.size() + " fileinfos total: " + counter);
	}

	private void getExistsTreeItem(int year) {
		TreeItem<FolderInfo> root = dates_treeTableView.getRoot();
		for (TreeItem<FolderInfo> fooo : root.getChildren()) {

		}
	}

}
