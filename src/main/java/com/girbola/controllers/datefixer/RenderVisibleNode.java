/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved.  
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.datefixer;

import com.girbola.Main;
import com.girbola.configuration.GUIPrefs;
import com.girbola.fileinfo.FileInfo;
import com.girbola.fileinfo.ThumbInfo;
import com.girbola.imagehandling.ConvertImage_Byte;
import com.girbola.imagehandling.ConvertVideo_Byte;
import com.girbola.imagehandling.ImageHandling;
import com.girbola.imagehandling.VideoThumbMaker;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.rotate.Rotate;
import com.girbola.sql.SQL_Utils;
import com.girbola.videothumbnailing.VideoThumnbnailator;
import common.utils.FileUtils;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.girbola.imagehandling.ImageHandling.handleImageThumb;
import static com.girbola.imagehandling.ImageHandling.handleRawImageThumb;
import static com.girbola.messages.Messages.sprintf;

/**
 *
 * @author Marko Lokka
 */
public class RenderVisibleNode {

	final private static String ERROR = RenderVisibleNode.class.getName();
	private ExecutorService exec_multi;
	private ExecutorService exec_single;

	private ScrollPane scrollPane;
	private Timeline timeline;
	private Map<ImageView, FileInfo> map = new HashMap<>();
	private Path currentFolderPath;

	private Connection connection;

	public RenderVisibleNode(ScrollPane aScrollPane, Path aCurrentFolderPath, Connection aConnection) {
		this.scrollPane = aScrollPane;
		this.currentFolderPath = aCurrentFolderPath;
		this.connection = aConnection;
		scrollPane.vvalueProperty().addListener((obs) -> {
			if (timeline != null) {
				timeline.stop();
			}
			timeline = new Timeline(new KeyFrame(javafx.util.Duration.millis(200), ae -> renderVisibleNodes()));
			sprintf("1time to render visible nodes> " + scrollPane.getVvalue() + " getVMax: " + scrollPane.getVmax());
			timeline.play();
			map.clear();
		});
		scrollPane.viewportBoundsProperty().addListener(new ChangeListener<Bounds>() {
			@Override
			public void changed(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue) {

				if (timeline != null) {
					timeline.stop();
				}
				timeline = new Timeline(new KeyFrame(javafx.util.Duration.millis(200), ae -> renderVisibleNodes()));
				sprintf("2time to render visible nodes> " + scrollPane.getVvalue() + " getVMax: "
						+ scrollPane.getVmax());
				timeline.play();
				map.clear();
			}
		});

//		scrollPane.setVvalue(-10);
//		scrollPane.setVvalue(0);

	}

