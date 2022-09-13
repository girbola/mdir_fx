/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.main;

import static com.girbola.messages.Messages.sprintf;

import com.girbola.Main;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.messages.Messages;

import common.utils.ui.ScreenUtils;
import common.utils.ui.UI_Tools;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.control.SplitPane;
import javafx.scene.control.SplitPane.Divider;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 *
 * @author Marko Lokka
 */
public class MainController {

	private final static String ERROR = MainController.class.getSimpleName();

	private Model_main model_main;

	private SimpleStringProperty table_hbox_pref_width = new SimpleStringProperty("TODO");

	public SimpleStringProperty getTable_hbox_pref_width() {
		return table_hbox_pref_width;
	}

	public void setTable_hbox_pref_width(SimpleStringProperty table_hbox_pref_width) {
		this.table_hbox_pref_width = table_hbox_pref_width;
	}

	@FXML
	private HBox tables_hbox;

	@FXML
	private AnchorPane main_container;
	@FXML
	private VBox main_vbox;
	@FXML
	MenuBarController menuBar_topController;

	private Bounds tables_rootPaneNodeLayoutBounds;

	//
	// @FXML
	// SortIt_TableController sortitController;
	// @FXML
	// Sorted_TableController sortedController;
	// @FXML
	// AsItIs_TableController asitisController;

	@FXML
	private AnchorPane tables_rootPane;

	@FXML
	TableController sortitController;

	@FXML
	TableController sortedController;

	@FXML
	TableController asitisController;

	@FXML
	BottomController bottomController;

	public void initialize(Model_main aModel) {
		this.model_main = aModel;
		sprintf("Maincontroller loading....");
		if (tables_rootPane == null) {
			Messages.sprintfError("		this.model_main.tables().setTables_rootPane(tables_rootPane);\r\n"
					+ tables_rootPane.getId());
		}
		this.model_main.tables().setTables_rootPane(tables_rootPane);

		menuBar_topController.init(model_main);
		sortitController.init(model_main, Main.bundle.getString("sortit"), TableType.SORTIT.getType());
		sortedController.init(model_main, Main.bundle.getString("sorted"), TableType.SORTED.getType());
		asitisController.init(model_main, Main.bundle.getString("asitis"), TableType.ASITIS.getType());

		sortitController.setShowHideTableButtonIcons(TableType.SORTIT.getType(), sortitController.hide_btn, true);
		sortedController.setShowHideTableButtonIcons(TableType.SORTED.getType(), sortedController.hide_btn, true);
		asitisController.setShowHideTableButtonIcons(TableType.ASITIS.getType(), asitisController.hide_btn, true);

		TableStatistic sortitTableStatistic = new TableStatistic(sortitController.getAllFilesCopied_lbl(),
				sortitController.getAllFilesSize_lbl(), sortitController.getAllFilesTotal_lbl());
		TableStatistic sortedTableStatistic = new TableStatistic(sortedController.getAllFilesCopied_lbl(),
				sortedController.getAllFilesSize_lbl(), sortedController.getAllFilesTotal_lbl());
		TableStatistic asitisTableStatistic = new TableStatistic(asitisController.getAllFilesCopied_lbl(),
				asitisController.getAllFilesSize_lbl(), asitisController.getAllFilesTotal_lbl());

		this.model_main.tables().setSortIt_table(sortitController.getTable());
		this.model_main.tables().setSorted_table(sortedController.getTable());
		this.model_main.tables().setAsItIs_table(asitisController.getTable());

		this.model_main.tables().setSortit_TableStatistic(sortitTableStatistic);
		this.model_main.tables().setSorted_TableStatistic(sortedTableStatistic);
		this.model_main.tables().setAsItIs_TableStatistic(asitisTableStatistic);

//		this.model_main.tables().getHideButtons().setSortItButtons_hbox(sortitController.getButtons_HBOX());
//		this.model_main.tables().getHideButtons().setSortedButtons_hbox(sortedController.getButtons_HBOX());
//		this.model_main.tables().getHideButtons().setAsItIsButtons_hbox(asitisController.getButtons_HBOX());

		this.model_main.tables().setDeleteKeyPressed(this.model_main.tables().getSorted_table());
		this.model_main.tables().setDeleteKeyPressed(this.model_main.tables().getSortIt_table());
		this.model_main.tables().setDeleteKeyPressed(this.model_main.tables().getAsItIs_table());

		bottomController.init(model_main);
		model_main.setBottomController(bottomController);
		model_main.setMainContainer(main_container);
		model_main.setMainVBox(main_vbox);

		tables_rootPaneNodeLayoutBounds = UI_Tools.getNodeLayoutBounds(tables_rootPane);
		tables_hbox.setMaxWidth(ScreenUtils.screenBouds().getWidth() - 300);

//		tables_hbox.widthProperty().addListener(new ChangeListener<Number>() {
//
//			@Override
//			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//				model_main.setTable_root_hbox_width("" + newValue);
//			}
//		});

//		model_main.tables().getHideButtons().setTables_RootPaneMaxWidth(tables_rootPaneNodeLayoutBounds.getWidth());

//		table_SplitPane.setDividerPosition(0, 0);
//		tables_rootPane.setMaxWidth(Double.MAX_VALUE);
//		tables_rootPane.setPrefWidth(Double.MAX_VALUE);
//		tables_rootPane.setMinWidth(Double.MIN_VALUE);
	}

}
