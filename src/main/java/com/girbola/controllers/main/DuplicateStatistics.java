package com.girbola.controllers.main;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class DuplicateStatistics {

    private AtomicInteger duplicateCounter;
    private AtomicInteger fileCounter;
    private AtomicLong folderSavedSize;
    private AtomicInteger folderCounter;
    private AtomicBoolean changesMadeInFolderInfo;


    public AtomicInteger getDuplicateCounter() {
        return duplicateCounter;
    }

    public AtomicInteger getFileCounter() {
        return fileCounter;
    }

    public AtomicLong getFolderSavedSize() {
        return folderSavedSize;
    }

    public AtomicInteger getFolderCounter() {
        return folderCounter;
    }

    public AtomicBoolean getChangesMadeInFolderInfo() {
        return changesMadeInFolderInfo;
    }

    public DuplicateStatistics() {
        this.duplicateCounter = new AtomicInteger(0);
        this.fileCounter = new AtomicInteger(0);
        this.folderSavedSize = new AtomicLong(0);
        this.folderCounter = new AtomicInteger(0);
        this.changesMadeInFolderInfo = new AtomicBoolean(false);
    }

}
