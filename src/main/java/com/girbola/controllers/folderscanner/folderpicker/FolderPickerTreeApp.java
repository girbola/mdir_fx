package com.girbola.controllers.folderscanner.folderpicker;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.stage.Stage;

import java.nio.file.FileSystems;
import java.nio.file.Path;

public class FolderPickerTreeApp extends Application {

    @Override
    public void start(Stage stage) {
        // A synthetic root label (not a real Path on disk)
        CheckBoxTreeItem<Path> root = new CheckBoxTreeItem<>(Path.of("Computer"));
        root.setExpanded(true);

        // Add filesystem roots (drives on Windows, '/' on Unix, etc.)
        for (Path r : FileSystems.getDefault().getRootDirectories()) {
            root.getChildren().add(new LazyDirTreeItem(r));
        }

        TreeView<Path> tree = new TreeView<>(root);
        tree.setShowRoot(false);

        // Render checkboxes using CheckBoxTreeCell, and show friendly names
        tree.setCellFactory(tv -> new CheckBoxTreeCell<Path>() {
            @Override
            public void updateItem(Path item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String name = item.getFileName() == null ? item.toString() : item.getFileName().toString();
                    setText(name);
                }
            }
        });

        // Selection propagation logic (parent <-> children)
//        SelectionPropagation.installSelectionPropagation(root);

        stage.setScene(new Scene(tree, 800, 600));
        stage.setTitle("Folder Tree with Checkboxes");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
