package com.girbola.controllers.main;

import com.girbola.Main;
import com.girbola.controllers.loading.LoadingProcessTask;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.controllers.main.tables.tabletype.FolderInfoStateType;
import com.girbola.fileinfo.FileInfo;
import com.girbola.utils.FileInfoUtils;
import com.girbola.filelisting.GetRootFiles;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.scene.control.TableView;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class CheckSelectedRowForChanges extends Task<Boolean> {

	private TableView<FolderInfo> tableView;
	private SimpleBooleanProperty changed = new SimpleBooleanProperty();
	private final String ERROR = CheckSelectedRowForChanges.class.getName();
	private ModelMain model_main;
	private LoadingProcessTask loadingProcess;

	public CheckSelectedRowForChanges(TableView<FolderInfo> tableView, ModelMain aModel_main,
			LoadingProcessTask loadingProcess) {
		super();
		this.tableView = tableView;
		this.model_main = aModel_main;
		this.loadingProcess = loadingProcess;
	}

	@Override
	protected Boolean call() {
		for (FolderInfo folderInfo : tableView.getSelectionModel().getSelectedItems()) {
			List<Path> fileList = null;
			FutureTask<List<Path>> getList = new FutureTask(new GetFolderList(folderInfo));
			Platform.runLater(getList);
			try {
				fileList = getList.get();
			} catch (InterruptedException | ExecutionException e1) {
				Messages.errorSmth(ERROR,
						"Something went wrong while getting list from path: " + folderInfo.getFolderPath(), e1,
						Misc.getLineNumber(), true);
				e1.printStackTrace();
			}
			if (fileList.isEmpty()) {
				Messages.sprintfError(this.getClass().getName() + " List were empty!");
				return null;
			} else {
				Messages.sprintf(this.getClass().getName() + " List size were: " + fileList.size());
			}

			List<FileInfo> listToRemove = new ArrayList<>(folderInfo.getFileInfoList());
			for (Path file : fileList) {
				FileInfo fileInfoToCompare = FileInfoUtils.findFileInFolderInfo(file, folderInfo);
				if (fileInfoToCompare == null) {
					Messages.sprintf("New file appeared!");
					FileInfo fileInfo = null;
					try {
						fileInfo = FileInfoUtils.createFileInfo(file);
						folderInfo.getFileInfoList().add(fileInfo);
						changed.set(true);
					} catch (IOException e) {
						e.printStackTrace();
						Messages.errorSmth(ERROR, "Something went wrong with saving database", e, Misc.getLineNumber(),
								true);
						return Boolean.FALSE;
					}
				} else {
					listToRemove.remove(fileInfoToCompare);
				}
			}

			if (!listToRemove.isEmpty()) {
				folderInfo.getFileInfoList().removeAll(listToRemove);
				changed.set(true);
				folderInfo.setState(FolderInfoStateType.CHANGED.getType());
				Messages.sprintf("cleanFileInfoList were true");
			}
		}
		return Boolean.TRUE;
	}

	@Override
	protected void succeeded() {
		super.succeeded();
		Messages.sprintf("Checking selected row for changes were succeeded");
		Task<Boolean> cleanTask = new FolderInfoStateCheck(tableView, loadingProcess);
		cleanTask.setOnSucceeded((WorkerStateEvent event) -> {
			boolean updated = false;
			try {
				updated = cleanTask.get();
				if (updated) {
					TableUtils.refreshTableContent(tableView);
					Task<Integer> saveTablesToDatabases = new SaveTablesToFolderInfoDatabases(model_main, Main.scene_Switcher.getWindow(), loadingProcess, true);
					Thread thread = new Thread(saveTablesToDatabases, "Saving data Thread");
					thread.start();
	//				model_main.saveTablesToDatabases();
					//loadingProcess.closeStage();
				} else {
					Messages.sprintf("No changes");
					loadingProcess.closeStage();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		cleanTask.setOnFailed((WorkerStateEvent event) -> {
			Messages.sprintf("Something went wrong with updating the content");
			Messages.errorSmth(ERROR, "Something went wrong with updating the content", null, Misc.getLineNumber(), true);
			loadingProcess.closeStage();
		});
		cleanTask.setOnCancelled((WorkerStateEvent event) -> {
			Messages.sprintf("Updating tableview's has been cancelled");
			loadingProcess.closeStage();
		});
		Thread thread = new Thread(cleanTask, "Cleaning task thread");
		thread.start();
	}

	@Override
	protected void cancelled() {
		super.cancelled();
		Messages.sprintf("Checking selected row for changes were cancelled");
		Platform.runLater(() -> {
			loadingProcess.closeStage();
		});

	}

	@Override
	protected void failed() {
		super.failed();
		Messages.sprintfError("Checking selected row for changes were failed");
		Platform.runLater(() -> {
			loadingProcess.closeStage();
		});
	}

	class GetFolderList implements Callable<List<Path>> {

		private FolderInfo folderInfo;

		public GetFolderList(FolderInfo folderInfo) {
			this.folderInfo = folderInfo;
		}

		@Override
		public List<Path> call() throws Exception {

			List<Path> list = null;
			try {
				list = GetRootFiles.getRootFiles(Paths.get(folderInfo.getFolderPath()));
			} catch (IOException ex) {
				Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
			}
			return list;
		}

	}

}
