package com.girbola.fileinfo;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import com.girbola.messages.Messages;

public enum FileInfoEnum {
    BAD("bad", "BOOLEAN"),
    CAMERA_MODEL("camera_model", "TEXT"),
    CONFIRMED("confirmed", "BOOLEAN"),
    DESTINATION_PATH("destination_Path", "TEXT"),
    GPS_COORDINATES("gpsCoordinates", "TEXT"),
    CUSTOM_GPS_COORDINATES("customGpsCoordinates", "BOOLEAN"),
    DATE("date", "INTEGER"),
    EVENT("event", "TEXT"),
    FILEINFO_ID("fileInfo_id", "INTEGER PRIMARY KEY"),
    FILEHISTORIES("fileHistories", "TEXT"),
    GOOD("good", "BOOLEAN"),
    COPIED("copied", "BOOLEAN"),
    IGNORED("ignored", "BOOLEAN"),
    IMAGE("image", "BOOLEAN"),
    IMAGE_DIFFERENCE_HASH("imageDifferenceHash", "TEXT"),
    LOCATION("location", "TEXT"),
    MODIFIED("modified", "BOOLEAN"),
    ORGPATH("orgPath", "TEXT UNIQUE"),
    ORGPATH_DRIVE_SERIAL_NUMBER("orgPathDriveSerialNumber", "TEXT"),
    ORIENTATION("orientation", "INTEGER"),
    RAW("raw", "BOOLEAN"),
    SHA256_CHECKSUM("sha256Checksum", "TEXT"),
    SIZE("size", "NUMERIC"),
    SUGGESTED("suggested", "BOOLEAN"),
    TABLE_DUPLICATED("tableDuplicated", "BOOLEAN"),
    TAGS("tags", "TEXT"),
    THUMB_LENGTH("thumb_length", "INTEGER"),
    THUMB_OFFSET("thumb_offset", "INTEGER"),
    TIME_SHIFT("timeShift", "INTEGER"),
    USER("user", "TEXT"),
    VIDEO("video", "BOOLEAN"),
    WORK_DIR("workDir", "TEXT"),
    WORK_DIR_DRIVE_SERIAL_NUMBER("workDirDriveSerialNumber", "TEXT");

    private final String columnName;
    private final String sqlType;

