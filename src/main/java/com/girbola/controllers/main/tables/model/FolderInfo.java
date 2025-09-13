package com.girbola.controllers.main.tables.model;

import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.fileinfo.FileInfo;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;

import java.nio.file.Path;
import java.util.*;

import static com.girbola.Main.simpleDates;

public class FolderInfo implements TableValues_inf {

    //@formatter:off
    // Fields (alphabetical)
    private SimpleIntegerProperty badFiles;
    // Image/Video category properties (alphabetical within the subgroup)
    private SimpleIntegerProperty acceptedImageFiles;
    private SimpleIntegerProperty acceptedVideoFiles;
    private SimpleIntegerProperty badImageFiles;
    private SimpleIntegerProperty badVideoFiles;
    private SimpleIntegerProperty goodImageFiles;
    private SimpleIntegerProperty goodVideoFiles;
    private SimpleIntegerProperty suggestedImageFiles;
    private SimpleIntegerProperty suggestedVideoFiles;

    private SimpleBooleanProperty changed;
    private SimpleBooleanProperty connected;
    private SimpleIntegerProperty confirmed;
    private SimpleIntegerProperty copied;
    private SimpleDoubleProperty dateDifference;
    private List<FileInfo> fileInfoList;
    private SimpleIntegerProperty folderFiles;
    private SimpleIntegerProperty folderImageFiles;
    private SimpleStringProperty folderPath;
    private SimpleIntegerProperty folderRawFiles;
    private SimpleLongProperty folderSize;
    private SimpleIntegerProperty folderVideoFiles;
    private SimpleIntegerProperty goodFiles;
    private SimpleBooleanProperty ignored;
    private SimpleStringProperty justFolderName;
    private SimpleStringProperty maxDate;
    private SimpleStringProperty minDate;
    private SimpleStringProperty sourceFolderSerialNumber;
    private SimpleStringProperty state;
    private IntegerProperty status;
    private SimpleIntegerProperty suggested;
    private SimpleStringProperty tableType;
    //@formatter:on

    /**
     * Constructs a new `FolderInfo` object with default initial values for all properties.
     *
     * This constructor initializes various properties related to folder statistics, metadata,
     * state tracking, and file categorization. These properties include counters for file
     * types (e.g., image files, video files), booleans for folder status (e.g., connected,
     * ignored), and additional metadata such as folder path and dates. All properties
     * are initialized with default values, such as zero for numerical properties, empty
     * strings for text properties, and false for boolean properties.
     *
     * The constructor also initializes specific category-tracking properties for image
     * and video files, including properties for bad, good, suggested, and accepted files.
     *
     * Additionally, this constructor sets up property bindings, such as calculating the
     * difference between the number of files and bad files.
     */
    public FolderInfo() {
        this.badFiles = new SimpleIntegerProperty(0);
        this.changed = new SimpleBooleanProperty(false);
        this.confirmed = new SimpleIntegerProperty(0);
        this.connected = new SimpleBooleanProperty(false);
        this.copied = new SimpleIntegerProperty(0);
        this.dateDifference = new SimpleDoubleProperty(0);
        this.folderFiles = new SimpleIntegerProperty(0);
        this.folderImageFiles = new SimpleIntegerProperty(0);
        this.folderPath = new SimpleStringProperty("");
        this.folderRawFiles = new SimpleIntegerProperty(0);
        this.folderSize = new SimpleLongProperty(0);
        this.folderVideoFiles = new SimpleIntegerProperty(0);
        this.goodFiles = new SimpleIntegerProperty(0);
        this.ignored = new SimpleBooleanProperty(false);
        this.justFolderName = new SimpleStringProperty("");
        this.maxDate = new SimpleStringProperty("0");
        this.minDate = new SimpleStringProperty("0");
        this.state = new SimpleStringProperty("");
        this.status = new SimpleIntegerProperty(0);
        this.suggested = new SimpleIntegerProperty(0);
        this.tableType = new SimpleStringProperty("");
        this.sourceFolderSerialNumber = new SimpleStringProperty("");

        // Image/Video category initializations
        this.badImageFiles = new SimpleIntegerProperty(0);
        this.goodImageFiles = new SimpleIntegerProperty(0);
        this.suggestedImageFiles = new SimpleIntegerProperty(0);
        this.acceptedImageFiles = new SimpleIntegerProperty(0);
        this.badVideoFiles = new SimpleIntegerProperty(0);
        this.goodVideoFiles = new SimpleIntegerProperty(0);
        this.suggestedVideoFiles = new SimpleIntegerProperty(0);
        this.acceptedVideoFiles = new SimpleIntegerProperty(0);

        Bindings.subtract(folderFiles, badFiles);
    }


