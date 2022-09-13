/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.fxml.operate;

import static com.girbola.messages.Messages.sprintf;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.girbola.Main;
import com.girbola.controllers.main.Model_operate;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;

import common.utils.FileUtils;
import javafx.application.Platform;
import javafx.concurrent.Task;

/**
 *
 * @author Marko Lokka
 */
public class CopyFiles {

	private final String ERROR = CopyFiles.class.getSimpleName();

	private Path source;
	private Path dest;
	private long currentFileByte;
	private Model_operate model_operate;
	private FileInfo fileInfo;
	private boolean copy = false;
	private String STATE = "";
	private int byteRead;
	private long currentSize;
	private Task<Integer> task;
	private boolean duplicated;

	public CopyFiles(Task<Integer> aTask, Path aSource, Path aDest, FileInfo aFileInfo, Model_operate aModel_operate) {
		this.task = aTask;
		this.source = aSource;
		this.dest = aDest;
		this.fileInfo = aFileInfo;
		this.model_operate = aModel_operate;
	}

	public void start() throws Exception {
		if (!dest.toString().contains(Main.conf.getWorkDir())) {
			Main.setProcessCancelled(true);
			Messages.sprintfError("FATAL ERROR! Something went terribly wrong during copy process method at class: "
					+ ERROR + " line: " + Misc.getLineNumber());

			task.cancel();

			Platform.exit();
			System.exit(-1);
		}
		if (task.isCancelled()) {
			Main.setProcessCancelled(true);
			task.cancel();
		}
		if (Main.getProcessCancelled()) {
			task.cancel();
		}
		Path destFolder = createDirectory(dest.getParent());

		if (!Files.exists(destFolder)) {
			sprintf("destfolder didn't exists!");
			task.cancel();
			Main.setProcessCancelled(true);
		}
		if (Files.exists(dest)) {
			if (Files.size(source) != Files.size(dest)) {
				sprintf("---> RENAMING - File size different: " + source + " dest: " + dest);
				dest = FileUtils.renameFile(source, dest);

				if (dest != null) {
					copy = true;
					STATE = Copy_State.RENAME.getType();
					sprintf("2STATE IS: " + STATE);
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							model_operate.getCopyProcess_values().setCopyTo(dest.toString());
						}
					});

				} else {
					copy = false;
					STATE = Copy_State.DUPLICATE.getType();
					sprintf("4STATE IS DUPLICATED? : " + STATE);
					setDuplicated(true);

					fileInfo.setCopied(true);
					if (!fileInfo.isCopied()) {
						fileInfo.setCopied(true);
					}
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							model_operate.getCopyProcess_values().increaseDuplicated_tmp();
						}
					});
				}
			} else {
				copy = false;
				STATE = Copy_State.DUPLICATE.getType();
				sprintf("5STATE IS DUPLICATED? : " + STATE);
				if (!fileInfo.isCopied()) {
					fileInfo.setCopied(true);
				}
				model_operate.getCopyProcess_values().increaseDuplicated_tmp();
				Messages.sprintf("duplicated are: " + (model_operate.getCopyProcess_values().getDuplicated_tmp()));
			}
		} else {
			copy = true;
			STATE = Copy_State.COPY.getType();
			sprintf("3STATE IS: " + STATE);

		}
		sprintf("STATE IS NOW: " + STATE);
		if (dest != null && copy) {
//			long fileSize = source.toFile().length();
			try {
				//
				InputStream from = new FileInputStream(source.toString());
				OutputStream to = new FileOutputStream(dest.toFile() + ".tmp");
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						model_operate.getCopyProcess_values().setCopyFrom_tmp(source.toString());
						model_operate.getCopyProcess_values().setCopyTo_tmp(dest.toString());
//						model_operate.getCopyProcess_values().setCopyProgress_lbl(0);
						model_operate.getCopyProcess_values().setFilesCopyProgress_MAX_tmp(source.toFile().length());
					}
				});

				long nread = 0L;
				byteRead = 0;
				byte[] buf = new byte[1024];
				while ((byteRead = from.read(buf)) > 0) {
					if (Main.getProcessCancelled()) {
						cleanCancelledFile(from, to);
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

				if (currentFileByte != model_operate.getCopyProcess_values().getFilesCopyProgress_MAX_tmp()) {
					Messages.sprintf("=====CopyFiles files were not copyed and it was cancelled.\nCurrentByte : "
							+ currentFileByte + " to compare: "
							+ model_operate.getCopyProcess_values().getFilesCopyProgress_MAX_tmp());
					Files.delete(dest);
					task.cancel();
					Main.setProcessCancelled(true);
				} else {
					sprintf("files were fully copied");
					try {
						Files.move(Paths.get(dest.toString() + ".tmp"), dest);
						// fileInfo.setDestinationPath(dest.toString());
						Platform.runLater(new Runnable() {

							@Override
							public void run() {
								model_operate.getCopyProcess_values().setLastSecondFileSize_tmp(0);
								model_operate.getCopyProcess_values().decreaseFilesLeft_tmp();
							}
						});
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
				Messages.sprintfError("error: " + ex.getMessage());
				byteRead = 0;
				currentFileByte = 0;
				Main.setProcessCancelled(true);
				task.cancel();
				Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
			}
		}
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
			task.cancel();
			Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
		}
		task.cancel();
	}

	private Path createDirectory(Path path) {
		if (!Files.exists(path)) {
			sprintf("Creating folder: " + path);
			try {
				Files.createDirectories(path);
			} catch (IOException ex) {
				Main.setProcessCancelled(true);
				Messages.errorSmth(ERROR, "Couldn't create directory: " + path, ex, Misc.getLineNumber(), true);
				return path;
			}
		}
		return path;
	}

	public void setDuplicated(boolean value) {
		this.duplicated = value;
	}

	public boolean isDuplicated() {
		return this.duplicated;
	}
}
