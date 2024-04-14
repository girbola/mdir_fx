/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.folderscanner.choosefolders;

import com.girbola.controllers.folderscanner.Model_folderScanner;
import com.girbola.controllers.main.SelectedFolderScanner;
import com.girbola.filelisting.ValidatePathUtils;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.DosFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.girbola.messages.Messages.sprintf;

/**
 * FXML Controller class
 *
 * @author Marko Lokka
 */
public class ChooseFoldersController {

	// private ObservableList<Path> rootFolder_obs;
	private Model_folderScanner model_folderScanner;
	private SelectedFolderScanner selection_FolderScanner;
	private Stage inherited_stage;
	private Scene inherited_scene;

	private List<Path> selected = new ArrayList<>();
	// private FolderScannerController folderScannerController;
	private TreeItem<Path> root = new TreeItem<>(Paths.get(System.getProperty("user.home")));

	@FXML
	private TreeView<Path> chooseFolders_treeView;
	@FXML
	private Button ok;
	@FXML
	private Button cancel;

	@FXML
	private void cancel_action(ActionEvent event) {
		inherited_stage.setScene(inherited_scene);
	}

	public void init(Model_folderScanner model_folderScanner, SelectedFolderScanner selection_FolderScanner) {
		this.model_folderScanner = model_folderScanner;
		this.selection_FolderScanner = selection_FolderScanner;

		chooseFolders_treeView.setRoot(root);
		root.setExpanded(true);

		chooseFolders_treeView.setShowRoot(true);
		chooseFolders_treeView.setCellFactory(CheckBoxTreeCell.<Path>forTreeView());

	}

	@FXML
	private void ok_action(ActionEvent event) {
		sprintf("Stage ok_action pressed");

		inherited_stage.setScene(inherited_scene);
		model_folderScanner.getAnalyzeList_vbox().getChildren().clear();

		Task<Void> analyze = new AnalyzeFolderContent(selected, model_folderScanner);
		analyze.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				sprintf(" analyze.setOnSucceeded");
			}
		});
		analyze.setOnFailed(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				sprintf(" analyze.setOnFailed");
			}
		});
		analyze.setOnCancelled(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				sprintf(" analyze.setOnCancelled");
			}
		});

		Thread analyze_th = new Thread(analyze, "analyze_th");
		sprintf("analyze_th.getName(): " + analyze_th.getName());
		analyze_th.run();

		// new Thread(analyze).run();
	}

	public void loadTreeView() {
		sprintf("loadTreeview started: ");
		for (Path selectedFolder : model_folderScanner.getSelectedDrivesFoldersList_obs()) {
			sprintf("loadTreeView is: " + selectedFolder);
			CheckBoxTreeItem<Path> tree = createCheckBoxTreeItem(selectedFolder);
			createTreeView(selectedFolder, tree);
		}
	}

	private void createTreeView(Path dbPath, TreeItem<Path> tree) {
		List<TreeItem<Path>> dirs = new ArrayList<>();
		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(dbPath)) {
			for (Path path : directoryStream) {
				if (Files.isDirectory(path)) {
					sprintf("Is dir: " + path);
					DosFileAttributes dfa = Files.readAttributes(path, DosFileAttributes.class);
					if (!Files.isHidden(path) && Files.isReadable(path) && !dfa.isSystem()
							&& !ValidatePathUtils.isInSkippedFolderList(path)) {
						System.out.println("Gonna create new subDir treeview");
						CheckBoxTreeItem<Path> subDirectory = createCheckBoxTreeItem(path);
						getSubLeafs(path, subDirectory);
						dirs.add(subDirectory);
					}
				}
			}

			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					tree.getChildren().addAll(dirs);
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}

		tree.setExpanded(true);
		chooseFolders_treeView.setRoot(tree);
		// chooseFolders_treeView.setShowRoot(false);
	}

	private void getSubLeafs(Path subPath, TreeItem<Path> parent) {
		if (!Files.isDirectory(subPath)) {
			return;
		}
		try {
			if (Files.isHidden(subPath)) {
				return;
			}
			if (!Files.isReadable(subPath)) {
				return;
			}
			DosFileAttributes dfa = Files.readAttributes(subPath, DosFileAttributes.class);
			if (dfa.isSystem()) {
				return;
			}
			if (dfa.isHidden()) {
				return;
			}
			if (ValidatePathUtils.isInSkippedFolderList(subPath)) {
				return;
			}
		} catch (IOException ex) {
			Logger.getLogger(ChooseFoldersController.class.getName()).log(Level.SEVERE, null, ex);
		}

		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(subPath)) {
			for (Path subDir : directoryStream) {
				// explicit search for files because we dont want to get sub-sub-directories
				if (Files.isDirectory(subDir)) {
					sprintf("dir found at subtree: " + subDir);
					CheckBoxTreeItem<Path> subLeafs = createCheckBoxTreeItem(subDir);

					parent.getChildren().add(subLeafs);
					getSubLeafs(subDir, subLeafs);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setStage(Stage inherited_stage) {
		this.inherited_stage = inherited_stage;
	}

	public void setScene(Scene inherited_scene) {
		this.inherited_scene = inherited_scene;
	}

	private CheckBoxTreeItem<Path> createCheckBoxTreeItem(Path path) {
		CheckBoxTreeItem<Path> cti = new CheckBoxTreeItem<>(path);
		cti.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue) {
					selected.add(path);
				} else {
					selected.remove(path);
				}
			}
		});
		return cti;
	}

}
