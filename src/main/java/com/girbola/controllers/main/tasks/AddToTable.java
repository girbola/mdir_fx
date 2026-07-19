
package com.girbola.controllers.main.tasks;

import com.girbola.Main;
import com.girbola.concurrency.ConcurrencyUtils;
import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.filelisting.ValidatePathUtils;
import com.girbola.messages.Messages;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.concurrent.Task;
import javafx.scene.control.TableView;

import static com.girbola.controllers.main.tables.tabletype.TableType.SORTED;
import static com.girbola.controllers.main.tables.tabletype.TableType.SORTIT;
import static com.girbola.messages.Messages.sprintf;


public class AddToTable extends Task<Integer> {

    private final String ERROR = AddToTable.class.getSimpleName();

    private List<Path> list;
    private ModelMain model;
    private AtomicInteger counter = new AtomicInteger(0);

    private boolean refreshTable;

    private TableType tableType;

    public AddToTable(Path path, ModelMain model) {
        this.list = new ArrayList<>();
        this.list.add(path);
        this.model = model;
        refreshTable = false;
        tableType = TableType.SORTIT;
    }

    @Override
    protected Integer call() throws Exception {
        for (Path p : list) {
            Messages.sprintf("AddToTable PATH WOULD BE: " + p);
            if (Main.getProcessCancelled()) {
                cancel();
                ConcurrencyUtils.stopExecThreadNow();
                break;
            }
            // TODO Tämä uusiksi!


            if (ValidatePathUtils.hasMediaFilesInFolder(p)) {
                tableType = TableUtils.resolveTableTypeByPath(p);

                Messages.sprintf("TABLETYPE IS: " + tableType + " Path is: PPPP: " + p);
                switch (tableType) {
                    case SORTED: {
                        FolderInfo folderInfo = new FolderInfo(p);
                        Messages.sprintf("SORTED FolderINFOOOOO: " + folderInfo.getFolderPath());
                        if (!hasDuplicates(model.tables().getSorted_table(), folderInfo) || !hasDuplicates(model.tables().getSortIt_table(), folderInfo)) {
                            folderInfo.setTableType(tableType.getType());
// TODO                            folderInfo is empty here!
                            model.tables().getSorted_table().getItems().add(folderInfo);
                            counter.incrementAndGet();
                            sprintf("Add to table Sorted: " + p + " c= " + counter.get());
                            refreshTable = true;
//						TableUtils.refreshTableContent(model.tables().getSorted_table());
                        }
                        break;
                    }
                    case SORTIT: {
                        FolderInfo folderInfo = new FolderInfo(p);
                        if (!hasDuplicates(model.tables().getSortIt_table(), folderInfo) || !hasDuplicates(model.tables().getSorted_table(), folderInfo)) {
                            folderInfo.setTableType(tableType.getType());
                            model.tables().getSortIt_table().getItems().add(folderInfo);
                            counter.incrementAndGet();
                            sprintf("Add to table SortIt: " + p + " c= " + counter.get());
                            refreshTable = true;
//						TableUtils.refreshTableContent(model.tables().getSortIt_table());
                        }
                        break;
                    }
                    default:
                        sprintf("Can't find specific place to put this folder: " + p);
                        break;
                }
                tableType = null;
            }
        }
        return null;
    }

    private boolean hasDuplicates(TableView<FolderInfo> table, FolderInfo folderInfo) {
        for (FolderInfo src_folderInfo : table.getItems()) {
            if (src_folderInfo.getFolderPath().equals(folderInfo.getFolderPath())) {
                Messages.sprintf("DUPLICATE FOUND: " + folderInfo.getFolderPath());
                return true;
            }
        }
        Messages.sprintf("NO DUPLICATE FOUND: " + folderInfo.getFolderPath());
        return false;

    }

    @Override
    protected void succeeded() {
        super.succeeded();
        if (refreshTable) {
            if (tableType.getType().equals(SORTED.getType())) {
                TableUtils.refreshTableContent(model.tables().getSorted_table());
            } else if (tableType.getType().equals(SORTIT.getType())) {
                TableUtils.refreshTableContent(model.tables().getSortIt_table());
            }
        }
    }

    @Override
    protected void cancelled() {
        Messages.sprintf("AddToTable cancelled.");
    }

    @Override
    protected void failed() {
        Messages.sprintf("AddToTable failed.");
    }

}
