/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.imageViewer;

import static com.girbola.Main.conf;
import static com.girbola.messages.Messages.sprintf;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.girbola.Main;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.rotate.Rotate;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class ImageViewerController {

	private final static String ERROR = ImageViewerController.class.getSimpleName();

//	private List<Path> list = new ArrayList<>();

	private List<FileInfo> fileInfoList;
	private FileInfo currentFileInfo;
	private SimpleIntegerProperty currentImageIndex;

	private final int zoomPos_x = 64;
	private double zoomPos_y = 64;

	private Path currentFile;
	private Scene scene;
	private Stage stage;
	private double ratio = 0;

	private double zoomScale = 1;
	private String orientation;

	private double X_OFFSET;
	private double Y_OFFSET;
	private double layout_x;
	private double layout_y;

	private double IMAGE_WIDTH = 0;
	private double IMAGE_HEIGHT = 0;

	private DoubleProperty C_WIDTH = new SimpleDoubleProperty();
	private DoubleProperty C_HEIGHT = new SimpleDoubleProperty();

	@FXML
	private AnchorPane anchor_main;

	@FXML
	private HBox bottom_controls;
	@FXML
	private StackPane stackPane;

	@FXML
	private HBox top_bar;

	@FXML
	private AnchorPane image_anchor;
	@FXML
	private Button view_next;
	@FXML
	private Button view_prev;

	@FXML
	private Button close;
	@FXML
	private Button zoom_in;
	@FXML
	private Button zoom_out;

	@FXML
	private ImageView imageView;

	@FXML
	private void close_button(ActionEvent event) {
		closeWindow();
	}

	public void closeWindow() {
		imageView.setImage(null);
		stage.close();
	}

	private void dragWindow() {
		scene.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				X_OFFSET = (stage.getX() - event.getScreenX());
				Y_OFFSET = (stage.getY() - event.getScreenY());
				layout_x = (imageView.getLayoutX());
				layout_y = (imageView.getLayoutY());

			}
		});
		scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				stage.setX(event.getScreenX() + X_OFFSET);
				if (event.getScreenY() <= 0) {
					stage.setY(0);
				} else {
					stage.setY(event.getScreenY() + Y_OFFSET);
				}
				sprintf("event.getScreenY(); = " + event.getScreenY());
			}
		});

	}

	private void initKeyEvent() {

		scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == (KeyCode.ESCAPE)) {
					sprintf("Escape pressed");
					closeWindow();
				}
				if (event.getCode() == (KeyCode.MINUS)) {
					sprintf("Minus key pressed");
					zoomOut();
				}
				if (event.getCode() == (KeyCode.PLUS)) {
					sprintf("Plus key pressed");
					zoomIn();
				}
				Messages.sprintf("keyEvent: " + event.getCode());
			}
		});
	}

	@FXML
	private void view_next_action(ActionEvent event) {
		currentImageIndex.set(currentImageIndex.get() + 1);
		if (currentImageIndex.get() > (fileInfoList.size() + 1)) {
			currentImageIndex.set(0);
		}
		FileInfo fileInfo = fileInfoList.get(currentImageIndex.get());
		loadImage(fileInfo);
	}

	private void loadImage(FileInfo fileInfo) {
		if (fileInfo.isImage()) {
			Image image = new Image(new File(fileInfo.getOrgPath()).toURI().toString(), 0, 0, true, true, true);
			image.progressProperty().addListener(new ChangeListener<Number>() {

				@Override
				public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
					if ((double) newValue == 1.0) {
						double imageRatio = (image.getWidth() / image.getHeight());
						if (imageRatio >= 1) { // Horizontal
							double height = Screen.getPrimary().getBounds().getHeight() - 100;
							double width = height / imageRatio;
							stage.setWidth(width);
							stage.setHeight(height);
						} else if (imageRatio < 1) { // Vertical image
							double height = Screen.getPrimary().getBounds().getHeight() - 100;
							double width = height * imageRatio;
							stage.setWidth(width);
							stage.setHeight(height);
						} else { // Square image
							double height = Screen.getPrimary().getBounds().getHeight() - 100;
							double width = height;
							stage.setWidth(width);
							stage.setHeight(height);
						}
						stage.setHeight(Screen.getPrimary().getBounds().getHeight() - 100);
						Messages.sprintf("progressing: " + newValue + " Image width: " + image.getWidth()
								+ " Image heigth: " + image.getHeight());
						imageView.setRotate(0);
						imageView.setRotate(Rotate.rotate(fileInfo.getOrientation()));
						imageView.setImage(image);
					}
				}
			});
		} else if (fileInfo.isVideo()) {
			Messages.warningText("Video viewing is not ready yet");
//			SQL_Utils.loadth
		}
