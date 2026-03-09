package com.girbola.controllers.folderscanner.folderpicker;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.CheckBoxTreeItem;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LazyDirTreeItem extends CheckBoxTreeItem<Path> {

    private boolean childrenLoaded = false;

    public LazyDirTreeItem(Path path) {
        super(path);

        // Show an expand arrow by adding a dummy child for directories that might have children
        if (mayHaveChildren(path)) {
            getChildren().add(new CheckBoxTreeItem<>(null)); // placeholder
        }

        // Lazy-load on expand
        expandedProperty().addListener((obs, wasExpanded, isExpanded) -> {
            if (isExpanded && !childrenLoaded) {
                loadChildrenAsync();
            }
        });
    }

    private boolean mayHaveChildren(Path path) {
        try {
            return path != null && Files.isDirectory(path) && Files.isReadable(path);
        } catch (Exception e) {
            return false;
        }
    }

    private void loadChildrenAsync() {
        childrenLoaded = true;

        Task<List<LazyDirTreeItem>> task = new Task<>() {
            @Override
            protected List<LazyDirTreeItem> call() {

                Path dir = LazyDirTreeItem.this.getValue();
                List<LazyDirTreeItem> result = new ArrayList<>();
                if (dir == null) return result;



                try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                    for (Path p : stream) {
                        // Only show readable directories; avoid following symlinks to prevent cycles
                        if (Files.isDirectory(p, LinkOption.NOFOLLOW_LINKS)
                                && Files.isReadable(p)
                                && !Files.isHidden(p)
                                && !p.toFile().toString().toLowerCase().contains("opt")) {
                            result.add(new LazyDirTreeItem(p));
                        }
                    }
                } catch (IOException | SecurityException ignored) {
                    // Permission denied or other IO issues; ignore silently for UI robustness
                }

                // Sort case-insensitively by display name
                result.sort(Comparator.comparing(
                        it -> {
                            Path val = it.getValue();
                            return val.getFileName() == null ? val.toString() : val.getFileName().toString();
                        }, String.CASE_INSENSITIVE_ORDER));

                return result;
            }
        };

        task.setOnSucceeded(e -> {
            List<LazyDirTreeItem> children = task.getValue();
            Platform.runLater(() -> {
                getChildren().clear();
                getChildren().addAll(children);
            });
        });

        task.setOnFailed(e -> Platform.runLater(() -> getChildren().clear()));

        Thread t = new Thread(task, "dir-scan-" + String.valueOf(getValue()));
        t.setDaemon(true);
        t.start();
    }
}