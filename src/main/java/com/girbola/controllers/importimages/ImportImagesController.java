/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.importimages;

import com.girbola.controllers.main.Model_main;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.girbola.messages.Messages.sprintf;

public class ImportImagesController {

	private final String ERROR = ImportImagesController.class.getSimpleName();

	private Model_main model_main;
	private Model_importImages model_importImages;

	@FXML
	private Button apply_btn_main;
	@FXML
	private ComboBox<String> event_main;
	@FXML
	private ComboBox<String> location_main;

	@FXML
	private Button start_ok;
	@FXML
	private TitledPane titledPane_demo;
	@FXML
	private HBox topButtons;

	// private ObservableList<Node> visibleNodes =
	// FXCollections.observableArrayList();
	private Timeline timeline = null;

	private Path currentPath;

	@FXML
	private ScrollPane scrollPane;

	@FXML
	private VBox container;
	@FXML
	private MenuItem close;

	@FXML
	private MenuItem view_compact;
	@FXML
	private MenuItem view_full;

	// Needed @FXML DateTimeSelectorController dateTimeSelectorController;
	@FXML
	private DateTimeSelectorController dateTimeSelectorController;

	// private List<FileInfo> fileInfo_list;
	private boolean importer;

	private FolderInfo folderInfo;

	@FXML
	private void apply_btn_main_action(ActionEvent event) {
	}

	@FXML
	private void close_action(ActionEvent event) {
		// dfb;
	}

	@FXML
	private void view_full_action(ActionEvent event) {
		Task<Void> drawImages = new DrawImagesToImportImages_Full(model_importImages.getTheList(), model_importImages, folderInfo,
				container, null);

		Thread drawImages_th = new Thread(drawImages, "drawImages_th");
		sprintf("drawImages_th.getName(): " + drawImages_th.getName());
		drawImages_th.run();

	}

	public void init(Model_main aModel_main, FolderInfo aFolderInfo, boolean importer) {
		sprintf("Initing importimages controller");
		container.getChildren().removeAll();
		this.model_main = aModel_main;
		this.folderInfo = aFolderInfo;
		this.currentPath = Paths.get(this.folderInfo.getFolderPath());
		this.importer = importer;
		container.getChildren().clear();
		List<FileInfo> fileInfo_list = folderInfo.getFileInfoList();
		model_importImages = new Model_importImages(currentPath);
		model_importImages.setScrollPane(scrollPane);
		model_importImages.instantiateRenderVisibleNodes();
		apply_btn_main.setDisable(true);

		Task<Map<String,
				List<FileInfo>>> getList = new CreateDateList(fileInfo_list);
		getList.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				Map<String,
						List<FileInfo>> list = null;
				try {
					list = getList.get();
				} catch (Exception ex) {
					Logger.getLogger(ImportImagesController.class.getName()).log(Level.SEVERE, null, ex);
					Messages.errorSmth(ERROR, "Problem with getting list from task", ex, Misc.getLineNumber(), true);
				}
				model_importImages.setMin_Max_TIMES(list);
				model_importImages.setTheList(list);
				dateTimeSelectorController.init(model_importImages);
				Task<Void> drawImages = new DrawImagesToImportImages_Full(list, model_importImages, folderInfo, container, null);
				drawImages.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
					@Override
					public void handle(WorkerStateEvent event) {
						model_importImages.getRenderVisibleNode().startTimeLine();
					}
				});

				drawImages.setOnCancelled(new EventHandler<WorkerStateEvent>() {
					@Override
					public void handle(WorkerStateEvent event) {
						model_importImages.getRenderVisibleNode().stopTimeLine();
					}
				});
				drawImages.setOnFailed(new EventHandler<WorkerStateEvent>() {

					@Override
					public void handle(WorkerStateEvent event) {
						model_importImages.getRenderVisibleNode().stopTimeLine();
					}
				});
				Thread drawImages_th = new Thread(drawImages, "drawImages_th");
				sprintf("drawImages_th.getName(): " + drawImages_th.getName());
				drawImages_th.run();
			}
		});
		getList.setOnCancelled(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				sprintf("getList.setOnCancelled");
			}
		});

		getList.setOnFailed(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				sprintf("getList.setOnFailed");
			}
		});

		Thread getList_th = new Thread(getList, "getList_th");
		sprintf("getList_th.getName(): " + getList_th.getName());
		getList_th.start();
	}

	@FXML
	private void start_ok_action(ActionEvent event) {
		sprintf("start_ok_action");
		if (container == null) {
			sprintf("start_ok_action container were null");
			return;
		}
		for (Node root : container.getChildren()) {
			// sprintf("root: " + root);
			if (root instanceof TitledPane) {
				HBox hbox = (HBox) ((TitledPane) root).getGraphic();
				if (hbox instanceof HBox) {
					for (Node hboxNode : hbox.getChildren()) {
						// sprintf("----->hboxNode: " + hboxNode);
						if (hboxNode instanceof HBox) {
							for (Node underNode : ((HBox) hboxNode).getChildren()) {
								sprintf("------------->underNode: " + underNode);
								if (underNode instanceof ComboBox) {
									ComboBox cb = (ComboBox) underNode;
									if (cb.getId().contains("location")) {
										sprintf("Location combobox: " + cb.getId());
									} else if (cb.getId().contains("event")) {
										sprintf("Event combobox: " + cb.getId());
									}
								}
							}
						}
					}
				}
			}
		}
	}

	public void stopTimeLine() {
		if (timeline != null) {
			timeline.stop();
		}
	}
}
