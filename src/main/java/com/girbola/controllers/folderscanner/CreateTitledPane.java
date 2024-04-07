/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.folderscanner;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.nio.file.Path;
import java.util.List;

import static com.girbola.controllers.folderscanner.FolderScanner_Methods.titledPaneExists;

/**
 *
 * @author Marko Lokka
 */
public class CreateTitledPane extends Task<Void> {

    private Path path;
    private List<Path> list;
    private VBox analyzeList_vbox;
//    private List<TreeItem<FolderInfo>> analyzeFolder_selected;

    public CreateTitledPane(Path path, List<Path> list, VBox analyzeList_vbox) {
        this.path = path;
        this.list = list;
        this.analyzeList_vbox = analyzeList_vbox;
//        this.analyzeFolder_selected = analyzeFolder_selected;
//        TreeItem<FolderInfo> treeItem = createTreeItem_FolderInfo(path.toFile(), analyzeFolder_selected);

    }

    @Override
    protected Void call() throws Exception {

        System.out.println("Processing rootfolder_obs: " + path);
        if (!titledPaneExists(analyzeList_vbox, path)) {
            TitledPane titledPane = new TitledPane(path.toString(), createTreeTableView(path));
            titledPane.setId("titledPane");
            Platform.runLater(() -> {
                try {
                    analyzeList_vbox.getChildren().add(titledPane);
                } catch (Exception ex) {
                    System.out.println("ex: " + ex.getMessage());
                }
            });
        }

        return null;
    }

    private TreeTableView<FolderInfoTable> createTreeTableView(Path path) {
        TreeItem<FolderInfoTable> root = new TreeItem<>(new FolderInfoTable(path.toString()));
        root.setExpanded(true);

        TreeTableView<FolderInfoTable> treeTableView = new TreeTableView<>();
        treeTableView.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);
        treeTableView.setRoot(root);

        TreeTableColumn<FolderInfoTable, String> path_col = new TreeTableColumn<>("Folder");
        TreeTableColumn<FolderInfoTable, Integer> folders_col = new TreeTableColumn<>("Folders");
        TreeTableColumn<FolderInfoTable, Integer> files_col = new TreeTableColumn<>("Files");
        TreeTableColumn<FolderInfoTable, Integer> media_col = new TreeTableColumn<>("Media");
        treeTableView.getColumns().addAll(path_col, folders_col, files_col, media_col);

        setColumnMaxWidth(path_col, 70);
        setColumnMaxWidth(files_col, 8);
        setColumnMaxWidth(folders_col, 8);
        setColumnMaxWidth(media_col, 8);

        path_col.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<FolderInfoTable, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<FolderInfoTable, String> param) {
                return new ReadOnlyStringWrapper(param.getValue().getValue().getPath());
            }
        });

        folders_col.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<FolderInfoTable, Integer>, ObservableValue<Integer>>() {
            @Override
            public ObservableValue<Integer> call(TreeTableColumn.CellDataFeatures<FolderInfoTable, Integer> param) {
                return new ReadOnlyObjectWrapper<Integer>(param.getValue().getValue().getFolders());
            }
        });

        files_col.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<FolderInfoTable, Integer>, ObservableValue<Integer>>() {
            @Override
            public ObservableValue<Integer> call(TreeTableColumn.CellDataFeatures<FolderInfoTable, Integer> param) {
                return new ReadOnlyObjectWrapper<Integer>(param.getValue().getValue().getFiles());
            }
        });
        media_col.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<FolderInfoTable, Integer>, ObservableValue<Integer>>() {
            @Override
            public ObservableValue<Integer> call(TreeTableColumn.CellDataFeatures<FolderInfoTable, Integer> param) {
                return new ReadOnlyObjectWrapper<Integer>(param.getValue().getValue().getMedia());
            }
        });
        return treeTableView;
    }

    private void setColumnMaxWidth(TreeTableColumn<FolderInfoTable, ?> treeTableColumn, int procentage) {
        treeTableColumn.setMaxWidth(1F * Integer.MAX_VALUE * procentage);
    }

}
