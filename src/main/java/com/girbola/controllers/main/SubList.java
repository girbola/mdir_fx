package com.girbola.controllers.main;

import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javafx.concurrent.Task;

public class SubList extends Task<List<Path>> {

    private static final int MAX_SCANNER_THREADS = 4;

    private final List<Path> selectedFolderScannerList;
    private final Set<Path> foldersWithMedia = ConcurrentHashMap.newKeySet();

    public SubList(List<Path> selectedFolderScannerList) {
        this.selectedFolderScannerList = selectedFolderScannerList;
    }

    @Override
    protected List<Path> call() {

        if (selectedFolderScannerList == null || selectedFolderScannerList.isEmpty()) {
            Messages.sprintfError("selectedFolderScannerList is null or empty.");
            return List.of();
        }

        //int threadCount = calculateThreadCount(selectedFolderScannerList.size());

        ExecutorService executor = Executors.newSingleThreadExecutor(r ->{
            Thread t = new Thread(r,"sublist-folder-scanner");
            t.setDaemon(true);
            return t;
        });
//        ExecutorService executor2 = Executors.newSingleThreadExecutor(threadCount, r -> {
//            Thread t = new Thread(r, "sublist-folder-scanner");
//            t.setDaemon(true);
//            return t;
//        });

        try {
            List<Future<?>> futures = new ArrayList<>();

            for (Path root : selectedFolderScannerList) {
                Messages.sprintf("-------------------Submitting task for folder: " + root);
                futures.add(executor.submit(() -> scanRootFolder(root)));
            }

            // Wait for all tasks to complete safely
            for (Future<?> f : futures) {
                try {
                    f.get(); // wait
                    Messages.sprintf("Task completed." + foldersWithMedia.size());
                } catch (CancellationException e) {
                    Messages.sprintf("Task cancelled." + foldersWithMedia.size());
                } catch (ExecutionException e) {
                    Messages.sprintfError("Task error: " + e.getMessage());
                }
            }

        } catch (Exception ex) {
            Messages.sprintfError("Executor error: " + ex.getMessage());
        } finally {
            executor.shutdown(); // ✅ graceful shutdown
        }

        List<Path> result = new ArrayList<>(foldersWithMedia);
        Collections.sort(result);
Messages.sprintf("foldersWithMedia size: " + result.size());
        return result;
    }

//    private int calculateThreadCount(int rootCount) {
//        int cores = Runtime.getRuntime().availableProcessors();
//        return Math.max(1, Math.min(Math.min(rootCount, cores), MAX_SCANNER_THREADS));
//    }

    private void scanRootFolder(Path rootFolder) {
        Messages.sprintf("-------------------Scanning folder: " + rootFolder);
        if (isCancelled()) return;

        if (rootFolder == null) {
            Messages.sprintfError("Root folder is null.");
            return;
        }

        if (!Files.exists(rootFolder) || !Files.isDirectory(rootFolder)) {
            Messages.sprintfError("Invalid root folder: " + rootFolder);
            return;
        }

        try {
            Files.walkFileTree(rootFolder, new SimpleFileVisitor<>() {

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {

                    if (isCancelled()) {
                        Messages.sprintf("SubList cancelled.");
                        return FileVisitResult.TERMINATE; // global stop if user cancels
                    }
                    try {
                        if(Files.isHidden(dir)) {
                            Messages.sprintf("Hidden folder: " + dir);
                            return FileVisitResult.SKIP_SUBTREE;
                        }
                    } catch (IOException e) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    if (!Files.isReadable(dir)) {
                        Messages.sprintfError("Directory is not readable: " + dir);
                        return FileVisitResult.SKIP_SUBTREE;
                    }
//                    if (ValidatePathUtils.hasMediaFilesInFolder(dir)) {
//                        Messages.sprintf("Found media in folder: " + dir);
//                        foldersWithMedia.add(dir);
//                    }
                    Messages.sprintf("----------Found media in folder: " + dir);
                    foldersWithMedia.add(dir);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {

                    if (isCancelled()) {
                        return FileVisitResult.TERMINATE;
                    }

                    Messages.sprintfError("Sublist access failed: " + file + " -> " + exc.getMessage());
                    return FileVisitResult.CONTINUE;
                }
            });

        } catch (IOException ex) {
            Messages.sprintfError("Walk failed: " + rootFolder + " -> " + ex.getMessage());
            Messages.errorSmth("SubList", "", ex, Misc.getLineNumber(), true);
        }
    }

    @Override
    protected void cancelled() {
        Messages.sprintf("SubList cancelled.");
        super.cancelled();
    }

    @Override
    protected void failed() {
        Messages.sprintf("SubList failed.");
        super.failed();
    }

    @Override
    protected void succeeded() {
        try {
            Messages.sprintf("SubList succeeded. " + get().size());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        super.succeeded();
    }
}