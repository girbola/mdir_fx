package com.girbola.fileinfo;

import java.util.*;
import java.util.stream.*;

public enum FileInfoEnum {
    BAD("bad"),
    CAMERA_MODEL("camera_model"),
    CONFIRMED("confirmed"),
    DATE("date"),
    DESTINATION_PATH("destination_Path"),
    EVENT("event"),
    FILE_INFO_ID("fileInfo_id"),
    GOOD("good"),
    IGNORED("ignored"),
    IMAGE("image"),
    IMAGE_DIFFERENCE_HASH("imageDifferenceHash"),
    LOCAL_DATE_TIME("localDateTime"),
    LOCATION("location"),
    ORG_PATH("org_path"),
    ORIENTATION("orientation"),
    RAW("raw"),
    SIZE("size"),
    SUGGESTED("suggested"),
    TABLE_DUPLICATED("tableDuplicated"),
    TAGS("tags"),
    THUMB_LENGTH("thumb_length"),
    THUMB_OFFSET("thumb_offset"),
    TIME_SHIFT("timeShift"),
    USER("user"),
    VIDEO("video"),
    WORK_DIR("workDir"),
    WORK_DIR_DRIVE_SERIAL_NUMBER("workDirDriveSerialNumber");

    private final String columnName;

    FileInfoEnum(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnName() {
        return columnName;
    }

    public static String getAllFileInfoEnumValues() {
        return Arrays.stream(FileInfoEnum.values())
                .map(FileInfoEnum::name)
                .collect(Collectors.joining(", "));
    }

}