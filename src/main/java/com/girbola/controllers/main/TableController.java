/*
 @(#)Copyright:  Copyright (c) 2012-2020 All right reserved.
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.main;

import static com.girbola.Main.bundle;
import static com.girbola.Main.conf;
import static com.girbola.messages.Messages.sprintf;
import static com.girbola.messages.Messages.warningText;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import com.girbola.Main;
import com.girbola.Scene_NameType;
import com.girbola.configuration.GUIPrefs;
import com.girbola.controllers.datefixer.GUI_Methods;
import com.girbola.controllers.loading.LoadingProcess_Task;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.dialogs.Dialogs;
import com.girbola.fileinfo.FileInfo;
import com.girbola.fxml.main.merge.MergeDialogController;
import com.girbola.fxml.operate.OperateFiles;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.sql.SQL_Utils;

import common.utils.Conversion;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.converter.NumberStringConverter;

public class TableController {

	private Model_main model_main;

	private Window owner;

	private final String ERROR = TableController.class.getSimpleName();

	private ObservableList<FolderInfo> data_obs = FXCollections.observableArrayList();

	private SimpleIntegerProperty allFilesTotal_obs = new SimpleIntegerProperty(0);

	@FXML
	private TextField tableDescription_tf;
	@FXML
	private Button select_all_btn;
	@FXML
	private HBox buttons_hbox;
	@FXML
	private ImageView hide_btn_iv;
	@FXML
	private FlowPane topMenuButtonFlowPane;
	@FXML
	private Button hide_btn;
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
	private Button copySelected_btn;
	@FXML
	private Button addToBatch_btn;
	@FXML
	private Button mergeCopy_btn;
	@FXML
	private Button resetSelectedFileInfos_btn;
	@FXML
	private Label allFilesCopied_lbl;

	@FXML
	private Label allFilesTotal_lbl;

	@FXML
	private Label allFilesSize_lbl;

	@FXML
	private Tooltip updateFolderInfo_btn_tooltip;
	@FXML
	private Tooltip select_bad_btn_tooltip;
	@FXML
	private Tooltip select_good_btn_tooltip;
	@FXML
	private Tooltip select_invert_btn_tooltip;
	@FXML
	private Tooltip select_none_btn_tooltip;
	@FXML
	private Tooltip select_dateDifference_tooltip;
	@FXML
	private Tooltip tableDescription_tf_tooltip;
	@FXML
	private Tooltip mergeCopy_btn_tooltip;

	@FXML
	private Tooltip copySelected_btn_tooltip;
	@FXML
	private Tooltip addToBatch_tooltip;
	@FXML
	private Tooltip resetSelectedFileInfos_btn_tooltip;

	@FXML
	private MenuItem checkChanges_mi;

	public Label getAllFilesCopied_lbl() {
		return allFilesCopied_lbl;
	}

	public Label getAllFilesTotal_lbl() {
		return allFilesTotal_lbl;
	}

	public Label getAllFilesSize_lbl() {
		return allFilesSize_lbl;
	}

	@FXML
	private MenuItem reload_all_mi;

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

	@FXML
	private void resetSelectedFileInfos_btn_action(ActionEvent event) {
		LoadingProcess_Task ldt = new LoadingProcess_Task(Main.scene_Switcher.getWindow());
		AtomicInteger counter = new AtomicInteger(0);

		Task<Void> task = new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				for (FolderInfo folderInfo : table.getSelectionModel().getSelectedItems()) {
					for (FileInfo fileInfo : folderInfo.getFileInfoList()) {
						fileInfo.setWorkDir("");
						fileInfo.setWorkDirDriveSerialNumber("");
						fileInfo.setDestination_Path("");
						fileInfo.setCopied(false);
						fileInfo.setConfirmed(false);
						fileInfo.setEvent("");
						fileInfo.setLocation("");
						fileInfo.setIgnored(false);
						fileInfo.setTableDuplicated(false);
						fileInfo.setTags("");
						Main.setChanged(true);
					}
					TableUtils.updateFolderInfos_FileInfo(folderInfo);
					TableUtils.refreshAllTableContent(model_main.tables());
					// ldt.setMessage("counter; " + counter.get());
					counter.getAndIncrement();

				}
				return null;
			}

		};
		task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				Messages.sprintf("resetSelectedFileInfos were succeeded");

				ldt.closeStage();
			}
		});
		task.setOnCancelled(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				Messages.sprintf("resetSelectedFileInfos were cancelled");
				ldt.closeStage();
			}
		});
		task.setOnFailed(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				Messages.sprintf("resetSelectedFileInfos were failed");
				ldt.closeStage();
			}
		});
		ldt.setTask(task);
//		totalProcesses = table.getSelectionModel().getSelectedItems().size();
		ldt.showLoadStage();
		new Thread(task, "resettingSelecteFileInfos_thread").start();

		Messages.sprintf("Resetting selected fileinfo's done!");
//		ldt.closeStage();
	}

	@FXML
	private void mergeCopy_btn_action(ActionEvent event) {
		Main.setProcessCancelled(false);
		try {
			if (!Files.exists(Paths.get(conf.getWorkDir()).toRealPath())) {
				warningText(bundle.getString("cannotFindWorkDir"));
				return;
			}
		} catch (IOException ex) {
			warningText(bundle.getString("cannotFindWorkDir"));
			return;
		}
		FXMLLoader loader = null;

		Parent root = null;

		try {
			loader = new FXMLLoader(Main.class.getResource("fxml/main/merge/MergeDialog.fxml"), Main.bundle);
			root = loader.load();
			Stage stage = new Stage();
			Scene scene = new Scene(root);
			MergeDialogController mergeDialogController = loader.getController();
			mergeDialogController.init(model_main, model_main.tables(), table, tableType);
			stage.setScene(scene);
			stage.showAndWait();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void copySelected_btn_action(ActionEvent event) {
		Main.setProcessCancelled(false);
		try {
			if (!Files.exists(Paths.get(conf.getWorkDir()).toRealPath())) {
				warningText(bundle.getString("cannotFindWorkDir"));
				return;
			}
		} catch (IOException ex) {
			warningText(bundle.getString("cannotFindWorkDir"));
			return;
		}
		Dialog<ButtonType> iHaveCheckedEverythingAndAcceptAllChanges_dialog = Dialogs.createDialog_YesNo(
				Main.scene_Switcher.getWindow(), bundle.getString("iHaveCheckedEverythingAndAcceptAllChanges"));
		Optional<ButtonType> result = iHaveCheckedEverythingAndAcceptAllChanges_dialog.showAndWait();
		if (result.get().getButtonData().equals(ButtonBar.ButtonData.YES)) {
			Messages.sprintf("Starting moving files from sortit to sorted");
			copySelectedTableRows(model_main.tables(), table, tableType);
		}
	}

	@FXML
	private void addToBatch_btn_action(ActionEvent event) {
		Main.setProcessCancelled(false);
		try {
			if (!Files.exists(Paths.get(conf.getWorkDir()).toRealPath())) {
				warningText(bundle.getString("cannotFindWorkDir"));
				return;
			}
		} catch (IOException ex) {
			warningText(bundle.getString("cannotFindWorkDir"));
			return;
		}
		addToBatchSelectedTableRows(model_main.tables(), table, tableType);
	}

	//@formatter:on
	@FXML
	private void checkChanges_mi_action(ActionEvent event) {
		LoadingProcess_Task loadingProcess = new LoadingProcess_Task(owner);
		Task<Boolean> checkTask = new CheckSelectedRowForChanges(table, model_main, loadingProcess);

		Thread thread = new Thread(checkTask, "Checking changes thread");
		thread.start();
	}

	@FXML
	private void updateFolderInfo_btn_action(ActionEvent event) {
		TableUtils.updateTableContent(table);
	}

	@FXML
	private void reload_all_mi_action(ActionEvent event) {
		sprintf("Reload All");
		LoadingProcess_Task lpt = new LoadingProcess_Task(Main.scene_Switcher.getWindow());
		Task<Void> updateTableValuesUsingFileInfo_task = new CreateFileInfoRow(model_main, table,
				Main.scene_Switcher.getWindow());
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
		model_main.buttons().select_all_Table(table);
	}

	@FXML
	private void hide_btn_action(ActionEvent event) {
		model_main.tables().getHideButtons().hide_show_table(hide_btn, tableType);
	}

	@FXML
	private void reload_btn_action(ActionEvent event) {
		Messages.warningText("No methods for reload btn");
		// model_main.getTables().updateFolderInfoFileInfo(table);
	}

	@FXML
	private void select_bad_btn_action(ActionEvent event) {
		model_main.buttons().select_bad_Table(table);
	}

	@FXML
	private void select_dateDifference_action(ActionEvent event) {
		model_main.buttons().select_dateDifference_Table(table);
	}

	@FXML
	private void select_good_btn_action(ActionEvent event) {
		model_main.buttons().select_good_Table(table);
	}

	@FXML
	private void select_invert_btn_action(ActionEvent event) {
		model_main.buttons().select_invert_Table(table);
	}

	@FXML
	private void select_none_btn_action(ActionEvent event) {
		model_main.buttons().select_none_Table(table);
	}

	@FXML
	private void handleMouseClicked(MouseEvent mouseEvent) {
		if (mouseEvent.getClickCount() == 2) {
			Messages.sprintf("Mouse clicked on tableview: " + mouseEvent);
		}
	}

	public TableView<FolderInfo> getTable() {
		return this.table;
	}

	public HBox getButtons_HBOX() {
		return this.buttons_hbox;
	}

	public Label allFilesTotal_label() {
		return this.allFilesTotal_lbl;
	}

	public Label allFilesCopied_label() {
		return this.allFilesCopied_lbl;
	}

	public Label allFilesSize_label() {
		return this.allFilesSize_lbl;
	}

	private String tableType;

	public void init(Model_main aModel_main, String tableName, String tableType) {
		this.model_main = aModel_main;
		this.model_main.tables().setDeleteKeyPressed(table);
		this.tableType = tableType;
		tableDescription_tf.setText(tableName);

		table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		table.setEditable(true);
		table.setPlaceholder(new Label(bundle.getString("tableContentEmpty")));
		hide_btn_iv.setImage(GUI_Methods.loadImage("showtable.png", GUIPrefs.BUTTON_WIDTH));
		table.setId(tableType);
		table.setItems(data_obs);
		model_main.tables().setDrag(table);
		connected_col.setCellValueFactory(
				(TableColumn.CellDataFeatures<FolderInfo, Boolean> cellData) -> new SimpleObjectProperty<>(
						cellData.getValue().isConnected()));
		connected_col.setCellFactory(model_main.tables().connected_cellFactory);
		badFiles_col.setCellValueFactory(
				(TableColumn.CellDataFeatures<FolderInfo, Integer> cellData) -> new SimpleObjectProperty<>(
						cellData.getValue().getBadFiles()));

		dateDifference_ratio_col.setCellValueFactory(
				(TableColumn.CellDataFeatures<FolderInfo, Double> param) -> new SimpleObjectProperty<>(
						param.getValue().getDateDifferenceRatio()));
		dateDifference_ratio_col.setCellFactory(model_main.tables().dateDifference_Status_cellFactory);
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
		justFolderName_col.setEditable(true);
		justFolderName_col.setCellValueFactory(
				(TableColumn.CellDataFeatures<FolderInfo, String> cellData) -> new SimpleObjectProperty<>(
						cellData.getValue().getJustFolderName()));
		justFolderName_col.setCellFactory(model_main.tables().textFieldEditingCellFactory);

		allFilesTotal_lbl.textProperty().bindBidirectional(allFilesTotal_obs, new NumberStringConverter());
//		data_obs.addListener(new ListChangeListener<FolderInfo>() {
//
//			@Override
//			public void onChanged(Change<? extends FolderInfo> c) {
//				if (c != null) {
//					while (c.next()) {
//						if (c.wasPermutated() || c.wasAdded() || c.wasUpdated()) {
//							FolderInfo fo = (FolderInfo) c.getList();
//							if (fo != null) {
//
//								if (fo.getFolderFiles() > 0) {
//									Messages.sprintf("change c: " + c.toString());
//									allFilesTotal_obs.set(fo.getFolderFiles());
//								}
//							}
//						}
//					}
//				}
//			}
//
//		});
//		justFolderName_col.setCellFactory(TextFieldTableCell.forTableColumn());
//		justFolderName_col.setOnEditStart(new EventHandler<TableColumn.CellEditEvent<FolderInfo, String>>() {
//
//			@Override
//			public void handle(CellEditEvent<FolderInfo, String> event) {
//				Messages.sprintf("start edit " + event.getSource() + " old " + event.getOldValue() + " new value "
//						+ event.getNewValue());
////				if (event.getNewValue().contains("\\") || event.getNewValue().contains("/") || event.getNewValue().contains(":") || event.getNewValue().contains("*") || event.getNewValue().contains("?")
////						|| event.getNewValue().contains("<") || event.getNewValue().contains(">") || event.getNewValue().contains("|")) {
////				Messages.sprintf("Bad words");
////				}
////				if (newValue.length() > 160) {
////					if (!tooltip_tooLongFileName.isShowing()) {
////						show(tooltip_tooLongFileName, textField);
////					}
////					textField.setText(oldValue);
////				}
//
//			}
//		});

		justFolderName_col.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<FolderInfo, String>>() {

			@Override
			public void handle(CellEditEvent<FolderInfo, String> event) {
				Messages.sprintf("edit commited event.getNewValue(); " + event.getNewValue());
				if (event.getRowValue().getJustFolderName() != event.getNewValue()) {
					event.getRowValue().setJustFolderName(event.getNewValue().trim());
					Path src = Paths.get(event.getRowValue().getFolderPath());
					Path dest = Paths
							.get(src.getParent().toString() + File.separator + event.getRowValue().getJustFolderName());
					if (Files.exists(dest)) {
						event.getRowValue().setJustFolderName(event.getOldValue());
						Messages.sprintf("");
						return;
					}
					try {
						Path renamed = Files.move(src, dest);
						if (Files.exists(renamed)) {
							event.getRowValue().setFolderPath(dest.toString());
							for (FileInfo fileInfo : event.getRowValue().getFileInfoList()) {
								fileInfo.setOrgPath(dest + File.separator
										+ Paths.get(fileInfo.getOrgPath()).getFileName().toString());
								Messages.sprintf("FileInfo orgName changed to: " + dest + File.separator
										+ Paths.get(fileInfo.getOrgPath()).getFileName().toString());

							}

							Messages.sprintf(
									"Folder were renamed as: " + renamed + " and it exists? " + Files.exists(dest));
							TableUtils.refreshAllTableContent(model_main.tables());
							SQL_Utils.renameToFolderInfoDB(event.getRowValue(), src.toString());
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					Main.setChanged(true);
					event.getRowValue().setChanged(true);
				}
			}
		});
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
		size_col.setCellFactory(tableColumn -> new TableCell<FolderInfo, Long>() {
			@Override
			protected void updateItem(Long value, boolean empty) {
				super.updateItem(value, empty);
				if (value == null || empty) {
					setText("");
				} else {
					setText("" + (Conversion.convertToSmallerConversion(value)));
				}
			}
		});
		status_col.setCellValueFactory(
				(TableColumn.CellDataFeatures<FolderInfo, Integer> cellData) -> new SimpleObjectProperty<>(
						cellData.getValue().getStatus()));
		status_col.setCellFactory(model_main.tables().cell_Status_cellFactory);
		suggested_col.setCellValueFactory(
				(TableColumn.CellDataFeatures<FolderInfo, Integer> cellData) -> new SimpleObjectProperty<>(
						cellData.getValue().getSuggested()));
		video_col.setCellValueFactory(
				(TableColumn.CellDataFeatures<FolderInfo, Integer> cellData) -> new SimpleObjectProperty<>(
						cellData.getValue().getFolderVideoFiles()));
//		dateFix_col.setCellValueFactory(new PropertyValueFactory<>("DUMMY"));
		dateFix_col.setCellFactory(model_main.tables().dateFixer_cellFactory);
		copied_col.setCellValueFactory(
				(TableColumn.CellDataFeatures<FolderInfo, Integer> cellData) -> new SimpleObjectProperty<>(
						cellData.getValue().getCopied()));
		copied_col.setCellFactory(model_main.tables().copied_cellFactory);
		if (hide_btn == null) {
			Messages.errorSmth(ERROR, "model_main.tables().getHideButtons(). were null", null, Misc.getLineNumber(),
					true);
		}
		/*
		 * Disabling buttons
		 */
		if (tableType.equals(TableType.ASITIS.getType())) {
			mergeCopy_btn.setVisible(false);
		}
		Messages.sprintf("table is editable? " + table.isEditable() + " just fold editable?  "
				+ justFolderName_col.isEditable());

		if (tableType == TableType.ASITIS.getType()) {
			tableDescription_tf_tooltip.setText(Main.bundle.getString("asitis_table_desc"));
			tableDescription_tf.setTooltip(tableDescription_tf_tooltip);
		} else if (tableType == TableType.SORTIT.getType()) {
			tableDescription_tf_tooltip.setText(Main.bundle.getString("sortit_table_desc"));
			tableDescription_tf.setTooltip(tableDescription_tf_tooltip);
		} else if (tableType == TableType.SORTED.getType()) {
			tableDescription_tf_tooltip.setText(Main.bundle.getString("sorted_table_desc"));
			tableDescription_tf.setTooltip(tableDescription_tf_tooltip);
		}

		Main.conf.showTooltips_property().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue == true) {
					updateFolderInfo_btn.setTooltip(updateFolderInfo_btn_tooltip);
					select_bad_btn.setTooltip(select_bad_btn_tooltip);
					select_good_btn.setTooltip(select_good_btn_tooltip);
					select_invert_btn.setTooltip(select_invert_btn_tooltip);
					select_none_btn.setTooltip(select_none_btn_tooltip);
					select_dateDifference.setTooltip(select_dateDifference_tooltip);

					if (tableType == TableType.ASITIS.getType()) {
						tableDescription_tf_tooltip.setText(Main.bundle.getString("sortit_table_desc"));
						tableDescription_tf.setTooltip(tableDescription_tf_tooltip);
					} else if (tableType == TableType.SORTIT.getType()) {
						tableDescription_tf_tooltip.setText(Main.bundle.getString("sorted_table_desc"));
						tableDescription_tf.setTooltip(tableDescription_tf_tooltip);
					} else if (tableType == TableType.SORTED.getType()) {
						tableDescription_tf_tooltip.setText(Main.bundle.getString("asitis_table_desc"));
						tableDescription_tf.setTooltip(tableDescription_tf_tooltip);
					}
				} else {
					hideTooltip(updateFolderInfo_btn);
					hideTooltip(select_bad_btn);
					hideTooltip(select_good_btn);
					hideTooltip(select_invert_btn);
					hideTooltip(select_none_btn);
					hideTooltip(select_dateDifference);
				}
			}

		});
	}

	private void hideTooltip(Control control) {

		control.getTooltip().setText("");
		control.getTooltip().hide();

	}

	private void addToBatchSelectedTableRows(Tables tables, TableView<FolderInfo> table, String tableType) {
		if (Main.conf.getWorkDir() == null) {
			Messages.warningText("copySelectedTableRows Workdir were null");
			return;
		}
		if (Main.conf.getWorkDir().isEmpty()) {
			Messages.warningText("copySelectedTableRows Workdir were empty");
			return;
		}
		for (FolderInfo folderInfo : table.getSelectionModel().getSelectedItems()) {
			if (folderInfo.getBadFiles() >= 1) {
				Messages.warningText(Main.bundle.getString("badDatesFound"));
				return;
			}

			for (FileInfo fileInfo : folderInfo.getFileInfoList()) {

				Path destPath = TableUtils.resolveFileDestinationPath(folderInfo.getJustFolderName(), fileInfo,
						tableType);
				if (destPath != null) {
					if (!destPath.toString().equals(fileInfo.getDestination_Path())) {
						fileInfo.setWorkDir(Main.conf.getWorkDir());
						fileInfo.setWorkDirDriveSerialNumber(Main.conf.getWorkDirSerialNumber());
						fileInfo.setDestination_Path(destPath.toString());
						fileInfo.setCopied(false);
						Main.setChanged(true);
					}
				}
				Messages.sprintf("Destination path would be: " + fileInfo.getDestination_Path());
			}
		}
	}

	private void copySelectedTableRows(Tables tables, TableView<FolderInfo> table, String tableType) {
		if (Main.conf.getWorkDir() == null) {
			Messages.warningText("copySelectedTableRows Workdir were null");
			return;
		}
		if (Main.conf.getWorkDir().isEmpty()) {
			Messages.warningText("copySelectedTableRows Workdir were empty");
			return;
		}
		for (FolderInfo folderInfo : table.getSelectionModel().getSelectedItems()) {
			if (folderInfo.getBadFiles() >= 1) {
				Messages.warningText(Main.bundle.getString("badDatesFound"));
				return;
			}

			for (FileInfo fileInfo : folderInfo.getFileInfoList()) {
				Path destPath = TableUtils.resolveFileDestinationPath(folderInfo.getJustFolderName(), fileInfo,
						tableType);
				if (destPath != null) {
					if (!destPath.toString().equals(fileInfo.getDestination_Path())) {
						fileInfo.setWorkDir(Main.conf.getWorkDir());
						fileInfo.setWorkDirDriveSerialNumber(Main.conf.getWorkDirSerialNumber());
						fileInfo.setDestination_Path(destPath.toString());
						fileInfo.setCopied(false);
						Main.setChanged(true);
					}
				}
				Messages.sprintf("Destination path would be: " + fileInfo.getDestination_Path());
			}
		}
		List<FileInfo> list = new ArrayList<>();
		ExecutorService exec = Executors.newSingleThreadExecutor();
		for (FolderInfo folderInfo : table.getSelectionModel().getSelectedItems()) {
			if (folderInfo.getBadFiles() >= 1) {
				Messages.warningText(Main.bundle.getString("badDatesFound"));
				return;
			}
			list.addAll(folderInfo.getFileInfoList());

		}

		Task<Boolean> operate = new OperateFiles(list, true, model_main, Scene_NameType.MAIN.getType());

		operate.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				for (FolderInfo folderInfo : table.getSelectionModel().getSelectedItems()) {
					TableUtils.updateFolderInfos_FileInfo(folderInfo);
				}
				TableUtils.refreshAllTableContent(tables);
			}
		});
		operate.setOnFailed(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				Messages.warningText("Copy process failed");
			}
		});

		operate.setOnCancelled(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				Messages.sprintf("Copy process were cancelled");
			}
		});

		Thread thread = new Thread(operate, "Operate Thread");
//		thread.start();
		exec.submit(thread);

	}

}
