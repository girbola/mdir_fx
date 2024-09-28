package com.girbola.controllers.datefixer;

import com.girbola.Main;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

import java.util.Iterator;

public class SelectByTableModel extends Task<Boolean> {

    //	private ObservableList<Node> children;
    private String type;
    private TableView<EXIF_Data_Selector> tableView;
    private Model_datefix model_datefix;

    public SelectByTableModel(Model_datefix model_datefix, String type, TableView<EXIF_Data_Selector> aTableView) {
        super();
        this.model_datefix = model_datefix;
        this.type = type;
        this.tableView = aTableView;
    }

    @Override
    protected Boolean call() throws Exception {
        Iterator<Node> it = model_datefix.getTilePane().getChildren().iterator();
        Messages.sprintf("===============" + type + " tableView");
        while (it.hasNext()) {
            Node node = it.next();
            if (node instanceof VBox && node.getId().equals("imageFrame")) {
                FileInfo fileInfo = (FileInfo) node.getUserData();
                if (hasNewValue(fileInfo)) {
                    Platform.runLater(() -> {
                        model_datefix.getSelectionModel().addOnly(node);
                    });
                } else {
                    Platform.runLater(() -> {
                        model_datefix.getSelectionModel().remove(node);
                    });
                }
            }
        }
        return null;
    }

    private boolean hasNewValue(FileInfo fileInfo) {
        Iterator<EXIF_Data_Selector> it = tableView.getSelectionModel().getSelectedItems().iterator();
        SelectorModelType sbt = SelectorModelType.valueOf(type);
        String dataModel = "";
        switch (sbt) {
            case CAMERA:
                dataModel = fileInfo.getCamera_model();
                break;
            case EVENT:
                dataModel = fileInfo.getEvent();
                break;
            case DATE:
                dataModel = Main.simpleDates.getSdf_ymd_minus().format(fileInfo.getDate());
                break;
            case LOCATION:
                dataModel = fileInfo.getLocation();
                break;
        }

        while (it.hasNext()) {
            EXIF_Data_Selector eds = it.next();
            if (eds.getInfo().equals(dataModel)) {
                return true;
            }
        }
        return false;
    }

}
