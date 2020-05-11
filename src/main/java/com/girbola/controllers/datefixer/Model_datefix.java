/*
 @(#)Copyright:  Copyright (c) 2012-2020 All right reserved.
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.datefixer;

import static com.girbola.Main.bundle;
import static com.girbola.Main.simpleDates;
import static com.girbola.messages.Messages.errorSmth;
import static com.girbola.messages.Messages.sprintf;
import static com.girbola.messages.Messages.warningText;
import static com.girbola.misc.Misc.getLineNumber;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.girbola.Main;
import com.girbola.concurrency.ConcurrencyUtils;
import com.girbola.controllers.datefixer.DateFix_Utils.Field;
import com.girbola.controllers.main.Model_main;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.dialogs.Dialogs;
import com.girbola.fileinfo.FileInfo;
import com.girbola.fileinfo.FileInfo_Utils;
import com.girbola.fileinfo.ThumbInfo;
import com.girbola.fileinfo.ThumbInfo_Utils;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.sql.SQL_Utils;
import com.girbola.sql.SqliteConnection;
import com.girbola.workdir.WorkDir_Handler;

import common.media.DateTaken;
import common.utils.Conversion;
import common.utils.FileNameParseUtils;
import common.utils.FileUtils;
import common.utils.date.DateUtils;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.util.StringConverter;

/**
 *
 * @author Marko Lokka
 */
public class Model_datefix {
	private final String ERROR = Model_datefix.class.getSimpleName();
	private Model_main model_Main;

	private ScheduledExecutorService selector_exec = Executors.newScheduledThreadPool(1);

	private VBox infoTables_container;
	private ObservableList<MetaData> metaDataTableView_obs = FXCollections.observableArrayList();
	private BooleanProperty ignored = new SimpleBooleanProperty(false);
	private BooleanProperty copied = new SimpleBooleanProperty(false);
	private BooleanProperty events = new SimpleBooleanProperty(true);
	private BooleanProperty locations = new SimpleBooleanProperty(true);

	private AtomicBoolean content_changed = new AtomicBoolean(false);
	private ObservableHandler observableHandler = new ObservableHandler();

	private TimeControl s_time = new TimeControl();
	private TimeControl e_time = new TimeControl();

	private AnchorPane anchorPane;
	private FolderInfo folderInfo_full;
	// private FolderInfo folderInfo_full;
	private FolderInfo folderInfo_filtered;

	private GridPane gridPane;
	private TableView<EXIF_Data_Selector> cameras_TableView;
	private TableView<EXIF_Data_Selector> dates_TableView;
	private TableView<EXIF_Data_Selector> events_TableView;
	private TableView<EXIF_Data_Selector> locations_TableView;

	private VBox rightInfoPanel;
	private TableView<MetaData> metaDataTableView;

	private ObservableList<Node> allNodes = FXCollections.observableArrayList();
	private ObservableList<Node> currentNodes = FXCollections.observableArrayList();
	private CssStylesController cssStyles = new CssStylesController();
	private DateFix_Utils dateFix_Utils = new DateFix_Utils();

	private Path currentFolderPath;
	private QuickPick_Navigator quickPick_Navigator;
	private ScrollPane scrollPane;
	private SelectionModel selectionModel = new SelectionModel();
	private TilePane quickPick_tilePane;
	private int imagesPerLine;

	private DatePicker start_datePicker;
	private DatePicker end_datePicker;

	private WorkDir_Handler workDir_Handler;

	private RenderVisibleNode renderVisibleNode = null;
	private Connection connection;

	public Model_datefix(Model_main model_Main, Path aCurrentFolderPath) {
		this.currentFolderPath = aCurrentFolderPath;
		Messages.sprintf("Model_datefix loading... " + currentFolderPath);
		this.model_Main = model_Main;
		this.connection = SqliteConnection.connector(currentFolderPath, Main.conf.getFileInfo_db_fileName());
		// this.thumbInfo_list = SQL_Utils.loadThumbInfo_list(this.connection);
		// if (this.thumbInfo_list != null) {
		// Messages.sprintf("thumbInfo_list were loaded!");
		// } else {
		// Messages.sprintf("thumbInfo_list were not loaded");
		// }

	}

	public AtomicBoolean getContent_changed() {
		return content_changed;
	}

	public void setContent_changed(AtomicBoolean content_changed) {
		this.content_changed = content_changed;
	}

	private ObservableList<String> workDir_obs = FXCollections.observableArrayList();

	/**
	 * @return the workDir_obs
	 */
	public final ObservableList<String> getWorkDir_obs() {
		return workDir_obs;
	}

	public DateFix_Utils getDateFix_Utils() {
		return dateFix_Utils;
	}

	public final ObservableList<Node> getCurrentNodes() {
		return currentNodes;
	}

	public final void setCurrentNodes(ObservableList<Node> currentNodes) {
		this.currentNodes = currentNodes;
	}

