
package com.girbola.configuration;


public enum Configuration_Type {

    SHOWHINTS("showHints"),
    SAVETHUMBS("savingThumbs"),
    WORKDIR("workDir"),
    THEMEPATH("currentTheme"),
    VLCPATH("vlcPath"),
    SAVEFOLDER("saveFolder"),
    CONFIRMONEXIT("confirmOnExit"),
    SHOWFULLPATH("showFullPath"),
    VLCSUPPORT("vlcSupport"),
    ID_COUNTER("id_counter"),
    SHOWTOOLTIPS("showTooltips"),
    BETTERQUALITYTHUMBS("betterQualityThumbs"),
    SAVEDATATOHD("saveDataToHD"),
    WINDOW_START_POS_X("windowStartPosX"),
    WINDOW_START_POS_Y("windowStartPosY"),
    WINDOW_START_WIDTH("windowStartWidth"),
    WINDOW_START_HEIGTH("windowStartHeigth"),
    IMAGEVIEW_X_POS("imageViewXPos"),
    IMAGEVIEW_Y_POS("imageViewYPos"),
    WORKDIR_SERIAL_NUMBER("workDirSerialNumber"),
    TABLE_SHOW_SORT_IT("tableShow_sortIt"),
    TABLE_SHOW_SORTED("tableShow_sorted"),
    TABLE_SHOW_ASITIS("tableShow_asItIs");

    private String type;

    Configuration_Type(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

}
