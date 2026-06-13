package com.girbola.controllers.folderscanner.searchservice;

import java.io.IOException;
import javafx.concurrent.Task;

import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Consumer;

public class ScanJob extends Task<Void> {

    private final Path root;
    private final Consumer<ScanEvent> sink;

    public ScanJob(Path root, Consumer<ScanEvent> sink) {
        this.root = root;
        this.sink = sink;
    }

    @Override
    protected Void call() throws Exception {

        if (!Files.exists(root)) return null;

        Files.walkFileTree(root, new SimpleFileVisitor<>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {

                if (isCancelled()) return FileVisitResult.TERMINATE;

                sink.accept(new ScanEvent(dir, ScanEvent.Type.FOUND_FOLDER));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {

                if (isCancelled()) return FileVisitResult.TERMINATE;

                sink.accept(new ScanEvent(file, ScanEvent.Type.FOUND_FILE));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {

                sink.accept(new ScanEvent(file, ScanEvent.Type.ERROR));
                return FileVisitResult.CONTINUE;
            }
        });

        sink.accept(new ScanEvent(root, ScanEvent.Type.DONE));

        return null;
    }
}