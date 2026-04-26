package com.girbola.controllers.main.populatetableview;

import javafx.concurrent.Task;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import static com.girbola.filelisting.ValidatePathUtils.acceptedFolder;

public class FileTreeScanTask extends Task<List<Path>> {
    private final Path root;
    private final boolean estimate; // whether to do a pre-pass to estimate entries

    public FileTreeScanTask(Path root) {
        this(root, true);
    }

    public FileTreeScanTask(Path root, boolean estimate) {
        this.root = root;
        this.estimate = estimate;
        updateTitle("Scanning: " + root);
    }

    /**
     * Performs cancellable file tree traversal; collects paths
     */
    @Override
    protected List<Path> call() throws Exception {
        if (!Files.exists(root)) {
            throw new NoSuchFileException(root.toString());
        }

        final long total = estimate ? estimateCount(root) : -1L;
        if (total <= 0) {
            // Use indeterminate progress if we can't estimate
            updateProgress(-1, -1);
        }

        List<Path> collected = new ArrayList<>();
        final long[] processed = {0};

        // Visitor that is cancellation-aware and updates message/progress
        FileVisitor<Path> visitor = new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                if (isCancelled()) return FileVisitResult.TERMINATE;
                try {
                    if (!acceptedFolder(dir)) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                } catch (IOException e) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                record(dir);
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (isCancelled()) return FileVisitResult.TERMINATE;
                record(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                // You can choose to collect the error or skip silently:
                // updateMessage("Failed: " + file + " -> " + exc.getMessage());
                return isCancelled() ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
            }

            private void record(Path p) {
                collected.add(p);
                long d = ++processed[0];
                // update message with the current path
                updateMessage(p.toString());
                if (total > 0) {
                    updateProgress(d, total);
                }
            }
        };

        Files.walkFileTree(root, visitor);

        if (!isCancelled()) {
            // Complete progress if estimable; otherwise leave it indeterminate
            if (total > 0) updateProgress(total, total);
            updateMessage("Done");
        }

        return collected;
    }

    private long estimateCount(Path r) {
        // Pre-walk: count directories + files
        try (var walk = Files.walk(r)) {
            return walk.count();
        } catch (IOException e) {
            return -1L;
        }
    }
}
