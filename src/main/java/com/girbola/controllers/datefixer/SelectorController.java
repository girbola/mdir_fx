
package com.girbola.controllers.datefixer;

import com.girbola.controllers.datefixer.table.EXIF_Data_Selector;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.messages.*;
import com.girbola.workdir.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import javafx.beans.property.*;
import javafx.beans.value.*;
import javafx.concurrent.*;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.*;

public class SelectorController {

	private WorkDirSQL workDir_SQL;
	private ModelDatefix modelDatefix;
	private TilePane df_tilePane;
	private FolderInfo folderInfo;
	//@formatter:off
	@FXML private ScrollPane infoTables_container;
	@FXML private VBox selector_root;
	@FXML private TitledPane dates_titledPane;
	@FXML private TitledPane cameras_titledPane;
	@FXML private TitledPane events_titledPane;
	@FXML private TitledPane locations_titledPane;
	@FXML private TableView<EXIF_Data_Selector> cameras_tableView;
//	@FXML private TableColumn<EXIF_Data_Selector, Boolean> cameras_checkBox_hide_col;
	@FXML private TableColumn<EXIF_Data_Selector, String> cameras_col;
	@FXML private TableColumn<EXIF_Data_Selector, Integer> cameras_counter_col;
	@FXML private TableView<EXIF_Data_Selector> dates_tableView;
//	@FXML private TableColumn<EXIF_Data_Selector, Boolean> dates_checkBox_hide_col;
	@FXML private TableColumn<EXIF_Data_Selector, String> dates_col;
	@FXML private TableColumn<EXIF_Data_Selector, Integer> dates_counter_col;
	@FXML private TableView<EXIF_Data_Selector> locations_tableView;
//	@FXML private TableColumn<EXIF_Data_Selector, Boolean> locations_checkBox_hide_col;
	@FXML private TableColumn<EXIF_Data_Selector, String> locations_col;
	@FXML private TableColumn<EXIF_Data_Selector, Integer> locations_counter_col;
	@FXML private TableView<EXIF_Data_Selector> events_tableView;
	@FXML private TableColumn<EXIF_Data_Selector, Boolean> events_checkBox_hide_col;
	@FXML private TableColumn<EXIF_Data_Selector, String> events_col;
	@FXML private TableColumn<EXIF_Data_Selector, Integer> events_counter_col;

//	Callback<TableColumn<EXIF_Data_Selector, Boolean>, TableCell<EXIF_Data_Selector, Boolean>> checkbox_DATES_CellFactory = p -> new CheckBoxCell_Dates(model_datefix);
//	Callback<TableColumn<EXIF_Data_Selector, Boolean>, TableCell<EXIF_Data_Selector, Boolean>> checkbox_CAMERAS_CellFactory = p -> new CheckBoxCell_Cameras(model_datefix);
//	Callback<TableColumn<EXIF_Data_Selector, Boolean>, TableCell<EXIF_Data_Selector, Boolean>> checkbox_EVENTS_CellFactory = p -> new CheckBoxCell_Events(model_datefix);
//	Callback<TableColumn<EXIF_Data_Selector, Boolean>, TableCell<EXIF_Data_Selector, Boolean>> checkbox_LOCATIONS_CellFactory = p -> new CheckBoxCell_Locations(model_datefix);
	//@formatter:on

	@Deprecated
	Callback<TableColumn<EXIF_Data_Selector, Boolean>, TableCell<EXIF_Data_Selector, Boolean>> dates_INFO_Select_CellFactory = p -> new Dates_INFO_Select_CellFactory(
			modelDatefix);

