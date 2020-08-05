package com.girbola.controllers.main;

import com.girbola.controllers.main.tables.tabletype.TableType;

import common.utils.Conversion;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;

public class TableStatistic {

	private Model_main model_main;
	private TableType tableType;
	private SimpleIntegerProperty totalFilesCopied = new SimpleIntegerProperty(0);
	private SimpleLongProperty totalFilesSize = new SimpleLongProperty(0);
	private SimpleIntegerProperty totalFiles = new SimpleIntegerProperty(0);

	private Label allFilesCopied_lbl;
	private Label allFilesSize_lbl;
	private Label allFilesTotal_lbl;

	public TableStatistic(Label allFilesCopied_lbl, Label allFilesSize_lbl, Label allFilesTotal_lbl) {
		this.allFilesCopied_lbl = allFilesCopied_lbl;
		this.allFilesSize_lbl = allFilesSize_lbl;
		this.allFilesTotal_lbl = allFilesTotal_lbl;

		this.allFilesCopied_lbl.textProperty().bind(totalFilesCopied.asString());
//		this.allFilesSize_lbl.textProperty().bind(totalFilesSize.asString());
		totalFilesSize.addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				long value = (long)newValue;
				Platform.runLater(() -> {
					allFilesSize_lbl.setText("" + Conversion.convertToSmallerConversion(value));
				});
			}
		});
		this.allFilesTotal_lbl.textProperty().bind(totalFiles.asString());

	}

	public SimpleIntegerProperty getTotalFilesCopied() {
		return totalFilesCopied;
	}

	public void setTotalFilesCopied(int totalFilesCopied) {
		this.totalFilesCopied.set(totalFilesCopied);
	}

	public SimpleLongProperty getTotalFilesSize() {
		return totalFilesSize;
	}

	public void setTotalFilesSize(long totalFilesSize) {
		this.totalFilesSize.set(totalFilesSize);
	}

	public SimpleIntegerProperty getTotalFiles() {
		return totalFiles;
	}

	public void setTotalFiles(int totalFiles) {
		this.totalFiles.set(totalFiles);
	}
}
