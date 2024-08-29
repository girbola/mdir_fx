/*
 @(#)Copyright:  Copyright (c) 2012-2024 All right reserved.
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.main;

import com.girbola.Main;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.messages.Messages;
import common.utils.ui.ScreenUtils;
import common.utils.ui.UI_Tools;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import static com.girbola.messages.Messages.sprintf;

/**
 * @author Marko Lokka
 */
public class MainController {

    private final static String ERROR = MainController.class.getSimpleName();

    private Model_main model_main;

    private SimpleStringProperty table_hbox_pref_width = new SimpleStringProperty("TODO");

    private Bounds tables_rootPaneNodeLayoutBounds;

    //@formatter:off
	@FXML BottomController bottomController;
	@FXML TableController asitisController;
	@FXML TableController sortedController;
	@FXML TableController sortitController;
	@FXML private AnchorPane main_container;
	@FXML private AnchorPane tables_rootPane;
	@FXML private HBox tables_hbox;
	@FXML private VBox main_vbox;
    @FXML MenuBarController menuBar_topController;
    @FXML TabPane tablesTabPane;
	//@formatter:on

    public void initialize(Model_main model_main) {
        this.model_main = model_main;

        sprintf("Maincontroller loading....");

        validateRootPane();
        initControllers();
        initStatistics();
        configureTableActions();

        bottomController.init(model_main);
        setModelProperties();


        //tables_hbox.setMaxWidth(ScreenUtils.screenBouds().getWidth() - 300);
        tablesTabPane.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                /*Messages.sprintf("TABPANE WIDTH IS: " + newValue);*/
            }
        });
    }

    private void validateRootPane() {
        if (tables_rootPane == null) {
            Messages.sprintfError(tables_rootPane.getId());
        }
        this.model_main.tables().setTables_rootPane(tables_rootPane);
    }

    private void initControllers() {
        menuBar_topController.init(model_main);
        sortitController.init(model_main, Main.bundle.getString("sortit"), TableType.SORTIT.getType());
        sortedController.init(model_main, Main.bundle.getString("sorted"), TableType.SORTED.getType());
        asitisController.init(model_main, Main.bundle.getString("asitis"), TableType.ASITIS.getType());

        sortitController.setShowHideTableButtonIcons(sortitController.hide_btn, true);
        sortedController.setShowHideTableButtonIcons(sortedController.hide_btn, true);
        asitisController.setShowHideTableButtonIcons(asitisController.hide_btn, true);
    }

    private void initStatistics() {
        TableStatistic sortitTableStatistic = new TableStatistic(sortitController.getAllFilesCopied_lbl(),
                sortitController.getAllFilesSize_lbl(), sortitController.getAllFilesCopied_lbl());
        TableStatistic sortedTableStatistic = new TableStatistic(sortedController.getAllFilesCopied_lbl(),
                sortedController.getAllFilesSize_lbl(), sortedController.getAllFilesCopied_lbl());
        TableStatistic asitisTableStatistic = new TableStatistic(asitisController.getAllFilesCopied_lbl(),
                asitisController.getAllFilesSize_lbl(), asitisController.getAllFilesCopied_lbl());


        this.model_main.tables().setSortIt_table(sortitController.getTable());
        this.model_main.tables().setSorted_table(sortedController.getTable());
        this.model_main.tables().setAsItIs_table(asitisController.getTable());

        this.model_main.tables().setSortit_TableStatistic(sortitTableStatistic);
        this.model_main.tables().setSorted_TableStatistic(sortedTableStatistic);
        this.model_main.tables().setAsItIs_TableStatistic(asitisTableStatistic);
    }

    private void configureTableActions() {
        this.model_main.tables().setDeleteKeyPressed(this.model_main.tables().getSorted_table());
        this.model_main.tables().setDeleteKeyPressed(this.model_main.tables().getSortIt_table());
        this.model_main.tables().setDeleteKeyPressed(this.model_main.tables().getAsItIs_table());
    }

    private void setModelProperties() {
        model_main.setBottomController(bottomController);
        model_main.setMainContainer(main_container);
        model_main.setMainVBox(main_vbox);
        tables_rootPaneNodeLayoutBounds = UI_Tools.getNodeLayoutBounds(tables_rootPane);
    }

}