	public void init(ModelDatefix aModel_datefix, TilePane aDf_tilePane) {
		this.modelDatefix = aModel_datefix;
		this.df_tilePane = aDf_tilePane;
		this.folderInfo = modelDatefix.getFolderInfo_full();
		if (folderInfo == null) {
			Messages.sprintfError("folderinfo were null!!!");
		}

		Messages.sprintf("Folderinfo path name is: " + this.folderInfo.getFolderPath());

		workDir_SQL = new WorkDirSQL(Paths.get(folderInfo.getFolderPath()));

		modelDatefix.setDates_TableView(dates_tableView);
		modelDatefix.setCameras_TableView(cameras_tableView);
		modelDatefix.setEvents_TableView(events_tableView);
		modelDatefix.setLocations_TableView(locations_tableView);

		modelDatefix.getDateFix_Utils().createDates_list(modelDatefix.getFolderInfo_full().getFileInfoList());
		dates_tableView.setItems(modelDatefix.getDateFix_Utils().getDate_obs());
		Messages.sprintf("model_datefix.getDateFix_Utils().getDate_obs():  " + modelDatefix.getDateFix_Utils().getDate_obs().size());
//		dates_checkBox_hide_col.setCellFactory(checkbox_DATES_CellFactory);
//		dates_checkBox_hide_col.setCellValueFactory(
//				(TableColumn.CellDataFeatures<EXIF_Data_Selector, Boolean> cellData) -> new SimpleObjectProperty<>(
//						cellData.getValue().isShowing()));
		dates_col.setCellValueFactory(
				(TableColumn.CellDataFeatures<EXIF_Data_Selector, String> cellData) -> new SimpleObjectProperty<>(
						cellData.getValue().getInfo()));
		dates_counter_col.setCellValueFactory(
				(TableColumn.CellDataFeatures<EXIF_Data_Selector, Integer> cellData) -> new SimpleObjectProperty<>(
						cellData.getValue().getCount()));
		dates_tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		dates_tableView.setRowFactory(new TableRowSelector(dates_tableView, modelDatefix.getScrollPane()));
		dates_tableView.getSelectionModel().selectedItemProperty()
				.addListener(new ChangeListener<EXIF_Data_Selector>() {
					@Override
					public void changed(ObservableValue<? extends EXIF_Data_Selector> observable,
							EXIF_Data_Selector oldValue, EXIF_Data_Selector newValue) {
						Task<Boolean> selectByType = new SelectByTableModel(modelDatefix,
								SelectorModelType.DATE.getType(), dates_tableView);
						modelDatefix.getSelector_exec().scheduleAtFixedRate(selectByType, 0, 100,
								TimeUnit.MILLISECONDS);
					}
				});

		modelDatefix.getDateFix_Utils().createCamera_list(modelDatefix.getFolderInfo_full().getFileInfoList());
		cameras_tableView.setItems(modelDatefix.getDateFix_Utils().getCameras_obs());
		cameras_tableView.getSelectionModel().selectedItemProperty()
				.addListener(new ChangeListener<EXIF_Data_Selector>() {
					@Override
					public void changed(ObservableValue<? extends EXIF_Data_Selector> observable,
							EXIF_Data_Selector oldValue, EXIF_Data_Selector newValue) {
						Task<Boolean> selectByType = new SelectByTableModel(modelDatefix,
								SelectorModelType.CAMERA.getType(), cameras_tableView);
						modelDatefix.getSelector_exec().scheduleAtFixedRate(selectByType, 0, 100,
								TimeUnit.MILLISECONDS);
					}
				});
//		cameras_checkBox_hide_col.setCellFactory(checkbox_CAMERAS_CellFactory);
//		cameras_checkBox_hide_col.setCellValueFactory(
//				(TableColumn.CellDataFeatures<EXIF_Data_Selector, Boolean> cellData) -> new SimpleObjectProperty<>(
//						cellData.getValue().isShowing()));
		cameras_col.setCellValueFactory(
				(TableColumn.CellDataFeatures<EXIF_Data_Selector, String> cellData) -> new SimpleObjectProperty<>(
						cellData.getValue().getInfo()));
		cameras_counter_col.setCellValueFactory(
				(TableColumn.CellDataFeatures<EXIF_Data_Selector, Integer> cellData) -> new SimpleObjectProperty<>(
						cellData.getValue().getCount()));
		cameras_tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		cameras_tableView.setRowFactory(new TableRowSelector(cameras_tableView, modelDatefix.getScrollPane()));

		modelDatefix.getDateFix_Utils().createEvent_list(modelDatefix.getFolderInfo_full().getFileInfoList());
		events_tableView.setItems(modelDatefix.getDateFix_Utils().getEvents_obs());
//		events_checkBox_hide_col.setCellFactory(checkbox_EVENTS_CellFactory);
//		events_checkBox_hide_col.setCellValueFactory(
//				(TableColumn.CellDataFeatures<EXIF_Data_Selector, Boolean> cellData) -> new SimpleObjectProperty<>(
//						cellData.getValue().isShowing()));
		events_col.setCellValueFactory(
				(TableColumn.CellDataFeatures<EXIF_Data_Selector, String> cellData) -> new SimpleObjectProperty<>(
						cellData.getValue().getInfo()));
		events_counter_col.setCellValueFactory(
				(TableColumn.CellDataFeatures<EXIF_Data_Selector, Integer> cellData) -> new SimpleObjectProperty<>(
						cellData.getValue().getCount()));
		events_tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		events_tableView.setRowFactory(new TableRowSelector(events_tableView, modelDatefix.getScrollPane()));

		modelDatefix.getDateFix_Utils().createLocation_list(modelDatefix.getFolderInfo_full().getFileInfoList());
		locations_tableView.setItems(modelDatefix.getDateFix_Utils().getLocations_obs());
//		locations_checkBox_hide_col.setCellFactory(checkbox_LOCATIONS_CellFactory);
//		locations_checkBox_hide_col.setCellValueFactory(
//				(TableColumn.CellDataFeatures<EXIF_Data_Selector, Boolean> cellData) -> new SimpleObjectProperty<>(
//						cellData.getValue().isShowing()));
		locations_col.setCellValueFactory(
				(TableColumn.CellDataFeatures<EXIF_Data_Selector, String> cellData) -> new SimpleObjectProperty<>(
						cellData.getValue().getInfo()));
		locations_counter_col.setCellValueFactory(
				(TableColumn.CellDataFeatures<EXIF_Data_Selector, Integer> cellData) -> new SimpleObjectProperty<>(
						cellData.getValue().getCount()));
		locations_tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		locations_tableView.setRowFactory(new TableRowSelector(locations_tableView, modelDatefix.getScrollPane()));

		selector_root.heightProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				Messages.sprintf("selector_root height has changed to: " + newValue);
			}
		});
		cameras_titledPane.heightProperty().addListener((ChangeListener<Number>) (observable, oldValue, newValue) -> Messages.sprintf("cameras_titledPane height has changed to: " + newValue + " root height is : "
				+ selector_root.getHeight()));

	}

	private boolean hasDate(String format, List<String> listOfDates) {
		for (String str : listOfDates) {
			if (str.equals(format)) {
				return true;
			}
		}
		return false;
	}

	public ScrollPane getInfoTables_container() {
		return this.infoTables_container;
	}
}
