package com.girbola.media.collector;

import com.girbola.controllers.main.Tables;
import com.girbola.controllers.main.tables.Events;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import common.utils.date.DateUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class Collector {

	private TreeMap<LocalDate, List<FileInfo>> map = new TreeMap<>();

	public ObservableList<FileInfo> collect(TableView<FolderInfo> tableView_sorted,
			TableView<FolderInfo> tableView_sortIt) {
		ObservableList<FileInfo> obs = FXCollections.observableArrayList();

		for (FolderInfo folderInfo_sorted : tableView_sorted.getItems()) {
			LocalDateTime start = DateUtils.parseLocalDateTimeFromString(folderInfo_sorted .getMaxDate()).minusDays(1);
			LocalDateTime end = DateUtils.parseLocalDateTimeFromString(folderInfo_sorted .getMinDate()).plusDays(1);

			// (date.isAfter(ldt_start) && date.isBefore(ldt_end)) {
			for (FolderInfo folderInfo_sortIt : tableView_sortIt.getItems()) {
				for (FileInfo fileInfo : folderInfo_sortIt.getFileInfoList()) {
					LocalDateTime date_ld = DateUtils.longToLocalDateTime(fileInfo.getDate());
					if (date_ld.isAfter(start) && date_ld.isBefore(end)) {
						add(date_ld.toLocalDate(), fileInfo);
					}
				}
			}
		}
		return obs;
	}

	private void add(LocalDate date, FileInfo fileInfo) {
		if (map.containsKey(date)) {
			for (Entry<LocalDate, List<FileInfo>> entry : map.entrySet()) {
				if (entry.getKey().equals(date)) {
					if (!fileInfoExists(fileInfo, entry.getValue())) {
						entry.getValue().add(fileInfo);
						// Messages.sprintf("FileInfo not exists");
					}
				}
			}
		} else {
			List<FileInfo> list = new ArrayList<>();
			list.add(fileInfo);
			map.put(date, list);
		}
	}

	private boolean fileInfoExists(FileInfo fileInfo, List<FileInfo> list) {
		for (FileInfo fi : list) {
			if (fi.equals(fileInfo)) {
				return true;
			}
		}
		return false;
	}

	public void listMap(Tables tables) {
		ObservableList<Events> events_obs = FXCollections.observableArrayList();

		for (Entry<LocalDate, List<FileInfo>> entry : map.entrySet()) {
			LocalDate localDateToFind = entry.getKey();
			List<FileInfo> fileInfo_temp = new ArrayList<>();
			for (FolderInfo folderInfo : tables.getSorted_table().getItems()) {
				for (FileInfo fileInfo : folderInfo.getFileInfoList()) {
					if (!fileInfo.isIgnored()) {
						LocalDate start = DateUtils.longToLocalDateTime(fileInfo.getDate()).toLocalDate().minusDays(1);
						LocalDate end = DateUtils.longToLocalDateTime(fileInfo.getDate()).toLocalDate().plusDays(1);
						if (localDateToFind.isBefore(end) && localDateToFind.isAfter(start)) {

							fileInfo_temp.add(fileInfo);
//									listOfPossibleFolders.add(folderInfo);

							Messages.sprintf("FolderInfo path found: " + folderInfo.getFolderPath() + " date: "
									+ localDateToFind);
						}
					}
				}
//				TODO keksi jokin toinen kuin folderinfo jotta saa lajiteltua päiväykset ja lisättyä kansion nimen ehdotus eventin mukaan
//				folderInfoList.add(new FolderInfo())
			}
			Messages.sprintf("Date: " + entry.getKey() + "\n" + "=====================" + entry.getValue().size());

//			}
//			if (!listOfPossibleFolders.isEmpty()) {
//				Dialog<ButtonType> changesDialog = Dialogs.createDialog_YesNo(Main.sceneManager.getWindow(),
//						" There were possible foldernames. Choose oneee\n");
//				changesDialog.setWidth(500);
//				changesDialog.setHeight(500);
//				changesDialog.setHeaderText("JEEEEE");
//				ListView<String> list = new ListView<>();
//				changesDialog.getDialogPane().prefWidth(500);
//				changesDialog.getDialogPane().minWidth(500);
//				changesDialog.getDialogPane().maxWidth(500);
//				changesDialog.getDialogPane().prefHeight(500);
//				changesDialog.getDialogPane().minHeight(500);
//				changesDialog.getDialogPane().maxHeight(500);
//				
//				VBox vbox = new VBox(list, new Label("JOOOOO"));
//				vbox.prefHeight(450);
//				vbox.minHeight(450);
//				vbox.maxHeight(450);
//				changesDialog.getDialogPane().getChildren().add(vbox);
//				ObservableList<String> list_obs = FXCollections.observableArrayList();
//				list.setItems(list_obs);
//				for (FolderInfo folderInfo : listOfPossibleFolders) {
//					Messages.sprintf("FolderInfo path found: " + folderInfo.getFolderPath());
//					list_obs.add(folderInfo.getFolderPath());
//				}
//				Optional<ButtonType> result = changesDialog.showAndWait();
//				if (result.get().getButtonData().equals(ButtonBar.ButtonData.YES)) {
//					for (FolderInfo folderInfo : listOfPossibleFolders) {
//						Messages.sprintf("FolderInfo path found: " + folderInfo.getFolderPath());
//					}
//					listOfPossibleFolders.clear();
//					break;
//				}
		}

	}

	public Map<LocalDate, List<FileInfo>> getMap() {
		return map;
	}

	public void collectAll(Tables tables) {
		collect(tables.getSorted_table(), tables.getSortIt_table());
		collect(tables.getSorted_table(), tables.getSorted_table());
			
		// collect(tables.getAsItIs_table());

	}

}