    /**
     * Constructs a new FolderInfo object with initial values for the provided folder path
     * and associated metadata.
     *
     * @param folderPath the path to the folder to initialize the FolderInfo object for
     * @param tableType the type of table associated with this folder
     * @param justFolderName the name of the folder, or an empty string to use the folder name derived from the path
     * @param connected a boolean indicating if the folder is connected
     */
    public FolderInfo(Path folderPath, String tableType, String justFolderName, boolean connected) {
        this.badFiles = new SimpleIntegerProperty(0);
        this.changed = new SimpleBooleanProperty(false);
        this.confirmed = new SimpleIntegerProperty(0);
        this.connected = new SimpleBooleanProperty(connected);
        this.copied = new SimpleIntegerProperty(0);
        this.dateDifference = new SimpleDoubleProperty(0);
        this.fileInfoList = new ArrayList<>();
        this.folderFiles = new SimpleIntegerProperty(0);
        this.folderImageFiles = new SimpleIntegerProperty(0);
        this.justFolderName = new SimpleStringProperty(justFolderName.isEmpty() ? folderPath.getFileName().toString() : justFolderName);
        this.folderPath = new SimpleStringProperty(folderPath.toString());
        this.folderRawFiles = new SimpleIntegerProperty(0);
        this.folderSize = new SimpleLongProperty(0);
        this.folderVideoFiles = new SimpleIntegerProperty(0);
        this.goodFiles = new SimpleIntegerProperty(0);
        this.ignored = new SimpleBooleanProperty(false);
        this.maxDate = new SimpleStringProperty("0");
        this.minDate = new SimpleStringProperty("0");
        this.state = new SimpleStringProperty("");
        this.status = new SimpleIntegerProperty(0);
        this.suggested = new SimpleIntegerProperty(0);
        this.tableType = new SimpleStringProperty(tableType);
        this.sourceFolderSerialNumber = new SimpleStringProperty("");

        // Image/Video category initializations
        this.badImageFiles = new SimpleIntegerProperty(0);
        this.goodImageFiles = new SimpleIntegerProperty(0);
        this.suggestedImageFiles = new SimpleIntegerProperty(0);
        this.acceptedImageFiles = new SimpleIntegerProperty(0);
        this.badVideoFiles = new SimpleIntegerProperty(0);
        this.goodVideoFiles = new SimpleIntegerProperty(0);
        this.suggestedVideoFiles = new SimpleIntegerProperty(0);
        this.acceptedVideoFiles = new SimpleIntegerProperty(0);

        Bindings.subtract(folderFiles, badFiles);
    }


