package com.girbola.controllers.datefixer.utils;

import com.girbola.fileinfo.FileInfo;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DateFixerControllerUtilsTest {
    static {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }

    private static VBox frame(String orgPath, long date) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setOrgPath(orgPath);
        fileInfo.setDate(date);
        VBox vbox = new VBox();
        vbox.setId("imageFrame");
        vbox.setUserData(fileInfo);
        return vbox;
    }

    @Test
    void isImageFrame_trueOnlyForVBoxWithImageFrameId() {
        VBox frame = new VBox();
        frame.setId("imageFrame");
        VBox other = new VBox();
        other.setId("other");
        HBox hbox = new HBox();
        hbox.setId("imageFrame");

        assertTrue(DateFixerControllerUtils.isImageFrame(frame));
        assertFalse(DateFixerControllerUtils.isImageFrame(other));
        assertFalse(DateFixerControllerUtils.isImageFrame(hbox));
        assertFalse(DateFixerControllerUtils.isImageFrame(new VBox()), "VBox without id must not match");
        assertFalse(DateFixerControllerUtils.isImageFrame(null));
    }

    @Test
    void getImageFrameNodes_filtersAndKeepsOrder() {
        VBox a = frame("a", 1);
        VBox b = frame("b", 2);
        List<Node> input = Arrays.asList(a, new HBox(), new VBox(), b);

        assertEquals(List.of(a, b), DateFixerControllerUtils.getImageFrameNodes(input));
    }

    @Test
    void getImageFrameNodes_emptyAndNull() {
        assertTrue(DateFixerControllerUtils.getImageFrameNodes(Collections.emptyList()).isEmpty());
        assertTrue(DateFixerControllerUtils.getImageFrameNodes(null).isEmpty());
    }

    @Test
    void collectImageFrameInfos_returnsFileInfosOfFrames() {
        VBox a = frame("a", 1);
        VBox b = frame("b", 2);

        List<FileInfo> result = DateFixerControllerUtils.collectImageFrameInfos(List.of(a, new HBox(), b), false);

        assertEquals(2, result.size());
        assertSame(a.getUserData(), result.get(0));
        assertSame(b.getUserData(), result.get(1));
    }

    @Test
    void collectImageFrameInfos_skipsIgnoredWhenRequested() {
        VBox a = frame("a", 1);
        VBox b = frame("b", 2);
        ((FileInfo) a.getUserData()).setIgnored(true);

        assertEquals(1, DateFixerControllerUtils.collectImageFrameInfos(List.of(a, b), true).size());
        assertEquals(2, DateFixerControllerUtils.collectImageFrameInfos(List.of(a, b), false).size());
    }

    @Test
    void collectImageFrameInfos_skipsFramesWithoutFileInfo() {
        VBox noData = new VBox();
        noData.setId("imageFrame");

        assertTrue(DateFixerControllerUtils.collectImageFrameInfos(List.of(noData), false).isEmpty());
    }

    @Test
    void applyDestination_setsFieldsAndResetsCopied() {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setCopied(true);
        Path dest = Paths.get("work", "2020", "file.jpg");

        DateFixerControllerUtils.applyDestination(fileInfo, dest, "workDir", "SERIAL123");

        assertEquals("workDir", fileInfo.getWorkDir());
        assertEquals("SERIAL123", fileInfo.getWorkDirDriveSerialNumber());
        assertEquals(dest.toString(), fileInfo.getDestination_Path());
        assertFalse(fileInfo.isCopied());
    }

    @Test
    void compareByOrgPath_sortsAlphabetically() {
        VBox a = frame("a.jpg", 3);
        VBox b = frame("b.jpg", 2);
        VBox c = frame("c.jpg", 1);
        List<Node> nodes = new ArrayList<>(List.of(c, a, b));

        nodes.sort(DateFixerControllerUtils.compareByOrgPath());

        assertEquals(List.of(a, b, c), nodes);
    }

    @Test
    void compareByDate_sortsAscendingAndHandlesEqualDates() {
        VBox oldest = frame("x", 100);
        VBox middle1 = frame("y", 200);
        VBox middle2 = frame("z", 200);
        VBox newest = frame("w", 300);
        List<Node> nodes = new ArrayList<>(List.of(newest, middle1, oldest, middle2));

        nodes.sort(DateFixerControllerUtils.compareByDate());

        assertSame(oldest, nodes.get(0));
        assertSame(newest, nodes.get(3));
        assertEquals(0, DateFixerControllerUtils.compareByDate().compare(middle1, middle2));
        assertTrue(DateFixerControllerUtils.compareByDate().compare(oldest, newest) < 0);
    }

    @Test
    void panelWidth_returnsShownOrHidden() {
        assertEquals(250, DateFixerControllerUtils.panelWidth(true, 250, -100));
        assertEquals(-100, DateFixerControllerUtils.panelWidth(false, 250, -100));
        assertEquals(0, DateFixerControllerUtils.panelWidth(false, 250, 0));
    }

    @Test
    void arrowRotation_returns180WhenShownAnd0WhenHidden() {
        assertEquals(180, DateFixerControllerUtils.arrowRotation(true));
        assertEquals(0, DateFixerControllerUtils.arrowRotation(false));
    }

    @Test
    void setFixedWidth_setsMinPrefAndMax() {
        Region region = new Region();

        DateFixerControllerUtils.setFixedWidth(region, 250);

        assertEquals(250, region.getMinWidth());
        assertEquals(250, region.getPrefWidth());
        assertEquals(250, region.getMaxWidth());
    }
}
