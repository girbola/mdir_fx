/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.datefixer;

import com.girbola.Main;
import com.girbola.configuration.GUIPrefs;
import com.girbola.controllers.loading.LoadingProcessTask;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import common.utils.ui.ScreenUtils;
import common.utils.ui.UI_Tools;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.concurrent.atomic.AtomicInteger;

import static com.girbola.messages.Messages.errorSmth;
import static com.girbola.messages.Messages.sprintf;
import static com.girbola.misc.Misc.getLineNumber;

public class AddToGridPane2 extends Task<Integer> {

	private final String ERROR = AddToGridPane2.class.getSimpleName();
	// private final GridPane gridPane;

	private AtomicInteger counter = new AtomicInteger(1);
	private AtomicInteger x = new AtomicInteger(0);
	private AtomicInteger y = new AtomicInteger(0);

	private ObservableList<Node> list;
	private Model_datefix model_datefix;

	private int size;
	private LoadingProcessTask loadingProcess_Task;

	public AddToGridPane2(Model_datefix aModel_dateFix, ObservableList<Node> aList,
			LoadingProcessTask loading_Process_Task) {
		Messages.sprintf("AddToGridPane2 started: " + aList.size());
		this.model_datefix = aModel_dateFix;
		this.list = aList;
		this.size = aList.size();
		this.loadingProcess_Task = loading_Process_Task;
		model_datefix.getGridPane().getChildren().clear();
		sprintf("total nodes: " + model_datefix.getAllNodes().size() + " aGridPane list size is after clearing: "
				+ list.size());

		model_datefix.getGridPane().getRowConstraints().removeAll(model_datefix.getGridPane().getRowConstraints());
		model_datefix.getGridPane().getColumnConstraints()
				.removeAll(model_datefix.getGridPane().getColumnConstraints());
		setGridPane_Constraints();
		loading_Process_Task.setMessage("Adding images");
	}

	private Label createText(int i) {
		Label label = new Label("" + i);
		label.getStyleClass().add("imageNumber");
		label.setId("imageNumber");
		label.setMouseTransparent(true);
		return label;
	}

	// private AtomicInteger counter = new AtomicInteger(list.size());
	@Override
	protected Integer call() throws Exception {

		for (Node node : list) {
			if (node instanceof VBox && node.getId().equals("imageFrame")) {

				createNode(node);
				// loadingProcess_Task.getProgressBar().setProgress((double) counter.get());

			}
		}
		return null;
	}

	private void createNode(Node node) {
		StackPane sp = (StackPane) node.lookup("#stackPane");
		Label old_text = (Label) sp.lookup("#imageNumber");
		if (old_text == null) {
			Label imageNumber = createText(counter.get());
			sp.getChildren().add(imageNumber);
			StackPane.setAlignment(imageNumber, Pos.TOP_RIGHT);
		} else {
			old_text.setText("" + counter.get());
		}

		model_datefix.getGridPane().add(node, x.get(), y.get());
		if (x.get() >= model_datefix.getImagesPerLine() - 1) {
			x.set(0);
			y.incrementAndGet();
		} else {
			x.incrementAndGet();
		}
		counter.incrementAndGet();

	}

	private FileInfo getFileInfo(StackPane sp) {
		if (sp.getUserData() != null) {
			return (FileInfo) sp.getUserData();
		}
		return null;
	}

	private void setGridPane_Constraints() {
		int imagesPerLine = defineImagesPerLine();
		model_datefix.setImagesPerLine(imagesPerLine);
		int rows_MAX = (int) Math.ceil((double) size / (double) (imagesPerLine));
		if (rows_MAX == 0) {
			rows_MAX = 1;
		} else if (rows_MAX < 0) {
			errorSmth(ERROR, "", null, getLineNumber(), true);
		}
		UI_Tools.setGridColumnConstraints(model_datefix.getGridPane(), imagesPerLine);
		UI_Tools.setGridRowConstraints(model_datefix.getGridPane(), rows_MAX);
	}

	private int defineImagesPerLine() {
		return (int) Math.floor((double) (ScreenUtils.screenBouds().getWidth()
				- UI_Tools.getScrollBarWidth(model_datefix.getScrollPane())) / GUIPrefs.imageFrame_x);
	}

	@Override
	protected void failed() {
		super.failed();
	}

	@Override
	protected void cancelled() {
		super.cancelled();
	}

	@Override
	protected void succeeded() {
		super.succeeded();
		ContextMenu contextMenu = new ContextMenu();
		MenuItem pickDateTime_Start = new MenuItem("Pick date&time start");
		MenuItem pickDateTime_End = new MenuItem("Pick date&time end");

		Platform.runLater(() -> {
			// TODO BUGI!!!!
			contextMenu.getItems().addAll(pickDateTime_Start, pickDateTime_End);
//				model_datefix.getScrollPane().setVvalue(-1);
//				model_datefix.getScrollPane().setVvalue(0);

			model_datefix.updateAllInfos(model_datefix.getGridPane());
			loadingProcess_Task.closeStage();
		});
		loadingProcess_Task.closeStage();
		sprintf("gridPane height: " + model_datefix.getGridPane().getHeight() + " list size: " + list.size());
		// model_datefix.updateAllInfos();
		model_datefix.getGridPane().addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
			if (event.getButton() == MouseButton.SECONDARY) {
				if (event.getTarget() instanceof VBox && ((Node) event.getTarget()).getId().equals("imageFrame")) {
					VBox vbox = (VBox) event.getTarget();
					FileInfo fileInfo = (FileInfo) vbox.getUserData();
					pickDateTime_Start.setOnAction(event2 -> {
						model_datefix.setDateTime(
								Main.simpleDates.getSdf_ymd_hms_minusDots_default().format(fileInfo.getDate()), true);
					});
					pickDateTime_End.setOnAction(event2 -> {
						model_datefix.setDateTime(
								Main.simpleDates.getSdf_ymd_hms_minusDots_default().format(fileInfo.getDate()), false);
					});
					contextMenu.show(vbox, event.getScreenX(), event.getScreenY());
				}
			} else {
				event.consume();
			}
		});
	}
}
