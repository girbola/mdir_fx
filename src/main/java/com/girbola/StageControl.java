package com.girbola;

import com.girbola.controllers.main.Model_main;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.messages.Messages;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.stage.Stage;

public class StageControl extends Stage {

    private Model_main modelMain;
    private Stage primaryStage;


    public StageControl(Model_main modelMain, Stage primaryStage) {
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
                Main.conf.setWindowStartPosX((double) newValue);
                Messages.sprintf("windowstartposX: " + newValue);
            }
        });

        primaryStage.yProperty().addListener((observable, oldValue, newValue) -> {
            if (Main.conf != null) {
                Main.conf.setWindowStartPosY((double) newValue);
                Messages.sprintf("windowstartposY: " + newValue);
            }
        });

        primaryStage.widthProperty().addListener((observable, oldValue, newValue) -> {
            if (Main.conf != null) {
                Main.conf.setWindowStartWidth((double) newValue);
                int visibles = modelMain.tables().showAndHideTables.getVisibles();

                double sortedWidth = -1;
                double sortItWidth = -1;
                double asitisWidth = -1;
                double hiddenWidths = ((3 - visibles) * 50);
                double bWidth = modelMain.tables().getTables_rootPane().getPrefWidth();
                double divider = Math.floor(((double) bWidth) / (double) visibles);
                divider -= (hiddenWidths);

                if (modelMain.tables().showAndHideTables.getSorted_show_property().get()) {
                    TableUtils.setHandleDividingTableWidthEqually(modelMain.tables().getSorted_table(), divider);
                    sortedWidth = modelMain.tables().getSorted_table().getPrefWidth();
                } else if (!modelMain.tables().showAndHideTables.getSorted_show_property().get()) {
                    Messages.sprintf("Hiding sorted table!!!");
                    TableUtils.setHandleDividingTableWidthEqually(modelMain.tables().getSorted_table(), 35);
                    sortedWidth = modelMain.tables().getSorted_table().getPrefWidth();
                }

                if (modelMain.tables().showAndHideTables.getSortit_show_property().get()) {
                    TableUtils.setHandleDividingTableWidthEqually(modelMain.tables().getSortIt_table(), divider);
                    sortItWidth = modelMain.tables().getSortIt_table().getPrefWidth();

                } else if (!modelMain.tables().showAndHideTables.getSortit_show_property().get()) {
                    Messages.sprintf("Hiding sortit table!!!");
                    TableUtils.setHandleDividingTableWidthEqually(modelMain.tables().getSortIt_table(), 35);
                    sortItWidth = modelMain.tables().getSortIt_table().getPrefWidth();
                }

                if (modelMain.tables().showAndHideTables.getAsitis_show_property().get()) {
                    TableUtils.setHandleDividingTableWidthEqually(modelMain.tables().getAsItIs_table(), divider);
                    asitisWidth = modelMain.tables().getAsItIs_table().getPrefWidth();
                } else if (!modelMain.tables().showAndHideTables.getAsitis_show_property().get()) {
                    Messages.sprintf("Hiding asitis table!!!");
                    TableUtils.setHandleDividingTableWidthEqually(modelMain.tables().getAsItIs_table(), 35);
                    asitisWidth = modelMain.tables().getAsItIs_table().getPrefWidth();

                }

                Messages.sprintf(visibles + " setWindowStartWidth newValue : " + newValue + " bWidth " + bWidth + " sorted: " + sortedWidth + " sortit: " + sortItWidth + " asitis: " + asitisWidth + " divider: " + divider + " hiddenWidths: " + hiddenWidths + " modelMain.tables().getSorted_table().getParent(); " + modelMain.tables().getSorted_table().getParent().getParent().getParent().getId());
            }
        });

        primaryStage.heightProperty().addListener((observable, oldValue, newValue) -> {
            if (Main.conf != null) {
                Main.conf.setWindowStartHeight((double) newValue);
                Messages.sprintf("setWindowStartHeight: " + newValue);
            }

        });
    }

    public void setStageBoundarys() {
        primaryStage.setX(Main.conf.getWindowStartPosX());
        primaryStage.setY(Main.conf.getWindowStartPosY());
        primaryStage.setWidth(Main.conf.getWindowStartWidth());
        primaryStage.setHeight(Main.conf.getWindowStartHeight());
        Messages.sprintf("AEORGAEOTJHAETh:" + Main.conf.toString());
    }

    public int calculateVisibles(Model_main modelMain) {
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