//		image.widthProperty().addListener(new ChangeListener<Number>() {
//
//			@Override
//			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//				stage.setWidth((double) newValue);
//			}
//		});
//		image.heightProperty().addListener(new ChangeListener<Number>() {
//
//			@Override
//			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//				stage.setHeight((double) newValue);
//			}
//		});
	}

	@FXML
	private void view_prev_action(ActionEvent event) {
		currentImageIndex.set(currentImageIndex.get() - 1);
		if (currentImageIndex.get() < 0) {
			Messages.sprintf("min exceeded currentImageIndex.get(): " + currentImageIndex.get());
			currentImageIndex.set(fileInfoList.size());
		}
		Messages.sprintf("currentImageIndex.get(): " + currentImageIndex.get());
		FileInfo fileInfo = fileInfoList.get(currentImageIndex.get());
		loadImage(fileInfo);
	}

	@FXML
	private void zoom_in_action(ActionEvent event) {
		zoomIn();
	}

	@FXML
	private void zoom_out_action(ActionEvent event) {
		zoomOut();
	}

	public void init(List<FileInfo> fileInfoList, FileInfo currentFileInfo, Scene scene, Stage stage) {
		this.fileInfoList = fileInfoList;
		this.currentFileInfo = currentFileInfo;
		this.scene = scene;
		this.stage = stage;
		if (fileInfoList == null) {
			view_next.setVisible(false);
			view_prev.setVisible(false);
			currentImageIndex = new SimpleIntegerProperty(0);
		} else {
			currentImageIndex = new SimpleIntegerProperty(getCurrentNumber(fileInfoList, currentFileInfo));
		}
		imageView.setPreserveRatio(true);

		loadImage(currentFileInfo);
		stage.xProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				Messages.sprintf("stage Xproperty: " + newValue);
				Main.conf.setImageViewXProperty((double) newValue);
			}
		});
		stage.yProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				Messages.sprintf("stage Yproperty: " + newValue);
				Main.conf.setImageViewYProperty((double) newValue);
			}
		});
		for(Screen scr : Screen.getScreens()) {
//			Main.conf.getWindowStartHeight()
//			scr.getBounds().getMaxX();
		}
		stage.setX(Main.conf.getWindowStartPosX());
		stage.setY(Main.conf.getWindowStartPosY());
//		stage.heightProperty().addListener(new ChangeListener<Number>() {
//			@Override
//			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//				imageView.setFitHeight((double) newValue - 100.0);
//			}
//		});
//		stage.widthProperty().addListener(new ChangeListener<Number>() {
//			@Override
//			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//				imageView.setFitWidth((double) newValue - 100.0);
//			}
//		});
		stage.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (!newValue) {
					sprintf("focus lost");
					closeWindow();
				}
			}
		});
		scene.setOnMouseEntered(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				top_bar.setVisible(true);
				bottom_controls.setVisible(true);
			}
		});
		scene.setOnMouseExited(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				top_bar.setVisible(false);
				bottom_controls.setVisible(false);
			}
		});

		imageView.fitWidthProperty().bind(scene.widthProperty());
		imageView.fitHeightProperty().bind(scene.heightProperty());

		stage.setY(0);
		dragWindow();
		initKeyEvent();
	}

	private int getCurrentNumber(List<FileInfo> fileInfoList, FileInfo fileInfoToSearch) {
		AtomicInteger counter = new AtomicInteger();
		for (FileInfo fileInfo : fileInfoList) {
			if (fileInfo.equals(fileInfoToSearch)) {
				return counter.get();
			}
			counter.incrementAndGet();
		}
		return 0;
	}

	public void clearImage() {
		sprintf("Clearing image");
		imageView.setImage(null);
	}

	public double getImageView_width() {
		return imageView.getBoundsInLocal().getWidth();
	}

	public double getImageView_height() {
		return imageView.getBoundsInLocal().getHeight();
	}

	private void zoomIn() {
		if ((stage.getWidth() + zoomPos_x) >= (conf.getScreenBounds().getWidth() - 10)) {
			return;
		}
		if ((stage.getHeight() + zoomPos_x) >= (conf.getScreenBounds().getHeight() - 10)) {
			return;
		}
		stage.setWidth(stage.getWidth() + zoomPos_x);
		stage.setHeight(stage.getHeight() + zoomPos_y);
		sprintf("zoomPos: " + zoomPos_x + " iv width: " + getImageView_width());
	}

	private void zoomOut() {
		if ((stage.getWidth() - zoomPos_x) <= 200) {
			return;
		}
		if ((stage.getHeight() - zoomPos_x) <= 200) {
			return;
		}

		stage.setWidth(stage.getWidth() - zoomPos_x);
		stage.setHeight(stage.getHeight() - zoomPos_y);
		sprintf("btm: " + bottom_controls.getHeight() + " i_a : " + image_anchor.getHeight() + " tp: "
				+ top_bar.getHeight());
		sprintf("zoomPos: " + zoomPos_x + " iv width: " + getImageView_width());
	}

}
