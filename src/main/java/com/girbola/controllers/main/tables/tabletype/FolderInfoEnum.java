package com.girbola.controllers.main.tables.tabletype;

import com.girbola.fileinfo.FileInfoEnum;
import com.girbola.messages.Messages;

public enum FolderInfoEnum {

    ID("id"),
    STATUS("status"),
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
            FolderInfoEnum.TABLE_TYPE.getColumnName()
    };

    FolderInfoEnum(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnName() {
        return columnName;
    }

//    public static String getCreateTableSQL(String tableName) {
//        StringBuilder createTableSQLBuilder = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " (");
//
//        boolean first = true;
//        for (FolderInfoEnum e : values()) {
//            if (!first) {
//                createTableSQLBuilder.append(", ");
//            }
//            createTableSQLBuilder.append(e.getColumnDefinition());
//            first = false;
//        }
//        createTableSQLBuilder.append(")");
//
//        Messages.sprintf("create table sql: " + createTableSQLBuilder);
//
//        return createTableSQLBuilder.toString();
//    }

//    public static String getAllColumnFolderInfoColumnNames() {
//        StringBuilder columnNamesBuilder = new StringBuilder();
//        for (int i = 0; i < BINDING_ORDER.length; i++) {
//            if (i > 0) {
//                columnNamesBuilder.append(", ");
//            }
//            columnNamesBuilder.append(BINDING_ORDER[i]);
//        }
//        Messages.sprintf("Binding-order columns: " + columnNamesBuilder);
//        return columnNamesBuilder.toString();
//    }
}