
package com.girbola.controllers.main;

import com.girbola.MDir_Stylesheets_Constants;
import com.girbola.Main;
import com.girbola.concurrency.ConcurrencyUtils;
import com.girbola.controllers.importimages.ImportImagesController;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.nio.file.Path;
import java.util.List;

import static com.girbola.Main.bundle;
import static com.girbola.Main.conf;

public class ImportImages {

	private final String ERROR = ImportImages.class.getSimpleName();

	private Scene scene;
	private ModelMain model_main;
	private List<FileInfo> fileInfo_list;
	private FolderInfo folderInfo;
	private Task<List<Path>> createFileList;
	private boolean isImporter;

	public ImportImages(Scene scene, FolderInfo aFolderInfo, ModelMain aModel_main, boolean isImporter) {
		Main.setProcessCancelled(false);
		this.scene = scene;
		this.model_main = aModel_main;
		this.folderInfo = aFolderInfo;
		this.isImporter = isImporter;

		Parent parent = null;
		FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/importimages/ImportImages.fxml"), bundle);
		try {
			parent = loader.load();
		} catch (Exception ex) {
			Messages.errorSmth(ERROR, "Problem with loading FXML", ex, Misc.getLineNumber(), true);
		}
		ImportImagesController importImagesController = (ImportImagesController) loader.getController();
		importImagesController.init(aModel_main, folderInfo, isImporter);
		Stage stage = new Stage();
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				importImagesController.stopTimeLine();
				Main.setProcessCancelled(true);
				if (createFileList != null) {
					if (createFileList.isRunning()) {
						createFileList.cancel();
						ConcurrencyUtils.stopExecThread();
					}
				}
			}
		});
		Scene importImages_scene = new Scene(parent, Misc.getScreenBounds().getWidth(), Misc.getScreenBounds().getHeight() - 50, true,
				SceneAntialiasing.BALANCED);
		importImages_scene.getStylesheets().add(Main.class.getResource(conf.getThemePath() + MDir_Stylesheets_Constants.DATEFIXER.getType()).toExternalForm());

		stage.setScene(importImages_scene);
		stage.show();
	}
}
