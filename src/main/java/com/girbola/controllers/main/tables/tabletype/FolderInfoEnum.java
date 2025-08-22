package com.girbola.controllers.main.tables.tabletype;

public enum FolderInfoEnum {

    CHANGED("changed"),
    CONNECTED("connected"),
    IGNORED("ignored"),
    DATE_DIFFERENCE("dateDifference"),
    BAD_FILES("badFiles"),
    CONFIRMED("confirmed"),
    COPIED("copied"),
    FOLDER_FILES("folderFiles"),
    FOLDER_IMAGE_FILES("folderImageFiles"),
    FOLDER_RAW_FILES("folderRawFiles"),
    FOLDER_VIDEO_FILES("folderVideoFiles"),
    GOOD_FILES("goodFiles"),
    SUGGESTED("suggested"),
    FOLDER_SIZE("folderSize"),
    JUST_FOLDER_NAME("justFolderName"),
    FOLDER_PATH("folderPath"),
    MAX_DATE("maxDate"),
    MIN_DATE("minDate"),
    STATE("state"),
    WORKDIR_SERIAL_NUMBER("workdirSerialNumber"),
    TABLE_TYPE("tableType");

    private final String columnName;

    FolderInfoEnum(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnName() {
        return columnName;
    }
}