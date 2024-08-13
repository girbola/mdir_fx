/*
 @(#)Copyright:  Copyright (c) 2012-2024 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.datefixer;

import com.girbola.Main;
import com.girbola.controllers.loading.LoadingProcessTask;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.util.concurrent.atomic.AtomicInteger;

public class AddContentToDateFixContainer extends Task<Integer> {

    private final String ERROR = AddContentToDateFixContainer.class.getSimpleName();
    // private final GridPane gridPane;

    private AtomicInteger counter = new AtomicInteger(1);
    private AtomicInteger x = new AtomicInteger(0);
    private AtomicInteger y = new AtomicInteger(0);

    private ObservableList<Node> list;
    private Model_datefix model_datefix;

    private int size;
    private LoadingProcessTask loadingProcess_Task;

    private TilePane tilePane;

    public AddContentToDateFixContainer(Model_datefix aModel_dateFix, ObservableList<Node> aList,
                                        LoadingProcessTask loading_Process_Task, TilePane aTilePane) {
        Messages.sprintf("AddToGridPane2 started: " + aList.size());
        this.model_datefix = aModel_dateFix;
        this.tilePane = aTilePane;
        this.list = aList;
        this.size = aList.size();
        this.loadingProcess_Task = loading_Process_Task;
//		//model_datefix.getGridPane().getChildren().clear();
//		sprintf("total nodes: " + model_datefix.getAllNodes().size() + " aGridPane list size is after clearing: "
//				+ list.size());
//
//		model_datefix.getGridPane().getRowConstraints().removeAll(model_datefix.getGridPane().getRowConstraints());
//		model_datefix.getGridPane().getColumnConstraints()
//				.removeAll(model_datefix.getGridPane().getColumnConstraints());
//		setGridPane_Constraints();
        loading_Process_Task.setMessage("Adding images");
    }

    private Label createText(int i) {
        Label label = new Label("" + i);
        label.getStyleClass().add("imageNumber");
        label.setId("imageNumber");
        label.setMouseTransparent(true);
        return label;
    }

    // private AtomicInteger counter = new AtomicInteger(list.size());
    @Override
    protected Integer call() throws Exception {

        for (Node node : list) {
            if (node instanceof VBox && node.getId().equals("imageFrame")) {
                createNode(node);
                // loadingProcess_Task.getProgressBar().setProgress((double) counter.get());
            }
        }
        return null;
    }


    private void createNode(Node node) {
        StackPane sp = (StackPane) node.lookup("#stackPane");
        Label old_text = (Label) sp.lookup("#imageNumber");
        if (old_text == null) {
            Label imageNumber = createText(counter.get());
            sp.getChildren().add(imageNumber);
            StackPane.setAlignment(imageNumber, Pos.TOP_RIGHT);
        } else {
            old_text.setText("" + counter.get());
        }

        model_datefix.getTilePane().getChildren().add(node);

        counter.incrementAndGet();

    }

    @Override
    protected void failed() {
        super.failed();
    }

    @Override
    protected void cancelled() {
        super.cancelled();
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        loadingProcess_Task.closeStage();
    }
}
