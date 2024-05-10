package com.girbola.controllers.main;

import common.utils.Conversion;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;

public class TableStatistic {

	private SimpleIntegerProperty totalFilesCopied = new SimpleIntegerProperty(0);
	private SimpleLongProperty totalFilesSize = new SimpleLongProperty(0);
	private SimpleIntegerProperty totalFiles = new SimpleIntegerProperty(0);

	private Label totalFilesCopied_lbl;
	private Label totalFilesSize_lbl;
	private Label totalFilesTotal_lbl;

	/**
	 * Represents a Table Statistic object that updates the labels with the total files copied, total file size, and total files.
	 *
	 * @param aTotalFilesCopied_lbl The label to display the total number of files copied.
	 * @param aTotalFilesSize_lbl The label to display the total size of the files.
	 * @param aTotalFilesTotal_lbl The label to display the total number of files.
	 */
	public TableStatistic(Label aTotalFilesCopied_lbl, Label aTotalFilesSize_lbl, Label aTotalFilesTotal_lbl) {
		this.totalFilesCopied_lbl = aTotalFilesCopied_lbl;
		this.totalFilesSize_lbl = aTotalFilesSize_lbl;
		this.totalFilesTotal_lbl = aTotalFilesTotal_lbl;

		this.totalFilesCopied_lbl.textProperty().bind(totalFilesCopied.asString());
		totalFilesSize.addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				long value = (long) newValue;
				Platform.runLater(() -> {
					totalFilesSize_lbl.setText("" + Conversion.convertToSmallerConversion(value));
				});
			}
		});
		this.totalFilesTotal_lbl.textProperty().bind(totalFiles.asString());

	}

	public SimpleIntegerProperty totalFilesCopied_property() {
		return totalFilesCopied;
	}

	public synchronized void setTotalFilesCopied(int totalFilesCopied) {
		this.totalFilesCopied.set(totalFilesCopied);
	}

	public SimpleLongProperty totalFilesSize_property() {
		return totalFilesSize;
	}

	public synchronized void setTotalFilesSize(long totalFilesSize) {
		this.totalFilesSize.set(totalFilesSize);
	}

	public SimpleIntegerProperty totalFiles_property() {
		return totalFiles;
	}

	public synchronized void setTotalFiles(int totalFiles) {
		this.totalFiles.set(totalFiles);
	}
}
