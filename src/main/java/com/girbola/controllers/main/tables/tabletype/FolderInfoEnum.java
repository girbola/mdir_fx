package com.girbola.controllers.main.tables.tabletype;

import java.util.LinkedHashMap;
import java.util.Map;

public enum FolderInfoEnum {

    ID("id", "INTEGER PRIMARY KEY AUTOINCREMENT"),
    STATUS("status", "INTEGER"),
    BAD_FILES("badFiles", "INTEGER"),
    CHANGED("changed", "BOOLEAN"),
    CONFIRMED("confirmed", "INTEGER"),
    CONNECTED("connected", "BOOLEAN"),
    COPIED("copied", "INTEGER"),
    DATE_DIFFERENCE("dateDifference", "DOUBLE"),
    FOLDER_FILES("folderFiles", "INTEGER"),
    FOLDER_IMAGE_FILES("folderImageFiles", "INTEGER"),
    FOLDER_PATH("folderPath", "TEXT"),
    FOLDER_RAW_FILES("folderRawFiles", "INTEGER"),
    FOLDER_SIZE("folderSize", "INTEGER"),
    FOLDER_VIDEO_FILES("folderVideoFiles", "INTEGER"),
    GOOD_FILES("goodFiles", "INTEGER"),
    IGNORED("ignored", "BOOLEAN"),
    JUST_FOLDER_NAME("justFolderName", "TEXT"),
    MAX_DATE("maxDate", "TEXT"),
    MIN_DATE("minDate", "TEXT"),
    STATE("state", "TEXT"),
    SUGGESTED("suggested", "INTEGER"),
    TABLE_TYPE("tableType", "TEXT"),
    WORKDIR_SERIAL_NUMBER("workdirSerialNumber", "TEXT");

    private final String columnName;
    private final String sqlType;

    private static final String[] BINDING_ORDER = {
            FolderInfoEnum.ID.getColumnName(),
            FolderInfoEnum.STATUS.getColumnName(),
            FolderInfoEnum.CHANGED.getColumnName(),
            FolderInfoEnum.CONNECTED.getColumnName(),
            FolderInfoEnum.IGNORED.getColumnName(),
            FolderInfoEnum.DATE_DIFFERENCE.getColumnName(),
            FolderInfoEnum.BAD_FILES.getColumnName(),
            FolderInfoEnum.CONFIRMED.getColumnName(),
            FolderInfoEnum.COPIED.getColumnName(),
            FolderInfoEnum.FOLDER_FILES.getColumnName(),
            FolderInfoEnum.FOLDER_IMAGE_FILES.getColumnName(),
            FolderInfoEnum.FOLDER_RAW_FILES.getColumnName(),
            FolderInfoEnum.FOLDER_VIDEO_FILES.getColumnName(),
            FolderInfoEnum.GOOD_FILES.getColumnName(),
            FolderInfoEnum.SUGGESTED.getColumnName(),
            FolderInfoEnum.FOLDER_SIZE.getColumnName(),
            FolderInfoEnum.JUST_FOLDER_NAME.getColumnName(),
            FolderInfoEnum.FOLDER_PATH.getColumnName(),
            FolderInfoEnum.MAX_DATE.getColumnName(),
            FolderInfoEnum.MIN_DATE.getColumnName(),
            FolderInfoEnum.STATE.getColumnName(),
            FolderInfoEnum.TABLE_TYPE.getColumnName(),
            FolderInfoEnum.WORKDIR_SERIAL_NUMBER.getColumnName()
    };

    FolderInfoEnum(String columnName, String sqlType) {
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

    public static String getAllFolderInfoColumnNames() {
        StringBuilder columnNamesBuilder = new StringBuilder();
        for (int i = 0; i < BINDING_ORDER.length; i++) {
            if (i > 0) {
                columnNamesBuilder.append(", ");
            }
            columnNamesBuilder.append(BINDING_ORDER[i]);
        }
        return columnNamesBuilder.toString();
    }

    public static FolderInfoEnum[] getValuesInBindingOrder() {
        FolderInfoEnum[] orderedValues = new FolderInfoEnum[BINDING_ORDER.length];
        for (int i = 0; i < BINDING_ORDER.length; i++) {
            for (FolderInfoEnum enumValue : values()) {
                if (enumValue.getColumnName().equals(BINDING_ORDER[i])) {
                    orderedValues[i] = enumValue;
                    break;
                }
            }
        }
        return orderedValues;
    }

    public static Map<String, String> getRequiredColumns() {
        Map<String, String> requiredColumns = new LinkedHashMap<>();
        for (FolderInfoEnum column : getValuesInBindingOrder()) {
            requiredColumns.put(column.getColumnName(), column.getSqlType());
        }
        return requiredColumns;
    }
}
