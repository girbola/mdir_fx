package com.girbola;

import com.girbola.controllers.main.Model_main;
import com.girbola.messages.Messages;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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

        primaryStage.fullScreenProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
                                Boolean newValue) {
                Messages.sprintf("stage fullScreen changed: " + newValue);
            }
        });

        primaryStage.setOnCloseRequest(modelMain.dontExit);


        primaryStage.xProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue,
                                Number newValue) {
                if (Main.conf != null) {
                    Main.conf.setWindowStartPosX((double) newValue);
                    Messages.sprintf("windowstartposX: " + newValue);
                }
            }
        });

        primaryStage.yProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue,
                                Number newValue) {
                if (Main.conf != null) {
                    Main.conf.setWindowStartPosY((double) newValue);
                   Messages.sprintf("windowstartposY: " + newValue);
                }
            }
        });

        primaryStage.widthProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue,
                                Number newValue) {
                if (Main.conf != null) {
                    Main.conf.setWindowStartWidth((double) newValue);
                    Messages.sprintf("setWindowStartWidth: " + newValue);
                }
            }
        });

        primaryStage.heightProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue,
                                Number newValue) {
                if (Main.conf != null) {
                    Main.conf.setWindowStartHeight((double) newValue);
                    Messages.sprintf("setWindowStartHeight: " + newValue);
                }
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

    public void setStageManually() {
        primaryStage.setX(30);
        primaryStage.setY(30);
        primaryStage.setWidth(1024);
        primaryStage.setHeight(700);
    }
}
