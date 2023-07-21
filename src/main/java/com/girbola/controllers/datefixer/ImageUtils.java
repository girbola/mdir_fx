/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.datefixer;

import com.girbola.Main;
import com.girbola.controllers.imageViewer.ImageViewerController;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.vlcj.VLCPlayerController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static com.girbola.Main.bundle;
import static com.girbola.Main.conf;

/**
 *
 * @author Marko Lokka
 */
public class ImageUtils {

	private static Scene scene;
	private static Stage stage;
	private static final String ERROR = ImageUtils.class.getSimpleName();

	/*public static void view(Path path) {

		ImageViewerController imageViewerController = null;
		Parent parent = null;

		// Parent parent = null;
		FXMLLoader loader = null;
		try {
			loader = new FXMLLoader(Main.class.getResource("fxml/imageView/ImageViewer.fxml"), bundle);
			parent = loader.load();
			imageViewerController = (ImageViewerController) loader.getController();
			scene = new Scene(parent);
			scene.getStylesheets()
					.add(Main.class.getResource(conf.getThemePath() + "imageViewer.css").toExternalForm());

			stage = new Stage();
			stage.centerOnScreen();
			stage.initStyle(StageStyle.TRANSPARENT);
			stage.setScene(scene);
			imageViewerController.init(path, scene, stage);

			stage.show();
		} catch (IOException e) {
			System.out.println(ERROR + " e: " + e.getMessage() + " line: " + Misc.getLineNumber());
			// Messages.errorSmth(ERROR + " e: " + e.getMessage(), Misc_GUI.getLineNumber(),
			// true);
		}

	}*/
	public static void view(List<FileInfo> fileInfoList, FileInfo fileInfo, Window window) {

		ImageViewerController imageViewerController = null;
		Parent parent = null;

		FXMLLoader loader = null;
		try {
			loader = new FXMLLoader(Main.class.getResource("fxml/imageView/ImageViewer.fxml"), bundle);
			parent = loader.load();
			imageViewerController = (ImageViewerController) loader.getController();
			scene = new Scene(parent);
			scene.getStylesheets()
					.add(Main.class.getResource(conf.getThemePath() + "imageViewer.css").toExternalForm());

			stage = new Stage();
			stage.centerOnScreen();
			stage.initStyle(StageStyle.TRANSPARENT);
			stage.setScene(scene);
			stage.initOwner(window);
			imageViewerController.init(fileInfoList, fileInfo, scene, stage);

			stage.show();
		} catch (IOException e) {
			System.out.println(ERROR + " e: " + e.getMessage() + " line: " + Misc.getLineNumber());
			// Messages.errorSmth(ERROR + " e: " + e.getMessage(), Misc_GUI.getLineNumber(),
			// true);
		}

	}
	public static void playVideo(Path path, Node node) {
		Messages.sprintf("Playing playVideo");
		if (Main.conf.isVlcSupport()) {
			try {
				Parent root = null;
				FXMLLoader loader = new FXMLLoader(Main.class.getResource("/com/girbola/vlcj/VLCPlayer.fxml"));
				root = loader.load();
				VLCPlayerController vlcPlayerController = (VLCPlayerController) loader.getController();
				Scene scene = new Scene(root);
				scene.getStylesheets()
						.add(Main.class.getResource(conf.getThemePath() + "vlcPlayer.css").toExternalForm());

				Stage stage = new Stage();

				stage.setScene(scene);
				DropShadow ds = new DropShadow(19.9175, Color.RED);
				ds.setHeight(31.26);
				ds.setWidth(50.41);
				root.setEffect(ds);
				stage.initStyle(StageStyle.UNDECORATED);
				vlcPlayerController.init(path, stage);
				stage.show();

				stage.focusedProperty().addListener(new ChangeListener<Boolean>() {

					@Override
					public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
							Boolean newValue) {
						System.out.println("focus? " + newValue);
						if (!newValue) {
							vlcPlayerController.stop();
							stage.close();
						}
					}
				});
				stage.setOnCloseRequest(new EventHandler<WindowEvent>() {

					@Override
					public void handle(WindowEvent event) {
						vlcPlayerController.stop();
					}

				});

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
