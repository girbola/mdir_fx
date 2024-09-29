package com.girbola.controllers.datefixer.utils;

import com.girbola.fileinfo.FileInfo;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class DateFixGuiUtilsTest {
    static {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized, this exception can be ignored
        }
    }
    @Test
    void getFileDateLabel_WhenNodeIsNotAVBox() {
        Label result = DateFixGuiUtils.getFileDateLabel(new HBox());
        assertNull(result, "Expected null since the passed node is not a VBox");
    }

    @Test
    void getFileDateLabel_WhenVBoxIdIsNotImageFrame() {
        VBox vBox = new VBox();
        vBox.setId("NotImageFrame");

        Label result = DateFixGuiUtils.getFileDateLabel(vBox);
        assertNull(result, "Expected null since the id of the VBox is not 'imageFrame'");
    }

    @Test
    void getFileDateLabel_WhenVBoxHasNoChildAsHBox() {
        VBox vBox = new VBox();
        vBox.setId("imageFrame");

        Label result = DateFixGuiUtils.getFileDateLabel(vBox);
        assertNull(result, "Expected null since there is no child node of VBox which is an instance of HBox");
    }

    @Test
    void getFileDateLabel_WhenVBoxHasHBoxChildButNoLabelInside() {
        VBox vBox = new VBox();
        vBox.setId("imageFrame");
        vBox.getChildren().add(new HBox());

        Label result = DateFixGuiUtils.getFileDateLabel(vBox);
        assertNull(result, "Expected null since there is no child node of HBox which is an instance of Label");
    }

    @Test
    void getFileDateLabel_WhenVBoxHasHBoxChildWithLabelInside() {
        VBox vBox = new VBox();
        vBox.setId("imageFrame");
        HBox hBox = new HBox();
        hBox.setId("bottomContainer");

        Label label = new Label("jee");
        label.setId("fileName_tf");

        hBox.getChildren().add(label);
        vBox.getChildren().add(hBox);

        Label result = DateFixGuiUtils.getFileDateLabel(vBox);

        assertNotNull(result, "Expected a non-null label instance");
        assertEquals(result.getId(), "fileName_tf", "Expected the ID of the label to be 'fileName_tf'");

    }

    //New test methods begins here
    @Test
    void createImageView_WhenFileInfoIsImage() {
        Path fileName = Paths.get("src", "test", "resources", "test-material", "milky-way-559641_640.jpg");

        FileInfo fi = new FileInfo();
        fi.setRaw(false);
        fi.setImage(true);
        fi.setVideo(false);
        fi.setOrgPath(fileName.toString());

        Image image = new Image(fileName.toUri().toString());
        ImageView result = DateFixGuiUtils.createImageView(fi, 200.0, 200.0);
        result.setImage(image);

        assertNotNull(result.getImage(), "Expected a non-null ImageView instance");
        assertEquals(200.0, result.getFitWidth(), "Expected width to be same as set in method parameter");
        assertEquals(200.0, result.getFitHeight(), "Expected height to be same as set in method parameter");
        assertEquals("imageView", result.getId(), "Expected ID of created ImageView to be 'imageView'");
    }

    @Test
    void createImageView_WhenFileInfoIsNotImage() {
        Path fileName = Paths.get("src", "test", "resources", "test-material", "153976-817104245_tiny.mp4");

        FileInfo fi = new FileInfo();
        fi.setRaw(false);
        fi.setImage(false);
        fi.setVideo(true);
        fi.setOrgPath("/path/to/test/file.mp4");

        ImageView result = DateFixGuiUtils.createImageView(fi, 200.0, 200.0);
        assertNotNull(result, "Expected a non-null ImageView instance");
        assertEquals(200.0, result.getFitWidth(), "Expected width to be same as set in method parameter");
        assertEquals(200.0, result.getFitHeight(), "Expected height to be same as set in method parameter");
        assertEquals("imageView", result.getId(), "Expected ID of created ImageView to be 'imageView'");
    }

    @Test
    void createImageView_WhenFileInfoIsRaw() {
        FileInfo fi = new FileInfo();
        fi.setRaw(true);
        fi.setImage(false);
        fi.setVideo(false);
        fi.setOrgPath("/path/to/test/file.raw");

        ImageView result = DateFixGuiUtils.createImageView(fi, 200.0, 200.0);
        assertNotNull(result, "Expected a non-null ImageView instance");
        assertEquals(200.0, result.getFitWidth(), "Expected width to be same as set in method parameter");
        assertEquals(200.0, result.getFitHeight(), "Expected height to be same as set in method parameter");
        assertEquals("imageView", result.getId(), "Expected ID of created ImageView to be 'imageView'");
    }

    //New test methods ends here
}
