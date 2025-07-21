
package com.girbola.controllers.main.tasks;

import com.girbola.Main;
import com.girbola.concurrency.ConcurrencyUtils;
import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.filelisting.ValidatePathUtils;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import common.utils.FileUtils;
import javafx.concurrent.Task;
import javafx.scene.control.TableView;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.girbola.Main.conf;
import static com.girbola.messages.Messages.sprintf;


public class AddToTable extends Task<Integer> {

	private final String ERROR = AddToTable.class.getSimpleName();
	private List<Path> list;
	private ModelMain model;
	private AtomicInteger counter = new AtomicInteger(0);

	public AddToTable(Path path, ModelMain model) {
		this.list = new ArrayList<>();
		this.list.add(path);
		this.model = model;
	}

	public AddToTable(List<Path> list, ModelMain model) {
		this.list = list;
		this.model = model;
	}

	@Override
	protected Integer call() throws Exception {
		for (Path p : list) {
			Messages.sprintf("PATH WOULD BE: " + p);
			if (Main.getProcessCancelled()) {
				cancel();
				ConcurrencyUtils.stopExecThreadNow();
				break;
			}
			// TODO Tämä uusiksi!

			if (folderHasFiles(p)) {
				TableType tableType = TableUtils.resolvePath(p);

				Messages.sprintf("TABLETYPE IS: " + tableType + " Path is: PPPP: " + p);
				switch (tableType) {
				case SORTED: {
					FolderInfo folderInfo = new FolderInfo(p);
					Messages.sprintf("FolderINFOOOOO: " + folderInfo.getFolderPath());
					if (!hasDuplicates(model.tables().getSorted_table(), folderInfo) || !hasDuplicates(model.tables().getSortIt_table(), folderInfo)) {
						folderInfo.setTableType(TableType.SORTED.getType());
						model.tables().getSorted_table().getItems().add(folderInfo);
						TableUtils.refreshTableContent(model.tables().getSorted_table());
						counter.incrementAndGet();
						sprintf("sorted: " + p + " c= " + counter.get());
					}
					break;
				}
				case SORTIT: {
					FolderInfo folderInfo = new FolderInfo(p);
					if (hasDuplicates(model.tables().getSortIt_table(), folderInfo) || !hasDuplicates(model.tables().getSorted_table(), folderInfo)) {
						folderInfo.setTableType(TableType.SORTIT.getType());
						model.tables().getSortIt_table().getItems().add(folderInfo);
						counter.incrementAndGet();
						sprintf("sortit: " + p + " c= " + counter.get());
						TableUtils.refreshTableContent(model.tables().getSortIt_table());
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
				return true;
			}
		}
		return false;

	}

	private boolean folderHasFiles(Path p) {
		if (isIgnoredList(p)) {
			return false;
		}
        try (DirectoryStream<Path> list = FileUtils.createDirectoryStream(p, FileUtils.filter_directories)) {

            for (Path path : list) {
                try {
                    if (ValidatePathUtils.validFile(path) && FileUtils.supportedMediaFormat(path.toFile())) {
                        return true;
                    }
                } catch (IOException ex) {
                    Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
	}

	private boolean isIgnoredList(Path path) {
		for (Path file : conf.getIgnoredFoldersScanList()) {
			if (file.equals(path)) {
				return true;
			}
		}
		return false;
	}

}
