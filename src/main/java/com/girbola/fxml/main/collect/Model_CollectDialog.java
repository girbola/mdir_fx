package com.girbola.fxml.main.collect;

import com.girbola.controllers.datefixer.DateFixerModel;
import com.girbola.controllers.main.Model_main;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.fileinfo.FileInfo;
import com.girbola.fileinfo.FileInfo_Event;
import com.girbola.fileinfo.FileInfoUtils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Model_CollectDialog extends DateFixerModel {

	private Model_main model_Main;
	private Model_CollectDialog model_CollectDialog;

	public Model_CollectDialog() {
		super();
	}

	public ObservableList<FileInfo_Event> obs_Events = FXCollections.observableArrayList();
	public ObservableList<FileInfo> obs_Location = FXCollections.observableArrayList();

	/**
	 * 
	 * @param fileInfo_ToFind
	 * @param tabletype
	 */
	public void addToEvent(FileInfo fileInfo_ToFind, String tabletype) {
		if (tabletype.equals(TableType.SORTED.getType())) {
			if (fileInfo_ToFind.getEvent().isBlank()) {
				fileInfo_ToFind.setEvent(FileInfoUtils.getFolderName(fileInfo_ToFind));
			}
		}
		for (FileInfo_Event fileInfo : obs_Events) {
			if (fileInfo.getEvent().equals(fileInfo_ToFind.getEvent())) {
				return;
			}
		}
		obs_Events.add(new FileInfo_Event(fileInfo_ToFind));
	}

	public void addToLocation(FileInfo fileInfo_ToFind) {

		for (FileInfo fileInfo : obs_Location) {
			if (fileInfo.getLocation().equals(fileInfo_ToFind.getLocation())) {
				return;
			}
		}
		obs_Location.add(fileInfo_ToFind);
	}

	public void init(Model_main model_Main, Model_CollectDialog model_CollectDialog) {
		this.model_Main = model_Main;
		this.model_CollectDialog = model_CollectDialog;
	}

}
