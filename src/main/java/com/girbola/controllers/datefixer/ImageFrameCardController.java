package com.girbola.controllers.datefixer;

import com.girbola.Main;
import com.girbola.configuration.UIContants;
import com.girbola.controllers.datefixer.utils.DateFixGuiUtils;
import com.girbola.fileinfo.FileInfo;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;

import static common.utils.Conversion.toHumanReadableSize;

/**
 * Controller for ImageFrameCard.fxml. The FXML is the visual template of an image frame;
 * {@link #populate(FileInfo, int)} adds the per-file content and behaviour.
 */
public class ImageFrameCardController {

    private static final String FXML_PATH = "fxml/datefixer/ImageFrameCard.fxml";

    @FXML
    private VBox imageFrame;
    @FXML
    private GridPane topGridPane;
    @FXML
    private Label fileNameLabel;
    @FXML
    private Label imageNumberLabel;
    @FXML
    private Label cameraLabel;
    @FXML
    private Label dimensionsLabel;
    @FXML
    private Label fileSizeLabel;
    @FXML
    private HBox imageViewContainer;
    @FXML
    private ImageView imageView;
    @FXML
    private HBox buttonDateTimeContainer;
    @FXML
    private Button acceptButton;
    @FXML
    private FontIcon acceptIcon;
    @FXML
    private Label fileDateLabel;

    /**
     * Loads a new card from the FXML template and fills it with the given file's data.
     */
    public static VBox create(FileInfo fileInfo, int index) throws IOException {
        URL location = Main.class.getResource(FXML_PATH);
        FXMLLoader loader = new FXMLLoader(location);
        VBox frame = loader.load();
        ImageFrameCardController controller = loader.getController();
        controller.populate(fileInfo, index);
        return frame;
    }

    public void populate(FileInfo fileInfo, int index) {
        fileNameLabel.setText(Paths.get(fileInfo.getOrgPath()).getFileName().toString());
        imageNumberLabel.setText(String.valueOf(index + 1));
        cameraLabel.setText(fileInfo.getCamera_model());
        dimensionsLabel.setText((int) fileInfo.getWidth() + " x " + (int) fileInfo.getHeight());
        fileSizeLabel.setText(toHumanReadableSize(fileInfo.getSize()));

        imageView.setFitWidth(UIContants.THUMBNAIL_MAX_WIDTH);
        imageView.setFitHeight(UIContants.THUMBNAIL_MAX_HEIGHT);

        // The FXML uses a Scene Builder friendly placeholder icon
        acceptIcon.setIconLiteral("far-edit");

        DateFixGuiUtils.configureFileDateLabel(fileDateLabel, fileInfo);
        DateFixGuiUtils.configureAcceptButton(acceptButton, acceptIcon, fileInfo, buttonDateTimeContainer, fileDateLabel);

        // Clicks must reach the frame itself, like in the programmatically built frame
        imageViewContainer.setMouseTransparent(true);
        imageView.setMouseTransparent(true);
        imageNumberLabel.setMouseTransparent(true);
        dimensionsLabel.setMouseTransparent(true);

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(imageViewContainer.widthProperty());
        clip.heightProperty().bind(imageViewContainer.heightProperty());
        imageViewContainer.setClip(clip);
    }
}