	private synchronized void renderVisibleNodes() throws NullPointerException {
		sprintf("renderVisibleNodes started");
		if (exec_multi != null) {
			if (!exec_multi.isShutdown() || exec_multi.isTerminated()) {
				timeline.stop();
				exec_multi.shutdownNow();
			}
		}
		if (exec_single != null) {
			if (!exec_single.isShutdown() || exec_single.isTerminated()) {
				timeline.stop();
				exec_single.shutdownNow();
			}
		}

		checkVisible(scrollPane);

		if (!map.isEmpty()) {
			exec_multi = Executors.newCachedThreadPool(r -> {
				Thread t = new Thread(r);
				t.setName("multi thread");
				t.setDaemon(true);
				return t;

			});
			exec_single = Executors.newSingleThreadExecutor(r -> {
				Thread t = new Thread(r);
				t.setName("single thread");
				t.setDaemon(true);
				return t;
			});
			List<Task<?>> byte_List = new ArrayList<>();
			List<Task<?>> needToConvert_Image_list = new ArrayList<>();
			List<Task<?>> needToConvert_Video_list = new ArrayList<>();
			List<Task<?>> needToConvert_SlowRender_list = new ArrayList<>();

			for (Entry<ImageView, FileInfo> entry : map.entrySet()) {
				ImageView imageView = entry.getKey();
				FileInfo fileInfo = (FileInfo) entry.getValue();
				if (fileInfo == null) {
					Main.setProcessCancelled(true);
					Messages.errorSmth(ERROR, "fileInfo were null!!!", null, Misc.getLineNumber(), true);
				}

				if (imageView != null) {
					Path file = Paths.get(fileInfo.getOrgPath());
					if (Files.exists(file)) {
						if (imageView.getImage() == null) {
							if (FileUtils.supportedMediaFormat(file.toFile())) {
								ThumbInfo thumbInfo = SQL_Utils.loadThumbInfo(connection, fileInfo.getFileInfo_id());
								
								int value = handle_thumb(fileInfo, thumbInfo, Main.conf.isBetterQualityThumbs());
								/*
								 * 0 = thumbinfo found with image(s). Load 1 = thumbinfo has no arraylist.
								 * Create
								 */
								switch (value) {
								case 0:
									if (FileUtils.supportedImage(file)) {
										Task<Image> convertByte_thumb_fast = new ConvertImage_Byte(
												Paths.get(fileInfo.getOrgPath()), thumbInfo, GUIPrefs.thumb_x_MAX,
												imageView);
										byte_List.add(convertByte_thumb_fast);

									}
									if (FileUtils.supportedRaw(file)) {
										Task<Image> convertByte_thumb_fast = new ConvertImage_Byte(
												Paths.get(fileInfo.getOrgPath()), thumbInfo, GUIPrefs.thumb_x_MAX,
												imageView);
										byte_List.add(convertByte_thumb_fast);

									} else if (FileUtils.supportedVideo(file)) {
										Task<List<BufferedImage>> convertByte_thumb_fast = new ConvertVideo_Byte(
												Paths.get(fileInfo.getOrgPath()), thumbInfo, GUIPrefs.thumb_x_MAX,
												imageView);
										byte_List.add(convertByte_thumb_fast);
									}
									break;
								case 1:
									if (FileUtils.supportedVideo(file)) {
//										Task<List<BufferedImage>> convertVideo_task = VideoThumnbnailator.getVideoThumbnails(fileInfo, imageView, (GUIPrefs.thumb_x_MAX - 2));
										Task<List<BufferedImage>> convertVideo_task = new VideoThumbMaker(fileInfo,
												imageView, (GUIPrefs.thumb_x_MAX - 2));
										needToConvert_Video_list.add(convertVideo_task);
									} else if (FileUtils.supportedImage(file)) {
										if (FileUtils.isTiff(file.toFile())) {
											Messages.sprintf(
													"Tiff file. Can't find getThumbs.get(0). Creating imageThumb and rotate");
//											Task<Image> imageThumb = handleImageThumb(fileInfo, GUIPrefs.thumb_x_MAX,
//													imageView);
											Task<Image> imageThumb = ImageHandling.handleTiffThumb(fileInfo,
													GUIPrefs.thumb_x_MAX, imageView);
											imageView.setRotate(Rotate.rotate(fileInfo.getOrientation()));
											needToConvert_Image_list.add(imageThumb);
										} else {
											Messages.sprintf(
													"2 Can't find getThumbs.get(0). Creating imageThumb and rotate");
											Task<Image> imageThumb = handleImageThumb(fileInfo, GUIPrefs.thumb_x_MAX,
													imageView);
											imageView.setRotate(Rotate.rotate(fileInfo.getOrientation()));
											needToConvert_Image_list.add(imageThumb);
										}
									} else if (FileUtils.supportedRaw(file)) {
										Messages.sprintf(
												"3_Can't find getThumbs.get(0). Creating imageThumb and rotate");
										Task<Image> imageThumb = handleRawImageThumb(fileInfo, GUIPrefs.thumb_x_MAX,
												imageView);
										imageView.setRotate(Rotate.rotate(fileInfo.getOrientation()));
										needToConvert_Image_list.add(imageThumb);
									}
									break;
								}
							}
						}
					}
				}
			}

			if (byte_List.size() > 1) {
				for (Task<?> bytes_Task : byte_List) {
					if (bytes_Task != null && !Main.getProcessCancelled()) {
						exec_multi.submit(bytes_Task);
					}
				}
				exec_multi.shutdown();
				try {
					exec_multi.awaitTermination(byte_List.size() * 100, TimeUnit.SECONDS);
				} catch (InterruptedException ex) {
					Logger.getLogger(DateFixerController.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
			sprintf("needToConvert_Image_list size: " + needToConvert_Image_list.size());
			sprintf("exec_single isTerminated? " + exec_single.isTerminated() + " isShutDown: "
					+ exec_single.isShutdown());
			for (Task<?> image_Task : needToConvert_Image_list) {
				if (image_Task != null && !Main.getProcessCancelled()) {
					Messages.sprintf("Adding needToConvert_Image_list to exec to create a thumbnail");
					exec_single.submit(image_Task);
				}
			}
			sprintf("needToConvert_Video_list size: " + needToConvert_Video_list.size());
			for (Task<?> video_task : needToConvert_Video_list) {
				if (video_task != null && !Main.getProcessCancelled()) {
					exec_single.submit(video_task);
				}
			}
			sprintf("needToConvert_SlowRender_list size: " + needToConvert_Video_list.size());
			for (Task<?> slow_render : needToConvert_SlowRender_list) {
				if (slow_render != null && !Main.getProcessCancelled()) {
					exec_single.submit(slow_render);
				}
			}
			exec_single.shutdown();
		} else {
			sprintf("Visible node were empty");
		}
	}

	/**
	 * 0 = thumbinfo found 1 = thumbinfo create thumbs
	 * 
	 * @param fileInfo
	 * @param thumbInfo
	 * @param betterQuality
	 * @return
	 */
	private int handle_thumb(FileInfo fileInfo, ThumbInfo thumbInfo, boolean betterQuality) {
		if (thumbInfo == null) {
			Messages.sprintf("thumbsInfo were null! " + fileInfo.getOrgPath());
			return 1;
		}
		if (thumbInfo.getThumbs() == null) {
			Messages.sprintf("getThumbswere null"+ fileInfo.getOrgPath());
			return 1;
		}
		if (thumbInfo.getThumbs().get(0) == null) {
			Messages.sprintf("getThumbs.get(0) is empty"+ fileInfo.getOrgPath());
			return 1;
		}
		if (thumbInfo.getThumbs().isEmpty()) {
			Messages.sprintf("getThumbs list is empty"+ fileInfo.getOrgPath());
			return 1;
		} else {
			Messages.sprintf("getThumbs() were not empty");
			return 0;
		}
	}

	private void checkVisible(ScrollPane scrollPane) {
		map.clear();
		Bounds paneBounds = scrollPane.localToScene(scrollPane.getBoundsInParent());
		sprintf("Scrollpane id is: " + scrollPane.getContent());

		Node mainNode = scrollPane.getContent();

		if (mainNode instanceof TilePane && mainNode.getId().equals("dateFixer")) {
			Messages.sprintf("datefixer");
			for (Node gridPane : ((TilePane) mainNode).getChildren()) {
				if (gridPane instanceof VBox && gridPane.getId().equals("imageFrame")) {
					Bounds nodeBounds = gridPane.localToScene(gridPane.getBoundsInLocal());
					if (paneBounds.intersects(nodeBounds)) {
						for (Node imageFrame_vbox : ((VBox) gridPane).getChildren()) {
							if (imageFrame_vbox instanceof StackPane) {
								ImageView iv = (ImageView) imageFrame_vbox.lookup("#imageView");
								map.put(iv, (FileInfo) gridPane.getUserData());
							}
						}
					}
				}
			}
			// container
		} else if (mainNode instanceof VBox && mainNode.getId().equals("container")) {
			Messages.sprintf("importImagesContainer");
			for (Node scrollPane_NODE : ((VBox) scrollPane.getContent()).getChildrenUnmodifiable()) {
				Bounds nodeBounds = scrollPane_NODE.localToScene(scrollPane_NODE.getBoundsInLocal());
				if (paneBounds.intersects(nodeBounds)) {
					TitledPane titledPane = (TitledPane) scrollPane_NODE;
					TilePane tilePane = (TilePane) titledPane.getContent();
					for (Node root : tilePane.getChildren()) {
						ImageView iv = (ImageView) root.lookup("#imageView");
						StackPane stackPane = (StackPane) root.lookup("#imageFrame");
						if (iv != null) {
							map.put(iv, (FileInfo) stackPane.getUserData());
						}
					}
				}
			}
		} else if (mainNode instanceof VBox && mainNode.getId().equals("closerLook")) {
			Messages.sprintf("CloseLook: " + mainNode.getId());
			for (Node scrollPane_NODE : ((VBox) scrollPane.getContent()).getChildrenUnmodifiable()) {
				Messages.sprintf("scrollPane_NODE: " + scrollPane_NODE);
				Bounds nodeBounds = scrollPane_NODE.localToScene(scrollPane_NODE.getBoundsInLocal());
				if (paneBounds.intersects(nodeBounds)) {
					Messages.sprintf("Does intersects the node");
					if (scrollPane_NODE instanceof TilePane) {
						Messages.sprintf("Were TilePane");
						for (Node tiles : ((TilePane) scrollPane_NODE).getChildren()) {
							if (tiles instanceof StackPane) {
								Messages.sprintf("scrollPane_NODE was TilePane: " + tiles);
								if (tiles instanceof StackPane) {
									Messages.sprintf("Were StackPane");
									if (tiles instanceof StackPane) {
										ImageView iv = (ImageView) tiles.lookup("#imageView");
										StackPane stackPane = (StackPane) tiles.lookup("#imageFrame");
										if (iv != null) {
											map.put(iv, (FileInfo) stackPane.getUserData());
										}
									}
								}
							}
						}
					} else {
						Messages.sprintf("Else if doesn't intersect: " + scrollPane_NODE);
					}

				}
			}

		} else {
			sprintf("scrollPane.getContent node was: " + scrollPane.getContent());
		}
	}

	/**
	 * cancels execution service right away
	 */
	public void stopTimeLine() {
		timeline.stop();
		try {
			exec_multi.shutdownNow();
			exec_single.shutdownNow();
		} catch (Exception e) {
			sprintf("cannot cancel: " + ERROR);
		}
	}

	public void startTimeLine() {
		timeline.play();
	}
}
