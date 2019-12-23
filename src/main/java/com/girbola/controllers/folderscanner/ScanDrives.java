package com.girbola.controllers.folderscanner;

import static com.girbola.messages.Messages.sprintf;

import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.girbola.Main;
import com.girbola.drive.Drive;
import com.girbola.drive.DriveInfo;
import com.girbola.filelisting.ValidatePathUtils;
import com.girbola.messages.Messages;
import com.girbola.sql.SQL_Utils;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.util.Duration;

public class ScanDrives {

	private CheckBoxTreeItem rootItem;
	private DriveInfo driveInfo;
	private ObservableList<Path> drivesList_selected_obs;
	private Model_folderScanner model_folderScanner;

	private int i = 0;
	private int rootCount = 0;
	private Drive drive;

	public ScanDrives(CheckBoxTreeItem rootItem, ObservableList<Path> aDrivesList_selected_obs, Drive drive, Model_folderScanner aModel_folderScanner) {
		this.rootItem = rootItem;
		this.drivesList_selected_obs = aDrivesList_selected_obs;
		this.drive = drive;
		this.model_folderScanner = aModel_folderScanner;

		scanner.setPeriod(Duration.seconds(5));

	}

	public void restart() {
		// scanner.reset();
		scanner.restart();
	}

	public void stop() {

		scanner.cancel();
		sprintf("Scanning cancelled? " + scanner.isRunning());
	}

	ScheduledService<Void> scanner = new ScheduledService<Void>() {

		@Override
		protected Task createTask() {
			return new Task<Integer>() {
				@Override
				protected Integer call() throws Exception {
					File[] listOfRoots = null;
					if (com.sun.jna.Platform.isWindows()) {
						listOfRoots = File.listRoots();
					} else if (com.sun.jna.Platform.isMac()) {
						File media = new File(File.separator + "media");
						listOfRoots = media.listFiles();
					} else if (com.sun.jna.Platform.isLinux()) {
						File media = new File(File.separator + "media");
						listOfRoots = media.listFiles();
					}

					i = 0;
					if (listOfRoots.length != rootCount && listOfRoots != null) {
						rootItem.getChildren().clear();
						rootCount = listOfRoots.length;

						for (i = 0;
								i < listOfRoots.length;
								i++) {
							CheckBoxTreeItem<String> checkBoxTreeItem = createBranch(listOfRoots[i].toString());
							DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(listOfRoots[i].toString()));
							for (Path f : stream) {
								if (ValidatePathUtils.validFolder(f)) {
									Messages.sprintf("==== validfolderstream file: " + f);
									if (System.getProperty("user.home").contains(f.toString())) {
										if (Files.exists(Paths.get(System.getProperty("user.home") + File.separator + "Pictures"))) {
											CheckBoxTreeItem<String> checkBoxTreeItem2 = createBranch(System.getProperty("user.home") + File.separator + "Pictures");
											checkBoxTreeItem2.setSelected(true);
											checkBoxTreeItem.getChildren().add(checkBoxTreeItem2);
										}
										if (Files.exists(Paths.get(System.getProperty("user.home") + File.separator + "Videos"))) {
											CheckBoxTreeItem<String> checkBoxTreeItem3 = createBranch(System.getProperty("user.home") + File.separator + "Videos");
											checkBoxTreeItem3.setSelected(true);
											checkBoxTreeItem.getChildren().add(checkBoxTreeItem3);
										}
									} else {
										CheckBoxTreeItem<String> checkBoxTreeItem2 = createBranch(f.toString());
										checkBoxTreeItem.getChildren().add(checkBoxTreeItem2);
									}
								}
							}
							rootItem.getChildren().add(checkBoxTreeItem);
						}
					}
					return null;
				}

				private CheckBoxTreeItem<String> createBranch(String string) {
					CheckBoxTreeItem<String> cb = new CheckBoxTreeItem<>(string);
					cb.setExpanded(true);
					cb.selectedProperty().addListener(new ChangeListener<Boolean>() {
						@Override
						public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
							sprintf("Path selection: " + cb.getValue());
							if (newValue == true) {

								if (Main.conf.getWorkDir().contains(cb.getValue())) {
									Platform.runLater(() -> {
										cb.setSelected(false);
										Messages.warningText(Main.bundle.getString("workDirConflict"));
									});

									drivesList_selected_obs.remove(Paths.get(cb.getValue()));
								} else {
									model_folderScanner.getDrivesList_selected_obs().add(Paths.get(cb.getValue()));
									sprintf("drive selected: " + cb.getValue());
								}
							} else {
								sprintf("drive de-selected: " + cb.getValue());
								drivesList_selected_obs.remove(Paths.get(cb.getValue()));
							}
							drive.createDriveInfo(cb.getValue(), newValue);
						}
					});
					if (drive.hasDrive(string)) {
						for (DriveInfo d : drive.getDrivesList()) {
							if (d.getDrivePath().equals(string)) {
								cb.setSelected(d.getSelected());
							}
						}
					} else {
						Messages.sprintf("has no drive in list");
						drive.createDriveInfo(cb.getValue(), false);
					}
					return cb;
				}
			};
		}
	};

}