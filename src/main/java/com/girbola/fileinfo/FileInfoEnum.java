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

    public static String getCreateTableSQL(String tableName) {
        StringBuilder createTableSQLBuilder = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " (");

        boolean first = true;
        for (FileInfoEnum e : values()) {
            Messages.sprintf("creating fileInfo table " + e);
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

    public static String getAllColumnNames() {
        int counter = 0;
        Messages.sprintf("##################################### values count: " + values().length);
        StringBuilder columnNamesBuilder = new StringBuilder();

        for (FileInfoEnum e : values()) {
            Messages.sprintf("fileinfo enum value: " + e.toString());
            if (!columnNamesBuilder.isEmpty()) {
                columnNamesBuilder.append(", ");
                counter++;
            }
            columnNamesBuilder.append(e.getColumnName());
            //Messages.sprintf("C::::::::::::::::::: " + counter + " --- eee: " + columnNamesBuilder);
        }
    Messages.sprintf("----------------columnNamesBuilder: " + columnNamesBuilder);
        return columnNamesBuilder.toString();
    }
}