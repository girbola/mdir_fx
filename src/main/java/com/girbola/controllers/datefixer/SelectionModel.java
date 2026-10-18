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


public class SelectionModel {

    final private String style_deselected =
            "-fx-border-color: transparent;" +
                    "-fx-border-radius: 1 1 1 1;" +
                    "-fx-border-style: none;" +
                    "-fx-border-width: 1px;";

    final private String style_selected =
            "-fx-border-color: #ba1d1d;" +
                    "-fx-border-width: 1px;";

    private SimpleIntegerProperty selectedIndicator_property = new SimpleIntegerProperty();

    private ObservableList<Node> selectionList = FXCollections.observableArrayList();

    public SelectionModel() {
        selectionList.addListener((ListChangeListener<Node>) c -> Platform.runLater(() -> {
            selectedIndicator_property.set(selectionList.size());
        }));
    }

    public synchronized void addAll(Node node) {
        if (contains(node)) {
            return;
        }
        Platform.runLater(() -> node.setStyle(style_selected));
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

        if(node.getId() == null || !node.getId().equals(DateFixConstants.IMAGEFRAME.getType())) {
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

    public synchronized void clearAll() {
        for(Node selected : selectionList) {
            selected.setStyle(style_deselected);
        }
        selectionList.clear();
    }

    public synchronized void clearAll(Pane parent) {
        sprintf("clearing all");
        for(Node pane : parent.getChildren()) {
            if(pane instanceof VBox && pane.getId().equals(DateFixConstants.IMAGEFRAME.getType())) {
                Platform.runLater(() -> {
                    remove(pane);
                });
            }
        }
    }

    public synchronized boolean contains(Node node) {
        for (Node n : selectionList) {
            if (sameFileInfo(n, node)) {
                return true;
            }
        }
        return false;
    }

    private boolean sameFileInfo(Node first, Node second) {
        FileInfo firstFileInfo = (FileInfo) first.getUserData();
        FileInfo secondFileInfo = (FileInfo) second.getUserData();
        return firstFileInfo.getFileInfo_id() == secondFileInfo.getFileInfo_id();
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
        if (!(pane instanceof TilePane)) {
            return;
        }

        List<Node> selectedNodes = new ArrayList<>();
        List<Node> unselectedNodes = new ArrayList<>();
        for (Node node : pane.getChildren()) {
            if (node instanceof VBox && DateFixConstants.IMAGEFRAME.getType().equals(node.getId())) {
                if (contains(node)) {
                    selectedNodes.add(node);
                } else {
                    unselectedNodes.add(node);
                }
            }
        }

        for (Node node : selectedNodes) {
            Platform.runLater(() -> node.setStyle(style_deselected));
            selectionList.removeIf(selected -> sameFileInfo(selected, node));
        }
        for (Node node : unselectedNodes) {
            addAll(node);
        }
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
