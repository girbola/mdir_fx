
package com.girbola.configuration;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


class ConfigurationGui {

    @Override
    public String toString() {
        return "ConfigurationGui{" +
                "betterQualityThumbs=" + isBetterQualityThumbs() +
                ", confirmOnExit=" + isConfirmOnExit() +
                ", showHints=" + isShowHints() +
                ", showFullPath=" + isShowFullPath() +
                ", showTooltips=" + isShowTooltips() +
                ", savingThumb=" + isSavingThumb() +
                ", tableShowSortIt=" + getTableShowSortIt() +
                ", tableShowSorted=" + getTableShowSorted() +
                ", tableShowAsItIs=" + getTableShowAsItIs() +
                ", windowStartPosX=" + getWindowStartPosX() +
                ", windowStartPosY=" + getWindowStartPosY() +
                ", windowStartWidth=" + getWindowStartWidth() +
                ", windowStartHeight=" + getWindowStartHeight() +
                ", workDir=" + (getWorkDir() != null ? getWorkDir() : "null") +
                ", workDirSerialNumber=" + (getWorkDirSerialNumber() != null ? getWorkDirSerialNumber() : "null") +
                ", currentTheme='" + (getThemePath() != null ? getThemePath() : "null") + '\'' +
                '}';
    }

    private BooleanProperty betterQualityThumbs = new SimpleBooleanProperty(false);
    private BooleanProperty confirmOnExit = new SimpleBooleanProperty(true);
    private BooleanProperty savingThumb = new SimpleBooleanProperty(true);
    private BooleanProperty showFullPath = new SimpleBooleanProperty(true);
    private BooleanProperty showHints = new SimpleBooleanProperty(true);
    private BooleanProperty showTooltips = new SimpleBooleanProperty(true);
    private BooleanProperty tableShowAsItIs = new SimpleBooleanProperty(true);
    private BooleanProperty tableShowSorted = new SimpleBooleanProperty(true);
    private BooleanProperty tableShowSortIt = new SimpleBooleanProperty(true);
    private SimpleDoubleProperty windowStartHeight = new SimpleDoubleProperty(-1);
    private SimpleDoubleProperty windowStartPosX = new SimpleDoubleProperty(-1);
    private SimpleDoubleProperty windowStartPosY = new SimpleDoubleProperty(-1);
    private SimpleDoubleProperty windowStartWidth = new SimpleDoubleProperty(-1);
    private SimpleStringProperty workDir = new SimpleStringProperty("");
    private SimpleStringProperty workDirSerialNumber = new SimpleStringProperty("");
    private String currentTheme = ThemePath.DARK.getType();
    private String themePath = "/themes/" + ThemePath.DARK.getType() + "/";

    public BooleanProperty betterQualityThumbs_property() {return this.betterQualityThumbs;}
    public BooleanProperty confirmOnExitProperty() {return this.confirmOnExit;}
    public BooleanProperty savingThumbProperty() {return this.savingThumb;}
    public BooleanProperty showFullPathProperty() {return this.showFullPath;}
    public BooleanProperty showHintsProperties() {return this.showHints;}
    public BooleanProperty showTooltipsProperty() {return this.showTooltips;}
    public SimpleDoubleProperty windowStartPosXProperty() {return this.windowStartPosX;}
    public SimpleDoubleProperty windowStartPosYProperty() {return this.windowStartPosY;}
    public SimpleDoubleProperty windowStartWidthProperty() {return this.windowStartWidth;}
    public SimpleDoubleProperty windowStartHeightProperty() {return this.windowStartHeight;}
    public StringProperty workDirProperty() {return this.workDir;}

    public boolean getTableShowAsItIs() {return this.tableShowAsItIs.get();}
    public boolean getTableShowSorted() {return this.tableShowSorted.get();}
    public boolean getTableShowSortIt() {return this.tableShowSortIt.get();}
    public boolean isBetterQualityThumbs() {return this.betterQualityThumbs.get();}
    public boolean isConfirmOnExit() {return this.confirmOnExit.get();}
    public boolean isSavingThumb() {return this.savingThumb.get();}
    public boolean isShowFullPath() {return this.showFullPath.get();}
    public boolean isShowHints() {return this.showHints.get();}
    public boolean isShowTooltips() {return this.showTooltips.get();}
    public double getWindowStartHeight() {return this.windowStartHeight.get();}
    public double getWindowStartPosX() {return this.windowStartPosX.get();}
    public double getWindowStartPosY() {return this.windowStartPosY.get();}
    public double getWindowStartWidth() {return this.windowStartWidth.get();}
    public String getCurrentTheme() {return currentTheme;}
    public String getThemePath() {return this.themePath;}
    public String getWorkDir() {return this.workDir.get();}
    public String getWorkDirSerialNumber() {return this.workDirSerialNumber.get();}
    public void setBetterQualityThumbs(boolean value) {this.betterQualityThumbs.set(value);}
    public void setConfirmOnExit(boolean confirmOnExit) {this.confirmOnExit.set(confirmOnExit);}
    public void setCurrentTheme(String currentTheme) {this.currentTheme = currentTheme;}
    public void setSavingThumb(boolean value) {this.savingThumb.set(value);}
    public void setShowFullPath(boolean showFullPath) {this.showFullPath.set(showFullPath);}
    public void setShowHints(boolean showHints) {this.showHints.set(showHints);}
    public void setShowTooltips(boolean showTooltips) {this.showTooltips.set(showTooltips);}
    public void setTableShowAsItIs(boolean tableShowAsItIs) {this.tableShowAsItIs.set(tableShowAsItIs);}
    public void setTableShowSorted(boolean tableShowSorted) {this.tableShowSorted.set(tableShowSorted);}
    public void setTableShowSortIt(boolean tableShowSortIt) {this.tableShowSortIt.set(tableShowSortIt);}
    public void setThemePath(String themePath) {this.themePath = themePath;}
    public void setWindowStartHeight(double value) {this.windowStartHeight.set(value);}
    public void setWindowStartPosX(double value) {this.windowStartPosX.set(value);}
    public void setWindowStartPosX(SimpleDoubleProperty windowStartPosX) {this.windowStartPosX = windowStartPosX;}
    public void setWindowStartPosY(double value) {this.windowStartPosY.set(value);}
    public void setWindowStartPosY(SimpleDoubleProperty windowStartPosY) {this.windowStartPosY = windowStartPosY;}
    public void setWindowStartWidth(double value) {this.windowStartWidth.set(value);}
    public void setWindowStartWidth(SimpleDoubleProperty windowStartWidth) {this.windowStartWidth = windowStartWidth;}
    public void setWorkDir(String workDir) {this.workDir.set(workDir);}
    public void setWorkDirSerialNumber(String value) {this.workDirSerialNumber.set(value);}

}
