/*
 @(#)Copyright:  Copyright (c) 2012-2019 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.imageViewer;

import static com.girbola.Main.bundle;
import static com.girbola.Main.conf;
import static com.girbola.messages.Messages.sprintf;
import static com.girbola.misc.Misc.getLineNumber;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.girbola.filelisting.ValidatePathUtils;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;

import common.utils.FileUtils;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
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
import javafx.stage.Stage;

public class ImageViewerController {

	private final static String ERROR = ImageViewerController.class.getSimpleName();
	
	@FXML
	private AnchorPane anchor_main;

	@FXML
	private HBox bottom_controls;
	@FXML
	private StackPane stackPane;

	// private AnchorPane anchorPane;
	@FXML
	private HBox top_bar;

	@FXML
	private AnchorPane image_anchor;
	@FXML
	private Button view_next;
	@FXML
	private Button view_prev;

	private final int zoomPos_x = 64;
	private double zoomPos_y = 64;

	private Image image;
	private Path path;
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
	private Button close;
	// private VBox viewer_root;
	// private ScrollPane viewer_scrollPane;
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
		// Stage stage = (Stage) stage;
		imageView.setImage(null);
		stage.close();
		// Platform.exit();
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
			}
		});
	}

	private void loadImage(Path p) {
		image = new Image(p.toUri().toString(), true);
		imageView.setImage(image);
	}

	@FXML
	private void view_next_action(ActionEvent event) {
		// currentFile = path;

		boolean found = false;
		for (Path p : list) {
			sprintf("ppp: " + p);
			if (currentFile.equals(p)) {
				found = true;
				sprintf("File found: " + p);
			}
			if (found) {
				try {
					if (!p.equals(currentFile)) {
						if (ValidatePathUtils.validFile(p)) {
							if (FileUtils.supportedImage(p)) {
								sprintf("loading image: " + p);
								loadImage(p);
								found = false;
								currentFile = p;
								break;
							}
						}
					}
				} catch (IOException ex) {
					Logger.getLogger(ImageViewerController.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
	}

	@FXML
	private void view_prev_action(ActionEvent event) {
	}

	@FXML
	private void zoom_in_action(ActionEvent event) {
		zoomIn();
	}

	@FXML
	private void zoom_out_action(ActionEvent event) {
		zoomOut();
	}

	private List<Path> list = new ArrayList<>();

	public void init(Path path, Scene scene, Stage stage) {
		this.path = path;
		this.scene = scene;
		this.stage = stage;
		currentFile = path;
		DirectoryStream<Path> ds = null;
		try {
			ds = Files.newDirectoryStream(path.getParent(), FileUtils.filter_directories);
		} catch (IOException ex) {
			Logger.getLogger(ImageViewerController.class.getName()).log(Level.SEVERE, null, ex);
		}
		for (Path p : ds) {
			try {
				if (ValidatePathUtils.validFile(p)) {
					if (FileUtils.supportedImage(p) || FileUtils.supportedRaw(p)) {
						list.add(p);
					}
				}
			} catch (IOException ex) {
				Logger.getLogger(ImageViewerController.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

		imageDimensions(path);

		imageView.setPreserveRatio(true);
		// imageView.setFitWidth(300);
		// imageView.setFitHeight(300);

		// imageView.fitWidthProperty().bind(stage.widthProperty());
		// imageView.fitHeightProperty().bind(stage.heightProperty());
		// stage.widthProperty().addListener(new ChangeListener<Number>() {
		// @Override
		// public void changed(ObservableValue<? extends Number> observable, Number
		// oldValue, Number newValue) {
		// imageView.setFitWidth((double) newValue);
		// }
		// });
		stage.heightProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				imageView.setFitHeight((double) newValue);
			}
		});
		if (IMAGE_WIDTH >= conf.getScreenBounds().getWidth()) {
			stage.setWidth(400);
			if (ratio == 0) {
				sprintf("IMAGE_WIDTH ratio were 0!!!");
				stage.setHeight(400 / 1.5);
			} else {
				stage.setHeight(400 / ratio);
			}

		}
		if (IMAGE_HEIGHT >= conf.getScreenBounds().getHeight()) {
			stage.setHeight(400);
			if (ratio == 0) {
				sprintf("IMAGE_HEIGHT ratio were 0!!!");
				stage.setHeight(400 * 1.5);
			} else {
				stage.setHeight(400 * ratio);
			}
			// stage.setWidth(400 * ratio);

		}
		image = new Image(path.toFile().toURI().toString(), true);

		imageView.setImage(image);
		// imageView.fitHeightProperty().bind(stage.heightProperty());

		stage.widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				// stage.setX((conf.getScreenMaxBounds().getWidth() / 2) -
				// (anchor_main.getWidth() / 2));
				sprintf("imageView width: " + imageView.getBoundsInLocal().getWidth() + " imageView height: "
						+ imageView.getBoundsInLocal().getHeight() + " anchor_main.getWidth(); " + anchor_main.getWidth() + " bottom_controls width: "
						+ bottom_controls.getWidth());
				sprintf("image_anchor width: " + image_anchor.getWidth() + " image_anchor height: " + image_anchor.getHeight());
				// anchor_main.setPrefWidth((double) newValue);
			}
		});
		stage.heightProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				// stage.setY((conf.getScreenMaxBounds().getHeight() / 2) -
				// (anchor_main.getHeight() / 2));
				sprintf("imageView width: " + imageView.getBoundsInLocal().getWidth() + " imageView height: "
						+ imageView.getBoundsInLocal().getHeight() + " anchor_main.getWidth(); " + anchor_main.getWidth() + " bottom_controls width: "
						+ bottom_controls.getWidth());
				sprintf("image_anchor width: " + image_anchor.getWidth() + " image_anchor height: " + image_anchor.getHeight());
				// stage.setHeight(bottom_controls.getHeight() + image_anchor.getHeight() +
				// top_bar.getHeight());
				// anchor_main.setPrefHeight((double) newValue);

			}
		});

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
				// image_anchor.getChildren().add(new Text("JOOOOOOOOOO"));
				top_bar.setVisible(true);
				bottom_controls.setVisible(true);
			}
		});
		scene.setOnMouseExited(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				// image_anchor.getChildren().add(new Text("iiiiiiiiiiiiiiiiiiiiiiii"));
				top_bar.setVisible(false);
				bottom_controls.setVisible(false);

			}
		});
		dragWindow();
		initKeyEvent();
	}

	public void imageDimensions(Path path1) {
		sprintf("FIX THISSSSS!!!!!!!!! <> ImageDimensions path is: " + path1);
		//TODO Fix image viewing image types.
		BufferedImage buff = null;
		try {
			Messages.sprintf("Probe type is" + Files.probeContentType(path1));
			buff = ImageIO.read(path1.toFile());
		} catch (IOException ex) {
			Messages.errorSmth(ERROR, bundle.getString("cannotReadImage"), ex, Misc.getLineNumber(), false);
			Logger.getLogger(ImageViewerController.class.getName()).log(Level.SEVERE, null, ex);
			return;
		}
		if (buff == null) {
			Messages.errorSmth(ERROR, bundle.getString("cannotReadImage"), null, getLineNumber(), true);
		}
		// Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
		try {
			IMAGE_WIDTH = buff.getWidth();
			IMAGE_HEIGHT = buff.getHeight();
		} catch (NullPointerException ex) {
			Messages.errorSmth(ERROR, bundle.getString("mediaCorruptedNotSupported"), ex, Misc.getLineNumber(), false);

		}
		if (IMAGE_WIDTH == 0 || IMAGE_HEIGHT == 0) {
			sprintf("can't get image size");
			Messages.errorSmth(ERROR, bundle.getString("cannotReadImage"), null, getLineNumber(), true);
		}
		sprintf("Image org width is = " + IMAGE_WIDTH + " height is = " + IMAGE_HEIGHT);
		ratio = IMAGE_WIDTH / IMAGE_HEIGHT;
		zoomPos_y = Math.floor(zoomPos_x / ratio);
	}

	public void init2(Path path, Scene scene, Stage stage) {
		this.path = path;
		this.scene = scene;
		this.stage = stage;
		currentFile = path;
		DirectoryStream<Path> ds = null;
		try {
			ds = Files.newDirectoryStream(path.getParent());
		} catch (IOException ex) {
			Logger.getLogger(ImageViewerController.class.getName()).log(Level.SEVERE, null, ex);
		}
		for (Path p : ds) {
			try {
				if (ValidatePathUtils.validFile(p)) {
					if (FileUtils.supportedImage(p) || FileUtils.supportedRaw(p)) {
						list.add(p);
					}
				}
			} catch (IOException ex) {
				Logger.getLogger(ImageViewerController.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		image = new Image(path.toUri().toString(), true);

		// sprintf("image.getWidth(); " + image.getWidth() + " image.getHeight(); " +
		// image.getHeight());
		image.widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				sprintf("Image width is: " + newValue + " image height: " + image.getHeight());
			}
		});
		image.heightProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				sprintf("Image height is: " + newValue + " image width: " + image.getWidth());
			}
		});

		imageView.setImage(image);

		imageView.fitWidthProperty().bind(image.widthProperty());
		imageView.fitHeightProperty().bind(image.heightProperty());
		double imageRatio = 0;
		double width = imageView.getBoundsInLocal().getWidth();
		double height = imageView.getBoundsInLocal().getHeight();
		sprintf("width is: " + width + " height: " + height);
		if (width > height) {
			imageRatio = width / height;
			sprintf("imageratio is hor: " + imageRatio);
		} else if (height > width) {
			imageRatio = height / width;
			sprintf("imageratio is ver: " + imageRatio);
		} else {
			imageRatio = 1;
			sprintf("imageratio is box: " + imageRatio);
		}

		// stage.setHeight(300);
		stage.widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {

				// image_anchor.setPrefWidth((double) newValue);
				// double width = (conf.getScreenMaxBounds().getWidth() / 2) -
				// (anchor_main.getWidth() / 2);
				stage.setX((conf.getScreenBounds().getWidth() / 2) - (anchor_main.getWidth() / 2));
				image_anchor.setPrefWidth(stage.getWidth());
				image_anchor.setPrefHeight(bottom_controls.getHeight() + image_anchor.getHeight() + top_bar.getHeight());
				sprintf("imageView width: " + imageView.getBoundsInLocal().getWidth() + " imageView height: "
						+ imageView.getBoundsInLocal().getHeight() + " anchor_main.getWidth(); " + anchor_main.getWidth() + " bottom_controls width: "
						+ bottom_controls.getWidth());
				sprintf("image_anchor width: " + image_anchor.getWidth() + " image_anchor height: " + image_anchor.getHeight());
			}
		});
		stage.heightProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				// image_anchor.setPrefHeight((double) newValue);
				// double height = (conf.getScreenMaxBounds().getHeight() / 2) -
				// (anchor_main.getHeight() / 2);
				stage.setY((conf.getScreenBounds().getHeight() / 2) - (anchor_main.getHeight() / 2));
				image_anchor.setPrefHeight(bottom_controls.getHeight() + image_anchor.getHeight() + top_bar.getHeight());
				sprintf("anchor_main.getHeight(); " + anchor_main.getHeight() + " bottom_controls height: " + bottom_controls.getHeight());

			}
		});
		stage.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (!newValue) {
					sprintf("focus lost");
					closeWindow();
				}
			}
		});
		// centerNodeInScrollPane(viewer_scrollPane, imageView);
		initKeyEvent();
	}

	public void clearImage() {
		sprintf("Clearing image");
		imageView.setImage(null);
	}

	public void setPath(Path path) {
		sprintf("New path is: " + path);
		image = new Image(path.toUri().toString(), true);
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
		sprintf("btm: " + bottom_controls.getHeight() + " i_a : " + image_anchor.getHeight() + " tp: " + top_bar.getHeight());
		sprintf("zoomPos: " + zoomPos_x + " iv width: " + getImageView_width());
	}

}
