
package com.girbola.controllers.main;

import com.girbola.Main;
import com.girbola.controllers.main.folderinfoscan.FolderInfoScanner;
import com.girbola.controllers.main.tables.FolderInfoUtils;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.controllers.main.tasks.AddToTable;
import com.girbola.messages.Messages;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.concurrent.Task;
import javafx.scene.control.TableView;

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
        if (selectedFolders.isEmpty()) {
            sprintf("Sorter list were empty!");
            return counter.get();
        }

        for (Path selectedFolder : selectedFolders) {
            sprintf("---Adding folder: " + selectedFolder);

            if (Main.getProcessCancelled() || isCancelled()) {
                Messages.sprintf("Sorter process were cancelled");
                cancel();
                break;
            }

            if (selectedFolder == null || !Files.isDirectory(selectedFolder)) {
                Messages.sprintfError("Selected folder was invalid: " + selectedFolder);
                continue;
            }

            FolderInfoScanner folderInfoScanner = new FolderInfoScanner(model.tables(), selectedFolder);

            folderInfoScanner.setOnSucceeded(event -> {
                Integer changedFolders = folderInfoScanner.getValue();
                Messages.sprintf("FolderInfoScanner completed for: "
                        + selectedFolder
                        + " changed folders: "
                        + changedFolders);
            });

            folderInfoScanner.setOnFailed(event -> {
                Throwable exception = folderInfoScanner.getException();

                if (exception != null) {
                    Messages.sprintfError("FolderInfoScanner failed for: "
                            + selectedFolder
                            + " error: "
                            + exception.getMessage());
                } else {
                    Messages.sprintfError("FolderInfoScanner failed for: " + selectedFolder);
                }
            });

            folderInfoScanner.setOnCancelled(event ->
                    Messages.sprintf("FolderInfoScanner cancelled for: " + selectedFolder));

            /*
             * Run synchronously inside this Sorter task.
             * FolderInfoScanner itself performs the recursive walk and updates/adds FolderInfo rows.
             */
            folderInfoScanner.run();

            if (folderInfoScanner.isCancelled()) {
                cancel();
                break;
            }

            if (folderInfoScanner.getException() != null) {
                throw new RuntimeException(folderInfoScanner.getException());
            }

            Integer changedFolders = folderInfoScanner.getValue();

            if (changedFolders != null) {
                counter.addAndGet(changedFolders);
            }
        }
        return counter.get();
    }


    private FolderInfo findExistingFolderInfo(Path folderPath) {
        List<TableView<FolderInfo>> allTables = TableUtils.getAllTables(model.tables());
        for (TableView<FolderInfo> table : allTables) {
            for (FolderInfo folderInfo : table.getItems()) {
                if (Paths.get(folderInfo.getFolderPath()).equals(folderPath)) {
                    return folderInfo;
                }
            }
        }
        return null;
    }
}
