/*

 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.main;

import com.girbola.MDir_Constants;
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
import com.girbola.fxml.main.collect.Collect_DialogController;
import com.girbola.fxml.main.collect.Model_CollectDialog;
import com.girbola.fxml.main.merge.copy.MergeCopyDialogController;
import com.girbola.fxml.main.merge.move.MergeMoveDialogController;
import com.girbola.fxml.operate.OperateFiles;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.sql.FileInfo_SQL;
import com.girbola.sql.SqliteConnection;
import common.utils.Conversion;
import common.utils.ui.ScreenUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
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
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import javafx.util.converter.NumberStringConverter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static com.girbola.Main.bundle;
import static com.girbola.Main.conf;
import static com.girbola.messages.Messages.sprintf;
import static com.girbola.messages.Messages.warningText;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TableController {

	private Model_main model_main;
	private Model_CollectDialog model_CollectDialog;

	private Window owner;

	private final String ERROR = TableController.class.getSimpleName();

	private ObservableList<FolderInfo> data_obs = FXCollections.observableArrayList();

	private SimpleIntegerProperty allFilesTotal_obs = new SimpleIntegerProperty(0);

	private SimpleBooleanProperty showTable = new SimpleBooleanProperty(true);

	@FXML
	private AnchorPane tables_rootPane;

	@FXML
	private HBox showHideButton_hbox;

	@FXML
	private HBox tables_parent;

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
	public Button hide_btn;

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

//	@FXML
//	private Button copySelected_btn;
//	@FXML
//	private Button addToBatch_btn;
//	@FXML
//	private Button mergeCopy_btn;
//	@FXML
//	private Button collectSimilarDates_btn;
//	@FXML
//	private Button resetSelectedFileInfos_btn;

	@FXML
	private Label allFilesCopied_lbl;
	@FXML
	private Label allFilesTotal_lbl;
	@FXML
	private Label allFilesSize_lbl;
	@FXML
	private Tooltip select_all_btn_tooltip;
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
	private Tooltip collectSimilarDates_btn_tooltip;
	@FXML
	private Tooltip copySelected_btn_tooltip;
	@FXML
	private Tooltip addToBatch_tooltip;
	@FXML
	private Tooltip resetSelectedFileInfos_btn_tooltip;

	@FXML
	private VBox group;

	@FXML
	private HBox tableInformation_hbox;

	@FXML
	private MenuItem checkChanges_mi;

	@FXML
	private MenuItem mergeCopy_MenuItem;

	@FXML
	private MenuItem mergeMove_MenuItem;

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
	private MenuItem select_dateDifference_btn;
	@FXML
	private TableColumn<FolderInfo, Double> dateDifference_ratio_col;
	@FXML
	private TableColumn<FolderInfo, Boolean> connected_col;
	@FXML
	private VBox table_Vbox;
	@FXML
	private AnchorPane table_RootPane;
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

	private Image show_im;

	private Image hide_im;

	@FXML
	private void resetSelectedFileInfos_btn_action(ActionEvent event) {
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
					TableUtils.updateFolderInfo(folderInfo);
					TableUtils.refreshAllTableContent(model_main.tables());
					// ldt.setMessage("counter; " + counter.get());
					counter.getAndIncrement();

				}
				return null;
			}

		};
		Thread thread = new Thread(task, "resettingSelecteFileInfos_thread");
		thread.start();
		Messages.sprintf("Resetting selected fileinfo's done!");
	}

	@FXML
	private void mergeMove_btn_action(ActionEvent event) {
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

		if (tableType.equals(TableType.SORTIT.getType())) {
			if (model_main.tables().getSortIt_table().getSelectionModel().getSelectedItems().size() <= 1) {
				warningText(bundle.getString("noSelectedFoldersToMerge"));
				return;
			}
		} else if (tableType.equals(TableType.SORTED.getType())) {
			if (model_main.tables().getSorted_table().getSelectionModel().getSelectedItems().size() <= 1) {
				warningText(bundle.getString("noSelectedFoldersToMerge"));
				return;
			}
		} else if (tableType.equals(TableType.ASITIS.getType())) {
			if (model_main.tables().getAsItIs_table().getSelectionModel().getSelectedItems().size() <= 1) {
				warningText(bundle.getString("noSelectedFoldersToMerge"));
				return;
			} 
		}

		FXMLLoader loader = null;
		Parent root = null;

		try {
			loader = new FXMLLoader(Main.class.getResource("fxml/main/merge/move/MergeMoveDialog.fxml"), Main.bundle);
			root = loader.load();
			Stage stage = new Stage();
			Scene scene = new Scene(root);
			stage.initOwner(Main.scene_Switcher.getScene_main().getWindow());
			stage.initModality(Modality.WINDOW_MODAL);
			stage.setMaxHeight(200);

			Main.centerWindowDialog(stage);

			stage.setMaxWidth(Main.conf.getScreenBounds().getWidth());
			stage.setAlwaysOnTop(true);
			scene.getStylesheets().add(
					Main.class.getResource(conf.getThemePath() + MDir_Constants.DIALOGS.getType()).toExternalForm());
			MergeMoveDialogController mergeMoveDialogController = (MergeMoveDialogController) loader.getController();
			mergeMoveDialogController.init(model_main, model_main.tables(), table, tableType);
			stage.setScene(scene);

			stage.showAndWait();
		} catch (Exception e) {
			e.printStackTrace();
		}

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
			loader = new FXMLLoader(Main.class.getResource("fxml/main/merge/copy/MergeCopyDialog.fxml"), Main.bundle);
			root = loader.load();
			Stage stage = new Stage();
			Scene scene = new Scene(root);
			stage.initOwner(Main.scene_Switcher.getScene_main().getWindow());
			stage.initModality(Modality.WINDOW_MODAL);
			stage.setMaxHeight(200);
			Main.centerWindowDialog(stage);

			stage.setMaxWidth(Main.conf.getScreenBounds().getWidth());
			stage.setAlwaysOnTop(true);
			scene.getStylesheets().add(
					Main.class.getResource(conf.getThemePath() + MDir_Constants.DIALOGS.getType()).toExternalForm());
			MergeCopyDialogController mergeCopyDialogController = (MergeCopyDialogController) loader.getController();
			mergeCopyDialogController.init(model_main, model_main.tables(), table, tableType);
			stage.setScene(scene);

			stage.showAndWait();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void collectSimilarDates_btn_action(ActionEvent event) {
		try {
			Parent parent = null;
			FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/main/collect/Collect_Dialog.fxml"), bundle);
			parent = loader.load();
			Stage stage = new Stage();
			Scene scene = new Scene(parent);
			model_CollectDialog = new Model_CollectDialog();

			Collect_DialogController controller = (Collect_DialogController) loader.getController();
			controller.init(model_main, model_CollectDialog, table, tableType);
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

	// @formatter:on
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
		updateTableValuesUsingFileInfo_task.setOnSucceeded(event1 -> {
			sprintf("updateTableValuesFileInfo done successfully");
			lpt.closeStage();
		});

		updateTableValuesUsingFileInfo_task.setOnCancelled(event12 -> {
			lpt.closeStage();
			Messages.warningText("Creating file info cancelled");
		});
		updateTableValuesUsingFileInfo_task.setOnFailed(event13 -> {
			lpt.closeStage();
			Messages.errorSmth(ERROR, "", null, Misc.getLineNumber(), true);
		});
		lpt.setTask(updateTableValuesUsingFileInfo_task);
		Thread thread = new Thread(updateTableValuesUsingFileInfo_task, "Reloading Thread");
		thread.start();
	}

	@FXML
	private void select_all_btn_action(ActionEvent event) {
		model_main.buttons().select_all_Table(table);
	}

	public void setShowHideTableButtonIcons(String tableType, Button button, boolean show) {
		ImageView iv = (ImageView) button.getGraphic();
		if (show) {
			iv.setImage(hide_im);
			iv.setRotate(-90);
			button.setGraphic(iv);
		} else {
			iv.setImage(show_im);
			iv.setRotate(0);
			button.setGraphic(iv);
		}
	}

	public void setTableIsShown(boolean show) {
		if (tableType.equals(TableType.SORTIT)) {
			model_main.tables().showAndHideTables
					.setSortit_show_property(!model_main.tables().showAndHideTables.getSortit_show_property().get());
		} else if (tableType.equals(TableType.SORTED)) {
			model_main.tables().showAndHideTables
					.setSorted_show_property(!model_main.tables().showAndHideTables.getSorted_show_property().get());
		} else if (tableType.equals(TableType.ASITIS)) {
			model_main.tables().showAndHideTables
					.setAsitis_show_property(!model_main.tables().showAndHideTables.getAsitis_show_property().get());
		}

	}

	private void handleTableStates() {

		int visibles = getVisibles();
		int hidden = (3 - visibles) + 1;
		double tableWidth = Math.floor(ScreenUtils.screenBouds().getWidth() / visibles);
		double buttonWidth = hide_btn.getLayoutBounds().getWidth();

		Messages.sprintf("Show table?: " + table.isVisible() + " visibles: " + visibles + " hidden: " + hidden
				+ " buttonWidth " + buttonWidth + " tableWidth: " + tableWidth);

		HBox showHideButton_hbox_sortit = (HBox) getPaneFromParent(model_main.tables().getSortIt_table().getParent(),
				"showHideButton_hbox");
		HBox buttons_hbox_sortit = (HBox) getPaneFromParent(model_main.tables().getSortIt_table().getParent(),
				"buttons_hbox");
		assertNotNull(buttons_hbox_sortit);

		HBox showHideButton_hbox_sorted = (HBox) getPaneFromParent(model_main.tables().getSorted_table().getParent(),
				"showHideButton_hbox");
		HBox buttons_hbox_sorted = (HBox) getPaneFromParent(model_main.tables().getSorted_table().getParent(),
				"buttons_hbox");
		assertNotNull(buttons_hbox_sorted);

		HBox showHideButton_hbox_asitis = (HBox) getPaneFromParent(model_main.tables().getAsItIs_table().getParent(),
				"showHideButton_hbox");
		HBox buttons_hbox_asitis = (HBox) getPaneFromParent(model_main.tables().getAsItIs_table().getParent(),
				"buttons_hbox");
		assertNotNull(buttons_hbox_asitis);

		if (model_main.tables().getSortIt_table().isVisible()) {
			setWidth(showHideButton_hbox_sortit, tableWidth - (buttonWidth * hidden));
			buttons_hbox_sortit.setVisible(true);
			model_main.tables().showAndHideTables.setSortit_show_property(true);
		} else {
			setWidth(showHideButton_hbox_sortit, buttonWidth);
			buttons_hbox_sortit.setVisible(false);
			model_main.tables().showAndHideTables.setSortit_show_property(false);
		}

		if (model_main.tables().getSorted_table().isVisible()) {
			setWidth(showHideButton_hbox_sorted, tableWidth - (buttonWidth * hidden));
			buttons_hbox_sorted.setVisible(true);
			model_main.tables().showAndHideTables.setSorted_show_property(true);
		} else {
			setWidth(showHideButton_hbox_sorted, buttonWidth);
			buttons_hbox_sorted.setVisible(false);
			model_main.tables().showAndHideTables.setSorted_show_property(false);
		}

		if (model_main.tables().getAsItIs_table().isVisible()) {
			setWidth(showHideButton_hbox_asitis, tableWidth - (buttonWidth * hidden));
			buttons_hbox_asitis.setVisible(true);
			model_main.tables().showAndHideTables.setAsitis_show_property(true);
		} else {
			setWidth(showHideButton_hbox_asitis, buttonWidth);
			buttons_hbox_asitis.setVisible(false);
			model_main.tables().showAndHideTables.setAsitis_show_property(false);
		}
	}

	@FXML
	private void hide_btn_action(ActionEvent event) {

		int visibles = getVisibles();
		if (visibles <= 1 && table.isVisible()) {
			return;
		}
		table.setVisible(!table.isVisible());
		handleTableStates();
	}

	private int getVisibles() {
		int visibles = 0;
		if (model_main.tables().getSortIt_table().isVisible()) {
			visibles++;
		}
		if (model_main.tables().getSorted_table().isVisible()) {
			visibles++;
		}
		if (model_main.tables().getAsItIs_table().isVisible()) {
			visibles++;
		}

		return visibles;
	}

	private Pane getPaneFromParent(Parent parent, String id) {
		Pane pane = (Pane) parent;
		if (pane instanceof VBox) {
			if (pane.getId().contains("table_vbox")) {
				VBox main = (VBox) pane;
				for (Node node : main.getChildren()) {
					if (node instanceof HBox) {
						if (node.getId().equals(id) && node.getId().equals("showHideButton_hbox")) {
							HBox showHideButton_hbox = (HBox) node;
							return showHideButton_hbox;
						}
						if (node.getId().equals(id) && node.getId().equals("buttons_hbox")) {
							HBox buttons_hbox = (HBox) node;
							return buttons_hbox;
						}
					}
				}
			}
		}
		return null;
	}

	public void handleTable(boolean show) {
		double visibles = model_main.tables().showAndHideTables.getVisibles();
		double hidden = 3 - model_main.tables().showAndHideTables.getVisibles();

		double tableWidth = ScreenUtils.screenBouds().getWidth();

		if (visibles == 1) {
			tableWidth = Math.floor((tableWidth) / visibles) - (hidden * 35);
		} else if (visibles == 2) {
			tableWidth = Math.floor((tableWidth) / visibles) - 35;
		} else if (visibles == 3) {
			tableWidth = Math.floor((tableWidth) / visibles) - (hidden * 35);
		}
		Messages.sprintf("VISIBLES: " + visibles + " hidden: " + hidden);
		if (show) {
			showHideButton_hbox.setPrefWidth(35);
			showHideButton_hbox.setMinWidth(35);
			showHideButton_hbox.setMaxWidth(35);
			buttons_hbox.setVisible(!show);
			table_RootPane.setVisible(!show);
			tableInformation_hbox.setVisible(!show);
			showTable.set(!show);

			table_Vbox.setPrefWidth(35);
			table_Vbox.setMinWidth(35);
			table_Vbox.setMaxWidth(35);

			Messages.sprintf(
					"1 table is= " + table.getId() + " table Is disabled? " + table.isDisable() + " show: " + show);

		} else {

			table_Vbox.setPrefWidth(Region.USE_COMPUTED_SIZE);
			table_Vbox.setMinWidth(Region.USE_COMPUTED_SIZE);
			table_Vbox.setMaxWidth(Region.USE_COMPUTED_SIZE);

			showHideButton_hbox.setPrefWidth(tableWidth);
			showHideButton_hbox.setMinWidth(tableWidth);
			showHideButton_hbox.setMaxWidth(tableWidth);

			buttons_hbox.setVisible(!show);

			tableInformation_hbox.setVisible(!show);

			showTable.set(!show);

			table_RootPane.setVisible(!show);

			Messages.sprintf("2 table is= " + table.getId() + " table Is disabled? " + table.isDisable() + " show: "
					+ show + " model_main.tables().showAndHideTables.getVisibles(): "
					+ model_main.tables().showAndHideTables.getVisibles() + " hidden: " + hidden);
		}
		updateTableWidths(tableWidth);
	}

	private void updateTableWidths(double tableWidth) {
		Messages.sprintf("updateTableWidths: " + tableWidth);
		if (model_main.tables().showAndHideTables.getSortit_show_property().get()) {
			setTableWidth(model_main.tables().getSortIt_table().getParent(), tableWidth);
			Messages.sprintf("1SORTIT Width: " + tableWidth);
		} else {
			Messages.sprintf("2SORTIT Width: " + model_main.tables().getSortIt_table().getPrefWidth());
		}
		if (model_main.tables().showAndHideTables.getSorted_show_property().get()) {
			setTableWidth(model_main.tables().getSorted_table().getParent(), tableWidth);
			Messages.sprintf("3SORTED Width: " + tableWidth);
		} else {
			Messages.sprintf("4SORTED Width: " + model_main.tables().getSorted_table().getPrefWidth());
		}
		if (model_main.tables().showAndHideTables.getAsitis_show_property().get()) {
			setTableWidth(model_main.tables().getAsItIs_table().getParent(), tableWidth);
			Messages.sprintf("5ASITIS Width: " + tableWidth);
		} else {
			Messages.sprintf("6ASITIS Width: " + model_main.tables().getAsItIs_table().getPrefWidth());
		}
	}

	private void setTableWidth(Parent parent, double tableWidth) {
		Platform.runLater(() -> {
			parent.prefWidth(tableWidth);
			parent.minWidth(tableWidth);
			parent.maxWidth(tableWidth);
			Messages.sprintf("Parents parent is: " + parent.toString() + " tableWidth: " + tableWidth);
		});
	}

	private void showPanes(boolean show) {
		table_Vbox.setFillWidth(show);
		buttons_hbox.setVisible(show);

		table.setVisible(show);
		tableInformation_hbox.setVisible(show);
		double maxWidthOnScreen = common.utils.ui.ScreenUtils.screenBouds().getWidth();
	}

	@FXML
	private void reload_btn_action(ActionEvent event) {
		Messages.warningText("No methods for reload btn");
	}

	@FXML
	private void select_bad_btn_action(ActionEvent event) {
		model_main.buttons().select_bad_Table(table);
	}

	@FXML
	private void select_dateDifference_btn_action(ActionEvent event) {
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

		show_im = GUI_Methods.loadImage("showTable.png", GUIPrefs.BUTTON_WIDTH);
		hide_im = GUI_Methods.loadImage("hideTable.png", GUIPrefs.BUTTON_WIDTH);

		showTable.addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (tableType.equals(TableType.SORTIT)) {
					model_main.tables().showAndHideTables.setSortit_show_property(newValue);
				} else if (tableType.equals(TableType.SORTED)) {
					model_main.tables().showAndHideTables.setSorted_show_property(newValue);
				} else if (tableType.equals(TableType.ASITIS)) {
					model_main.tables().showAndHideTables.setAsitis_show_property(newValue);
				}
			}
		});

		Platform.runLater(() -> {
			setShowHideTableButtonIcons(tableType, hide_btn, true);
		});

		tableDescription_tf.setText(tableName);

		table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		table.setEditable(true);
		table.setPlaceholder(new Label(bundle.getString("tableContentEmpty")));
		table_Vbox.setId("table_vbox");
		table_Vbox.setFillWidth(true);

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
//		justFolderName_col.setCellFactory(model_main.tables().textFieldEditingCellFactory);
		justFolderName_col
				.setCellFactory(new Callback<TableColumn<FolderInfo, String>, TableCell<FolderInfo, String>>() {
					@Override
					public TableCell<FolderInfo, String> call(TableColumn<FolderInfo, String> param) {
//						param.getTableView().getSelectionModel().getSelectedItem();
						return new EditingCell(model_main, param);
					}
				});

		allFilesTotal_lbl.textProperty().bindBidirectional(allFilesTotal_obs, new NumberStringConverter());

		showTable.addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				model_main.tables().showAndHideTables.showTable(tableType, newValue);
			}
		});

		justFolderName_col.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<FolderInfo, String>>() {

			@Override
			public void handle(CellEditEvent<FolderInfo, String> event) {
				Messages.sprintf("edit commited event.getNewValue(); " + event.getNewValue());
				FolderInfo folderInfo = (FolderInfo) event.getRowValue();
				if (event.getNewValue().isEmpty()) {
					folderInfo.setJustFolderName(Paths.get(folderInfo.getFolderPath()).getFileName().toString());

				} else if (folderInfo.getJustFolderName() != event.getNewValue() && !event.getNewValue().isEmpty()) {
					folderInfo.setJustFolderName(event.getNewValue());
					for (FileInfo fileInfo : folderInfo.getFileInfoList()) {
						fileInfo.setEvent(event.getNewValue());
					}

					Main.setChanged(true);
					folderInfo.setChanged(true);
					TableUtils.updateFolderInfo(folderInfo);
					Connection connection = SqliteConnection.connector(folderInfo.getFolderPath(),
							Main.conf.getMdir_db_fileName());
					try {
						connection.setAutoCommit(false);
						FileInfo_SQL.insertFileInfoListToDatabase(connection, folderInfo.getFileInfoList(), false);

					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						if (connection != null) {
							try {
								connection.commit();
								connection.close();
							} catch (SQLException e2) {
								e2.printStackTrace();
							}
						}
					}
				}
				TableUtils.refreshAllTableContent(model_main.tables());
				TableUtils.saveChangesContentsToTables(model_main.tables());

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
			mergeCopy_MenuItem.setVisible(false);
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
					select_all_btn.setTooltip(select_all_btn_tooltip);
					select_bad_btn.setTooltip(select_bad_btn_tooltip);
					select_good_btn.setTooltip(select_good_btn_tooltip);
					select_invert_btn.setTooltip(select_invert_btn_tooltip);
					select_none_btn.setTooltip(select_none_btn_tooltip);

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
				} else {
					hideTooltip(updateFolderInfo_btn);
					hideTooltip(select_bad_btn);
					hideTooltip(select_good_btn);
					hideTooltip(select_invert_btn);
					hideTooltip(select_none_btn);
					hideTooltip(select_all_btn);
				}
			}
		});
	}

	private void setWidth(Pane pane, double width) {
		Platform.runLater(() -> {
			pane.setMinWidth(width);
			pane.setMaxWidth(width);
			pane.setPrefWidth(width);
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
						if (!folderInfo.getChanged()) {
							folderInfo.setChanged(true);
						}
					}
				}
				Messages.sprintf("Destination path would be: " + fileInfo.getDestination_Path());
			}
		}
		List<FileInfo> list = new ArrayList<>();
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
					TableUtils.updateFolderInfo(folderInfo);
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
		ExecutorService exec = Executors.newSingleThreadExecutor();
		exec.submit(thread);

	}

}
