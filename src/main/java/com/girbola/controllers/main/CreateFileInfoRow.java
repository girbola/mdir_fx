
package com.girbola.controllers.main;

import com.girbola.Main;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.controllers.main.tables.FolderInfoUtils;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.dialogs.Dialogs;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import javafx.concurrent.Task;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TableView;
import javafx.stage.Window;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static com.girbola.utils.FileInfoUtils.createFileInfo_list;
import static com.girbola.messages.Messages.sprintf;


public class CreateFileInfoRow extends Task<Void> {

	private AtomicInteger counter = new AtomicInteger(0);
	private final String ERROR = CreateFileInfoRow.class.getSimpleName();
	private ModelMain model_Main;
	private Window owner;
	private TableView<FolderInfo> table;

	CreateFileInfoRow(ModelMain aModel_main, TableView<FolderInfo> aTable, Window anOwner) {
		this.model_Main = aModel_main;
		this.table = aTable;
		this.owner = anOwner;
	}

	@Override
	protected Void call() throws Exception {
		updateProgress(counter.get(), table.getSelectionModel().getSelectedItems().size());
		sprintf("Update Progress: " + counter.get() + " max " + table.getSelectionModel().getSelectedItems().size());

		for (FolderInfo folderInfo : table.getSelectionModel().getSelectedItems()) {
			if (Main.getProcessCancelled() || isCancelled()) {
				// cancel();
				Messages.sprintf("Main process were cancelled. Breaking");
				break;
			}
			if (folderInfo.isConnected()) {
				if (Files.exists(Paths.get(folderInfo.getFolderPath()))) {
					sprintf("Reloading: " + folderInfo.getFolderPath());
					long start = System.currentTimeMillis();
					List<FileInfo> list = createFileInfo_list(folderInfo);
					Messages.sprintf("creating fileinfo list took: " + (System.currentTimeMillis() - start)
							+ " list size: " + list.size());
					if (list != null) {
						folderInfo.setFileInfoList(list);
						FolderInfoUtils.calculateFolderInfoStatus(folderInfo);
						TableUtils.refreshTableContent(table);

						updateProgress(counter.incrementAndGet(), table.getSelectionModel().getSelectedItems().size());
						updateMessage(folderInfo.getFolderPath());
						sprintf("Update Progress: " + counter.get() + " max "
								+ table.getSelectionModel().getSelectedItems().size());
					}
				} else {
					Dialog<ButtonType> dialog = Dialogs
							.createDialog_YesNo(owner, Main.bundle.getString("folderDoesNotExists"));
					ButtonType yes = new ButtonType(Main.bundle.getString("yes"), ButtonData.YES);
					ButtonType no = new ButtonType(Main.bundle.getString("no"), ButtonData.NO);
					dialog.getDialogPane().getButtonTypes().addAll(yes, no);
					dialog.setContentText(Main.bundle.getString("doYouWantToIgnoreTheseFiles"));

					Optional<ButtonType> result = dialog.showAndWait();
					result = dialog.showAndWait();
					if (result.get().getButtonData().equals(ButtonBar.ButtonData.YES)) {
						Messages.sprintf("yes pressed!");

					} else if (result.get().getButtonData().equals(ButtonBar.ButtonData.CANCEL_CLOSE)) {
						cancel();
						Main.setProcessCancelled(true);
					}
				}
			}
		}
		return null;
	}

}
