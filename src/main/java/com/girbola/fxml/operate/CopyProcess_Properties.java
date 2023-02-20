/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.fxml.operate;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

public class CopyProcess_Properties extends CopyBasic_Values {

	private final DecimalFormat df = new DecimalFormat("#.##");

	private SimpleIntegerProperty copied;

	private SimpleStringProperty copyFrom;
	private SimpleDoubleProperty copyProgress;
	private SimpleDoubleProperty totalProgress;

	private SimpleStringProperty copyTo;
	private SimpleIntegerProperty duplicated;
	private SimpleIntegerProperty filesLeft;

	private SimpleIntegerProperty progressed;

	private SimpleIntegerProperty renamed;
	private SimpleIntegerProperty skipped;
	private SimpleStringProperty timeElapsed;

	private SimpleStringProperty timeLeft;
	private SimpleStringProperty totalFiles;
	private SimpleLongProperty totalFilesSize_property;
	private SimpleLongProperty fileCopyProgress_property;
	private SimpleStringProperty transferRate_property;

	public CopyProcess_Properties() {
		copied = new SimpleIntegerProperty(0);
		copyFrom = new SimpleStringProperty("");
		copyProgress = new SimpleDoubleProperty(0);

		copyTo = new SimpleStringProperty("");
		duplicated = new SimpleIntegerProperty(0);
		filesLeft = new SimpleIntegerProperty(0);
		progressed = new SimpleIntegerProperty(0);
		renamed = new SimpleIntegerProperty(0);
		skipped = new SimpleIntegerProperty(0);
		timeElapsed = new SimpleStringProperty("");
		timeLeft = new SimpleStringProperty("");
		totalFiles = new SimpleStringProperty("");
		totalFilesSize_property = new SimpleLongProperty(0);
		fileCopyProgress_property = new SimpleLongProperty(0);
		transferRate_property = new SimpleStringProperty("");
		totalProgress = new SimpleDoubleProperty(0);

	}

	private AtomicLong timeElapsed_counter = new AtomicLong(0);

	private DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

	public synchronized void update() {
		// Messages.sprintf("Updating: " + getCopyFrom_tmp());
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
//				Messages.sprintf("Updating copyprocess");
				setCopyFrom(getCopyFrom_tmp());
				setCopyTo(getCopyTo_tmp());

				setTimeLeft("" + getTimeLeft_tmp() + 1000);
				timeElapsed_counter.set(timeElapsed_counter.get() + 1000);
				setTimeElapsed("" + timeFormat.format(timeElapsed_counter.get()));

				setCopied(getCopied_tmp());
				setRenamed(getRenamed_tmp());
				setDuplicated(getDuplicated_tmp());
				setTotalFiles("" + getTotalFiles_tmp());
				setFileCopyProgress(
						Math.round((double) getFileCopyProgress_tmp() / (double) getFilesCopyProgress_MAX_tmp()));
				addToLastSecondFileSize_list(getFileCurrentCopied_Size_tmp());
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

	public synchronized void reset() {
		copyFrom.set("");
		copyTo.set("");
		copyProgress.set(0);
		duplicated.set(0);
		filesLeft.set(0);
		progressed.set(0);
		skipped.set(0);
		timeElapsed.set("");
		timeLeft.set("");
		// timeLeftProperty.set(0);
		totalFiles.set("0");

		totalFilesSize_property.set(0);
		fileCopyProgress_property.set(0);
		transferRate_property.set("0");
	}

	public synchronized SimpleDoubleProperty totalProgress_property() {
		return totalProgress;
	}

	public synchronized void setTotalProgress(double totalProgress) {
		this.totalProgress.set(totalProgress);
	}

	public synchronized double getTotalProgress() {
		return this.totalProgress.get();
	}

	/*
	 * private int totalFiles_tmp; to String private long fileCopyProgress_tmp to
	 * double;
	 */
	public synchronized SimpleIntegerProperty copied_property() {
		return this.copied;
	}

	public synchronized SimpleStringProperty copyFrom_property() {
		return this.copyFrom;
	}

	public synchronized SimpleDoubleProperty copyProgress_property() {
		return this.copyProgress;
	}

	public synchronized SimpleStringProperty copyTo_property() {
		return this.copyTo;
	}

	public synchronized SimpleIntegerProperty duplicated_property() {
		return this.duplicated;
	}

	public synchronized SimpleIntegerProperty filesLeft_property() {
		return this.filesLeft;
	}

	public synchronized int getCopied() {
		return this.copied.get();
	}

	public synchronized String getCopyFrom() {
		return this.copyFrom.get();
	}

	public synchronized double getCopyProgress() {
		return copyProgress.get();
	}

	public synchronized String getCopyTo() {
		return this.copyTo.get();
	}

	public synchronized int getDuplicated() {
		return this.duplicated.get();
	}

	public synchronized int getFilesLeft() {
		return this.filesLeft.get();
	}

	public synchronized int getProgressed() {
		return this.progressed.get();
	}

	public synchronized int getRenamed() {
		return this.renamed.get();
	}

	public synchronized int getSkipped() {
		return this.skipped.get();
	}

	public synchronized String getTimeElapsed() {
		return timeElapsed.get();
	}
	//
	// public synchronized String getTimeLeft() {
	// return this.timeLeft.get();
	// }

	public synchronized String getTotalFiles() {
		return this.totalFiles.get();
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
		return this.progressed;
	}

	public synchronized SimpleIntegerProperty renamed_property() {
		return this.renamed;
	}

	public synchronized void setCopied(int value) {
		this.copied.set(value);
	}

	public synchronized void setCopyFrom(String value) {
		this.copyFrom.set(value);
	}

	public synchronized void setCopyProgress(double value) {
		this.copyProgress.set(value);
	}

	public synchronized void setCopyTo(String value) {
		this.copyTo.set(value);
	}

	public synchronized void setDuplicated(int value) {
//		Messages.sprintf("duplicated: " + value);
		this.duplicated.set(value);
	}

	public synchronized void setFilesLeft(int value) {
		this.filesLeft.set(value);
	}

	public synchronized void setProgressed(int value) {
		this.progressed.set(value);
	}

	public synchronized void setRenamed(int value) {
		this.renamed.set(value);
	}

	public synchronized void setSkipped(int value) {
		this.skipped.set(value);
	}

	public synchronized void setTimeElapsed(String value) {
		this.timeElapsed.set(value);
	}

	public synchronized void setTimeLeft(String value) {
		this.timeLeft.set(value);
	}

	public synchronized void setTotalFiles(String value) {
		this.totalFiles.set(value);
	}

	public synchronized void setFileCopyProgress(long value) {
		this.fileCopyProgress_property.set(value);
	}

	public synchronized void setTransferRate(String value) {
		this.transferRate_property.set(value);
	}

	public synchronized SimpleIntegerProperty skipped_property() {
		return this.skipped;
	}

	public synchronized SimpleStringProperty timeElapsed_property() {
		return this.timeElapsed;
	}

	public synchronized SimpleStringProperty timeLeft_property() {
		return this.timeLeft;
	}

	public synchronized SimpleStringProperty totalFiles_property() {
		return this.totalFiles;
	}

	public synchronized SimpleLongProperty totalFilesSize_property() {
		return this.totalFilesSize_property;
	}

	public synchronized SimpleLongProperty totalProcessedFileSizes_property() {
		return this.fileCopyProgress_property;
	}

}
