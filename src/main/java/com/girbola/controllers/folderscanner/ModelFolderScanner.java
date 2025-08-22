
package com.girbola.controllers.folderscanner;

import com.girbola.controllers.folderscanner.choosefolders.ChooseFoldersController;
import com.girbola.controllers.main.ModelMain;
import com.girbola.drive.DriveInfo;
import com.girbola.drive.DriveInfoUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ModelFolderScanner {

	//	@SuppressWarnings("unused")
	private ModelMain modelMain;

//	private Connection connection = SqliteConnection.connector(Main.conf.getAppDataPath(), Main.conf.getFolderInfos_db_fileName());

	private ScanDrives scanDrives;
	private DriveInfoUtils driveInfoUtils = new DriveInfoUtils();

	private ChooseFoldersController chooseFoldersController;

	private List<TreeItem<FolderInfoTable>> analyzeList_selected = new ArrayList<>();
	private ObservableList<Path> selectedDrivesFoldersListObs = FXCollections.observableArrayList();
	private VBox analyzeList_vbox;
	private CheckBoxTreeItem<File> drivesRootItem;

	public ScanDrives getScanDrives() {
		return scanDrives;
	}

	public DriveInfoUtils drive() {
		return this.driveInfoUtils;
	}

	public List<TreeItem<FolderInfoTable>> getAnalyzeList_selected() {
		return analyzeList_selected;
	}

	public void setAnalyzeList_selected(List<TreeItem<FolderInfoTable>> analyzeList_selected) {
		this.analyzeList_selected = analyzeList_selected;
	}

	public VBox getAnalyzeList_vbox() {
		return analyzeList_vbox;
	}

	public void setAnalyzeList_vbox(VBox analyzeList_vbox) {
		this.analyzeList_vbox = analyzeList_vbox;
	}

	public ObservableList<Path> getSelectedDrivesFoldersListObs() {
		return selectedDrivesFoldersListObs;
	}

	// public void setDrivesList(ObservableList<SelectedFolder> drivesList) {
	// this.drivesList = drivesList;
	// }

	void setChooseFoldersController(ChooseFoldersController chooseFoldersController) {
		this.chooseFoldersController = chooseFoldersController;
	}

	public ChooseFoldersController getChooseFoldersController() {
		return chooseFoldersController;
	}

    @Deprecated(since = "1.0", forRemoval = false)
    public void setDeleteKeyPressed(TableView<SelectedFolder> table) {
		table.setOnKeyPressed((KeyEvent event) -> {
			if (event.getCode() == (KeyCode.DELETE)) {
				ObservableList<SelectedFolder> table_row_list = table.getSelectionModel().getSelectedItems();

				List<SelectedFolder> listToRemove = new ArrayList<>();

				for (SelectedFolder folderInfo : table_row_list) {
					listToRemove.add(folderInfo);
				}

				modelMain.getSelectedFolders().getSelectedFolderScanner_obs().removeAll(listToRemove);
				listToRemove.clear();

				table.getSelectionModel().clearSelection();

			}
		});
	}

	public void init(ModelMain modelMain, CheckBoxTreeItem<File> drivesRootItem) {
		this.modelMain = modelMain;
		this.drivesRootItem = drivesRootItem;
		List<DriveInfo> driveInfos = modelMain.driveInfos();
		scanDrives = new ScanDrives(this.modelMain, this.drivesRootItem, selectedDrivesFoldersListObs, driveInfoUtils, this);

		scanDrives.restart();
	}
//
//	public Connection getConnection() {
//		return connection;
//	}
}
