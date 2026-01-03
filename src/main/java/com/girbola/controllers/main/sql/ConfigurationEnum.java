package com.girbola.controllers.main.sql;

import com.girbola.messages.Messages;

public enum ConfigurationEnum {

    ID("id", "INTEGER PRIMARY KEY"),
    BETTER_THUMBNAIL_QUALITY("betterQualityThumbs", "BOOLEAN"),
    CONFIRM_ON_EXIT("confirmOnExit", "BOOLEAN"),
    ID_COUNTER("id_counter", "INTEGER UNIQUE"),
    SHOW_FULL_PATH("showFullPath", "BOOLEAN"),
    SHOW_HINTS("showHints", "BOOLEAN"),
    SHOW_TOOLTIPS("showTooltips", "BOOLEAN"),
    CURRENTTHEME("currentTheme", "TEXT"),
    VLC_PATH("vlcPath", "TEXT"),
    VLC_SUPPORT("vlcSupport", "BOOLEAN"),
    SAVE_DATA_AS_HD("saveDataToHD", "BOOLEAN"),
    WINDOW_START_POSITION_X("windowStartPosX", "DOUBLE"),
    WINDOW_START_POSITION_Y("windowStartPosY", "DOUBLE"),
    WINDOW_START_WIDTH("windowStartWidth", "DOUBLE"),
    WINDOW_START_HEIGHT("windowStartHeight", "DOUBLE"),
    IMAGE_VIEW_X_POSITION("imageViewXPos", "DOUBLE"),
    IMAGE_VIEW_Y_POSITION("imageViewYPos", "DOUBLE"),
    WORK_DIR_SERIAL_NUMBER("workDirSerialNumber", "TEXT"),
    WORK_DIR("workDir", "TEXT");

    private final String columnName;
    private final String sqlType;

    ConfigurationEnum(String columnName, String sqlType) {
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

    public static String getAllColumnNames() {
        int counter = 0;
        Messages.sprintf("##################################### values count: " + values().length);
        StringBuilder columnNamesBuilder = new StringBuilder();

        for (ConfigurationEnum e : values()) {
            if (!columnNamesBuilder.isEmpty()) {
                columnNamesBuilder.append(", ");
                counter++;
            }
            columnNamesBuilder.append(e.getColumnName());
            Messages.sprintf("eeeeeeeeeeeeeeeeee: " + e.columnName);
        }

        return columnNamesBuilder.toString();
    }

}
