package com.girbola.controllers.main;

import com.girbola.SceneNameType;
import com.girbola.fileinfo.FileInfo;
import com.girbola.fxml.operate.OperateFiles;

import java.util.ArrayList;
import java.util.List;

public class CopyBatch {

	private ModelMain model_Main;

//	private ObservableList<FileInfo> conflictWithWorkdir_list = FXCollections.observableArrayList();
//	private ObservableList<FileInfo> cantCopy_list = FXCollections.observableArrayList();
//	private ObservableList<FileInfo> okFiles_list = FXCollections.observableArrayList();
//	private ObservableList<FileInfo> list = FXCollections.observableArrayList();

	public CopyBatch(ModelMain model_Main) {
		this.model_Main = model_Main;
	}



	public void start() {
		List<FileInfo> filesReadyToCopy = new ArrayList<>();
		CheckTableContent sorted_TableContent = new CheckTableContent(model_Main.tables().getSorted_table(), model_Main);
		sorted_TableContent.checkTables();
		filesReadyToCopy.addAll(sorted_TableContent.getFileInfoList());
		
		CheckTableContent sortIt_TableContent = new CheckTableContent(model_Main.tables().getSortIt_table(), model_Main);
		sortIt_TableContent .checkTables();
		filesReadyToCopy.addAll(sortIt_TableContent.getFileInfoList());

		if (!filesReadyToCopy.isEmpty()) {
			OperateFiles operateFiles = new OperateFiles(filesReadyToCopy, true, model_Main, SceneNameType.MAIN.getType());
		}
	}
}
