package com.girbola.controllers.datefixer.utils;

import com.girbola.controllers.datefixer.DateFixConstants;
import com.girbola.fileinfo.FileInfo;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * UI independent helpers extracted from DateFixerController.
 */
public final class DateFixerControllerUtils {

    public static final double ROTATION_HIDDEN = 0;
    public static final double ROTATION_SHOWN = 180;

    private DateFixerControllerUtils() {
    }

    public static boolean isImageFrame(Node node) {
        return node instanceof VBox
                && DateFixConstants.IMAGEFRAME.getType().equals(node.getId());
    }

    /**
     * Returns only the image frame nodes, keeping their original order.
     */
    public static List<Node> getImageFrameNodes(Collection<? extends Node> nodes) {
        List<Node> frames = new ArrayList<>();
        if (nodes == null) {
            return frames;
        }
        for (Node node : nodes) {
            if (isImageFrame(node)) {
                frames.add(node);
            }
        }
        return frames;
    }

    /**
     * Returns FileInfos of the image frame nodes. Frames without FileInfo user data are skipped.
     *
     * @param skipIgnored when true, FileInfos flagged as ignored are left out
     */
    public static List<FileInfo> collectImageFrameInfos(Collection<? extends Node> nodes, boolean skipIgnored) {
        List<FileInfo> infos = new ArrayList<>();
        for (Node node : getImageFrameNodes(nodes)) {
            if (node.getUserData() instanceof FileInfo fileInfo) {
                if (skipIgnored && fileInfo.isIgnored()) {
                    continue;
                }
                infos.add(fileInfo);
            }
        }
        return infos;
    }

    /**
     * Marks the FileInfo as waiting to be copied to the given destination.
     */
    public static void applyDestination(FileInfo fileInfo, Path destination, String workDir, String workDirSerialNumber) {
        fileInfo.setWorkDir(workDir);
        fileInfo.setWorkDirDriveSerialNumber(workDirSerialNumber);
        fileInfo.setDestination_Path(destination.toString());
        fileInfo.setCopied(false);
    }

    public static Comparator<Node> compareByOrgPath() {
        return Comparator.comparing(node -> ((FileInfo) node.getUserData()).getOrgPath());
    }

    public static Comparator<Node> compareByDate() {
        return (o1, o2) -> Long.compare(
                ((FileInfo) o1.getUserData()).getDate(),
                ((FileInfo) o2.getUserData()).getDate());
    }

    public static double panelWidth(boolean visible, double shownWidth, double hiddenWidth) {
        return visible ? shownWidth : hiddenWidth;
    }

    public static double arrowRotation(boolean visible) {
        return visible ? ROTATION_SHOWN : ROTATION_HIDDEN;
    }

    public static void setFixedWidth(Region region, double width) {
        region.setMinWidth(width);
        region.setPrefWidth(width);
        region.setMaxWidth(width);
    }
}