    /**
     * Constructs a new FolderInfo object with initial values for the provided folder path.
     *
     * @param folderPath the path of the folder to initialize the FolderInfo object for
     */
    public FolderInfo(Path folderPath) {
        this.badFiles = new SimpleIntegerProperty(0);
        this.changed = new SimpleBooleanProperty(false);
        this.confirmed = new SimpleIntegerProperty(0);
        this.connected = new SimpleBooleanProperty(false);
        this.copied = new SimpleIntegerProperty(0);
        this.dateDifference = new SimpleDoubleProperty(0);
        this.fileInfoList = new ArrayList<>();
        this.folderFiles = new SimpleIntegerProperty(0);
        this.folderImageFiles = new SimpleIntegerProperty(0);
        this.justFolderName = new SimpleStringProperty(folderPath.getFileName().toString());
        this.folderPath = new SimpleStringProperty(folderPath.toString());
        this.folderRawFiles = new SimpleIntegerProperty(0);
        this.folderSize = new SimpleLongProperty(0);
        this.folderVideoFiles = new SimpleIntegerProperty(0);
        this.goodFiles = new SimpleIntegerProperty(0);
        this.ignored = new SimpleBooleanProperty(false);
        this.maxDate = new SimpleStringProperty("0");
        this.minDate = new SimpleStringProperty("0");
        this.state = new SimpleStringProperty("");
        this.status = new SimpleIntegerProperty(0);
        this.suggested = new SimpleIntegerProperty(0);
        this.tableType = new SimpleStringProperty("");
        this.sourceFolderSerialNumber = new SimpleStringProperty("");

        // Image/Video category initializations
        this.badImageFiles = new SimpleIntegerProperty(0);
        this.goodImageFiles = new SimpleIntegerProperty(0);
        this.suggestedImageFiles = new SimpleIntegerProperty(0);
        this.acceptedImageFiles = new SimpleIntegerProperty(0);
        this.badVideoFiles = new SimpleIntegerProperty(0);
        this.goodVideoFiles = new SimpleIntegerProperty(0);
        this.suggestedVideoFiles = new SimpleIntegerProperty(0);
        this.acceptedVideoFiles = new SimpleIntegerProperty(0);

        Bindings.subtract(folderFiles, badFiles);
    }

    //@formatter:off
    // Getters (values)
    @Override public int               getBadFiles()                 { return this.badFiles.get(); }
    @Override public boolean           getChanged()                  { return this.changed.get(); }
    @Override public boolean           getIgnored()                  { return this.ignored.get(); }
    @Override public boolean           isConnected()                 { return this.connected.get(); }
    @Override public double            getDateDifferenceRatio()      { return this.dateDifference.get(); }
    @Override public int               getConfirmed()                { return this.confirmed.get(); }
    @Override public int               getCopied()                   { return this.copied.get(); }
    @Override public List<FileInfo>    getFileInfoList()             { return this.fileInfoList; }
    @Override public int               getFolderFiles()              { return this.folderFiles.get(); }
    @Override public int               getFolderImageFiles()         { return this.folderImageFiles.get(); }
    @Override public String            getFolderPath()               { return this.folderPath.get(); }
    @Override public int               getFolderRawFiles()           { return this.folderRawFiles.get(); }
    @Override public long              getFolderSize()               { return this.folderSize.get(); }
    @Override public int               getFolderVideoFiles()         { return this.folderVideoFiles.get(); }
    @Override public int               getGoodFiles()                { return this.goodFiles.get(); }
    @Override public String            getJustFolderName()           { return this.justFolderName.get(); }
    @Override public String            getMaxDate()                  { return this.maxDate.get(); }
    @Override public String            getMinDate()                  { return this.minDate.get(); }
    @Override public String            getSourceFolderSerialNumber() { return this.sourceFolderSerialNumber.get(); }
    @Override public int               getStatus()                   { return this.status.get(); }
    @Override public String            getState()                    { return this.state.get(); }
    @Override public int               getSuggested()                { return this.suggested.get(); }
    @Override public int               getBadImageFiles()            { return this.badImageFiles.get(); }
    @Override public int               getGoodImageFiles()           { return this.goodImageFiles.get(); }
    @Override public int               getSuggestedImageFiles()      { return this.suggestedImageFiles.get(); }
    @Override public int               getAcceptedImageFiles()       { return this.acceptedImageFiles.get(); }
    @Override public int               getBadVideoFiles()            { return this.badVideoFiles.get(); }
    @Override public int               getGoodVideoFiles()           { return this.goodVideoFiles.get(); }
    @Override public int               getSuggestedVideoFiles()      { return this.suggestedVideoFiles.get(); }
    @Override public int               getAcceptedVideoFiles()       { return this.acceptedVideoFiles.get(); }
    @Override public String            getTableType()                { return tableType.get(); }