	public void instantiateRenderVisibleNodes() {
		if (renderVisibleNode == null) {
			renderVisibleNode = new RenderVisibleNode(scrollPane, currentFolderPath, this.connection);
		}
	}

	public RenderVisibleNode getRenderVisibleNode() {
		return renderVisibleNode;
	}

	public TableView<EXIF_Data_Selector> getCameras_TableView() {
		return cameras_TableView;
	}

	public void setCameras_TableView(TableView<EXIF_Data_Selector> cameras_TableView) {
		this.cameras_TableView = cameras_TableView;
	}

	public TableView<EXIF_Data_Selector> getDates_TableView() {
		return dates_TableView;
	}

	public void setDates_TableView(TableView<EXIF_Data_Selector> dates_TableView) {
		this.dates_TableView = dates_TableView;
	}

	public int getImagesPerLine() {
		return imagesPerLine;
	}

	public void setImagesPerLine(int imagesPerLine) {
		this.imagesPerLine = imagesPerLine;
	}

	public ObservableList<Node> getAllNodes() {
		return allNodes;
	}

	public void setAllNodes(ObservableList<Node> allNodes) {
		this.allNodes = allNodes;
	}

	public QuickPick_Navigator getQuickPick_Navigator() {
		return this.quickPick_Navigator;
	}

	public void setQuickPick_Navigator(QuickPick_Navigator aQuickPick_Navigator) {
		this.quickPick_Navigator = aQuickPick_Navigator;
	}

	public AnchorPane getAnchorPane() {
		return anchorPane;
	}

	public void setAnchorPane(AnchorPane anchorPane) {
		this.anchorPane = anchorPane;
	}

	public ScrollPane getScrollPane() {
		return scrollPane;
	}

	public void setScrollPane(ScrollPane scrollPane) {
		this.scrollPane = scrollPane;
	}

	public TilePane getQuickPick_tilePane() {
		return quickPick_tilePane;
	}

	public void setQuickPick_tilePane(TilePane quickPick_tilePane) {
		this.quickPick_tilePane = quickPick_tilePane;
	}

	public GridPane getGridPane() {
		return gridPane;
	}

	public void setGridPane(GridPane gridPane) {
		this.gridPane = gridPane;
	}

	public FolderInfo getFolderInfo_full() {
		return folderInfo_full;
	}

	public void setFolderInfo_full(FolderInfo aCurrentFolderInfo) {
		this.folderInfo_full = aCurrentFolderInfo;
	}

	public void setCurrentFolderPath(Path currentFilePath) {
		this.currentFolderPath = currentFilePath;
	}

	public Path getCurrentFolderPath() {
		return currentFolderPath;
	}

	public SelectionModel getSelectionModel() {
		return this.selectionModel;
	}

	public TimeControl start_time() {
		return this.s_time;
	}

	public TimeControl end_time() {
		return this.e_time;
	}

