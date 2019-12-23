/*
 @(#)Copyright:  Copyright (c) 2012-2019 All right reserved.
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.fxml.operate;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalTime;

import com.girbola.messages.Messages;

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class CopyProcess_Properties extends Basic_Values {

	private final DecimalFormat df = new DecimalFormat("#.##");

	private SimpleIntegerProperty copied_property;

	private SimpleStringProperty copyFrom_property;
	private SimpleDoubleProperty copyProgress_property;
	private SimpleDoubleProperty totalProgress_property;

	private SimpleStringProperty copyTo_property;
	private SimpleIntegerProperty duplicated_property;
	private SimpleIntegerProperty filesLeft_property;

	private SimpleIntegerProperty progressed_property;

	private SimpleIntegerProperty renamed_property;
	private SimpleIntegerProperty skipped_property;
	private SimpleStringProperty timeElapsed_property;

	private SimpleStringProperty timeLeft_property;
	private SimpleStringProperty totalFiles_property;
	private SimpleDoubleProperty totalFilesSize_property;
	private SimpleDoubleProperty fileCopyProgress_property;
	private SimpleStringProperty transferRate_property;

	public synchronized SimpleDoubleProperty totalProgress_property() {
		return totalProgress_property;
	}

	public synchronized void setTotalProgress(double totalProgress) {
		this.totalProgress_property.set(totalProgress);
	}

	public synchronized double getTotalProgress_property() {
		return this.totalProgress_property.get();
	}
	public synchronized double getTotalProgress() {
		return this.totalProgress_property.get();
	}
	public CopyProcess_Properties() {
		copied_property = new SimpleIntegerProperty(0);
		copyFrom_property = new SimpleStringProperty("");
		copyProgress_property = new SimpleDoubleProperty(0);

		copyTo_property = new SimpleStringProperty("");
		duplicated_property = new SimpleIntegerProperty(0);
		filesLeft_property = new SimpleIntegerProperty(0);
		progressed_property = new SimpleIntegerProperty(0);
		renamed_property = new SimpleIntegerProperty(0);
		skipped_property = new SimpleIntegerProperty(0);
		timeElapsed_property = new SimpleStringProperty("");
		timeLeft_property = new SimpleStringProperty("");
		totalFiles_property = new SimpleStringProperty("");
		totalFilesSize_property = new SimpleDoubleProperty(0);
		fileCopyProgress_property = new SimpleDoubleProperty(0);
		transferRate_property = new SimpleStringProperty("");
		totalProgress_property = new SimpleDoubleProperty(0);

	}

	/*
	 * private int totalFiles_tmp; to String private long fileCopyProgress_tmp to
	 * double;
	 */
	public synchronized SimpleIntegerProperty copied_property() {
		return this.copied_property;
	}

	public synchronized SimpleStringProperty copyFrom_property() {
		return this.copyFrom_property;
	}

	public synchronized SimpleDoubleProperty copyProgress_property() {
		return this.copyProgress_property;
	}

	public synchronized SimpleStringProperty copyTo_property() {
		return this.copyTo_property;
	}

	public synchronized SimpleIntegerProperty duplicated_property() {
		return this.duplicated_property;
	}

	public synchronized SimpleIntegerProperty filesLeft_property() {
		return this.filesLeft_property;
	}

	public synchronized int getCopied_property() {
		return this.copied_property.get();
	}

	public synchronized String getCopyFrom_property() {
		return this.copyFrom_property.get();
	}

	public synchronized double getCopyProgress_property() {
		return copyProgress_property.get();
	}

	public synchronized String getCopyTo_property() {
		return this.copyTo_property.get();
	}

	public synchronized int getDuplicated_property() {
		return this.duplicated_property.get();
	}

	public synchronized int getFilesLeft_property() {
		return this.filesLeft_property.get();
	}

	public synchronized int getProgressed_property() {
		return this.progressed_property.get();
	}

	public synchronized int getRenamed_property() {
		return this.renamed_property.get();
	}

	public synchronized int getSkipped_property() {
		return this.skipped_property.get();
	}

	public synchronized String getTimeElapsed_property() {
		return timeElapsed_property.get();
	}
	//
	// public synchronized String getTimeLeft() {
	// return this.timeLeft.get();
	// }

	public synchronized String getTotalFiles_property() {
		return this.totalFiles_property.get();
	}

	public synchronized double getTotalFilesSize_property() {
		return this.totalFilesSize_property.get();
	}

	public synchronized double getFileCopyProgress_property() {
		return this.fileCopyProgress_property.get();
	}

	public synchronized String getTransferRate_property() {
		return this.transferRate_property.get();
	}

	public synchronized SimpleStringProperty transferRate_property() {
		return this.transferRate_property;
	}

	public synchronized SimpleIntegerProperty progressed_property() {
		return this.progressed_property;
	}

	public synchronized SimpleIntegerProperty renamed_property() {
		return this.renamed_property;
	}

	public synchronized void reset() {
		copyFrom_property.set("");
		copyTo_property.set("");
		copyProgress_property.set(0);
		duplicated_property.set(0);
		filesLeft_property.set(0);
		progressed_property.set(0);
		skipped_property.set(0);
		timeElapsed_property.set("");
		timeLeft_property.set("");
		// timeLeftProperty.set(0);
		totalFiles_property.set("0");

		totalFilesSize_property.set(0);
		fileCopyProgress_property.set(0);
		transferRate_property.set("0");
	}

	public synchronized void setCopied(int value) {
		this.copied_property.set(value);
	}

	public synchronized void setCopyFrom(String value) {
		this.copyFrom_property.set(value);
	}

	public synchronized void setCopyProgress(double value) {
		this.copyProgress_property.set(value);
	}

	public synchronized void setCopyTo(String value) {
		this.copyTo_property.set(value);
	}

	public synchronized void setDuplicated(int value) {
		Messages.sprintf("duplicated: " + value);
		this.duplicated_property.set(value);
	}

	public synchronized void setFilesLeft(int value) {
		this.filesLeft_property.set(value);
	}

	public synchronized void setProgressed(int value) {
		this.progressed_property.set(value);
	}

	public synchronized void setRenamed(int value) {
		this.renamed_property.set(value);
	}

	public synchronized void setSkipped(int value) {
		this.skipped_property.set(value);
	}

	public synchronized void setTimeElapsed(String value) {
		this.timeElapsed_property.set(value);
	}

	public synchronized void setTimeLeft(String value) {
		this.timeLeft_property.set(value);
	}

	public synchronized void setTotalFiles(String value) {
		this.totalFiles_property.set(value);
	}

	public synchronized void setFileCopyProgress(double value) {
		this.fileCopyProgress_property.set(value);
	}

	public synchronized void setTransferRate(String value) {
		this.transferRate_property.set(value);
	}

	public synchronized SimpleIntegerProperty skipped_property() {
		return this.skipped_property;
	}

	public synchronized SimpleStringProperty timeElapsed_property() {
		return this.timeElapsed_property;
	}

	public synchronized SimpleStringProperty timeLeft_property() {
		return this.timeLeft_property;
	}

	public synchronized SimpleStringProperty totalFiles_property() {
		return this.totalFiles_property;
	}

	public synchronized SimpleDoubleProperty totalFilesSize_property() {
		return this.totalFilesSize_property;
	}

	public synchronized SimpleDoubleProperty totalProcessedFileSizes_property() {
		return this.fileCopyProgress_property;
	}

	public synchronized void update() {
		// Messages.sprintf("Updating: " + getCopyFrom_tmp());
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				 Messages.sprintf("Updating copyprocess");
				setCopyFrom(getCopyFrom_tmp());
				setCopyTo(getCopyTo_tmp());

				setTimeLeft("" + getTimeLeft_tmp() + 1000);
				setTimeElapsed("" + getTimeElapsed_tmp() + 1000);

				setCopied(getCopied_tmp());
				setRenamed(getRenamed_tmp());
				setDuplicated(getDuplicated_tmp() + 1);
				Messages.sprintf("getdup 1SECs: "+getDuplicated_tmp());
				Messages.sprintf("getDuplicated_tmp() + 1" + getDuplicated_tmp());
				setTotalFiles("" + getTotalFiles_tmp());
				setFileCopyProgress(Math.floor(getFileCopyProgress_tmp() / getFilesCopyProgress_MAX_tmp()));
				addToLastSecondFileSize_list(getLastSecondFileSize_tmp());
				setTransferRate(df.format(getAverageTransferRate() / 1024000) + " MB/sec");
				setLastSecondFileSize_tmp(0);
				if (getFileCopyProgress_tmp() != 0 && getFilesCopyProgress_MAX_tmp() != 0) {
					setCopyProgress(
							(int) Math.floor((getFileCopyProgress_tmp() / getFilesCopyProgress_MAX_tmp()) * 100));
				}
				if (getFilesLeft_tmp() != 0 && getTotalFiles_tmp() != 0) {
					setTotalProgress((int) Math.floor((getFilesLeft_tmp() / getTotalFiles_tmp()) * 100));
				}
				if (getStartTime() != null) {
					setTimeLeft(LocalTime.ofSecondOfDay(Duration.between(getStartTime(), getEndTime()).getSeconds())
							.toString());
				}

			}
		});

	}

}
