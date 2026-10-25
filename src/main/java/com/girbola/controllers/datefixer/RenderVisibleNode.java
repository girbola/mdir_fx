
package com.girbola.controllers.datefixer;

import com.girbola.Main;
import com.girbola.configuration.UIContants;
import com.girbola.fileinfo.FileInfo;
import com.girbola.imagehandling.*;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.rotate.Rotate;
import com.girbola.persistence.thumbinfo.ThumbInfoDao;
import com.girbola.thumbinfo.ThumbInfo;
import common.utils.FileUtils;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
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


public class RenderVisibleNode {

    final private static String ERROR = RenderVisibleNode.class.getName();
    private ExecutorService exec_multi;
    private ExecutorService exec_single;

    private ScrollPane scrollPane;
    private Timeline timeline;
    private Map<ImageView, FileInfo> imageViewsMap = new HashMap<>();
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
            timeline = new Timeline(new KeyFrame(javafx.util.Duration.millis(200), e -> renderVisibleNodes()));
            sprintf("1time to render visible nodes> " + scrollPane.getVvalue() + " getVMax: " + scrollPane.getVmax());
            timeline.play();
            //imageViewsMap.clear();
        });

        scrollPane.viewportBoundsProperty().addListener((observable, oldValue, newValue) -> {
            if (timeline != null) {
                timeline.stop();
            }
            timeline = new Timeline(new KeyFrame(javafx.util.Duration.millis(200), ae -> renderVisibleNodes()));
            Messages.sprintf("2time to render visible nodes> " + scrollPane.getVvalue() + " getVMax: " + scrollPane.getVmax());
            timeline.play();
            //imageViewsMap.clear();
        });
        Platform.runLater(() -> {

            scrollPane.setVvalue(1);
            scrollPane.setVvalue(0);

        });


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

        checkVisible();

        if (!imageViewsMap.isEmpty()) {
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
            Messages.sprintf("############Map size: " + imageViewsMap.size());
            for (Entry<ImageView, FileInfo> entry : imageViewsMap.entrySet()) {
                ImageView imageView = entry.getKey();
                FileInfo fileInfo = (FileInfo) entry.getValue();
                if (fileInfo == null) {
                    Main.setProcessCancelled(true);
                    Messages.errorSmth(ERROR, "fileInfo were null!!!", null, Misc.getLineNumber(), true);
                    break;
                }
                Messages.sprintf("************File: " + fileInfo.getOrgPath());
                if (fileInfo.getOrgPath().contains("D:\\Temppi\\2011\\2011-04-28 - Espanja - Kiertelemässä")) {
                    Messages.sprintf("Found problematic file: " + fileInfo.getOrgPath());
                }
                if (imageView != null) {
                    Path file = Paths.get(fileInfo.getOrgPath());
                    if (Files.exists(file)) {
                        if (imageView.getImage() == null) {
                            if (FileUtils.supportedMediaFormat(file.toFile())) {
                                ThumbInfo thumbInfo = ThumbInfoDao.loadThumbInfo(connection, fileInfo.getFileInfo_id());
//                                for(byte[] thumb : thumbInfo.getThumbs()) {
//                                    if(thumb == null) {
//                                     Messages.sprintf("##############Thumb ID: " + thumbInfo.getId() + " is null");
//                                    } else {
//                                        Messages.sprintf("##############Thumb ID: " + thumbInfo.getId() + " size: " + thumb.length);
//                                    }
//                                }
                                Messages.sprintf("thumbInfo is: " + thumbInfo);
                                int value = handle_thumb(fileInfo, thumbInfo);
                                /*
                                 * 0 = thumbinfo found with image(s). Load 1 = thumbinfo has no arraylist.
                                 * Create
                                 */
                                switch (value) {
                                    case 0:
                                        if (FileUtils.supportedImage(file)) {
                                            Task<Image> convertByteThumbFast = new ConvertImage_Byte(
                                                    Paths.get(fileInfo.getOrgPath()), thumbInfo, UIContants.THUMBNAIL_MAX_WIDTH,
                                                    imageView);
                                            byte_List.add(convertByteThumbFast);
                                        }
                                        if (FileUtils.supportedHeic(file)) {
                                            Task<List<BufferedImage>> convertVideotask = new MFFmpegFrameGrabber(fileInfo, imageView, (UIContants.THUMBNAIL_MAX_WIDTH - 2));
                                            needToConvert_Video_list.add(convertVideotask);
                                        }
                                        if (FileUtils.supportedRaw(file)) {
                                            Task<Image> convertRawByteFast = new ConvertImage_Byte(
                                                    Paths.get(fileInfo.getOrgPath()), thumbInfo, UIContants.THUMBNAIL_MAX_WIDTH,
                                                    imageView);
                                            byte_List.add(convertRawByteFast);

                                        } else if (FileUtils.supportedVideo(file)) {
                                            Task<List<BufferedImage>> convertByteThumbFast = new ConvertVideo_Byte(
                                                    Paths.get(fileInfo.getOrgPath()), thumbInfo, UIContants.THUMBNAIL_MAX_WIDTH,
                                                    imageView);
                                            byte_List.add(convertByteThumbFast);
                                        }
                                        break;
                                    case 1:
                                        if (FileUtils.supportedVideo(file)) {
                                            Task<List<BufferedImage>> convertVideoTask = new MFFmpegFrameGrabber(fileInfo, imageView, (UIContants.THUMBNAIL_MAX_WIDTH - 2));
                                            needToConvert_Video_list.add(convertVideoTask);
                                        } else if (FileUtils.supportedImage(file)) {
                                            if (FileUtils.isTiff(file.toFile())) {
                                                Messages.sprintf("Tiff file. Can't find getThumbs.get(0). Creating imageThumb and rotate");
                                                Task<Image> tiffThumb = ImageHandling.handleTiffThumb(fileInfo, UIContants.THUMBNAIL_MAX_WIDTH, imageView);
                                                imageView.setRotate(Rotate.rotate(fileInfo.getOrientation()));
                                                needToConvert_Image_list.add(tiffThumb);
                                            } else if (FileUtils.supportedHeic(file)) {
                                                Messages.sprintf("Heic file. Can't find getThumbs.get(0). Creating imageThumb and rotate");
                                                Task<List<BufferedImage>> convertVideoTask = new MFFmpegFrameGrabber(fileInfo, imageView, (UIContants.THUMBNAIL_MAX_WIDTH - 2));
                                                needToConvert_Video_list.add(convertVideoTask);
                                            } else {
                                                Messages.sprintf("2 Can't find getThumbs.get(0). Creating imageThumb and rotate");
                                                Task<Image> imageThumb = handleImageThumb(fileInfo, UIContants.THUMBNAIL_MAX_WIDTH, imageView);
                                                imageView.setRotate(Rotate.rotate(fileInfo.getOrientation()));
                                                needToConvert_Image_list.add(imageThumb);
                                            }
                                        } else if (FileUtils.supportedRaw(file)) {
                                            Messages.sprintf("3_Can't find getThumbs.get(0). Creating imageThumb and rotate");
                                            Task<Image> imageThumb = handleRawImageThumb(fileInfo, UIContants.THUMBNAIL_MAX_WIDTH, imageView);
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

            if (!byte_List.isEmpty()) {
                for (Task<?> bytes_Task : byte_List) {
                    if (bytes_Task != null && !Main.getProcessCancelled()) {
                        exec_multi.submit(bytes_Task);
                    }
                }
                exec_multi.shutdown();
                try {
                    exec_multi.awaitTermination(byte_List.size() * 100L, TimeUnit.SECONDS);
                } catch (InterruptedException ex) {
                    Logger.getLogger(DateFixerController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            sprintf("needToConvert_Image_list size: " + needToConvert_Image_list.size());
            sprintf("exec_single isTerminated? " + exec_single.isTerminated() + " isShutDown: "
                    + exec_single.isShutdown());
            for (Task<?> image_Task : needToConvert_Image_list) {
                if (image_Task != null && !Main.getProcessCancelled()) {
//                    Messages.sprintf("Adding needToConvert_Image_list to exec to create a thumbnail");
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
     * @return
     */
    private int handle_thumb(FileInfo fileInfo, ThumbInfo thumbInfo) {
        String path = (fileInfo != null) ? fileInfo.getOrgPath() : "unknown path";

        if (thumbInfo == null) {
            Messages.sprintf("[handle_thumb] ThumbInfo object is null for path: %s", path);
            return 1;
        }
        if (thumbInfo.getThumbs() == null) {
            Messages.sprintf("[handle_thumb] Thumbs list is null for path: %s", path);
            return 1;
        }
        if (thumbInfo.getThumbs().isEmpty()) {
            Messages.sprintf("[handle_thumb] Thumbs list is empty for path: %s", path);
            return 1;
        }
        if (thumbInfo.getThumbs().getFirst() == null) {
            Messages.sprintf("[handle_thumb] First thumbnail in list is null for path: %s", path);
            return 1;
        }
//        if (thumbInfo.getThumb_fast() == null) {
//            Messages.sprintf("[handle_thumb] Fast thumbnail byte array is null for path: %s", path);
//            return 1;
//        }
        if (thumbInfo.getThumbs().getFirst().length == 0) {
            Messages.sprintf("[handle_thumb] Fast thumbnail byte array is empty for path: %s", path);
            return 1;
        }

        Messages.sprintf("[handle_thumb] Successfully retrieved fast thumb (%d bytes) for path: " + path);
        return 0;
    }

    private synchronized void checkVisible() {

        //imageViewsMap.clear();
        Bounds paneBounds = scrollPane.localToScene(scrollPane.getBoundsInParent());
        sprintf("Scrollpane id is: " + scrollPane.getContent());

        Node mainNode = scrollPane.getContent();
        Messages.sprintf("mainNode is: " + mainNode + " id: " + mainNode.getId() + " class: " + mainNode.getClass().getName());
        if (mainNode instanceof TilePane && mainNode.getId().equals("dateFixer")) {
            Messages.sprintf("before children iteration datefixer. mainNode is: " + ((TilePane) mainNode).getChildren().size());
            for (Node tilePane : ((TilePane) mainNode).getChildren()) {
                Messages.sprintf("tilePane is: " + tilePane + " id: " + tilePane.getId() + " class: " + tilePane.getClass().getName());
                if (tilePane instanceof VBox && tilePane.getId().equals(DateFixConstants.IMAGEFRAME.getType())) {
                    Bounds nodeBounds = tilePane.localToScene(tilePane.getBoundsInLocal());
                    if (paneBounds.intersects(nodeBounds)) {
                        for (Node imageFrame_vbox : ((VBox) tilePane).getChildren()) {
                            if (imageFrame_vbox instanceof HBox && imageFrame_vbox.getId().equals("imageViewContainer")) {
                                ImageView iv = (ImageView) imageFrame_vbox.lookup("#imageView");
                                imageViewsMap.put(iv, (FileInfo) tilePane.getUserData());
                            }
                        }
                    }
                } else {
                    Messages.sprintf("datefixer. mainNode is: " + mainNode);
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
                        ImageView iv = (ImageView) root.lookup("#" + DateFixConstants.IMAGEVIEW.getType());
                        StackPane stackPane = (StackPane) root.lookup("#" + DateFixConstants.IMAGEFRAME.getType());
                        if (iv != null) {
                            imageViewsMap.put(iv, (FileInfo) stackPane.getUserData());
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
                                            imageViewsMap.put(iv, (FileInfo) stackPane.getUserData());
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
