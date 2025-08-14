
package com.girbola.controllers.closerlook;

import com.girbola.configuration.UIContants;
import com.girbola.controllers.datefixer.RenderVisibleNode;
import com.girbola.controllers.misc.DateCollectionUtils;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.girbola.rotate.Rotate.rotate;

//import static com.girbola.rotate.Rotate.rotate;


public class CloserLookController {
    private RenderVisibleNode renderVisibleNode;
    private List<FileInfo> current_fileInfo;
    private Connection connection;

    //@formatter:off
   @FXML private VBox images_root;
   @FXML private Button viewByDate;
   @FXML private Button viewByHour;
   @FXML private Button viewByMin;
   @FXML private ScrollPane scrollPane;
	//@formatter:on

    @FXML
    private void viewByDate_action(ActionEvent event) {
        images_root.getChildren().clear();
        scrollPane.setVvalue(0);
        Map<String, List<FileInfo>> dateList = DateCollectionUtils.getByDay(current_fileInfo);
        drawNodes(dateList);
    }

    @FXML
    private void viewByHour_action(ActionEvent event) {
        images_root.getChildren().clear();
        scrollPane.setVvalue(0);
        Map<String, List<FileInfo>> dateList = DateCollectionUtils.getByHour(current_fileInfo);
        drawNodes(dateList);
    }

    @FXML
    private void viewByMinute_action(ActionEvent event) {
        images_root.getChildren().clear();
        scrollPane.setVvalue(0);
        Map<String, List<FileInfo>> dateList = DateCollectionUtils.getByMin(current_fileInfo);
        drawNodes(dateList);
    }

    public void init(List<FileInfo> list, Path thumbFilePath, Connection connection) {
        this.current_fileInfo = list;
        this.connection = connection;

        renderVisibleNode = new RenderVisibleNode(scrollPane, thumbFilePath, this.connection);

        // Map<String, List<FileInfo>> dateList =
        // DateCollectionUtils.getByHour(current_fileInfo);
        images_root.setId("closerLook");

        /*
         * 2018 08 07 19:00:00 2018 08 07 20:00:00
         */
		/*	Task<Map<String, List<FileInfo>>> task = new Task<Map<String, List<FileInfo>>>() {
		
				@Override
				protected Map<String, List<FileInfo>> call() throws Exception {
					Map<String, List<FileInfo>> map = DateCollectionUtils.getByHour(current_fileInfo);
					return map;
				}
		
			};
			task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
		
				@Override
				public void handle(WorkerStateEvent event) {
					try {
						
					} catch (Exception e) {
						Messages.sprintf("Something went wrong with closerlook");
					}
				}
			});
			Thread thread = new Thread(task, "CloserlookTask");
			thread.start();
			*/
        Map<String, List<FileInfo>> dateList = DateCollectionUtils.getByHour(current_fileInfo);

        drawNodes(dateList);
    }

    private ImageView createImageView(FileInfo fi, double width, double height) {
        ImageView iv = new ImageView();
        iv.setFitWidth(Math.floor(width / 3));
        iv.setFitHeight(Math.floor(height / 3));
        iv.maxWidth(Math.floor(width / 3));
        iv.maxHeight(Math.floor(height / 3));
        iv.setPreserveRatio(true);
        iv.setMouseTransparent(true);
        iv.setRotate(rotate(fi.getOrientation()));
        iv.setId("imageView");
        return iv;
    }

    private StackPane createStackPane(FileInfo fileInfo, double width, double height) {
        StackPane stackPane = new StackPane();
        stackPane.setAlignment(Pos.CENTER);
        stackPane.setId("imageFrame");
        stackPane.getStyleClass().add("imageFrame");
        stackPane.setUserData(fileInfo);
        stackPane.setMaxSize(Math.floor(width / 3), Math.floor(height / 3));
        stackPane.setMinSize(Math.floor(width / 3), Math.floor(height / 3));
        stackPane.setPrefSize(Math.floor(width / 3), Math.floor(height / 3));
        return stackPane;
    }

    private void drawNodes(Map<String, List<FileInfo>> list) {
        Messages.sprintf("Drawing nodes: " + list.size());
        if (!list.isEmpty()) {
            renderVisibleNode.stopTimeLine();

            for (Entry<String, List<FileInfo>> entry : list.entrySet()) {
                Messages.sprintf("Date: " + entry.getKey());
                TilePane tilePane = new TilePane();
                tilePane.setId("tilePane");
                tilePane.setStyle("-fx-background-color: black;" + " -fx-border-color: white;");
                Pane datePane = new Pane(new Label(entry.getKey() + ":00"));
                tilePane.getChildren().add(datePane);
                for (FileInfo fi : entry.getValue()) {
                    Messages.sprintf("------FIIIIII: " + fi);
                    ImageView iv = createImageView(fi, UIContants.THUMBNAIL_MAX_WIDTH - 2, UIContants.THUMBNAIL_MAX_HEIGHT - 2);
                    StackPane pane = createStackPane(fi, UIContants.THUMBNAIL_MAX_WIDTH, UIContants.THUMBNAIL_MAX_HEIGHT);
                    pane.getChildren().add(iv);
                    tilePane.getChildren().add(pane);
                }

                images_root.getChildren().add(tilePane);
            }
        } else {
            Messages.sprintf("dateList were empty");
        }
        renderVisibleNode.startTimeLine();

    }

}
