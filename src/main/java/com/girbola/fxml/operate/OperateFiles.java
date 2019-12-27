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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.girbola.Main;
import com.girbola.controllers.main.Model_main;
import com.girbola.controllers.main.Model_operate;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.sql.SqliteConnection;

import common.utils.FileUtils;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.WindowEvent;

public class OperateFiles extends Task<Boolean> {

	private final String ERROR = OperateFiles.class.getSimpleName();
	private boolean close;
	private Model_operate model_operate = new Model_operate();
	private List<FileInfo> list = new ArrayList<>();
	private Model_main model_main;
	private String scene_NameType;

	public OperateFiles(List<FileInfo> list, boolean close, Model_main aModel_main, String scene_NameType) {
		Messages.sprintf("OperateFiles started LIST");
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
				Main.getMain_stage().setOnCloseRequest(model_Main.exitProgram);
				Main.scene_Switcher.getWindow().setScene(Main.scene_Switcher.getScene_main());

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
				return null;
			}
			if (isCancelled()) {
				Main.setProcessCancelled(true);
				model_operate.stopTimeLine();
				return null;
			}
			if (Main.getProcessCancelled()) {
				cancel();
				model_operate.stopTimeLine();
				return null;
			}
			model_operate.getTimeline().play();

			boolean copy = false;
			for (FileInfo fileInfo : list) {
				copy = false;
				if (isCancelled()) {
					Main.setProcessCancelled(true);
					break;
				}
				if (Main.getProcessCancelled()) {
					cancel();
					break;
				}
				
				source = Paths.get(fileInfo.getOrgPath());
				// dest = DestinationResolver.getDestinationFileName(fileInfo);

				dest = Paths.get(fileInfo.getWorkDir() + fileInfo.getDestination_Path());
				
				Messages.sprintf("source is: " + source + " dest: " + dest);
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						model_operate.getCopyProcess_values().setCopyFrom(source.toString());
						model_operate.getCopyProcess_values().setCopyTo(dest.toString());
					}
				});
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
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									model_operate.getCopyProcess_values().increaseDuplicated_tmp();
									Messages.sprintf("Increader dups is now: "
											+ model_operate.getCopyProcess_values().getDuplicated_tmp());
								}
							});
							copy = false;
							STATE = Copy_State.DUPLICATE.getType();
						} else {
							if (dest_test != dest) {
								dest = dest_test;
								copy = true;
								STATE = Copy_State.RENAME.getType();
								String newDest = FileUtils.parseWorkDir(dest.toString(), fileInfo.getWorkDir());
								fileInfo.setDestination_Path(newDest);
								Messages.sprintf("Renamed: " + fileInfo.getDestination_Path());
								Platform.runLater(new Runnable() {
									@Override
									public void run() {
										model_operate.getCopyProcess_values().increaseRenamed_tmp();
									}
								});
							} else if (dest_test == dest) {
								copy = false;
								STATE = Copy_State.DUPLICATE.getType();
								Platform.runLater(new Runnable() {
									@Override
									public void run() {
										model_operate.getCopyProcess_values().increaseDuplicated_tmp();
									}
								});
							}
						}
					} catch (IOException ex) {
						ex.printStackTrace();
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
						Messages.sprintf("Files.exists dest: " + dest.getParent());
					}
					// long fileSize = source.toFile().length();
					try {
						//
						InputStream from = new FileInputStream(source.toString());
						OutputStream to = new FileOutputStream(dest.toFile() + ".tmp");
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								model_operate.getCopyProcess_values().setCopyFrom_tmp(source.toString());
								model_operate.getCopyProcess_values().setCopyTo_tmp(dest.toString());
								model_operate.getCopyProcess_values().setCopyProgress(0);
								model_operate.getCopyProcess_values()
										.setFilesCopyProgress_MAX_tmp(source.toFile().length());
							}
						});
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
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									model_operate.getCopyProcess_values().increaseLastSecondFileSize_tmp(byteRead);
								}
							});
						}
						from.close();
						to.close();

						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								model_operate.getCopyProcess_values().setLastSecondFileSize_tmp(0);
								model_operate.getCopyProcess_values().decreaseFilesLeft_tmp();
							}
						});
						if (nread != model_operate.getCopyProcess_values().getFilesCopyProgress_MAX_tmp()) {
							sprintf("files were NOT fully copied. currentFileByte = " + nread
									+ " filecopyprogress_max = "
									+ model_operate.getCopyProcess_values().getFilesCopyProgress_MAX_tmp());
							cancel();
							Main.setProcessCancelled(true);
							return null;
						} else {
							try {
								Messages.sprintf("Renaming file .tmp back to org extension: " + dest.toString() + ".tmp" + " to dest: " + dest);
								Files.move(Paths.get(dest.toString() + ".tmp"), dest);
								String newName = FileUtils.parseWorkDir(dest.toString(), fileInfo.getWorkDir());
								fileInfo.setDestination_Path(newName);
								fileInfo.setCopied(true);
								boolean added = model_main.getWorkDir_Handler().add(fileInfo);
								if (added) {
									Messages.sprintf("FileInfo added to destination succesfully");
									model_main.getWorkDir_Handler().add(fileInfo);
								} else {
									Messages.sprintf("FileInfo were not added somehow...: " + fileInfo);
								}
							} catch (Exception ex) {
								Messages.sprintfError(ex.getMessage());
							}
						}
						if (STATE.equals(Copy_State.COPY.getType())) {
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									model_operate.getCopyProcess_values().increaseCopied_tmp();
								}
							});
							fileInfo.setCopied(true);
						} else if (STATE.equals(Copy_State.RENAME.getType())) {
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									model_operate.getCopyProcess_values().increaseRenamed_tmp();
								}
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
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						model_operate.getCopyProcess_values().setFilesLeft(counter.decrementAndGet());
					}
				});
			}
			model_operate.getCopyProcess_values().update();
			return null;
		}

		private void cleanCancelledFile(InputStream from, OutputStream to) {
			sprintf("cancelled");
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

//			model_operate.doneButton(scene_NameType, close);
			Messages.errorSmth(ERROR, "", null, Misc.getLineNumber(), true);
		}

		@Override
		protected void cancelled() {
			Messages.sprintf("OperateFiles COPY were cancelled");
			model_operate.stopTimeLine();
			model_operate.doneButton(scene_NameType, close);

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

					if (saveWorkDirList) {
						Messages.sprintf("saveWorkDirList DONE!!!");
					} else {
						Messages.sprintf("saveWorkDirList FAILED");
					}
					return null;
				}
			};
			//@formatter:off
			saveWorkDirToDatabase.setOnSucceeded((eventti)-> {
				Messages.sprintf("saveWorkDirListToDatabase finished success!");
				try {
					Messages.sprintf("Insert worked!");
					Connection connection = SqliteConnection.connector(Paths.get(Main.conf.getWorkDir()), Main.conf.getFileInfo_db_fileName());
					connection.setAutoCommit(false);
					boolean clean = model_main.getWorkDir_Handler().cleanDatabase(connection);
					if(clean) {
						Messages.sprintf("clean is done succeeded");
					} else {
						Messages.sprintfError("clean not worked");
					}
				} catch (Exception e) {
				e.printStackTrace();
				}
			});
			saveWorkDirToDatabase.setOnCancelled((eventti)-> {Messages.sprintfError("saveWorkDirListToDatabase finished success!");});
			saveWorkDirToDatabase.setOnFailed((eventti) -> {Messages.sprintf("Task failed!");});
			
			Thread savingWorkDirContent = new Thread(saveWorkDirToDatabase, "Saving workDir content");
			savingWorkDirContent.setDaemon(true);
			savingWorkDirContent.start();

			sprintf("OperateFiles succeeded");
		}

		@Override
		protected void running() {
			if(!isRunning()) {
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
		Platform.runLater(new Runnable() {

			@Override
			public void run() {

				model_operate.getStart_btn().setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						Task<Integer> copy = new Copy();
						copy.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
							@Override
							public void handle(WorkerStateEvent event) {
								Messages.sprintf("copy succeeded");
							}
						});
						copy.setOnFailed(new EventHandler<WorkerStateEvent>() {

							@Override
							public void handle(WorkerStateEvent event) {
								Messages.sprintf("copy failed");
							}
						});
						copy.setOnCancelled(new EventHandler<WorkerStateEvent>() {

							@Override
							public void handle(WorkerStateEvent event) {
								model_operate.getCancel_btn().setText(Main.bundle.getString("close"));
								model_operate.doneButton(scene_NameType, close);
								Messages.sprintf("copy cancelled");
							}
						});
						Thread copy_thread = new Thread(copy, "Copy Thread");
						copy_thread.start();
					}
				});
				model_operate.getCancel_btn().setOnAction(new EventHandler<ActionEvent>() {

					@Override
					public void handle(ActionEvent event) {
						Main.setProcessCancelled(true);
						Messages.sprintf(
								"Current file cancelled is: " + model_operate.getCopyProcess_values().getCopyTo_property());
						model_operate.stopTimeLine();
						Main.setProcessCancelled(true);
					}
				});

			}
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

}
