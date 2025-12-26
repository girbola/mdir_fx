package com.girbola.controllers.main.tables.model;

import com.girbola.fileinfo.FileInfo;
import javafx.beans.property.*;

import java.util.List;
import java.util.Map;


interface TableValues_inf {

    /**
     * Interface defining observable properties, value getters and mutators for a folder row in the tables.
     * Methods are grouped into:
     * - Properties: JavaFX observable properties for binding
     * - Getters: Plain value accessors
     * - Setters: Mutators for updating the underlying values
     */
    //@formatter:off

    // ============================ Properties (JavaFX observable) ============================
    public IntegerProperty status_property();
    public List<FileInfo> getFileInfoList();
    public Map<String, String> getFileList();
    public SimpleBooleanProperty changed_property();
    public SimpleBooleanProperty connected_property();
    public SimpleBooleanProperty ignored_prop();
    public SimpleDoubleProperty dateDifferenceRatio_prop();

    // Aggregate file counters (all)
    public SimpleIntegerProperty badFiles_prop();
    public SimpleIntegerProperty goodFiles_prop();
    public SimpleIntegerProperty suggested_prop();

    // Aggregate file counters (images)
    public SimpleIntegerProperty badImageFiles_prop();
    public SimpleIntegerProperty goodImageFiles_prop();
    public SimpleIntegerProperty suggestedImageFiles_prop();
    public SimpleIntegerProperty confirmedImageFiles_prop();

    // Aggregate file counters (videos)
    public SimpleIntegerProperty badVideoFiles_prop();
    public SimpleIntegerProperty goodVideoFiles_prop();
    public SimpleIntegerProperty suggestedVideoFiles_prop();
    public SimpleIntegerProperty confirmedVideoFiles_prop();

    // Workflow/status counters
    public SimpleIntegerProperty confirmed_property();
    public SimpleIntegerProperty copied_property();

    // Folder content counters
    public SimpleIntegerProperty folderFiles_prop();
    public SimpleIntegerProperty folderImageFiles_prop();
    public SimpleIntegerProperty folderRawFiles_prop();
    public SimpleIntegerProperty folderVideoFiles_prop();

    // Folder metadata
    public SimpleLongProperty folderSize_prop();
    public SimpleStringProperty folderPath_prop();
    public SimpleStringProperty maxDate_prop();
    public SimpleStringProperty minDate_prop();
    public SimpleStringProperty state_property();
    public SimpleStringProperty tableType_property();

    // ============================ Getters (plain values) ============================
    public String getFolderPath();
    public String getJustFolderName();
    public String getMaxDate();
    public String getMinDate();
    public String getState();
    public String getTableType();
    public String getSourceFolderSerialNumber();

    public boolean getChanged();
    public boolean getIgnored();
    public boolean isConnected();

    public double getDateDifferenceRatio();

    // Aggregates (all)
    public int getBadFiles();
    public int getGoodFiles();
    public int getSuggested();

    // Aggregates (images)
    public int getBadImageFiles();
    public int getGoodImageFiles();
    public int getSuggestedImageFiles();
    public int getConfirmedImageFiles();

    // Aggregates (videos)
    public int getBadVideoFiles();
    public int getGoodVideoFiles();
    public int getSuggestedVideoFiles();
    public int getConfirmedVideoFiles();

    // Workflow/status counters
    public int getConfirmed();
    public int getCopied();
    public int getStatus();

    // Folder content
    public int getFolderFiles();
    public int getFolderImageFiles();
    public int getFolderRawFiles();
    public int getFolderVideoFiles();
    public long getFolderSize();

    // ============================ Setters (mutators) ============================

    public void setBadFiles(int value);

    public void setBadImageFiles(int value);
    public void setGoodImageFiles(int value);
    public void setSuggestedImageFiles(int value);
    public void setConfirmedImageFiles(int value);

    // Video aggregates
    public void setBadVideoFiles(int value);
    public void setGoodVideoFiles(int value);
    public void setSuggestedVideoFiles(int value);
    public void setConfirmedVideoFiles(int value);

    // State/workflow flags and counters
    public void setChanged(boolean changed);
    public void setConfirmed(int value);
    public void setConnected(boolean value);
    public void setCopied(int value);
    public void setDateDifferenceRatio(double value);

    // Data collections
    public void setFileInfoList(List<FileInfo> fileInfo);

    // Folder content counters
    public void setFolderFiles(int value);
    public void setFolderImageFiles(int value);
    public void setFolderRawFiles(int value);
    public void setFolderVideoFiles(int value);

    // Folder metadata
    public void setFolderPath(String value);
    public void setFolderSize(long value);
    public void setGoodFiles(int value);
    public void setIgnored(boolean value);
    public void setJustFolderName(String value);
    public void setMaxDate(String value);
    public void setMinDate(String value);
    public void setState(String value);
    public void setStatus(int value);
    public void setSuggested(int value);
    public void setTableType(String value);
    public void setSourceFolderSerialNumber(String workdirSerialNumber);

}