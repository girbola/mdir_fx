/*
 @(#)Copyright:  Copyright (c) 2012-2020 All right reserved.
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.datefixer;

import static com.girbola.Main.bundle;
import static com.girbola.messages.Messages.sprintf;
import static com.girbola.messages.Messages.warningText;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.girbola.Main;
import com.girbola.Scene_NameType;
import com.girbola.concurrency.ConcurrencyUtils;
import com.girbola.controllers.loading.LoadingProcess_Task;
import com.girbola.controllers.main.ImportImages;
import com.girbola.controllers.main.Model_main;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.fileinfo.DestinationResolver;
import com.girbola.fileinfo.FileInfo;
import com.girbola.fileinfo.FileInfo_Utils;
import com.girbola.fxml.operate.OperateFiles;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;

import common.utils.FileUtils;
import common.utils.date.DateUtils;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class DateFixerController {

	private static final String ERROR = DateFixerController.class.getSimpleName();
	// private boolean infoTables_visible = true;
	private SimpleBooleanProperty leftInfoTables_visible = new SimpleBooleanProperty(true);

	private QuickPick_Navigator quickPick_Navigator;
	private Model_datefix model_datefix;
	private Model_main model_main;

	@FXML
	private Button cameras_hide_Deselected_btn;
	@FXML
	private Button cameras_show_all_btn;
	@FXML
	private Button setDateAsFileName_btn;
	@FXML
	private MenuButton move_menuBtn;
	@FXML
	private TableView<MetaData> metaDataTableView;
	@FXML
	private CheckBox ignored_chk;
	@FXML
	private CheckBox copied_chk;
	@FXML
	private CheckBox events_chk;
	@FXML
	private CheckBox locations_chk;
	@FXML
	private HBox infoTable_container_root;
	@FXML
	private VBox vbox_image_test2;
	@FXML
	private VBox vbox_image_test21;
	@FXML
	private VBox rightInfoPanel;
	@FXML
	private AnchorPane df_anchorPane;
	@FXML
	private GridPane df_gridPane;
	@FXML
	private ScrollPane df_scrollPane;
	@FXML
	private TextField filePath_tf;
	@FXML
	private TilePane quickPick_tilePane;

	@FXML
	private VBox vbox_image_test;
	@FXML
	private VBox vbox_image_test1;

	@FXML
	private Label bad_stat;
	@FXML
	private Label confirmed_stat;
	@FXML
	private Label good_stat;
	@FXML
	private Label images_stat;
	@FXML
	private Label suggested_stat;
	@FXML
	private Label videos_stat;
	@FXML
	private Button hideRightInfo_btn;
	@FXML
	private Button hideInfoTables_btn;
	@FXML
	private Button accept_dates_btn;
	@FXML
	private Button applyChanges_btn;
	@FXML
	private Button addToBatch_btn;
	@FXML
	private Button close_btn;
	@FXML
	private Button restoresExifDates_btn;
	@FXML
	private Button dateFromFileName_btn;
	@FXML
	private Button dateFix_btn;
	@FXML
	private Button folderize_btn;
	@FXML
	private Button lastModified_date_btn;
	@FXML
	private Button retrieveFileInfos_btn;
	@FXML
	private Button select_btn;
	@FXML
	private Button updateDate_btn;
	@FXML
	private Button copyToMisc_btn;
	@FXML
	private Button select_acceptable_btn;
	@FXML
	private Button listFileInfo_btn;
	@FXML
	private TableColumn<MetaData, String> info_column;
	@FXML
	private TableColumn<MetaData, String> value_column;
	@FXML
	private Button remove_btn;
	@FXML
	private ImageView remove;
	@FXML
	private Label selection_text;
	@FXML
	private Button select_all;
	@FXML
	private Button select_none_btn;
	@FXML
	private ScrollPane rightInfoPanel_scrollPane;
	@FXML
	private Label selected;
	// FXML needed! DateTimeAdjusterController
	@FXML
	DateTimeAdjusterController dateTimeAdjusterController;
	// FXML needed! SelectorController
	@FXML
	SelectorController selectorController;
	// FXML needed! TimeShiftController
	@FXML
	TimeShiftController timeShiftController;
	// FXML needed! FileOperationsController
	@FXML
	FileOperationsController fileOperationsController;

	@FXML
	private Button addToUnsorted_btn;

	@FXML
	private void addToUnsorted_btn_action(ActionEvent event) {
		if (model_datefix.getSelectionModel().getSelectionList().isEmpty()) {
			warningText(Main.bundle.getString("youHaventSelectedMedia"));
			return;
		}
		if (!Files.exists(Paths.get(Main.conf.getWorkDir()))) {
			warningText(Main.bundle.getString("workDirHasNotConnected"));
			return;
		}
		ConcurrencyUtils.stopExecThread();

		Messages.warningText("Not ready yet!");
	}

	@FXML
	private Button addToAsItIs_btn;

	@FXML
	private void addToAsItIs_btn_action(ActionEvent event) {
//		addToAsItIs
		// Copy files to workdir root as it is. If folder name is empty files will be
		// under Unsorted
		if (model_datefix.getSelectionModel().getSelectionList().isEmpty()) {
			warningText(Main.bundle.getString("youHaventSelectedMedia"));
			return;
		}
		if (!Files.exists(Paths.get(Main.conf.getWorkDir()))) {
			warningText(Main.bundle.getString("workDirHasNotConnected"));
			return;
		}
		ConcurrencyUtils.stopExecThread();

		Messages.warningText("Not ready yet!");
	}

	@FXML
	private void addToBatch_btn_action(ActionEvent event) {
		if (model_datefix.getSelectionModel().getSelectionList().isEmpty()) {
			warningText(Main.bundle.getString("youHaventSelectedMedia"));
			return;
		}
		if (!Files.exists(Paths.get(Main.conf.getWorkDir()))) {
			warningText(Main.bundle.getString("workDirHasNotConnected"));
			return;
		}
		ConcurrencyUtils.stopExecThread();

		List<FileInfo> fileInfo_list = new ArrayList<>();

		for (Node n : model_datefix.getSelectionModel().getSelectionList()) {
			if (n instanceof VBox && n.getId().equals("imageFrame")) {
				FileInfo fileInfo = (FileInfo) n.getUserData();
				Path source = Paths.get(fileInfo.getOrgPath());
				Path dest = DestinationResolver.getDestinationFileNameMisc(source, fileInfo);
				if (dest != null) {
					Messages.sprintf("blaaadestination is: " + dest);
					fileInfo.setWorkDir(Main.conf.getWorkDir());
					fileInfo.setWorkDirDriveSerialNumber(Main.conf.getWorkDirSerialNumber());
					fileInfo.setDestination_Path(dest.toString());
					fileInfo.setCopied(false);
					fileInfo_list.add(fileInfo);
				} else {
					Messages.sprintf("Dest were null. process is about to be cancelled");
					break;
				}
			}
		}

		for (Node n : model_datefix.getSelectionModel().getSelectionList()) {
			if (n instanceof VBox && n.getId().equals("imageFrame")) {
				FileInfo fileInfo = (FileInfo) n.getUserData();
				Messages.sprintf(
						"destination is: " + fileInfo.getDestination_Path() + " isCopied?: " + fileInfo.isCopied());
			}
		}
		Main.setChanged(true);
	}

	@FXML
	private void setBadDate_btn_action(ActionEvent event) {
		sprintf("setBadDate_btn_action: ");
		for (Node node : model_datefix.getSelectionModel().getSelectionList()) {
			sprintf("video_bad_btn_action: " + node.getId());

			if (node instanceof VBox && node.getId().equals("imageFrame")) {
				Node hboxi = node.lookup("#fileDate");
				sprintf("hboxi: " + hboxi);
				if (hboxi instanceof TextField) {
					FileInfo fileInfo = (FileInfo) node.getUserData();
					// model_datefix.getSelectionModel().add(node);
					FileInfo_Utils.setBad(fileInfo);
					hboxi.setStyle(CssStylesController.getBad_style());
				}
			}
		}

	}

	@FXML
	private void setModifiedDate_btn_action(ActionEvent event) {
		sprintf("setBadDate_btn_action: ");
		for (Node node : model_datefix.getSelectionModel().getSelectionList()) {
			sprintf("video_bad_btn_action: " + node.getId());

			if (node instanceof VBox && node.getId().equals("imageFrame")) {
				Node hboxi = node.lookup("#fileDate");
				sprintf("hboxi: " + hboxi);
				if (hboxi instanceof TextField) {
					FileInfo fileInfo = (FileInfo) node.getUserData();
					// model_datefix.getSelectionModel().add(node);
					TextField tf = (TextField) hboxi;
					// FileInfo_Utils.setBad(fileInfo);(fileInfo);
					tf.setStyle(CssStylesController.getModified_style());
				}
			}
		}
	}

	@FXML
	private void cameras_hide_Deselected_btn_action(ActionEvent event) {
		if (model_datefix.getSelectionModel().getSelectionList().isEmpty()) {
			Messages.warningText(Main.bundle.getString("youHaventSelectedMedia"));
			return;
		}
		LoadingProcess_Task loadingProcess_task = new LoadingProcess_Task(Main.scene_Switcher.getWindow());
		loadingProcess_task.setTask(null);
		UpdateGridPane_Task.updateGridPaneContent(model_datefix, model_datefix.getSelectionModel().getSelectionList(),
				loadingProcess_task);
	}

	@FXML
	private void cameras_show_all_btn_action(ActionEvent event) {

		model_datefix.getSelectionModel().clearAll();
		model_datefix.deselectAllExifDataSelectors();
		LoadingProcess_Task loadingProcess_task = new LoadingProcess_Task(Main.scene_Switcher.getWindow());
		UpdateGridPane_Task.updateGridPaneContent(model_datefix, model_datefix.getAllNodes(), loadingProcess_task);
		//
		// AddToGridPane2 apg2 = new AddToGridPane2(model_datefix,
		// model_datefix.getAllNodes(), lp);
		// new Thread(apg2).run();

	}

	@FXML
	private void folderize_btn_action(ActionEvent event) {
		if (model_datefix.getSelectionModel().getSelectionList().isEmpty()) {
			warningText(Main.bundle.getString("youHaventSelectedMedia"));
			return;
		} else {
			ConcurrencyUtils.stopExecThread();
			Parent parent = null;
			// rgwerg;
			try {
				FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/datefixer/AskEventDialog.fxml"),
						bundle);
				parent = loader.load();
				AskEventDialogController askEventDialogController = (AskEventDialogController) loader.getController();
				askEventDialogController.init(model_datefix);

				Scene scene = new Scene(parent);
				Stage stage = new Stage();
				// stage.initModality(Modality.APPLICATION_MODAL);
				stage.setScene(scene);
				stage.show();
				stage.setOnHiding(new EventHandler<WindowEvent>() {
					@Override
					public void handle(WindowEvent event) {
						model_datefix.getSelectionModel().clearAll();
					}
				});
				for (FileInfo fileInfo : model_main.getWorkDir_Handler().getWorkDir_List()) {
					Messages.sprintf("===========WORKDIR::::: FileInfo: " + fileInfo.getOrgPath());
				}
			} catch (Exception ex) {
				Logger.getLogger(DateFixerController.class.getName()).log(Level.SEVERE, null, ex);
				Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
			}
		}

	}

//	private void addToMiscFolder(FileInfo fileInfo) {
//		LocalDate ldl = DateUtils.longToLocalDateTime(fileInfo.getDate()).toLocalDate();
//		String fileName = DateUtils.longToLocalDateTime(fileInfo.getDate())
//				.format(Main.simpleDates.getDtf_ymd_hms_minusDots_default());
//		fileInfo.setWorkDir(Main.conf.getWorkDir());
//		fileInfo.setDestination_Path(File.separator + ldl.getYear() + File.separator
//				+ Conversion.formatStringTwoDigits(ldl.getMonthValue()) + File.separator + fileName + "."
//				+ FileUtils.getFileExtension(Paths.get(fileInfo.getOrgPath())));
//
//		ldl = null;
//
//	}

	@FXML
	private void dateFix_btn_action(ActionEvent event) {
		Path p = Paths.get(model_datefix.getFolderInfo_full().getFolderPath());
		Scene scene = dateFix_btn.getScene();
		sprintf("ImportImages about to start: " + p);
		ImportImages importImages = new ImportImages(scene, model_datefix.getFolderInfo_full(), model_main, false);
		// asc;
		// DateFixer dateFixer = new DateFixer(scene,
		// Paths.get(model_datefix.getFolderInfo_full().getFolderPath()),
		// model_datefix.getFolderInfo_full(), model_main);
		// Thread dateFixer_th = new Thread(dateFixer, "dateFixer_th");
		// sprintf("dateFixer_th.getName(): " + dateFixer_th.getName());
		// dateFixer_th.run();
	}

	@FXML
	private void dateFromFileName_btn_action(ActionEvent event) {
		model_datefix.dateFromFileName();
	}

	@FXML
	private void applyChanges_btn_action(ActionEvent event) {
		model_datefix.acceptEverything();
	}

	@FXML
	private void updateDate_btn_action(ActionEvent event) {
		Messages.warningText("retrieveFileInfos_btn_action Not ready yet");
	}

	@FXML
	private void retrieveFileInfos_btn_action(ActionEvent event) {
		if (model_datefix.getSelectionModel().getSelectionList().isEmpty()) {
			warningText(Main.bundle.getString("youHaventSelectedMedia"));
			return;
		}
		Messages.warningText("retrieveFileInfos_btn_action Not ready yet");
		for (Node node : model_datefix.getSelectionModel().getSelectionList()) {
			FileInfo fileInfo = (FileInfo) node.getUserData();
			if (fileInfo != null) {
				// asC;
				// aerb;
			}
		}
	}

	@FXML
	private void accept_dates_btn_action(ActionEvent event) {
		if (model_datefix.getSelectionModel().getSelectionList().isEmpty()) {
			warningText(Main.bundle.getString("youHaventSelectedMedia"));
			return;
		}
		for (Node node : model_datefix.getSelectionModel().getSelectionList()) {
			sprintf("Node is: " + node.getId() + " NODE ALL INFO: " + node.toString());
			FileInfo fileInfo = (FileInfo) node.getUserData();
			if (fileInfo != null) {
				File file = new File(fileInfo.getOrgPath());
				// long date = DateTaken.getMetadataDateTaken(file.toPath());
				Button accept = getAcceptButton(node);
				if (accept != null) {
					if (!accept.isDisabled()) {
						accept.fire();
					}
				}

				sprintf("Node name is: " + node + " LastMod was: " + DateUtils.longToLocalDateTime(file.lastModified())
						.format(Main.simpleDates.getDtf_ymd_hms_minusDots_default()));
			}
		}
	}

	/*
	 * Gets exifDate for selected files
	 */
	@FXML
	private void restoresExifDates_btn_action(ActionEvent event) {
		model_datefix.restoreSelectedExifDateInfos();
	}

	@FXML
	private void lastModified_date_btn_action(ActionEvent event) {
		model_datefix.restoreLastModified();
	}

	@FXML
	private void setDateAsFileName_btn_action(ActionEvent event) {
		model_datefix.dateAsFileName();
	}

	@FXML
	private void close_btn_action(ActionEvent event) {
		Messages.sprintf("Close button pressed");
		Main.scene_Switcher.getWindow().getOnCloseRequest()
				.handle(new WindowEvent(Main.scene_Switcher.getWindow(), WindowEvent.WINDOW_CLOSE_REQUEST));
	}

	private Button getAcceptButton(Node node) {
		if (node instanceof VBox) {
			if (node.getId().equals("imageFrame")) {
				for (Node node2 : ((VBox) node).getChildren()) {
					sprintf("Node2 : " + node2);
					if (node2 instanceof HBox) {
						for (Node node3 : ((HBox) node2).getChildren()) {
							sprintf("TextField: " + node3);
							if (node3 instanceof Button) {
								Button button = (Button) node3;
								if (button.getId().equals("accept")) {
									return button;
								}
							}
						}
					}
				}
			}
		}
		return null;
	}

	public void init(Model_datefix aModel_datefix, Model_main aModel_main, Path currentPath, FolderInfo folderInfo,
			boolean isImported) {
		this.model_datefix = aModel_datefix;
		this.model_main = aModel_main;
		// WorkDir_Handler workDir_Handler = new
		// WorkDir_Handler(Paths.get(Main.conf.getWorkDir()), false);
		// this.model_datefix.setWorkDir_Handler(workDir_Handler);
		// for (Entry<Path, FolderInfo> entry :
		// workDir_Handler.getFolderInfo_Map().entrySet()) {
		// Messages.sprintf("path: " + entry.getKey());
		// }
		// workDir_Handler.load_All_WorkDirSub(workDir, start_year, end_year)
		Main.setProcessCancelled(false);
//		rightInfo_visible hideInfoTables_btn.visibleProperty()
//		leftInfoTables_visible.bind(info.visibleProperty()); 
		this.model_datefix.setCurrentFolderPath(currentPath);
		this.model_datefix.setFolderInfo_full(folderInfo);
		this.model_datefix.setGridPane(df_gridPane);
		this.model_datefix.setScrollPane(df_scrollPane);
		this.model_datefix.setQuickPick_tilePane(quickPick_tilePane);
		this.model_datefix.setAnchorPane(df_anchorPane);
		if (isImported) {
			applyChanges_btn.setDefaultButton(true);
		}

		ignored_chk.selectedProperty().bindBidirectional(this.model_datefix.ignored_property());

		copied_chk.selectedProperty().bindBidirectional(this.model_datefix.copied_property());

		events_chk.selectedProperty().bindBidirectional(this.model_datefix.events_property());

		locations_chk.selectedProperty().bindBidirectional(this.model_datefix.locations_property());

		events_chk.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				LoadingProcess_Task loadingProcess_task = new LoadingProcess_Task(Main.scene_Switcher.getWindow());
				UpdateGridPane_Task.updateGridPaneContent(model_datefix,
						model_datefix.filterAllNodesList(model_datefix.getAllNodes()), loadingProcess_task);
			}
		});
		folderize_btn.disableProperty().bind(Main.conf.drive_connected_property().not());
		locations_chk.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				LoadingProcess_Task loadingProcess_task = new LoadingProcess_Task(Main.scene_Switcher.getWindow());
				UpdateGridPane_Task.updateGridPaneContent(model_datefix,
						model_datefix.filterAllNodesList(model_datefix.getAllNodes()), loadingProcess_task);
			}
		});
		copied_chk.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				LoadingProcess_Task loadingProcess_task = new LoadingProcess_Task(Main.scene_Switcher.getWindow());
				UpdateGridPane_Task.updateGridPaneContent(model_datefix,
						model_datefix.filterAllNodesList(model_datefix.getAllNodes()), loadingProcess_task);
			}
		});
		ignored_chk.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				LoadingProcess_Task loadingProcess_task = new LoadingProcess_Task(Main.scene_Switcher.getWindow());
				UpdateGridPane_Task.updateGridPaneContent(model_datefix,
						model_datefix.filterAllNodesList(model_datefix.getAllNodes()), loadingProcess_task);
			}
		});

		this.model_datefix.setRightInfoPanel(rightInfoPanel);

		df_gridPane.setId("dateFixer");
		df_gridPane.getChildren().clear();
		df_gridPane.getRowConstraints().removeAll();
		df_gridPane.getColumnConstraints().removeAll();
		filePath_tf.setText(this.model_datefix.getCurrentFolderPath().toString());

		dateTimeAdjusterController.init(model_main, aModel_datefix, df_gridPane, quickPick_tilePane);
		selectorController.init(aModel_datefix, df_gridPane);
		timeShiftController.init(aModel_datefix);

		fileOperationsController.init(aModel_datefix, aModel_main);

		sprintf("quickPick_tilePane= " + quickPick_tilePane);
		df_gridPane.heightProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				sprintf("grid height: " + newValue);
			}
		});
		df_scrollPane.vmaxProperty().bind(df_gridPane.heightProperty());
		bad_stat.textProperty().bind(model_datefix.getFolderInfo_full().badFiles_prop().asString());
		good_stat.textProperty().bind(model_datefix.getFolderInfo_full().goodFiles_prop().asString());
		images_stat.textProperty().bind(model_datefix.getFolderInfo_full().folderImageFiles_prop().asString());
		videos_stat.textProperty().bind(model_datefix.getFolderInfo_full().folderVideoFiles_prop().asString());
		suggested_stat.textProperty().bind(model_datefix.getFolderInfo_full().suggested_prop().asString());

		quickPick_Navigator = new QuickPick_Navigator(model_datefix, df_scrollPane, df_gridPane, quickPick_tilePane);
		model_datefix.setQuickPick_Navigator(quickPick_Navigator);
		model_datefix.instantiateRenderVisibleNodes();

		Main.scene_Switcher.getWindow().setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				Messages.sprintf("Close request pressed");
				model_datefix.getSelector_exec().shutdownNow();
				model_datefix.exitDateFixerWindow(model_datefix.getGridPane(), Main.scene_Switcher.getWindow(), event);
				// TODO KORJAA TÄMÄ EXITDATEFIXERWINDOW!

				// event.consume();
			}
		});
		model_datefix.getSelectionModel().getSelectionList().addListener(new ListChangeListener<Node>() {
			@Override
			public void onChanged(ListChangeListener.Change<? extends Node> c) {
				selected.setText("" + model_datefix.getSelectionModel().getSelectionList().size());
			}
		});
		/*
		 * info_column.setCellValueFactory((TableColumn.CellDataFeatures< MetaData,
		 * String> cellData) -> new
		 * SimpleObjectProperty<>(cellData.getValue().getTag()));
		 * value_column.setCellValueFactory( (TableColumn.CellDataFeatures<MetaData,
		 * String> cellData) -> new
		 * SimpleObjectProperty<>(cellData.getValue().getValue()));
		 */
		rightInfoPanel_scrollPane.setMinWidth(0);
		rightInfoPanel_scrollPane.setPrefWidth(0);
		rightInfoPanel_scrollPane.setMaxWidth(0);
		rightInfoPanel.setVisible(false);
		rightInfoPanel.setMinWidth(-100);
		rightInfoPanel.setMaxWidth(-100);
		rightInfoPanel.setPrefWidth(-100);

		selectorController.getInfoTables_container().setMinWidth(250);
		selectorController.getInfoTables_container().setMaxWidth(250);
		selectorController.getInfoTables_container().setPrefWidth(250);
	}

	@FXML
	private void listFileInfo_btn_action(ActionEvent event) {

		for (Node node : model_datefix.getGridPane().getChildren()) {
			if (node instanceof VBox && node.getId().equals("imageFrame")) {
				FileInfo fileInfo = (FileInfo) node.getUserData();
				Messages.sprintf("fileinfo: " + fileInfo.toString());
			}
		}
	}

	// @formatter:off
	/*
	 * ========================================================= Selection buttons
	 */
	// @formatter:on
	@FXML
	private void select_acceptable_btn_action(ActionEvent event) {
		sprintf("select_acceptable_btn_action");
		for (Node root : df_gridPane.getChildren()) {
			Node hboxi = root.lookup("#fileDate");
			if (hboxi instanceof TextField) {
				if (hboxi.getStyle().equals(CssStylesController.getSuggested_style())) {
					model_datefix.getSelectionModel().add(root);
				}
			}
		}

	}

	@FXML
	private void select_video_bad_btn_action(ActionEvent event) {
		Messages.sprintf("select_video_bad_btn_action");
		for (Node root : df_gridPane.getChildren()) {
			sprintf("video_good_btn_action: " + root);
			if (root instanceof VBox && root.getId().equals("imageFrame")) {
				FileInfo fileInfo = (FileInfo) root.getUserData();
				if (FileUtils.supportedVideo(Paths.get(fileInfo.getOrgPath()))) {
					if (fileInfo.isBad()) {
						model_datefix.getSelectionModel().add(root);
					}
				}
			}
		}
	}

	@FXML
	private void select_video_good_btn_action(ActionEvent event) {
		Messages.sprintf("select_video_good_btn_action");
		for (Node root : df_gridPane.getChildren()) {
			if (root instanceof VBox && root.getId().equals("imageFrame")) {
				FileInfo fileInfo = (FileInfo) root.getUserData();
				if (FileUtils.supportedVideo(Paths.get(fileInfo.getOrgPath()))) {
					if (fileInfo.isGood()) {
						model_datefix.getSelectionModel().add(root);
					}
				}
			}
		}
	}

	@FXML
	private void select_modified_video_btn_action(ActionEvent event) {
		Messages.sprintf("select_modified_video_btn_action");
		for (Node root : df_gridPane.getChildren()) {
			if (root instanceof VBox && root.getId().equals("imageFrame")) {
				FileInfo fileInfo = (FileInfo) root.getUserData();
				if (FileUtils.supportedVideo(Paths.get(fileInfo.getOrgPath()))) {
					Node hboxi = root.lookup("#fileDate");
					if (hboxi instanceof TextField) {
						TextField tf = (TextField) hboxi;
						if (tf != null) {
							if (tf.getStyle().equals(CssStylesController.getModified_style())) {
								model_datefix.getSelectionModel().add(root);
							}
						}
					}
				}
			}
		}
	}

	@FXML
	private void select_modified_btn_action(ActionEvent event) {
		Messages.sprintf("select_modified_btn_action");
		for (Node root : df_gridPane.getChildren()) {
			if (root instanceof VBox && root.getId().equals("imageFrame")) {
//				FileInfo fileInfo = (FileInfo) root.getUserData();
				Node hboxi = root.lookup("#fileDate");
				if (hboxi instanceof TextField) {
					TextField tf = (TextField) hboxi;
					if (tf != null) {
						if (tf.getStyle().equals(CssStylesController.getModified_style())) {
							model_datefix.getSelectionModel().add(root);
						}
					}
				}
			}
		}

	}

	@FXML
	private void select_bad_video_btn_action(ActionEvent event) {
		Messages.sprintf("select_bad_video_btn_action");
		for (Node root : df_gridPane.getChildren()) {
			sprintf("video_bad_btn_action: " + root);
			if (root instanceof VBox && root.getId().equals("imageFrame")) {
				FileInfo fileInfo = (FileInfo) root.getUserData();
				if (FileUtils.supportedVideo(Paths.get(fileInfo.getOrgPath()))) {
					for (Node hbox : ((VBox) root).getChildren()) {
						if (hbox instanceof HBox) {
							for (Node tff : ((HBox) hbox).getChildren()) {
								if (tff instanceof TextField) {
									TextField tf = (TextField) tff;
									if (tf != null) {
										if (tf.getStyle().equals(CssStylesController.getBad_style())) {
											model_datefix.getSelectionModel().add(root);
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	@FXML
	private void select_all_btn_action(ActionEvent event) {
		sprintf("select_all_btn_action");

		df_gridPane.getChildren().stream().filter((n) -> (n instanceof VBox && n.getId().equals("imageFrame")))
				.forEachOrdered((n) -> {
					model_datefix.getSelectionModel().addAll(n);
				});
//		FolderInfo fo = model_datefix.getFolderInfo_full();
//		for (FileInfo fileInfo : fo.getFileInfoList()) {
//			fileInfo.toString();
//		}
		ObservableList<EXIF_Data_Selector> listi = model_datefix.getCameras_TableView().getItems();

		for (EXIF_Data_Selector eds : listi) {
			eds.setIsShowing(false);
		}
	}

	@FXML
	private void select_bad_btn_action(ActionEvent event) {
		sprintf("select_bad_btn_action");
		for (Node root : df_gridPane.getChildren()) {
			Node hboxi = root.lookup("#fileDate");
			if (hboxi instanceof TextField) {
				if (hboxi.getStyle().equals(CssStylesController.getBad_style())) {
					model_datefix.getSelectionModel().add(root);
				}
			}
		}
	}

	@FXML
	private void select_good_btn_action(ActionEvent event) {
		sprintf("select_good_btn_action");
		for (Node root : df_gridPane.getChildren()) {
			if (root instanceof VBox) {
				for (Node vbox : ((VBox) root).getChildren()) {
					if (vbox instanceof HBox) {
						for (Node hbox : ((HBox) vbox).getChildren()) {
							if (hbox instanceof TextField) {
								if (hbox.getId().contains("fileDate")) {
									if (hbox.getStyle().equals(CssStylesController.getGood_style())) {
										model_datefix.getSelectionModel().add(root);
									}
								}
							}
						}
					}
				}
			}
		}
	}

	@FXML
	private void select_invert_btn_action(ActionEvent event) {
		sprintf("select_invert_btn_action");
		model_datefix.getSelectionModel().invertSelection(df_gridPane);
	}

	@FXML
	private void select_none_btn_action(ActionEvent event) {
		model_datefix.getSelectionModel().clearAll();
		for (EXIF_Data_Selector cam : model_datefix.getCameras_TableView().getItems()) {
			cam.setIsShowing(false);
		}
		for (EXIF_Data_Selector date : model_datefix.getDates_TableView().getItems()) {
			date.setIsShowing(false);
		}
		TableUtils.refreshTableContent(model_datefix.getCameras_TableView());
		TableUtils.refreshTableContent(model_datefix.getDates_TableView());
		TableUtils.refreshTableContent(model_datefix.getEvents_TableView());
		TableUtils.refreshTableContent(model_datefix.getLocations_TableView());
	}

	@FXML
	private void remove_btn_action(ActionEvent event) {
		Messages.sprintf("remove_btn_action");
		if (model_datefix.getSelectionModel().getSelectionList().isEmpty()) {
			warningText(Main.bundle.getString("youHaventSelectedMedia"));
			return;
		}

		Dialog<ButtonType> dialog = new Dialog<>();
		ButtonType yes = new ButtonType(Main.bundle.getString("yes"), ButtonData.YES);
		ButtonType no = new ButtonType(Main.bundle.getString("no"), ButtonData.NO);
		dialog.getDialogPane().getButtonTypes().addAll(yes, no);
		dialog.setContentText(Main.bundle.getString("doYouWantToIgnoreTheseFiles"));

		Optional<ButtonType> result = dialog.showAndWait();

		if (result.get().getButtonData().equals(ButtonBar.ButtonData.YES)) {
			Messages.sprintf("yes pressed!");
			boolean update = false;
			List<Node> toRemove = new ArrayList<>();
			for (Node n : model_datefix.getSelectionModel().getSelectionList()) {
				sprintf("remove_btn_action setIgnored. df_gridPane.getChildren(): " + n);
				if (n instanceof VBox && n.getId().equals("imageFrame")) {
					FileInfo fileInfo = (FileInfo) n.getUserData();
					fileInfo.setIgnored(true);
					toRemove.add(n);
					Main.setChanged(true);
					sprintf("remove_btn_action setIgnored. df_gridPane.getChildren(): " + n);
					update = true;
				}
			}
			if (update) {
				model_datefix.getGridPane().getChildren().removeAll(toRemove);
				LoadingProcess_Task loadingProcess_task = new LoadingProcess_Task(Main.scene_Switcher.getWindow());
				UpdateGridPane_Task.updateGridPaneContent(model_datefix,
						model_datefix.filterAllNodesList(model_datefix.getAllNodes()), loadingProcess_task);
			} else {
				Messages.sprintf("Nothing to update");
			}

		} else if (result.get().getButtonData().equals(ButtonBar.ButtonData.NO)) {
			Messages.sprintf("no pressed!");
			dialog.close();
		} else {
			Messages.errorSmth(ERROR, "Ei saatana!", null, Misc.getLineNumber(), true);
		}
		Messages.sprintf("EI TOIMIIII pressed!");
		// model_datefix.
	}

	// @formatter:off
	/*
	 * ============================================ Selection buttons==============
	 * ENDS
	 */

	@FXML
	private void hideInfoTables_btn_action(ActionEvent event) {
		if (leftInfoTables_visible.get()) {
			hideInfoTables_btn.setRotate(0);
			selectorController.getInfoTables_container().setVisible(false);
			selectorController.getInfoTables_container().setMinWidth(-100);
			selectorController.getInfoTables_container().setMaxWidth(-100);
			selectorController.getInfoTables_container().setPrefWidth(-100);
			leftInfoTables_visible.set(false);
		} else {
			hideInfoTables_btn.setRotate(180);
			selectorController.getInfoTables_container().setVisible(true);
			selectorController.getInfoTables_container().setMinWidth(250);
			selectorController.getInfoTables_container().setMaxWidth(250);
			selectorController.getInfoTables_container().setPrefWidth(250);
			leftInfoTables_visible.set(true);
		}
	}
	

	@FXML
	private void copyToMisc_btn_action(ActionEvent event) {
		if (model_datefix.getSelectionModel().getSelectionList().isEmpty()) {
			warningText(Main.bundle.getString("youHaventSelectedMedia"));
			return;
		}
		ConcurrencyUtils.stopExecThread();
	
		List<FileInfo> fileInfo_list = new ArrayList<>();

		for (Node n : model_datefix.getSelectionModel().getSelectionList()) {
			if (n instanceof VBox && n.getId().equals("imageFrame")) {
				FileInfo fileInfo = (FileInfo) n.getUserData();
				Path source = Paths.get(fileInfo.getOrgPath());
				Path dest = DestinationResolver.getDestinationFileNameMisc(source, fileInfo);
				if (dest != null && Main.conf.getDrive_connected()) {
					Messages.sprintf("copyToMisc_btn_action dest is: " + dest);
					fileInfo.setWorkDir(Main.conf.getWorkDir());
					fileInfo.setWorkDirDriveSerialNumber(Main.conf.getWorkDirSerialNumber());
					fileInfo.setDestination_Path(dest.toString());
					fileInfo.setCopied(false);
					fileInfo_list.add(fileInfo);
				} else {
					Messages.sprintf("Dest were null. process is about to be cancelled");
					break;
				}
			}
		}

		Task<Boolean> operateFiles = new OperateFiles(fileInfo_list, true, model_main,
				Scene_NameType.DATEFIXER.getType());
		operateFiles.setOnSucceeded((workerStateEvent) -> {
//			operateFiles.get
			Messages.sprintf("operateFiles Succeeded");
		});
		operateFiles.setOnCancelled((workerStateEvent) -> {
			Messages.sprintf("operateFiles CANCELLED");
		});
		operateFiles.setOnFailed((workerStateEvent) -> {
			Messages.sprintf("operateFiles FAILED");
			Main.setProcessCancelled(true);
		});
		Thread operateFiles_th = new Thread(operateFiles, "operateFiles_th");
		operateFiles_th.setDaemon(true);
		operateFiles_th.start();

	}

	@FXML
	private void hideRightInfo_btn_action(ActionEvent event) {
		if (model_datefix.getRightInfo_visible()) {
			hideRightInfo_btn.setRotate(0);
			rightInfoPanel_scrollPane.setVisible(false);

			rightInfoPanel_scrollPane.setMinWidth(0);
			rightInfoPanel_scrollPane.setPrefWidth(0);
			rightInfoPanel_scrollPane.setMaxWidth(0);
			rightInfoPanel.setVisible(false);
			rightInfoPanel.setMinWidth(-100);
			rightInfoPanel.setMaxWidth(-100);
			rightInfoPanel.setPrefWidth(-100);
			model_datefix.setRightInfo_visible(false);
		} else {
			hideRightInfo_btn.setRotate(180);
			rightInfoPanel_scrollPane.setVisible(true);
			rightInfoPanel_scrollPane.setMinWidth(250);
			rightInfoPanel_scrollPane.setPrefWidth(250);
			rightInfoPanel_scrollPane.setMaxWidth(250);
			rightInfoPanel.setVisible(true);
			rightInfoPanel.setMinWidth(250);
			rightInfoPanel.setMaxWidth(250);
			rightInfoPanel.setPrefWidth(250);
			model_datefix.setRightInfo_visible(true);
		}
	}

}
