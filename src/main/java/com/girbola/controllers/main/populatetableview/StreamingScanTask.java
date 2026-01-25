package com.girbola.controllers.main.populatetableview;

import javafx.concurrent.Task;

import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Consumer;

public class StreamingScanTask extends Task<Void> {
    private final Path root;
    private final Consumer<Path> onEntry;

    public StreamingScanTask(Path root, Consumer<Path> onEntry) {
        this.root = root;
        this.onEntry = onEntry;
    }

    @Override
    protected Void call() throws Exception {
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                if (isCancelled()) return FileVisitResult.TERMINATE;
                if (validFolder(dir)) {
                    emit(dir);
                    return FileVisitResult.CONTINUE;
                }
                return FileVisitResult.SKIP_SUBTREE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (isCancelled()) return FileVisitResult.TERMINATE;
                if (Files.isReadable(file)) {
                    emit(file);
                }
                return FileVisitResult.CONTINUE;
            }

            private boolean validFolder(Path dir) {
                return Files.exists(dir) && Files.isDirectory(dir) && Files.isReadable(dir);
            }

            private void emit(Path p) {
                updateMessage(p.toString());
                // DO NOT call UI code here; keep it background-safe:
                onEntry.accept(p);
            }
        });
        return null;
    }
}