    // Property accessors (…_prop and …_property)
    @Override public SimpleIntegerProperty badFiles_prop()                 { return badFiles; }
    @Override public SimpleIntegerProperty badImageFiles_prop()            { return badImageFiles; }
    @Override public SimpleIntegerProperty badVideoFiles_prop()            { return badImageFiles; }
    @Override public SimpleBooleanProperty changed_property()              { return this.changed; }
    @Override public SimpleBooleanProperty connected_property()            { return connected; }
    @Override public SimpleIntegerProperty confirmed_property()            { return confirmed; }
    @Override public SimpleIntegerProperty copied_property()               { return this.copied; }
    @Override public SimpleDoubleProperty  dateDifferenceRatio_prop()      { return dateDifference; }
    @Override public SimpleIntegerProperty folderFiles_prop()              { return this.folderFiles; }
    @Override public SimpleIntegerProperty folderImageFiles_prop()         { return this.folderImageFiles; }
    @Override public SimpleStringProperty  folderPath_prop()               { return this.folderPath; }
    @Override public SimpleIntegerProperty folderRawFiles_prop()           { return this.folderRawFiles; }
    @Override public SimpleLongProperty    folderSize_prop()               { return this.folderSize; }
    @Override public SimpleIntegerProperty folderVideoFiles_prop()         { return this.folderVideoFiles; }
    @Override public SimpleIntegerProperty goodFiles_prop()                { return this.goodFiles; }
    @Override public SimpleIntegerProperty goodImageFiles_prop()           { return goodImageFiles; }
    @Override public SimpleIntegerProperty goodVideoFiles_prop()           { return goodVideoFiles; }
    @Override public SimpleBooleanProperty ignored_prop()                  { return this.ignored; }
    @Override public SimpleStringProperty  maxDate_prop()                  { return this.maxDate; }
    @Override public SimpleStringProperty  minDate_prop()                  { return this.minDate; }
    @Override public SimpleIntegerProperty suggested_prop()                { return this.suggested; }
    @Override public SimpleIntegerProperty suggestedImageFiles_prop()      { return suggestedImageFiles; }
    @Override public SimpleIntegerProperty suggestedVideoFiles_prop()      { return suggestedVideoFiles; }
    @Override public SimpleStringProperty  state_property()                { return state; }
    @Override public SimpleIntegerProperty acceptedImageFiles_prop()       { return acceptedImageFiles; }
    @Override public SimpleIntegerProperty acceptedVideoFiles_prop()       { return acceptedVideoFiles; }
    @Override public SimpleStringProperty  tableType_property()            { return tableType; }
    @Override public IntegerProperty       status_property()               { return status; }

    // Setters (alphabetical)
    @Override public void setBadImageFiles(int value)        { this.badImageFiles.set(value); }
    @Override public void setGoodImageFiles(int value)       { this.goodImageFiles.set(value); }
    @Override public void setSuggestedImageFiles(int value)  { this.suggestedImageFiles.set(value); }
    @Override public void setAcceptedImageFiles(int value)   { this.acceptedImageFiles.set(value); }

    @Override public void setBadVideoFiles(int value)        { this.badVideoFiles.set(value); }
    @Override public void setGoodVideoFiles(int value)       { this.goodVideoFiles.set(value); }
    @Override public void setSuggestedVideoFiles(int value)  { this.suggestedVideoFiles.set(value); }
    @Override public void setAcceptedVideoFiles(int value)   { this.acceptedVideoFiles.set(value); }

