/*
 @(#)Copyright:  Copyright (c) 2012-2024 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.datefixer;

import com.girbola.controllers.datefixer.utils.DateFixGuiUtils;
import com.girbola.controllers.loading.LoadingProcessTask;
import com.girbola.messages.Messages;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.util.concurrent.atomic.AtomicInteger;

import static com.girbola.controllers.datefixer.utils.DateFixGuiUtils.createImageNumberLbl;

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
        loading_Process_Task.setMessage("Adding images");
    }


    // private AtomicInteger counter = new AtomicInteger(list.size());
    @Override
    protected Integer call() throws Exception {

        for (Node node : list) {
            Messages.sprintf("ImageFrameeeeee: " + node.getId());
            changeImageNumberOfNode(node);
        }
        return null;
    }

    private void changeImageNumberOfNode(Node node) {
        if (node instanceof VBox && node.getId().equals("imageFrame")) {
            VBox vbox = (VBox) node;
            Label imageFrameImageNumber = DateFixGuiUtils.getImageFrameImageNumber(vbox);
            if (imageFrameImageNumber != null) {
                imageFrameImageNumber.setText("" + counter.get());
            }
        }

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
