package com.girbola.controllers.main.tables.tabletype;

public enum FolderInfoEnum {

    BAD_FILES("badFiles"),
    CHANGED("changed"),
    CONFIRMED("confirmed"),
    CONNECTED("connected"),
    COPIED("copied"),
    DATE_DIFFERENCE("dateDifference"),
    FOLDER_FILES("folderFiles"),
    FOLDER_IMAGE_FILES("folderImageFiles"),
    FOLDER_PATH("folderPath"),
    FOLDER_RAW_FILES("folderRawFiles"),
    FOLDER_SIZE("folderSize"),
    FOLDER_VIDEO_FILES("folderVideoFiles"),
    GOOD_FILES("goodFiles"),
    IGNORED("ignored"),
    JUST_FOLDER_NAME("justFolderName"),
    MAX_DATE("maxDate"),
    MIN_DATE("minDate"),
    STATE("state"),
    SUGGESTED("suggested"),
    TABLE_TYPE("tableType"),
    WORKDIR_SERIAL_NUMBER("workdirSerialNumber");

    private final String columnName;

    FolderInfoEnum(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnName() {
        return columnName;
    }
}