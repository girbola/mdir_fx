
package com.girbola.controllers.main;

import com.girbola.Main;
import com.girbola.controllers.main.tasks.AddToTable;
import com.girbola.messages.Messages;
import javafx.concurrent.Task;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.girbola.concurrency.ConcurrencyUtils.exec;
import static com.girbola.concurrency.ConcurrencyUtils.getExecCounter;
import static com.girbola.messages.Messages.sprintf;


public class Sorter extends Task<Integer> {

    private List<Path> selectedFolders;
    private ModelMain model;
    private AtomicInteger counter = new AtomicInteger(0);

    public Sorter(ModelMain model, List<Path> selectedFolders) {
        this.model = model;
        this.selectedFolders = selectedFolders;
    }

    public void addToTable() {
        if (!selectedFolders.isEmpty()) {
            for (Path selectedFolder : selectedFolders) {
                sprintf("Adding folder: " + selectedFolder);
                if (Main.getProcessCancelled()) {
                    Messages.sprintf("Sorter process were cancelled");
                    exec[getExecCounter()].shutdownNow();
                    break;
                }

            }
        } else {
            sprintf("list was empty!");
        }
    }


    @Override
    protected Integer call() throws Exception {
        if (!selectedFolders.isEmpty()) {
            for (Path selectedFolder : selectedFolders) {
                sprintf("---Adding folder: " + selectedFolder);
                if (Main.getProcessCancelled()) {
                    Messages.sprintf("Sorter process were cancelled");
                    exec[getExecCounter()].shutdownNow();
                    break;
                }
                Task<Integer> addToTable = new AddToTable(selectedFolder, model);
                addToTable.setOnSucceeded(e -> Messages.sprintf("Sorter addToTable Sorter done! " + selectedFolder));
                addToTable.setOnFailed(e -> Messages.sprintf("Sorter addToTable.setOnFailed: " + selectedFolder));
                addToTable.setOnCancelled(e -> Messages.sprintf("Sorter addToTable.setOnCancelled: " + selectedFolder));

                counter.incrementAndGet();

                exec[getExecCounter()].submit(addToTable);
            }
        } else {
            sprintf("Sorter list were empty!");
        }

        return counter.get();
    }
}
