package com.girbola.controllers.main.sql;

import com.girbola.messages.Messages;

public enum ConfigurationEnum {

    BETTER_THUMBNAIL_QUALITY("betterQualityThumbs", "BOOLEAN"),
    CONFIRM_ON_EXIT("confirmOnExit", "BOOLEAN"),
    CURRENTTHEME("currentTheme", "TEXT"),
    ID("id", "INTEGER PRIMARY KEY"),
    ID_COUNTER("id_counter", "INTEGER UNIQUE"),
    IMAGE_VIEW_X_POSITION("imageViewXPos", "DOUBLE"),
    IMAGE_VIEW_Y_POSITION("imageViewYPos", "DOUBLE"),
    SAVE_DATA_AS_HD("saveDataToHD", "BOOLEAN"),
    SHOW_FULL_PATH("showFullPath", "BOOLEAN"),
    SHOW_HINTS("showHints", "BOOLEAN"),
    SHOW_TOOLTIPS("showTooltips", "BOOLEAN"),
    VLC_PATH("vlcPath", "TEXT"),
    VLC_SUPPORT("vlcSupport", "BOOLEAN"),
    WINDOW_START_HEIGHT("windowStartHeight", "DOUBLE"),
    WINDOW_START_POSITION_X("windowStartPosX", "DOUBLE"),
    WINDOW_START_POSITION_Y("windowStartPosY", "DOUBLE"),
    WINDOW_START_WIDTH("windowStartWidth", "DOUBLE"),
    WORK_DIR("workDir", "TEXT"),
    WORK_DIR_SERIAL_NUMBER("workDirSerialNumber", "TEXT");

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
        }

        return columnNamesBuilder.toString();
    }

}
