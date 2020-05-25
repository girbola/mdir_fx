package com.girbola.fxml.operate;

import static com.girbola.messages.Messages.sprintf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.girbola.Main;
import com.girbola.controllers.main.Model_main;
import com.girbola.controllers.main.Model_operate;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.dialogs.YesNoCancelDialogController;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.sql.SQL_Utils;
import com.girbola.sql.SqliteConnection;

import common.utils.FileUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

public class OperateFiles extends Task<Boolean> {

	private final String ERROR = OperateFiles.class.getSimpleName();
	private boolean close;
	private Model_operate model_operate = new Model_operate();
	private List<FileInfo> list = new ArrayList<>();
	private Model_main model_main;
	private String scene_NameType;
	private List<FileInfo> listCopiedFiles = new ArrayList<>();

	public OperateFiles(List<FileInfo> list, boolean close, Model_main aModel_main, String scene_NameType) {
		Messages.sprintf("OperateFiles starting LIST");
		this.list = list;
		this.close = close;
		this.model_main = aModel_main;
		this.scene_NameType = scene_NameType;
	}

	@Override
	protected Boolean call() throws Exception {
		Main.setProcessCancelled(false);
		try {
			if (!Files.exists(Paths.get(Main.conf.getWorkDir()).toRealPath())) {
				Messages.warningText(Main.bundle.getString("cannotFindWorkDir"));
				Messages.sprintfError(Main.bundle.getString("cannotFindWorkDir"));
				return null;
			}

		} catch (IOException ex) {
			ex.printStackTrace();
			Messages.warningText_title(ex.getMessage(), Main.bundle.getString("cannotFindWorkDir"));
		}

		Parent parent = null;
		FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/operate/OperateDialog.fxml"), Main.bundle);
		parent = loader.load();

		OperateDialogController operateDialogController = (OperateDialogController) loader.getController();
		operateDialogController.init(model_operate);

		Scene operate_scene = new Scene(parent);
		operate_scene.getStylesheets()
				.add(Main.class.getResource(Main.conf.getThemePath() + "operateFiles.css").toExternalForm());
		Platform.runLater(() -> {
			Main.scene_Switcher.getWindow().setScene(operate_scene);
		});

		Main.scene_Switcher.getWindow().setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				Model_main model_Main = (Model_main) Main.getMain_stage().getUserData();
				Main.scene_Switcher.getWindow().setScene(Main.scene_Switcher.getScene_main());
				Main.getMain_stage().setOnCloseRequest(model_Main.exitProgram);
				event.consume();
			}
		});

		return null;
	}

	private class Copy extends Task<Integer> {
		private AtomicInteger counter = new AtomicInteger(list.size());
		private int byteRead;
		// private long currentFileByte;
		private long currentSize;
		private String STATE = "";
		private Path source = null;
		private Path dest = null;

		@Override
		protected Integer call() throws Exception {

			if (!Files.exists(Paths.get(Main.conf.getWorkDir()).toRealPath())) {
				Messages.warningText(Main.bundle.getString("cannotFindWorkDir"));
				cancel();
				model_operate.stopTimeLine();
				Main.setProcessCancelled(true);
				return null;
			}
			if (isCancelled()) {
				Messages.sprintf("Copy process is cancelled");
				Main.setProcessCancelled(true);
				model_operate.stopTimeLine();
				return null;
			}
			if (Main.getProcessCancelled()) {
				Messages.sprintf("Copy process getProcessCancelled is cancelled");
				cancel();
				model_operate.stopTimeLine();
				return null;
			}
			if (!list.isEmpty()) {
//				model_operate.getStart_btn().setDisable(false);
				model_operate.getTimeline().play();
			} else {
				Messages.warningText("List were empty!!!!!!!");
				cancel();
				Main.setProcessCancelled(true);
				return null;
			}

			boolean copy = false;
			for (FileInfo fileInfo : list) {
				Messages.sprintf("Copying file: " + fileInfo.getOrgPath() + " dest: " + fileInfo.getWorkDir()
						+ fileInfo.getDestination_Path());
				copy = false;
				if (isCancelled()) {
					Main.setProcessCancelled(true);
					cancel();
					break;
				}
				if (Main.getProcessCancelled()) {
					cancel();
					Main.setProcessCancelled(true);
					break;
				}
				if (fileInfo.getDestination_Path() == null) {
					Messages.warningText("getDestination_Path were null: " + fileInfo.getOrgPath());
					cancel();
					Main.setProcessCancelled(true);
					break;
				}

				if (fileInfo.getDestination_Path().isEmpty()) {
					Messages.warningText("getDestination_Path were empty: " + fileInfo.getOrgPath());
					cancel();
					Main.setProcessCancelled(true);
					break;
				}

				source = Paths.get(fileInfo.getOrgPath());

				// dest = DestinationResolver.getDestinationFileName(fileInfo);

				dest = Paths.get(fileInfo.getWorkDir() + fileInfo.getDestination_Path());

				Messages.sprintf("source is: " + source + " dest: " + dest);
				updateSourceAndDestProcessValues();
				if (source.getParent().toString().contains(Main.conf.getWorkDir())) {
					Messages.warningText(Main.bundle.getString("conflictWithWorkDir"));
					Main.setProcessCancelled(true);
					cancel();
					break;
				}
				if (Files.exists(dest)) {
					try {
						Path dest_test = FileUtils.renameFile(source, dest);
						if (dest_test == null) {
							updateIncreaseDuplicatesProcessValues();

							copy = false;
							STATE = Copy_State.DUPLICATE.getType();
						} else {
							if (dest_test != dest) {
								dest = dest_test;
								copy = true;
								STATE = Copy_State.RENAME.getType();
								String newDest = FileUtils.parseWorkDir(dest.toString(), fileInfo.getWorkDir());
								fileInfo.setDestination_Path(newDest);
								fileInfo.setWorkDirDriveSerialNumber(Main.conf.getWorkDirSerialNumber());
								Messages.sprintf("Renamed: " + fileInfo.getDestination_Path());
								updateIncreaseRenamedProcessValues();
							} else if (dest_test == dest) {
								copy = false;
								STATE = Copy_State.DUPLICATE.getType();
								updateIncreaseDuplicatesProcessValues();
							}
						}
					} catch (IOException ex) {
						ex.printStackTrace();
						cancel();
						Main.setProcessCancelled(true);
						Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
					}

				} else {
					STATE = Copy_State.COPY.getType();
					copy = true;
				}

				Messages.sprintf("Dest: " + dest + " copy? " + copy);
				if (dest != null && copy) {
					try {
						if (!Files.exists(dest.getParent())) {
							Messages.sprintf("destFolder: " + dest.getParent() + " did NOT exists");
							Files.createDirectories(dest.getParent());
						}
					} catch (Exception ex) {
						Messages.errorSmth(ERROR, Main.bundle.getString("cannotCreateDirectories"), ex,
								Misc.getLineNumber(), true);
						cancel();
						Main.setProcessCancelled(true);
					}

					if (!Files.exists(dest.getParent())) {
						Messages.errorSmth(ERROR, "Folder were not able to create. Folder name: " + dest.getParent(),
								null, Misc.getLineNumber(), true);
						Main.setProcessCancelled(true);
						break;
					}
					// long fileSize = source.toFile().length();
					// Not ready yet copyFile(source, dest);
					try {
						Path destTmp = Paths.get(dest.toFile() + ".tmp");

						Files.deleteIfExists(destTmp);

						InputStream from = new FileInputStream(source.toFile());
						OutputStream to = new FileOutputStream(destTmp.toFile());
						resetAndupdateSourceAndDestProcessValues();

						long nread = 0L;
						byteRead = 0;
						byte[] buf = new byte[8192];
						sprintf("----] Starting copying: " + source);
						while ((byteRead = from.read(buf)) > 0) {
							if (Main.getProcessCancelled()) {
								cleanCancelledFile(from, to);
								Messages.sprintf("Cleanup is done");
								return null;
							}
							to.write(buf, 0, byteRead);
							nread += byteRead;
							Messages.sprintf("Nread: " + nread + " of "
									+ model_operate.getCopyProcess_values().getFilesCopyProgress_MAX_tmp());
							updateIncreaseLastSecondFileSizeProcessValues();
						}
						from.close();
						to.flush();
						to.close();
						resetAndUpdateFileCopiedProcessValues();

						Window window = (Window) model_operate.getStart_btn().getScene().getWindow();
						if (window == null) {
							Messages.sprintfError("Error! Window were null");
						} else {
							Messages.sprintf("THIS WORKS!");
						}

						if (nread != model_operate.getCopyProcess_values().getFilesCopyProgress_MAX_tmp()) {
							int answer = -1;
							FutureTask<Integer> task = new FutureTask(new Dialogue(
									(Window) model_operate.getStart_btn().getScene().getWindow(), fileInfo,
									model_operate.getCopyProcess_values().getFilesCopyProgress_MAX_tmp(), answer));
							Platform.runLater(task);
							answer = task.get();

							if (answer == 0) {
								renameTmpFileBackToOriginalExtentension(fileInfo, destTmp, dest);
							} else if (answer == 1) {
								Messages.sprintf("Don't keep the file. Tmp file will be deleted: " + destTmp);
								Files.deleteIfExists(destTmp);
							} else if (answer == 2) {
								Messages.sprintf("Cancel pressed. This is not finished!");
								cancel();
								Main.setProcessCancelled(true);
								return null;
							}
						} else {
							renameTmpFileBackToOriginalExtentension(fileInfo, destTmp, dest);
						}
						if (STATE.equals(Copy_State.COPY.getType())) {
							Platform.runLater(() -> {
								model_operate.getCopyProcess_values().increaseCopied_tmp();
							});
							fileInfo.setCopied(true);
						} else if (STATE.equals(Copy_State.RENAME.getType())) {
							Platform.runLater(() -> {
								model_operate.getCopyProcess_values().increaseRenamed_tmp();
							});
							fileInfo.setCopied(true);
						} else {
							sprintf("STATE ERROR! : " + STATE);
						}
					} catch (Exception ex) {
						Logger.getLogger(OperateFiles.class.getName()).log(Level.SEVERE, null, ex);
						byteRead = 0;
						Main.setProcessCancelled(true);
						cancel();
						Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
					}
				}
				Platform.runLater(() -> {
					model_operate.getCopyProcess_values().setFilesLeft(counter.decrementAndGet());
				});
			}
			model_operate.getCopyProcess_values().update();
			return null;
		}

		private void renameTmpFileBackToOriginalExtentension(FileInfo fileInfo, Path destTmp, Path dest2) {
			try {
				Messages.sprintf(
						"Renaming file .tmp back to org extension: " + dest.toString() + ".tmp" + " to dest: " + dest);
				Files.move(Paths.get(dest.toString() + ".tmp"), dest);
				String newName = FileUtils.parseWorkDir(dest.toString(), fileInfo.getWorkDir());
				fileInfo.setDestination_Path(newName);
				fileInfo.setWorkDirDriveSerialNumber(Main.conf.getWorkDirSerialNumber());
				fileInfo.setCopied(true);
				listCopiedFiles.add(fileInfo);
				boolean added = model_main.getWorkDir_Handler().add(fileInfo);
				if (added) {
					Messages.sprintf("FileInfo added to destination succesfully");
//					model_main.getWorkDir_Handler().add(fileInfo);
				} else {
					Messages.sprintf("FileInfo were not added somehow...: " + fileInfo);
				}
			} catch (Exception ex) {
				Messages.sprintfError(ex.getMessage());
			}

		}

		private void resetAndUpdateFileCopiedProcessValues() {
			Platform.runLater(() -> {
				model_operate.getCopyProcess_values().setLastSecondFileSize_tmp(0);
				model_operate.getCopyProcess_values().decreaseFilesLeft_tmp();
			});
		}

		private void updateIncreaseLastSecondFileSizeProcessValues() {
			Platform.runLater(() -> {
				model_operate.getCopyProcess_values().increaseLastSecondFileSize_tmp(byteRead);
			});
		}

		private void resetAndupdateSourceAndDestProcessValues() {
			Platform.runLater(() -> {
				model_operate.getCopyProcess_values().setCopyFrom_tmp(source.toString());
				model_operate.getCopyProcess_values().setCopyTo_tmp(dest.toString());
				model_operate.getCopyProcess_values().setCopyProgress(0);
				model_operate.getCopyProcess_values().setFilesCopyProgress_MAX_tmp(source.toFile().length());
			});
		}

		private void updateIncreaseRenamedProcessValues() {
			Platform.runLater(() -> {
				model_operate.getCopyProcess_values().increaseRenamed_tmp();
			});
		}

		private void updateIncreaseDuplicatesProcessValues() {
			Platform.runLater(() -> {
				model_operate.getCopyProcess_values().increaseDuplicated_tmp();
				Messages.sprintf("Increader dups is now: " + model_operate.getCopyProcess_values().getDuplicated_tmp());
			});
		}

		private void updateSourceAndDestProcessValues() {
			Platform.runLater(() -> {
				model_operate.getCopyProcess_values().setCopyFrom(source.toString());
				model_operate.getCopyProcess_values().setCopyTo(dest.toString());
			});

		}

		private void cleanCancelledFile(InputStream from, OutputStream to) {
			sprintf("cleanCancelledFile cancelled");
			try {
				from.close();
				to.close();
				if (Files.size(source) != Files.size(dest)) {
					Files.deleteIfExists(Paths.get(dest.toString() + ".tmp"));
					sprintf("2file is gonna be deleted! " + dest.toString());
				}
			} catch (Exception ex) {
				// task.cancel();
				Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
			}
			// task.cancel();
		}

		@Override
		protected void failed() {
			model_operate.stopTimeLine();

			TableUtils.refreshAllTableContent(model_main.tables());
//			model_operate.doneButton(scene_NameType, close);
			Messages.errorSmth(ERROR, "", null, Misc.getLineNumber(), true);
		}

		@Override
		protected void cancelled() {
			Messages.sprintf("OperateFiles COPY were cancelled");
			model_operate.stopTimeLine();
			model_operate.doneButton(scene_NameType, close);
			TableUtils.refreshAllTableContent(model_main.tables());
		}

		@Override
		protected void succeeded() {
			Messages.sprintf("OperateFiles COPY were succeeded");
			model_operate.stopTimeLine();
			model_operate.doneButton(scene_NameType, close);
			model_operate.stopTimeLine();
			Task<Void> saveWorkDirToDatabase = new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					Messages.sprintf("Step1");
					if (model_main.getWorkDir_Handler() == null) {
						Messages.sprintf("Workdir_handler null");
					}
					boolean saveWorkDirList = model_main.getWorkDir_Handler().saveWorkDirListToDatabase();
					Messages.sprintf("Step2");
					TableUtils.refreshAllTableContent(model_main.tables());
					if (saveWorkDirList) {
						Messages.sprintf("saveWorkDirList DONE!!!");
					} else {
						Messages.sprintf("saveWorkDirList FAILED");
					}
					TableUtils.updateAllFolderInfos(model_main.tables());
					TableUtils.refreshAllTableContent(model_main.tables());
					return null;
				}
			};
			// @formatter:on
			saveWorkDirToDatabase.setOnSucceeded((eventti) -> {
				Messages.sprintf("saveWorkDirListToDatabase finished success!");
				writeToDatabase();
			});
			saveWorkDirToDatabase.setOnCancelled((eventti) -> {
				Messages.sprintfError("saveWorkDirListToDatabase finished success!");
				writeToDatabase();

			});
			saveWorkDirToDatabase.setOnFailed((eventti) -> {
				Messages.sprintf("saveWorkDirToDatabase Task failed!");
				writeToDatabase();

			});

			Thread savingWorkDirContent = new Thread(saveWorkDirToDatabase, "Saving workDir content");
			savingWorkDirContent.setDaemon(true);
			savingWorkDirContent.start();

			sprintf("OperateFiles succeeded");
		}

		private void writeToDatabase() {
			Messages.sprintf("Insert worked!");
			if (Main.conf.getDrive_connected()) {
				try {

					Connection connection = SqliteConnection.connector(Paths.get(Main.conf.getWorkDir()),
							Main.conf.getFileInfo_db_fileName());
					connection.setAutoCommit(false);
//					listCopiedFiles
					SQL_Utils.insertFileInfoListToDatabase(connection, listCopiedFiles);
//					boolean clean = model_main.getWorkDir_Handler().cleanDatabase(connection);
//					if (clean) {
//						Messages.sprintf("clean is done succeeded");
//					} else {
//						Messages.sprintfError("clean not worked");
//					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}

		@Override
		protected void running() {
			if (!isRunning()) {
				Messages.sprintf("OperateFiles running were stopped");
			}
			model_operate.getStart_btn().setDisable(true);
			model_operate.getCancel_btn().setDisable(false);
		}
	}

	@Override
	protected void succeeded() {

		super.succeeded();
		Messages.sprintf("stage is showing");
		if (!list.isEmpty()) {
			model_operate.getStart_btn().setDisable(false);
		} else {
			Messages.warningText("List were empty!");
			return;
		}
		model_operate.getCopyProcess_values().setTotalFiles(String.valueOf(list.size()));
		long totalSize = 0;
		for (FileInfo fileInfo : list) {
			totalSize += fileInfo.getSize();
		}

		Messages.sprintf(
				"totalSize: " + totalSize + 10000000L + " workdir: " + new File(Main.conf.getWorkDir()).getFreeSpace());
		Platform.runLater(() -> {

			model_operate.getStart_btn().setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					Task<Integer> copy = new Copy();

					/*
					 * copy.setOnSucceeded((WorkerStateEvent eventWorker) -> {
					 * Messages.sprintf("copy succeeded"); }); copy.setOnFailed((WorkerStateEvent
					 * eventWorker) -> { Messages.sprintf("copy failed"); });
					 * copy.setOnCancelled((WorkerStateEvent eventWorker) -> {
					 * model_operate.getCancel_btn().setText(Main.bundle.getString("close"));
					 * model_operate.doneButton(scene_NameType, close);
					 * Messages.sprintf("copy cancelled"); });
					 */
					Thread copy_thread = new Thread(copy, "Copy Thread");
					copy_thread.start();
				}
			});
			model_operate.getCancel_btn().setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					Main.setProcessCancelled(true);
					Messages.sprintf("Current file cancelled is: " + model_operate.getCopyProcess_values().getCopyTo());
					model_operate.stopTimeLine();
					Main.setProcessCancelled(true);
				}
			});
		});

	}

	@Override
	protected void cancelled() {
		super.cancelled();
		Messages.sprintf("OperateFiles were cancelled!");
	}

	@Override
	protected void failed() {
		super.failed();
		Messages.warningText("OperateFiles were cancelled!");
	}

	class Dialogue implements Callable<Integer> {
		private SimpleIntegerProperty answer;

		private Window owner;
		private FileInfo fileInfo;
		private long copyedFileCurrentSize;

		Dialogue(Window owner, FileInfo fileInfo, long copyedFileCurrentSize, int answer) {
			this.owner = owner;
			this.fileInfo = fileInfo;
			this.copyedFileCurrentSize = copyedFileCurrentSize;
			this.answer = new SimpleIntegerProperty(answer);
		}

		@Override
		public Integer call() throws Exception {
			FXMLLoader loader = null;
			Parent parent = null;
			YesNoCancelDialogController yesNoCancelDialogController = null;
			try {
				loader = new FXMLLoader(Main.class.getResource("dialogs/YesNoCancelDialog.fxml"), Main.bundle);
				parent = loader.load();

				yesNoCancelDialogController = (YesNoCancelDialogController) loader.getController();

				final Stage stage = new Stage();
				stage.setTitle(Main.bundle.getString("corruptedFile"));
				stage.initOwner(owner);
				stage.initStyle(StageStyle.UTILITY);
				stage.initModality(Modality.WINDOW_MODAL);
				yesNoCancelDialogController.init(stage, answer, fileInfo, "Corrupted file",
						"Corrupted file found at " + fileInfo.getOrgPath() + " size should be: " + fileInfo.getSize()
								+ " but it is now " + copyedFileCurrentSize + "\n"
								+ Main.bundle.getString("doYouWantToKeepTheFile") + "",
						Main.bundle.getString("yes"), Main.bundle.getString("no"), Main.bundle.getString("abort"));

				stage.setScene(new Scene(parent));
				stage.showAndWait();
			} catch (Exception e) {
				e.printStackTrace();
			}

			return answer.get();
		}

	}

}
