package com.girbola.controllers.main.populatetableview;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.girbola.controllers.loading.LoadingProcessTask;

public class SequentialMultiScan {
    private final ExecutorService pool = Executors.newSingleThreadExecutor();
    private final LoadingProcessTask loadingProcessTask;

    public SequentialMultiScan(LoadingProcessTask loadingProcessTask) {
        this.loadingProcessTask = loadingProcessTask;
    }

    public void scanInOrder(List<Path> roots) {
        if (roots.isEmpty()) {
            loadingProcessTask.setMessage("No folders to scan.");
            return;
        }
        loadingProcessTask.setProgress(0, roots.size());
        runNext(roots.iterator(), 0, roots.size(), new ArrayList<>());
    }

    private void runNext(Iterator<Path> it, int idx, int total, List<Path> aggregate) {
        if (!it.hasNext()) {
            loadingProcessTask.setProgress(total, total);
            loadingProcessTask.setMessage("All scans done. Items: " + aggregate.size());
            return;
        }
        Path root = it.next();
        FileTreeScanTask task = new FileTreeScanTask(root, /*estimate*/ true);
        // Map per-task progress into overall progress: (idx + pTask)/total
        task.progressProperty().addListener((obs, ov, nv) -> {
            double p = nv == null ? 0 : nv.doubleValue();
            loadingProcessTask.setProgress(idx + Math.max(0, p), total);
        });
        loadingProcessTask.setMessage("Scanning: " + root);

        task.setOnSucceeded(e -> {
            aggregate.addAll(task.getValue());
            loadingProcessTask.setMessage("Completed: " + root + " (" + task.getValue().size() + ")");
            runNext(it, idx + 1, total, aggregate);
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            loadingProcessTask.setMessage("Failed on " + root + ": " + (ex != null ? ex.getMessage() : "Unknown"));
            // continue to next anyway:
            runNext(it, idx + 1, total, aggregate);
        });

        pool.submit(task);
    }

    public void shutdown() { pool.shutdown(); }
}

