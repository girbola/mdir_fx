package com.girbola;

import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.messages.Messages;
import javafx.application.Platform;
import javafx.stage.Stage;

public class StageControl extends Stage {

    private ModelMain modelMain;
    private Stage primaryStage;


    public StageControl(ModelMain modelMain, Stage primaryStage) {
        this.modelMain = modelMain;
        this.primaryStage = primaryStage;

        primaryStage.setUserData(modelMain);

        primaryStage.setTitle(Main.conf.getProgramName());

//        primaryStage.setMinWidth(800);
//        primaryStage.setMinHeight(600);
//
//        primaryStage.setX(Main.conf.getWindowStartPosX());
//        primaryStage.setY(Main.conf.getWindowStartPosY());
//        primaryStage.setWidth(Main.conf.getWindowStartWidth());
//        primaryStage.setHeight(Main.conf.getWindowStartHeight());

        Messages.sprintf("Configucatiooneonroganerog: " + Main.conf.toString());

        primaryStage.fullScreenProperty().addListener((observable, oldValue, newValue) -> Messages.sprintf("stage fullScreen changed: " + newValue));

        primaryStage.setOnCloseRequest(modelMain.dontExit);

        primaryStage.xProperty().addListener((observable, oldValue, newValue) -> {
            if (Main.conf != null) {
                Platform.runLater(() -> {
                    Main.conf.setWindowStartPosX((double) newValue);
                });
            }
        });

        primaryStage.yProperty().addListener((observable, oldValue, newValue) -> {
            if (Main.conf != null) {
                Platform.runLater(() -> {
                    Main.conf.setWindowStartPosY((double) newValue);
                });
            }
        });
        primaryStage.widthProperty().addListener((observable, oldValue, newValue) -> {
            if (Main.conf != null) {
                Main.conf.setWindowStartWidth((double) newValue);
                int visibles = modelMain.tables().showAndHideTables.getVisibles();

//                if (visibles >= 3) {
//                    return;
//                }
                modelMain.tables().getTables_rootPane().setPrefWidth((double) newValue);

                double sortedWidth = 0;
                double sortItWidth = 0;
                double asitisWidth = 0;
                double hiddenWidths = ((3 - visibles) * 50);

                double divider = Math.floor((double) newValue / (double) visibles);
                divider -= hiddenWidths;

                if (modelMain.tables().getSorted_table().isVisible()) {
                    TableUtils.setHandleDividingTableWidthEqually(modelMain.tables().getSorted_table(), divider);
                    sortedWidth = modelMain.tables().getSorted_table().getPrefWidth();
                } else if (!modelMain.tables().getSorted_table().isVisible()) {
                    Messages.sprintf("Hiding sorted table!!!");
                    TableUtils.setHandleDividingTableWidthEqually(modelMain.tables().getSorted_table(), 35);
                    sortedWidth = modelMain.tables().getSorted_table().getPrefWidth();
                }

                if (modelMain.tables().getSortIt_table().isVisible()) {
                    TableUtils.setHandleDividingTableWidthEqually(modelMain.tables().getSortIt_table(), divider);
                    sortItWidth = modelMain.tables().getSortIt_table().getPrefWidth();

                } else if (!modelMain.tables().getSortIt_table().isVisible()) {
                    Messages.sprintf("Hiding sortit table!!!");
                    TableUtils.setHandleDividingTableWidthEqually(modelMain.tables().getSortIt_table(), 35);
                    sortItWidth = modelMain.tables().getSortIt_table().getPrefWidth();
                }

                if (modelMain.tables().getAsItIs_table().isVisible()) {
                    TableUtils.setHandleDividingTableWidthEqually(modelMain.tables().getAsItIs_table(), divider);
                    asitisWidth = modelMain.tables().getAsItIs_table().getPrefWidth();
                } else if (!modelMain.tables().getAsItIs_table().isVisible()) {
                    Messages.sprintf("Hiding asitis table!!!");
                    TableUtils.setHandleDividingTableWidthEqually(modelMain.tables().getAsItIs_table(), 35);
                    asitisWidth = modelMain.tables().getAsItIs_table().getPrefWidth();
                }

                Messages.sprintf("visibles: " + visibles +
                        " setWindowStartWidth newValue : " + newValue +
                        " sorted: " + sortedWidth +
                        " sortit: " + sortItWidth +
                        " asitis: " + asitisWidth +
                        " divider: " + divider +
                        " hiddenWidths: " + hiddenWidths);
            }
        });

        primaryStage.heightProperty().addListener((observable, oldValue, newValue) -> {
            if (Main.conf != null) {
                Main.conf.setWindowStartHeight((double) newValue);
            }
        });
    }

    public void setStageBoundarys() {
        Messages.sprintf("setStageBoundarys started");
        Platform.runLater(() -> {
            primaryStage.setX(Main.conf.getWindowStartPosX());
            primaryStage.setY(Main.conf.getWindowStartPosY());
            primaryStage.setWidth(Main.conf.getWindowStartWidth());
            primaryStage.setHeight(Main.conf.getWindowStartHeight());
        });
    }

    public int calculateVisibles(ModelMain modelMain) {
        int visibles = 0;
        visibles += getVisibilityValue(modelMain.tables().getSorted_table().isVisible());
        visibles += getVisibilityValue(modelMain.tables().getSortIt_table().isVisible());
        visibles += getVisibilityValue(modelMain.tables().getAsItIs_table().isVisible());
        return visibles;
    }

    private int getVisibilityValue(boolean isVisible) {
        return isVisible ? -1 : 1;
    }

    public void setStageManually() {
        primaryStage.setX(30);
        primaryStage.setY(30);
        primaryStage.setWidth(1024);
        primaryStage.setHeight(700);
    }
}
