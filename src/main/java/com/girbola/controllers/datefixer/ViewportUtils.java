package com.girbola.controllers.datefixer;

import com.girbola.fileinfo.FileInfo;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import java.util.HashMap;
import java.util.Map;

public class ViewportUtils {

    /**
     * Finds all visible ImageViews inside a ScrollPane's viewport and maps them to their FileInfo.
     * Expects FileInfo to be stored in the ImageView or a parent container's userData.
     */
    public static Map<ImageView, FileInfo> getVisibleImageViews(ScrollPane scrollPane, String targetImageViewId) {
        Map<ImageView, FileInfo> visibleMap = new HashMap<>();
        Node content = scrollPane.getContent();

        if (content == null) {
            return visibleMap;
        }

        // Get scrollPane bounds in scene coordinates
        Bounds viewportBounds = scrollPane.localToScene(scrollPane.getBoundsInParent());

        // Find all ImageViews inside the ScrollPane content hierarchy
        findVisibleImageViewsRecursive(content, viewportBounds, targetImageViewId, visibleMap);

        return visibleMap;
    }

    private static void findVisibleImageViewsRecursive(
            Node node, Bounds viewportBounds, String targetImageViewId, Map<ImageView, FileInfo> resultMap) {

        if (node == null || !node.isVisible()) {
            return;
        }

        // Fast bounding-box check: skip entire subtrees that are outside the scroll viewport
        Bounds nodeBounds = node.localToScene(node.getBoundsInLocal());
        if (!viewportBounds.intersects(nodeBounds)) {
            return;
        }

        // If this node is a matching ImageView, retrieve its associated FileInfo
        if (node instanceof ImageView && targetImageViewId.equals(node.getId())) {
            FileInfo info = extractFileInfo(node);
            if (info != null) {
                resultMap.put((ImageView) node, info);
            }
            return;
        }

        // Recurse down through child nodes if this is a parent container
        if (node instanceof Parent) {
            for (Node child : ((Parent) node).getChildrenUnmodifiable()) {
                findVisibleImageViewsRecursive(child, viewportBounds, targetImageViewId, resultMap);
            }
        }
    }

    /**
     * Resolves FileInfo by checking the node itself or walking up its parent chain.
     */
    private static FileInfo extractFileInfo(Node node) {
        Node current = node;
        while (current != null) {
            if (current.getUserData() instanceof FileInfo) {
                return (FileInfo) current.getUserData();
            }
            current = current.getParent();
        }
        return null;
    }
}
