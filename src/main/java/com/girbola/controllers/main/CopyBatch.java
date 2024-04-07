package com.girbola.controllers.main;

import com.girbola.SceneNameType;
import com.girbola.fileinfo.FileInfo;
import com.girbola.fxml.operate.OperateFiles;
import com.girbola.messages.Messages;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

import java.util.ArrayList;
import java.util.List;

public class CopyBatch {

	private Model_main model_Main;

//	private ObservableList<FileInfo> conflictWithWorkdir_list = FXCollections.observableArrayList();
//	private ObservableList<FileInfo> cantCopy_list = FXCollections.observableArrayList();
//	private ObservableList<FileInfo> okFiles_list = FXCollections.observableArrayList();
//	private ObservableList<FileInfo> list = FXCollections.observableArrayList();

	public CopyBatch(Model_main model_Main) {
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
			Task<Boolean> task = new OperateFiles(filesReadyToCopy, true, model_Main, SceneNameType.MAIN.getType());
			task.setOnCancelled(new EventHandler<WorkerStateEvent>() {

				@Override
				public void handle(WorkerStateEvent event) {
					Messages.sprintf("ConflictTable copy cancelled");
				}
			});
			task.setOnFailed(new EventHandler<WorkerStateEvent>() {

				@Override
				public void handle(WorkerStateEvent event) {
					Messages.sprintf("ConflictTable copy failed");
				}
			});
			task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

				@Override
				public void handle(WorkerStateEvent event) {
					Messages.sprintf("ConflictTable copy Succeeded");
				}
			});
			new Thread(task).start();
		}
	}
}
