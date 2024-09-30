/*
 @(#)Copyright:  Copyright (c) 2012-2024 All right reserved.
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.datefixer;

import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

import static com.girbola.messages.Messages.sprintf;

/**
 * @author Marko Lokka
 */
public class SelectionModel {

    final private String style_deselected =
            "-fx-border-color: transparent;" +
                    "-fx-border-radius: 1 1 1 1;" +
                    "-fx-border-style: none;" +
                    "-fx-border-width: 3px;";

    final private String style_selected =
            "-fx-border-color: #ba1d1d;" +
                    "-fx-border-width: 3px;";

    private SimpleIntegerProperty selectedIndicator_property = new SimpleIntegerProperty();

    private ObservableList<Node> selectionList = FXCollections.observableArrayList();

    public SelectionModel() {
        selectionList.addListener((ListChangeListener<Node>) c -> Platform.runLater(() -> {
            selectedIndicator_property.set(selectionList.size());
        }));
    }

    public synchronized void addAll(Node node) {
        if (!contains(node)) {
            Platform.runLater(() -> {
                node.setStyle(style_selected);
            });
        }
        selectionList.add((VBox) node);
    }

    /**
     * Returns true if node is already selected. Otherwise returns false as node is
     * deselected
     *
     * @param node
     * @return
     */
    public synchronized boolean addWithToggle(Node node) {

        if(node.getId() == null || !node.getId().equals("imageFrame")) {
            return false;
        }
        if (!contains(node)) {
            Platform.runLater(() -> {
                node.setStyle(style_selected);
            });
            selectionList.add((VBox) node);
            return false;
        } else {
            Platform.runLater(() -> {
                node.setStyle(style_deselected);
            });
            selectionList.remove((VBox) node);
            return true;
        }

    }

    public synchronized void addOnly(Node node) {
        if (!contains(node)) {
            Platform.runLater(() -> {
                node.setStyle(style_selected);
                selectionList.add(node);
            });
        }
    }

    public synchronized void clearAll(Pane parent) {
        sprintf("clearing all");
        for(Node pane : parent.getChildren()) {
            if(pane instanceof VBox && pane.getId().equals("imageFrame")) {
                Platform.runLater(() -> {
                    remove(pane);
                });
            }

        }

    }

    public synchronized boolean contains(Node node) {
        FileInfo targetFileInfo = (FileInfo) node.getUserData();
        for (Node n : selectionList) {

            FileInfo sourceFileInfo = (FileInfo) n.getUserData();
            if(sourceFileInfo.getFileInfo_id() == targetFileInfo.getFileInfo_id()) {
                return true;
            }
        }
        return false;

    }

    public synchronized void remove(Node node) {
        if (contains(node)) {
            Platform.runLater(() -> {
                node.setStyle(style_deselected);
            });
            selectionList.remove((VBox) node);
        }
    }

    public synchronized void invertSelection(Pane pane) {
        if (pane == null || !pane.isVisible()) {
            return;
        }
        List<Node> list = new ArrayList<>();

        if (pane instanceof TilePane) {
            for (Node node : pane.getChildren()) {
                if (!contains(node)) {
                    list.add((VBox) node);
                }
            }
        }

        if (list.isEmpty()) {
            Messages.sprintf("list was empty at selectionModel lineb " + selectionList.size());
            return;
        }
        clearAll(pane);
        for (Node n : list) {
            addWithToggle(n);
        }
        list.clear();

    }

    public ObservableList<Node> getSelectionList() {
        return this.selectionList;
    }

    public SimpleIntegerProperty getSelectedIndicator_property() {
        return this.selectedIndicator_property;
    }

    public synchronized void setSelected(SimpleIntegerProperty selected) {
        this.selectedIndicator_property = selected;
    }

}
