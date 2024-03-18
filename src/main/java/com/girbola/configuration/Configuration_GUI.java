/*
 @(#)Copyright:  Copyright (c) 2012-2024 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.configuration;

import javafx.beans.property.*;

/**
 * @author Marko Lokka
 */
class Configuration_GUI {
	@Override
	public String toString() {
		return "Configuration_GUI{" +
				"betterQualityThumbs=" + betterQualityThumbs +
				", confirmOnExit=" + confirmOnExit +
				", showHints=" + showHints +
				", showFullPath=" + showFullPath +
				", showTooltips=" + showTooltips +
				", savingThumb=" + savingThumb +
				", tableShow_sortIt=" + tableShow_sortIt +
				", tableShow_sorted=" + tableShow_sorted +
				", tableShow_asItIs=" + tableShow_asItIs +
				", windowStartPosX=" + windowStartPosX +
				", windowStartPosY=" + windowStartPosY +
				", windowStartWidth=" + windowStartWidth +
				", windowStartHeight=" + windowStartHeight +
				", workDir=" + workDir +
				", workDirSerialNumber=" + workDirSerialNumber +
				", themePath='" + themePath + '\'' +
				'}';
	}

	private BooleanProperty betterQualityThumbs = new SimpleBooleanProperty(false);
	private BooleanProperty confirmOnExit = new SimpleBooleanProperty(true);
	private BooleanProperty showHints = new SimpleBooleanProperty(true);
	private BooleanProperty showFullPath = new SimpleBooleanProperty(true);
	private BooleanProperty showTooltips = new SimpleBooleanProperty(true);
	private BooleanProperty savingThumb = new SimpleBooleanProperty(true);

	private BooleanProperty tableShow_sortIt = new SimpleBooleanProperty(true);
	private BooleanProperty tableShow_sorted = new SimpleBooleanProperty(true);
	private BooleanProperty tableShow_asItIs = new SimpleBooleanProperty(true);

	public boolean getTableShow_sortIt() {
		return this.tableShow_sortIt.get();
	}

	public void setTableShow_sortIt(boolean tableShow_sortIt) {
		this.tableShow_sortIt.set(tableShow_sortIt);
	}

	public boolean getTableShow_sorted() {
		return this.tableShow_sorted.get();
	}

	public void setTableShow_sorted(boolean tableShow_sorted) {
		this.tableShow_sorted.set(tableShow_sorted);
	}

	public boolean getTableShow_asItIs() {
		return this.tableShow_asItIs.get();
	}

	public void setTableShow_asItIs(boolean tableShow_asItIs) {
		this.tableShow_asItIs.set(tableShow_asItIs);
	}

	private SimpleDoubleProperty windowStartPosX = new SimpleDoubleProperty(-1);
	private SimpleDoubleProperty windowStartPosY = new SimpleDoubleProperty(-1);

	private SimpleDoubleProperty windowStartWidth = new SimpleDoubleProperty(-1);
	private SimpleDoubleProperty windowStartHeight = new SimpleDoubleProperty(-1);

	private SimpleStringProperty workDir = new SimpleStringProperty("");
	private SimpleStringProperty workDirSerialNumber = new SimpleStringProperty("");

	private String themePath = ThemePath.DARK.getType();

	public SimpleDoubleProperty windowStartPosX_property() {
		return this.windowStartPosX;
	}

	public void setWindowStartPosX(SimpleDoubleProperty windowStartPosX) {
		this.windowStartPosX = windowStartPosX;
	}

	public SimpleDoubleProperty windowStartPosY_property() {
		return this.windowStartPosY;
	}

	public void setWindowStartPosY(SimpleDoubleProperty windowStartPosY) {
		this.windowStartPosY = windowStartPosY;
	}

	public SimpleDoubleProperty windowStartWidth_property() {
		return this.windowStartWidth;
	}

	public void setWindowStartWidth(SimpleDoubleProperty windowStartWidth) {
		this.windowStartWidth = windowStartWidth;
	}

	public SimpleDoubleProperty windowStartHeight_property() {
		return this.windowStartHeight;
	}

	public void setWindowStartPosX(double value) {
		this.windowStartPosX.set(value);
	}

	public void setWindowStartPosY(double value) {
		this.windowStartPosY.set(value);
	}

	public void setWindowStartWidth(double value) {
		this.windowStartWidth.set(value);
	}

	public void setWindowStartHeight(double value) {
		this.windowStartHeight.set(value);
	}

	public double getWindowStartPosX() {
		return this.windowStartPosX.get();
	}

	public double getWindowStartPosY() {
		return this.windowStartPosY.get();
	}

	public double getWindowStartWidth() {
		return this.windowStartWidth.get();
	}

	public double getWindowStartHeight() {
		return this.windowStartHeight.get();
	}

	public boolean isSavingThumb() {
		return this.savingThumb.get();
	}

	public void setSavingThumb(boolean value) {
		this.savingThumb.set(value);
	}

	public BooleanProperty savingThumb_property() {
		return this.savingThumb;
	}

	public BooleanProperty showTooltips_property() {
		return this.showTooltips;
	}

	public boolean isShowTooltips() {
		return this.showTooltips.get();
	}

	public void setShowTooltips(boolean showTooltips) {
		this.showTooltips.set(showTooltips);
	}

	public BooleanProperty showFullPath_property() {
		return this.showFullPath;
	}

	public boolean isShowFullPath() {
		return this.showFullPath.get();
	}

	public void setShowFullPath(boolean showFullPath) {
		this.showFullPath.set(showFullPath);
	}

	public BooleanProperty showHints_properties() {
		return this.showHints;
	}

	public boolean isShowHints() {
		return this.showHints.get();
	}

	public void setShowHints(boolean showHints) {
		this.showHints.set(showHints);
	}

	public String getThemePath() {
		return this.themePath;
	}

	public void setThemePath(String themePath) {
		this.themePath = themePath;
//		Configuration_SQL_Utils.saveConfig(Configuration_SQL_Utils.themePath, this.themePath);
	}

	public StringProperty workDir_property() {
		return this.workDir;
	}

	public void setWorkDirSerialNumber(String value) {
		this.workDirSerialNumber.set(value);
	}

	public String getWorkDirSerialNumber() {
		return this.workDirSerialNumber.get();
	}

	public String getWorkDir() {
		return this.workDir.get();
	}

	public void setWorkDir(String workDir) {
		System.err.println("setWorkdDir: " + workDir);
		this.workDir.set(workDir);
	}

	public BooleanProperty confirmOnExit_property() {
		return this.confirmOnExit;
	}

	public boolean isConfirmOnExit() {
		return this.confirmOnExit.get();
	}

	public void setConfirmOnExit(boolean confirmOnExit) {
		this.confirmOnExit.set(confirmOnExit);
	}

	/**
	 * 
	 * @return
	 */
	public BooleanProperty betterQualityThumbs_property() {
		return this.betterQualityThumbs;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isBetterQualityThumbs() {
		return this.betterQualityThumbs.get();
	}

	public final void setBetterQualityThumbs(boolean value) {
		this.betterQualityThumbs.set(value);
	}

}
