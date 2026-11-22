
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
import javafx.beans.InvalidationListener;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.girbola.imagehandling.ImageHandling.handleImageThumb;
import static com.girbola.imagehandling.ImageHandling.handleRawImageThumb;
import static com.girbola.messages.Messages.sprintf;


public class RenderVisibleNode {

    final private static String ERROR = RenderVisibleNode.class.getName();
    private ThreadPoolExecutor exec_multi;
    private ThreadPoolExecutor exec_single;

    private ScrollPane scrollPane;
    private final Timeline timeline;
    private final Map<ImageView, FileInfo> imageViewsMap = new HashMap<>();
    private final Map<ImageView, Task<?>> inFlight = new HashMap<>();
    private final Map<ImageView, ThumbnailPreview> previews = new HashMap<>();
    private final InvalidationListener contentSizeListener = observable -> scheduleRender();
    private boolean active = true;
    private boolean cleanupScheduled;
    private Path currentFolderPath;

    private Connection connection;

    public RenderVisibleNode(ScrollPane aScrollPane, Path aCurrentFolderPath, Connection aConnection) {
        this.scrollPane = aScrollPane;
        this.currentFolderPath = aCurrentFolderPath;
        this.connection = aConnection;
        timeline = new Timeline(new KeyFrame(javafx.util.Duration.millis(120), e -> renderVisibleNodes()));
        scrollPane.vvalueProperty().addListener((obs) -> scheduleRender());
        scrollPane.hvalueProperty().addListener((obs) -> scheduleRender());
        scrollPane.viewportBoundsProperty().addListener((observable, oldValue, newValue) -> scheduleRender());
        scrollPane.contentProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != null) {
                oldValue.layoutBoundsProperty().removeListener(contentSizeListener);
            }
            if (newValue != null) {
                newValue.layoutBoundsProperty().addListener(contentSizeListener);
            }
            scheduleRender();
        });
        if (scrollPane.getContent() != null) {
            scrollPane.getContent().layoutBoundsProperty().addListener(contentSizeListener);
        }
        Platform.runLater(() -> {
            scrollPane.setVvalue(1);
            scrollPane.setVvalue(0);
            scheduleRender();
        });
    }

    private void scheduleRender() {
        if (active) {
            if (!cleanupScheduled) {
                cleanupScheduled = true;
                Platform.runLater(() -> {
                    cleanupScheduled = false;
                    if (active) {
                        scrollPane.layout();
                        checkVisible();
                    }
                });
            }
            timeline.playFromStart();
        }
    }

    private void renderVisibleNodes() {
        sprintf("renderVisibleNodes started");
        checkVisible();

        if (!imageViewsMap.isEmpty()) {
            Messages.sprintf("############Map size: " + imageViewsMap.size());
            for (Entry<ImageView, FileInfo> entry : imageViewsMap.entrySet()) {
                ImageView imageView = entry.getKey();
                FileInfo fileInfo = (FileInfo) entry.getValue();
                Messages.sprintf("Rendering fileInfo: " + fileInfo);
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
                        if (imageView.getImage() == null && !inFlight.containsKey(imageView) && !Main.getProcessCancelled()) {
                            if (FileUtils.supportedMediaFormat(file.toFile())) {
                                Task<ThumbInfo> lookup = new Task<>() {
                                    @Override
                                    protected ThumbInfo call() {
                                        return ThumbInfoDao.loadThumbInfo(connection, fileInfo.getFileInfo_id());
                                    }
                                };
                                lookup.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, event -> {
                                    if (inFlight.remove(imageView, lookup)
                                            && active && imageViewsMap.get(imageView) == fileInfo
                                            && imageView.getImage() == null && !Main.getProcessCancelled()) {
                                        renderThumbnail(imageView, fileInfo, file, lookup.getValue());
                                    }
                                });
                                submitThumbnail(imageView, lookup, false);
                            }
                        }
                    }
                }
            }

        } else {
            sprintf("Visible node were empty");
        }
    }

    private void renderThumbnail(ImageView imageView, FileInfo fileInfo, Path file, ThumbInfo thumbInfo) {
        Messages.sprintf("thumbInfo is: " + thumbInfo);
        switch (handle_thumb(fileInfo, thumbInfo)) {
            case 0:
                if (FileUtils.supportedHeic(file)) {
                    Task<List<BufferedImage>> videoTask = new MFFmpegFrameGrabber(
                            fileInfo, imageView, UIContants.THUMBNAIL_MAX_WIDTH - 2);
                    submitThumbnail(imageView, videoTask, false);
                } else if (FileUtils.supportedImage(file) || FileUtils.supportedRaw(file)) {
                    Task<Image> imageTask = new ConvertImage_Byte(
                            file, thumbInfo, UIContants.THUMBNAIL_MAX_WIDTH, null);
                    submitThumbnail(imageView, imageTask, true);
                } else if (FileUtils.supportedVideo(file)) {
                    Task<List<BufferedImage>> videoTask = new ConvertVideo_Byte(
                            file, thumbInfo, UIContants.THUMBNAIL_MAX_WIDTH, imageView);
                    submitThumbnail(imageView, videoTask, true);
                }
                break;
            case 1:
                if (FileUtils.supportedVideo(file) || FileUtils.supportedHeic(file)) {
                    Task<List<BufferedImage>> videoTask = new MFFmpegFrameGrabber(
                            fileInfo, imageView, UIContants.THUMBNAIL_MAX_WIDTH - 2);
                    submitThumbnail(imageView, videoTask, false);
                } else if (FileUtils.supportedImage(file)) {
                    Task<Image> imageTask;
                    if (FileUtils.isTiff(file.toFile())) {
                        Messages.sprintf("-Tiff file. Can't find getThumbs.get(0). Creating imageThumb and rotate");
                        imageTask = ImageHandling.handleTiffThumb(fileInfo, UIContants.THUMBNAIL_MAX_WIDTH, null);
                    } else {
                        Messages.sprintf("---#### Can't find getThumbs.get(0). Creating imageThumb and rotate");
                        imageTask = handleImageThumb(fileInfo, UIContants.THUMBNAIL_MAX_WIDTH, null);
                    }
                    imageView.setRotate(Rotate.rotate(fileInfo.getOrientation()));
                    submitThumbnail(imageView, imageTask, false);
                } else if (FileUtils.supportedRaw(file)) {
                    Messages.sprintf("RAW 3_Can't find getThumbs.get(0). Creating imageThumb and rotate");
                    Task<Image> imageTask = handleRawImageThumb(fileInfo, UIContants.THUMBNAIL_MAX_WIDTH, null);
                    imageView.setRotate(Rotate.rotate(fileInfo.getOrientation()));
                    submitThumbnail(imageView, imageTask, false);
                }
                break;
        }
    }

    void submitThumbnail(ImageView imageView, Task<?> task, boolean fast) {
        if (!active || imageView.getImage() != null || inFlight.containsKey(imageView)) {
            return;
        }
        inFlight.put(imageView, task);
        task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, event -> {
            if (inFlight.get(imageView) == task) {
                if (task.getValue() instanceof Image image && imageView.getImage() == null) {
                    imageView.setImage(image);
                }
                if (task instanceof ThumbnailPreview preview) {
                    previews.put(imageView, preview);
                }
            } else if (task instanceof ThumbnailPreview preview) {
                preview.releasePreview();
            }
            inFlight.remove(imageView, task);
        });
        task.addEventHandler(WorkerStateEvent.WORKER_STATE_FAILED, event -> {
            inFlight.remove(imageView, task);
            Messages.sprintfError("Thumbnail rendering failed: " + task.getException());
        });
        task.addEventHandler(WorkerStateEvent.WORKER_STATE_CANCELLED, event -> inFlight.remove(imageView, task));
        if (fast) {
            if (exec_multi == null) {
                exec_multi = createExecutor(2, "thumbnail bytes");
            }
            exec_multi.execute(task);
        } else {
            if (exec_single == null) {
                exec_single = createExecutor(1, "thumbnail render");
            }
            exec_single.execute(task);
        }
    }

    private static ThreadPoolExecutor createExecutor(int threads, String name) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(threads, threads, 30, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(), runnable -> {
                    Thread thread = new Thread(runnable, name);
                    thread.setDaemon(true);
                    return thread;
                });
        executor.allowCoreThreadTimeOut(true);
        return executor;
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
            Messages.sprintfError("[handle_thumb] ThumbInfo object is null for path: %s", path);
            return 1;
        }
        if (thumbInfo.getThumbs() == null) {
            Messages.sprintfError("[handle_thumb] Thumbs list is null for path: %s", path);
            return 1;
        }
        if (thumbInfo.getThumbs().isEmpty()) {
            Messages.sprintfError("[handle_thumb] Thumbs list is empty for path: %s", path);
            return 1;
        }
        if (thumbInfo.getThumbs().getFirst() == null) {
            Messages.sprintfError("[handle_thumb] First thumbnail in list is null for path: %s", path);
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

    private void checkVisible() {

        Map<ImageView, FileInfo> previouslyVisible = new HashMap<>(imageViewsMap);
        imageViewsMap.clear();
        Node viewport = scrollPane.lookup(".viewport");
        Bounds paneBounds = viewport == null ? scrollPane.localToScene(scrollPane.getBoundsInLocal())
                : viewport.localToScene(viewport.getBoundsInLocal());
        Node mainNode = scrollPane.getContent();
        if (mainNode == null) {
            releaseHidden(previouslyVisible);
            return;
        }
        if (mainNode instanceof TilePane && "dateFixer".equals(mainNode.getId())) {
            for (Node tilePane : ((TilePane) mainNode).getChildren()) {
                if (tilePane instanceof VBox && DateFixConstants.IMAGEFRAME.getType().equals(tilePane.getId())) {
                    Bounds nodeBounds = tilePane.localToScene(tilePane.getBoundsInLocal());
                    if (paneBounds.intersects(nodeBounds)) {
                        for (Node imageFrame_vbox : ((VBox) tilePane).getChildren()) {
                            if (imageFrame_vbox instanceof HBox && "imageViewContainer".equals(imageFrame_vbox.getId())) {
                                ImageView iv = (ImageView) imageFrame_vbox.lookup("#imageView");
                                if (iv != null) {
                                    imageViewsMap.put(iv, (FileInfo) tilePane.getUserData());
                                }
                            }
                        }
                    }
                }
            }
            // container
        } else if (mainNode instanceof VBox && "container".equals(mainNode.getId())) {
            for (Node scrollPane_NODE : ((VBox) scrollPane.getContent()).getChildrenUnmodifiable()) {
                Bounds nodeBounds = scrollPane_NODE.localToScene(scrollPane_NODE.getBoundsInLocal());
                if (paneBounds.intersects(nodeBounds) && scrollPane_NODE instanceof TitledPane
                        && ((TitledPane) scrollPane_NODE).isExpanded()) {
                    TitledPane titledPane = (TitledPane) scrollPane_NODE;
                    TilePane tilePane = (TilePane) titledPane.getContent();
                    for (Node root : tilePane.getChildren()) {
                        ImageView iv = (ImageView) root.lookup("#" + DateFixConstants.IMAGEVIEW.getType());
                        StackPane stackPane = (StackPane) root.lookup("#" + DateFixConstants.IMAGEFRAME.getType());
                        if (iv != null && stackPane != null
                                && paneBounds.intersects(root.localToScene(root.getBoundsInLocal()))) {
                            imageViewsMap.put(iv, (FileInfo) stackPane.getUserData());
                        }
                    }
                }
            }
        } else if (mainNode instanceof VBox && "closerLook".equals(mainNode.getId())) {
            for (Node scrollPane_NODE : ((VBox) scrollPane.getContent()).getChildrenUnmodifiable()) {
                Bounds nodeBounds = scrollPane_NODE.localToScene(scrollPane_NODE.getBoundsInLocal());
                if (paneBounds.intersects(nodeBounds)) {
                    if (scrollPane_NODE instanceof TilePane) {
                        for (Node tiles : ((TilePane) scrollPane_NODE).getChildren()) {
                            if (tiles instanceof StackPane
                                    && paneBounds.intersects(tiles.localToScene(tiles.getBoundsInLocal()))) {
                                if (tiles instanceof StackPane) {
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
                    }
                }
            }
        }
        releaseHidden(previouslyVisible);
    }

    private void releaseHidden(Map<ImageView, FileInfo> previouslyVisible) {
        for (ImageView imageView : previouslyVisible.keySet()) {
            if (imageViewsMap.containsKey(imageView)) {
                continue;
            }
            Task<?> task = inFlight.remove(imageView);
            if (task != null) {
                if (exec_multi != null) {
                    exec_multi.remove(task);
                }
                if (exec_single != null) {
                    exec_single.remove(task);
                }
                task.cancel();
                if (task instanceof ThumbnailPreview preview) {
                    preview.releasePreview();
                }
            }
            ThumbnailPreview preview = previews.remove(imageView);
            if (preview != null) {
                preview.releasePreview();
            }
            imageView.setImage(null);
            imageView.setUserData(null);
        }
    }

    /**
     * cancels execution service right away
     */
    public void stopTimeLine() {
        active = false;
        timeline.stop();
        for (Task<?> task : List.copyOf(inFlight.values())) {
            task.cancel();
            if (task instanceof ThumbnailPreview preview) {
                preview.releasePreview();
            }
        }
        inFlight.clear();
        if (exec_multi != null) {
            exec_multi.shutdownNow();
            exec_multi = null;
        }
        if (exec_single != null) {
            exec_single.shutdownNow();
            exec_single = null;
        }
    }

    public void startTimeLine() {
        active = true;
        scheduleRender();
    }
}
