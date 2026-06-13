package com.girbola.controllers.folderscanner.searchservice;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class ScanController {

    // -------------------------
    // Executors
    // -------------------------

    private final ExecutorService ioPool =
            Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r);
                t.setName("Scan-IO-Thread");
                t.setDaemon(false);
                return t;
            });

    // -------------------------
    // State
    // -------------------------

    private final List<ScanJob> activeJobs =
            Collections.synchronizedList(new ArrayList<>());

    private final Set<Path> discoveredFolders =
            ConcurrentHashMap.newKeySet();

    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    private final AtomicInteger remainingJobs = new AtomicInteger(0);

    // -------------------------
    // Event callbacks (UI connects here)
    // -------------------------

    private Consumer<Path> onFolderFound = path -> {};
    private Runnable onFinished = () -> {};

    // -------------------------
    // Public API
    // -------------------------

    public void setOnFolderFound(Consumer<Path> listener) {
        this.onFolderFound = listener;
    }

    public void setOnFinished(Runnable listener) {
        this.onFinished = listener;
    }

    /**
     * Start scanning multiple roots
     */
    public void startScan(List<Path> roots) {

        cancelAll();

        if (roots == null || roots.isEmpty()) return;

        isRunning.set(true);
        remainingJobs.set(roots.size());

        for (Path root : roots) {
            ScanJob job = new ScanJob(root, this::handleEvent);
            activeJobs.add(job);
            ioPool.submit(job);
        }
    }

    /**
     * Cancel all scans
     */
    public void cancelAll() {

        isRunning.set(false);

        synchronized (activeJobs) {
            for (ScanJob job : activeJobs) {
                job.cancel();
            }
            activeJobs.clear();
        }

        remainingJobs.set(0);
    }

    /**
     * Shutdown (app exit only)
     */
    public void shutdown() {

        cancelAll();
        ioPool.shutdownNow();
    }

    // -------------------------
    // Event handling
    // -------------------------

    private void handleEvent(ScanEvent event) {

        switch (event.type()) {

            case FOUND_FOLDER -> handleFolder(event.path());

            case FOUND_FILE -> {
                // optional future extension
            }

            case DONE -> handleJobFinished();

            case ERROR -> {
                System.err.println("Scan error: " + event.path());
            }
        }
    }

    // -------------------------
    // Folder handling
    // -------------------------

    private void handleFolder(Path path) {

        if (discoveredFolders.add(path)) {

            onFolderFound.accept(path);
        }
    }

    // -------------------------
    // Completion handling (FIXED)
    // -------------------------

    private void handleJobFinished() {

        if (remainingJobs.decrementAndGet() <= 0) {

            isRunning.set(false);

            onFinished.run();
        }
    }

    // -------------------------
    // Status
    // -------------------------

    public boolean isRunning() {
        return isRunning.get();
    }

    public Set<Path> getSnapshot() {
        return new HashSet<>(discoveredFolders);
    }
}