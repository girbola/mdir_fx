/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.fileinfo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author Marko Lokka
 */
@Getter
@Setter
public class FileInfo extends Metadata implements Cloneable {
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    private final int fileInfo_version = 2;
    /**
     * FileInfo version 1 ================== fileInfo_version number created
     * fileName changed from thumbfile.xml to fileinfo.dat
     * <p>
     * FileInfo version 2 ================== workDirDriveSerialNumber added
     */
    private boolean bad;
    private boolean confirmed;
    private boolean copied;
    private boolean good;
    private boolean ignored;
    private boolean image;
    private boolean raw;
    private boolean suggested;
    private boolean tableDuplicated;
    private boolean video;

    private int fileInfo_id;
    private int orientation;
    private int thumb_length;
    private int thumb_offset;
    private LocalDateTime localDateTime;
    private long date;
    private long size;
    private long timeShift;
    private String camera_model;
    private String destination_Path;
    private String event;
    private String location;
    private String orgPath;

    private String tags;
    private String user;
    private String workDir;
    private String workDirDriveSerialNumber;

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    public String showAllValues() {
        return "FileInfo [orgPath=" + orgPath + ", workdir=" + workDir + ", workDirDriveSerialNumber="
                + workDirDriveSerialNumber + ", destination_Path=" + destination_Path + ", fileInfo_version="
                + fileInfo_version + ", event=" + event + ", location=" + location + ", tags=" + tags
                + ", + fileInfo_id=" + fileInfo_id + ", camera_model=" + camera_model + ", bad=" + bad + ", confirmed="
                + confirmed + ", copied=" + copied + ", good=" + good + ", ignored=" + ignored + ", image=" + image
                + ", raw=" + raw + ", suggested=" + suggested + ", video=" + video + ", orientation=" + orientation
                + ", thumb_length=" + thumb_length + ", timeShift=" + timeShift + ", date=" + date + ", size=" + size
                + ", tableDuplicated=" + tableDuplicated + ", thumb_offset=" + thumb_offset + " user " + user + "]";
    }

    /**
     * @param aOrgPath
     * @param fileInfo_id
     */
    public FileInfo(String aOrgPath, int fileInfo_id) {
        this.orgPath = aOrgPath;
        this.fileInfo_id = fileInfo_id;
        this.destination_Path = "";
        this.event = "";
        this.location = "";
        this.tags = "";
        this.camera_model = "Unknown";
        this.orientation = 0;
        this.timeShift = 0;
        this.bad = false;
        this.good = false;
        this.suggested = false;
        this.confirmed = false;
        this.raw = false;
        this.image = false;
        this.video = false;
        this.ignored = false;
        this.tableDuplicated = false;
        this.date = 0;
        this.size = 0;
        this.thumb_offset = 0;
        this.thumb_length = 0;
        this.user = "";
        this.workDir = "";
        this.workDirDriveSerialNumber = "";
    }

    /**
     *
     */
    public FileInfo() {
        this(null, null, null, null, null, null, null, null, null, 0, 0, 0, false, false, false, false, false, false,
                false, false, false, false, 0, 0, 0, 0);
    }

    /**
     * @param aOrgPath
     * @param aWorkDir
     * @param aWorkDirDriveSerialNumber
     * @param aDestinationStructure
     * @param aEvent
     * @param aLocation
     * @param aTags
     * @param aCamera_model
     * @param user
     * @param aOrientation
     * @param aTimeShift
     * @param aFileInfo_id
     * @param aBad
     * @param aGood
     * @param aSuggested
     * @param aConfirmed
     * @param aImage
     * @param aRaw
     * @param aVideo
     * @param aIgnored
     * @param aCopied
     * @param aTableDuplicated
     * @param aDate
     * @param aSize
     * @param aThumb_offset
     * @param aThumb_length
     */
    public FileInfo(String aOrgPath, String aWorkDir, String aWorkDirDriveSerialNumber, String aDestinationStructure,
                    String aEvent, String aLocation, String aTags, String aCamera_model, String user, int aOrientation,
                    long aTimeShift, int aFileInfo_id, boolean aBad, boolean aGood, boolean aSuggested, boolean aConfirmed,
                    boolean aImage, boolean aRaw, boolean aVideo, boolean aIgnored, boolean aCopied, boolean aTableDuplicated,
                    long aDate, long aSize, int aThumb_offset, int aThumb_length) {
        this.orgPath = aOrgPath;
        this.workDir = aWorkDir;
        this.workDirDriveSerialNumber = aWorkDirDriveSerialNumber;
        this.destination_Path = aDestinationStructure;
        this.event = aEvent;
        this.location = aLocation;
        this.tags = aTags;
        this.camera_model = aCamera_model;
        this.orientation = aOrientation;
        this.timeShift = aTimeShift;
        this.fileInfo_id = aFileInfo_id;
        this.date = aDate;
        this.size = aSize;
        this.bad = aBad;
        this.good = aGood;
        this.ignored = aIgnored;
        this.tableDuplicated = aTableDuplicated;
        this.suggested = aSuggested;
        this.confirmed = aConfirmed;
        this.raw = aRaw;
        this.image = aImage;
        this.video = aVideo;
        this.copied = aCopied;
        this.thumb_offset = aThumb_offset;
        this.thumb_length = aThumb_length;
        this.user = user;
    }


    @Override
    public String toString() {
        return this.orgPath;
    }

}
