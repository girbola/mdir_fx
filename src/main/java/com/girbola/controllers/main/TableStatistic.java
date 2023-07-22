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
	 * Calculates statistic from tableview
	 * @param aTotalFilesCopied_lbl
	 * @param aTotalFilesSize_lbl
	 * @param aTotalFilesTotal_lbl
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
