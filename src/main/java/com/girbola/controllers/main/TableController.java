/*
 @(#)Copyright:  Copyright (c) 2012-2020 All right reserved.
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.main;

import static com.girbola.Main.bundle;
import static com.girbola.messages.Messages.sprintf;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.girbola.configuration.GUIPrefs;
import com.girbola.controllers.datefixer.GUI_Methods;
import com.girbola.controllers.loading.LoadingProcess_Task;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;

import common.utils.FileUtils;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class TableController {
//@formatter:off
	private Model_main model;

	private final String ERROR = TableController.class.getSimpleName();

	private ObservableList<FolderInfo> data_obs = FXCollections.observableArrayList();

	@FXML
	private TextField tableDescription_tf;
	@FXML
	private Button select_all_btn;
	@FXML
	private HBox buttons_hbox;
	@FXML
	private Button hide_btn;
	@FXML
	private ImageView hide_btn_iv;
	@FXML
	private Button updateFolderInfo_btn;
	@FXML
	private Button select_bad_btn;
	@FXML
	private Button select_good_btn;
	@FXML
	private Button select_invert_btn;
	@FXML
	private Button select_none_btn;
	@FXML
	private Button select_dateDifference;
	@FXML
	private TableColumn<FolderInfo, Double> dateDifference_ratio_col;
	@FXML
	private TableColumn<FolderInfo, Boolean> connected_col;
	@FXML
	private TableView<FolderInfo> table;
	@FXML
	private TableColumn<FolderInfo, Integer> badFiles_col;
	@FXML
	private TableColumn<FolderInfo, Integer> folderFiles_col;
	@FXML
	private TableColumn<FolderInfo, String> fullPath_col;
	@FXML
	private TableColumn<FolderInfo, Integer> image_col;
	@FXML
	private TableColumn<FolderInfo, String> justFolderName_col;
	@FXML
	private TableColumn<FolderInfo, Integer> copied_col;
	@FXML
	private TableColumn<FolderInfo, String> maxDates_col;
	@FXML
	private TableColumn<FolderInfo, Integer> media_col;
	@FXML
	private TableColumn<FolderInfo, String> minDate_col;
	@FXML
	private TableColumn<FolderInfo, Integer> raw_col;
	@FXML
	private TableColumn<FolderInfo, Long> size_col;
	@FXML
	private TableColumn<FolderInfo, Integer> status_col;
	@FXML
	private TableColumn<FolderInfo, Integer> suggested_col;
	@FXML
	private TableColumn<FolderInfo, Integer> video_col;
	@FXML
	private TableColumn<FolderInfo, String> dateFix_col;

	//@formatter:on
	@FXML
	private void checkChanges_action(ActionEvent event) {
		Task<Void> checkForChanges = new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				for (FolderInfo folderInfo : table.getSelectionModel().getSelectedItems()) {

					DirectoryStream<Path> list = null;
					try {
						list = Files.newDirectoryStream(Paths.get(folderInfo.getFolderPath()),
								FileUtils.filter_directories);
					} catch (IOException ex) {
						Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
					}
					List<Path> listOfFiles = new ArrayList<>();
					for (Path p : list) {
						listOfFiles.add(p);
					}
					if (listOfFiles.size() != folderInfo.getFolderFiles()) {
						folderInfo.setState("Folder content changed!");
					} else {
						folderInfo.setState("Nothing new :(");
					}
				}
				return null;
			}

		};
		new Thread(checkForChanges).start();
	}

	@FXML
	private void updateFolderInfo_btn_action(ActionEvent event) {
		ExecutorService exec = Executors.newSingleThreadExecutor();
		for (FolderInfo folderInfo : model.tables().getSortIt_table().getSelectionModel().getSelectedItems()) {
			UpdateFolderInfoContent up = new UpdateFolderInfoContent(folderInfo);
			up.setOnCancelled(new EventHandler<WorkerStateEvent>() {

				@Override
				public void handle(WorkerStateEvent event) {
					Messages.sprintf("Updating folderinfo cancelled: " + folderInfo.getFolderPath());
				}
			});
			up.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

				@Override
				public void handle(WorkerStateEvent event) {
					Messages.sprintf("Updating folderinfo succeeded: " + folderInfo.getFolderPath());
				}
			});
			up.setOnFailed(new EventHandler<WorkerStateEvent>() {

				@Override
				public void handle(WorkerStateEvent event) {
					Messages.sprintf("Updating folderinfo failed: " + folderInfo.getFolderPath());
				}
			});
			exec.submit(up);
		}
	}

	@FXML
	private void reload_all_action(ActionEvent event) {
		sprintf("Reload All");
		Stage stage = (Stage) updateFolderInfo_btn.getScene().getWindow();
		LoadingProcess_Task lpt = new LoadingProcess_Task();
		//
		Task<Void> updateTableValuesUsingFileInfo_task = new CreateFileInfoRow(model, table);
		updateTableValuesUsingFileInfo_task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				sprintf("updateTableValuesFileInfo done successfully");
				lpt.closeStage();
			}
		});

		updateTableValuesUsingFileInfo_task.setOnCancelled(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				lpt.closeStage();
				Messages.warningText("Creating file info cancelled");
			}
		});
		updateTableValuesUsingFileInfo_task.setOnFailed(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				lpt.closeStage();
				Messages.errorSmth(ERROR, "", null, Misc.getLineNumber(), true);
			}
		});
		lpt.setTask(updateTableValuesUsingFileInfo_task);
		Thread thread = new Thread(updateTableValuesUsingFileInfo_task, "Reloading Thread");
		thread.start();
	}

	@FXML
	private void select_all_btn_action(ActionEvent event) {
		model.buttons().select_all_Table(table);
	}

	@FXML
	private void hide_btn_action(ActionEvent event) {
		model.tables().getHideButtons().hide_show_table(hide_btn, TableType.SORTED);
	}

	@FXML
	private void reload_btn_action(ActionEvent event) {

		// model.getTables().updateFolderInfoFileInfo(table);
	}

	@FXML
	private void select_bad_btn_action(ActionEvent event) {
		model.buttons().select_bad_Table(table);
	}

	@FXML
	private void select_dateDifference_action(ActionEvent event) {
		model.buttons().select_dateDifference_Table(table);
	}

	@FXML
	private void select_good_btn_action(ActionEvent event) {
		model.buttons().select_good_Table(table);
	}

	@FXML
	private void select_invert_btn_action(ActionEvent event) {
		model.buttons().select_invert_Table(table);
	}

	@FXML
	private void select_none_btn_action(ActionEvent event) {
		model.buttons().select_none_Table(table);
	}

	public TableView<FolderInfo> getTable() {
		return this.table;
	}

	public HBox getButtons_HBOX() {
		return this.buttons_hbox;
	}

	public void init(Model_main model, String tableName, String tableType) {
		this.model = model;
		this.model.tables().setDeleteKeyPressed(table);
		tableDescription_tf.setText(tableName);
		table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		table.setEditable(true);
		table.setPlaceholder(new Label(bundle.getString("tableContentEmpty")));
		hide_btn_iv.setImage(GUI_Methods.loadImage("showtable.png", GUIPrefs.BUTTON_WIDTH));
		table.setId(tableType);
		table.setItems(data_obs);
		model.tables().setDrag(table);
		connected_col.setCellValueFactory(
				(TableColumn.CellDataFeatures<FolderInfo, Boolean> cellData) -> new SimpleObjectProperty<>(
						cellData.getValue().isConnected()));
		connected_col.setCellFactory(model.tables().connected_cellFactory);
		badFiles_col.setCellValueFactory(
				(TableColumn.CellDataFeatures<FolderInfo, Integer> cellData) -> new SimpleObjectProperty<>(
						cellData.getValue().getBadFiles()));

		dateDifference_ratio_col.setCellValueFactory(
				(TableColumn.CellDataFeatures<FolderInfo, Double> param) -> new SimpleObjectProperty<>(
						param.getValue().getDateDifferenceRatio()));
		dateDifference_ratio_col.setCellFactory(model.tables().dateDifference_Status_cellFactory);
		dateDifference_ratio_col.setSortType(SortType.ASCENDING);
		folderFiles_col.setCellValueFactory(
				(TableColumn.CellDataFeatures<FolderInfo, Integer> cellData) -> new SimpleObjectProperty<>(
						cellData.getValue().getFolderFiles()));

		fullPath_col.setCellValueFactory(
				(TableColumn.CellDataFeatures<FolderInfo, String> cellData) -> new SimpleObjectProperty<>(
						cellData.getValue().getFolderPath()));
		image_col.setCellValueFactory(
				(TableColumn.CellDataFeatures<FolderInfo, Integer> cellData) -> new SimpleObjectProperty<>(
						cellData.getValue().getFolderImageFiles()));
		// justFolderName_col.setCellFactory(TextFieldTableCell.forTableColumn());
		justFolderName_col.setCellValueFactory(
				(TableColumn.CellDataFeatures<FolderInfo, String> cellData) -> new SimpleObjectProperty<>(
						cellData.getValue().getJustFolderName()));
		maxDates_col.setCellValueFactory(
				(TableColumn.CellDataFeatures<FolderInfo, String> cellData) -> new SimpleObjectProperty<>(
						cellData.getValue().getMaxDate()));

		minDate_col.setCellValueFactory(
				(TableColumn.CellDataFeatures<FolderInfo, String> cellData) -> new SimpleObjectProperty<>(
						cellData.getValue().getMinDate()));
		raw_col.setCellValueFactory(
				(TableColumn.CellDataFeatures<FolderInfo, Integer> cellData) -> new SimpleObjectProperty<>(
						cellData.getValue().getFolderRawFiles()));
		size_col.setCellValueFactory(
				(TableColumn.CellDataFeatures<FolderInfo, Long> cellData) -> new SimpleObjectProperty<>(
						cellData.getValue().getFolderSize()));
		size_col.setCellFactory(new DecimalColumnFactory<>(new DecimalFormat("0000")));
		status_col.setCellValueFactory(
				(TableColumn.CellDataFeatures<FolderInfo, Integer> cellData) -> new SimpleObjectProperty<>(
						cellData.getValue().getStatus()));
		// status_col.setCellFactory(model.tables().cell_Status_cellFactory);
		suggested_col.setCellValueFactory(
				(TableColumn.CellDataFeatures<FolderInfo, Integer> cellData) -> new SimpleObjectProperty<>(
						cellData.getValue().getSuggested()));
		video_col.setCellValueFactory(
				(TableColumn.CellDataFeatures<FolderInfo, Integer> cellData) -> new SimpleObjectProperty<>(
						cellData.getValue().getFolderVideoFiles()));
//		dateFix_col.setCellValueFactory(new PropertyValueFactory<>("DUMMY"));
		dateFix_col.setCellFactory(model.tables().dateFixer_cellFactory);
		copied_col.setCellValueFactory(
				(TableColumn.CellDataFeatures<FolderInfo, Integer> cellData) -> new SimpleObjectProperty<>(
						cellData.getValue().getCopied()));
		copied_col.setCellFactory(model.tables().copied_cellFactory);

		model.tables().getHideButtons().setAccelerator(select_all_btn, TableType.SORTED, 2);
		Messages.sprintf("sorted table were editable? " + table.isEditable() + " just fold editable?  "
				+ justFolderName_col.isEditable());
		select_dateDifference.setTooltip(new Tooltip("Selects by datedifference ratio\nwhich is higher than 1"));
		select_bad_btn.setTooltip(new Tooltip("Selects folders which hasn't got properly date & time"));
		select_good_btn.setTooltip(new Tooltip("Selects folders which has proper date & time info"));
		select_invert_btn.setTooltip(new Tooltip("Invert selection"));
		select_none_btn.setTooltip(new Tooltip("Selects none"));
		select_all_btn.setTooltip(new Tooltip("Selects all"));
	}

}
