package com.girbola.controllers.main.populatetableview;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Label;

public class SequentialMultiScan {
    private final ExecutorService pool = Executors.newSingleThreadExecutor();
    private final ProgressBar totalProgress;
    private final Label statusLabel;

    public SequentialMultiScan(ProgressBar totalProgress, Label statusLabel) {
        this.totalProgress = totalProgress;
        this.statusLabel = statusLabel;
    }

    public void scanInOrder(List<Path> roots) {
        if (roots.isEmpty()) {
            statusLabel.setText("No folders to scan.");
            return;
        }
        totalProgress.setProgress(0);
        runNext(roots.iterator(), 0, roots.size(), new ArrayList<>());
    }

    private void runNext(Iterator<Path> it, int idx, int total, List<Path> aggregate) {
        if (!it.hasNext()) {
            totalProgress.setProgress(1.0);
            statusLabel.setText("All scans done. Items: " + aggregate.size());
            return;
        }

        Path root = it.next();
        FileTreeScanTask task = new FileTreeScanTask(root, /*estimate*/ true);

        // Map per-task progress into overall progress: (idx + pTask)/total
        task.progressProperty().addListener((obs, ov, nv) -> {
            double p = nv == null ? 0 : nv.doubleValue();
            totalProgress.setProgress((idx + Math.max(0, p)) / total);
        });
        statusLabel.textProperty().bind(task.messageProperty());

        task.setOnSucceeded(e -> {
            statusLabel.textProperty().unbind();
            aggregate.addAll(task.getValue());
            statusLabel.setText("Completed: " + root + " (" + task.getValue().size() + ")");
            runNext(it, idx + 1, total, aggregate);
        });

        task.setOnFailed(e -> {
            statusLabel.textProperty().unbind();
            Throwable ex = task.getException();
            statusLabel.setText("Failed on " + root + ": " + (ex != null ? ex.getMessage() : "Unknown"));
            // continue to next anyway:
            runNext(it, idx + 1, total, aggregate);
        });

        pool.submit(task);
    }

    public void shutdown() { pool.shutdown(); }
}
