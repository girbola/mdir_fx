package com.girbola.controllers.main;

import static com.girbola.Main.bundle;
import static com.girbola.Main.conf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.girbola.MDir_Constants;
import com.girbola.Main;
import com.girbola.Scene_NameType;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.fileinfo.FileInfo;
import com.girbola.fileinfo.FileInfo_Utils;
import com.girbola.fxml.conflicttableview.ConflictFile;
import com.girbola.fxml.conflicttableview.ConflictTableViewController;
import com.girbola.fxml.operate.OperateFiles;
import com.girbola.messages.Messages;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class CopyBatch {

	private Model_main model_Main;

	private ObservableList<FileInfo> conflictWithWorkdir_list = FXCollections.observableArrayList();
	private ObservableList<FileInfo> cantCopy_list = FXCollections.observableArrayList();
	private ObservableList<FileInfo> okFiles_list =FXCollections.observableArrayList();
	private ObservableList<FileInfo> list = FXCollections.observableArrayList();

	public CopyBatch(Model_main model_Main) {
		this.model_Main = model_Main;
	}

	private void showConflictFileInfos() {
		Messages.sprintf("showConflictFileInfos: ");
		for (FileInfo fileInfo : conflictWithWorkdir_list) {
			conflictWithWorkdir_list.add(fileInfo);
		}
	}

	private void showCantCopyFileInfos() {
		Messages.sprintf("showCantCopyFileInfos: ");
		for (FileInfo fileInfo : cantCopy_list) {
			cantCopy_list.add(fileInfo);
		}
	}


//	private void handleOKFiles(TableView<FolderInfo> table) {
//		for (FolderInfo folderInfo : table.getItems()) {
//			for (FileInfo fileInfo : folderInfo.getFileInfoList()) {
//				if (!fileInfo.getDestination_Path().isEmpty() && !fileInfo.isCopied()) {
//
//				}
//			}
//		}
//	}

	public void start() {
		handleTable(model_Main.tables().getSorted_table());
		handleTable(model_Main.tables().getSortIt_table());
		if (!conflictWithWorkdir_list.isEmpty()) {
//			showConflictFileInfos();
			showConflictTable(conflictWithWorkdir_list);
		}
//		showCantCopyFileInfos();

//		showOkFileInfos();

		if (!list.isEmpty()) {
//			showConflictTable();
		} else {

			Task<Boolean> task = new OperateFiles(list, true, model_Main, Scene_NameType.MAIN.getType());
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

	public void showConflictTable(ObservableList<FileInfo> list) {
		
		try {
			Parent parent = null;
			FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/conflicttableview/ConflictTableView.fxml"),
					bundle);
			try {
				parent = loader.load();
			} catch (IOException ex) {
				Logger.getLogger(CopyBatch.class.getName()).log(Level.SEVERE, null, ex);
			}

			ConflictTableViewController conflictTableViewController = (ConflictTableViewController) loader
					.getController();
			conflictTableViewController.init(model_Main, list);
			Scene scene_conflictTableView = new Scene(parent);
			scene_conflictTableView.getStylesheets().add(
					Main.class.getResource(conf.getThemePath() + MDir_Constants.MAINSTYLE.getType()).toExternalForm());

			Stage window = new Stage();
			window.setScene(scene_conflictTableView);
			window.show();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void handleTable(TableView<FolderInfo> table) {
		for (FolderInfo folderInfo : table.getItems()) {
			for (FileInfo fileInfo : folderInfo.getFileInfoList()) {
				if (!fileInfo.getDestination_Path().isEmpty() && !fileInfo.isCopied()) {
					/*
					 * 0 if good 1 if conflict with workdir 2 if copying is not possible
					 */
					int status = FileInfo_Utils.checkWorkDir(fileInfo);
					if (status == 0) {
						okFiles_list.add(fileInfo);
						Messages.sprintf(
								"okFiles: " + fileInfo.getDestination_Path() + " isCopied? " + fileInfo.isCopied());
					} else if (status == 1) {
						conflictWithWorkdir_list.add(fileInfo);
						Messages.sprintf(
								"conflicts: " + fileInfo.getDestination_Path() + " isCopied? " + fileInfo.isCopied());
					} else if (status == 2) {
						cantCopy_list.add(fileInfo);
						Messages.sprintf(
								"can't copy: " + fileInfo.getDestination_Path() + " isCopied? " + fileInfo.isCopied());
					}else if (status == 3) {
						// Workdir is not connected
//						Messages.errorSmth(className, message, exception, line, exit);
//						cantCopy_list.add(fileInfo);
						Messages.sprintf(
								"can't copy: " + fileInfo.getDestination_Path() + " isCopied? " + fileInfo.isCopied());
					}  
					else {
						Messages.sprintf("something else?!?!?!: " + fileInfo.getDestination_Path() + " isCopied? "
								+ fileInfo.isCopied());
					}
				}
			}
		}

	}

}
