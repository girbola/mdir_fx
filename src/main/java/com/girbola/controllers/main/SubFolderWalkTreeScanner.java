package com.girbola.controllers.main;

import com.girbola.filelisting.ValidatePathUtils;
import com.girbola.messages.Messages;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;

public class SubFolderWalkTreeScanner {

    private final BooleanSupplier cancellationRequested;

    public SubFolderWalkTreeScanner(BooleanSupplier cancellationRequested) {
        this.cancellationRequested = cancellationRequested;
    }

    public List<Path> collectAcceptedFolders(List<Path> rootFolders) {
        Set<Path> result = new LinkedHashSet<>();

        if (rootFolders == null || rootFolders.isEmpty()) {
            return new ArrayList<>(result);
        }

        for (Path root : rootFolders) {
            if (isCancelled()) {
                break;
            }

            collectAcceptedFolders(root, result);
        }

        return new ArrayList<>(result);
    }

    private void collectAcceptedFolders(Path root, Set<Path> result) {
        if (root == null) {
            return;
        }

        try {
            Files.walkFileTree(root, new SimpleFileVisitor<>() {

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    if (isCancelled()) {
                        return FileVisitResult.TERMINATE;
                    }

                    if (!Files.isReadable(dir)) {
                        Messages.sprintfError("Directory is not readable: " + dir);
                        return FileVisitResult.SKIP_SUBTREE;
                    }

                    try {
                        if (!ValidatePathUtils.acceptedFolder(dir)) {
                            return FileVisitResult.SKIP_SUBTREE;
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    if (ValidatePathUtils.hasMediaFilesInFolder(dir)) {
                        result.add(dir);
                    }

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    Messages.sprintfError("Could not access path: " + file + " reason: " + exc.getMessage());

                    if (isCancelled()) {
                        return FileVisitResult.TERMINATE;
                    }

                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ex) {
            Messages.sprintfError("Folder walk failed: " + root + " reason: " + ex.getMessage());
        }
    }

    private boolean isCancelled() {
        return cancellationRequested != null && cancellationRequested.getAsBoolean();
    }
}
