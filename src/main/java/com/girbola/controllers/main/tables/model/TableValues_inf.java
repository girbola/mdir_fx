
package com.girbola.controllers.main.tables.model;

import com.girbola.fileinfo.FileInfo;
import javafx.beans.property.*;

import java.util.List;
import java.util.Map;


interface TableValues_inf {

    //@formatter:off
    public IntegerProperty status_property();
    public List<FileInfo> getFileInfoList();
    public Map<String, String> getFileList();
    public SimpleBooleanProperty changed_property();
    public SimpleBooleanProperty connected_property();
    public SimpleBooleanProperty ignored_prop();
    public SimpleDoubleProperty dateDifferenceRatio_prop();
    public SimpleIntegerProperty badFiles_prop();
    public SimpleIntegerProperty confirmed_property();
    public SimpleIntegerProperty copied_property();
    public SimpleIntegerProperty folderFiles_prop();
    public SimpleIntegerProperty folderImageFiles_prop();
    public SimpleIntegerProperty folderRawFiles_prop();
    public SimpleIntegerProperty folderVideoFiles_prop();
    public SimpleIntegerProperty goodFiles_prop();
    public SimpleIntegerProperty suggested_prop();
    public SimpleLongProperty folderSize_prop();
    public SimpleStringProperty folderPath_prop();
    public SimpleStringProperty maxDate_prop();
    public SimpleStringProperty minDate_prop();
    public SimpleStringProperty state_property();
    public SimpleStringProperty tableType_property();
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
    public int getBadFiles();
    public int getConfirmed();
    public int getCopied();
    public int getFolderFiles();
    public int getFolderImageFiles();
    public int getFolderRawFiles();
    public int getFolderVideoFiles();
    public int getGoodFiles();
    public int getStatus();
    public int getSuggested();
    public long getFolderSize();
    public void setBadFiles(int value);
    public void setChanged(boolean changed);
    public void setConfirmed(int value);
    public void setConnected(boolean value);
    public void setCopied(int value);
    public void setDateDifferenceRatio(double value);
    public void setFileInfoList(List<FileInfo> fileInfo);
    public void setFolderFiles(int value);
    public void setFolderImageFiles(int value);
    public void setFolderPath(String value);
    public void setFolderRawFiles(int value);
    public void setFolderSize(long value);
    public void setFolderVideoFiles(int value);
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
