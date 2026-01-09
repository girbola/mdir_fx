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
                emit(dir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (isCancelled()) return FileVisitResult.TERMINATE;
                emit(file);
                return FileVisitResult.CONTINUE;
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