	public void setDateTime(String date, boolean start) {
		sprintf("SetDateTime: " + date);
		if (start) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					getStart_datePicker().setValue(DateUtils.parseLocalDateFromString(date));
					s_time.setTime(date);
				}
			});
		} else {
			Platform.runLater(new Runnable() {

				@Override
				public void run() {

					getEnd_datePicker().setValue(DateUtils.parseLocalDateFromString(date));
					e_time.setTime(date);
				}

			});
		}
	}

	public void setStart_datePicker(DatePicker start_datePicker) {
		this.start_datePicker = start_datePicker;
		this.start_datePicker.setConverter(converter);
		this.start_datePicker.getEditor().textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				sprintf("1s_datePicker: " + newValue);
			}
		});
	}

	public void setEnd_datePicker(DatePicker end_datePicker) {
		this.end_datePicker = end_datePicker;
		this.end_datePicker.setConverter(converter);
		this.end_datePicker.getEditor().textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				sprintf("e_datePicker: " + newValue);
			}
		});
	}

	public DatePicker getEnd_datePicker() {
		return end_datePicker;
	}

	public DatePicker getStart_datePicker() {
		return start_datePicker;
	}

	StringConverter<LocalDate> converter = new StringConverter<LocalDate>() {

		@Override
		public String toString(LocalDate date) {
			if (date != null) {
				return simpleDates.getDtf_ymd_minus().format(date);
			} else {
				return "";
			}
		}

		@Override
		public LocalDate fromString(String string) {
			if (string != null && !string.isEmpty()) {
				return LocalDate.parse(string, simpleDates.getDtf_ymd_minus());
			} else {
				return null;
			}
		}
	};

	/**
	 * getLocalDateTime read datePicker time and gets time from DateFixer Time
	 * chooser
	 *
	 * If parameter start is true it will read start_datePicker and start time and
	 * if it is false it will read end_datePicker and end time. Example: Start
	 * date/time 2018/11/08 12:00:00 End date/time 2018/11/09 12:30:00 It will
	 * combine there values as a one LocalDateTime
	 *
	 * @param start
	 * @return
	 */
	public LocalDateTime getLocalDateTime(boolean start) {
		if (start) {
			return LocalDateTime.of(start_datePicker.getValue(),
					LocalTime.of(start_time().getHour(), start_time().getMin(), start_time().getSec()));
		}
		return LocalDateTime.of(end_datePicker.getValue(),
				LocalTime.of(end_time().getHour(), end_time().getMin(), end_time().getSec()));
	}

	public FolderInfo getFolderInfo_filtered() {
		return folderInfo_filtered;
	}

	public void setFolderInfo_filtered(FolderInfo folderInfo_filtered) {
		this.folderInfo_filtered = folderInfo_filtered;
	}

	/**
	 * @return the observableHandler
	 */
	public final ObservableHandler getObservableHandler() {
		return observableHandler;
	}
	//
	// /**
	// * @return the changes
	// */
	// public final SimpleBooleanProperty getChanges() {
	// return changes;
	// }

	public TableView<EXIF_Data_Selector> getEvents_TableView() {
		return events_TableView;
	}

	public TableView<EXIF_Data_Selector> getLocations_TableView() {
		return locations_TableView;
	}

	public void setEvents_TableView(TableView<EXIF_Data_Selector> table) {
		this.events_TableView = table;
	}

	public void setLocations_TableView(TableView<EXIF_Data_Selector> table) {
		this.locations_TableView = table;
	}

	public void setWorkDir_Handler(WorkDir_Handler workDir_Handler) {
		this.workDir_Handler = workDir_Handler;
	}

	public WorkDir_Handler getWorkDir_Handler() {
		return workDir_Handler;
	}

	public void setInfoTables_container(VBox infoTables_container) {
		this.infoTables_container = infoTables_container;
	}

	/**
	 * @return the infoTables_container
	 */
	public VBox getInfoTables_container() {
		return infoTables_container;
	}

	/**
	 * @return the rightInfoPanel
	 */
	public VBox getRightInfoPanel() {
		return rightInfoPanel;
	}

	/**
	 * @param rightInfoPanel the rightInfoPanel to set
	 */
	public void setRightInfoPanel(VBox rightInfoPanel) {
		this.rightInfoPanel = rightInfoPanel;
	}

	public TableView<MetaData> getMetaDataTableView() {
		return this.metaDataTableView;
	}

	public void setMetaDataTableView(TableView<MetaData> metadataTableView) {
		this.metaDataTableView = metadataTableView;
	}

	public ObservableList<MetaData> getMetaDataTableView_obs() {
		return this.metaDataTableView_obs;
	}

	public void updateCameraInfos(List<FileInfo> fileInfo_List) {
		getCameras_TableView().getItems().clear();
		getDateFix_Utils().createTableEXIF_Data_Selector_list(fileInfo_List, getCameras_TableView().getItems(),
				Field.CAMERA.getType());

	}

	public void updateDateInfos(List<FileInfo> fileInfo_List) {
		getDates_TableView().getItems().clear();
		getDateFix_Utils().createDates_list(fileInfo_List);
	}

	public void updateLocationInfos(List<FileInfo> fileInfo_List) {
		getLocations_TableView().getItems().clear();
		getDateFix_Utils().createTableEXIF_Data_Selector_list(fileInfo_List, getLocations_TableView().getItems(),
				Field.LOCATION.getType());

	}

	public void updateEventsInfos(List<FileInfo> fileInfo_List) {
		getEvents_TableView().getItems().clear();
		getDateFix_Utils().createTableEXIF_Data_Selector_list(fileInfo_List, getEvents_TableView().getItems(),
				Field.EVENT.getType());
	}

	public void updateAllInfos(List<FileInfo> fileInfo_List) {
		getCameras_TableView().getItems().clear();
		getDates_TableView().getItems().clear();
		getEvents_TableView().getItems().clear();
		getLocations_TableView().getItems().clear();

		updateCameraInfos(fileInfo_List);
		updateDateInfos(fileInfo_List);
		updateEventsInfos(fileInfo_List);
		updateLocationInfos(fileInfo_List);
	}

	public void updateAllInfos(GridPane gridPane) {
		getCameras_TableView().getItems().clear();
		getDates_TableView().getItems().clear();
		getEvents_TableView().getItems().clear();
		getLocations_TableView().getItems().clear();

		updateCameraInfo(gridPane);
		updateDatesInfos(gridPane);
		updateEventsInfos(gridPane);
		updateLocationsInfos(gridPane);
	}

	public void updateCameraInfo(GridPane gridPane) {

		List<FileInfo> fileInfo_list = new ArrayList<>();
		for (Node node : gridPane.getChildren()) {
			if (node instanceof VBox) {
				FileInfo fileInfo = (FileInfo) node.getUserData();
				if (fileInfo != null) {
					fileInfo_list.add(fileInfo);
				}
			}
		}
		if (getCameras_TableView() == null) {
			Messages.sprintfError("Cameras TableView were null!!!!");
		} else {
			dateFix_Utils.createTableEXIF_Data_Selector_list(fileInfo_list, getCameras_TableView().getItems(),
					Field.CAMERA.getType());
		}
	}

	public void updateEventsInfos(GridPane gridPane) {
		List<FileInfo> fileInfo_list = new ArrayList<>();
		for (Node node : gridPane.getChildren()) {
			if (node instanceof VBox) {
				FileInfo fileInfo = (FileInfo) node.getUserData();
				if (fileInfo != null) {
					fileInfo_list.add(fileInfo);
				}
			}
		}
		dateFix_Utils.createTableEXIF_Data_Selector_list(fileInfo_list, getEvents_TableView().getItems(),
				Field.EVENT.getType());
	}

	public void updateLocationsInfos(GridPane gridPane) {
		List<FileInfo> fileInfo_list = new ArrayList<>();
		for (Node node : gridPane.getChildren()) {
			if (node instanceof VBox) {
				FileInfo fileInfo = (FileInfo) node.getUserData();
				if (fileInfo != null) {
					fileInfo_list.add(fileInfo);
				}
			}
		}
		dateFix_Utils.createTableEXIF_Data_Selector_list(fileInfo_list, getLocations_TableView().getItems(),
				Field.LOCATION.getType());
	}

	public void updateDatesInfos(GridPane gridPane) {
		List<FileInfo> fileInfo_list = new ArrayList<>();
		for (Node node : gridPane.getChildren()) {
			if (node instanceof VBox && node.getId().equals("imageFrame")) {
				FileInfo fileInfo = (FileInfo) node.getUserData();
				if (fileInfo != null) {
					fileInfo_list.add(fileInfo);
				} else {
					Messages.errorSmth(ERROR, "fileinfo were null!", null, Misc.getLineNumber(), true);
				}
			}
		}
		// wef;
		dateFix_Utils.createDates_list(fileInfo_list);
	}

	public void acceptEverything() {
		boolean changed = false;
		// CssStylesController css = new CssStylesController();
		Dialog<ButtonType> changesDialog = Dialogs
				.createDialog_YesNo(Main.scene_Switcher.getScene_dateFixer().getWindow(), bundle.getString("iHaveCheckedEverythingAndAcceptAllChanges"));
		Optional<ButtonType> result = changesDialog.showAndWait();
		if (result.get().getButtonData().equals(ButtonBar.ButtonData.YES)) {
			for (Node node : getGridPane().getChildren()) {
				TextField tf = getTextField(node);
				if (tf != null) {
					if (!tf.getStyle().equals(CssStylesController.getBad_style())) {
						FileInfo fileInfo = (FileInfo) node.getUserData();
						if (fileInfo != null) {
							if (!fileInfo.isIgnored()) {
								fileInfo.setDate(Conversion.stringDateToLong(tf.getText(),
										simpleDates.getSdf_ymd_hms_minusDots_default()));
								fileInfo.setGood(true);
								fileInfo.setSuggested(false);
								fileInfo.setBad(false);
								fileInfo.setConfirmed(true);
								node.setStyle(CssStylesController.getGood_style());
								changed = true;
							}
						}
					}
				}
			}
			if (changed) {
				// TODO Korjaa apply_btn;
				TableUtils.updateFolderInfos_FileInfo(getFolderInfo_full());
				if (model_Main.tables() == null) {
					Main.setProcessCancelled(true);
					errorSmth(ERROR, "", null, Misc.getLineNumber(), true);
				}
				model_Main.tables().refreshAllTables();
				updateAllInfos(getFolderInfo_full().getFileInfoList());

				getFolderInfo_full().setChanged(true);
				setDateTime(getFolderInfo_full().getMinDate(), true);
				setDateTime(getFolderInfo_full().getMaxDate(), false);
			}
			getSelectionModel().clearAll();
		}
		/*
		
		*/
	}

	public TextField getTextField(Node node) {
		if (node instanceof VBox) {
			if (node.getId().equals("imageFrame")) {
				for (Node node2 : ((VBox) node).getChildren()) {
					sprintf("Node2 : " + node2);
					if (node2 instanceof HBox) {
						for (Node node3 : ((HBox) node2).getChildren()) {
							sprintf("TextField: " + node3);
							if (node3 instanceof TextField) {
								return (TextField) node3;
							}
						}
					}
				}
			}
		}
		return null;
	}

	public void dateFromFileName() {
		if (getSelectionModel().getSelectionList().isEmpty()) {
			warningText(Main.bundle.getString("youHaventSelectedMedia"));
			return;
		}
		for (Node node : getSelectionModel().getSelectionList()) {
			FileInfo fileInfo = (FileInfo) node.getUserData();
			if (fileInfo != null) {
				long date = FileNameParseUtils.hasFileNameDate(Paths.get(fileInfo.getOrgPath()));
				TextField tf = getTextField(node);
				if (date != 0) {
					if (tf != null) {
						tf.setText("" + DateUtils.longToLocalDateTime(date)
								.format(Main.simpleDates.getDtf_ymd_hms_minusDots_default()));
						tf.setStyle(CssStylesController.getGood_style());
					}
				} else {
					tf.setStyle(CssStylesController.getBad_style());
				}
			}
		}
	}

	public void restoreSelectedExifDateInfos() {

		if (getSelectionModel().getSelectionList().isEmpty()) {
			warningText(Main.bundle.getString("youHaventSelectedMedia"));
			return;
		}
		for (Node node : getSelectionModel().getSelectionList()) {
			sprintf("Node is: " + node.getId() + " NODE ALL INFO: " + node.toString());
			FileInfo fileInfo = (FileInfo) node.getUserData();
			if (fileInfo != null) {
				File file = new File(fileInfo.getOrgPath());
				try {
					fileInfo = FileInfo_Utils.createFileInfo(file.toPath());
				} catch (IOException e) {
					e.printStackTrace();
				}

//				long date = DateTaken.getCreationDate(file.toPath());

				// sdv;
				sprintf("Node name is: " + node + " LastMod was: " + DateUtils.longToLocalDateTime(fileInfo.getDate())
						.format(Main.simpleDates.getDtf_ymd_hms_minusDots_default()));

				TextField tf = getTextField(node);
				if (fileInfo.getDate() != 0) {
					if (tf != null) {
						tf.setText("" + DateUtils.longToLocalDateTime(fileInfo.getDate())
								.format(Main.simpleDates.getDtf_ymd_hms_minusDots_default()));
						tf.setStyle(CssStylesController.getGood_style());
					}
				} else {
					// tf.setText("" +
					// DateUtils.longToLocalDateTime(file.lastModified()).format(Main.simpleDates.getDtf_ymd_hms_minusDots_default()));
					tf.setStyle(CssStylesController.getBad_style());
				}
			}
		}
	}

	public void restoreLastModified() {
		sprintf("lastModified_date_btn pressed");
		if (getSelectionModel().getSelectionList().isEmpty()) {
			warningText(Main.bundle.getString("youHaventSelectedMedia"));
			return;
		}
		for (Node node : getSelectionModel().getSelectionList()) {
			sprintf("Node is: " + node.getId() + " NODE ALL INFO: " + node.toString());
			FileInfo fileInfo = (FileInfo) node.getUserData();
			if (fileInfo != null) {
				File file = new File(fileInfo.getOrgPath());
				sprintf("Node name is: " + node + " LastMod was: " + DateUtils.longToLocalDateTime(file.lastModified())
						.format(Main.simpleDates.getDtf_ymd_hms_minusDots_default()));
				TextField tf = getTextField(node);
				if (tf != null) {
					tf.setText("" + DateUtils.longToLocalDateTime(file.lastModified())
							.format(Main.simpleDates.getDtf_ymd_hms_minusDots_default()));
					tf.setStyle(CssStylesController.getModified_style());
				}
			}
		}
	}

	public void exitDateFixerWindow(GridPane gridPane, Window owner, WindowEvent event) {
		Messages.sprintf("exitDateFixerWindow");
		model_Main.getMonitorExternalDriveConnectivity().cancel();

		int badDates = checkIfRedDates(gridPane);
		if (badDates != 0) {
			Dialog<ButtonType> dialog = Dialogs.createDialog_YesNoCancel(owner,
					bundle.getString("badFilesFoundWantToClose"));

			Messages.sprintf("2changesDialog width: " + dialog.getWidth());
			Optional<ButtonType> result = dialog.showAndWait();
			if (result.get().getButtonData().equals(ButtonBar.ButtonData.YES)) {
				Stage stage = (Stage) Main.scene_Switcher.getScene_dateFixer().getWindow();
				stage.setScene(Main.scene_Switcher.getScene_dateFixer());
				saveThumbs();
				event.consume();
//				return;
			} else if (result.get().getButtonData().equals(ButtonBar.ButtonData.CANCEL_CLOSE)) {
				event.consume();
				return;
			}
		}
		if (Main.conf.isSavingThumb()) {
			saveThumbs();
		}
		if (Main.getChanged()) {
			Messages.sprintf("changes made");
			Dialog<ButtonType> changesDialog = Dialogs.createDialog_YesNoCancel(owner, bundle.getString("changesMade"));
			Messages.sprintf("changesDialog width: " + changesDialog.getWidth());
			Optional<ButtonType> result = changesDialog.showAndWait();
			if (result.get().getButtonData().equals(ButtonBar.ButtonData.YES)) {
				acceptEverything();
				Stage stage = (Stage) Main.scene_Switcher.getScene_dateFixer().getWindow();
				stage.setScene(Main.scene_Switcher.getScene_dateFixer());
				Main.scene_Switcher.getWindow().setOnCloseRequest(model_Main.exitProgram);
				model_Main.getMonitorExternalDriveConnectivity().restart();
				Platform.runLater(() -> {
					Main.scene_Switcher.getWindow().setOnCloseRequest(model_Main.exitProgram);
					Main.scene_Switcher.getWindow().setScene(Main.scene_Switcher.getScene_main());
				});

				event.consume();
//				return;
			} else if (result.get().getButtonData().equals(ButtonBar.ButtonData.NO)) {
//				Stage stage = (Stage) Main.scene_Switcher.getScene_dateFixer().getWindow();
//				stage.setScene(Main.scene_Switcher.getScene_dateFixer());
//				Main.scene_Switcher.getWindow().setOnCloseRequest(model_Main.exitProgram);
				Messages.sprintf("No PRESSED: NO changes saved");

				Main.scene_Switcher.getWindow().setOnCloseRequest(model_Main.exitProgram);
				model_Main.getMonitorExternalDriveConnectivity().restart();
				Platform.runLater(() -> {
					Main.scene_Switcher.getWindow().setOnCloseRequest(model_Main.exitProgram);
					Main.scene_Switcher.getWindow().setScene(Main.scene_Switcher.getScene_main());
				});
				event.consume();
			} else if (result.get().getButtonData().equals(ButtonBar.ButtonData.CANCEL_CLOSE)) {
				Messages.sprintf("Cancel pressed!");
				event.consume();
				return;
			}
		} else {
			Messages.sprintf("No changes made");
			Main.scene_Switcher.getWindow().setScene(Main.scene_Switcher.getScene_dateFixer());
			Main.scene_Switcher.getWindow().setOnCloseRequest(model_Main.exitProgram);
			event.consume();
		}
		getSelectionModel().clearAll();
		selector_exec.shutdownNow();
		Platform.runLater(() -> {
			Main.scene_Switcher.getWindow().setScene(Main.scene_Switcher.getScene_main());
			Messages.sprintf("Changing back to main");
		});

	}

	public void saveThumbs() {
		if (!Main.conf.isSavingThumb()) {
			Messages.sprintf("isSavingThumb() were turned off");
			return;
		}
		Main.setProcessCancelled(true);
		getRenderVisibleNode().stopTimeLine();
		ConcurrencyUtils.stopExecThreadNow();
		List<ThumbInfo> thumbInfo_list = new ArrayList<>();
		for (Node n : getGridPane().getChildren()) {
			if (n instanceof VBox) {
				for (Node vbox : ((VBox) n).getChildren()) {
					if (vbox instanceof StackPane) {
						ImageView iv = (ImageView) vbox.lookup("#imageView");
						if (iv.getImage() != null) {
							FileInfo fi = (FileInfo) n.getUserData();
							if (FileUtils.supportedVideo(Paths.get(fi.getOrgPath()))) {
								if (iv.getUserData() instanceof List<?>) {
									Messages.sprintf("iv.getUserData() was instanceof List<?> ? is BufferedImage");
									ThumbInfo thumbInfo = ThumbInfo_Utils.findThumbInfo(thumbInfo_list,
											fi.getFileInfo_id());
									if (thumbInfo == null) {
										thumbInfo = new ThumbInfo(fi.getOrgPath(), fi.getFileInfo_id());
									}
									@SuppressWarnings("unchecked")
									List<BufferedImage> buffList = (List<BufferedImage>) iv.getUserData();
									Messages.sprintf("buffList size is: " + buffList);
									if (buffList != null) {
										for (BufferedImage bufImage : buffList) {
											try {
												ByteArrayOutputStream baos = new ByteArrayOutputStream();
												ImageIO.write(bufImage, "jpg", baos);
												baos.flush();
												byte[] imageInByte = baos.toByteArray();
												baos.close();
												thumbInfo.getThumbs().add(imageInByte);
											} catch (Exception e) {
												e.printStackTrace();
											}
										}
									} else {
										Messages.sprintfError("BufferedImage list were empty! " + fi.getOrgPath());
									}
								}
							} else {
								Messages.sprintf("Picture: " + fi.getOrgPath());
								ThumbInfo thumbInfo = ThumbInfo_Utils.findThumbInfo(thumbInfo_list,
										fi.getFileInfo_id());
								if (thumbInfo == null) {
									thumbInfo = new ThumbInfo(fi.getOrgPath(), fi.getFileInfo_id());
								}
								if (thumbInfo.getThumbs().isEmpty()) {
									WritableImage writableImage = iv.snapshot(new SnapshotParameters(), null);
									thumbInfo.setThumb_fast_width(writableImage.getWidth());
									thumbInfo.setThumb_fast_height(writableImage.getHeight());
									ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();

									try {
										ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), "png",
												byteArrayOS);
									} catch (IOException e) {
										e.printStackTrace();
									}
									byte[] res = byteArrayOS.toByteArray();
									try {
										byteArrayOS.close();
									} catch (IOException ex) {
										Logger.getLogger(DateFixer.class.getName()).log(Level.SEVERE, null, ex);
									}
									thumbInfo.getThumbs().add(res);
									thumbInfo_list.add(thumbInfo);
								}
							}
						}
					}
				}
			}
		}

		Messages.sprintf("Thumbinfo list size is: " + thumbInfo_list.size());
		SQL_Utils.insertThumbInfoListToDatabase(connection, thumbInfo_list);
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean hasThumbInfo(List<ThumbInfo> thumbInfo_list, FileInfo fi) {
		for (ThumbInfo thumbInfo : thumbInfo_list) {
			if (thumbInfo.getId() == fi.getFileInfo_id() && thumbInfo.getFileName().equals(fi.getOrgPath())) {
				return true;
			}
		}
		return false;
	}

	private int checkIfRedDates(GridPane gridPane) {
		int counter = 0;
		for (Node n : gridPane.getChildren()) {
			if (n instanceof VBox) {
				if (n.getId().contains("imageFrame")) {
					for (Node vbox : ((VBox) n).getChildren()) {
						if (vbox instanceof HBox) {
							for (Node hbox : ((HBox) vbox).getChildren()) {
								if (hbox instanceof TextField) {
									if (hbox.getStyle().equals(CssStylesController.getBad_style())) {
										counter++;
									}
								}
							}
						}
					}
				}
			}
		}
		return counter;
	}

	private void updateList() {
		sprintf("Updating the list...");
		int answer = checkIfRedDates(this.gridPane);

		sprintf("checkIfRedDates answer was: " + answer);
		if (answer != 0) {
			warningText("There were still bad dates. Fix them before you can continue");
		} else {
			boolean changed = false;
			for (FileInfo fi : getFolderInfo_full().getFileInfoList()) {
				long date = findDate_TextField(fi.getOrgPath());
				sprintf("FileInfo file: " + fi.getOrgPath() + " date would be; "
						+ simpleDates.getSdf_ymd_hms_slashColon().format(date));
				if (date != 0) {
					fi.setDate(date);
					FileInfo_Utils.setGood(fi);
					changed = true;
					Main.setChanged(true);
				}
			}
			if (changed) {
				TableUtils.updateFolderInfos_FileInfo(getFolderInfo_full());
				if (model_Main.tables() == null) {
					Main.setProcessCancelled(true);
					errorSmth(ERROR, "", null, Misc.getLineNumber(), true);
				}
				model_Main.tables().refreshAllTables();
			}
		}
	}

	private long findDate_TextField(String path) {
		for (Node n : this.gridPane.getChildren()) {
			if (n instanceof VBox) {
				for (Node vbox : ((VBox) n).getChildren()) {
					if (vbox instanceof TextField) {
						if ((getCurrentFolderPath().toString() + File.separator + ((TextField) vbox).getText().trim())
								.equals(path)) {
							TextField textField = getTextFieldDate(vbox.getParent());
							if (textField == null) {
								errorSmth(ERROR, "", null, getLineNumber(), true);
								break;
							} else {
								String tf = textField.getText();
								return Conversion.stringDateToLong(tf.trim(),
										simpleDates.getSdf_ymd_hms_minusDots_default());
							}
						}
					}
				}
			}
		}
		return 0;
	}

	private TextField getTextFieldDate(Node parent) {
		if (parent instanceof VBox) {
			for (Node vbox : ((VBox) parent).getChildren()) {
				if (vbox instanceof HBox) {
					for (Node textField : ((HBox) vbox).getChildren()) {
						if (textField instanceof TextField) {
							return (TextField) textField;
						}
					}
				}
			}
		}
		return null;
	}

	public List<FileInfo> observableNode_toList(ObservableList<Node> filterlist) {
		ArrayList<FileInfo> arrayList = new ArrayList<>();
		for (Node node : filterlist) {
			if (node instanceof VBox && node.getId().equals("imageFrame")) {
				FileInfo fileInfo = (FileInfo) node.getUserData();
				arrayList.add(fileInfo);
			}
		}
		return null;
	}

	public BooleanProperty ignored_property() {
		return this.ignored;
	}

	public BooleanProperty events_property() {
		return this.events;
	}

	public BooleanProperty copied_property() {
		return this.copied;
	}

	public BooleanProperty locations_property() {
		return this.locations;
	}

	public boolean getIgnored() {
		return this.ignored.get();
	}

	public boolean getEvents() {
		return this.events.get();
	}

	public boolean getCopied() {
		return this.copied.get();
	}

	public boolean getLocations() {
		return this.locations.get();
	}

	public boolean fileInfoisShowing(FileInfo fileInfo) {
		boolean hide = false;
		if (!copied.get()) {
			if (fileInfo.isCopied()) {
				hide = true;
			}
		}
		if (!ignored.get()) {
			if (fileInfo.isIgnored()) {
				hide = true;
			}
		}
		if (!events.get()) {
			if (fileInfo.getEvent().length() >= 1) {
				hide = true;
			}
		}
		if (!locations.get()) {
			if (fileInfo.getLocation().length() >= 1) {
				hide = true;
			}
		}

		return hide;

	}

	public ObservableList<Node> filterAllNodesList(ObservableList<Node> obs) {
		ObservableList<Node> observable = FXCollections.observableArrayList();

		boolean hide = false;
		for (Node node : obs) {
			FileInfo fileInfo = (FileInfo) node.getUserData();
			if (!copied.get()) {
				if (fileInfo.isCopied()) {
					hide = true;
				}
			}
			if (!ignored.get()) {
				if (fileInfo.isIgnored()) {
					hide = true;
				}
			}
			if (!events.get()) {
				if (fileInfo.getEvent().length() >= 1) {
					hide = true;
				}
			}
			if (!locations.get()) {
				if (fileInfo.getLocation().length() >= 1) {
					hide = true;
				}
			}
			if (!hide) {
				observable.add(node);
			}
			hide = false;
		}
		return observable;
	}

	public void checkIfDupsNodesExists(CountDownLatch latch) {
		List<Node> remove_nodeList = new ArrayList<>();
		final int start = getAllNodes().size();

		Iterator<Node> it = getAllNodes().iterator();
		while (it.hasNext()) {
			Node node = it.next();
			if (node instanceof VBox && node.getId().equals("imageFrame")) {
				FileInfo fileInfo = (FileInfo) node.getUserData();
				// Messages.sprintf("Finding dups: " + fileInfo.getOrgPath());
				findDups(node, fileInfo, allNodes, remove_nodeList);
			}
		}
		getAllNodes().removeAll(remove_nodeList);
		latch.countDown();
		Messages.sprintf("Files before dupcheck: " + start + " and after dup check it is now: " + getAllNodes().size());

	}

	private void findDups(Node node_ToFind, FileInfo fileInfo_toFind, ObservableList<Node> allNodes,
			List<Node> remove_nodeList) {
		boolean pass = false;
		Iterator<Node> it = allNodes.iterator();
		while (it.hasNext()) {
			Node node = it.next();
			if (node instanceof VBox && node.getId().equals("imageFrame")) {
				FileInfo fileInfo = (FileInfo) node.getUserData();

				if (fileInfo.getOrgPath().equals(fileInfo_toFind.getOrgPath())) {
					if (pass) {
						if (fileInfo.getOrgPath().equals(fileInfo_toFind.getOrgPath())) {
							Messages.sprintf("Node removed: " + fileInfo.getOrgPath() + " to find: "
									+ fileInfo_toFind.getOrgPath());
							remove_nodeList.add(node);
						}
					} else {
						pass = true;
					}
				}
			}
		}
	}

	public void deselectAllExifDataSelectors() {
		for (EXIF_Data_Selector eds : getCameras_TableView().getItems()) {
			eds.setIsShowing(false);
		}
		for (EXIF_Data_Selector eds : getDates_TableView().getItems()) {
			eds.setIsShowing(false);
		}
		for (EXIF_Data_Selector eds : getEvents_TableView().getItems()) {
			eds.setIsShowing(false);
		}
		for (EXIF_Data_Selector eds : getLocations_TableView().getItems()) {
			eds.setIsShowing(false);
		}
	}

	/**
	 * @return the selector_exec
	 */
	public final ScheduledExecutorService getSelector_exec() {
		return selector_exec;
	}

	public Connection getConnection() {
		return connection;
	}

	public void dateAsFileName() {
		sprintf("lastModified_date_btn pressed");
		if (getSelectionModel().getSelectionList().isEmpty()) {
			warningText(Main.bundle.getString("youHaventSelectedMedia"));
			return;
		}
		Messages.warningText("Not tested");
//		for (Node node : getSelectionModel().getSelectionList()) {
//			sprintf("Node is: " + node.getId() + " NODE ALL INFO: " + node.toString());
//			FileInfo fileInfo = (FileInfo) node.getUserData();
//			if (fileInfo != null) {
//				Path source = Paths.get(fileInfo.getOrgPath());
//				Path dest = FileInfo_Utils.renameFileToDate(source, fileInfo);
//				if (!source.equals(dest) && !Files.exists(dest)) {
//
//					try {
//						Files.move(source, dest);
//						fileInfo.setOrgPath(dest.toString());
//						if (!SQL_Utils.isDbConnected(getConnection())) {
//							Messages.sprintf("database is not connected!!!");
//							return;
//						}
//						SQL_Utils.insertFileInfoToDatabase(getConnection(), fileInfo);
//						TableUtils.updateFolderInfos_FileInfo(getFolderInfo_full());
//						if (model_Main.tables() == null) {
//							Main.setProcessCancelled(true);
//							errorSmth(ERROR, "", null, Misc.getLineNumber(), true);
//						}
//						model_Main.tables().refreshAllTables();
//						updateAllInfos(getFolderInfo_full().getFileInfoList());
//						getFolderInfo_full().setChanged(true);
//						sprintf("source is: " + source + " renamed name is: " + dest);
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//			}
//		}
	}
}
