package com.girbola.concurrency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import javafx.concurrent.Task;

public class ConcurrencyPool {

    // CPU-heavy work (parsing, image processing, metadata, etc.)
    private static final ExecutorService CPU_POOL =
            Executors.newFixedThreadPool(
                    Math.max(2, Runtime.getRuntime().availableProcessors() - 1)
            );

    // Disk / HDD traversal (kept single-thread for predictable IO)
    private static final ExecutorService DISK_POOL =
            Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r);
                t.setName("Disk-IO-Thread");
                t.setDaemon(false);
                return t;
            });

    // Track currently running tasks (optional but useful for cancel-all)
    private static final List<Task<?>> ACTIVE_TASKS =
            Collections.synchronizedList(new ArrayList<>());

    private static final AtomicReference<Task<?>> CURRENT_DISK_TASK =
            new AtomicReference<>();

    private ConcurrencyPool() {
    }

    // -------------------------
    // PUBLIC API
    // -------------------------

    /**
     * Submit CPU-bound JavaFX Task
     */
    public static <T> Future<?> submitCpuTask(Task<T> task) {
        ACTIVE_TASKS.add(task);
        return CPU_POOL.submit(task);
    }

    /**
     * Submit disk traversal / HDD scanning Task
     * Only one disk task runs at a time (important for IO stability)
     */
    public static <T> Future<?> submitDiskTask(Task<T> task) {

        // cancel previous disk task if running
        Task<?> previous = CURRENT_DISK_TASK.getAndSet(task);
        if (previous != null) {
            previous.cancel();
        }

        ACTIVE_TASKS.add(task);
        return DISK_POOL.submit(task);
    }

    /**
     * Cancel all running tasks (UI "stop everything")
     */
    public static void cancelAllTasks() {

        synchronized (ACTIVE_TASKS) {
            for (Task<?> task : ACTIVE_TASKS) {
                task.cancel();
            }
            ACTIVE_TASKS.clear();
        }
    }

    /**
     * Stop only disk scanning
     */
    public static void cancelDiskTask() {
        Task<?> task = CURRENT_DISK_TASK.getAndSet(null);
        if (task != null) {
            task.cancel();
        }
    }

    /**
     * Graceful shutdown (app exit)
     */
    public static void shutdown() {
        cancelAllTasks();
        CPU_POOL.shutdownNow();
        DISK_POOL.shutdownNow();
    }

    // -------------------------
    // HELPERS
    // -------------------------

    /**
     * Utility: safe cancellation check inside loops
     */
    public static boolean isCancelled(Task<?> task) {
        return task.isCancelled();
    }

}
