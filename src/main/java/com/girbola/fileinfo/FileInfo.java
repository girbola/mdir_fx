package com.girbola.fileinfo;

import com.girbola.controllers.datefixer.utils.MetadataField;
import common.utils.OSHI_Utils;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileInfo extends Metadata implements Cloneable {

    private long timeShift;

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public String showAllValues() {
            return "FileInfo{" +
                    "  \n bad=" + isBad() +
                    ", \n camera_model='" + getCamera_model() + '\'' +
                    ", \n confirmed=" + isConfirmed() +
                    ", \n copied=" + isCopied() +
                    ", \n date=" + getDate() +
                    ", \n destination_Path='" + getDestination_Path() + '\'' +
                    ", \n event='" + getEvent() + '\'' +
                    ", \n fileInfo_id=" + getFileInfo_id() +
                    ", \n good=" + isGood() +
                    ", \n ignored=" + isIgnored() +
                    ", \n image=" + isImage() +
                    ", \n imageDifferenceHash=" + getImageDifferenceHash() +
                    ", \n location='" + getLocation() + '\'' +
                    ", \n modified=" + isModified() +
                    ", \n orientation=" + getOrientation() +
                    ", \n orgPath='" + getOrgPath() + '\'' +
                    ", \n orgPathDriveSerialNumber='" + getOrgPathDriveSerialNumber() + '\'' +
                    ", \n raw=" + isRaw() +
                    ", \n size=" + getSize() +
                    ", \n suggested=" + isSuggested() +
                    ", \n tableDuplicated=" + isTableDuplicated() +
                    ", \n tags='" + getTags() + '\'' +
                    ", \n thumb_length=" + getThumb_length() +
                    ", \n thumb_offset=" + getThumb_offset() +
                    ", \n timeShift=" + getTimeShift() +
                    ", \n user='" + getUser() + '\'' +
                    ", \n video=" + isVideo() +
                    ", \n workDir='" + getWorkDir() + '\'' +
                    ", \n workDirDriveSerialNumber='" + getWorkDirDriveSerialNumber() + '\'' +
                    ", \n fileInfoHistories='" + getFileHistories() + '\'' +
                    '}';
        }

        /**
         * Represents information about a file.
         *
         * @param orgPath                     The original path of the file.
         * @param fileInfo_id                 The ID of the file info.
         */
    public FileInfo(String orgPath, int fileInfo_id){
            this.setOrgPath(orgPath);
            this.setOrgPathDriveSerialNumber(OSHI_Utils.getDriveSerialNumber(orgPath));
            this.setFileInfo_id(fileInfo_id);

            this.setDestination_Path("");
            this.setEvent("");
            this.setLocation("");
            this.setOrientation(0);
            this.setTags("");

            this.setCamera_model(MetadataField.UNKNOWN.getType());
            this.setBad(false);
            this.setGood(false);
            this.setSuggested(false);
            this.setConfirmed(false);
            this.setModified(false);

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
            this.setFileHistories(new ArrayList<>());
            this.timeShift = 0;

        }

        /**
         *
         */
    /**
     * Default no-argument constructor that initializes all fields with default values.
     */
    public FileInfo() {
        this(
                null,                     // aOrgPath - Original file path
                null,                     // aOrgPathDriveSerialNumber - Serial number of the original file drive
                null,                     // aWorkDir - Working directory path
                null,                     // aWorkDirDriveSerialNumber - Serial number of the work directory drive
                null,                     // aDestinationStructure - Destination path structure
                null,                     // aEvent - Event name/identifier
                null,                     // aLocation - Location information
                null,                     // aTags - File tags
                null,                     // aCamera_model - Camera model name
                null,                     // user - User identifier
                0,                        // aOrientation - Image orientation value
                0,                        // aTimeShift - Time shift value in milliseconds
                0,                        // aFileInfo_id - Unique file info identifier
                false,                    // aBad - Bad file flag
                false,                    // aGood - Good file flag
                false,                    // aSuggested - Suggested file flag
                false,                    // aConfirmed - Confirmed file flag
                false,                    // aModified - Modified file flag
                false,                    // aImage - Is image file flag
                false,                    // aRaw - Is RAW image file flag
                false,                    // aVideo - Is video file flag
                false,                    // aIgnored - Ignored file flag
                false,                    // aCopied - Copied file flag
                false,                    // aTableDuplicated - Table duplicated flag
                0,                        // aDate - File date timestamp
                0L,                       // aSize - File size in bytes
                "",                       // aImageDifferenceHash - Image difference hash value
                0,                        // aThumb_offset - Thumbnail offset in file
                0,                        // aThumb_length - Thumbnail length in bytes
                new ArrayList<String>()   // fileInfoHistories - List of file history records
        );
    }

    /**
     * Represents information about a file.
     */
    public FileInfo(
            String aOrgPath,
            String aOrgPathDriveSerialNumber,
            String aWorkDir,
            String aWorkDirDriveSerialNumber,
            String aDestinationStructure,
            String aEvent,
            String aLocation,
            String aTags,
            String aCamera_model,
            String user,
            int aOrientation,
            long aTimeShift,
            int aFileInfo_id,
            boolean aBad,
            boolean aGood,
            boolean aSuggested,
            boolean aConfirmed,
            boolean aModified,
            boolean aImage,
            boolean aRaw,
            boolean aVideo,
            boolean aIgnored,
            boolean aCopied,
            boolean aTableDuplicated,
            long aDate,
            long aSize,
            String aImageDifferenceHash,
            int aThumb_offset,
            int aThumb_length,
            List<String> fileInfoHistories) {
        this.setBad(aBad);
        this.setCamera_model(aCamera_model);
        this.setConfirmed(aConfirmed);
        this.setCopied(aCopied);
        this.setDate(aDate);
        this.setDestination_Path(aDestinationStructure);
        this.setEvent(aEvent);
        this.setFileHistories(fileInfoHistories);
        this.setFileInfo_id(aFileInfo_id);
        this.setGood(aGood);
        this.setIgnored(aIgnored);
        this.setImage(aImage);
        this.setImageDifferenceHash(aImageDifferenceHash);
        this.setLocation(aLocation);
        this.setModified(aModified);
        this.setOrgPath(aOrgPath);
        this.setOrgPathDriveSerialNumber(aOrgPathDriveSerialNumber);
        this.setOrientation(aOrientation);
        this.setRaw(aRaw);
        this.setSize(aSize);
        this.setSuggested(aSuggested);
        this.setTableDuplicated(aTableDuplicated);
        this.setTags(aTags);
        this.setThumb_length(aThumb_length);
        this.setThumb_offset(aThumb_offset);
        this.setTimeShift(aTimeShift);
        this.setUser(user);
        this.setVideo(aVideo);
        this.setWorkDir(aWorkDir);
        this.setWorkDirDriveSerialNumber(aWorkDirDriveSerialNumber);
    }

        @Override
        public String toString () {
            return this.getOrgPath();
        }

    }
