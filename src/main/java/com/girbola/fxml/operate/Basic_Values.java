package com.girbola.fxml.operate;

import java.time.LocalTime;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

import com.girbola.messages.Messages;

public class Basic_Values {

    private String copyFrom_tmp;
    private String copyTo_tmp;

    private AtomicInteger copied_tmp = new AtomicInteger(0);
    private AtomicInteger renamed_tmp = new AtomicInteger(0);
    private AtomicInteger duplicated_tmp = new AtomicInteger(0);

    private AtomicInteger totalFiles_tmp = new AtomicInteger(0);
    private AtomicInteger filesLeft_tmp = new AtomicInteger(0);
    private String transferRate_tmp;
    private long timeLeft_tmp;
    private long timeElapsed_tmp;

    private double fileCopyProgress_tmp;

    private long filesCopyProgress_MAX_tmp;
    private long lastSecondFileSize_tmp = 0;

    private Deque<Long> transferRate_list = new LinkedList<>();

    private LocalTime startTime = LocalTime.now();
    private LocalTime endTime = LocalTime.now();

    /**
     * getStartTime
     * 
     * @return
     */
	public LocalTime getStartTime() {
	return startTime;
    }

    /**
     * getEndTime
     * 
     * @return LocalTime
     */
    public LocalTime getEndTime() {
	return endTime;
    }

    public void setStartTime(LocalTime startTime) {
	this.startTime = startTime;
    }

    public void setEndTime_lbl(LocalTime endTime) {
	this.endTime = endTime;
    }

    public synchronized double getAverageTransferRate() {
	long count = 0;
	if (transferRate_list.isEmpty()) {
	    return 0;
	}
	for (Long size : transferRate_list) {
	    count += size;
	}
	return Math.floor((double) count / (double) transferRate_list.size());
    }

    public synchronized void increaseLastSecondFileSize_tmp(long value) {
	this.lastSecondFileSize_tmp += value;
	long sum = (this.lastSecondFileSize_tmp / filesCopyProgress_MAX_tmp);
	setFileCopyProgress_tmp((long) Math.floor(sum));
    }

    public synchronized void addToLastSecondFileSize_list(long value) {
	if (transferRate_list.size() > 10) {
	    transferRate_list.removeLast();
	    transferRate_list.addFirst(value);
	} else {
	    if (value != 0) {
		Messages.sprintf("addtolastsecondfilesizelis: " + value);
		transferRate_list.add(value);
	    }
	}
    }

    public synchronized long getLastSecondFileSize_tmp() {
	return lastSecondFileSize_tmp;
    }

    public synchronized Deque<Long> getTransferRate_list() {
	return transferRate_list;
    }

    public synchronized void setLastSecondFileSize_tmp(long lastSecondFileSize) {
	this.lastSecondFileSize_tmp = lastSecondFileSize;
    }

    public synchronized int getTotalFiles_tmp() {
	return this.totalFiles_tmp.get();
    }

    public synchronized void setTotalFiles_tmp(int totalFiles_tmp) {
	this.totalFiles_tmp.set(totalFiles_tmp);
    }

    public synchronized long getFilesCopyProgress_MAX_tmp() {
	return filesCopyProgress_MAX_tmp;
    }

    public synchronized void setFilesCopyProgress_MAX_tmp(long processMaxSize_tmp) {
	this.filesCopyProgress_MAX_tmp = processMaxSize_tmp;
    }

    public synchronized void decreaseFilesLeft_tmp() {
	Messages.sprintf("decreaseFilesLeft_tmp: " + getFilesLeft_tmp());
	setFilesLeft_tmp(getFilesLeft_tmp() - 1);
    }

    public synchronized int getCopied_tmp() {
	return this.copied_tmp.get();
    }

    public synchronized String getCopyFrom_tmp() {
	return copyFrom_tmp;
    }

    public synchronized String getCopyTo_tmp() {
	return copyTo_tmp;
    }

    public synchronized int getDuplicated_tmp() {
	return this.duplicated_tmp.get();
    }

    public synchronized int getFilesLeft_tmp() {
	return this.filesLeft_tmp.get();
    }

    public synchronized int getRenamed_tmp() {
	return this.renamed_tmp.get();
    }

    public synchronized long getTimeElapsed_tmp() {
	return timeElapsed_tmp;
    }

    public synchronized long getTimeLeft_tmp() {
	return timeLeft_tmp;
    }

    public synchronized double getFileCopyProgress_tmp() {
	return fileCopyProgress_tmp;
    }

    public synchronized String getTransferRate_tmp() {
	return transferRate_tmp;
    }

    public synchronized void increaseCopied_tmp() {
	this.copied_tmp.incrementAndGet();
    }

    public synchronized void increaseDuplicated_tmp() {
	this.duplicated_tmp.incrementAndGet();
	Messages.sprintf("dup_tmp= " + this.duplicated_tmp.get());
//		setDuplicated_tmp(getDuplicated_tmp() + 1);
    }

    public synchronized void increaseFileCopyProgress_tmp(long value) {
	setFileCopyProgress_tmp(value);
    }

    public synchronized void increaseRenamed_tmp() {
	this.renamed_tmp.incrementAndGet();
    }

    public synchronized void setCopied_tmp(int copied_tmp) {
	this.copied_tmp.set(copied_tmp);
    }

    public synchronized void setCopyFrom_tmp(String copyFrom_tmp) {
	this.copyFrom_tmp = copyFrom_tmp;
    }

    public synchronized void setCopyTo_tmp(String copyTo_tmp) {
	this.copyTo_tmp = copyTo_tmp;
    }

    public synchronized void setDuplicated_tmp(int duplicated_tmp) {
	this.duplicated_tmp.set(duplicated_tmp);
    }

    public synchronized void setFilesLeft_tmp(int filesLeft_tmp) {
	this.filesLeft_tmp.set(filesLeft_tmp);
    }

    public synchronized void setRenamed_tmp(int renamed_tmp) {
	this.renamed_tmp.set(renamed_tmp);
    }

    public synchronized void setTimeElapsed_tmp(long timeElapsed_tmp) {
	this.timeElapsed_tmp = timeElapsed_tmp;
    }

    public synchronized void setTimeLeft_tmp(long timeLeft_tmp) {
	this.timeLeft_tmp = timeLeft_tmp;
    }

    public synchronized void setFileCopyProgress_tmp(long value) {
	this.fileCopyProgress_tmp = value;
    }

    public synchronized void setTransferRate_tmp(String value) {
	this.transferRate_tmp = value;
    }

}
