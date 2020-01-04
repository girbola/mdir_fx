/*
 @(#)Copyright:  Copyright (c) 2012-2020 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.main;

import static com.girbola.messages.Messages.sprintf;

import com.girbola.Main;
import com.girbola.controllers.main.tables.tabletype.TableType;

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

/**
 *
 * @author Marko Lokka
 */
public class MainController {

    private final static String ERROR = MainController.class.getSimpleName();

    private Model_main model_main;

    @FXML
    private AnchorPane main_container;
    @FXML
    private VBox main_vbox;
    @FXML
    MenuBarController menuBar_topController;
    //
    // @FXML
    // SortIt_TableController sortitController;
    // @FXML
    // Sorted_TableController sortedController;
    // @FXML
    // AsItIs_TableController asitisController;

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

	menuBar_topController.init(model_main);
	sortitController.init(model_main, Main.bundle.getString("sortit"), TableType.SORTIT.getType());
	sortedController.init(model_main, Main.bundle.getString("sorted"), TableType.SORTED.getType());
	asitisController.init(model_main, Main.bundle.getString("asitis"), TableType.ASITIS.getType());

	this.model_main.tables().setSortIt_table(sortitController.getTable());
	this.model_main.tables().setSorted_table(sortedController.getTable());
	this.model_main.tables().setAsItIs_table(asitisController.getTable());

	this.model_main.tables().getHideButtons().setSortItButtons_hbox(sortitController.getButtons_HBOX());
	this.model_main.tables().getHideButtons().setSortedButtons_hbox(sortedController.getButtons_HBOX());
	this.model_main.tables().getHideButtons().setAsItIsButtons_hbox(asitisController.getButtons_HBOX());

	this.model_main.tables().setDeleteKeyPressed(this.model_main.tables().getSorted_table());
	this.model_main.tables().setDeleteKeyPressed(this.model_main.tables().getSortIt_table());
	this.model_main.tables().setDeleteKeyPressed(this.model_main.tables().getAsItIs_table());

	bottomController.init(model_main);
	model_main.setBottomController(bottomController);
	model_main.setMainContainer(main_container);
	model_main.setMainVBox(main_vbox);
	
    }
}
