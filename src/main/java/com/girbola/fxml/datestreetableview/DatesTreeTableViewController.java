package com.girbola.fxml.datestreetableview;

import com.girbola.Main;
import com.girbola.controllers.main.Model_main;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.controllers.main.tables.FolderInfo_Utils;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import common.utils.date.CreateDateList;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.control.TreeTableView;
import javafx.util.Callback;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

public class DatesTreeTableViewController {

    TreeMap<String, List<FileInfo>> filesMap = new TreeMap<>();

    private Model_main model_main;
    //@formatter:off
   @FXML private TreeTableView<TreeFolderInfo> dates_treeTableView;
   @FXML private TreeTableColumn<TreeFolderInfo, String> folders_ttc;
   @FXML private TreeTableColumn<TreeFolderInfo, String> rootMonth_ttc;
   @FXML private TreeTableColumn<TreeFolderInfo, String> dayMonth_ttc;
   @FXML private TreeTableColumn<TreeFolderInfo, String> possibleEvents_ttc;
   @FXML private TreeTableColumn rootDay_ttc;
	//@formatter:on
    private TreeItem<TreeFolderInfo> root;

    public void init(Model_main model_main) {
        this.model_main = model_main;

        root = new TreeItem<>();

        dates_treeTableView.setRoot(root);

        folders_ttc.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<TreeFolderInfo, String>, ObservableValue<String>>() {

            @Override
            public ObservableValue<String> call(CellDataFeatures<TreeFolderInfo, String> param) {
                return param.getValue().getValue().getYear();
            }
        });

        rootMonth_ttc.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<TreeFolderInfo, String>, ObservableValue<String>>() {

            @Override
            public ObservableValue<String> call(CellDataFeatures<TreeFolderInfo, String> param) {
                return param.getValue().getValue().getMonth();
            }
        });
        dayMonth_ttc.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<TreeFolderInfo, String>, ObservableValue<String>>() {

            @Override
            public ObservableValue<String> call(CellDataFeatures<TreeFolderInfo, String> param) {
                return param.getValue().getValue().getDay();
            }
        });
        possibleEvents_ttc.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<TreeFolderInfo, String>, ObservableValue<String>>() {

            @Override
            public ObservableValue<String> call(CellDataFeatures<TreeFolderInfo, String> param) {
                return param.getValue().getValue().getYear();
            }
        });

//		return new ReadOnlyIntegerWrapper(DateUtils.parseLocalDateTimeFromString(param.getValue().getValue().getMinDate()).getYear());
        makeTable();
    }

    public void makeTable() {
        /*
         * 2017 01 02 03 04 - Kalassa 2008 03 04 06 - Tukholmassa
         */
//		TreeItem<>

        int counter = 0;
        for (FolderInfo folderInfo : model_main.tables().getSorted_table().getItems()) {
            if (!folderInfo.getJustFolderName().isEmpty()) {
//				TreeItem<FolderInfo> treeItem = new TreeItem<>();
//				root.getChildren().add(treeItem);
//				for (FileInfo fileInfo : folderInfo.getFileInfoList()) {
//					fileInfo.setEvent(folderInfo.getJustFolderName());
//				}
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
            Path destPath = Paths.get(Main.conf.getWorkDir() + File.separator + entry.getKey());
            FolderInfo folderInfo = new FolderInfo(destPath);
            List<FileInfo> listFileInfos = entry.getValue();
            FolderInfo_Utils.addFileInfoList(folderInfo, listFileInfos);
            FolderInfo_Utils.calculateFileInfoStatuses(folderInfo);
            TreeItem<TreeFolderInfo> fold = new TreeItem<>();
            root.getChildren().add(fold);

        }

        Messages.sprintf("Map Size: " + filesMap.size() + " fileinfos total: " + counter);
    }

//	private void getExistsTreeItem(int year) {
//		TreeItem<FolderInfo> root = dates_treeTableView.getRoot();
//		for (TreeItem<FolderInfo> fooo : root.getChildren()) {
//
//		}
//	}

}