    @Override public void setChanged(boolean changed)        { this.changed.set(changed); }
    @Override public void setConfirmed(int value)                { this.confirmed.set(value); }
    @Override public void setConnected(boolean connected)        { this.connected.set(connected); }
    @Override public void setCopied(int value)                   { this.copied.set(value); }
    @Override public void setDateDifferenceRatio(double value)   { this.dateDifference.set(value); }
    @Override public void setFileInfoList(List<FileInfo> value)  { this.fileInfoList = value; }
    @Override public void setFolderFiles(int value)              { this.folderFiles.set(value); }

    @Override
    public void setBadFiles(int value) {
        this.badFiles.set(value);
        TableUtils.updateStatus(status, this.folderFiles.get(), this.badFiles.get(), this.suggested.get());
    }

    @Override
    public void setFolderImageFiles(int value) {
        this.folderImageFiles.set(value);
        TableUtils.updateStatus(status, this.folderFiles.get(), this.badFiles.get(), this.suggested.get());
    }

    @Override
    public void setFolderPath(String value) {
        this.folderPath.set(value);
    }

    @Override
    public void setFolderRawFiles(int value) {
        this.folderRawFiles.set(value);
        TableUtils.updateStatus(status, this.folderFiles.get(), this.badFiles.get(), this.suggested.get());
    }

    @Override
    public void setFolderSize(long value) {
        this.folderSize.set(value);
    }

    @Override
    public void setFolderVideoFiles(int value) {
        this.folderVideoFiles.set(value);
        TableUtils.updateStatus(status, this.folderFiles.get(), this.badFiles.get(), this.suggested.get());
    }

    @Override public void setGoodFiles(int value) {this.goodFiles.set(value);}
    @Override public void setIgnored(boolean value) {this.ignored.set(value);}
    @Override public void setJustFolderName(String value) {this.justFolderName.set(value);}
    @Override public void setMaxDate(String value) {this.maxDate.set(value);}
    @Override public void setMinDate(String value) {this.minDate.set(value);}
    @Override public void setSourceFolderSerialNumber(String serialNumber) {this.sourceFolderSerialNumber.set(serialNumber);}
    @Override public void setState(String value) {this.state.set(value);}
    @Override public void setStatus(int value) {this.status.set(value);}
    @Override public void setSuggested(int value) {this.suggested.set(value);TableUtils.updateStatus(status, this.folderFiles.get(), this.badFiles.get(), this.suggested.get());}
    @Override public void setTableType(String value) {this.tableType.set(value);}

    // Other methods
    @Override
    public Map<String, String> getFileList() {
        Map<String, String> map = new HashMap<>();
        Iterator<FileInfo> it = getFileInfoList().iterator();
        while (it.hasNext()) {
            FileInfo fileInfo = it.next();
            if (!fileInfo.isIgnored()) {
                map.put(fileInfo.getOrgPath(),
                        simpleDates.getSdf_ymd_hms_minusDots_default().format(fileInfo.getDate()));
            }
        }
        return map;
    }

    @Override
    public String toString() {
        return "FolderInfo{" +
                "status=" + status.get() +
                ", fileInfoList.size =" + fileInfoList.size() +
                ", changed=" + changed.get() +
                ", connected=" + connected.get() +
                ", ignored=" + ignored.get() +
                ", dateDifference=" + dateDifference.get() +
                ", badFiles=" + badFiles.get() +
                ", confirmed=" + confirmed.get() +
                ", copied=" + copied.get() +
                ", folderFiles=" + folderFiles.get() +
                ", folderImageFiles=" + folderImageFiles.get() +
                ", folderRawFiles=" + folderRawFiles.get() +
                ", folderVideoFiles=" + folderVideoFiles.get() +
                ", goodFiles=" + goodFiles.get() +
                ", suggested=" + suggested.get() +
                ", folderSize=" + folderSize.get() +
                ", folderPath=" + folderPath.get() +
                ", justFolderName=" + justFolderName.get() +
                ", maxDate=" + maxDate.get() +
                ", minDate=" + minDate.get() +
                ", state=" + state.get() +
                ", tableType=" + tableType.get() +
                ", sourceFolderSerialNumber=" + sourceFolderSerialNumber.get() +
                '}';
    }
}