package com.girbola.controllers.main.tables;


import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class DuplicateStatistics {
    private AtomicBoolean changesMadeInFolderInfo = new AtomicBoolean(false);
    private AtomicInteger duplicateCounter = new AtomicInteger(0);
    private AtomicInteger fileCounter = new AtomicInteger(0);
    private AtomicInteger folderCounter = new AtomicInteger(0);
    private AtomicLong folderSavedSize = new AtomicLong(0);


    public AtomicInteger getDuplicateCounter() {
        return duplicateCounter;
    }

    public void setDuplicateCounter(AtomicInteger duplicateCounter) {
        this.duplicateCounter = duplicateCounter;
    }

    public AtomicInteger getFileCounter() {
        return fileCounter;
    }

    public void setFileCounter(AtomicInteger fileCounter) {
        this.fileCounter = fileCounter;
    }

    public AtomicLong getFolderSavedSize() {
        return folderSavedSize;
    }

    public void setFolderSavedSize(AtomicLong folderSavedSize) {
        this.folderSavedSize = folderSavedSize;
    }

    public AtomicInteger getFolderCounter() {
        return folderCounter;
    }

    public void setFolderCounter(AtomicInteger folderCounter) {
        this.folderCounter = folderCounter;
    }

    public AtomicBoolean getChangesMadeInFolderInfo() {
        return this.changesMadeInFolderInfo;
    }
}
