package com.girbola.media.collector;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.girbola.controllers.main.Tables;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;

import java.util.TreeMap;

import common.utils.date.DateUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

public class Collector {

	private TreeMap<LocalDate, List<FileInfo>> map = new TreeMap<>();

	public ObservableList<FileInfo> collect(TableView<FolderInfo> table) {
		ObservableList<FileInfo> obs = FXCollections.observableArrayList();

		for (FolderInfo table_v : table.getItems()) {
			LocalDateTime max = DateUtils.parseLocalDateTimeFromString(table_v.getMaxDate()).plusDays(1);
			LocalDateTime min = DateUtils.parseLocalDateTimeFromString(table_v.getMinDate()).minusDays(1);

			for (FileInfo fileInfo : table_v.getFileInfoList()) {
				LocalDateTime date_ld = DateUtils.longToLocalDateTime(fileInfo.getDate());
				if (date_ld.isBefore(max) && date_ld.isAfter(min)) {
					add(date_ld.toLocalDate(), fileInfo);
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

	public void listMap() {
		for (Entry<LocalDate, List<FileInfo>> entry : map.entrySet()) {
			Messages.sprintf("Date: " + entry.getKey() + "\n" + "=====================" + entry.getValue().size());
		}
	}

	public Map<LocalDate, List<FileInfo>> getMap() {
		return map;
	}

	public void collectAll(Tables tables) {
		collect(tables.getSorted_table());
		// collect(tables.getSortIt_table());
		// collect(tables.getAsItIs_table());

	}

}
