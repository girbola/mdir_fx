package com.girbola.controllers.main;

import com.girbola.Main;
import com.girbola.controllers.datefixer.DateFixer;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.messages.Messages;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;

import static com.girbola.messages.Messages.sprintf;

public class TableCell_Folderize extends TableCell<FolderInfo, String> {

    Button dateFixerButton = new Button();

    private ModelMain modelMain;

    public TableCell_Folderize(ModelMain modelMain) {
        this.modelMain = modelMain;
        dateFixerButton.setStyle(null);

        FontIcon fontIcon = new FontIcon();
        fontIcon.setIconLiteral("far-edit");
        fontIcon.setIconSize(20);
        fontIcon.setIconColor(Color.WHITESMOKE);
        dateFixerButton.getStyleClass().add("view_btn");
        dateFixerButton.setDisable(false);

        dateFixerButton.setGraphic(fontIcon);
    }

    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
            setText(null);
        } else {
            FolderInfo folderInfo = getTableView().getItems().get(getIndex());
            if (folderInfo.getFolderFiles() == 0) {
                Platform.runLater(() -> {
                    dateFixerButton.setDisable(true);
                    dateFixerButton.setOnAction(null);
                });
            } else {
                Platform.runLater(() -> {
                    dateFixerButton.setDisable(false);
                });
                Path path = Paths.get(folderInfo.getFolderPath());
                dateFixerButton.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        Messages.sprintf("dateFixerButton clicked");
//						modelMain.getRegisterTableActivity().cancel();
                        modelMain.getMonitorExternalDriveConnectivity().cancel();
                        //Scene scene, FolderInfo aFolderInfo, ModelMain aModel_main, boolean isImporter)
                        new ImportImages(Main.sceneManager.getScene_main(), folderInfo, modelMain, false);

//                        Task<Void> dateFixer = new DateFixer(path, folderInfo, modelMain, false);
//                        Thread dateFixer_th = new Thread(dateFixer, "dateFixer_th");
//                        dateFixer_th.setDaemon(true);
//                        sprintf("dateFixer_th.getName(): " + dateFixer_th.getName());
//                        dateFixer.setOnCancelled(e-> {
//                            Messages.sprintf("datefixer button cancelled");
//                        });
//                        dateFixer.setOnFailed(e-> {
//                            Messages.sprintf("datefixer button failed");
//                        });
//                        dateFixer.setOnSucceeded(e-> {
//                            Messages.sprintf("datefixer button succeeded");
//                        });
//                        dateFixer_th.run();
                    }
                });
            }
            setGraphic(dateFixerButton);
            setText(null);
        }
    }
}
