
package com.girbola.controllers.main;

import com.girbola.Main;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.controllers.main.tasks.AddToTable;
import com.girbola.messages.Messages;
import java.nio.file.Path;
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
        if (!selectedFolders.isEmpty()) {
            for (Path selectedFolder : selectedFolders) {
                sprintf("---Adding folder: " + selectedFolder);
                if (Main.getProcessCancelled()) {
                    Messages.sprintf("Sorter process were cancelled");
                    exec[getExecCounter()].shutdownNow();
                    break;
                }

//                List<TableView<FolderInfo>> allTables = TableUtils.getAllTables(model.tables());

                TableView<FolderInfo> folderInfoExistence = TableUtils.getExistingTableFolderInfo(TableUtils.getAllTables(model.tables()), selectedFolder);
                if(folderInfoExistence == null) {
                    Task<Integer> addToTable = new AddToTable(selectedFolder, model);
                    addToTable.setOnSucceeded(e -> Messages.sprintf("Sorter addToTable Sorter done! " + selectedFolder));
                    addToTable.setOnFailed(e -> Messages.sprintf("Sorter addToTable.setOnFailed: " + selectedFolder));
                    addToTable.setOnCancelled(e -> Messages.sprintf("Sorter addToTable.setOnCancelled: " + selectedFolder));

                    counter.incrementAndGet();

                    exec[getExecCounter()].submit(addToTable);
                } else {
                    sprintf("Folder already in table: " + selectedFolder);
                    counter.incrementAndGet();

                    Task<Integer> updateObservableListFolderInfoContent = new UpdateObservableListFolderInfoContent(model.tables(), selectedFolder);
//                    UpdateFolderInfoContent updateFolderI¸nfoContent = new UpdateFolderInfoContent(folderInfoExistence.getItems());

                    exec[getExecCounter()].submit(updateObservableListFolderInfoContent);
                }

//if(!TableUtils.tableHasFolder(selectedFolder) {
//    sprintf("Table already has this folder: " + selectedFolder);
//
//}


            }
        } else {
            sprintf("Sorter list were empty!");
        }

        return counter.get();
    }
}