    FileInfoEnum(String columnName, String sqlType) {
        this.columnName = columnName;
        this.sqlType = sqlType;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getSqlType() {
        return sqlType;
    }

    public String getColumnDefinition() {
        return columnName + " " + sqlType;
    }

    private static final String[] BINDING_ORDER = {
            FILEINFO_ID.getColumnName(),
            BAD.getColumnName(),
            CAMERA_MODEL.getColumnName(),
            CONFIRMED.getColumnName(),
            DESTINATION_PATH.getColumnName(),
            DATE.getColumnName(),
            EVENT.getColumnName(),
            FILEHISTORIES.getColumnName(),
            GOOD.getColumnName(),
            COPIED.getColumnName(),
            GPS_COORDINATES.getColumnName(),
            CUSTOM_GPS_COORDINATES.getColumnName(),
            IGNORED.getColumnName(),
            IMAGE.getColumnName(),
            IMAGE_DIFFERENCE_HASH.getColumnName(),
            LOCATION.getColumnName(),
            MODIFIED.getColumnName(),
            ORGPATH.getColumnName(),
            ORGPATH_DRIVE_SERIAL_NUMBER.getColumnName(),
            ORIENTATION.getColumnName(),
            RAW.getColumnName(),
            SHA256_CHECKSUM.getColumnName(),
            SIZE.getColumnName(),
            SUGGESTED.getColumnName(),
            TABLE_DUPLICATED.getColumnName(),
            TAGS.getColumnName(),
            THUMB_LENGTH.getColumnName(),
            THUMB_OFFSET.getColumnName(),
            TIME_SHIFT.getColumnName(),
            USER.getColumnName(),
            VIDEO.getColumnName(),
            WORK_DIR.getColumnName(),
            WORK_DIR_DRIVE_SERIAL_NUMBER.getColumnName(),
    };
//
//    private static final String[] BINDING_ORDER = {
//            FILEINFO_ID.getColumnName(),
//            ORGPATH.getColumnName(),
//            ORGPATH_DRIVE_SERIAL_NUMBER.getColumnName(),
//            WORK_DIR.getColumnName(),
//            WORK_DIR_DRIVE_SERIAL_NUMBER.getColumnName(),
//            DESTINATION_PATH.getColumnName(),
//            CAMERA_MODEL.getColumnName(),
//            USER.getColumnName(),
//            ORIENTATION.getColumnName(),
//            BAD.getColumnName(),
//            GOOD.getColumnName(),
//            CONFIRMED.getColumnName(),
//            COPIED.getColumnName(),
//            IGNORED.getColumnName(),
//            MODIFIED.getColumnName(),
//            SUGGESTED.getColumnName(),
//            IMAGE.getColumnName(),
//            RAW.getColumnName(),
//            VIDEO.getColumnName(),
//            TIME_SHIFT.getColumnName(),
//            DATE.getColumnName(),
//            SIZE.getColumnName(),
//            SHA256_CHECKSUM.getColumnName(),
//            TABLE_DUPLICATED.getColumnName(),
//            TAGS.getColumnName(),
//            EVENT.getColumnName(),
//            LOCATION.getColumnName(),
//            IMAGE_DIFFERENCE_HASH.getColumnName(),
//            THUMB_OFFSET.getColumnName(),w
//            THUMB_LENGTH.getColumnName(),
//            FILEHISTORIES.getColumnName()
//    };


    public static String getCreateTableSQL(String tableName) {
        StringBuilder createTableSQLBuilder = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " (");

        boolean first = true;
        for (FileInfoEnum fileInfoEnum : getValuesInBindingOrder()) {
            Messages.sprintf("getCreateTableSQL e: " + fileInfoEnum.getColumnName());
            if (!first) {
                createTableSQLBuilder.append(", ");
            }
            createTableSQLBuilder.append(fileInfoEnum.getColumnDefinition());
            first = false;
        }
        createTableSQLBuilder.append(")");

        Messages.sprintf("create table sql: " + createTableSQLBuilder);

        return createTableSQLBuilder.toString();
    }

    public static String getAllFileInfoColumnNames() {
        StringBuilder columnNamesBuilder = new StringBuilder();
        for (int i = 0; i < BINDING_ORDER.length; i++) {
            if (i > 0) {
                columnNamesBuilder.append(", ");
            }
            columnNamesBuilder.append(BINDING_ORDER[i]);
        }
        Messages.sprintf("Binding-order columns: " + columnNamesBuilder);
        return columnNamesBuilder.toString();
    }

    public static FileInfoEnum[] getValuesInBindingOrder() {
        FileInfoEnum[] orderedValues = new FileInfoEnum[BINDING_ORDER.length];
        for (int i = 0; i < BINDING_ORDER.length; i++) {
            for (FileInfoEnum enumValue : values()) {
                if (enumValue.getColumnName().equals(BINDING_ORDER[i])) {
                    orderedValues[i] = enumValue;
                    break;
                }
            }
        }

        return orderedValues;
    }

    private static final Map<FileInfoEnum, Integer> BIND_INDEX_MAP = createBindIndexMap();

    private static Map<FileInfoEnum, Integer> createBindIndexMap() {
        Map<FileInfoEnum, Integer> indexes = new EnumMap<>(FileInfoEnum.class);
        FileInfoEnum[] orderedValues = getValuesInBindingOrder();

        for (int i = 0; i < orderedValues.length; i++) {
            indexes.put(orderedValues[i], i + 1);
        }

        return Collections.unmodifiableMap(indexes);
    }

    public static Map<FileInfoEnum, Integer> getBindIndexMap() {
        return BIND_INDEX_MAP;
    }

    public int getBindIndex() {
        Integer index = BIND_INDEX_MAP.get(this);
        if (index == null) {
            throw new IllegalStateException("Column is missing from BINDING_ORDER: " + this);
        }
        return index;
    }

    public static int getBindingColumnCount() {
        return BINDING_ORDER.length;
    }

    public static String getInsertPlaceholders() {
        StringBuilder placeholders = new StringBuilder();

        for (int i = 0; i < getBindingColumnCount(); i++) {
            if (i > 0) {
                placeholders.append(", ");
            }
            placeholders.append("?");
        }

        return placeholders.toString();
    }
}