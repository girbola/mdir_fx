package com.girbola.controllers.main;

import com.girbola.controllers.loading.LoadingProcessTask;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.controllers.main.tables.FolderInfo_Utils;
import com.girbola.controllers.main.tables.tabletype.FolderInfoStateType;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.scene.control.TableView;

public class FolderInfoStateCheck extends Task<Boolean> {

	private final String ERROR = FolderInfoStateCheck.class.getName();
	private TableView<FolderInfo> table;
	private LoadingProcessTask loadingProcess;
	private SimpleBooleanProperty updated = new SimpleBooleanProperty(false);

	public FolderInfoStateCheck(TableView<FolderInfo> table, LoadingProcessTask loadingProcess) {
		this.table = table;
		this.loadingProcess = loadingProcess;
	}

	@Override
	protected Boolean call() throws Exception {
		for (FolderInfo folderInfo : table.getSelectionModel().getSelectedItems()) {
			if (folderInfo.getState().equals(FolderInfoStateType.CHANGED.getType())) {
				FolderInfo_Utils.calculateFileInfoStatuses(folderInfo);
				folderInfo.setState(FolderInfoStateType.OK.getType());
				if(!updated.get()) {
					updated.set(true);
				}
			}
		}
		return updated.get();
	}

	@Override
	protected void succeeded() {
		super.succeeded();

	}

	@Override
	protected void cancelled() {

		super.cancelled();
		Messages.sprintf("FolderInfoStateCheck has been cancelled");
	}

	@Override
	protected void failed() {
		super.failed();
		Messages.errorSmth(ERROR, "FolderInfoStateCheck has been cancelled", null, Misc.getLineNumber(), true);
	}

}
