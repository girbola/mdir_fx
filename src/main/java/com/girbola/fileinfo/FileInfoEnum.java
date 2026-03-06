package com.girbola.fileinfo;

import com.girbola.messages.Messages;

public enum FileInfoEnum {
    BAD("bad", "BOOLEAN"),
    CAMERA_MODEL("camera_model", "TEXT"),
    CONFIRMED("confirmed", "BOOLEAN"),
    DESTINATION_PATH("destination_Path", "TEXT"),
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
            BAD.getColumnName(),
            CAMERA_MODEL.getColumnName(),
            CONFIRMED.getColumnName(),
            DESTINATION_PATH.getColumnName(),
            DATE.getColumnName(),
            EVENT.getColumnName(),
            FILEINFO_ID.getColumnName(),
            FILEHISTORIES.getColumnName(),
            GOOD.getColumnName(),
            COPIED.getColumnName(),
            IGNORED.getColumnName(),
            IMAGE.getColumnName(),
            IMAGE_DIFFERENCE_HASH.getColumnName(),
            LOCATION.getColumnName(),
            MODIFIED.getColumnName(),
            ORGPATH.getColumnName(),
            ORGPATH_DRIVE_SERIAL_NUMBER.getColumnName(),
            ORIENTATION.getColumnName(),
            RAW.getColumnName(),
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

    public static String getCreateTableSQL(String tableName) {
        StringBuilder createTableSQLBuilder = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " (");

        boolean first = true;
        for (FileInfoEnum e : values()) {
            if (!first) {
                createTableSQLBuilder.append(", ");
            }
            createTableSQLBuilder.append(e.getColumnDefinition());
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
}