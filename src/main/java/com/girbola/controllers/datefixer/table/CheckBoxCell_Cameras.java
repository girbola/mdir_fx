
package com.girbola.controllers.datefixer.table;

import com.girbola.controllers.datefixer.ModelDatefix;
import com.girbola.controllers.datefixer.utils.MetadataField;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

import static com.girbola.messages.Messages.sprintf;


public class CheckBoxCell_Cameras extends TableCell<EXIF_Data_Selector, Boolean> {
    private static final String ERROR = CheckBoxCell_Cameras.class.getSimpleName();
    private CheckBox checkBox = new CheckBox();
    private final ModelDatefix modelDateFix;

    public CheckBoxCell_Cameras(ModelDatefix modelDateFix) {
        this.modelDateFix = modelDateFix;
        initializeCheckBox();
    }

    @Override
    protected void updateItem(Boolean item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
            setText(null);
        } else {
            checkBox.setSelected(item);
            setText(null);
            setGraphic(checkBox);
        }
    }

    private void initializeCheckBox() {
        //checkBox.setSelected(getValue());
        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            Task<ObservableList<Node>> updateCameraTask = createUpdateCameraTask(newValue);
            updateCameraTask.setOnCancelled(event -> {
                enableTables(false);
                Messages.sprintf("updateCameraTask cancelled");
            });
            updateCameraTask.setOnFailed(event -> {
                enableTables(false);
                Messages.sprintf("updateCameraTask failed");
            });
            updateCameraTask.setOnSucceeded((WorkerStateEvent event) -> {
                ObservableList<Node> updatedList = updateCameraTask.getValue();
                modelDateFix.getTilePane().getChildren().setAll(updatedList);
                enableTables(false);
            });
            Thread updateCameraTaskThread = new Thread(updateCameraTask, "updateCameraTask Thread");
            updateCameraTaskThread.start();
        });
    }

    private Task<ObservableList<Node>> createUpdateCameraTask(boolean newValue) {
        return new Task<>() {
            @Override
            protected ObservableList<Node> call() {
                enableTables(true);
                EXIF_Data_Selector cameraData = getTableView().getItems().get(getIndex());
                checkBox.selectedProperty().bindBidirectional(cameraData.isShowing_property());
                //cameraData.setIsShowing(newValue);
                return updateCameraList(newValue);
            }
        };
    }

    private ObservableList<Node> updateCameraList(boolean newValue) {
        ObservableList<Node> nodeList = FXCollections.observableArrayList();
        List<String> cameraList = new ArrayList<>();
        ObservableList<Node> selectionList = modelDateFix.getSelectionModel().getSelectionList();
        for(Node node : selectionList) {
            FileInfo fileInfo = (FileInfo) node.getUserData();
            if(fileInfo.getCamera_model() != null) {

            }
            Messages.sprintf("selectionList node: " + node);

        }

        for (EXIF_Data_Selector cameras : modelDateFix.getCameras_TableView().getItems()) {
            if (cameras.isShowing() == newValue) {
                String info = cameras.getInfo();
                cameraList.add(info);
            }
        }

        if (cameraList.isEmpty()) {
            return nodeList;
        }

        for (Node node : modelDateFix.getTilePane().getChildren()) {
            if (node instanceof VBox && "imageFrame".equals(node.getId())) {
                FileInfo fileInfo = (FileInfo) node.getUserData();
                initializeCameraModel(fileInfo);
                if (cameraList.contains(fileInfo.getCamera_model())) {
                    nodeList.add(node);
                    if (newValue) {
                        modelDateFix.getSelectionModel().addOnly(node);
                    } else {
                        modelDateFix.getSelectionModel().remove(node);
                    }
                }
            }
        }
        return nodeList;
    }

    private void initializeCameraModel(FileInfo fileInfo) {
        if (fileInfo.getCamera_model() == null || fileInfo.getCamera_model().isEmpty()) {
            fileInfo.setCamera_model(MetadataField.UNKNOWN.getType());
        }
    }

    private void enableTables(boolean value) {
        modelDateFix.getCameras_TableView().setDisable(value);
        modelDateFix.getDates_TableView().setDisable(value);
        modelDateFix.getEvents_TableView().setDisable(value);
        modelDateFix.getLocations_TableView().setDisable(value);
    }

    private Boolean getValue() {
        return getItem() != null && getItem();
    }

}
