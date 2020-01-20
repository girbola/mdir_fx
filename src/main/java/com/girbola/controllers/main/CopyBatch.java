package com.girbola.controllers.main;

import static com.girbola.Main.bundle;
import static com.girbola.Main.conf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.girbola.Main;
import com.girbola.controllers.datefixer.DateFixer;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.fileinfo.FileInfo;
import com.girbola.fileinfo.FileInfo_Utils;
import com.girbola.fxml.conflicttableview.ConflictTableViewController;
import com.girbola.messages.Messages;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class CopyBatch {

	private Model_main model_Main;

	public CopyBatch(Model_main model_Main) {
		this.model_Main = model_Main;
	}

	List<FileInfo> conflictWithWorkdir_list = new ArrayList<>();
	List<FileInfo> cantCopy_list = new ArrayList<>();
	List<FileInfo> okFiles_list = new ArrayList<>();

	private void showConflictFileInfos() {
		Messages.sprintf("showConflictFileInfos: ");
//		for (FileInfo fileInfo : conflictWithWorkdir) {
//			fileInfo.getWorkDir() + fileInfo.getDestination_Path();
//		}
		boolean userChoise = DialogShow(conflictWithWorkdir_list);
		if (userChoise) {
			// Do something
		} else {
			// Do something
		}

	}

	private boolean DialogShow(List<FileInfo> conflictWithWorkdir2) {

		return false;
	}

	private void showCantCopyFileInfos() {
		Messages.sprintf("showCantCopyFileInfos: ");

		for (FileInfo fileInfo : cantCopy_list) {

		}

		boolean userChoise = DialogShow(cantCopy_list);
		if (userChoise) {
			// Do something
		} else {
			// Do something
		}
	}

	private void showOkFileInfos() {
		Messages.sprintf("showCantCopyFileInfos: ");

		for (FileInfo fileInfo : okFiles_list) {

		}

	}

	public void start() {

		handleTable(model_Main.tables().getSorted_table());
		handleTable(model_Main.tables().getSortIt_table());
		showConflictFileInfos();
	}

	public void showConflictTable() {
		// FXMLLoader fxml
		try {
			Parent parent = null;
			FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/conflicttableview/ConflictTableView.fxml"),
					bundle);
			try {
				parent = loader.load();
			} catch (IOException ex) {
				Logger.getLogger(DateFixer.class.getName()).log(Level.SEVERE, null, ex);
			}

			ConflictTableViewController conflictTableViewController = (ConflictTableViewController) loader
					.getController();
			conflictTableViewController.init(model_Main);
			Scene scene_conflictTableView = new Scene(parent);
			scene_conflictTableView.getStylesheets().add(Main.class.getResource(conf.getThemePath() + "mainStyle.css").toExternalForm());

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
					} else {
						Messages.sprintf("something else?!?!?!: " + fileInfo.getDestination_Path() + " isCopied? "
								+ fileInfo.isCopied());
					}
				}
			}
		}

	}

}
