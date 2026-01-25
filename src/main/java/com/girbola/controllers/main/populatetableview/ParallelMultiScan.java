package com.girbola.controllers.main.populatetableview;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;

public class ParallelMultiScan {
    private final ExecutorService pool;
    private final ProgressBar overallProgress;
    private final Label statusLabel;

    // Track per-task progress to compute overall average
    private final Map<FileTreeScanTask, DoubleProperty> progresses = new ConcurrentHashMap<>();

    public ParallelMultiScan(ProgressBar overallProgress, Label statusLabel, int maxParallel) {
        this.overallProgress = overallProgress;
        this.statusLabel = statusLabel;
        this.pool = Executors.newFixedThreadPool(Math.max(1, maxParallel));
    }

    public void scanConcurrently(List<Path> roots) {
        if (roots.isEmpty()) {
            statusLabel.setText("No folders to scan.");
            return;
        }
        overallProgress.setProgress(0);

        List<FileTreeScanTask> tasks = new ArrayList<>();
        for (Path root : roots) {
            FileTreeScanTask task = new FileTreeScanTask(root, /*estimate*/ true);
            DoubleProperty p = new SimpleDoubleProperty(0.0);
            progresses.put(task, p);

            task.progressProperty().addListener((obs, ov, nv) -> {
                double v = nv == null ? 0 : nv.doubleValue();
                // clamp for indeterminate case (< 0) to 0 so average remains sane
                p.set(v >= 0 ? v : 0);
                updateOverall();
            });

            task.setOnSucceeded(e -> {
                progresses.get(task).set(1.0);
                statusLabel.setText("Completed: " + root + " (" + task.getValue().size() + " items)");
                updateOverall();
            });

            task.setOnFailed(e -> {
                progresses.get(task).set(1.0); // mark done for aggregate
                Throwable ex = task.getException();
                statusLabel.setText("Failed: " + root + " -> " + (ex != null ? ex.getMessage() : "Unknown"));
                updateOverall();
            });

            tasks.add(task);
        }

        tasks.forEach(pool::submit);
    }

    private void updateOverall() {
        double sum = progresses.values().stream().mapToDouble(DoubleProperty::get).sum();
        double n = Math.max(1, progresses.size());
        overallProgress.setProgress(sum / n);
    }

    public void shutdown() { pool.shutdown(); }
}
