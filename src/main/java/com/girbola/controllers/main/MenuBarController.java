/*
 @(#)Copyright:  Copyright (c) 2012-2020 All right reserved.
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.main;

import static com.girbola.Main.bundle;
import static com.girbola.Main.conf;
import static com.girbola.messages.Messages.sprintf;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import com.girbola.Main;
import com.girbola.configuration.Configuration_SQL_Utils;
import com.girbola.controllers.datefixer.DateFixer;
import com.girbola.controllers.folderscanner.FolderScannerController;
import com.girbola.controllers.main.options.OptionsController;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.dialogs.Dialogs;
import com.girbola.fileinfo.FileInfo;
import com.girbola.fileinfo.FileInfo_Utils;
import com.girbola.messages.Messages;
import com.girbola.messages.html.HTMLClass;
import com.girbola.misc.Misc;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class MenuBarController {

	private final String ERROR = MenuBarController.class.getSimpleName();

	private Model_main model_main;

	@FXML
	private MenuItem menuItem_tools_options_viewIgnoredList;

	@FXML
	private CheckMenuItem menuItem_tools_themes_dark;
	@FXML
	private CheckMenuItem menuItem_tools_themes_light;
	@FXML
	private CheckMenuItem menuItem_tools_showFullPath;

	@FXML
	private MenuBar menuBar;

	@FXML
	private MenuItem menuItem_file_addFolders;
	@FXML
	private MenuItem menuItem_file_clear;
	@FXML
	private MenuItem menuItem_file_close;
	@FXML
	private MenuItem menuItem_file_import;
	@FXML
	private MenuItem menuItem_file_load;
	@FXML
	private MenuItem menuItem_file_save;
	@FXML
	private MenuItem menuItem_help_about;
	@FXML
	private MenuItem menuItem_help_help;
	@FXML
	private MenuItem menuItem_tools_options;
	@FXML
	private MenuItem menuItem_help_supportUs;
	@FXML
	private MenuItem menuItem_help_update;

	@FXML
	private void menuItem_file_addFolders_action(ActionEvent event) {
		Messages.sprintf("locale is: " + Main.bundle.getLocale().toString());
		model_main.getMonitorExternalDriveConnectivity().cancel();
		FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/folderscanner/FolderScanner.fxml"),
				Main.bundle);

		Parent parent = null;
		FolderScannerController folderScannerController = null;
		try {
			parent = loader.load();
			folderScannerController = (FolderScannerController) loader.getController();
		} catch (Exception ex) {
			ex.printStackTrace();
			Messages.errorSmth(ERROR,
					"Country= " + Main.bundle.getLocale().getCountry() + " location?\n: " + Main.bundle.getLocale(), ex,
					Misc.getLineNumber(), true);
		}
		Stage fc_stage = new Stage();
//		fc_stage.setWidth(conf.getScreenBounds().getWidth());
//		fc_stage.setHeight(conf.getScreenBounds().getHeight() / 1.3);
		fc_stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				model_main.getMonitorExternalDriveConnectivity().restart();
				fc_stage.close();
			}
		});

		Scene fc_scene = new Scene(parent, 800, 400);
		fc_scene.getStylesheets()
				.add(Main.class.getResource(conf.getThemePath() + "folderChooser.css").toExternalForm());
		folderScannerController.setStage(fc_stage);
		folderScannerController.setScene(fc_scene);
		folderScannerController.init(model_main);
		fc_stage.setScene(fc_scene);

		fc_stage.show();

	}

	@FXML
	private void menuItem_file_clear_action(ActionEvent event) {
		TableUtils.clearTablesContents(model_main.tables());
		Main.setChanged(false);
	}

	@FXML
	private void menuItem_file_close_action(ActionEvent event) {
		model_main.exitProgram();
	}

	@FXML
	private void menuItem_file_import_action(ActionEvent event) {
		// t�h�n tarvitaan folderinfo creator
		// srth;
		Stage stage = (Stage) menuBar.getScene().getWindow();

		DirectoryChooser dc = new DirectoryChooser();
		File file = dc.showDialog(stage);
		if (file == null) {
			return;
		} else {
			FolderInfo folderInfo = new FolderInfo(file.toPath());
			List<FileInfo> list = FileInfo_Utils.createFileInfo_list(folderInfo);

			if (list != null) {
				folderInfo.setFileInfoList(list);
				TableUtils.updateFolderInfos_FileInfo(folderInfo);
			}

			if (folderInfo.getFileInfoList().isEmpty()) {
				Messages.warningText("noMediaFilesFoundInCurrentDir");
				return;
			} else {
				Task<Void> dateFixer = new DateFixer(Paths.get(folderInfo.getFolderPath()), folderInfo, model_main,
						true);
				Thread dateFixer_th = new Thread(dateFixer, "dateFixer_th");
				dateFixer_th.start();
				// new ImportImages(model_main.getScene(), folderInfo, model_main, true);
			}
			//
			// ObservableList<Path> obs = FXCollections.observableArrayList();
			// obs.add(file.toPath());
			//
			// Task<List<Path>> createFileList = new SubList(obs);
			// sdv;
			// Thread createFileList_th = new Thread(createFileList, "createFileList_th");
			// sprintf("createFileList_th.getName(): " + createFileList_th.getName());
			// createFileList_th.start();

		}
		Messages.sprintf("menuItem_file_import_action");
		// ImportImages importImages = new ImportImages(model_main.getScene(), null,
		// model_main, true);

	}

	@FXML
	private void menuItem_file_load_action(ActionEvent event) {
		Messages.sprintf("menuItem_file_load_action");
//		boolean load = true;
		if (Main.getChanged()) {

			Dialog<ButtonType> dialog = Dialogs.createDialog_YesNoCancel(Main.scene_Switcher.getWindow(),
					bundle.getString("changesMadeDataLost"));
			dialog.getDialogPane().getButtonTypes().remove(1);
			Messages.sprintf("dialog changesDialog width: " + dialog.getWidth());
			Optional<ButtonType> result = dialog.showAndWait();
			if (result.get().getButtonData().equals(ButtonBar.ButtonData.YES)) {
				Main.setChanged(false);
				model_main.load();
				Main.setChanged(false);
			} else if (result.get().getButtonData().equals(ButtonBar.ButtonData.CANCEL_CLOSE)) {

			}
		} else {
			model_main.load();
		}

	}

	@FXML
	private void menuItem_file_save_action(ActionEvent event) {
		sprintf("menuItem_file_save_action");
		Task<Integer> saveTablesToDatabases = new SaveTablesToDatabases(model_main, Main.scene_Switcher.getWindow(), null, true);
		Thread thread = new Thread(saveTablesToDatabases, "Saving data MenuBarConctroller Thread");
		thread.start();
		
	}

	@FXML
	private void menuItem_help_about_action(ActionEvent event) {
		VBox root = new VBox();
		root.setAlignment(Pos.CENTER);
		Label programName = new Label("Organizer tool for backing up image & video files");
		Label programVersion = new Label(conf.getProgramVersion());
		Label programCopyRight = new Label("Copyright © 2012-2020");
		Label programUserInfo = new Label("Marko Lokka. marko.lokka@gmail.com");
		Label programMoreInfo = new Label("NOT FOR PUBLIC DISTRIBUTION");
		Hyperlink programHomePage = new Hyperlink(HTMLClass.programHomePage);
		programHomePage.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {

				viewWebPage(programHomePage.getText());
			}
		});
		root.getChildren().addAll(programName, programVersion, programCopyRight, programUserInfo, programMoreInfo,
				programHomePage);

		Dialog dialog = new Dialog();
		DialogPane dialogPane = new DialogPane();
		dialogPane.setContent(root);
		dialog.setDialogPane(dialogPane);
		dialog.setTitle("About");
		dialog.setResizable(true);

		dialogPane.setHeaderText("About");

		ButtonType buttonTypeYes = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);

		dialog.getDialogPane().getButtonTypes().addAll(buttonTypeYes);
		Optional<ButtonType> result = dialog.showAndWait();
		if ((result.isPresent()) && (result.get().getText().equals("OK"))) {
			dialog.close();
		}
	}

	@FXML
	private void menuItem_help_help_action(ActionEvent event) {
		viewWebPage("http://girbola.com/index.html");
	}

	@FXML
	private void menuItem_help_supportUs_action(ActionEvent event) {
		viewWebPage("http://girbola.com/supportUs.html");
	}

	@FXML
	private void menuItem_help_update_action(ActionEvent event) {
		viewWebPage("http://girbola.com/downloads.html");
	}

	@FXML
	private void menuItem_tools_options_action(ActionEvent event) {
		sprintf("menuItem_tools_options_action starting Theme path is: " + conf.getThemePath());
		FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/main/options/Options.fxml"), bundle);
		Parent parent = null;
		try {
			parent = loader.load();
		} catch (IOException ex) {
			Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
		}
		OptionsController optionsController = (OptionsController) loader.getController();
		optionsController.init(model_main);

		Stage stage_opt = new Stage();
		Scene scene_opt = new Scene(parent);
		scene_opt.getStylesheets()
				.add(Main.class.getResource(conf.getThemePath() + "option_pane.css").toExternalForm());

		stage_opt.setScene(scene_opt);
		stage_opt.setOnCloseRequest(new EventHandler<WindowEvent>() {

			@Override
			public void handle(WindowEvent event) {
				Configuration_SQL_Utils.update_Configuration();
			}
		});
		stage_opt.show();

	}

	public void init(Model_main aModel_main) {
		this.model_main = aModel_main;
		sprintf("menuBarController....");
		sprintf("menuItem_tools_showFullPath: " + conf.isShowFullPath());

		menuItem_tools_showFullPath.selectedProperty().bindBidirectional(conf.showFullPath_property());
		if (conf.getThemePath().endsWith("light/")) {
			menuItem_tools_themes_light.setSelected(true);
			menuItem_tools_themes_dark.setSelected(false);
		} else if (conf.getThemePath().endsWith("dark/")) {
			menuItem_tools_themes_light.setSelected(false);
			menuItem_tools_themes_dark.setSelected(true);
		} else {
			sprintf("Cannot find theme:" + conf.getThemePath());
			Messages.errorSmth(ERROR, "Problem by find theme path", null, Misc.getLineNumber(), false);
		}
	}

	@FXML
	private void menuItem_tools_options_viewIgnoredList_action(ActionEvent event) {
		try {
			Parent parent = FXMLLoader.load(Main.class.getResource("fxml/misc/ViewIgnoredList.fxml"), bundle);

			Scene viewIgnored_scene = new Scene(parent);
			// Main.class.getResource(conf.getThemePath() +
			// "dateFixer.css").toExternalForm());

			viewIgnored_scene.getStylesheets()
					.add(Main.class.getResource(Main.conf.getThemePath() + "viewignoredlist.css").toExternalForm());
			Stage viewIgnored_stage = new Stage();
			viewIgnored_stage.setScene(viewIgnored_scene);
			viewIgnored_stage.show();
		} catch (IOException ex) {
			Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
		}

	}

	@FXML
	private void menuItem_tools_themes_dark_action(ActionEvent event) {
		Main.scene_Switcher.getScene_main().getStylesheets().clear();
		conf.setThemePath("/resources/themes/dark/");
		menuItem_tools_themes_light.setSelected(false);
		Main.scene_Switcher.getScene_main().getStylesheets()
				.add(getClass().getResource(conf.getThemePath() + "mainStyle.css").toExternalForm());
		Configuration_SQL_Utils.update_Configuration();
	}

	@FXML
	private void menuItem_tools_themes_light_action(ActionEvent event) {
		Main.scene_Switcher.getScene_main().getStylesheets().clear();
		conf.setThemePath("/resources/themes/light/");
		menuItem_tools_themes_dark.setSelected(false);
		Main.scene_Switcher.getScene_main().getStylesheets()
				.add(getClass().getResource(conf.getThemePath() + "mainStyle.css").toExternalForm());
		Configuration_SQL_Utils.update_Configuration();
	}

	private void viewWebPage(String string) {

		try {
			Desktop.getDesktop().browse(new URL(string).toURI());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		/*
		 * Dialog dialog = new Dialog(); DialogPane dp = new DialogPane();
		 * dialog.setDialogPane(dp);
		 * 
		 * WebView wv = new WebView(); WebEngine we = wv.getEngine();
		 * dialog.setResizable(true); VBox vbox = new VBox(wv); we.load(string);
		 * HostServices.getHostServices().showDocument("http://www.yahoo.com");
		 * dp.setContent(vbox); ButtonType buttonTypeYes = new ButtonType("OK",
		 * ButtonBar.ButtonData.OK_DONE);
		 * 
		 * dialog.getDialogPane().getButtonTypes().addAll(buttonTypeYes);
		 * 
		 * Optional<ButtonType> result = dialog.showAndWait(); if ((result.isPresent())
		 * && (result.get().getText().equals("OK"))) { dialog.close(); }
		 */
	}

}
