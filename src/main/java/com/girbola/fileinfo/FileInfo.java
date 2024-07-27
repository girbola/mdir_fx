/*
 @(#)Copyright:  Copyright (c) 2012-2024 All right reserved.
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.fileinfo;

import com.girbola.MDir_Constants;
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

    private final int fileInfo_version = 1;

    private LocalDateTime localDateTime;

    private long timeShift;

    public String showAllValues() {

        return "FileInfo{" +
                "bad=" + isBad() +
                ", camera_model='" + getCamera_model() + '\'' +
                ", confirmed=" + isConfirmed() +
                ", copied=" + isCopied() +
                ", date=" + getDate() +
                ", destination_Path='" + getDestination_Path() + '\'' +
                ", event='" + getEvent() + '\'' +
                ", fileInfo_id=" + getFileInfo_id() +
                ", fileInfo_version=" + getFileInfo_version() +
                ", good=" + isGood() +
                ", ignored=" + isIgnored() +
                ", image=" + isImage() +
                ", imageDifferenceHash=" + getImageDifferenceHash() +
                ", localDateTime=" + getLocalDateTime() +
                ", location='" + getLocation() + '\'' +
                ", orientation=" + getOrientation() +
                ", orgPath='" + getOrgPath() + '\'' +
                ", raw=" + isRaw() +
                ", size=" + getSize() +
                ", suggested=" + isSuggested() +
                ", tableDuplicated=" + isTableDuplicated() +
                ", tags='" + getTags() + '\'' +
                ", thumb_length=" + getThumb_length() +
                ", thumb_offset=" + getThumb_offset() +
                ", timeShift=" + getTimeShift() +
                ", user='" + getUser() + '\'' +
                ", video=" + isVideo() +
                ", workDir='" + getWorkDir() + '\'' +
                ", workDirDriveSerialNumber='" + getWorkDirDriveSerialNumber() + '\'' +
                '}';
    }

    /**
     * Represents information about a file.
     *
     * @param orgPath                     The original path of the file.
     * @param fileInfo_id                 The ID of the file info.
     */
    public FileInfo(String orgPath, int fileInfo_id) {
        this.setOrgPath(orgPath);
        this.setFileInfo_id(fileInfo_id);

        this.setDestination_Path("");
        this.setEvent("");
        this.setLocation("");
        this.setOrientation(0);
        this.setTags("");

        this.setCamera_model(MDir_Constants.UNKNOWN.getType());
        this.setBad(false);
        this.setGood(false);
        this.setSuggested(false);
        this.setConfirmed(false);

        this.setIgnored(false);
        this.setTableDuplicated(false);
        this.setRaw(false);
        this.setImage(false);
        this.setVideo(false);
        this.setDate(0);
        this.setSize(0);
        this.setThumb_offset(0);
        this.setThumb_length(0);
        this.setImageDifferenceHash("");
        this.setUser("");
        this.setWorkDir("");
        this.setWorkDirDriveSerialNumber("");

        this.timeShift = 0;

    }

    /**
     *
     */
    public FileInfo() {
        this(null, null, null, null, null, null, null, null, null, 0, 0, 0, false, false, false, false, false, false,
                false, false, false, false, 0, 0L, "", 0, 0);
    }


    /**
     * Represents information about a file.
     */
    public FileInfo(String aOrgPath, String aWorkDir, String aWorkDirDriveSerialNumber, String aDestinationStructure,
                    String aEvent, String aLocation, String aTags, String aCamera_model, String user, int aOrientation,
                    long aTimeShift, int aFileInfo_id, boolean aBad, boolean aGood, boolean aSuggested, boolean aConfirmed,
                    boolean aImage, boolean aRaw, boolean aVideo, boolean aIgnored, boolean aCopied, boolean aTableDuplicated,
                    long aDate, long aSize, String aImageDifferenceHash, int aThumb_offset, int aThumb_length) {
        this.setOrgPath(aOrgPath);
        this.setWorkDir(aWorkDir);
        this.setWorkDirDriveSerialNumber(aWorkDirDriveSerialNumber);
        this.setDestination_Path(aDestinationStructure);
        this.setEvent(aEvent);
        this.setLocation(aLocation);
        this.setTags(aTags);
        this.setOrientation(aOrientation);
        this.setFileInfo_id(aFileInfo_id);
        this.setCamera_model(aCamera_model);
        this.setTimeShift(aTimeShift);
        this.setDate(aDate);
        this.setSize(aSize);
        this.setBad(aBad);
        this.setGood(aGood);
        this.setIgnored(aIgnored);
        this.setTableDuplicated(aTableDuplicated);
        this.setSuggested(aSuggested);
        this.setConfirmed(aConfirmed);
        this.setRaw(aRaw);
        this.setImage(aImage);
        this.setVideo(aVideo);
        this.setCopied(aCopied);
        this.setImageDifferenceHash(aImageDifferenceHash);
        this.setThumb_offset(aThumb_offset);
        this.setThumb_length(aThumb_length);
        this.setUser(user);
    }


    @Override
    public String toString() {
        return this.getOrgPath();
    }

}
